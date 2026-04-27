package com.webtoapp.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.webtoapp.core.logging.AppLogger

/**
 * Android 壳应用数据库迁移定义
 * 
 * 版本记录：
 * - 28: 引入 AppUsageStats, AppHealthRecords
 * - 29: 添加 cloudConfig
 * - 30: 添加 multiWebConfig
 * - 31: 添加 browserDisguiseConfig
 * - 32: 添加 deviceDisguiseConfig
 * - 33: 添加 extensionEnabled
 */
object AppDatabaseMigrations {

    private fun createAddColumnMigration(
        startVersion: Int,
        endVersion: Int,
        columnName: String,
        columnType: String = "TEXT",
        defaultValue: String = "NULL",
    ): Migration {
        return object : Migration(startVersion, endVersion) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE web_apps ADD COLUMN $columnName $columnType DEFAULT $defaultValue")
                } catch (e: Exception) {
                    AppLogger.w("AppDatabase", "迁移 $startVersion->$endVersion 跳过列 $columnName: ${e.message}")
                }
            }
        }
    }

    val MIGRATION_28_29 = object : Migration(28, 29) {
        override fun migrate(db: SupportSQLiteDatabase) {
            AppLogger.i("AppDatabase", "迁移 28->29: 添加使用统计和健康检测表")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_usage_stats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    appId INTEGER NOT NULL,
                    launchCount INTEGER NOT NULL DEFAULT 0,
                    totalUsageMs INTEGER NOT NULL DEFAULT 0,
                    lastUsedAt INTEGER NOT NULL DEFAULT 0,
                    lastSessionDurationMs INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (appId) REFERENCES web_apps(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_app_usage_stats_appId ON app_usage_stats(appId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_app_usage_stats_lastUsedAt ON app_usage_stats(lastUsedAt)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_app_usage_stats_launchCount ON app_usage_stats(launchCount)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_health_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    appId INTEGER NOT NULL,
                    url TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'UNKNOWN',
                    responseTimeMs INTEGER NOT NULL DEFAULT 0,
                    httpStatusCode INTEGER NOT NULL DEFAULT 0,
                    errorMessage TEXT,
                    checkedAt INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (appId) REFERENCES web_apps(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_app_health_records_appId ON app_health_records(appId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_app_health_records_checkedAt ON app_health_records(checkedAt)")

            AppLogger.i("AppDatabase", "迁移 28->29 完成")
        }
    }

    val MIGRATION_29_30 = createAddColumnMigration(29, 30, "cloudConfig")
    val MIGRATION_30_31 = createAddColumnMigration(30, 31, "multiWebConfig")
    val MIGRATION_31_32 = createAddColumnMigration(31, 32, "browserDisguiseConfig")
    val MIGRATION_32_33 = createAddColumnMigration(32, 33, "deviceDisguiseConfig")
    
    val MIGRATION_33_34 = object : Migration(33, 34) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE web_apps ADD COLUMN extensionEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE web_apps SET extensionEnabled = 1 WHERE extensionModuleIds != '[]' AND extensionModuleIds != ''")
            } catch (e: Exception) {
                AppLogger.w("AppDatabase", "迁移 33->34 跳过列 extensionEnabled: ${e.message}")
            }
        }
    }
}
