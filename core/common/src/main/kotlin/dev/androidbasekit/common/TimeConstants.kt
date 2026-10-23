package dev.androidbasekit.common

/**
 * 时间单位常量，统一使用毫秒。用于缓存 TTL 和超时计算，避免秒与毫秒混用。
 */
object TimeConstants {
    const val SECOND_MS = 1_000L
    const val MINUTE_MS = 60_000L
}
