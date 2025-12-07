package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

data class ChangeRequestListResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<ChangeRequestApiData>?
)

data class ChangeRequestApiData(
    @SerializedName("cr_id")
    val crId: String,
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
    @SerializedName("ticket_id")
    val ticketId: String?,
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
    val cmdbUpdatedAt: String?
)

data class ChangeRequestDetailResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ChangeRequestApiData?
)

// Generic Response