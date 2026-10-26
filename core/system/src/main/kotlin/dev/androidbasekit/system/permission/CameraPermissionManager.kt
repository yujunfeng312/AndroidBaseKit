package dev.androidbasekit.system.permission

import android.Manifest
import android.content.Context
import dev.androidbasekit.system.R

/** 相机权限；Manifest 声明由接入 App 按实际功能添加。 */
open class CameraPermissionManager : PermissionManager() {
    override fun permissions(context: Context) = listOf(Manifest.permission.CAMERA)
    override fun explanationDialog(context: Context) = PermissionDialog(
        context.getString(R.string.permission_camera_explanation),
        context.getString(R.string.permission_continue), context.getString(R.string.permission_cancel)
    )
}
