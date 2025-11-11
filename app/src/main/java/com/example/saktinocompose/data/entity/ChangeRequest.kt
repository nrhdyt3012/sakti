package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "change_requests")
data class ChangeRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ticketId: String, // Format: CR-YYYYMMDD-XXXX
    val userId: Int, // Foreign key ke User
    val jenisPerubahan: String, // "Standar", "Minor", "Major", "Emergency"
    val alasan: String,
    val tujuan: String,
    val asetTerdampak: String,
    val rencanaImplementasi: String,
    val usulanJadwal: String, // Format: yyyy-MM-dd
    val rencanaRollback: String,
    val assignedTeknisiId: Int? = null, // ID teknisi yang dipilih
    val assignedTeknisiName: String? = null, // Nama teknisi yang dipilih
    val photoPath: String? = null, // Path foto bukti dari teknisi (setelah inspeksi)
    val estimasiBiaya: String? = null, // Estimasi biaya dari teknisi
    val estimasiWaktu: String? = null, // Estimasi waktu dari teknisi
    val status: String = "Submitted", // "Submitted", "Reviewed", "Approved", "Scheduled", "Implementing", "Completed", "Failed", "Closed"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)