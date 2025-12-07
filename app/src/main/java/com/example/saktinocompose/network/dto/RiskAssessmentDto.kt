package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

data class RiskAssessmentApiRequest(
    @SerializedName("change_request_id")
    val changeRequestId: Int,
    @SerializedName("teknisi_id")
    val teknisiId: Int,
    @SerializedName("teknisi_name")
    val teknisiName: String,
    @SerializedName("skor_dampak")
    val skorDampak: Int,
    @SerializedName("skor_kemungkinan")
    val skorKemungkinan: Int,
    @SerializedName("skor_eksposur")
    val skorEksposur: Int,
    @SerializedName("skor_risiko")
    val skorRisiko: Int,
    @SerializedName("level_risiko")
    val levelRisiko: String,
    @SerializedName("estimasi_biaya")
    val estimasiBiaya: String?,
    @SerializedName("estimasi_waktu")
    val estimasiWaktu: String?,
    @SerializedName("photo_url")
    val photoUrl: String? // URL foto di server
)

data class RiskAssessmentApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: RiskAssessmentApiRequest?
)
