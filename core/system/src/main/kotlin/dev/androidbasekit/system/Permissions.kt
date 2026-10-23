package dev.androidbasekit.system

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * 运行时权限查询与申请注册入口。只注册 Activity Result launcher，何时解释或申请权限由业务页面决定。
 */
object Permissions {
    @JvmStatic fun isGranted(context: Context, permission: String): Boolean = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    @JvmStatic fun register(activity: ComponentActivity, callback: ActivityResultCallback<Map<String, Boolean>>): ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), callback)
}
