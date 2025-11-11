package com.example.saktinocompose.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColoredJenisPerubahan(
    jenis: String,
    modifier: Modifier = Modifier
) {
    val color = when (jenis) {
        "Emergency" -> Color(0xFFD32F2F)  // Red
        "Major" -> Color(0xFFFF9800)      // Orange
        "Minor" -> Color(0xFF2196F3)      // Blue
        "Standar" -> Color(0xFF4CAF50)    // Green
        else -> Color.Gray
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = jenis,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// Helper untuk text saja tanpa card
@Composable
fun ColoredJenisText(
    jenis: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 13
) {
    val color = when (jenis) {
        "Emergency" -> Color(0xFFD32F2F)  // Red
        "Major" -> Color(0xFFFF9800)      // Orange
        "Minor" -> Color(0xFF2196F3)      // Blue
        "Standar" -> Color(0xFF4CAF50)    // Green
        else -> Color.Gray
    }

    Text(
        text = jenis,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}