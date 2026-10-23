package dev.androidbasekit.common

import java.math.BigDecimal
import java.math.RoundingMode

/** 按 1024 换算的 IEC 文件大小显示，单位 B/KiB/MiB/GiB/TiB/PiB/EiB。 */
object ByteSizes {
    /** 小数位 0..6、HALF_UP；B 始终显示整数，单位按舍入前的原始字节数选择。 */
    @JvmStatic @JvmOverloads fun format(bytes: Long, fractionDigits: Int = 2): String {
        require(bytes >= 0) { "bytes must be non-negative" }
        require(fractionDigits in 0..6) { "fractionDigits must be in 0..6" }
        val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB")
        var unit = 0
        var divisor = 1L
        while (unit < units.lastIndex && bytes / divisor >= 1024) {
            divisor *= 1024
            unit++
        }
        if (unit == 0) return "$bytes B"
        val number = BigDecimal.valueOf(bytes).divide(BigDecimal.valueOf(divisor), fractionDigits, RoundingMode.HALF_UP)
        return "${number.toPlainString()} ${units[unit]}"
    }
}
