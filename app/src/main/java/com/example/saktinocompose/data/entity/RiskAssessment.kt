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
    val skorDampak: Int, // 1-5
    val skorKemungkinan: Int, // 1-5
    val skorRisiko: Int, // skorDampak * skorKemungkinan
    val levelRisiko: String, // "Very Low", "Low", "Medium", "High", "Very High", "Extreme"
    val createdAt: Long = System.currentTimeMillis()
)