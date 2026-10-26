package dev.androidbasekit.system.permission

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/** 无界面原生 launcher 宿主。公开无参构造供 FragmentManager 恢复，不供业务直接创建。 */
class PermissionRequestFragment : Fragment() {
    internal var session: NativePermissionSession? = null
    private var waitingForSystem = false
    internal val busy: Boolean get() = session != null || waitingForSystem

    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        waitingForSystem = false
        session?.permissionsResult(it)
    }
    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        waitingForSystem = false
        session?.settingsResult()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        waitingForSystem = savedInstanceState?.getBoolean(WAITING) ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(WAITING, waitingForSystem)
    }

    internal fun requestPermissions(permissions: List<String>) {
        waitingForSystem = true
        try {
            permissionsLauncher.launch(permissions.toTypedArray())
        } catch (error: Exception) {
            waitingForSystem = false
            throw error
        }
    }

    internal fun openSettings(intent: Intent) {
        waitingForSystem = true
        try {
            settingsLauncher.launch(intent)
        } catch (error: Exception) {
            waitingForSystem = false
            throw error
        }
    }

    override fun onDestroy() {
        session?.destroy()
        session = null
        super.onDestroy()
    }

    internal companion object {
        private const val TAG = "androidbasekit.permission.host"
        private const val WAITING = "permission.waiting"
        fun obtain(activity: FragmentActivity): PermissionRequestFragment {
            val manager = activity.supportFragmentManager
            val existing = manager.findFragmentByTag(TAG)
            if (existing != null) return existing as PermissionRequestFragment
            return PermissionRequestFragment().also {
                manager.beginTransaction().add(it, TAG).commitNow()
            }
        }
    }
}
