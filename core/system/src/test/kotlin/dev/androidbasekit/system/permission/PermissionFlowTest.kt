package dev.androidbasekit.system.permission

import org.junit.Assert.*
import org.junit.Test

/** 验证拒绝分类和状态流转，不用 UI 假结果替代状态机分支。 */
class PermissionFlowTest {
    private class Host : PermissionFlow.Host {
        val grants = mutableSetOf<String>()
        val rationale = mutableSetOf<String>()
        val requests = mutableListOf<List<String>>()
        val explanations = mutableListOf<Pair<List<String>, Boolean>>()
        val settingsPrompts = mutableListOf<List<String>>()
        var proceed: () -> Unit = {}
        var cancel: () -> Unit = {}
        var settingsOpened = 0
        val results = mutableListOf<Pair<List<String>, List<String>>>()
        override fun isGranted(permission: String) = permission in grants
        override fun shouldExplain(permission: String) = permission in rationale
        override fun requestPermissions(permissions: List<String>) { requests += permissions }
        override fun explain(permissions: List<String>, beforeRequest: Boolean, proceed: () -> Unit, cancel: () -> Unit) {
            explanations += permissions to beforeRequest
            this.proceed = proceed
            this.cancel = cancel
        }
        override fun forwardToSettings(permissions: List<String>, proceed: () -> Unit, cancel: () -> Unit) {
            settingsPrompts += permissions
            this.proceed = proceed
            this.cancel = cancel
        }
        override fun openSettings() { settingsOpened++ }
        override fun complete(granted: List<String>, denied: List<String>) { results += granted to denied }
    }

    @Test fun alreadyGrantedFinishesWithoutSystemRequest() {
        val host = Host().apply { grants += "camera" }
        PermissionFlow(listOf("camera"), host).start(true)
        assertTrue(host.requests.isEmpty())
        assertTrue(host.explanations.isEmpty())
        assertEquals(listOf("camera") to emptyList<String>(), host.results.single())
    }

    @Test fun firstRequestDoesNotMistakeRationaleFalseForPermanentDenial() {
        val host = Host()
        PermissionFlow(listOf("camera"), host).start(false)
        assertEquals(listOf(listOf("camera")), host.requests)
        assertTrue(host.settingsPrompts.isEmpty())
    }

    @Test fun cancellingBeforeRequestDoesNotLaunchOrPretendToGrant() {
        val host = Host()
        PermissionFlow(listOf("camera"), host).start(true)
        assertEquals(listOf(listOf("camera") to true), host.explanations)
        host.cancel()
        host.proceed() // 旧按钮无法重新启动。
        assertTrue(host.requests.isEmpty())
        assertEquals(emptyList<String>() to listOf("camera"), host.results.single())
    }

    @Test fun ordinaryDenialCanExplainRetryAndSucceed() {
        val host = Host().apply { rationale += "camera" }
        val flow = PermissionFlow(listOf("camera"), host)
        flow.start(false)
        flow.onPermissionsResult(mapOf("camera" to false))
        assertEquals(listOf(listOf("camera") to false), host.explanations)
        host.proceed()
        assertEquals(2, host.requests.size)
        host.grants += "camera"
        flow.onPermissionsResult(mapOf("camera" to true))
        assertEquals(listOf("camera") to emptyList<String>(), host.results.single())
    }

    @Test fun permanentDenialGoesToSettingsAndRechecksActualGrant() {
        val host = Host()
        val flow = PermissionFlow(listOf("camera"), host)
        flow.start(false)
        flow.onPermissionsResult(mapOf("camera" to false))
        assertEquals(listOf(listOf("camera")), host.settingsPrompts)
        host.proceed()
        assertEquals(1, host.settingsOpened)
        host.grants += "camera"
        flow.onSettingsResult()
        assertEquals(1, host.requests.size)
        assertEquals(listOf("camera") to emptyList<String>(), host.results.single())
    }

    @Test fun mixedDenialExplainsRetryableFirstAndPreservesPermanentPermission() {
        val host = Host().apply { rationale += "camera" }
        val flow = PermissionFlow(listOf("camera", "microphone"), host)
        flow.start(false)
        flow.onPermissionsResult(mapOf("camera" to false, "microphone" to false))
        assertEquals(listOf("camera"), host.explanations.single().first)
        assertTrue(host.settingsPrompts.isEmpty())
        host.proceed()
        assertEquals(listOf("camera"), host.requests.last())
        host.grants += "camera"
        flow.onPermissionsResult(mapOf("camera" to true))
        assertEquals(listOf("microphone"), host.settingsPrompts.single())
        host.cancel()
        assertEquals(listOf("camera") to listOf("microphone"), host.results.single())
    }

    @Test fun emptySystemResultFinishesWithoutPermanentDenialDialog() {
        val host = Host()
        val flow = PermissionFlow(listOf("camera"), host)
        flow.start(false)
        flow.onPermissionsResult(emptyMap())
        assertTrue(host.settingsPrompts.isEmpty())
        assertTrue(host.explanations.isEmpty())
        assertEquals(emptyList<String>() to listOf("camera"), host.results.single())
    }

    @Test fun settingsReturnWithoutGrantFinishesOnceInsteadOfLooping() {
        val host = Host()
        val flow = PermissionFlow(listOf("camera"), host)
        flow.start(false)
        flow.onPermissionsResult(mapOf("camera" to false))
        host.proceed()
        flow.onSettingsResult()
        flow.onSettingsResult()
        assertEquals(1, host.settingsPrompts.size)
        assertEquals(1, host.results.size)
        assertEquals(listOf("camera"), host.results.single().second)
    }

    @Test fun ownerDestructionWaitsForSystemResultButSkipsFurtherDialogs() {
        val host = Host()
        val flow = PermissionFlow(listOf("camera"), host)
        flow.start(false)
        flow.abandon()
        assertTrue(host.results.isEmpty())
        flow.onPermissionsResult(mapOf("camera" to false))
        assertTrue(host.settingsPrompts.isEmpty())
        assertEquals(1, host.results.size)
    }

    @Test fun staleExplanationCannotCancelTheNextPromptOrLaunchTwice() {
        val host = Host().apply { rationale += "camera" }
        val flow = PermissionFlow(listOf("camera"), host)
        flow.start(true)
        val oldProceed = host.proceed
        val oldCancel = host.cancel
        oldProceed()
        flow.onPermissionsResult(mapOf("camera" to false))
        oldProceed()
        oldCancel()
        assertEquals(1, host.requests.size)
        assertTrue(host.results.isEmpty())
        host.cancel()
        assertEquals(1, host.results.size)
    }

    @Test fun settingsReturnRechecksPreviouslyGrantedPermissionsToo() {
        val host = Host().apply { grants += "microphone" }
        val flow = PermissionFlow(listOf("camera", "microphone"), host)
        flow.start(false)
        flow.onPermissionsResult(mapOf("camera" to false))
        host.proceed()
        host.grants.clear()
        host.grants += "camera"
        flow.onSettingsResult()
        assertEquals(listOf("camera") to listOf("microphone"), host.results.single())
    }
}
