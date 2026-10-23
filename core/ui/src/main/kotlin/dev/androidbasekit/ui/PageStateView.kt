package dev.androidbasekit.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*

/** 页面状态展示：统一加载、空数据和错误反馈，内容绑定与重试协议保持不变。 */
class PageStateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val progress: ProgressBar
    private val icon: ImageView
    private val message: TextView
    private val hint: TextView
    private val retry: Button
    private var content: View? = null

    init {
        orientation = VERTICAL
        gravity = android.view.Gravity.CENTER
        inflate(context, R.layout.basekit_page_state_content, this)
        progress = findViewById(R.id.basekit_state_progress)
        icon = findViewById(R.id.basekit_state_icon)
        message = findViewById(R.id.basekit_state_message)
        hint = findViewById(R.id.basekit_state_hint)
        retry = findViewById(R.id.basekit_state_retry)
    }

    fun attachContent(view: View) { content = view }
    fun setRetryAction(action: Runnable) { retry.setOnClickListener { action.run() } }

    fun render(state: PageState) {
        visibility = if (state.status == PageStatus.CONTENT) GONE else VISIBLE
        content?.visibility = if (state.status == PageStatus.CONTENT) VISIBLE else GONE
        progress.visibility = if (state.status == PageStatus.LOADING) VISIBLE else GONE
        icon.visibility = if (state.status == PageStatus.EMPTY || state.status == PageStatus.ERROR) VISIBLE else GONE
        icon.setImageResource(if (state.status == PageStatus.EMPTY) R.drawable.basekit_state_empty else R.drawable.basekit_state_error)
        retry.visibility = if (state.status == PageStatus.ERROR) VISIBLE else GONE
        message.setText(if (state.messageRes != 0) state.messageRes else when (state.status) {
            PageStatus.LOADING -> R.string.basekit_loading
            PageStatus.EMPTY -> R.string.basekit_empty
            else -> R.string.basekit_error
        })
        hint.setText(when (state.status) {
            PageStatus.LOADING -> R.string.basekit_loading_hint
            PageStatus.EMPTY -> R.string.basekit_empty_hint
            else -> R.string.basekit_error_hint
        })
    }
}
