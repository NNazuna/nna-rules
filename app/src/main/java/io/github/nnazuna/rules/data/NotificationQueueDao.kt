package io.github.nnazuna.rules.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationQueueDao {
    @Insert
    suspend fun insert(notification: QueuedNotification): Long

    @Query("SELECT * FROM notification_queue ORDER BY queued_at DESC")
    suspend fun getAll(): List<QueuedNotification>

    @Query("DELETE FROM notification_queue WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM notification_queue")
    suspend fun deleteAll()
}
