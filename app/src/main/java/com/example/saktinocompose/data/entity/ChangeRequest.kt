package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "change_requests")
data class ChangeRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ticketId: String,
    val userId: Int,
    val jenisPerubahan: String,
    val alasan: String,
    val tujuan: String,
    val asetTerdampak: String,
    val rencanaImplementasi: String,
    val usulanJadwal: String,
    val rencanaRollback: String,
    val assignedTeknisiId: Int? = null,
    val assignedTeknisiName: String? = null,
    val photoPath: String? = null,
    val estimasiBiaya: String? = null,
    val estimasiWaktu: String? = null,
    val scheduledDate: String? = null,  // Format: yyyy-MM-dd
    val scheduledTimestamp: Long? = null, // Timestamp for comparison

    // Field untuk hasil implementasi
    val dampakSetelahMitigasi: Int? = null,
    val kemungkinanSetelahMitigasi: Int? = null,
    val exposur: Int? = null,
    val skorResidual: Int? = null,
    val levelRisikoResidual: String? = null,
    val keteranganHasilImplementasi: String? = null,

    val status: String = "Submitted",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)