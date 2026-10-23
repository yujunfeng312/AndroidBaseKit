package dev.androidbasekit.common

import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder

/** HTTP(S) 地址语法和查询参数工具，不补协议、不访问网络、不判断目标是否可信。 */
object WebUrls {
    /** 绝对 HTTP(S)、非空可解析主机、无用户凭证；端口限定 0..65535 或省略。 */
    @JvmStatic fun isHttpUrl(value: String?): Boolean {
        if (value == null) return false
        return try {
            val uri = URI(value)
            (uri.scheme.equals("http", true) || uri.scheme.equals("https", true)) &&
                !uri.host.isNullOrBlank() && uri.rawUserInfo == null && uri.port in -1..65535
        } catch (_: URISyntaxException) { false }
    }

    /** 编码单个 UTF-8 查询参数名或值；空格为 %20，不对完整 URL 使用。 */
    @JvmStatic fun encodeQueryComponent(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    /** 追加参数，保留原查询和 fragment；同名参数不替换，输入必须为有效 HTTP(S) 地址。 */
    @JvmStatic fun appendQueryParameter(url: String, name: String, value: String): String {
        require(isHttpUrl(url)) { "An absolute HTTP(S) URL without user info is required" }
        val fragmentIndex = url.indexOf('#').let { if (it < 0) url.length else it }
        val base = url.substring(0, fragmentIndex)
        val separator = when {
            '?' !in base -> "?"
            base.endsWith('?') || base.endsWith('&') -> ""
            else -> "&"
        }
        return base + separator + encodeQueryComponent(name) + "=" + encodeQueryComponent(value) + url.substring(fragmentIndex)
    }
}
