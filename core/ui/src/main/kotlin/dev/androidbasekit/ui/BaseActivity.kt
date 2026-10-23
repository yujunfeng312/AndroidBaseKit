package dev.androidbasekit.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding Activity 基类。传入 XxxBinding::inflate 后统一创建 Binding、设置根布局并调用初始化钩子。
 * 保留无参构造和 inflateBinding 重写作为自定义布局兼容入口；两种方式至少提供一种。
 */
abstract class BaseActivity<VB : ViewBinding> @JvmOverloads constructor(
    private val bindingInflater: BindingInflater<VB>? = null
) : AppCompatActivity() {
    protected lateinit var binding: VB
        private set
    protected open fun inflateBinding(inflater: LayoutInflater): VB = checkNotNull(bindingInflater) {
        "Pass XxxBinding::inflate to BaseActivity or override inflateBinding"
    }.inflate(inflater, null, false)
    protected abstract fun onBindingCreated(savedInstanceState: Bundle?)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        onBindingCreated(savedInstanceState)
    }
}
