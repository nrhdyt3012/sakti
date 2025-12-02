package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "change_requests")
data class ChangeRequest(
    @PrimaryKey
    val id: String,  // cr_id dari API

    val ticketId: String,
    val type: String,  // jenisPerubahan dari API
    val title: String,  // alasan dari API
    val description: String,  // tujuan dari API

    // Asset info
    val assetId: String,  // asset_id dari API
    val asetTerdampak: String,  // Formatted: "id:nama:tipeRelasi"
    val relasiConfigurationItem: String,  // Related CIs

    // Implementation plan
    val rencanaImplementasi: String,
    val usulanJadwal: String,
    val rollbackPlan: String,

    // Assigned technician
    val assignedTeknisiId: String? = null,
    val assignedTeknisiName: String? = null,

    // Inspection results
    val photoPath: String? = null,
    val estimasiBiaya: String? = null,
    val estimasiWaktu: String? = null,

    // Schedule
    val scheduledDate: String? = null,
    val scheduleStart: String? = null,
    val scheduleEnd: String? = null,

    // Risk scores (from inspection)
    val scoreImpact: Int? = null,
    val scoreLikelihood: Int? = null,
    val scoreRisk: Int? = null,
    val riskLevel: String? = null,

    // Post-implementation
    val postImpact: Int? = null,
    val postLikelihood: Int? = null,
    val postResidualScore: Int? = null,
    val postRiskLevel: String? = null,
    val implementationResult: String? = null,

    // Status & timestamps
    val status: String,
    val approvalStatus: String? = null,
    val createdAt: String,  // ISO 8601 string
    val updatedAt: String,  // ISO 8601 string

    // Additional info from API
    val dinas: String? = null,
    val impactDesc: String? = null,
    val controlExisting: String? = null,
    val controlEffectiveness: String? = null,
    val mitigationPlan: String? = null,
    val jenisPerubahan: String,  // ✅ TAMBAHKAN INI
    val skorEksposur: Int,  // ✅ TAMBAHKAN INI

    // ✅ TAMBAHKAN field untuk revision
    val revisionNotes: String? = null,
    val revisionCount: Int = 0,

    // ✅ TAMBAHKAN field untuk scheduled timestamp
    val scheduledTimestamp: Long? = null,

    // ✅ TAMBAHKAN field untuk implementation details
    val dampakSetelahMitigasi: Int? = null,
    val kemungkinanSetelahMitigasi: Int? = null,
    val exposur: Int? = null,
    val skorResidual: Int? = null,
    val levelRisikoResidual: String? = null,
    val keteranganHasilImplementasi: String? = null,

    // ✅ TAMBAHKAN field untuk user ID
    val userId: String? = null,  // End user yang membuat request

    // ✅ TAMBAHKAN field untuk CI ID
    val idAset: String = assetId,  // Alias untuk assetId
    val idPerubahan: String = id  // Alias untuk id
)