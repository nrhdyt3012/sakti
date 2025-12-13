// File: app/src/main/java/com/example/saktinocompose/network/dto/EmergencyDto.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Request untuk create emergency
 * POST /emergency
 */
data class EmergencyRequest(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("impacted_assets")
    val impactedAssets: List<String>,

    @SerializedName("status")
    val status: String, // COMPLETED or FAILED

    @SerializedName("note")
    val note: String
) {
    companion object {
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_FAILED = "FAILED"
    }
}

/**
 * Response dari emergency endpoint
 */
data class EmergencyResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: EmergencyData?
)

data class EmergencyData(
    @SerializedName("id")
    val id: String,

    @SerializedName("emergency_code")
    val emergencyCode: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("impacted_asset_id")
    val impactedAssetId: String?,

    @SerializedName("reporter_id")
    val reporterId: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("photo_url")
    val photoUrl: String?
)

/**
 * Response from emergency photo upload
 * POST /emergency/photo
 */
data class EmergencyPhotoResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: EmergencyPhotoUploadData?
)

data class EmergencyPhotoUploadData(
    @SerializedName("path")
    val path: String,

    @SerializedName("url")
    val url: String
)