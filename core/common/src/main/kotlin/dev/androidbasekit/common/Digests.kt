package dev.androidbasekit.common

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

/**
 * SHA-256 摘要工具。支持 UTF-8 文本、字节和流式文件校验，输出 64 位小写十六进制。
 * 摘要不是加密，不用于密码存储；文件和流操作在后台执行，IO 异常原样传播。
 */
object Digests {
    @JvmStatic fun sha256(text: String): String = sha256(text.toByteArray(Charsets.UTF_8))
    @JvmStatic fun sha256(bytes: ByteArray): String = hex(MessageDigest.getInstance("SHA-256").digest(bytes))

    /** 从流的当前位置读到 EOF；不关闭调用方提供的流。 */
    @JvmStatic fun sha256(stream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            if (count == 0) {
                val single = stream.read()
                if (single < 0) break
                digest.update(single.toByte())
            } else digest.update(buffer, 0, count)
        }
        return hex(digest.digest())
    }

    /** 自行打开并关闭文件流，不将整个文件载入内存。 */
    @JvmStatic fun sha256(file: File): String = file.inputStream().use { sha256(it) }

    private fun hex(bytes: ByteArray): String {
        val digits = "0123456789abcdef"
        return buildString(bytes.size * 2) {
            bytes.forEach { byte ->
                val value = byte.toInt() and 0xff
                append(digits[value ushr 4]); append(digits[value and 0x0f])
            }
        }
    }
}
