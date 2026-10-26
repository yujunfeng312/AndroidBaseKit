package dev.androidbasekit.system.permission

import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.androidbasekit.system.SystemIntents

/** 绑定一次原生申请、页面生命周期、弹框及配置；完成后清理所有页面回调。 */
internal class NativePermissionSession(
    private val activity: FragmentActivity,
    owner: LifecycleOwner,
    private val host: PermissionRequestFragment,
    permissions: List<String>,
    options: PermissionOptions,
    private val reasonDialog: () -> PermissionDialog,
    private val settingsDialog: () -> PermissionDialog,
    canDeliver: () -> Boolean
) : PermissionFlow.Host, DefaultLifecycleObserver {
    private var config: PermissionOptions? = options
    private var canDeliver: (() -> Boolean)? = canDeliver
    private val lifecycle = owner.lifecycle
    private var prompt: PermissionPrompt? = null
    private val flow = PermissionFlow(permissions, this)

    fun start() {
        lifecycle.addObserver(this)
        flow.start(config?.explainBefore == true)
    }

    fun permissionsResult(result: Map<String, Boolean>) = flow.onPermissionsResult(result)
    fun settingsResult() = flow.onSettingsResult()

    override fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    override fun shouldExplain(permission: String) = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    override fun requestPermissions(permissions: List<String>) {
        if (canDeliver?.invoke() != true) { destroy(); return }
        host.requestPermissions(permissions)
    }

    override fun explain(permissions: List<String>, beforeRequest: Boolean, proceed: () -> Unit, cancel: () -> Unit) {
        val options = config
        if (options == null || canDeliver?.invoke() != true) { invalidate(); return }
        val current = createPrompt(proceed, cancel)
        val custom = options.explain
        if (custom != null) custom.onExplain(current, permissions, beforeRequest)
        else current.showDialog(options.reasonDialog ?: reasonDialog())
    }

    override fun forwardToSettings(permissions: List<String>, proceed: () -> Unit, cancel: () -> Unit) {
        val options = config
        if (options == null || canDeliver?.invoke() != true) { invalidate(); return }
        val current = createPrompt(proceed, cancel)
        val custom = options.settings
        if (custom != null) custom.onForwardToSettings(current, permissions)
        else current.showDialog(options.settingsDialog ?: settingsDialog())
    }

    private fun createPrompt(proceed: () -> Unit, cancel: () -> Unit): PermissionPrompt {
        prompt?.dispose()
        return PermissionPrompt(activity, {
            if (canDeliver?.invoke() == true) proceed() else invalidate()
        }, cancel).also { prompt = it }
    }

    override fun openSettings() {
        try {
            host.openSettings(SystemIntents.appSettings(activity))
        } catch (_: ActivityNotFoundException) {
            flow.onSettingsResult()
        } catch (_: SecurityException) {
            flow.onSettingsResult()
        }
    }

    override fun complete(granted: List<String>, denied: List<String>) {
        val options = config
        val deliver = canDeliver?.invoke() == true
        clear()
        if (deliver) {
            if (denied.isEmpty()) options?.granted?.onGranted(granted)
            else options?.denied?.onDenied(granted, denied)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) = invalidate()

    private fun invalidate() {
        config = null
        canDeliver = null
        lifecycle.removeObserver(this)
        prompt?.dispose()
        prompt = null
        flow.abandon()
    }

    fun destroy() {
        config = null
        canDeliver = null
        flow.destroy()
    }

    private fun clear() {
        config = null
        canDeliver = null
        prompt?.dispose()
        prompt = null
        lifecycle.removeObserver(this)
        if (host.session === this) host.session = null
    }
}
