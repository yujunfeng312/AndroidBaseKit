package dev.androidbasekit.common

/**
 * 框架默认配置常量。提供最低 API 和默认网络超时；Android 构建 SDK 以 Gradle 配置为准。
 */
object AppConfig {
    const val MIN_SDK = 24
    const val DEFAULT_TIMEOUT_SECONDS = 30L
}
