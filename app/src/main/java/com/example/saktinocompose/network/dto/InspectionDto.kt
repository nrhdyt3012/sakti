package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

// ✅ UPDATED: Sesuai dengan format API yang benar
data class InspectionRequest(
    @SerializedName("jenis_perubahan")
    val jenisPerubahan: String,

    @SerializedName("alasan")
    val alasan: String,

    @SerializedName("tujuan")
    val tujuan: String,

    @SerializedName("ci_id")
    val ciId: String,

    @SerializedName("impacted_assets")
    val impactedAssets: List<ImpactedAssetItem>,  // ✅ CHANGED: List of objects

    @SerializedName("rencana_implementasi")
    val rencanaImplementasi: String,

    @SerializedName("usulan_jadwal")
    val usulanJadwal: String, // Format: "2025-12-20"

    @SerializedName("rencana_rollback")
    val rencanaRollback: String,

    @SerializedName("skor_dampak")
    val skorDampak: Int,

    @SerializedName("skor_kemungkinan")
    val skorKemungkinan: Int,

    @SerializedName("skor_exposure")
    val skorExposure: Int
)

// ✅ NEW: Item untuk impacted assets
data class ImpactedAssetItem(
    @SerializedName("asset_id")
    val assetId: String
)

/**
 * Data untuk build InspectionRequest dari ChangeRequest local
 */

