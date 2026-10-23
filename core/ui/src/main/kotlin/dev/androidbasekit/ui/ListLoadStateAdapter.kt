package dev.androidbasekit.ui

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

/**
 * Use RecyclerView ConcatAdapter with the business ListAdapter; existing data remains visible.
 *
 * 列表状态 Adapter。配合 ConcatAdapter 展示加载、空数据或错误重试行，不清空业务 ListAdapter 的数据。
 */
class ListLoadStateAdapter(private val retryAction: Runnable) : RecyclerView.Adapter<ListLoadStateAdapter.Holder>() {
    private var state = PageState.content()
    /**
     * 列表行 ViewHolder。保存本行视图或 Binding，配合所属 Adapter 完成复用与绑定。
     */
    class Holder(val button: Button) : RecyclerView.ViewHolder(button)
    fun submitState(value: PageState) {
        if (state == value) return
        val before = itemCount
        state = value
        when {
            before == 0 && itemCount == 1 -> notifyItemInserted(0)
            before == 1 && itemCount == 0 -> notifyItemRemoved(0)
            before == 1 -> notifyItemChanged(0)
        }
    }
    override fun getItemCount(): Int = if (state.status == PageStatus.CONTENT) 0 else 1
    override fun onCreateViewHolder(parent: ViewGroup, type: Int) = Holder(Button(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    })
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.button.isEnabled = state.status == PageStatus.ERROR
        holder.button.setText(if (state.messageRes != 0) state.messageRes else when (state.status) {
            PageStatus.LOADING -> R.string.basekit_loading
            PageStatus.EMPTY -> R.string.basekit_empty
            else -> R.string.basekit_retry
        })
        holder.button.setOnClickListener { retryAction.run() }
    }
}
