package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int, // End user yang akan menerima notifikasi
    val changeRequestId: Int,
    val ticketId: String,
    val title: String,
    val message: String,
    val fromStatus: String,
    val toStatus: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)