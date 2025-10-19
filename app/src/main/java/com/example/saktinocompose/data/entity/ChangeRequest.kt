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
    val dampakRisiko: String,
    val rencanaImplementasi: String,
    val rencanaRollback: String,
    val jadwal: String, // Format: yyyy-MM-dd
    val pic: String,
    val status: String = "Submitted", // "Submitted", "Perlu Ditinjau", "Sedang Ditinjau", "Selesai Ditinjau"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)