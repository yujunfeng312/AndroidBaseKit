package dev.androidbasekit.sample.worker

import android.content.Context
import androidx.work.*
import dev.androidbasekit.storage.DemoDatabase
import kotlinx.coroutines.CancellationException

/**
 * WorkManager 后台清理示例。周期删除旧记录，失败有限重试；通过 schedule 显式启用，不自动启动。
 */
class CacheCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = DemoDatabase.open(applicationContext)
        return try {
            database.items().deleteOlderThan(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)
            Result.success()
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) { if (runAttemptCount < 3) Result.retry() else Result.failure()
        } finally { database.close() }
    }
    companion object {
        @JvmStatic fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<CacheCleanupWorker>(24, java.util.concurrent.TimeUnit.HOURS).build()
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork("basekit-cache-cleanup", ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
