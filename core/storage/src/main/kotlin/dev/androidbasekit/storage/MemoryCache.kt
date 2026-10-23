package dev.androidbasekit.storage

/**
 * Thread-safe LRU; TTL zero never expires. Uses a monotonic clock by default.
 *
 * 线程安全的内存 LRU/TTL 缓存。限制容量、访问时淘汰过期项，默认使用单调时钟；0 TTL 不过期。
 */
class MemoryCache<V : Any> @JvmOverloads constructor(
    private val capacity: Int = 100,
    private val clock: CacheClock = CacheClock { System.nanoTime() / 1_000_000 }
) : CacheStore<V> {
    /**
     * 内部缓存记录。将值与过期时间保存在一起，仅供所在缓存实现使用。
     */
    private data class Entry<V>(val value: V, val expiresAt: Long?)
    private val entries = LinkedHashMap<String, Entry<V>>(16, .75f, true)
    init { require(capacity > 0) }
    @Synchronized override fun get(key: String): V? {
        val entry = entries[key] ?: return null
        if (expired(entry)) { entries.remove(key); return null }
        return entry.value
    }
    @Synchronized override fun put(key: String, value: V, ttlMillis: Long) {
        require(ttlMillis >= 0)
        deleteExpired()
        val now = clock.nowMillis()
        val deadline = if (ttlMillis == 0L) null else if (now > Long.MAX_VALUE - ttlMillis) Long.MAX_VALUE else now + ttlMillis
        entries[key] = Entry(value, deadline)
        while (entries.size > capacity) entries.remove(entries.keys.first())
    }
    @Synchronized override fun remove(key: String) { entries.remove(key) }
    @Synchronized override fun clear() { entries.clear() }
    @Synchronized override fun deleteExpired(): Int {
        val before = entries.size
        entries.entries.removeAll { expired(it.value) }
        return before - entries.size
    }
    private fun expired(entry: Entry<V>) = entry.expiresAt?.let { clock.nowMillis() >= it } ?: false
}
