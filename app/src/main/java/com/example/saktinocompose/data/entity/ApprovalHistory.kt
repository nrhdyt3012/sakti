package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "approval_history")
data class ApprovalHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val changeRequestId: String,  // ✅ Changed from Int to String
    val approverUserId: Int,
    val approverName: String,
    val fromStatus: String,
    val toStatus: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)