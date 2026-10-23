package dev.androidbasekit.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * 精确十进制与本地化数字格式工具。输入 BigDecimal，避免 Double 引入金额精度损失。
 * 每次创建 NumberFormat，支持并发调用；这里只格式化，不推断货币、税率或业务计价规则。
 */
object Numbers {
    /** 固定小数位、不带分组的十进制文本；支持 0..100 位，默认 HALF_UP。 */
    @JvmStatic @JvmOverloads fun decimal(
        value: BigDecimal, scale: Int, roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): String {
        require(scale in 0..100) { "scale must be in 0..100" }
        return value.setScale(scale, roundingMode).toPlainString()
    }

    /** 按地区显示分组和小数分隔符，固定小数位；非法精度抛异常。 */
    @JvmStatic @JvmOverloads fun grouped(
        value: BigDecimal, scale: Int = 2, locale: Locale = Locale.getDefault()
    ): String {
        require(scale in 0..100) { "scale must be in 0..100" }
        return NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = true
            minimumFractionDigits = scale
            maximumFractionDigits = scale
            roundingMode = RoundingMode.HALF_UP
        }.format(value)
    }
}
