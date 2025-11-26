package com.example.saktinocompose.teknisi.pages

import androidx.compose.ui.graphics.Color


// Helper functions based on 5x5 risk matrix
fun calculateRiskLevel(impact: Int, probability: Int): String {
    val riskScore = impact * probability
    return when {
        riskScore <= 3 -> "Very Low"
        riskScore <= 6 -> "Low"
        riskScore <= 12 -> "Medium"
        riskScore <= 18 -> "High"
        riskScore <= 23 -> "Very High"
        else -> "Extreme"
    }
}

fun getRiskLevelColor(level: String): Color {
    return when (level) {
        "Very Low" -> Color(0xFF4CAF50)
        "Low" -> Color(0xFF8BC34A)
        "Medium" -> Color(0xFFFFEB3B)
        "High" -> Color(0xFFFF9800)
        "Very High" -> Color(0xFFFF5722)
        "Extreme" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
}

fun getImpactLabel(score: Int): String {
    return when (score) {
        1 -> "Insignificant"
        2 -> "Minor"
        3 -> "Significant"
        4 -> "Major"
        5 -> "Severe"
        else -> ""
    }
}

fun getProbabilityLabel(score: Int): String {
    return when (score) {
        1 -> "Rare"
        2 -> "Unlikely"
        3 -> "Moderate"
        4 -> "Likely"
        5 -> "Almost Certain"
        else -> ""
    }

}
fun getExposurLabel(score: Int): String {
    return when (score) {
        1 -> "Minimal"
        2 -> "Low"
        3 -> "Moderate"
        4 -> "High"
        else -> ""
    }
}