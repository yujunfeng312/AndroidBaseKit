package dev.androidbasekit.common

/**
 * HTTP 通用状态码常量。集中定义未授权、限流等协议码；业务成功码由 BaseResponse 的调用方配置。
 */
object ApiCodes {
    const val HTTP_UNAUTHORIZED = 401
    const val HTTP_TOO_MANY_REQUESTS = 429
}
