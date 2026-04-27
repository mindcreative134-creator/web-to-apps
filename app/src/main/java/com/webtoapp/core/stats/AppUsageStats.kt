package com.webtoapp.core.stats

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.webtoapp.data.model.WebApp

/**
 * 应用使用统计实体
 * 独立表存储，避免增加 WebApp 表的复杂度
 */
@Entity(
    tableName = "app_usage_stats",
    indices = [
        Index(value = ["appId"], unique = true),
        Index(value = ["lastUsedAt"]),
        Index(value = ["launchCount"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = WebApp::class,
            parentColumns = ["id"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AppUsageStats(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appId: Long,                          // 关联的 WebApp ID
    val launchCount: Int = 0,                 // 启动次数
    val totalUsageMs: Long = 0,               // 总使用时长（毫秒）
    val lastUsedAt: Long = 0,                 // 最后使用时间
    val lastSessionDurationMs: Long = 0,      // 上次会话时长（毫秒）
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 格式化的总使用时长 */
    val formattedTotalUsage: String
        get() {
            val totalSeconds = totalUsageMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "<1m"
            }
        }
    
    /** 格式化的最后使用时间（相对时间） */
    val formattedLastUsed: String
        get() {
            if (lastUsedAt == 0L) return "从未使用"
            val diff = System.currentTimeMillis() - lastUsedAt
            val minutes = diff / 60_000
            val hours = minutes / 60
            val days = hours / 24
            return when {
                minutes < 1 -> "刚刚"
                minutes < 60 -> "${minutes}分钟前"
                hours < 24 -> "${hours}小时前"
                days < 30 -> "${days}天前"
                else -> "${days / 30}个月前"
            }
        }
}
