package dev.androidbasekit.system.permission

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Looper

/** 一次性弹框决策。可使用任意原生/业务 Dialog，不需要继承框架弹框。所有操作在主线程执行。 */
class PermissionPrompt internal constructor(
    context: Context,
    accept: () -> Unit,
    reject: () -> Unit
) {
    private var context: Context? = context
    private var accept: (() -> Unit)? = accept
    private var reject: (() -> Unit)? = reject
    private var resolved = false
    private var dialog: Dialog? = null

    /** 确认：继续申请，或进入设置页。重复调用和销毁后的旧按钮操作均忽略。 */
    fun proceed() = resolve(true)
    /** 取消：结束本次申请并进入拒绝回调。 */
    fun cancel() = resolve(false)

    fun showDialog(config: PermissionDialog) {
        checkMainThread()
        if (resolved) return
        val alert = AlertDialog.Builder(checkNotNull(context))
            .setMessage(config.message)
            .setPositiveButton(config.positiveText) { _, _ -> proceed() }
            .setNegativeButton(config.negativeText) { _, _ -> cancel() }
            .create()
        attachDialog(alert)
        alert.show()
    }

    /** 先绑定再 show；外部点击/返回/直接 dismiss 都视为取消。确认按钮须调用 proceed。 */
    fun attachDialog(dialog: Dialog) {
        checkMainThread()
        check(this.dialog == null) { "A permission prompt can own only one dialog" }
        if (resolved) { dialog.dismiss(); return }
        this.dialog = dialog
        dialog.setOnCancelListener { cancel() }
        dialog.setOnDismissListener { cancel() }
    }

    private fun resolve(accepted: Boolean) {
        checkMainThread()
        if (resolved) return
        val action = if (accepted) accept else reject
        dispose()
        action?.invoke()
    }

    internal fun dispose() {
        resolved = true
        dismissDialog()
        context = null
        accept = null
        reject = null
    }

    private fun dismissDialog() {
        val current = dialog
        dialog = null
        current?.setOnCancelListener(null)
        current?.setOnDismissListener(null)
        current?.dismiss()
    }

    private fun checkMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) { "Resolve permission prompts on the main thread" }
    }
}
