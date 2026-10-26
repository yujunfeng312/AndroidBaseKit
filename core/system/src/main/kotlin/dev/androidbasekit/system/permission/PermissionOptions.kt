package dev.androidbasekit.system.permission

/** 每次 request 单独创建；请求开始时会快照配置，避免中途更换回调。Java 可直接使用 lambda。 */
class PermissionOptions {
    fun interface GrantedCallback { fun onGranted(permissions: List<String>) }
    fun interface DeniedCallback { fun onDenied(granted: List<String>, denied: List<String>) }
    fun interface ExplainHandler {
        /** 确认调用 prompt.proceed()，取消调用 prompt.cancel()；用 attachDialog 绑定弹框生命周期。 */
        fun onExplain(prompt: PermissionPrompt, permissions: List<String>, beforeRequest: Boolean)
    }
    fun interface SettingsHandler {
        /** 确认调用 prompt.proceed() 前往设置，取消调用 prompt.cancel()。 */
        fun onForwardToSettings(prompt: PermissionPrompt, permissions: List<String>)
    }

    internal var granted: GrantedCallback? = null
    internal var denied: DeniedCallback? = null
    internal var explain: ExplainHandler? = null
    internal var settings: SettingsHandler? = null
    internal var explainBefore = false
    internal var reasonDialog: PermissionDialog? = null
    internal var settingsDialog: PermissionDialog? = null

    fun onGranted(callback: GrantedCallback) = apply { granted = callback }
    fun onDenied(callback: DeniedCallback) = apply { denied = callback }
    fun onExplain(handler: ExplainHandler) = apply { explain = handler }
    fun onForwardToSettings(handler: SettingsHandler) = apply { settings = handler }
    @JvmOverloads fun explainBeforeRequest(enabled: Boolean = true) = apply { explainBefore = enabled }
    fun explanationDialog(dialog: PermissionDialog) = apply { reasonDialog = dialog }
    fun permanentDenialDialog(dialog: PermissionDialog) = apply { settingsDialog = dialog }

    internal fun snapshot() = PermissionOptions().also {
        it.granted = granted
        it.denied = denied
        it.explain = explain
        it.settings = settings
        it.explainBefore = explainBefore
        it.reasonDialog = reasonDialog
        it.settingsDialog = settingsDialog
    }
}

/** 始终提供取消按钮，取消会走最终拒绝回调。文案由业务资源提供。 */
data class PermissionDialog(val message: String, val positiveText: String, val negativeText: String) {
    init {
        require(message.isNotBlank() && positiveText.isNotBlank() && negativeText.isNotBlank()) {
            "Permission dialog text must not be blank"
        }
    }
}
