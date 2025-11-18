package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "risk_assessments")
data class RiskAssessment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val changeRequestId: Int,
    val teknisiId: Int,
    val teknisiName: String,
    val skorDampak: Int,
    val skorKemungkinan: Int,
    val skorEksposur: Int, // Tambahan field eksposur
    val skorRisiko: Int,
    val levelRisiko: String,
    val createdAt: Long = System.currentTimeMillis()
)