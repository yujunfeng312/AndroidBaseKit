package dev.androidbasekit.system

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Activity 导航与结果注册工具。创建显式 Intent、注册 Activity Result；在 STARTED 之前完成注册。
 */
object Navigation {
    @JvmStatic @JvmOverloads fun intent(activity: Activity, destination: Class<out Activity>, extras: Bundle? = null): Intent =
        Intent(activity, destination).apply { if (extras != null) putExtras(extras) }
    /** Register before STARTED, preferably during onCreate. */
    @JvmStatic fun registerResult(activity: ComponentActivity, callback: ActivityResultCallback<ActivityResult>): ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult(), callback)
}
