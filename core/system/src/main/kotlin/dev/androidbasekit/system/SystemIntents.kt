package dev.androidbasekit.system

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * 系统页面 Intent 工厂。构造网页、分享文本和应用设置页 Intent，由页面决定何时启动。
 * 不自行启动 Activity；无法处理 Intent 时 startIfAvailable 返回 false，不申请权限或绕过系统确认。
 */
object SystemIntents {
    /** 仅接受带主机的 HTTP/HTTPS 地址，拒绝 javascript/file 等协议。 */
    @JvmStatic fun web(url: String): Intent {
        val uri = Uri.parse(url)
        require(uri.scheme?.lowercase(java.util.Locale.ROOT) in setOf("http", "https") && !uri.host.isNullOrBlank()) {
            "An absolute HTTP(S) URL is required"
        }
        return Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)
    }

    /** 返回文本分享 Intent；建议交给 Intent.createChooser，避免默认直接发送给某个应用。 */
    @JvmStatic @JvmOverloads fun shareText(text: String, subject: String? = null): Intent =
        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text).apply {
            if (subject != null) putExtra(Intent.EXTRA_SUBJECT, subject)
        }

    /** 打开当前应用的系统详情页，可由用户自行管理权限。 */
    @JvmStatic fun appSettings(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))

    /** 复制 Intent 后启动，不修改调用方对象；Context 非 Activity 时使用 NEW_TASK。 */
    @JvmStatic fun startIfAvailable(context: Context, intent: Intent): Boolean = try {
        val request = Intent(intent)
        if (context !is android.app.Activity) request.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(request)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
