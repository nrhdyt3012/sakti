package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.saktinocompose.viewmodel.ApprovalHistoryViewModel
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailPage(
    changeRequest: ChangeRequest,
    userId: Int,
    userName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    changeRequestViewModel: ChangeRequestViewModel = viewModel(),
    approvalHistoryViewModel: ApprovalHistoryViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())

    var showActionDialog by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Dialog konfirmasi aksi
    if (showActionDialog && selectedAction != null) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("Konfirmasi Aksi") },
            text = {
                Column {
                    Text("Anda akan mengubah status menjadi: $selectedAction")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            // Update status
                            changeRequestViewModel.updateChangeRequestStatus(
                                changeRequest,
                                selectedAction!!
                            )

                            // Add approval history
                            approvalHistoryViewModel.addApprovalHistory(
                                changeRequestId = changeRequest.id,
                                approverUserId = userId,
                                approverName = userName,
                                fromStatus = changeRequest.status,
                                toStatus = selectedAction!!,
                                notes = notes
                            )

                            showActionDialog = false
                            showSuccessDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getStatusColor(selectedAction!!)
                    )
                ) {
                    Text("Konfirmasi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActionDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBackClick()
            },
            title = { Text("Berhasil") },
            text = { Text("Status berhasil diperbarui") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    onBackClick()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Review Permohonan", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF37474F),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ticket ID Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ticket ID",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = changeRequest.ticketId,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = getStatusColor(changeRequest.status)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = changeRequest.status,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Detail Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Informasi Permohonan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider()

                    DetailRow("Jenis Perubahan", changeRequest.jenisPerubahan)
                    DetailRow("Alasan", changeRequest.alasan)
                    DetailRow("Tujuan", changeRequest.tujuan)
                    DetailRow("Aset Terdampak", changeRequest.asetTerdampak)
                    DetailRow("Usulan Jadwal", changeRequest.usulanJadwal)
                    DetailRow("Dibuat pada", dateFormat.format(Date(changeRequest.createdAt)))
                    DetailRow("Diperbarui pada", dateFormat.format(Date(changeRequest.updatedAt)))
                }
            }

            // Action Buttons
            Text(
                text = "Aksi Review",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            when (changeRequest.status) {
                "Submitted" -> {
                    ActionButton(
                        text = "Mulai Review",
                        icon = Icons.Default.RateReview,
                        color = Color(0xFF2196F3),
                        onClick = {
                            selectedAction = "In-Review"
                            showActionDialog = true
                        }
                    )
                }
                "In-Review" -> {
                    ActionButton(
                        text = "Approve",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF4CAF50),
                        onClick = {
                            selectedAction = "Approved"
                            showActionDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ActionButton(
                        text = "Reject (Kembali ke Submitted)",
                        icon = Icons.Default.Cancel,
                        color = Color(0xFFD32F2F),
                        onClick = {
                            selectedAction = "Submitted"
                            showActionDialog = true
                        }
                    )
                }
                "Approved" -> {
                    ActionButton(
                        text = "Jadwalkan",
                        icon = Icons.Default.Schedule,
                        color = Color(0xFFFF9800),
                        onClick = {
                            selectedAction = "Scheduled"
                            showActionDialog = true
                        }
                    )
                }
                "Scheduled" -> {
                    ActionButton(
                        text = "Mulai Implementasi",
                        icon = Icons.Default.Build,
                        color = Color(0xFFFF5722),
                        onClick = {
                            selectedAction = "Implementing"
                            showActionDialog = true
                        }
                    )
                }
                "Implementing" -> {
                    ActionButton(
                        text = "Selesai (Completed)",
                        icon = Icons.Default.Done,
                        color = Color(0xFF4CAF50),
                        onClick = {
                            selectedAction = "Completed"
                            showActionDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ActionButton(
                        text = "Gagal (Failed)",
                        icon = Icons.Default.Error,
                        color = Color(0xFFD32F2F),
                        onClick = {
                            selectedAction = "Failed"
                            showActionDialog = true
                        }
                    )
                }
                "Completed", "Failed" -> {
                    ActionButton(
                        text = "Tutup Tiket",
                        icon = Icons.Default.Close,
                        color = Color(0xFF607D8B),
                        onClick = {
                            selectedAction = "Closed"
                            showActionDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Submitted" -> Color(0xFF9E9E9E)
        "In-Review" -> Color(0xFF2196F3)
        "Approved" -> Color(0xFF4CAF50)
        "Scheduled" -> Color(0xFFFF9800)
        "Implementing" -> Color(0xFFFF5722)
        "Completed" -> Color(0xFF4CAF50)
        "Failed" -> Color(0xFFD32F2F)
        "Closed" -> Color(0xFF607D8B)
        else -> Color.Gray
    }
}