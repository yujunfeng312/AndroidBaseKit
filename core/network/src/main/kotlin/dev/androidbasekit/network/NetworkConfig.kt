package dev.androidbasekit.network

import dev.androidbasekit.common.AppConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * 当前 Token 的读取接口。每次需要认证时读取最新值；实现方负责线程安全，不持有页面对象。
 */
fun interface TokenProvider { fun getToken(): String? }
/**
 * Runs on an OkHttp worker. Use a separate client without this authenticator; atomically persist the new token.
 *
 * Token 刷新接口。在 OkHttp 工作线程执行；使用独立客户端并原子保存新 Token，避免递归刷新和并发读到旧值。
 */
fun interface TokenRefresher { fun refreshToken(): String? }
/**
 * 有限重试策略。配置重试次数与退避上限，默认不重试；只对可重放的幂等请求生效。
 */
data class RetryPolicy @JvmOverloads constructor(
    val maxRetries: Int = 0, val backoffMillis: Long = 200,
    val maxBackoffMillis: Long = 2_000
) { init { require(maxRetries in 0..5 && backoffMillis >= 0 && maxBackoffMillis >= backoffMillis) } }
/**
 * 网络客户端配置。包含 baseUrl、Header 快照、超时、Token 提供/刷新及重试策略；baseUrl 必须以斜线结束。
 */
class NetworkConfig @JvmOverloads constructor(
    baseUrl: String,
    val tokenProvider: TokenProvider? = null,
    val tokenRefresher: TokenRefresher? = null,
    headers: Map<String, String> = emptyMap(),
    val timeoutSeconds: Long = AppConfig.DEFAULT_TIMEOUT_SECONDS,
    val debugLogging: Boolean = false,
    val retryPolicy: RetryPolicy = RetryPolicy()
) {
    val headers: Map<String, String> = headers.toMap()
    val baseUrl: HttpUrl = baseUrl.toHttpUrl()
    init { require(timeoutSeconds > 0); require(this.baseUrl.encodedPath.endsWith("/")) }
}
