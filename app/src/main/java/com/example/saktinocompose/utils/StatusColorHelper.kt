package com.example.saktinocompose.utils

import androidx.compose.ui.graphics.Color

/**
 * ✅ BARU: Centralized status color mapping
 * Gunakan ini di semua tempat untuk konsistensi
 */
object StatusColorHelper {

    fun getStatusColor(status: String): Color {
        return when (status) {
            "Submitted" -> Color(0xFF9E9E9E)        // Gray
            "Need Approval" -> Color(0xFFFFA726)    // Orange
            "Reviewed" -> Color(0xFF2196F3)         // Blue
            "Revision" -> Color(0xFFFF9800)         // Orange
            "Approved" -> Color(0xFF4CAF50)         // Green
            "Scheduled" -> Color(0xFFFF9800)        // Orange
            "Implementing" -> Color(0xFFFF5722)     // Deep Orange
            "Completed" -> Color(0xFF4CAF50)        // Green
            "Failed" -> Color(0xFFD32F2F)           // Red
            "Closed" -> Color(0xFF607D8B)           // Blue Gray
            "Emergency" -> Color(0xFFD32F2F)        // Red
            else -> Color.Gray
        }
    }

    fun getStatusLabel(status: String): String {
        return when (status.uppercase()) {
            "SUBMITTED", "PENDING" -> "Submitted"
            "NEED APPROVAL", "NEED_APPROVAL" -> "Need Approval"
            "REVIEWED", "IN_REVIEW" -> "Reviewed"
            "REVISION", "NEED_REVISION" -> "Revision"
            "APPROVED" -> "Approved"
            "SCHEDULED" -> "Scheduled"
            "IMPLEMENTING", "IN_PROGRESS" -> "Implementing"
            "COMPLETED", "DONE" -> "Completed"
            "FAILED", "REJECTED" -> "Failed"
            "CLOSED" -> "Closed"
            "EMERGENCY" -> "Emergency"
            else -> status
        }
    }

    fun getAllStatuses(): List<Pair<String, Color>> {
        return listOf(
            "Submitted" to getStatusColor("Submitted"),
            "Need Approval" to getStatusColor("Need Approval"),
            "Reviewed" to getStatusColor("Reviewed"),
            "Revision" to getStatusColor("Revision"),
            "Approved" to getStatusColor("Approved"),
            "Scheduled" to getStatusColor("Scheduled"),
            "Implementing" to getStatusColor("Implementing"),
            "Completed" to getStatusColor("Completed"),
            "Failed" to getStatusColor("Failed"),
            "Closed" to getStatusColor("Closed")
        )
    }
}