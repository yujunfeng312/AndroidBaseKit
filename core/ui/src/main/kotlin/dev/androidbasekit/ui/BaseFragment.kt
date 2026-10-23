package dev.androidbasekit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding Fragment 基类。接收 XxxBinding::inflate，统一使用当前容器和 attachToParent=false 创建视图。
 * onDestroyView 释放 Binding；观察 UI 数据使用 viewLifecycleOwner。保留无参构造与自定义 inflateBinding 重写。
 * 具体 Fragment 需提供公开无参构造，并在其中传入工厂，支持系统重建。
 */
abstract class BaseFragment<VB : ViewBinding> @JvmOverloads constructor(
    private val bindingInflater: BindingInflater<VB>? = null
) : Fragment() {
    private var viewBinding: VB? = null
    protected val binding: VB get() = checkNotNull(viewBinding) { "Binding only valid between onCreateView and onDestroyView" }
    protected open fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB = checkNotNull(bindingInflater) {
        "Pass XxxBinding::inflate to BaseFragment or override inflateBinding"
    }.inflate(inflater, container, false)
    protected abstract fun onBindingCreated(savedInstanceState: Bundle?)
    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        return inflateBinding(inflater, container).also { viewBinding = it }.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState); onBindingCreated(savedInstanceState)
    }
    override fun onDestroyView() { viewBinding = null; super.onDestroyView() }
}
