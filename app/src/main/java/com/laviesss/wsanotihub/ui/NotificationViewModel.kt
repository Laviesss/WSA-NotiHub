package com.laviesss.wsanotihub.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.laviesss.wsanotihub.data.NotiHubDatabase
import com.laviesss.wsanotihub.data.NotificationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = NotiHubDatabase.getDatabase(application).notificationDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications

    private val _sortOrder = MutableStateFlow("Newest")

    init {
        viewModelScope.launch {
            combine(dao.getAllNotifications(), _searchQuery, _sortOrder) { list, query, sort ->
                val filtered = if (query.isEmpty()) list
                else list.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.body.contains(query, ignoreCase = true) ||
                    it.appLabel.contains(query, ignoreCase = true)
                }

                when (sort) {
                    "By App" -> filtered.sortedBy { it.appLabel }
                    "By Priority" -> filtered.sortedByDescending { it.priority }
                    else -> filtered.sortedByDescending { it.timestamp }
                }
            }.collectLatest {
                _notifications.value = it
            }
        }
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun dismissNotification(id: Int) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }
}
