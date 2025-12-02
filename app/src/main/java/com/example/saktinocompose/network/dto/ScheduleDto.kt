// File: app/src/main/java/com/example/saktinocompose/network/dto/ScheduleDto.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * Request untuk schedule implementation
 * POST /change-requests/{id}/schedule
 *
 * Contoh request body:
 * {
 *   "tanggal_implementasi": "2025-11-30T07:26:24.580Z"
 * }
 */
data class ScheduleRequest(
    @SerializedName("tanggal_implementasi")
    val tanggalImplementasi: String // ISO 8601 format
) {
    companion object {
        /**
         * Create ScheduleRequest dari date string format "yyyy-MM-dd"
         */
        fun fromDateString(dateString: String): ScheduleRequest {
            val isoDate = convertToIso8601(dateString)
            return ScheduleRequest(tanggalImplementasi = isoDate)
        }

        /**
         * Create ScheduleRequest dari Date object
         */
        fun fromDate(date: Date): ScheduleRequest {
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val isoDate = outputFormat.format(date)
            return ScheduleRequest(tanggalImplementasi = isoDate)
        }

        /**
         * Create ScheduleRequest dari timestamp (Long)
         */
        fun fromTimestamp(timestamp: Long): ScheduleRequest {
            val date = Date(timestamp)
            return fromDate(date)
        }

        /**
         * Convert date string "yyyy-MM-dd" ke ISO 8601 format
         */
        private fun convertToIso8601(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(dateString)

                val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                outputFormat.timeZone = TimeZone.getTimeZone("UTC")
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                // Fallback: return original string or current date
                val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                outputFormat.timeZone = TimeZone.getTimeZone("UTC")
                outputFormat.format(Date())
            }
        }
    }
}

/**
 * Response dari schedule implementation
 * Contoh response ketika berhasil:
 * {
 *   "status": "success",
 *   "message": "Jadwal implementasi berhasil dibuat",
 *   "data": null
 * }
 */
data class ScheduleResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ScheduleData?
)

/**
 * Data schedule (optional, jika API mengembalikan data)
 */
data class ScheduleData(
    @SerializedName("cr_id")
    val crId: String?,

    @SerializedName("tanggal_implementasi")
    val tanggalImplementasi: String?,

    @SerializedName("scheduled_at")
    val scheduledAt: String?
)

/**
 * Helper class untuk validasi dan formatting schedule
 */
object ScheduleHelper {
    /**
     * Validate apakah tanggal valid (tidak di masa lalu)
     */
    fun isValidScheduleDate(dateString: String): Boolean {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            val now = Date()
            date != null && !date.before(now)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Format tanggal untuk display
     */
    fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Parse ISO 8601 date ke readable format
     */
    fun parseIso8601ToReadable(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoDate)

            val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            isoDate
        }
    }
}