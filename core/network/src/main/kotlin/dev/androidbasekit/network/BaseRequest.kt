package dev.androidbasekit.network

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import dev.androidbasekit.common.ApiResult
import kotlinx.coroutines.*
import retrofit2.Call

/**
 * One independently cancellable request channel. All launch/cancel/state operations belong on Main.
 *
 * 有生命周期作用域的请求控制器。提供 Kotlin launch/launchResult、Java enqueue/enqueueResponse、取消和 LiveData 状态；新请求覆盖旧请求，UI 状态更新在主线程调用。
 */
class BaseRequest<T : Any>(private val scope: CoroutineScope) {
    private val mutableState = MutableLiveData(RequestState<T>(RequestStatus.IDLE))
    val state: LiveData<RequestState<T>> = mutableState
    private var activeJob: Job? = null
    private var generation = 0L

    @MainThread fun enqueue(call: Call<T>): CancelHandle = launch { JavaRequests.await(call) }

    @MainThread @JvmOverloads fun enqueueResponse(call: Call<BaseResponse<T>>, successCode: Int = 0): CancelHandle =
        launch { JavaRequests.await(call).requireData(successCode) }

    /** Kotlin suspend API, mapped to ApiResult. Work that blocks must switch to Dispatchers.IO. */
    @MainThread fun launch(block: suspend () -> T): CancelHandle = launchResult { ApiErrorMapper.execute(block) }

    /** Use when the Repository already returns ApiResult, without mapping it twice. */
    @MainThread fun launchResult(block: suspend () -> ApiResult<T>): CancelHandle {
        scope.coroutineContext.ensureActive()
        val id = ++generation
        activeJob?.cancel()
        mutableState.value = RequestState(RequestStatus.RUNNING)
        val job = scope.launch(Dispatchers.Main.immediate, start = CoroutineStart.UNDISPATCHED) {
            try {
                ensureActive()
                val outcome = try { block() } catch (cancelled: CancellationException) { throw cancelled
                } catch (error: Exception) { ApiResult.failure<T>(ApiErrorMapper.map(error)) }
                ensureActive()
                if (id == generation) {
                    mutableState.value = RequestState(
                        if (outcome.isSuccess()) RequestStatus.SUCCESS else RequestStatus.ERROR, outcome)
                }
            } catch (cancelled: CancellationException) {
                if (id == generation) mutableState.value = RequestState(RequestStatus.CANCELLED)
                throw cancelled
            } finally { if (id == generation) activeJob = null }
        }
        if (!job.isCompleted && id == generation) activeJob = job
        return CancelHandle { job.cancel() }
    }

    @MainThread fun cancel() { activeJob?.cancel() }

    companion object {
        /** Fragment callers must supply viewLifecycleOwner; no UI owner is retained as a global reference. */
        @JvmStatic @MainThread fun <T : Any> forOwner(owner: LifecycleOwner): BaseRequest<T> {
            check(owner.lifecycle.currentState != Lifecycle.State.DESTROYED) { "Owner already destroyed" }
            return BaseRequest(owner.lifecycleScope)
        }
    }
}
