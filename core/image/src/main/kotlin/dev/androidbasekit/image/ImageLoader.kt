package dev.androidbasekit.image

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 图片加载结果通知。只提供最终成功/失败，复杂 Target 和数据源处理可直接使用 Glide。
 */
interface ImageCallback { fun onSuccess(); fun onFailure() }
/**
 * 图片缩放策略。CENTER_CROP 裁剪填充，FIT_CENTER 完整居中展示。
 */
enum class ImageScale { CENTER_CROP, FIT_CENTER }
/**
 * 图片请求选项。配置占位/错误图、缩放、内存/磁盘缓存、缩略图、GIF、圆形及圆角；圆角单位 dp。
 */
data class ImageOptions @JvmOverloads constructor(
    val placeholder: Int = 0, val error: Int = 0,
    val scale: ImageScale = ImageScale.CENTER_CROP,
    val diskCache: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC,
    val memoryCache: Boolean = true, val thumbnail: Float = 0f,
    val gif: Boolean = false, val circle: Boolean = false, val radiusDp: Int = 0
) { init { require(thumbnail in 0f..1f && radiusDp >= 0) } }
/**
 * Glide 常用入口。提供普通图、头像、圆角、GIF、预加载及缓存清理；加载绑定 View，磁盘清理在后台执行。
 */
object ImageLoader {
    @JvmStatic @JvmOverloads fun load(view: ImageView, model: Any?, options: ImageOptions = ImageOptions(), callback: ImageCallback? = null) {
        var requestOptions = RequestOptions().diskCacheStrategy(options.diskCache).skipMemoryCache(!options.memoryCache).dontAnimate()
        if (options.placeholder != 0) requestOptions = requestOptions.placeholder(options.placeholder)
        if (options.error != 0) requestOptions = requestOptions.error(options.error)
        requestOptions = if (options.scale == ImageScale.CENTER_CROP) requestOptions.centerCrop() else requestOptions.fitCenter()
        if (options.circle) requestOptions = requestOptions.circleCrop()
        else if (options.radiusDp > 0) {
            val radius = (options.radiusDp * view.resources.displayMetrics.density).toInt().coerceAtLeast(1)
            val transform = com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius)
            requestOptions = if (options.scale == ImageScale.CENTER_CROP) requestOptions.transform(com.bumptech.glide.load.resource.bitmap.CenterCrop(), transform)
                else requestOptions.transform(com.bumptech.glide.load.resource.bitmap.FitCenter(), transform)
        }
        val manager = Glide.with(view)
        if (options.gif) {
            var request = manager.asGif().load(model).apply(requestOptions)
            if (options.thumbnail > 0) request = request.thumbnail(manager.asGif().load(model).apply(requestOptions).sizeMultiplier(options.thumbnail))
            if (callback != null) request = request.listener(listener(callback))
            request.into(view)
        } else {
            var request = manager.load(model).apply(requestOptions)
            if (options.thumbnail > 0) request = request.thumbnail(manager.load(model).apply(requestOptions).sizeMultiplier(options.thumbnail))
            if (callback != null) request = request.listener(listener(callback))
            // Glide reuses equivalent requests and owns ImageView targets/recycling; no URL tag cache.
            request.into(view)
        }
    }
    private fun <T> listener(callback: ImageCallback): RequestListener<T> = object : RequestListener<T> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<T>, isFirstResource: Boolean): Boolean { callback.onFailure(); return false }
        override fun onResourceReady(resource: T & Any, model: Any, target: Target<T>?, dataSource: DataSource, isFirstResource: Boolean): Boolean { callback.onSuccess(); return false }
    }
    @JvmStatic fun loadAvatar(view: ImageView, model: Any?) = load(view, model, ImageOptions(circle = true))
    @JvmStatic fun loadRounded(view: ImageView, model: Any?, radiusDp: Int) = load(view, model, ImageOptions(radiusDp = radiusDp))
    // Clearing an existing target must also work during Activity/Fragment teardown.
    // Glide delegates removal to the owning manager; no new view request is started here.
    @JvmStatic fun clear(view: ImageView) = Glide.with(view.context.applicationContext).clear(view)
    @JvmStatic fun preload(context: Context, model: Any?) { Glide.with(context.applicationContext).load(model).preload() }
    @JvmStatic fun clearMemory(context: Context) = Glide.get(context.applicationContext).clearMemory()
    @JvmStatic fun clearDiskAsync(context: Context, executor: java.util.concurrent.Executor): java.util.concurrent.CompletableFuture<Void> {
        val app = context.applicationContext
        return java.util.concurrent.CompletableFuture.runAsync({ Glide.get(app).clearDiskCache() }, executor)
    }
    suspend fun clearDisk(context: Context) = withContext(Dispatchers.IO) { Glide.get(context.applicationContext).clearDiskCache() }
}
