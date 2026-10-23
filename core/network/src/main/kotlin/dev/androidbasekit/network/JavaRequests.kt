package dev.androidbasekit.network

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.androidbasekit.common.ApiResult
import dev.androidbasekit.common.AppError
import dev.androidbasekit.common.ErrorKind
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Java 请求取消句柄。取消对应任务及其 Retrofit Call；取消不回调为普通业务失败。
 */
fun interface CancelHandle { fun cancel() }
/**
 * Java 请求结果接口。成功返回数据，失败返回 AppError；由 JavaRequests 在主线程交付。
 */
interface ResultCallback<T> {
    fun onSuccess(value: T)
    fun onError(error: AppError)
}
/**
 * Creates one fresh Retrofit Call per request. Callback delivery uses Dispatchers.Main.
 *
 * Java 生命周期请求桥接。绑定 LifecycleOwner、主线程交付回调、销毁时取消；每次传入新的 Retrofit Call。
 */
object JavaRequests {
    @androidx.annotation.MainThread
    @JvmStatic fun <T : Any> enqueue(owner: LifecycleOwner, call: Call<T>, callback: ResultCallback<T>): CancelHandle {
        check(owner.lifecycle.currentState != Lifecycle.State.DESTROYED) { "Owner already destroyed" }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        lateinit var observer: LifecycleEventObserver
        observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) { scope.cancel(); call.cancel() }
        }
        owner.lifecycle.addObserver(observer)
        val job = scope.launch {
            try {
                when (val result = ApiErrorMapper.execute { await(call) }) {
                    is ApiResult.Success -> callback.onSuccess(result.data)
                    is ApiResult.Failure -> callback.onError(result.error)
                }
            } finally { owner.lifecycle.removeObserver(observer); scope.cancel() }
        }
        return CancelHandle { call.cancel(); job.cancel() }
    }
    suspend fun <T : Any> await(call: Call<T>): T = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (!continuation.isActive) { response.errorBody()?.close(); (response.body() as? java.io.Closeable)?.close(); return }
                if (!response.isSuccessful) continuation.resumeWithException(retrofit2.HttpException(response))
                else response.body()?.let { continuation.resume(it) }
                    ?: continuation.resumeWithException(EmptyBodyException())
            }
            override fun onFailure(call: Call<T>, error: Throwable) {
                if (continuation.isActive) continuation.resumeWithException(error)
            }
        })
    }
}
/**
 * HTTP 成功但缺少预期响应体的异常。用于错误映射，避免将空响应误当作非空业务数据。
 */
class EmptyBodyException : java.io.IOException("Empty response body")
