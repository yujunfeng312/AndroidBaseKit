package dev.androidbasekit.common

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 毫秒时间戳与文本互转。每次创建独立 formatter，避免 SimpleDateFormat 并发共享问题，兼容 API 24。
 * 默认使用当前地区和时区；服务端固定格式应显式传 Locale 与 TimeZone，不猜测日期单位。
 */
object Dates {
    /** pattern 使用 SimpleDateFormat 语法；非法 pattern 抛 IllegalArgumentException。 */
    @JvmStatic @JvmOverloads fun format(
        epochMillis: Long, pattern: String, locale: Locale = Locale.getDefault(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String = formatter(pattern, locale, timeZone).format(Date(epochMillis))

    /** 严格解析完整文本；无效日期、空文本、尾随内容返回 null，非法 pattern 仍抛异常。 */
    @JvmStatic @JvmOverloads fun parse(
        text: String, pattern: String, locale: Locale = Locale.getDefault(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long? {
        val parser = formatter(pattern, locale, timeZone)
        val position = ParsePosition(0)
        val date = parser.parse(text, position) ?: return null
        return if (position.index == text.length) date.time else null
    }

    private fun formatter(pattern: String, locale: Locale, zone: TimeZone): SimpleDateFormat {
        require(pattern.isNotBlank()) { "pattern must not be blank" }
        return SimpleDateFormat(pattern, locale).apply { isLenient = false; timeZone = zone }
    }
}
