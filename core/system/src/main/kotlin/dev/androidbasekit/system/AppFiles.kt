package dev.androidbasekit.system

import android.content.Context
import java.io.File

/**
 * 应用缓存目录临时文件工具。创建 basekit_ 前缀文件、清理过期文件；文件 IO 在后台执行，不遍历业务目录。
 */
object AppFiles {
    @JvmStatic @JvmOverloads fun createTemp(context: Context, suffix: String = ".tmp"): File {
        require(!suffix.contains('/') && !suffix.contains('\\'))
        return File.createTempFile("basekit_", suffix, context.applicationContext.cacheDir)
    }
    /** IO operation; call from a worker. Removes only framework-prefixed files. */
    @JvmStatic fun deleteExpiredTemp(context: Context, maxAgeMillis: Long): Int {
        require(maxAgeMillis >= 0)
        val cutoff = System.currentTimeMillis() - maxAgeMillis
        return context.applicationContext.cacheDir.listFiles()?.count {
            it.isFile && it.name.startsWith("basekit_") && it.lastModified() <= cutoff && it.delete()
        } ?: 0
    }
}
