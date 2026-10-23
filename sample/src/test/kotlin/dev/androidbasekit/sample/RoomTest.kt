package dev.androidbasekit.sample

import android.app.Application
import androidx.room.Room
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import dev.androidbasekit.storage.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * RoomTest 回归测试集。通过测试方法验证对应模块行为、错误与边界条件；测试结果见验收说明。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class RoomTest {
    @Test fun userIsolationAndDeletion() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(context, DemoDatabase::class.java).build()
        try {
            db.items().insert(listOf(DemoRecord("a", 1, "a", "", 10), DemoRecord("b", 1, "b", "", 20)))
            db.items().clearUser("a"); assertTrue(db.items().read("a").isEmpty()); assertEquals("b", db.items().read("b").single().title)
            assertEquals(1, db.items().deleteOlderThan(30)); assertTrue(db.items().read("b").isEmpty())
        } finally { db.close() }
    }
    @Test fun migrationPreservesDataAndRoomValidatesSchema() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val name = "migration-test.db"; context.deleteDatabase(name)
        val helper = FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name).callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE demo_items (userId TEXT NOT NULL, id INTEGER NOT NULL, title TEXT NOT NULL, imageUrl TEXT NOT NULL, PRIMARY KEY(userId,id))")
                    db.execSQL("INSERT INTO demo_items VALUES ('a',1,'kept','')")
                }
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) { error("Unexpected") }
            }).build())
        helper.writableDatabase; helper.close()
        val db = Room.databaseBuilder(context, DemoDatabase::class.java, name).addMigrations(DemoDatabase.MIGRATION_1_2).build()
        try { val row = db.items().read("a").single(); assertEquals("kept", row.title); assertEquals(0L, row.updatedAt) }
        finally { db.close(); context.deleteDatabase(name) }
    }
}
