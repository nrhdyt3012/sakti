package com.example.saktinocompose.network.dto

import android.util.Log
import com.google.gson.annotations.SerializedName

data class ChangeRequestListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<ChangeRequestApiData>?
)

data class ChangeRequestApiData(
    @SerializedName("cr_id")
    val crId: String,

    @SerializedName("tiket_id")
    val tiketId: String?,

    @SerializedName("rollback_plan")
    val rollbackPlan: String?,

    @SerializedName("type")
    val type: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("impact_desc")
    val impactDesc: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("dinas")
    val dinas: String?,

    @SerializedName("risk_score")
    val riskScore: Int?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("asset_id")
    val assetId: String?,

    @SerializedName("score_impact")
    val scoreImpact: Int?,

    @SerializedName("score_likelihood")
    val scoreLikelihood: Int?,

    @SerializedName("score_risk")
    val scoreRisk: Int?,

    @SerializedName("risk_level")
    val riskLevel: String?,

    @SerializedName("control_existing")
    val controlExisting: String?,

    @SerializedName("control_effectiveness")
    val controlEffectiveness: String?,

    @SerializedName("mitigation_plan")
    val mitigationPlan: String?,

    @SerializedName("pic_implementation")
    val picImplementation: String?,

    @SerializedName("target_completion")
    val targetCompletion: String?,

    @SerializedName("admin_schedule")
    val adminSchedule: String?,

    @SerializedName("schedule_implementation")
    val scheduleImplementation: String?,

    @SerializedName("post_likelihood")
    val postLikelihood: Int?,

    @SerializedName("post_impact")
    val postImpact: Int?,

    @SerializedName("post_residual_score")
    val postResidualScore: Int?,

    @SerializedName("post_risk_level")
    val postRiskLevel: String?,

    @SerializedName("implementation_result")
    val implementationResult: String?,

    @SerializedName("approval_status")
    val approvalStatus: String?,

    @SerializedName("change_type")
    val changeType: String?,

    @SerializedName("schedule_start")
    val scheduleStart: String?,

    @SerializedName("schedule_end")
    val scheduleEnd: String?,

    @SerializedName("implement_start_at")
    val implementStartAt: String?,

    @SerializedName("implement_end_at")
    val implementEndAt: String?,

    @SerializedName("cmdb_updated_at")
    val cmdbUpdatedAt: String?,

    // ✅ NEW: Field dari API yang hilang
    @SerializedName("ci_id")
    val ciId: String?,

    @SerializedName("impacted_asset_id")
    val impactedAssetId: String?,

    @SerializedName("rencana_implementasi")
    val rencanaImplementasi: String?,

    @SerializedName("usulan_jadwal")
    val usulanJadwal: String?,

    @SerializedName("rencana_rollback")
    val rencanaRollback: String?,

    @SerializedName("estimasi_biaya")
    val estimasiBiaya: String?,

    @SerializedName("estimasi_waktu")
    val estimasiWaktu: String?,

    @SerializedName("skor_dampak")
    val skorDampak: Int?,

    @SerializedName("skor_kemungkinan")
    val skorKemungkinan: Int?,

    @SerializedName("skor_exposure")
    val skorExposure: Int?,

    @SerializedName("inspection_photo_url")
    val inspectionPhotoUrl: String?
)

data class ChangeRequestDetailResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ChangeRequestApiData?
)

fun ChangeRequestApiData.logDetails(tag: String = "ChangeRequestDto") {
    Log.d(tag, """
        ╔════════════════════════════════════════════
        ║ CR Data Details
        ╠════════════════════════════════════════════
        ║ CR ID: $crId
        ║ Ticket ID: $tiketId
        ║ Status: $status
        ║ Approval Status: $approvalStatus
        ╠════════════════════════════════════════════
        ║ Asset Information:
        ║   - asset_id: $assetId
        ║   - impacted_asset_id: $impactedAssetId
        ║   - ci_id: $ciId
        ╠════════════════════════════════════════════
        ║ Implementation:
        ║   - rencana_implementasi: ${rencanaImplementasi?.take(50)}...
        ║   - usulan_jadwal: $usulanJadwal
        ║   - rollback_plan: ${rollbackPlan?.take(50)}...
        ╠════════════════════════════════════════════
        ║ Risk Scores:
        ║   - Impact: $skorDampak
        ║   - Likelihood: $skorKemungkinan  
        ║   - Exposure: $skorExposure
        ║   - Risk Score: $riskScore
        ║   - Risk Level: $riskLevel
        ╠════════════════════════════════════════════
        ║ Timestamps:
        ║   - Created: $createdAt
        ║   - Updated: $updatedAt
        ╚════════════════════════════════════════════
    """.trimIndent())
}

/**
 * ✅ Helper untuk check apakah data valid
 */
fun ChangeRequestApiData.isValid(): Boolean {
    return !crId.isNullOrBlank() &&
            !title.isNullOrBlank() &&
            !status.isNullOrBlank()
}

/**
 * ✅ Helper untuk get impacted assets as list
 */
fun ChangeRequestApiData.getImpactedAssetsList(): List<String> {
    if (impactedAssetId.isNullOrBlank()) return emptyList()

    return try {
        val trimmed = impactedAssetId.trim()

        when {
            // JSON array format
            trimmed.startsWith("[") && trimmed.endsWith("]") -> {
                trimmed
                    .removeSurrounding("[", "]")
                    .replace("\"", "")
                    .replace("\\", "")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }

            // PostgreSQL array format
            trimmed.startsWith("{") && trimmed.endsWith("}") -> {
                trimmed
                    .removeSurrounding("{", "}")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }

            // Comma-separated string
            trimmed.contains(",") -> {
                trimmed.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }

            // Single asset
            else -> listOf(trimmed)
        }
    } catch (e: Exception) {
        Log.e("ChangeRequestDto", "Error parsing impacted_asset_id: $impactedAssetId", e)
        emptyList()
    }
}

/**
 * ✅ Helper untuk format display impacted assets
 */
fun ChangeRequestApiData.formatImpactedAssetsDisplay(): String {
    val list = getImpactedAssetsList()
    return when {
        list.isEmpty() -> "No impacted assets"
        list.size == 1 -> list.first()
        list.size <= 3 -> list.joinToString(", ")
        else -> "${list.take(3).joinToString(", ")} +${list.size - 3} more"
    }
}