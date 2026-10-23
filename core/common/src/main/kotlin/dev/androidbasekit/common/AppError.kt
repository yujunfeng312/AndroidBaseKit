package dev.androidbasekit.common

/**
 * 错误分类枚举。区分 HTTP、网络、超时、解析、业务、缓存和未知错误，供上层统一展示。
 */
enum class ErrorKind { HTTP, NETWORK, TIMEOUT, PARSE, BUSINESS, CACHE, UNKNOWN }
/**
 * technicalCode is for diagnostics; translate ErrorKind in the business UI. Never display raw exception bodies.
 *
 * 结构化错误信息。kind 表示分类，technicalCode 仅用于诊断；不要将异常响应正文直接展示给用户。
 */
data class AppError @JvmOverloads constructor(val kind: ErrorKind, val technicalCode: String? = null)
/**
 * 业务失败信号，保存服务端业务码。交给 ApiErrorMapper 映射为 BUSINESS，不保存完整响应。
 */
class BusinessException(val code: String) : Exception("Business failure")
