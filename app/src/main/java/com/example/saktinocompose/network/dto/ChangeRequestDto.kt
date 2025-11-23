// 3. DTO untuk Change Request
// File: app/src/main/java/com/example/saktinocompose/network/dto/ChangeRequestDto.kt

package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

// Kirim Change Request ke API
data class ChangeRequestApiRequest(
    @SerializedName("ticket_id")
    val ticketId: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("jenis_perubahan")
    val jenisPerubahan: String,
    @SerializedName("alasan")
    val alasan: String,
    @SerializedName("tujuan")
    val tujuan: String,
    @SerializedName("aset_terdampak")
    val asetTerdampak: String,
    @SerializedName("rencana_implementasi")
    val rencanaImplementasi: String,
    @SerializedName("usulan_jadwal")
    val usulanJadwal: String,
    @SerializedName("rencana_rollback")
    val rencanaRollback: String,
    @SerializedName("assigned_teknisi_id")
    val assignedTeknisiId: Int?,
    @SerializedName("assigned_teknisi_name")
    val assignedTeknisiName: String?,
    @SerializedName("status")
    val status: String
)

// Response dari API
data class ChangeRequestApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: ChangeRequestData?
)

data class ChangeRequestData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("ticket_id")
    val ticketId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("created_at")
    val createdAt: String
)

// Get all Change Requests Response
data class ChangeRequestListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: List<ChangeRequestApiRequest>?
)

// Update Status Request
data class UpdateStatusRequest(
    @SerializedName("change_request_id")
    val changeRequestId: Int,
    @SerializedName("new_status")
    val newStatus: String,
    @SerializedName("teknisi_id")
    val teknisiId: Int,
    @SerializedName("teknisi_name")
    val teknisiName: String,
    @SerializedName("notes")
    val notes: String?
)