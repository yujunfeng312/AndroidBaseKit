package dev.androidbasekit.system

import android.content.ClipboardManager
import android.content.Intent
import android.provider.Settings
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * 系统工具本地回归测试。验证 Intent 数据和敏感复制标记，不代表真实系统页面或分享应用行为。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SystemToolsTest {
    @Test fun webIntentOnlyAllowsHttpSchemes() {
        val intent = SystemIntents.web("https://example.com/path?q=1")
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("example.com", intent.data?.host)
        assertTrue(intent.hasCategory(Intent.CATEGORY_BROWSABLE))
        listOf("javascript:alert(1)", "file:///tmp/a", "https:/missing-host").forEach {
            assertThrows(IllegalArgumentException::class.java) { SystemIntents.web(it) }
        }
    }

    @Test fun settingsAndShareRetainExplicitPayload() {
        val app = RuntimeEnvironment.getApplication()
        val settings = SystemIntents.appSettings(app)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, settings.action)
        assertEquals("package:${app.packageName}", settings.data.toString())
        val share = SystemIntents.shareText("hello", "subject")
        assertEquals(Intent.ACTION_SEND, share.action)
        assertEquals("text/plain", share.type)
        assertEquals("hello", share.getStringExtra(Intent.EXTRA_TEXT))
        assertEquals("subject", share.getStringExtra(Intent.EXTRA_SUBJECT))
    }

    @Test fun sensitiveCopySetsPreviewHintAndPlainCopyReplacesIt() {
        val app = RuntimeEnvironment.getApplication()
        val manager = app.getSystemService(ClipboardManager::class.java)
        ClipboardTools.copyText(app, "test-value", "test", true)
        assertEquals("test-value", manager.primaryClip?.getItemAt(0)?.text.toString())
        assertTrue(manager.primaryClipDescription?.extras?.getBoolean("android.content.extra.IS_SENSITIVE") == true)
        ClipboardTools.copyText(app, "plain")
        assertEquals("plain", manager.primaryClip?.getItemAt(0)?.text.toString())
        assertFalse(manager.primaryClipDescription?.extras?.getBoolean("android.content.extra.IS_SENSITIVE") == true)
    }

    @Test fun startingFromApplicationAddsNewTaskWithoutMutatingInput() {
        val app = RuntimeEnvironment.getApplication()
        val original = SystemIntents.web("https://example.com")
        assertTrue(SystemIntents.startIfAvailable(app, original))
        val started = shadowOf(app).nextStartedActivity
        assertTrue(started.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertEquals(0, original.flags)
    }

    @Test fun unavailableActivityReturnsFalse() {
        val context = object : android.content.ContextWrapper(RuntimeEnvironment.getApplication()) {
            override fun startActivity(intent: Intent) {
                throw android.content.ActivityNotFoundException("test unavailable")
            }
        }
        assertFalse(SystemIntents.startIfAvailable(context, SystemIntents.web("https://example.com")))
    }
}
