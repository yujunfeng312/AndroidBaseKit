package dev.androidbasekit.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.PersistableBundle

/**
 * 用户主动触发的剪贴板复制工具。只写入文本，不后台读取或监听剪贴板，不持有页面 Context。
 * sensitive=true 标记敏感预览；该标记不会加密内容，其他获准读取剪贴板的应用仍可能访问。
 */
object ClipboardTools {
    /** 在主线程、用户点击后调用；label 用于描述内容类型，不应放入敏感正文。 */
    @JvmStatic @JvmOverloads fun copyText(
        context: Context, text: CharSequence, label: String = "", sensitive: Boolean = false
    ) {
        val clip = ClipData.newPlainText(label, text)
        if (sensitive) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
        context.applicationContext.getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
    }
}
