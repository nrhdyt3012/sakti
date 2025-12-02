package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// File: app/src/main/java/com/example/saktinocompose/data/entity/ChangeRequest.kt

@Entity(tableName = "change_requests")
data class ChangeRequest(
    @PrimaryKey(autoGenerate = true)  // ✅ Tidak auto-generate, pakai cr_id dari API
    val id: String,  // ✅ cr_id dari API (String, bukan Int)

    val ticketId: String,
    val type: String,  // ✅ type (jenisPerubahan)
    val title: String,  // ✅ title (alasan)
    val description: String,  // ✅ description (tujuan)

    // Asset info
    val assetId: String,
    val asetTerdampak: String,  // ✅ Tetap untuk kompatibilitas UI
    val relasiConfigurationItem: String,

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

    // Risk scores
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
    val createdAt: String,  // ✅ ISO 8601 string dari API
    val updatedAt: String,  // ✅ ISO 8601 string dari API

    // Additional info
    val dinas: String? = null,
    val impactDesc: String? = null,
    val controlExisting: String? = null,
    val controlEffectiveness: String? = null,
    val mitigationPlan: String? = null,
    val jenisPerubahan: String,
    val skorEksposur: Int
)