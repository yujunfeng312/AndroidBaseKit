package dev.androidbasekit.storage

/**
 * Cache data only; never store credentials in a plain cache. Each store belongs to one scope/user.
 *
 * 有作用域的缓存存取协议。包含读、写、删除、清空和过期清理；ttlMillis 单位毫秒，当前实现中 0 表示永久。
 */
interface CacheStore<V : Any> {
    fun get(key: String): V?
    fun put(key: String, value: V, ttlMillis: Long)
    fun remove(key: String)
    fun clear()
    fun deleteExpired(): Int
}
/**
 * 可注入的毫秒时钟。供 TTL 计算和测试使用；同一缓存的读写必须使用同一时间基准。
 */
fun interface CacheClock { fun nowMillis(): Long }
/**
 * Repository 缓存策略。提供缓存优先、网络优先失败回退、仅网络三种模式。
 */
enum class CachePolicy { CACHE_FIRST, NETWORK_FIRST, NETWORK_ONLY }
/**
 * 缓存访问失败的包装异常。区分缓存层故障与网络失败，由调用方决定记录或降级。
 */
class CacheException(cause: Throwable) : RuntimeException("Cache operation failed", cause)
