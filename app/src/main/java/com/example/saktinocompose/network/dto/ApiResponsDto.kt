// File: app/src/main/java/com/example/saktinocompose/network/dto/ApiResponse.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response wrapper
 * Digunakan untuk semua response dari API yang mengikuti format standar
 *
 * Format standar response:
 * {
 *   "status": "success" | "error",
 *   "message": "Message dari server",
 *   "data": { ... } | null,
 *   "details": { ... } | null
 * }
 *
 * @param T tipe data yang dikembalikan di field "data"
 */
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
    /**
     * Check apakah response sukses
     */
    fun isSuccess(): Boolean {
        return status.equals("success", ignoreCase = true)
    }

    /**
     * Check apakah response error
     */
    fun isError(): Boolean {
        return status.equals("error", ignoreCase = true)
    }

    /**
     * Get data atau throw exception jika error
     */
    fun getDataOrThrow(): T {
        return if (isSuccess()) {
            data ?: throw ApiException("Data is null despite success status")
        } else {
            throw ApiException(message)
        }
    }

    /**
     * Get data atau return default value
     */
    fun getDataOrDefault(default: T): T {
        return if (isSuccess()) {
            data ?: default
        } else {
            default
        }
    }

    companion object {
        /**
         * Create success response
         */
        fun <T> success(data: T, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(
                status = "success",
                message = message,
                data = data,
                details = null
            )
        }

        /**
         * Create error response
         */
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

/**
 * Exception untuk API errors
 */
class ApiException(
    message: String,
    val statusCode: Int? = null,
    val errorDetails: Any? = null
) : Exception(message)

/**
 * Extension functions untuk handling API Response
 */

/**
 * Convert ApiResponse to Result
 */
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

/**
 * Map data dari ApiResponse
 */
fun <T, R> ApiResponse<T>.mapData(transform: (T) -> R): ApiResponse<R> {
    return ApiResponse(
        status = status,
        message = message,
        data = data?.let(transform),
        details = details
    )
}

/**
 * Execute block jika response success
 */
inline fun <T> ApiResponse<T>.onSuccess(block: (T) -> Unit): ApiResponse<T> {
    if (isSuccess() && data != null) {
        block(data)
    }
    return this
}

/**
 * Execute block jika response error
 */
inline fun <T> ApiResponse<T>.onError(block: (String) -> Unit): ApiResponse<T> {
    if (isError()) {
        block(message)
    }
    return this
}

/**
 * Response wrapper untuk list data
 */
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

/**
 * Pagination data (jika API support pagination)
 */
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

/**
 * Response wrapper untuk operation tanpa data
 */
typealias ApiEmptyResponse = ApiResponse<Unit>

/**
 * Helper untuk create empty success response
 */
fun createEmptySuccessResponse(message: String = "Operation successful"): ApiEmptyResponse {
    return ApiResponse.success(Unit, message)
}

/**
 * Response wrapper untuk error dengan error code
 */
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