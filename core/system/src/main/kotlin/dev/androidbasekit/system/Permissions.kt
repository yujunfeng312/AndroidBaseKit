package dev.androidbasekit.system

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * 运行时权限底层兼容入口。完整申请流程使用 permission.PermissionManager 及独立权限子类。
 */
object Permissions {
    @JvmStatic fun isGranted(context: Context, permission: String): Boolean = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    @JvmStatic fun register(activity: ComponentActivity, callback: ActivityResultCallback<Map<String, Boolean>>): ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), callback)
}
