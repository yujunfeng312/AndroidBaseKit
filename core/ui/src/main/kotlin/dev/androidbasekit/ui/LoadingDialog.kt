package dev.androidbasekit.ui

import android.view.*

/**
 * 简单加载对话框。基于 BaseDialogFragment 显示进度视图，由页面按自身生命周期展示和关闭。
 */
class LoadingDialog : BaseDialogFragment() {
    override fun createContentView(inflater: LayoutInflater, container: ViewGroup?): View = inflater.inflate(R.layout.basekit_loading_dialog, container, false)
}
