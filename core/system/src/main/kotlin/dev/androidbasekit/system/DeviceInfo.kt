package dev.androidbasekit.system

import android.content.Context
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

/**
 * 应用版本快照。包含可展示的版本名 name 和长整型版本号 code。
 */
data class AppVersion(val name: String, val code: Long)
/**
 * 应用与设备基础信息入口。读取版本、API、型号和厂商，不读取设备唯一标识。
 */
object DeviceInfo {
    @JvmStatic fun appVersion(context: Context): AppVersion {
        val info = if (Build.VERSION.SDK_INT >= 33) context.packageManager.getPackageInfo(context.packageName,
            android.content.pm.PackageManager.PackageInfoFlags.of(0)) else {
            @Suppress("DEPRECATION") context.packageManager.getPackageInfo(context.packageName, 0)
        }
        return AppVersion(info.versionName ?: "", PackageInfoCompat.getLongVersionCode(info))
    }
    @JvmStatic fun sdk(): Int = Build.VERSION.SDK_INT
    @JvmStatic fun model(): String = Build.MODEL
    @JvmStatic fun manufacturer(): String = Build.MANUFACTURER
}
