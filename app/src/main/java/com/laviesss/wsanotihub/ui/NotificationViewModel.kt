package com.laviesss.wsanotihub.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.laviesss.wsanotihub.data.NotiHubDatabase
import com.laviesss.wsanotihub.data.NotificationEntity
import com.laviesss.wsanotihub.model.NotificationGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = NotiHubDatabase.getDatabase(application).notificationDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOrder = MutableStateFlow("Newest")

    private val _expansionStates = mutableMapOf<String, Boolean>()
    private val _groupedNotifications = MutableStateFlow<List<NotificationGroup>>(emptyList())
    val groupedNotifications: StateFlow<List<NotificationGroup>> = _groupedNotifications

    init {
        viewModelScope.launch {
            combine(dao.getAllNotifications(), _searchQuery, _sortOrder) { list, query, sort ->
                val filtered = if (query.isEmpty()) list
                else list.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.body.contains(query, ignoreCase = true) ||
                    it.appLabel.contains(query, ignoreCase = true)
                }

                val groups = filtered.groupBy { it.appPackageName }.map { (pkg, notis) ->
                    NotificationGroup(
                        appPackageName = pkg,
                        appLabel = notis.first().appLabel,
                        notifications = notis.sortedByDescending { it.timestamp },
                        isExpanded = if (query.isNotEmpty()) true else _expansionStates[pkg] ?: false
                    )
                }

                when (sort) {
                    "By App" -> groups.sortedBy { it.appLabel }
                    "By Priority" -> groups.sortedByDescending { it.mostRecent.priority }
                    else -> groups.sortedByDescending { it.mostRecent.timestamp }
                }
            }.collectLatest {
                _groupedNotifications.value = it
            }
        }
    }

    fun toggleGroup(appPackageName: String) {
        val current = _expansionStates[appPackageName] ?: false
        _expansionStates[appPackageName] = !current
        // Trigger a refresh of the flow
        _searchQuery.value = _searchQuery.value
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
