// File: app/src/main/java/com/example/saktinocompose/network/dto/SubmittedReviewDto.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Request untuk complete submitted details
 * PUT /change-requests/{cr_id}/review
 */
data class SubmittedReviewRequest(
    @SerializedName("description")
    val description: String,

    @SerializedName("aset_terdampak_id")
    val asetTerdampakId: String,

    @SerializedName("ci_id")
    val ciId: String,

    @SerializedName("usulan_jadwal")
    val usulanJadwal: String  // Format: "yyyy-MM-dd"
) {
    companion object {
        /**
         * Create request dari form input
         */
        fun create(
            description: String,
            asetTerdampakId: String,
            ciId: String,
            usulanJadwal: String
        ): SubmittedReviewRequest {
            return SubmittedReviewRequest(
                description = description,
                asetTerdampakId = asetTerdampakId,
                ciId = ciId,
                usulanJadwal = usulanJadwal
            )
        }
    }
}

/**
 * Response dari submitted review endpoint
 */
data class SubmittedReviewResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: SubmittedReviewData?
)

data class SubmittedReviewData(
    @SerializedName("cr_id")
    val crId: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("aset_terdampak_id")
    val asetTerdampakId: String?,

    @SerializedName("ci_id")
    val ciId: String?,

    @SerializedName("usulan_jadwal")
    val usulanJadwal: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * Helper untuk validasi
 */
object SubmittedReviewValidator {
    fun validate(request: SubmittedReviewRequest): ValidationResult {
        val errors = mutableListOf<String>()

        if (request.description.isBlank()) {
            errors.add("Description is required")
        }

        if (request.asetTerdampakId.isBlank()) {
            errors.add("Affected asset ID is required")
        }

        if (request.ciId.isBlank()) {
            errors.add("CI ID is required")
        }

        if (request.usulanJadwal.isBlank()) {
            errors.add("Proposed schedule is required")
        } else if (!isValidDate(request.usulanJadwal)) {
            errors.add("Invalid date format (should be yyyy-MM-dd)")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            val parts = date.split("-")
            parts.size == 3 &&
                    parts[0].length == 4 &&
                    parts[1].length == 2 &&
                    parts[2].length == 2
        } catch (e: Exception) {
            false
        }
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()

    fun isValid() = this is Valid
    fun getErrorMessages() = (this as? Invalid)?.errors ?: emptyList()
}