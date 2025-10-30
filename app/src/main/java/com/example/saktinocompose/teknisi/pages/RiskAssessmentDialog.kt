package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAssessmentDialog(
    onDismiss: () -> Unit,
    onSave: (skorDampak: Int, skorKemungkinan: Int, skorRisiko: Int, levelRisiko: String) -> Unit
) {
    var skorDampak by remember { mutableIntStateOf(0) }
    var skorKemungkinan by remember { mutableIntStateOf(0) }
    var showDampakDropdown by remember { mutableStateOf(false) }
    var showKemungkinanDropdown by remember { mutableStateOf(false) }

    val skorRisiko = skorDampak * skorKemungkinan
    val levelRisiko = calculateRiskLevel(skorDampak, skorKemungkinan)
    val levelColor = getRiskLevelColor(levelRisiko)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Risk Assessment",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Skor Dampak
                Text(
                    text = "Skor Dampak (Impact)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenuBox(
                    expanded = showDampakDropdown,
                    onExpandedChange = { showDampakDropdown = !showDampakDropdown }
                ) {
                    OutlinedTextField(
                        value = if (skorDampak == 0) "" else "$skorDampak - ${getImpactLabel(skorDampak)}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Skor Dampak") },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showDampakDropdown,
                        onDismissRequest = { showDampakDropdown = false }
                    ) {
                        (1..5).forEach { score ->
                            DropdownMenuItem(
                                text = { Text("$score - ${getImpactLabel(score)}") },
                                onClick = {
                                    skorDampak = score
                                    showDampakDropdown = false
                                }
                            )
                        }
                    }
                }

                // Skor Kemungkinan
                Text(
                    text = "Skor Kemungkinan (Probability)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenuBox(
                    expanded = showKemungkinanDropdown,
                    onExpandedChange = { showKemungkinanDropdown = !showKemungkinanDropdown }
                ) {
                    OutlinedTextField(
                        value = if (skorKemungkinan == 0) "" else "$skorKemungkinan - ${getProbabilityLabel(skorKemungkinan)}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Skor Kemungkinan") },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showKemungkinanDropdown,
                        onDismissRequest = { showKemungkinanDropdown = false }
                    ) {
                        (1..5).forEach { score ->
                            DropdownMenuItem(
                                text = { Text("$score - ${getProbabilityLabel(score)}") },
                                onClick = {
                                    skorKemungkinan = score
                                    showKemungkinanDropdown = false
                                }
                            )
                        }
                    }
                }

                // Skor Risiko (Auto calculated)
                if (skorDampak > 0 && skorKemungkinan > 0) {
                    Divider()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Hasil Perhitungan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Skor Risiko:", fontSize = 13.sp)
                                Text(
                                    "$skorRisiko",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Level Risiko:", fontSize = 13.sp)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = levelColor
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = levelRisiko,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (skorDampak > 0 && skorKemungkinan > 0) {
                        onSave(skorDampak, skorKemungkinan, skorRisiko, levelRisiko)
                    }
                },
                enabled = skorDampak > 0 && skorKemungkinan > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// Helper functions based on 5x5 risk matrix
fun calculateRiskLevel(impact: Int, probability: Int): String {
    val riskScore = impact * probability
    return when {
        riskScore <= 3 -> "Very Low"
        riskScore <= 5 -> "Low"
        riskScore <= 10 -> "Medium"
        riskScore <= 15 -> "High"
        riskScore <= 19 -> "Very High"
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