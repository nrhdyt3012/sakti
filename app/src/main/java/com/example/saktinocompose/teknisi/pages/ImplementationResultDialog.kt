package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.viewmodel.NotificationViewModel
import androidx.compose.ui.res.stringResource
import com.example.saktinocompose.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImplementationResultDialog(
    changeRequest: ChangeRequest,
    onDismiss: () -> Unit,
    onSave: (
        dampakSetelahMitigasi: Int,
        kemungkinanSetelahMitigasi: Int,
        exposur: Int,
        skorResidual: Int,
        levelRisikoResidual: String,
        keteranganHasilImplementasi: String
    ) -> Unit
) {
    var dampakSetelahMitigasi by remember { mutableIntStateOf(0) }
    var kemungkinanSetelahMitigasi by remember { mutableIntStateOf(0) }
    var exposur by remember { mutableIntStateOf(0) }
    var keteranganHasilImplementasi by remember { mutableStateOf("") }

    var showDampakDropdown by remember { mutableStateOf(false) }
    var showKemungkinanDropdown by remember { mutableStateOf(false) }
    var showExposurDropdown by remember { mutableStateOf(false) }

    val skorResidual = dampakSetelahMitigasi * kemungkinanSetelahMitigasi * exposur
    val levelRisikoResidual = calculateRiskLevel(dampakSetelahMitigasi, kemungkinanSetelahMitigasi)
    val levelColor = getRiskLevelColor(levelRisikoResidual)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Result",
                    tint = Color(0xFF4CAF50)
                )
                Text(
                    text = stringResource(R.string.implementation_result),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Ticket: ${changeRequest.ticketId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Isi hasil implementasi untuk menyelesaikan change request",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Dampak Setelah Mitigasi
                Text(
                    text = stringResource(R.string.impact_after_mitigation) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenuBox(
                    expanded = showDampakDropdown,
                    onExpandedChange = { showDampakDropdown = !showDampakDropdown }
                ) {
                    OutlinedTextField(
                        value = if (dampakSetelahMitigasi == 0) "" else "$dampakSetelahMitigasi - ${getImpactLabel(dampakSetelahMitigasi)}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Impact Score") },
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
                                    dampakSetelahMitigasi = score
                                    showDampakDropdown = false
                                }
                            )
                        }
                    }
                }

                // Kemungkinan Setelah Mitigasi
                Text(
                    text = stringResource(R.string.probability_after_mitigation) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenuBox(
                    expanded = showKemungkinanDropdown,
                    onExpandedChange = { showKemungkinanDropdown = !showKemungkinanDropdown }
                ) {
                    OutlinedTextField(
                        value = if (kemungkinanSetelahMitigasi == 0) "" else "$kemungkinanSetelahMitigasi - ${getProbabilityLabel(kemungkinanSetelahMitigasi)}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Probabilit Score") },
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
                                    kemungkinanSetelahMitigasi = score
                                    showKemungkinanDropdown = false
                                }
                            )
                        }
                    }
                }

                // Exposur
                Text(
                    text = "3. Exposure *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenuBox(
                    expanded = showExposurDropdown,
                    onExpandedChange = { showExposurDropdown = !showExposurDropdown }
                ) {
                    OutlinedTextField(
                        value = if (exposur == 0) "" else exposur.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Exposure Score") },
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
                        expanded = showExposurDropdown,
                        onDismissRequest = { showExposurDropdown = false }
                    ) {
                        (1..4).forEach { score ->
                            DropdownMenuItem(
                                text = { Text(score.toString()) },
                                onClick = {
                                    exposur = score
                                    showExposurDropdown = false
                                }
                            )
                        }
                    }
                }

                // Hasil Perhitungan
                if (dampakSetelahMitigasi > 0 && kemungkinanSetelahMitigasi > 0 && exposur > 0) {
                    HorizontalDivider()

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
                                text = "Residual Risk Calculation Results",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Residual Score:", fontSize = 13.sp)
                                Text(
                                    "$skorResidual",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.residual_risk_level) + ":", fontSize = 13.sp)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = levelColor
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = levelRisikoResidual,
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

                // Keterangan Hasil Implementasi
                Text(
                    text = stringResource(R.string.implementation_description) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = keteranganHasilImplementasi,
                    onValueChange = { keteranganHasilImplementasi = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    placeholder = { Text("Explain the results of implementing changes...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dampakSetelahMitigasi > 0 &&
                        kemungkinanSetelahMitigasi > 0 &&
                        exposur > 0 &&
                        keteranganHasilImplementasi.isNotBlank()
                    ) {
                        onSave(
                            dampakSetelahMitigasi,
                            kemungkinanSetelahMitigasi,
                            exposur,
                            skorResidual,
                            levelRisikoResidual,
                            keteranganHasilImplementasi
                        )
                    }
                },
                enabled = dampakSetelahMitigasi > 0 &&
                        kemungkinanSetelahMitigasi > 0 &&
                        exposur > 0 &&
                        keteranganHasilImplementasi.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(stringResource(R.string.complete_implementation))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}