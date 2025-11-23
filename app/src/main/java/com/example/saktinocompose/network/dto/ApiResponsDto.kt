// 5. Generic API Response
// File: app/src/main/java/com/example/saktinocompose/network/dto/ApiResponse.kt

package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: T?,
    @SerializedName("error")
    val error: ErrorData?
)

data class ErrorData(
    @SerializedName("code")
    val code: String?,
    @SerializedName("details")
    val details: String?
)