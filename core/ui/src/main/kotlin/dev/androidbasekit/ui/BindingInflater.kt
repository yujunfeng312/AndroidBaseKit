package dev.androidbasekit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding 创建工厂。Java 直接传 XxxBinding::inflate，Kotlin 传 XxxBinding::inflate。
 * BaseActivity/BaseFragment 统一传 attachToParent=false，由窗口或 FragmentManager 挂载根 View。
 * 只捕获 Binding 的静态工厂，不捕获 Activity、Fragment 或旧 View。
 */
fun interface BindingInflater<VB : ViewBinding> {
    fun inflate(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): VB
}
