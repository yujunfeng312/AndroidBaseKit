package dev.androidbasekit.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment

/**
 * A dialog keeps its views within the view lifecycle. Subclasses must release their own bindings.
 *
 * DialogFragment 内容视图基类。子类实现 createContentView，并自行在视图生命周期结束时释放 Binding。
 */
abstract class BaseDialogFragment : DialogFragment() {
    protected abstract fun createContentView(inflater: LayoutInflater, container: ViewGroup?): View
    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View = createContentView(inflater, container)
}
