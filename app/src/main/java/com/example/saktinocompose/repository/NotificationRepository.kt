// File: app/src/main/java/com/example/saktinocompose/repository/NotificationRepository.kt
package com.example.saktinocompose.repository

import android.util.Log
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.MarkAsReadRequest
import com.example.saktinocompose.network.dto.NotificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository {

    /**
     * Fetch notifications from API
     */
    suspend fun fetchNotifications(
        page: Int = 1,
        limit: Int = 20
    ): Result<List<NotificationItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken

                if (token == null) {
                    Log.e("NotificationRepo", "No token available")
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                Log.d("NotificationRepo", "Fetching notifications: page=$page, limit=$limit")

                val response = RetrofitClient.notificationService.getNotifications(
                    page = page,
                    limit = limit
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiNotifications = response.body()?.data ?: emptyList()
                    val notifications = apiNotifications.map {
                        NotificationItem.fromApiData(it)
                    }

                    Log.d("NotificationRepo", "✅ Fetched ${notifications.size} notifications")
                    Result.Success(notifications)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = response.body()?.message ?: errorBody ?: "Failed to fetch notifications"
                    Log.e("NotificationRepo", "API Error: $errorMessage")

                    if (response.code() == 401) {
                        RetrofitClient.clearAuthToken()
                        return@withContext Result.Error(
                            Exception("Token expired"),
                            "Session expired. Please login again."
                        )
                    }

                    Result.Error(Exception("Fetch failed"), errorMessage)
                }
            } catch (e: Exception) {
                Log.e("NotificationRepo", "Exception during fetch", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    /**
     * Mark notification as read
     */
    suspend fun markAsRead(
        notificationId: String,
        crId: String?
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken

                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                Log.d("NotificationRepo", "Marking as read: $notificationId")

                val request = MarkAsReadRequest(crId = crId)
                val response = RetrofitClient.notificationService.markAsRead(
                    notificationId = notificationId,
                    request = request
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("NotificationRepo", "✅ Marked as read successfully")
                    Result.Success(true)
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to mark as read"
                    Log.e("NotificationRepo", "❌ API Error: $errorMessage")
                    Result.Error(Exception(errorMessage), errorMessage)
                }
            } catch (e: Exception) {
                Log.e("NotificationRepo", "❌ Exception during mark as read", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken

                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val response = RetrofitClient.notificationService.markAllAsRead()

                if (response.isSuccessful && response.body()?.success == true) {
                    Result.Success(true)
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to mark all as read"
                    Result.Error(Exception(errorMessage), errorMessage)
                }
            } catch (e: Exception) {
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }
}