package dev.androidbasekit.system.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import dev.androidbasekit.system.R

/** 每个权限或权限组独立继承；只声明权限和默认弹框，原生申请由框架统一管理。 */
abstract class PermissionManager {
    protected abstract fun permissions(context: Context): List<String>

    protected open fun explanationDialog(context: Context) = PermissionDialog(
        context.getString(R.string.permission_explanation),
        context.getString(R.string.permission_continue), context.getString(R.string.permission_cancel)
    )

    protected open fun permanentDenialDialog(context: Context) = PermissionDialog(
        context.getString(R.string.permission_settings_explanation),
        context.getString(R.string.permission_settings), context.getString(R.string.permission_cancel)
    )

    /** 空权限组表示当前系统版本无需申请。仅处理普通运行时权限。 */
    fun isGranted(context: Context): Boolean = checkedPermissions(context).all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    @JvmOverloads
    fun request(activity: FragmentActivity, options: PermissionOptions = PermissionOptions()): Boolean =
        start(activity, activity, options) { !activity.isFinishing && !activity.isDestroyed }

    @JvmOverloads
    fun request(fragment: Fragment, options: PermissionOptions = PermissionOptions()): Boolean {
        checkMainThread()
        check(fragment.isAdded && !fragment.isRemoving && !fragment.isDetached) { "Permission host Fragment must be attached" }
        check(!fragment.parentFragmentManager.isStateSaved) { "Permission host state is already saved" }
        val owner = fragment.viewLifecycleOwnerLiveData.value ?: fragment
        return start(fragment.requireActivity(), owner, options) {
            fragment.isAdded && !fragment.isRemoving && !fragment.isDetached
        }
    }

    private fun checkedPermissions(context: Context) = permissions(context).distinct().also {
        require(it.none(String::isBlank)) { "Permission names must not be blank" }
    }

    private fun start(
        activity: FragmentActivity, owner: LifecycleOwner, options: PermissionOptions, canDeliver: () -> Boolean
    ): Boolean {
        checkMainThread()
        check(!activity.isFinishing && !activity.isDestroyed && !activity.supportFragmentManager.isStateSaved &&
            owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            "Permission host must be STARTED with unsaved state"
        }
        val requested = checkedPermissions(activity)
        validateManifest(activity, requested)
        val host = PermissionRequestFragment.obtain(activity)
        if (host.busy) return false
        val session = NativePermissionSession(activity, owner, host, requested, options.snapshot(),
            { explanationDialog(activity) }, { permanentDenialDialog(activity) }, canDeliver)
        host.session = session
        try { session.start() } catch (error: Exception) { session.destroy(); throw error }
        return true
    }

    @Suppress("DEPRECATION")
    private fun validateManifest(context: Context, permissions: List<String>) {
        if (permissions.isEmpty()) return
        val declared = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions.orEmpty().toSet()
        require(declared.containsAll(permissions)) { "Declare requested permissions in AndroidManifest.xml: ${permissions - declared}" }
        permissions.forEach {
            val info = context.packageManager.getPermissionInfo(it, 0)
            require(info.protectionLevel and android.content.pm.PermissionInfo.PROTECTION_MASK_BASE ==
                android.content.pm.PermissionInfo.PROTECTION_DANGEROUS) { "Only runtime permissions are supported: $it" }
        }
    }

    private fun checkMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) { "Request permissions on the main thread" }
    }
}
