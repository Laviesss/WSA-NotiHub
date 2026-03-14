package com.laviesss.wsanotihub.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE appPackageName = :packageName ORDER BY timestamp DESC")
    fun getByApp(packageName: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications SET isDismissed = 1 WHERE appPackageName = :packageName AND timestamp = :timestamp")
    suspend fun markAsDismissed(packageName: String, timestamp: Long)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()

    @Query("SELECT * FROM notifications WHERE isDismissed = 0 ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentActive(): List<NotificationEntity>
}
