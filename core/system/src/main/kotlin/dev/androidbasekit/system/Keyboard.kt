package dev.androidbasekit.system

import android.view.View
import androidx.core.view.WindowInsetsControllerCompat
import android.content.Context
import android.content.ContextWrapper
import android.app.Activity
import androidx.core.view.WindowInsetsCompat

/**
 * 软键盘显示/隐藏入口。要求 View 使用 Activity Context；显示前请求焦点，使用窗口 Insets 控制 IME。
 */
object Keyboard {
    @JvmStatic fun show(view: View) { view.requestFocus(); view.post { WindowInsetsControllerCompat(view.context.findActivity().window, view)?.show(WindowInsetsCompat.Type.ime()) } }
    @JvmStatic fun hide(view: View) { WindowInsetsControllerCompat(view.context.findActivity().window, view)?.hide(WindowInsetsCompat.Type.ime()) }
}

private fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("Keyboard requires an Activity-backed view")
}
