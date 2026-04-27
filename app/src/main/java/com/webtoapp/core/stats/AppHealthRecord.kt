package com.webtoapp.core.stats

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.webtoapp.data.model.WebApp

/**
 * 网站健康状态
 */
enum class HealthStatus {
    UNKNOWN,    // 未检测
    ONLINE,     // 在线（响应时间 < 2s）
    SLOW,       // 慢速（响应时间 2-5s）
    OFFLINE     // 离线/不可达
}

/**
 * 网站健康检测记录
 */
@Entity(
    tableName = "app_health_records",
    indices = [
        Index(value = ["appId"]),
        Index(value = ["checkedAt"])
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
data class AppHealthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appId: Long,
    val url: String,
    val status: HealthStatus = HealthStatus.UNKNOWN,
    val responseTimeMs: Long = 0,             // 响应时间（毫秒）
    val httpStatusCode: Int = 0,              // HTTP 状态码
    val errorMessage: String? = null,
    val checkedAt: Long = System.currentTimeMillis()
)

/**
 * 网站健康状态摘要（用于首页显示）
 */
data class AppHealthSummary(
    val appId: Long,
    val latestStatus: HealthStatus,
    val latestResponseTimeMs: Long,
    val lastCheckedAt: Long,
    val uptimePercent: Float                  // 近 24h 在线率
)
