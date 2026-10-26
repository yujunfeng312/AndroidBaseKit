package dev.androidbasekit.system.permission

/** 原生权限状态机；不依赖 UI，系统结果和弹框决策由 Host 接入。 */
internal class PermissionFlow(private val permissions: List<String>, private val host: Host) {
    interface Host {
        fun isGranted(permission: String): Boolean
        fun shouldExplain(permission: String): Boolean
        fun requestPermissions(permissions: List<String>)
        fun explain(permissions: List<String>, beforeRequest: Boolean, proceed: () -> Unit, cancel: () -> Unit)
        fun forwardToSettings(permissions: List<String>, proceed: () -> Unit, cancel: () -> Unit)
        fun openSettings()
        fun complete(granted: List<String>, denied: List<String>)
    }

    private enum class State { NEW, RUNNING, REQUEST, PROMPT, SETTINGS, FINISHED }
    private var state = State.NEW
    private var promptId = 0
    private var abandoned = false
    private val permanent = linkedSetOf<String>()

    fun start(explainBefore: Boolean) {
        check(state == State.NEW)
        state = State.RUNNING
        val denied = denied()
        when {
            denied.isEmpty() -> finish()
            explainBefore -> prompt(denied, before = true, settings = false) { request(denied()) }
            else -> request(denied)
        }
    }

    fun onPermissionsResult(result: Map<String, Boolean>) {
        if (state != State.REQUEST) return
        state = State.RUNNING
        if (abandoned || result.isEmpty()) { finish(); return }
        val denied = denied()
        permanent.retainAll(denied.toSet())
        // 仅在实际系统申请明确返回 false 后判断，首次请求前不能用 rationale=false 判永久拒绝。
        val returnedDenied = denied.filter { result[it] == false }
        val retryable = returnedDenied.filter(host::shouldExplain)
        permanent.addAll(returnedDenied - retryable.toSet())
        when {
            denied.isEmpty() -> finish()
            retryable.isNotEmpty() -> prompt(retryable, before = false, settings = false) {
                request(denied().filterNot { it in permanent })
            }
            permanent.isNotEmpty() -> prompt(permanent.toList(), before = false, settings = true) {
                state = State.SETTINGS
                host.openSettings()
            }
            else -> finish()
        }
    }

    /** 设置页返回只查询真实授权状态，不根据 RESULT_OK 判断，也不自动循环弹框。 */
    fun onSettingsResult() {
        if (state == State.SETTINGS) finish()
    }

    /** View 销毁时释放弹框；系统请求进行中则等结果消费完再释放 Activity 的请求位。 */
    fun abandon() {
        abandoned = true
        if (state != State.REQUEST && state != State.SETTINGS) finish()
    }

    fun destroy() { abandoned = true; finish() }

    private fun request(pending: List<String>) {
        if (abandoned) { finish(); return }
        if (pending.isEmpty()) {
            if (permanent.isEmpty()) finish()
            else prompt(permanent.toList(), before = false, settings = true) {
                state = State.SETTINGS
                host.openSettings()
            }
            return
        }
        state = State.REQUEST
        host.requestPermissions(pending)
    }

    private fun prompt(items: List<String>, before: Boolean, settings: Boolean, proceed: () -> Unit) {
        state = State.PROMPT
        val id = ++promptId
        val accept = {
            if (state == State.PROMPT && promptId == id) {
                state = State.RUNNING
                proceed()
            }
        }
        val reject = {
            if (state == State.PROMPT && promptId == id) finish()
        }
        if (settings) host.forwardToSettings(items, accept, reject)
        else host.explain(items, before, accept, reject)
    }

    private fun denied() = permissions.filterNot(host::isGranted)

    private fun finish() {
        if (state == State.FINISHED) return
        state = State.FINISHED
        val denied = denied()
        host.complete(permissions - denied.toSet(), denied)
    }
}
