package dev.androidbasekit.storage

import dev.androidbasekit.common.ApiResult
import kotlinx.coroutines.CancellationException

/**
 * Cache failures are reported and do not replace successful network data. Cancellation always propagates.
 *
 * 组合缓存与远程数据的 Repository 辅助类。按 CachePolicy 读写，缓存失败单独报告，不覆盖成功的网络数据。
 */
class CachedRepository<V : Any>(private val cache: CacheStore<V>, private val onCacheError: (CacheException) -> Unit) {
    suspend fun get(key: String, policy: CachePolicy, ttlMillis: Long, fetch: suspend () -> ApiResult<V>): ApiResult<V> {
        fun read(): V? = try { cache.get(key) } catch (e: CacheException) { onCacheError(e); null }
        if (policy == CachePolicy.CACHE_FIRST) read()?.let { return ApiResult.success(it) }
        val result = fetch()
        if (result is ApiResult.Success && policy != CachePolicy.NETWORK_ONLY) {
            try { cache.put(key, result.data, ttlMillis) } catch (e: CacheException) { onCacheError(e) }
        } else if (result is ApiResult.Failure && policy == CachePolicy.NETWORK_FIRST) {
            read()?.let { return ApiResult.success(it) }
        }
        return result
    }
}
