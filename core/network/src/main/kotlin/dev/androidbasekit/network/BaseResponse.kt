package dev.androidbasekit.network

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonParseException
import dev.androidbasekit.common.BusinessException

/**
 * Optional envelope convention. Configure successCode for your server; missing code is not success.
 *
 * 可选的 code/message/data 服务端响应模型。支持自定义成功码和非空数据校验；无统一包裹结构的接口直接使用 ApiResult。
 */
data class BaseResponse<T> @JvmOverloads constructor(
    @field:SerializedName("code") val code: Int? = null,
    @field:SerializedName("message") val message: String? = null,
    @field:SerializedName("data") val data: T? = null
) {
    @JvmOverloads fun checkSuccess(successCode: Int = 0) {
        val actualCode = code ?: throw JsonParseException("Missing business code")
        if (actualCode != successCode) throw BusinessException(actualCode.toString())
    }
    @JvmOverloads fun requireData(successCode: Int = 0): T {
        checkSuccess(successCode)
        return data ?: throw EmptyBodyException()
    }
}
