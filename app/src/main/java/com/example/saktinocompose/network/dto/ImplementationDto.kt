// File: app/src/main/java/com/example/saktinocompose/network/dto/ImplementationDto.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Request untuk complete implementation
 * POST /change-requests/{id}/implementation
 *
 * Contoh request body:
 * {
 *   "dampak_setelah_mitigasi": 2,
 *   "kemungkinan_setelah_mitigasi": 2,
 *   "exposure_setelah_mitigasi": 1,
 *   "keterangan": "Implementasi berhasil dilakukan...",
 *   "status": "COMPLETED"
 * }
 */
data class ImplementationRequest(
    @SerializedName("dampak_setelah_mitigasi")
    val dampakSetelahMitigasi: Int, // 1-5: Impact score after mitigation

    @SerializedName("kemungkinan_setelah_mitigasi")
    val kemungkinanSetelahMitigasi: Int, // 1-5: Likelihood score after mitigation

    @SerializedName("exposure_setelah_mitigasi")
    val exposureSetelahMitigasi: Int, // 1-4: Exposure level after mitigation

    @SerializedName("keterangan")
    val keterangan: String, // Description of implementation result

    @SerializedName("status")
    val status: String // COMPLETED or FAILED
) {
    companion object {
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_FAILED = "FAILED"

        /**
         * Calculate residual risk score
         * Formula: dampak * kemungkinan * exposure
         */
        fun calculateResidualScore(
            dampak: Int,
            kemungkinan: Int,
            exposure: Int
        ): Int {
            return dampak * kemungkinan * exposure
        }

        /**
         * Determine risk level based on scores
         */
        fun determineRiskLevel(dampak: Int, kemungkinan: Int): String {
            val riskScore = dampak * kemungkinan
            return when {
                riskScore <= 3 -> "Very Low"
                riskScore <= 6 -> "Low"
                riskScore <= 12 -> "Medium"
                riskScore <= 18 -> "High"
                riskScore <= 23 -> "Very High"
                else -> "Extreme"
            }
        }
    }
}

/**
 * Response dari complete implementation
 * Contoh response ketika berhasil:
 * {
 *   "status": "success",
 *   "message": "Implementasi berhasil diselesaikan",
 *   "data": { ... }
 * }
 */
data class ImplementationResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ImplementationData?
)

/**
 * Data hasil implementation (jika API mengembalikan data)
 */
data class ImplementationData(
    @SerializedName("cr_id")
    val crId: String?,

    @SerializedName("dampak_setelah_mitigasi")
    val dampakSetelahMitigasi: Int?,

    @SerializedName("kemungkinan_setelah_mitigasi")
    val kemungkinanSetelahMitigasi: Int?,

    @SerializedName("exposure_setelah_mitigasi")
    val exposureSetelahMitigasi: Int?,

    @SerializedName("residual_score")
    val residualScore: Int?,

    @SerializedName("risk_level")
    val riskLevel: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("completed_at")
    val completedAt: String?
)

/**
 * Helper untuk build ImplementationRequest
 */
data class ImplementationBuilder(
    var dampakSetelahMitigasi: Int = 0,
    var kemungkinanSetelahMitigasi: Int = 0,
    var exposureSetelahMitigasi: Int = 0,
    var keterangan: String = "",
    var status: String = ImplementationRequest.STATUS_COMPLETED
) {
    fun build(): ImplementationRequest {
        return ImplementationRequest(
            dampakSetelahMitigasi = dampakSetelahMitigasi,
            kemungkinanSetelahMitigasi = kemungkinanSetelahMitigasi,
            exposureSetelahMitigasi = exposureSetelahMitigasi,
            keterangan = keterangan,
            status = status
        )
    }

    /**
     * Get calculated residual score
     */
    fun getResidualScore(): Int {
        return ImplementationRequest.calculateResidualScore(
            dampakSetelahMitigasi,
            kemungkinanSetelahMitigasi,
            exposureSetelahMitigasi
        )
    }

    /**
     * Get determined risk level
     */
    fun getRiskLevel(): String {
        return ImplementationRequest.determineRiskLevel(
            dampakSetelahMitigasi,
            kemungkinanSetelahMitigasi
        )
    }

    /**
     * Validate apakah semua field required sudah terisi
     */
    fun isValid(): Boolean {
        return dampakSetelahMitigasi > 0 &&
                kemungkinanSetelahMitigasi > 0 &&
                exposureSetelahMitigasi > 0 &&
                keterangan.isNotBlank() &&
                status.isNotBlank()
    }
}

/**
 * Helper untuk label score
 */
object ImplementationScoreHelper {
    fun getImpactLabel(score: Int): String {
        return when (score) {
            1 -> "Insignificant"
            2 -> "Minor"
            3 -> "Significant"
            4 -> "Major"
            5 -> "Severe"
            else -> "Unknown"
        }
    }

    fun getProbabilityLabel(score: Int): String {
        return when (score) {
            1 -> "Rare"
            2 -> "Unlikely"
            3 -> "Moderate"
            4 -> "Likely"
            5 -> "Almost Certain"
            else -> "Unknown"
        }
    }

    fun getExposureLabel(score: Int): String {
        return when (score) {
            1 -> "Minimal"
            2 -> "Low"
            3 -> "Moderate"
            4 -> "High"
            else -> "Unknown"
        }
    }
}