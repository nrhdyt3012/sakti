// File: app/src/main/java/com/example/saktinocompose/network/api/NotificationApiService.kt
package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface NotificationApiService {

    /**
     * Get notifications list with pagination
     * GET /notifications?page=1&limit=10
     */
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<NotificationListResponse>

    /**
     * Mark notification as read
     * PUT /notifications/{id}/read
     */
    @PUT("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: String,
        @Body request: MarkAsReadRequest
    ): Response<MarkAsReadResponse>

    /**
     * Mark all notifications as read (if endpoint exists)
     */
    @PUT("notifications/read-all")
    suspend fun markAllAsRead(): Response<MarkAsReadResponse>
}