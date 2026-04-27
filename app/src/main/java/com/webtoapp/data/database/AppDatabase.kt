package com.webtoapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.stats.AppHealthRecord
import com.webtoapp.core.stats.AppUsageStats
import com.webtoapp.core.stats.AppUsageStatsDao
import com.webtoapp.data.converter.Converters
import com.webtoapp.data.dao.AppCategoryDao
import com.webtoapp.data.dao.WebAppDao
import com.webtoapp.data.model.AppCategory
import com.webtoapp.data.model.WebApp

private const val DATABASE_NAME = "webtoapp.db"
private val UNSUPPORTED_LEGACY_VERSIONS = IntArray(27) { index -> index + 1 }

/**
 * Room database.
 *
 * Compatibility policy now focuses on API level 28+:
 * - `28 -> 34` keeps incremental migrations
 * - `<28` falls back to destructive rebuild
 */
@Database(
    entities = [WebApp::class, AppCategory::class, AppUsageStats::class, AppHealthRecord::class],
    version = 34,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun webAppDao(): WebAppDao
    abstract fun appCategoryDao(): AppCategoryDao
    abstract fun appUsageStatsDao(): AppUsageStatsDao
}

fun createAppDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        DATABASE_NAME
    )
        .addMigrations(
            AppDatabaseMigrations.MIGRATION_28_29,
            AppDatabaseMigrations.MIGRATION_29_30,
            AppDatabaseMigrations.MIGRATION_30_31,
            AppDatabaseMigrations.MIGRATION_31_32,
            AppDatabaseMigrations.MIGRATION_32_33,
            AppDatabaseMigrations.MIGRATION_33_34,
        )
        .fallbackToDestructiveMigrationFrom(*UNSUPPORTED_LEGACY_VERSIONS)
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()
}




