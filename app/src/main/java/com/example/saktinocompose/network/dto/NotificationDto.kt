// File: app/src/main/java/com/example/saktinocompose/network/dto/NotificationDto.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Response untuk GET /notifications
 */
data class NotificationListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<NotificationApiData>?,

    @SerializedName("meta")
    val meta: PaginationMeta?
)

data class NotificationApiData(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("channel")
    val channel: String,

    @SerializedName("sent_at")
    val sentAt: String?,

    @SerializedName("read_at")
    val readAt: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("cr_id")
    val crId: String?
)

data class PaginationMeta(
    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("total")
    val total: Int,

    @SerializedName("total_pages")
    val totalPages: Int
)

/**
 * Request untuk PUT /notifications/{id}/read
 */
data class MarkAsReadRequest(
    @SerializedName("cr_id")
    val crId: String?
)

/**
 * Response untuk mark as read
 */
data class MarkAsReadResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Any?
)

/**
 * Model untuk UI
 */
data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val channel: String,
    val sentAt: String?,
    val readAt: String?,
    val createdAt: String,
    val crId: String?,
    val isRead: Boolean = readAt != null
) {
    companion object {
        fun fromApiData(apiData: NotificationApiData): NotificationItem {
            return NotificationItem(
                id = apiData.id,
                title = apiData.title,
                body = apiData.body,
                channel = apiData.channel,
                sentAt = apiData.sentAt,
                readAt = apiData.readAt,
                createdAt = apiData.createdAt,
                crId = apiData.crId,
                isRead = apiData.readAt != null
            )
        }
    }
}