package dev.androidbasekit.common

/**
 * 统一的成功/失败结果。提供 Success、Failure 及 Java 静态工厂；协程取消应继续抛出，不包装成失败。
 */
sealed class ApiResult<out T> {
    /**
     * 成功结果，保存业务数据 data。调用方可通过 ApiResult.getOrNull 读取。
     */
    data class Success<T>(val data: T) : ApiResult<T>()
    /**
     * 失败结果，保存结构化 AppError。由 UI 根据错误类型选择本地化提示。
     */
    data class Failure(val error: AppError) : ApiResult<Nothing>()
    fun getOrNull(): T? = (this as? Success<T>)?.data
    fun errorOrNull(): AppError? = (this as? Failure)?.error
    fun isSuccess(): Boolean = this is Success
    companion object {
        @JvmStatic fun <T> success(value: T): ApiResult<T> = Success(value)
        @JvmStatic fun <T> failure(error: AppError): ApiResult<T> = Failure(error)
    }
}
