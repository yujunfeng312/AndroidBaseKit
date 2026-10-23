package dev.androidbasekit.network

import dev.androidbasekit.common.ApiResult
import dev.androidbasekit.common.AppError

/**
 * Request state does not decide whether a page keeps its existing content.
 *
 * 请求执行状态。包含空闲、执行中、成功、失败、取消；与页面是否保留旧内容分开处理。
 */
enum class RequestStatus { IDLE, RUNNING, SUCCESS, ERROR, CANCELLED }
/**
 * 单次请求状态快照。提供 status、result 以及便捷 data/error 属性，通过 BaseRequest.state 观察。
 */
class RequestState<T> internal constructor(val status: RequestStatus, val result: ApiResult<T>? = null) {
    val data: T? get() = result?.getOrNull()
    val error: AppError? get() = result?.errorOrNull()
}
