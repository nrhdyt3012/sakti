package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "change_requests")
data class ChangeRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ticketId: String,
    val userId: Int,
    val idPerubahan: String,  // ✅ UUID-based ID Perubahan
    val jenisPerubahan: String,
    val alasan: String,
    val tujuan: String,
    // ✅ BARU: 3 Field untuk Aset
    val idAset: String,                    // ID Aset (manual input)
    val asetTerdampak: String,              // Aset yang Diperbaiki (dari list)
    val relasiConfigurationItem: String,     val rencanaImplementasi: String,
    val usulanJadwal: String,
    val rencanaRollback: String,
    val assignedTeknisiId: Int? = null,
    val assignedTeknisiName: String? = null,
    val photoPath: String? = null,
    val estimasiBiaya: String? = null,
    val estimasiWaktu: String? = null,
    val scheduledDate: String? = null,
    val scheduledTimestamp: Long? = null,

    // Field untuk skor eksposur awal (dari inspeksi)
    val skorEksposur: Int? = null,

    // Field untuk hasil implementasi
    val dampakSetelahMitigasi: Int? = null,
    val kemungkinanSetelahMitigasi: Int? = null,
    val exposur: Int? = null,
    val skorResidual: Int? = null,
    val levelRisikoResidual: String? = null,
    val keteranganHasilImplementasi: String? = null,

    // Field untuk revisi
    val revisionNotes: String? = null,
    val revisionCount: Int = 0,

    val status: String = "Submitted",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)