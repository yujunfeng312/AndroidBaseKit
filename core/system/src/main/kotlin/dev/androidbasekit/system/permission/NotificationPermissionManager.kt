package dev.androidbasekit.system.permission

import android.Manifest
import android.content.Context
import android.os.Build

/** Android 13+ 的通知运行时权限；旧版本无需申请，但不代表通知开关/通知渠道已开启。 */
open class NotificationPermissionManager : PermissionManager() {
    override fun permissions(context: Context): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(Manifest.permission.POST_NOTIFICATIONS)
        else emptyList()
}
