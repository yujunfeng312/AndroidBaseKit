package dev.androidbasekit.network

import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Retrofit/OkHttp 客户端入口。创建 API 服务、注入同源凭证、受控重试及单次 Token 刷新；日志仅记录方法、状态码、耗时。
 */
class NetworkClient(config: NetworkConfig) {
    val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        .callTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
        // Framework owns retry policy, including connection failures.
        .retryOnConnectionFailure(false)
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
            if (sameOrigin(chain.request().url, config.baseUrl)) {
                config.headers.toMap().forEach { (key, value) -> builder.header(key, value) }
                config.tokenProvider?.getToken()?.let { builder.header("Authorization", "Bearer $it") }
            }
            chain.proceed(builder.build())
        }
        .addInterceptor(RetryInterceptor(config.retryPolicy))
        .addInterceptor { chain ->
            val start = System.nanoTime()
            val response = chain.proceed(chain.request())
            // Whitelist metadata only: no URLs (including paths/query), headers or payloads.
            if (config.debugLogging) Timber.d("Network method=%s code=%d costMs=%d",
                chain.request().method, response.code, (System.nanoTime() - start) / 1_000_000)
            response
        }
        // Network interceptors run again after redirects; custom auth headers must not cross origins.
        .addNetworkInterceptor { chain ->
            val request = chain.request()
            if (sameOrigin(request.url, config.baseUrl)) chain.proceed(request) else {
                val builder = request.newBuilder().removeHeader("Authorization")
                config.headers.keys.forEach { builder.removeHeader(it) }
                chain.proceed(builder.build())
            }
        }
        .authenticator(RefreshAuthenticator(config))
        .build()
    val retrofit: Retrofit = Retrofit.Builder().baseUrl(config.baseUrl).client(okHttp)
        .addConverterFactory(GsonConverterFactory.create()).build()
    fun <T> create(service: Class<T>): T = retrofit.create(service)
}
internal fun sameOrigin(a: HttpUrl, b: HttpUrl) = a.scheme == b.scheme && a.host == b.host && a.port == b.port
internal fun replayable(request: Request): Boolean = request.method in setOf("GET", "HEAD", "PUT", "DELETE", "OPTIONS") &&
    request.body?.isOneShot() != true && request.body?.isDuplex() != true

/**
 * 内部重试拦截器。仅重放幂等且可重复发送的请求，限制次数与退避；响应关闭后才重试，并检查取消。
 */
internal class RetryInterceptor(private val policy: RetryPolicy) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        while (true) {
            try {
                val response = chain.proceed(request)
                if (!replayable(request) || attempt >= policy.maxRetries || response.code !in setOf(502, 503, 504)) return response
                response.close()
            } catch (error: IOException) {
                if (chain.call().isCanceled() || !replayable(request) || attempt >= policy.maxRetries) throw error
            }
            var remaining = minOf(policy.backoffMillis * (1L shl attempt++), policy.maxBackoffMillis)
            while (remaining > 0) {
                if (chain.call().isCanceled()) throw IOException("Canceled")
                val step = minOf(remaining, 50L)
                try { Thread.sleep(step) } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt(); throw IOException("Interrupted", e)
                }
                remaining -= step
            }
        }
    }
}
/**
 * 内部 401 认证处理器。同一客户端串行协调 Token 刷新，限制重放并对失败 Token 短暂冷却；不承担登录 UI。
 */
internal class RefreshAuthenticator(private val config: NetworkConfig) : Authenticator {
    private val lock = Any()
    // Failed refresh is shared by concurrent calls; a short cooldown permits later recovery.
    private var failedAuthorization: String? = null
    private var failedAtNanos = 0L
    override fun authenticate(route: Route?, response: Response): Request? {
        var count = 1
        var previous = response.priorResponse
        while (previous != null) { count++; previous = previous.priorResponse }
        if (count >= 2 || !sameOrigin(response.request.url, config.baseUrl) || !replayable(response.request)) return null
        val provider = config.tokenProvider ?: return null
        val refresher = config.tokenRefresher ?: return null
        val failed = response.request.header("Authorization") ?: return null
        return synchronized(lock) {
            val current = provider.getToken()
            val token = if (current != null && "Bearer $current" != failed) current else {
                if (failedAuthorization == failed && System.nanoTime() - failedAtNanos < TimeUnit.SECONDS.toNanos(5)) return@synchronized null
                val refreshed = try { refresher.refreshToken() } catch (_: Exception) { null }
                if (refreshed.isNullOrBlank() || "Bearer $refreshed" == failed) {
                    failedAuthorization = failed; failedAtNanos = System.nanoTime(); return@synchronized null
                }
                refreshed
            }
            response.request.newBuilder().header("Authorization", "Bearer $token").build()
        }
    }
}
