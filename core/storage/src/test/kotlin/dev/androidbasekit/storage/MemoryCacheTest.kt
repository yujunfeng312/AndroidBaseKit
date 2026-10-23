package dev.androidbasekit.storage

import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.*
import dev.androidbasekit.common.*

/**
 * MemoryCacheTest 回归测试集。通过测试方法验证对应模块行为、错误与边界条件；测试结果见验收说明。
 */
class MemoryCacheTest {
    @Test fun expiresAtDeadline() {
        var time = 0L; val cache = MemoryCache<String>(3, CacheClock { time })
        cache.put("a", "one", 10); time = 9; assertEquals("one", cache.get("a")); time = 10; assertNull(cache.get("a"))
    }
    @Test fun zeroTtlNeverExpires() {
        var time = 0L; val cache = MemoryCache<String>(3, CacheClock { time })
        cache.put("a", "one", 0); time = Long.MAX_VALUE; assertEquals("one", cache.get("a"))
    }
    @Test fun lruKeepsRecentlyAccessed() {
        val cache = MemoryCache<String>(2)
        cache.put("a", "one", 0); cache.put("b", "two", 0); cache.get("a"); cache.put("c", "three", 0)
        assertNull(cache.get("b")); assertEquals("one", cache.get("a")); assertEquals("three", cache.get("c"))
    }
    @Test fun expirySweepAndClear() {
        var time = 0L; val cache = MemoryCache<String>(3, CacheClock { time })
        cache.put("a", "one", 10); cache.put("b", "two", 0); time = 20
        assertEquals(1, cache.deleteExpired()); cache.clear(); assertNull(cache.get("b"))
    }
    @Test fun scopesAreIsolated() {
        val a = MemoryCache<String>(); val b = MemoryCache<String>()
        a.put("key", "a", 0); b.put("key", "b", 0); a.clear(); assertEquals("b", b.get("key"))
    }
    @Test(expected = IllegalArgumentException::class) fun negativeTtlRejected() { MemoryCache<String>().put("key", "bad", -1) }
    @Test fun overflowDoesNotImmediatelyExpire() {
        val cache = MemoryCache<String>(2, CacheClock { Long.MAX_VALUE - 10 })
        cache.put("a", "one", 20); assertEquals("one", cache.get("a"))
    }
    @Test fun cacheFirstAvoidsFetch() = runBlocking {
        val cache = MemoryCache<String>(); cache.put("a", "cached", 0)
        val repository = CachedRepository(cache) { fail("Cache failure") }
        val result = repository.get("a", CachePolicy.CACHE_FIRST, 10) { fail("Fetch called"); ApiResult.success("bad") }
        assertEquals("cached", result.getOrNull())
    }
    @Test fun networkFirstFallsBackAndNetworkOnlyDoesNot() = runBlocking {
        val cache = MemoryCache<String>(); cache.put("a", "cached", 0)
        val repository = CachedRepository(cache) { fail("Cache failure") }
        val fetch: suspend () -> ApiResult<String> = { ApiResult.failure(AppError(ErrorKind.NETWORK)) }
        assertEquals("cached", repository.get("a", CachePolicy.NETWORK_FIRST, 10, fetch).getOrNull())
        assertEquals(ErrorKind.NETWORK, repository.get("a", CachePolicy.NETWORK_ONLY, 10, fetch).errorOrNull()?.kind)
    }
    @Test fun cancellationIsNotCacheFallback() = runBlocking {
        val repository = CachedRepository(MemoryCache<String>()) { }
        try { repository.get("a", CachePolicy.NETWORK_FIRST, 10) { throw CancellationException() }; fail("Cancellation swallowed") }
        catch (_: CancellationException) { }
    }
    @Test fun failedCacheWriteDoesNotLoseNetworkSuccess() = runBlocking {
        var failures = 0
        val store = object : CacheStore<String> {
            override fun get(key: String): String? = null
            override fun put(key: String, value: String, ttlMillis: Long) { throw CacheException(java.io.IOException()) }
            override fun remove(key: String) { }
            override fun clear() { }
            override fun deleteExpired() = 0
        }
        val result = CachedRepository(store) { failures++ }.get("a", CachePolicy.NETWORK_FIRST, 10) { ApiResult.success("network") }
        assertEquals("network", result.getOrNull()); assertEquals(1, failures)
    }
}
