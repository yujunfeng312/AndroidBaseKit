package dev.androidbasekit.sample.model.repository

import dev.androidbasekit.sample.model.api.DemoApi
import dev.androidbasekit.sample.model.entity.Photo
import dev.androidbasekit.sample.R
import dev.androidbasekit.sample.BuildConfig

import android.content.Context
import dev.androidbasekit.common.*
import dev.androidbasekit.network.*
import dev.androidbasekit.storage.*

/**
 * 示例数据仓库。组合 Room、MMKV、内存缓存和远程 API，提供本地初始化、网络刷新与用户清理；持有数据库并负责关闭。
 */
class DemoRepository(context: Context) : AutoCloseable {
    val database = DemoDatabase.open(context)
    private val app = context.applicationContext
    val userId = "demo-local-user"
    private val preferences = AppPreferences.open(userId)
    val api: DemoApi = NetworkClient(NetworkConfig("https://jsonplaceholder.typicode.com/", debugLogging = BuildConfig.DEBUG)).create(DemoApi::class.java)
    private val cache = MemoryCache<List<Photo>>(10)
    private val cached = CachedRepository(cache) { timber.log.Timber.w("Cache read/write failure") }
    suspend fun localItems(): List<DemoRecord> {
        val visits = preferences.getInt(PreferenceKeys.DEMO_VISITS) + 1
        preferences.putInt(PreferenceKeys.DEMO_VISITS, visits)
        val existing = database.items().read(userId)
        if (existing.isNotEmpty()) return existing
        val items = (1L..12L).map { DemoRecord(userId, it, app.getString(R.string.local_item, it), "", System.currentTimeMillis()) }
        database.items().replaceUser(userId, items)
        return database.items().read(userId)
    }
    suspend fun remoteItems(): ApiResult<List<DemoRecord>> {
        val result = cached.get(CacheKeys.DEMO_ITEMS, CachePolicy.NETWORK_FIRST, TimeConstants.MINUTE_MS) {
            ApiErrorMapper.execute { api.photos(12) }
        }
        return when (result) {
            is ApiResult.Success -> {
                val records = result.data.map { DemoRecord(userId, it.id, it.title, it.thumbnailUrl, System.currentTimeMillis()) }
                database.items().replaceUser(userId, records)
                ApiResult.success(records)
            }
            is ApiResult.Failure -> result
        }
    }
    suspend fun logout() { preferences.clearUserData(); cache.clear(); PreferencesCacheStore(userId).clear(); database.items().clearUser(userId) }
    override fun close() = database.close()
}
