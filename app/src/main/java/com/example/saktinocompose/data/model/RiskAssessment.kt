package com.example.saktinocompose.data.model


data class RiskAssessment(
    val id: Int = 0,
    val changeRequestId: String,
    val teknisiId: Int,
    val teknisiName: String,
    val skorDampak: Int,
    val skorKemungkinan: Int,
    val skorEksposur: Int,
    val skorRisiko: Int,
    val levelRisiko: String,
    val createdAt: Long = System.currentTimeMillis()
)