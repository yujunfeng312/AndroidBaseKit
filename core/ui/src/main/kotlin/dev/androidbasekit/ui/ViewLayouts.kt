package dev.androidbasekit.ui

import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.core.view.OneShotPreDrawListener

/** 主线程布局操作；尺寸均为 px，宽高也接受 MATCH_PARENT/WRAP_CONTENT；沿用父容器参数类型。 */
object ViewLayouts {
    @MainThread @JvmStatic fun setSize(view: View, widthPx: Int, heightPx: Int) {
        require(widthPx >= 0 || widthPx == ViewGroup.LayoutParams.MATCH_PARENT || widthPx == ViewGroup.LayoutParams.WRAP_CONTENT)
        require(heightPx >= 0 || heightPx == ViewGroup.LayoutParams.MATCH_PARENT || heightPx == ViewGroup.LayoutParams.WRAP_CONTENT)
        val params = requireNotNull(view.layoutParams) { "View must have layout params" }
        if (params.width != widthPx || params.height != heightPx) {
            params.width = widthPx
            params.height = heightPx
            view.layoutParams = params
        }
    }

    /** 相对方向 margin，适配 RTL；缺少 MarginLayoutParams 时明确抛异常。 */
    @MainThread @JvmStatic fun setMargins(view: View, startPx: Int, topPx: Int, endPx: Int, bottomPx: Int) {
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams
            ?: throw IllegalArgumentException("View requires MarginLayoutParams")
        if (params.marginStart != startPx || params.topMargin != topPx || params.marginEnd != endPx || params.bottomMargin != bottomPx) {
            params.marginStart = startPx
            params.topMargin = topPx
            params.marginEnd = endPx
            params.bottomMargin = bottomPx
            view.layoutParams = params
        }
    }

    /** 保留 start/top/end，只替换 bottom；不叠加旧值。 */
    @MainThread @JvmStatic fun setBottomPadding(view: View, bottomPx: Int) {
        if (view.paddingBottom != bottomPx) view.setPaddingRelative(view.paddingStart, view.paddingTop, view.paddingEnd, bottomPx)
    }

    /** 下一次绘制前执行一次；View 脱离窗口自动移除，也可通过返回值主动 removeListener。 */
    @MainThread @JvmStatic fun onNextPreDraw(view: View, action: Runnable): OneShotPreDrawListener =
        OneShotPreDrawListener.add(view, action)
}
