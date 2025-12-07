// File: app/src/main/java/com/example/saktinocompose/network/dto/ApiResponse.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("status")
    val status: String, // "success" atau "error"

    @SerializedName("message")
    val message: String, // Pesan dari server

    @SerializedName("data")
    val data: T?, // Data yang dikembalikan (nullable)

    @SerializedName("details")
    val details: Any? = null // Additional details (optional)
) {

    fun isSuccess(): Boolean {
        return status.equals("success", ignoreCase = true)
    }

    fun isError(): Boolean {
        return status.equals("error", ignoreCase = true)
    }

    fun getDataOrThrow(): T {
        return if (isSuccess()) {
            data ?: throw ApiException("Data is null despite success status")
        } else {
            throw ApiException(message)
        }
    }

    fun getDataOrDefault(default: T): T {
        return if (isSuccess()) {
            data ?: default
        } else {
            default
        }
    }

    companion object {

        fun <T> success(data: T, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(
                status = "success",
                message = message,
                data = data,
                details = null
            )
        }

        fun <T> error(message: String, details: Any? = null): ApiResponse<T> {
            return ApiResponse(
                status = "error",
                message = message,
                data = null,
                details = details
            )
        }
    }
}

class ApiException(
    message: String,
    val statusCode: Int? = null,
    val errorDetails: Any? = null
) : Exception(message)


fun <T> ApiResponse<T>.toResult(): com.example.saktinocompose.network.Result<T> {
    return if (isSuccess() && data != null) {
        com.example.saktinocompose.network.Result.Success(data)
    } else {
        com.example.saktinocompose.network.Result.Error(
            ApiException(message),
            message
        )
    }
}

fun <T, R> ApiResponse<T>.mapData(transform: (T) -> R): ApiResponse<R> {
    return ApiResponse(
        status = status,
        message = message,
        data = data?.let(transform),
        details = details
    )
}


inline fun <T> ApiResponse<T>.onSuccess(block: (T) -> Unit): ApiResponse<T> {
    if (isSuccess() && data != null) {
        block(data)
    }
    return this
}

inline fun <T> ApiResponse<T>.onError(block: (String) -> Unit): ApiResponse<T> {
    if (isError()) {
        block(message)
    }
    return this
}

data class ApiListResponse<T>(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<T>?,

    @SerializedName("pagination")
    val pagination: PaginationData? = null,

    @SerializedName("details")
    val details: Any? = null
) {
    fun isSuccess(): Boolean {
        return status.equals("success", ignoreCase = true)
    }

    fun getDataOrEmpty(): List<T> {
        return if (isSuccess()) {
            data ?: emptyList()
        } else {
            emptyList()
        }
    }
}

data class PaginationData(
    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("per_page")
    val perPage: Int,

    @SerializedName("total")
    val total: Int,

    @SerializedName("total_pages")
    val totalPages: Int,

    @SerializedName("has_more")
    val hasMore: Boolean
)

typealias ApiEmptyResponse = ApiResponse<Unit>

fun createEmptySuccessResponse(message: String = "Operation successful"): ApiEmptyResponse {
    return ApiResponse.success(Unit, message)
}

data class ApiErrorResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("error_code")
    val errorCode: String? = null,

    @SerializedName("details")
    val details: Map<String, Any>? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
) {
    fun toException(): ApiException {
        return ApiException(
            message = message,
            statusCode = errorCode?.toIntOrNull(),
            errorDetails = details
        )
    }
}