package dev.androidbasekit.system.permission

import android.Manifest
import android.content.Context

/** 麦克风权限；业务可继续继承并覆盖默认解释文案。 */
open class MicrophonePermissionManager : PermissionManager() {
    override fun permissions(context: Context) = listOf(Manifest.permission.RECORD_AUDIO)
}
