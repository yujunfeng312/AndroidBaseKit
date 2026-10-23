package dev.androidbasekit.network

import dev.androidbasekit.common.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import retrofit2.http.GET
import java.io.IOException
import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * NetworkClientTest 回归测试集。通过测试方法验证对应模块行为、错误与边界条件；测试结果见验收说明。
 */
class NetworkClientTest {
    private lateinit var server: MockWebServer
    /**
     * 网络测试响应模型。用于验证 JSON 转换和响应体错误。
     */
    data class Payload(val value: String)
    /**
     * MockWebServer 测试 API。用于验证网络链路，不访问外部业务服务。
     */
    interface Service { @GET("data") suspend fun data(): Payload }
    @Before fun start() { server = MockWebServer(); server.start() }
    @After fun stop() { server.shutdown() }
    private fun client(policy: RetryPolicy = RetryPolicy()) = NetworkClient(NetworkConfig(server.url("/").toString(), retryPolicy = policy))
    @Test fun suspendRequestParsesJson() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"value":"ok"}"""))
        assertEquals("ok", client().create(Service::class.java).data().value)
    }
    @Test fun httpErrorMappedWithoutBodyLeak() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(403).setBody("secret"))
        val result = ApiErrorMapper.execute { client().create(Service::class.java).data() }
        assertEquals(AppError(ErrorKind.HTTP, "403"), result.errorOrNull())
    }
    @Test fun malformedJsonMapped() = runBlocking {
        server.enqueue(MockResponse().setBody("not json"))
        val result = ApiErrorMapper.execute { client().create(Service::class.java).data() }
        assertEquals(ErrorKind.PARSE, result.errorOrNull()?.kind)
    }
    @Test fun cancellationPropagates() = runBlocking {
        try { ApiErrorMapper.execute<String> { throw CancellationException("cancel") }; fail("Cancellation swallowed") }
        catch (_: CancellationException) { }
    }
    @Test fun cancellingSuspendRequestStopsCall() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"value":"late"}""").setBodyDelay(3, TimeUnit.SECONDS))
        val job = launch(Dispatchers.IO) { ApiErrorMapper.execute { client().create(Service::class.java).data() } }
        withContext(Dispatchers.IO) { assertNotNull(server.takeRequest(2, TimeUnit.SECONDS)) }
        job.cancelAndJoin(); assertTrue(job.isCancelled)
    }
    @Test fun getRetriesBounded() {
        server.enqueue(MockResponse().setResponseCode(503)); server.enqueue(MockResponse().setResponseCode(503)); server.enqueue(MockResponse().setBody("done"))
        val c = client(RetryPolicy(1, 0, 0))
        c.okHttp.newCall(Request.Builder().url(server.url("/data")).build()).execute().use { assertEquals(503, it.code) }
        assertEquals(2, server.requestCount)
    }
    @Test fun postNeverRetries() {
        server.enqueue(MockResponse().setResponseCode(503)); server.enqueue(MockResponse().setBody("unexpected"))
        client(RetryPolicy(2, 0, 0)).okHttp.newCall(Request.Builder().url(server.url("/data")).post("body".toRequestBody()).build())
            .execute().use { assertEquals(503, it.code) }
        assertEquals(1, server.requestCount)
    }
    @Test fun defaultRetriesDisabled() {
        server.enqueue(MockResponse().setResponseCode(503))
        client().okHttp.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
        assertEquals(1, server.requestCount)
    }
    @Test fun retrySucceedsOnSecondGet() {
        server.enqueue(MockResponse().setResponseCode(502)); server.enqueue(MockResponse().setBody("done"))
        client(RetryPolicy(1, 0, 0)).okHttp.newCall(Request.Builder().url(server.url("/")).build()).execute().use { assertEquals("done", it.body.string()) }
        assertEquals(2, server.requestCount)
    }
    @Test fun timeoutMapsSeparately() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"value":"slow"}""").setBodyDelay(2, TimeUnit.SECONDS))
        val c = NetworkClient(NetworkConfig(server.url("/").toString(), timeoutSeconds = 1))
        val result = ApiErrorMapper.execute { c.create(Service::class.java).data() }
        assertEquals(ErrorKind.TIMEOUT, result.errorOrNull()?.kind)
    }
    @Test fun headerAndTokenInjected() {
        server.enqueue(MockResponse().setBody("ok"))
        val c = NetworkClient(NetworkConfig(server.url("/").toString(), TokenProvider { "private" }, headers = mapOf("X-App" to "sample")))
        c.okHttp.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
        val request = server.takeRequest(); assertEquals("Bearer private", request.getHeader("Authorization")); assertEquals("sample", request.getHeader("X-App"))
    }
    @Test fun credentialsNotSentToOtherOrigin() {
        val other = MockWebServer(); other.start()
        try {
            other.enqueue(MockResponse().setBody("ok"))
            val c = NetworkClient(NetworkConfig(server.url("/").toString(), TokenProvider { "private" }, headers = mapOf("X-Secret" to "secret")))
            c.okHttp.newCall(Request.Builder().url(other.url("/")).build()).execute().close()
            val request = other.takeRequest(); assertNull(request.getHeader("Authorization")); assertNull(request.getHeader("X-Secret"))
        } finally { other.shutdown() }
    }
    @Test fun tokenRefreshIsSingleFlight() {
        val token = AtomicReference("old"); val refreshes = AtomicInteger()
        val arrived = CountDownLatch(2)
        server.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.getHeader("Authorization") == "Bearer old") {
                    arrived.countDown(); arrived.await(3, TimeUnit.SECONDS)
                    return MockResponse().setResponseCode(401)
                }
                return MockResponse().setBody("ok")
            }
        }
        val c = NetworkClient(NetworkConfig(server.url("/").toString(), TokenProvider { token.get() }, TokenRefresher {
            refreshes.incrementAndGet(); token.set("new"); "new"
        }))
        val executor = Executors.newFixedThreadPool(2)
        try {
            val tasks = (1..2).map { executor.submit<Int> { c.okHttp.newCall(Request.Builder().url(server.url("/")).build()).execute().use { it.code } } }
            tasks.forEach { assertEquals(200, it.get(5, TimeUnit.SECONDS).toInt()) }
            assertEquals(1, refreshes.get())
        } finally { executor.shutdownNow() }
    }
    @Test fun failedRefreshDoesNotLoopOrRepeat() {
        repeat(2) { server.enqueue(MockResponse().setResponseCode(401)) }
        val refreshes = AtomicInteger()
        val c = NetworkClient(NetworkConfig(server.url("/").toString(), TokenProvider { "old" }, TokenRefresher { refreshes.incrementAndGet(); null }))
        repeat(2) { c.okHttp.newCall(Request.Builder().url(server.url("/")).build()).execute().use { assertEquals(401, it.code) } }
        assertEquals(1, refreshes.get()); assertEquals(2, server.requestCount)
    }
    @Test fun refreshedTokenStillRejectedStops() {
        repeat(2) { server.enqueue(MockResponse().setResponseCode(401)) }
        val token = AtomicReference("old")
        val c = NetworkClient(NetworkConfig(server.url("/").toString(), TokenProvider { token.get() }, TokenRefresher { token.set("new"); "new" }))
        c.okHttp.newCall(Request.Builder().url(server.url("/")).build()).execute().use { assertEquals(401, it.code) }
        assertEquals(2, server.requestCount)
    }
    @Test fun uploadAndDownloadProgress() {
        var sent = 0L; var uploaded = false
        val upload = ProgressRequestBody("hello".toRequestBody(), ProgressListener { bytes, total, done -> sent = bytes; uploaded = done; assertEquals(5L, total) })
        val sink = Buffer(); upload.writeTo(sink); assertEquals(5L, sent); assertTrue(uploaded)
        var downloaded = false; var received = 0L
        val download = ProgressResponseBody("world".toResponseBody(), ProgressListener { bytes, _, done -> received = bytes; downloaded = done })
        download.use { assertEquals("world", it.string()) }; assertEquals(5L, received); assertTrue(downloaded)
    }
    @Test fun businessAndNetworkErrorsAreDistinct() {
        assertEquals(AppError(ErrorKind.BUSINESS, "quota"), ApiErrorMapper.map(BusinessException("quota")))
        assertEquals(ErrorKind.NETWORK, ApiErrorMapper.map(IOException()).kind)
        assertEquals(ErrorKind.TIMEOUT, ApiErrorMapper.map(java.net.SocketTimeoutException()).kind)
    }
    @Test fun redirectStripsCustomCredentials() {
        val other = MockWebServer(); other.start()
        try {
            server.enqueue(MockResponse().setResponseCode(302).addHeader("Location", other.url("/redirect?secret=private")))
            other.enqueue(MockResponse().setBody("ok"))
            val c = NetworkClient(NetworkConfig(server.url("/").toString(), TokenProvider { "private" }, headers = mapOf("X-Secret" to "private")))
            c.okHttp.newCall(Request.Builder().url(server.url("/")).build()).execute().use { assertEquals(200, it.code) }
            val request = other.takeRequest(); assertNull(request.getHeader("Authorization")); assertNull(request.getHeader("X-Secret"))
        } finally { other.shutdown() }
    }
    @Test fun oneShotPutNeverRetries() {
        server.enqueue(MockResponse().setResponseCode(503))
        val body = object : RequestBody() {
            override fun contentType(): MediaType? = null
            override fun isOneShot() = true
            override fun writeTo(sink: okio.BufferedSink) { sink.writeUtf8("one shot") }
        }
        client(RetryPolicy(2, 0, 0)).okHttp.newCall(Request.Builder().url(server.url("/")).put(body).build()).execute().use { assertEquals(503, it.code) }
        assertEquals(1, server.requestCount)
    }
    @Test fun logNeverContainsCredentialsUrlOrBodies() {
        val logs = mutableListOf<String>()
        val tree = object : timber.log.Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) { logs.add(message) }
        }
        timber.log.Timber.plant(tree)
        try {
            server.enqueue(MockResponse().setBody("private response"))
            val c = NetworkClient(NetworkConfig(server.url("/").toString(), TokenProvider { "private token" }, debugLogging = true))
            c.okHttp.newCall(Request.Builder().url(server.url("/private?token=private")).build()).execute().close()
            assertTrue(logs.isNotEmpty()); assertFalse(logs.joinToString().contains("private")); assertTrue(logs.single().contains("code=200"))
        } finally { timber.log.Timber.uproot(tree) }
    }

}
