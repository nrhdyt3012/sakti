// File: app/src/main/java/com/example/saktinocompose/network/dto/InspectionPhotoDto.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Response dari POST /change-requests/{cr_id}/inspection/photo
 */
data class InspectionPhotoResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: InspectionPhotoData?
)

data class InspectionPhotoData(
    @SerializedName("url")
    val url: String
)