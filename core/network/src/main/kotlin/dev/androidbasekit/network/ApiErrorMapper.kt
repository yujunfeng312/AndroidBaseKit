package dev.androidbasekit.network

import com.google.gson.JsonParseException
import dev.androidbasekit.common.*
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 异常与请求结果的转换入口。区分网络、超时、HTTP、JSON 解析及业务异常；保留协程取消语义。
 */
object ApiErrorMapper {
    @JvmStatic fun map(error: Exception): AppError = when (error) {
        is CancellationException -> throw error
        is BusinessException -> AppError(ErrorKind.BUSINESS, error.code)
        is HttpException -> { error.response()?.errorBody()?.close(); AppError(ErrorKind.HTTP, error.code().toString()) }
        is java.io.InterruptedIOException -> AppError(ErrorKind.TIMEOUT)
        is com.google.gson.stream.MalformedJsonException, is EmptyBodyException, is JsonParseException, is IllegalStateException -> AppError(ErrorKind.PARSE)
        is IOException -> AppError(ErrorKind.NETWORK)
        else -> AppError(ErrorKind.UNKNOWN)
    }
    suspend fun <T> execute(block: suspend () -> T): ApiResult<T> = try {
        ApiResult.success(block())
    } catch (cancelled: CancellationException) { throw cancelled
    } catch (error: Exception) { ApiResult.failure(map(error)) }
}
