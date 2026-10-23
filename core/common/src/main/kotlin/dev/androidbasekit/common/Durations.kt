package dev.androidbasekit.common

import java.util.Locale

/** 非负时长显示；明确区分秒与毫秒，不参与日期、时区或倒计时调度。 */
object Durations {
    /** 小于一小时为 mm:ss，否则为 H:mm:ss；小时不按天回绕。 */
    @JvmStatic fun formatSeconds(seconds: Long): String {
        require(seconds >= 0) { "seconds must be non-negative" }
        val hours = seconds / 3600
        val minutes = seconds / 60 % 60
        val remaining = seconds % 60
        return if (hours == 0L) String.format(Locale.ROOT, "%02d:%02d", minutes, remaining)
        else String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, remaining)
    }

    /** 丢弃不足一秒的部分；负数抛 IllegalArgumentException。 */
    @JvmStatic fun formatMillis(millis: Long): String {
        require(millis >= 0) { "millis must be non-negative" }
        return formatSeconds(millis / 1000)
    }
}
