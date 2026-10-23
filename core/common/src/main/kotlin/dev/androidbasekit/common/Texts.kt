package dev.androidbasekit.common

/**
 * 常用文本工具。提供空白判断、回退值和按 Unicode 码点截断；Java 可直接调用静态方法。
 * 截断不会拆开代理对，但不保证组合字符或多个码点组成的 emoji 整体保留。
 */
object Texts {
    /** null、空字符串或全部空白字符都返回 true。 */
    @JvmStatic fun isBlank(value: CharSequence?): Boolean = value.isNullOrBlank()

    /** 非空白值原样保留，不自动 trim。 */
    @JvmStatic fun orDefault(value: String?, fallback: String): String =
        if (value.isNullOrBlank()) fallback else value

    /** 最多保留 maxCodePoints 个码点；suffix 不计入长度，仅在确实截断时添加。 */
    @JvmStatic @JvmOverloads fun truncate(value: String, maxCodePoints: Int, suffix: String = "…"): String {
        require(maxCodePoints >= 0) { "maxCodePoints must be non-negative" }
        if (value.codePointCount(0, value.length) <= maxCodePoints) return value
        return value.substring(0, value.offsetByCodePoints(0, maxCodePoints)) + suffix
    }
}
