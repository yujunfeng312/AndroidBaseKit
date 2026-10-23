package dev.androidbasekit.network

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.*

/**
 * Invoked on the transfer thread. totalBytes == -1 means unknown; dispatch/throttle UI updates in business code.
 *
 * 上传或下载字节进度回调。在传输线程触发，totalBytes 为 -1 表示未知；UI 更新需自行切主线程和节流。
 */
fun interface ProgressListener { fun onProgress(transferredBytes: Long, totalBytes: Long, done: Boolean) }
/**
 * 上传请求体装饰器。统计写入字节并报告完成，透传内容类型、长度和 one-shot/duplex 语义。
 */
class ProgressRequestBody(private val delegate: RequestBody, private val listener: ProgressListener) : RequestBody() {
    override fun contentType(): MediaType? = delegate.contentType()
    override fun contentLength(): Long = delegate.contentLength()
    override fun isOneShot(): Boolean = delegate.isOneShot()
    override fun isDuplex(): Boolean = delegate.isDuplex()
    override fun writeTo(sink: BufferedSink) {
        val total = contentLength()
        var sent = 0L
        val forwarding = object : ForwardingSink(sink) {
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount); sent += byteCount; listener.onProgress(sent, total, false)
            }
        }.buffer()
        delegate.writeTo(forwarding); forwarding.flush(); listener.onProgress(sent, total, true)
    }
}
/**
 * 下载响应体装饰器。统计实际读取的字节，读到 EOF 才报告完成；调用方仍须关闭响应体。
 */
class ProgressResponseBody(private val delegate: ResponseBody, listener: ProgressListener) : ResponseBody() {
    private val progressSource = object : ForwardingSource(delegate.source()) {
        private var received = 0L
        private var completed = false
        override fun read(sink: Buffer, byteCount: Long): Long {
            val count = super.read(sink, byteCount)
            if (count > 0) received += count
            if (!completed) listener.onProgress(received, contentLength(), count == -1L)
            if (count == -1L) completed = true
            return count
        }
    }.buffer()
    override fun contentType(): MediaType? = delegate.contentType()
    override fun contentLength(): Long = delegate.contentLength()
    override fun source(): BufferedSource = progressSource
}
