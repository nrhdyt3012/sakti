package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Request untuk complete submitted details
 * PUT /change-requests/{cr_id}/submitted
 */
data class SubmittedDetailRequest(
    @SerializedName("description")
    val description: String,

    @SerializedName("aset_terdampak")
    val asetTerdampak: String,  // Format: "id:nama:type" atau "id:nama"

    @SerializedName("ci_relation")
    val ciRelation: String,  // Format: "id1:nama1:type1,id2:nama2:type2"

    @SerializedName("proposed_schedule")
    val proposedSchedule: String  // Format: "yyyy-MM-dd"
) {
    companion object {
        /**
         * Create request dari form input
         */
        fun create(
            description: String,
            asetTerdampakId: String,
            asetTerdampakNama: String,
            ciRelationList: List<Triple<String, String, String>>,  // (id, nama, type)
            proposedSchedule: String
        ): SubmittedDetailRequest {
            val asetTerdampak = "$asetTerdampakId:$asetTerdampakNama"
            val ciRelation = ciRelationList.joinToString(",") { (id, nama, type) ->
                "$id:$nama:$type"
            }

            return SubmittedDetailRequest(
                description = description,
                asetTerdampak = asetTerdampak,
                ciRelation = ciRelation,
                proposedSchedule = proposedSchedule
            )
        }
    }
}

/**
 * Response dari submitted endpoint
 */
data class SubmittedDetailResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: SubmittedData?
)

data class SubmittedData(
    @SerializedName("cr_id")
    val crId: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("aset_terdampak")
    val asetTerdampak: String?,

    @SerializedName("ci_relation")
    val ciRelation: String?,

    @SerializedName("proposed_schedule")
    val proposedSchedule: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * Helper untuk validasi
 */
object SubmittedDetailValidator {
    fun validate(request: SubmittedDetailRequest): ValidationResult {
        val errors = mutableListOf<String>()

        if (request.description.isBlank()) {
            errors.add("Description is required")
        }

        if (request.asetTerdampak.isBlank()) {
            errors.add("Affected asset is required")
        } else if (!request.asetTerdampak.contains(":")) {
            errors.add("Affected asset format invalid (should be 'id:name')")
        }

        if (request.ciRelation.isBlank()) {
            errors.add("CI relation is required")
        }

        if (request.proposedSchedule.isBlank()) {
            errors.add("Proposed schedule is required")
        } else if (!isValidDate(request.proposedSchedule)) {
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