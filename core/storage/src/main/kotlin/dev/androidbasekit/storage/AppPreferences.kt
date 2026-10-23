package dev.androidbasekit.storage

import android.content.Context
import com.tencent.mmkv.MMKV
import java.security.MessageDigest

/**
 * MMKV initialization uses applicationContext once. Store is isolated by user (null = app scope).
 *
 * MMKV 设置存储。先在 Application 初始化，再按应用/用户打开；提供字符串、整数和清理，不是加密凭证保险箱。
 */
class AppPreferences private constructor(private val kv: MMKV) {
    fun putString(key: String, value: String) { if (!kv.encode(key, value)) throw CacheException(IllegalStateException("Encode failed")) }
    @JvmOverloads fun getString(key: String, fallback: String = ""): String = kv.decodeString(key, fallback) ?: fallback
    fun putInt(key: String, value: Int) { if (!kv.encode(key, value)) throw CacheException(IllegalStateException("Encode failed")) }
    @JvmOverloads fun getInt(key: String, fallback: Int = 0): Int = kv.decodeInt(key, fallback)
    fun remove(key: String) = kv.removeValueForKey(key)
    fun clearUserData() = kv.clearAll()
    internal fun keys(): Array<String> = kv.allKeys() ?: emptyArray()
    companion object {
        @Volatile private var initialized = false
        @JvmStatic @Synchronized fun initialize(context: Context) {
            if (!initialized) { MMKV.initialize(context.applicationContext); initialized = true }
        }
        @JvmStatic @JvmOverloads fun open(userId: String? = null): AppPreferences {
            check(initialized) { "Call AppPreferences.initialize(application) first" }
            val scope = if (userId == null) "app" else "user_" + MessageDigest.getInstance("SHA-256")
                .digest(userId.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
            return AppPreferences(MMKV.mmkvWithID("basekit_$scope", MMKV.SINGLE_PROCESS_MODE))
        }
    }
}
