package dev.androidbasekit.storage

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room 示例数据实体。userId 与 id 组成主键，保存标题、图片地址和更新时间；正式业务可替换表结构。
 */
@Entity(tableName = "demo_items", primaryKeys = ["userId", "id"])
data class DemoRecord @JvmOverloads constructor(
    val userId: String, val id: Long, val title: String,
    val imageUrl: String, @ColumnInfo(defaultValue = "0") val updatedAt: Long = 0
)
/**
 * Room 示例 DAO。按用户观察/查询/删除数据，支持事务替换和过期清理；挂起查询不阻塞主线程。
 */
@Dao
abstract class DemoDao {
    @Query("SELECT * FROM demo_items WHERE userId = :userId ORDER BY id") abstract fun observe(userId: String): LiveData<List<DemoRecord>>
    @Query("SELECT * FROM demo_items WHERE userId = :userId ORDER BY id") abstract suspend fun read(userId: String): List<DemoRecord>
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insert(records: List<DemoRecord>)
    @Query("DELETE FROM demo_items WHERE userId = :userId") abstract suspend fun clearUser(userId: String)
    @Query("DELETE FROM demo_items WHERE updatedAt < :cutoff") abstract suspend fun deleteOlderThan(cutoff: Long): Int
    @Transaction open suspend fun replaceUser(userId: String, records: List<DemoRecord>) {
        require(records.all { it.userId == userId }); clearUser(userId); insert(records)
    }
}
/**
 * Room 示例数据库。提供 DAO、导出 schema 和显式 1→2 迁移；持有方在任务结束后关闭数据库。
 */
@Database(entities = [DemoRecord::class], version = 2, exportSchema = true)
abstract class DemoDatabase : RoomDatabase() {
    abstract fun items(): DemoDao
    companion object {
        @JvmField val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE demo_items ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0") }
        }
        @JvmStatic fun open(context: Context): DemoDatabase = Room.databaseBuilder(context.applicationContext,
            DemoDatabase::class.java, "basekit-demo.db").addMigrations(MIGRATION_1_2).build()
    }
}
