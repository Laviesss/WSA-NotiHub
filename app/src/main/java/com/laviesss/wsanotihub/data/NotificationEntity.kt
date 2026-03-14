package com.laviesss.wsanotihub.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appPackageName: String,
    val appLabel: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val isDismissed: Boolean = false,
    val priority: Int = 0,
    val hasReplyAction: Boolean = false,
    val serializedActions: String = "" // JSON string of available actions
)
