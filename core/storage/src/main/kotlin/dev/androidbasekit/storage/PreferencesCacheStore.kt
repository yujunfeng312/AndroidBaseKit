package dev.androidbasekit.storage

import com.google.gson.Gson

/**
 * Separate MMKV namespace from settings. Persistence TTL uses wall time; clock changes affect deadlines.
 *
 * MMKV 字符串持久缓存。与设置分命名空间，按用户隔离并维护 TTL；使用墙上时钟，系统时间变化会影响期限。
 */
class PreferencesCacheStore @JvmOverloads constructor(userId: String? = null,
    private val clock: CacheClock = CacheClock { System.currentTimeMillis() }) : CacheStore<String> {
    /**
     * 内部缓存记录。将值与过期时间保存在一起，仅供所在缓存实现使用。
     */
    private data class Entry(val value: String, val expiresAt: Long)
    private val prefs = AppPreferences.open("cache:" + (userId?.let { "user:$it" } ?: "app"))
    private val gson = Gson()
    @Synchronized override fun get(key: String): String? = guarded {
        val encoded = prefs.getString(key)
        if (encoded.isEmpty()) null else {
            val entry = gson.fromJson(encoded, Entry::class.java)
            if (entry.expiresAt != 0L && clock.nowMillis() >= entry.expiresAt) { prefs.remove(key); null } else entry.value
        }
    }
    @Synchronized override fun put(key: String, value: String, ttlMillis: Long) = guarded {
        require(ttlMillis >= 0)
        val now = clock.nowMillis()
        val deadline = if (ttlMillis == 0L) 0L else if (now > Long.MAX_VALUE - ttlMillis) Long.MAX_VALUE else now + ttlMillis
        prefs.putString(key, gson.toJson(Entry(value, deadline)))
    }
    @Synchronized override fun remove(key: String) = guarded { prefs.remove(key) }
    @Synchronized override fun clear() = guarded { prefs.clearUserData() }
    @Synchronized override fun deleteExpired(): Int = guarded {
        val keys = prefs.keys(); keys.count { get(it) == null }
    }
    private fun <T> guarded(block: () -> T): T = try { block() } catch (e: Exception) { throw CacheException(e) }
}
