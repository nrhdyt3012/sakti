// File: app/src/main/java/com/example/saktinocompose/viewmodel/NotificationViewModel.kt

package com.example.saktinocompose.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.dto.NotificationItem
import com.example.saktinocompose.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotificationRepository()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    init {
        loadNotifications()
    }

    /**
     * Load notifications from API
     */
    fun loadNotifications(page: Int = 1, limit: Int = 50) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.fetchNotifications(page, limit)) {
                is Result.Success -> {
                    _notifications.value = result.data
                    _unreadCount.value = result.data.count { !it.isRead }
                    _error.value = null
                    Log.d("NotificationVM", "✅ Loaded ${result.data.size} notifications")
                }
                is Result.Error -> {
                    val errorMsg = result.message ?: "Failed to load notifications"
                    _error.value = errorMsg
                    Log.e("NotificationVM", "❌ Error: $errorMsg")
                }
                else -> {
                    _error.value = "Unknown error"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Refresh notifications
     */
    fun refreshNotifications() {
        loadNotifications()
    }

    /**
     * Mark single notification as read
     */
    fun markAsRead(notificationId: String, crId: String?) {
        viewModelScope.launch {
            when (val result = repository.markAsRead(notificationId, crId)) {
                is Result.Success -> {
                    // Update local state
                    val updated = _notifications.value.map {
                        if (it.id == notificationId) {
                            it.copy(
                                readAt = System.currentTimeMillis().toString(),
                                isRead = true
                            )
                        } else {
                            it
                        }
                    }
                    _notifications.value = updated
                    _unreadCount.value = updated.count { !it.isRead }

                    Log.d("NotificationVM", "✅ Marked notification as read")
                }
                is Result.Error -> {
                    Log.e("NotificationVM", "❌ Failed to mark as read: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            when (val result = repository.markAllAsRead()) {
                is Result.Success -> {
                    // Update all local notifications
                    val updated = _notifications.value.map {
                        it.copy(
                            readAt = if (it.readAt == null) System.currentTimeMillis().toString() else it.readAt,
                            isRead = true
                        )
                    }
                    _notifications.value = updated
                    _unreadCount.value = 0

                    Log.d("NotificationVM", "✅ Marked all as read")
                }
                is Result.Error -> {
                    _error.value = result.message
                    Log.e("NotificationVM", "❌ Failed to mark all as read")
                }
                else -> {}
            }
        }
    }

    /**
     * Get unread notifications only
     */
    fun getUnreadNotifications(): List<NotificationItem> {
        return _notifications.value.filter { !it.isRead }
    }

    /**
     * Get read notifications only
     */
    fun getReadNotifications(): List<NotificationItem> {
        return _notifications.value.filter { it.isRead }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}