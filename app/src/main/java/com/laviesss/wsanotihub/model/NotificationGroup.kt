package com.laviesss.wsanotihub.model

import com.laviesss.wsanotihub.data.NotificationEntity

data class NotificationGroup(
    val appPackageName: String,
    val appLabel: String,
    val notifications: List<NotificationEntity>,
    var isExpanded: Boolean = false
) {
    val unreadCount: Int get() = notifications.count { !it.isRead }
    val mostRecent: NotificationEntity get() = notifications.first()
}
