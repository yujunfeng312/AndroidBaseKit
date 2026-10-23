package dev.androidbasekit.ui

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

/**
 * 简短 UI 反馈入口。通过字符串资源显示 Toast/Snackbar，调用方在主线程使用当前页面 View。
 */
object UiFeedback {
    @JvmStatic fun toast(view: View, @StringRes message: Int) { Toast.makeText(view.context.applicationContext, message, Toast.LENGTH_SHORT).show() }
    @JvmStatic fun snackbar(view: View, @StringRes message: Int) { Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show() }
}
/**
 * View 点击防抖工具。每个监听器独立使用单调时钟控制点击间隔；重新绑定监听器会重置防抖状态。
 */
object Clicks {
    @JvmStatic @JvmOverloads fun setDebounced(view: View, action: Runnable, intervalMillis: Long = 600) {
        require(intervalMillis >= 0)
        var last: Long? = null
        view.setOnClickListener {
            val now = android.os.SystemClock.elapsedRealtime()
            if (last == null || now - last!! >= intervalMillis) { last = now; action.run() }
        }
    }
}
