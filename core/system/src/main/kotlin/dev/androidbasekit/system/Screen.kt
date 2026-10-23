package dev.androidbasekit.system

import android.content.Context
import android.util.TypedValue
import android.app.Activity
import android.graphics.Rect
import androidx.core.view.ViewCompat

/**
 * 屏幕尺寸与单位换算工具。提供 dp/sp 转 px 和已布局窗口边界；多窗口下不假定设备屏幕就是可用窗口。
 */
object Screen {
    @JvmStatic fun dp(context: Context, value: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics).toInt()
    @JvmStatic fun sp(context: Context, value: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.resources.displayMetrics)
    /** Actual laid-out window bounds, including multi-window; call after the decor view is laid out. */
    @JvmStatic fun windowBounds(activity: Activity): Rect {
        val decor = activity.window.decorView
        return Rect(0, 0, decor.width, decor.height)
    }
}
