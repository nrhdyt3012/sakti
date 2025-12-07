//package com.example.saktinocompose.viewmodel
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.saktinocompose.data.entity.Notification
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.launch
//
//class NotificationViewModel(application: Application) : AndroidViewModel(application) {
//
//
//    fun createNotification(
//        userId: String,  // ✅ Changed from Int to String
//        changeRequestId: String,  // ✅ Already String
//        ticketId: String,
//        fromStatus: String,
//        toStatus: String
//    ) {
//        viewModelScope.launch {
//            val userIdInt = userId.toIntOrNull() ?: 0  // ✅ Convert to Int for DB
//
//            val title = "Status Update: $ticketId"
//            val message = "Status berubah dari '$fromStatus' menjadi '$toStatus'"
//
//            val notification = Notification(
//                userId = userIdInt,
//                changeRequestId = changeRequestId,
//                ticketId = ticketId,
//                title = title,
//                message = message,
//                fromStatus = fromStatus,
//                toStatus = toStatus
//            )
//        }
//    }
//
//    fun markAsRead(notificationId: Int) {
//        viewModelScope.launch {
//            notificationDao.markAsRead(notificationId)
//        }
//    }
//
//    fun markAllAsRead(userId: Int) {
//        viewModelScope.launch {
//            notificationDao.markAllAsRead(userId)
//        }
//    }
//
//    fun deleteNotification(notification: Notification) {
//        viewModelScope.launch {
//            notificationDao.deleteNotification(notification)
//        }
//    }
//}