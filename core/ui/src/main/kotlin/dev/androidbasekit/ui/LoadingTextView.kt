package dev.androidbasekit.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.util.AttributeSet
import androidx.annotation.MainThread
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable

/**
 * A clickable TextView with UI-only loading state. Requests remain owned by ViewModel/Repository.
 *
 * 可点击加载文本控件。支持点击自动 Loading、防重复点击、文字/图标恢复及可配置指示器；隐藏或移除时暂停动画，不执行网络请求。
 */
class LoadingTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var loading = false
    private var updatingLabel = false
    private var idleText: CharSequence = ""
    private var idleBufferType = BufferType.NORMAL
    private var idleEnabled = true
    private var idleMinWidth = 0
    private var idleDrawablePadding = 0
    private var idleDrawables: Array<Drawable?>? = null
    private var idleStateDescription: CharSequence? = null
    private var spinner: CircularProgressDrawable? = null
    private var indicatorSize = 0
    private var indicatorPadding = 0
    private var indicatorColor: Int? = null
    private var loadingLabel: CharSequence? = null

    val isLoading: Boolean get() = loading
    var autoLoadingOnClick: Boolean = true
    var loadingText: CharSequence
        get() = loadingLabel ?: resources.getText(R.string.basekit_loading)
        @MainThread set(value) {
            loadingLabel = value
            if (loading) {
                updateLabel(value, BufferType.NORMAL)
                ViewCompat.setStateDescription(this, value)
            }
        }

    init {
        idleText = text
        idleEnabled = isEnabled
        idleBufferType = when (text) {
            is Editable -> BufferType.EDITABLE
            is Spannable -> BufferType.SPANNABLE
            else -> BufferType.NORMAL
        }
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.LoadingTextView, defStyleAttr, 0)
        try {
            loadingLabel = attributes.getText(R.styleable.LoadingTextView_ltv_loadingText)
            indicatorSize = attributes.getDimensionPixelSize(R.styleable.LoadingTextView_ltv_indicatorSize,
                resources.getDimensionPixelSize(R.dimen.basekit_loading_indicator_size))
            indicatorPadding = attributes.getDimensionPixelSize(R.styleable.LoadingTextView_ltv_indicatorPadding,
                resources.getDimensionPixelSize(R.dimen.basekit_loading_indicator_padding))
            if (attributes.hasValue(R.styleable.LoadingTextView_ltv_indicatorColor)) {
                indicatorColor = attributes.getColor(R.styleable.LoadingTextView_ltv_indicatorColor, currentTextColor)
            }
            autoLoadingOnClick = attributes.getBoolean(R.styleable.LoadingTextView_ltv_autoLoadingOnClick, true)
        } finally { attributes.recycle() }
        require(indicatorSize > 0 && indicatorPadding >= 0)
    }

    @MainThread fun setLoading(value: Boolean) {
        if (loading == value) { updateAnimation(); return }
        if (value) {
            idleText = text
            idleEnabled = isEnabled
            idleMinWidth = minimumWidth
            idleDrawablePadding = compoundDrawablePadding
            idleDrawables = compoundDrawablesRelative.copyOf()
            idleStateDescription = ViewCompat.getStateDescription(this)
            loading = true
            // Keep a laid-out wrap_content control from shrinking when the label changes.
            if (isLaidOut) minimumWidth = maxOf(minimumWidth, width)
            val drawable = spinner ?: CircularProgressDrawable(context).also { spinner = it }
            val stroke = minOf(resources.getDimension(R.dimen.basekit_loading_indicator_stroke), indicatorSize / 4f)
            drawable.strokeWidth = stroke
            drawable.centerRadius = (indicatorSize - 2f * stroke) / 2f
            drawable.setColorSchemeColors(indicatorColor ?: currentTextColor)
            drawable.setBounds(0, 0, indicatorSize, indicatorSize)
            setCompoundDrawablesRelative(drawable, null, null, null)
            compoundDrawablePadding = indicatorPadding
            updateLabel(loadingText, BufferType.NORMAL)
            super.setEnabled(false)
            ViewCompat.setStateDescription(this, loadingText)
        } else {
            loading = false
            spinner?.stop()
            idleDrawables?.let { setCompoundDrawablesRelative(it[0], it[1], it[2], it[3]) }
            idleDrawables = null
            compoundDrawablePadding = idleDrawablePadding
            minimumWidth = idleMinWidth
            updateLabel(idleText, idleBufferType)
            super.setEnabled(idleEnabled)
            ViewCompat.setStateDescription(this, idleStateDescription)
            idleStateDescription = null
        }
        updateAnimation()
    }

    @MainThread fun showLoading() = setLoading(true)
    @MainThread fun hideLoading() = setLoading(false)

    override fun performClick(): Boolean {
        if (loading || !isEnabled) return false
        val automatic = autoLoadingOnClick && hasOnClickListeners()
        if (automatic) showLoading()
        var handled = false
        try {
            handled = super.performClick()
            return handled
        } finally {
            // A throwing/unhandled listener must not leave the control permanently busy.
            if (automatic && !handled) hideLoading()
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (loading && !updatingLabel) {
            idleText = text ?: ""
            idleBufferType = type ?: BufferType.NORMAL
            return
        }
        super.setText(text, type ?: BufferType.NORMAL)
        if (!updatingLabel) {
            idleText = getText()
            idleBufferType = type ?: BufferType.NORMAL
        }
    }

    override fun setEnabled(enabled: Boolean) {
        idleEnabled = enabled
        // Remember the desired enabled state without reopening clicks during loading.
        if (!loading) super.setEnabled(enabled)
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); updateAnimation() }
    override fun onDetachedFromWindow() { spinner?.stop(); super.onDetachedFromWindow() }
    override fun onVisibilityAggregated(isVisible: Boolean) { super.onVisibilityAggregated(isVisible); updateAnimation() }
    override fun onWindowVisibilityChanged(visibility: Int) { super.onWindowVisibilityChanged(visibility); updateAnimation() }

    private fun updateLabel(value: CharSequence, type: BufferType) {
        updatingLabel = true
        try { setText(value, type) } finally { updatingLabel = false }
    }
    private fun updateAnimation() {
        val drawable = spinner ?: return
        if (loading && isAttachedToWindow && isShown && windowVisibility == VISIBLE) {
            if (!drawable.isRunning) drawable.start()
        } else if (drawable.isRunning) drawable.stop()
    }
}
