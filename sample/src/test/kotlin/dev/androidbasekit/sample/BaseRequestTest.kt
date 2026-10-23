package dev.androidbasekit.sample

import android.app.Application
import androidx.lifecycle.*
import dev.androidbasekit.common.*
import dev.androidbasekit.network.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * BaseRequestTest 回归测试集。通过测试方法验证对应模块行为、错误与边界条件；测试结果见验收说明。
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class BaseRequestTest {
    private lateinit var scope: CoroutineScope
    @Before fun start() { Dispatchers.setMain(UnconfinedTestDispatcher()); scope = CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    @After fun stop() { scope.cancel(); Dispatchers.resetMain() }
    @Test fun requestMovesFromRunningToSuccess() {
        val pending = CompletableDeferred<String>(); val request = BaseRequest<String>(scope)
        assertEquals(RequestStatus.IDLE, request.state.value?.status)
        request.launch { pending.await() }
        assertEquals(RequestStatus.RUNNING, request.state.value?.status)
        pending.complete("done")
        assertEquals(RequestStatus.SUCCESS, request.state.value?.status); assertEquals("done", request.state.value?.data)
    }
    @Test fun cancelledOldRequestCannotOverwriteNewResult() {
        val pending = CompletableDeferred<String>(); val request = BaseRequest<String>(scope)
        request.launch { withContext(NonCancellable) { pending.await() } }
        request.launch { "new" }
        pending.complete("old")
        assertEquals(RequestStatus.SUCCESS, request.state.value?.status); assertEquals("new", request.state.value?.data)
    }
    @Test fun explicitCancellationIsNotAnError() {
        val pending = CompletableDeferred<String>(); val request = BaseRequest<String>(scope)
        request.launch { pending.await() }.cancel()
        assertEquals(RequestStatus.CANCELLED, request.state.value?.status); assertNull(request.state.value?.error)
        assertTrue(scope.isActive)
    }
    @Test fun repositoryFailureIsPreserved() {
        val request = BaseRequest<String>(scope)
        request.launchResult { ApiResult.failure(AppError(ErrorKind.BUSINESS, "quota")) }
        assertEquals(RequestStatus.ERROR, request.state.value?.status)
        assertEquals("quota", request.state.value?.error?.technicalCode)
    }
    @Test fun repositoryExceptionBecomesNetworkError() {
        val request = BaseRequest<String>(scope)
        request.launchResult { throw java.io.IOException("private") }
        assertEquals(RequestStatus.ERROR, request.state.value?.status)
        assertEquals(AppError(ErrorKind.NETWORK), request.state.value?.error)
    }
    @Test fun ownerDestroyedCancelsRequest() {
        val owner = Owner(); owner.registry.currentState = Lifecycle.State.STARTED
        val pending = CompletableDeferred<String>(); val request = BaseRequest.forOwner<String>(owner)
        request.launch { pending.await() }; owner.registry.currentState = Lifecycle.State.DESTROYED
        assertEquals(RequestStatus.CANCELLED, request.state.value?.status); assertNull(request.state.value?.error)
    }
    @Test fun cancellingCompletedRequestDoesNotEraseResult() {
        val request = BaseRequest<String>(scope); val handle = request.launch { "done" }
        handle.cancel(); request.cancel()
        assertEquals(RequestStatus.SUCCESS, request.state.value?.status); assertEquals("done", request.state.value?.data)
    }
    /**
     * 测试用 LifecycleOwner。手动驱动生命周期事件，验证请求销毁取消。
     */
    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
}
