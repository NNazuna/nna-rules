package io.github.nnazuna.rules.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_queue")
data class QueuedNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "rule_id") val ruleId: Long,
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "app_label") val appLabel: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "queued_at") val queuedAt: Long = System.currentTimeMillis(),
)
