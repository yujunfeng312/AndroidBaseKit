package dev.androidbasekit.system

import android.content.Context
import android.net.*
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 网络可用性回调。接收具备 INTERNET 且 VALIDATED 的联网状态，由 NetworkStatus 主线程交付。
 */
fun interface NetworkListener { fun onChanged(online: Boolean) }
/**
 * 网络可用性查询与观察。依据默认网络 INTERNET/VALIDATED 能力判断；observe 返回订阅，页面结束时 close。
 */
object NetworkStatus {
    @JvmStatic fun isOnline(context: Context): Boolean {
        val manager = context.applicationContext.getSystemService(ConnectivityManager::class.java)
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    /** Callback is delivered on main; close the returned subscription when its lifecycle ends. */
    @JvmStatic fun observe(context: Context, listener: NetworkListener): AutoCloseable {
        val app = context.applicationContext
        val manager = app.getSystemService(ConnectivityManager::class.java)
        val handler = Handler(Looper.getMainLooper())
        val active = AtomicBoolean(true)
        fun emit() { handler.post { if (active.get()) listener.onChanged(isOnline(app)) } }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = emit()
            override fun onLost(network: Network) = emit()
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = emit()
        }
        manager.registerDefaultNetworkCallback(callback); emit()
        return AutoCloseable { if (active.getAndSet(false)) manager.unregisterNetworkCallback(callback) }
    }
}
