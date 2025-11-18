package com.example.saktinocompose.teknisi.pages

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.enduser.pages.DetailItem
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import com.example.saktinocompose.viewmodel.RiskAssessmentViewModel
import com.example.saktinocompose.viewmodel.ApprovalHistoryViewModel
import com.example.saktinocompose.viewmodel.NotificationViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailFormTeknisiPage(
    changeRequest: ChangeRequest,
    teknisiId: Int,
    teknisiName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    changeRequestViewModel: ChangeRequestViewModel = viewModel(),
    riskAssessmentViewModel: RiskAssessmentViewModel = viewModel(),
    approvalHistoryViewModel: ApprovalHistoryViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()

) {
    val scrollState = rememberScrollState()
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    val createdDate = dateFormat.format(Date(changeRequest.createdAt))
    val updatedDate = dateFormat.format(Date(changeRequest.updatedAt))

    var showInspectionDialog by remember { mutableStateOf(false) }
    var showSchedulingDialog by remember { mutableStateOf(false) }
    var showImplementationDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val existingRiskAssessment by riskAssessmentViewModel.getRiskAssessmentFlow(changeRequest.id)
        .collectAsState(initial = null)

    val isEmergency = changeRequest.jenisPerubahan == "Emergency"
    val isSubmitted = changeRequest.status == "Submitted"
    val isReviewed = changeRequest.status == "Reviewed"
    val isScheduled = changeRequest.status == "Scheduled"
    val isImplementing = changeRequest.status == "Implementing"
    val isCompleted = changeRequest.status == "Completed"


    // Inspection Dialog
    if (showInspectionDialog) {
        InspectionDialog(
            changeRequest = changeRequest,
            onDismiss = { showInspectionDialog = false },
            onSave = { jenisPerubahan, estimasiBiaya, estimasiWaktu, skorDampak, skorKemungkinan, skorRisiko, levelRisiko, photoPath ->
                // Update ChangeRequest dengan semua data baru
                val updatedRequest = changeRequest.copy(
                    jenisPerubahan = jenisPerubahan,
                    estimasiBiaya = estimasiBiaya,
                    estimasiWaktu = estimasiWaktu,
                    photoPath = photoPath,
                    status = "Reviewed",
                    updatedAt = System.currentTimeMillis()
                )
                changeRequestViewModel.updateFullChangeRequest(updatedRequest)

                // Save Risk Assessment
                riskAssessmentViewModel.saveRiskAssessment(
                    changeRequestId = changeRequest.id,
                    teknisiId = teknisiId,
                    teknisiName = teknisiName,
                    skorDampak = skorDampak,
                    skorKemungkinan = skorKemungkinan,
                    skorRisiko = skorRisiko,
                    levelRisiko = levelRisiko
                )

                // Add Approval History
                approvalHistoryViewModel.addApprovalHistory(
                    changeRequestId = changeRequest.id,
                    approverUserId = teknisiId,
                    approverName = teknisiName,
                    fromStatus = changeRequest.status,
                    toStatus = "Reviewed",
                    notes = "Inspeksi selesai. Estimasi biaya: $estimasiBiaya, Estimasi waktu: $estimasiWaktu, Level risiko: $levelRisiko"
                )

                // Kirim notifikasi ke end user
                notificationViewModel.createNotification(
                    userId = changeRequest.userId,
                    changeRequestId = changeRequest.id,
                    ticketId = changeRequest.ticketId,
                    fromStatus = changeRequest.status,
                    toStatus = "Reviewed"
                )

                showInspectionDialog = false
                successMessage = "Inspeksi berhasil! Status berubah menjadi 'Reviewed'"
                showSuccessDialog = true
            }
        )
    }

    // Scheduling Dialog
    if (showSchedulingDialog) {
        SchedulingDialog(
            changeRequest = changeRequest,
            onDismiss = { showSchedulingDialog = false },
            onSave = { scheduledDate ->
                // Parse to timestamp
                val dateTimeString = "$scheduledDate"
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val scheduledTimestamp = sdf.parse(dateTimeString)?.time ?: System.currentTimeMillis()

                // Update ChangeRequest
                val updatedRequest = changeRequest.copy(
                    scheduledDate = scheduledDate,
                    scheduledTimestamp = scheduledTimestamp,
                    status = "Scheduled",
                    updatedAt = System.currentTimeMillis()
                )
                changeRequestViewModel.updateFullChangeRequest(updatedRequest)

                // Add Approval History
                approvalHistoryViewModel.addApprovalHistory(
                    changeRequestId = changeRequest.id,
                    approverUserId = teknisiId,
                    approverName = teknisiName,
                    fromStatus = changeRequest.status,
                    toStatus = "Scheduled",
                    notes = "Implementasi dijadwalkan pada $scheduledDate"
                )

                // Kirim notifikasi ke end user
                notificationViewModel.createNotification(
                    userId = changeRequest.userId,
                    changeRequestId = changeRequest.id,
                    ticketId = changeRequest.ticketId,
                    fromStatus = changeRequest.status,
                    toStatus = "Scheduled"
                )

                showSchedulingDialog = false
                successMessage = "Implementasi berhasil dijadwalkan!"
                showSuccessDialog = true
            }
        )
    }
    // Implementation Result Dialog
    if (showImplementationDialog) {
        ImplementationResultDialog(
            changeRequest = changeRequest,
            onDismiss = { showImplementationDialog = false },
            onSave = { dampakSetelahMitigasi, kemungkinanSetelahMitigasi, exposur, skorResidual, levelRisikoResidual, keteranganHasilImplementasi ->
                val updatedRequest = changeRequest.copy(
                    dampakSetelahMitigasi = dampakSetelahMitigasi,
                    kemungkinanSetelahMitigasi = kemungkinanSetelahMitigasi,
                    exposur = exposur,
                    skorResidual = skorResidual,
                    levelRisikoResidual = levelRisikoResidual,
                    keteranganHasilImplementasi = keteranganHasilImplementasi,
                    status = "Completed",
                    updatedAt = System.currentTimeMillis()
                )
                changeRequestViewModel.updateFullChangeRequest(updatedRequest)

                approvalHistoryViewModel.addApprovalHistory(
                    changeRequestId = changeRequest.id,
                    approverUserId = teknisiId,
                    approverName = teknisiName,
                    fromStatus = "Implementing",
                    toStatus = "Completed",
                    notes = "Implementasi selesai. Skor residual: $skorResidual, Level risiko residual: $levelRisikoResidual"
                )

                // Kirim notifikasi ke end user
                notificationViewModel.createNotification(
                    userId = changeRequest.userId,
                    changeRequestId = changeRequest.id,
                    ticketId = changeRequest.ticketId,
                    fromStatus = "Implementing",
                    toStatus = "Completed"
                )

                showImplementationDialog = false
                successMessage = "Implementasi berhasil diselesaikan!"
                showSuccessDialog = true
            }
        )
    }
    if (showEmergencyDialog) {
        EmergencyActionDialog(
            changeRequest = changeRequest,
            teknisiId = teknisiId,
            teknisiName = teknisiName,
            onDismiss = { showEmergencyDialog = false },
            onSuccess = {
                showEmergencyDialog = false
                successMessage = "Status emergency change request berhasil diupdate"
                showSuccessDialog = true
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Berhasil!") },
            text = { Text(successMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
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
            title = { Text("Detail Pengajuan", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF384E66),
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
            // Emergency Badge (if emergency)
            if (isEmergency) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF5722)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚠️ EMERGENCY CHANGE REQUEST",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Ticket ID Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF384E66)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
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
            }

            // Detail Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Informasi Permohonan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    HorizontalDivider()

                    DetailItem(label = "Jenis Perubahan", value = changeRequest.jenisPerubahan)
                    DetailItem(label = "Alasan", value = changeRequest.alasan)
                    DetailItem(label = "Tujuan", value = changeRequest.tujuan)
                    DetailItem(label = "Aset Terdampak", value = changeRequest.asetTerdampak)
                    DetailItem(label = "Rencana Implementasi", value = changeRequest.rencanaImplementasi)
                    DetailItem(label = "Usulan Jadwal", value = changeRequest.usulanJadwal)
                    DetailItem(label = "Rencana Rollback", value = changeRequest.rencanaRollback)
                    if (changeRequest.assignedTeknisiName != null) {
                        DetailItem(label = "Teknisi Ditugaskan", value = changeRequest.assignedTeknisiName)
                    }
                }
            }

            // Inspection Result (if reviewed)
            if ((isReviewed || isScheduled || isImplementing || isCompleted)
                && changeRequest.estimasiBiaya != null && changeRequest.estimasiWaktu != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Hasil Inspeksi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Inspection",
                                tint = Color(0xFF4CAF50)
                            )
                        }

                        HorizontalDivider()

                        changeRequest.estimasiBiaya?.let {
                            DetailItem(label = "Estimasi Biaya", value = it)
                        }
                        changeRequest.estimasiWaktu?.let {
                            DetailItem(label = "Estimasi Waktu", value = it)
                        }
                    }
                }
            }

            // Schedule Info (if scheduled)
            if ((isScheduled || isImplementing || isCompleted) && changeRequest.scheduledDate != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Jadwal Implementasi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Schedule",
                                tint = Color(0xFFFF9800)
                            )
                        }

                        HorizontalDivider()

                        DetailItem(label = "Tanggal", value = changeRequest.scheduledDate ?: "-")

                    }
                }
            }
            // Implementation Result
            if (isCompleted && changeRequest.keteranganHasilImplementasi != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Hasil Implementasi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Result",
                                tint = Color(0xFF2196F3)
                            )
                        }

                        HorizontalDivider()

                        changeRequest.dampakSetelahMitigasi?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Dampak Setelah Mitigasi:", fontSize = 13.sp)
                                Text("$it - ${getImpactLabel(it)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        changeRequest.kemungkinanSetelahMitigasi?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kemungkinan Setelah Mitigasi:", fontSize = 13.sp)
                                Text("$it - ${getProbabilityLabel(it)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        changeRequest.exposur?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Exposur:", fontSize = 13.sp)
                                Text(it.toString(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        changeRequest.skorResidual?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Skor Residual:", fontSize = 13.sp)
                                Text(it.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        changeRequest.levelRisikoResidual?.let { level ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Level Risiko Residual:", fontSize = 13.sp)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = getRiskLevelColor(level)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = level,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        changeRequest.keteranganHasilImplementasi?.let {
                            DetailItem(label = "Keterangan Hasil", value = it)
                        }
                    }
                }
            }
            // Photo Card from Teknisi (if exists and reviewed/scheduled)
            changeRequest.photoPath?.let { path ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = "Photo",
                                tint = Color(0xFF384E66)
                            )
                            Text(
                                text = "Foto Bukti Inspeksi Lapangan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        HorizontalDivider()

                        val photoFile = File(path)
                        if (photoFile.exists()) {
                            val bitmap = remember(path) {
                                BitmapFactory.decodeFile(path)
                            }

                            bitmap?.let {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = { showFullImage = true }
                                ) {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "Foto Bukti",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Text(
                                    text = "Ketuk untuk memperbesar",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                                )
                            }
                        } else {
                            Text(
                                text = "Foto tidak ditemukan",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            // Status & Timeline Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Status & Waktu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status Saat Ini:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (changeRequest.status) {
                                    "Submitted" -> Color(0xFF9E9E9E)
                                    "Reviewed" -> Color(0xFF2196F3)
                                    "Approved" -> Color(0xFF4CAF50)
                                    "Scheduled" -> Color(0xFFFF9800)
                                    "Implementing" -> Color(0xFFFF5722)
                                    "Completed" -> Color(0xFF4CAF50)
                                    "Failed" -> Color(0xFFD32F2F)
                                    else -> Color.Gray
                                }
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = changeRequest.status,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    DetailItem(label = "Dibuat pada", value = createdDate)
                    DetailItem(label = "Diperbarui pada", value = updatedDate)
                }
            }

            // Risk Assessment Card (if exists)
            existingRiskAssessment?.let { risk ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = getRiskLevelColor(risk.levelRisiko).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Risk Assessment",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Risk",
                                tint = getRiskLevelColor(risk.levelRisiko)
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Skor Dampak:", fontSize = 13.sp)
                            Text(
                                "${risk.skorDampak} - ${getImpactLabel(risk.skorDampak)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Skor Kemungkinan:", fontSize = 13.sp)
                            Text(
                                "${risk.skorKemungkinan} - ${getProbabilityLabel(risk.skorKemungkinan)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Skor Risiko:", fontSize = 13.sp)
                            Text(
                                risk.skorRisiko.toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Level Risiko:", fontSize = 13.sp)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = getRiskLevelColor(risk.levelRisiko)
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = risk.levelRisiko,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "Dinilai oleh: ${risk.teknisiName}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Action Buttons
            when {
                isEmergency && !isCompleted && changeRequest.status != "Closed" -> {
                    Button(
                        onClick = { showEmergencyDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Inspection",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lakukan Inspeksi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                isSubmitted -> {
                    Button(
                        onClick = { showInspectionDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = "Submitted",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lakukan Inspeksi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                isReviewed -> {
                    Button(
                        onClick = { showSchedulingDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Schedule",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Jadwalkan Implementasi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                isScheduled -> {
                    Button(
                        onClick = {
                            // Update ke Implementing
                            changeRequestViewModel.updateChangeRequestStatus(
                                changeRequest = changeRequest,
                                newStatus = "Implementing"
                            )
                            approvalHistoryViewModel.addApprovalHistory(
                                changeRequestId = changeRequest.id,
                                approverUserId = teknisiId,
                                approverName = teknisiName,
                                fromStatus = "Scheduled",
                                toStatus = "Implementing",
                                notes = "Memulai implementasi"
                            )
                            // Kirim notifikasi ke end user
                            notificationViewModel.createNotification(
                                userId = changeRequest.userId,
                                changeRequestId = changeRequest.id,
                                ticketId = changeRequest.ticketId,
                                fromStatus = "Scheduled",
                                toStatus = "Implementing"
                            )
                            successMessage = "Status berubah menjadi 'Implementing'"
                            showSuccessDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Implementation",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mulai Implementasi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                isImplementing -> {
                    Button(
                        onClick = { showImplementationDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Complete",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Selesaikan Implementasi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                isCompleted -> {
                    Button(
                        onClick = {
                            // Update ke Closed
                            changeRequestViewModel.updateChangeRequestStatus(
                                changeRequest = changeRequest,
                                newStatus = "Closed"
                            )
                            approvalHistoryViewModel.addApprovalHistory(
                                changeRequestId = changeRequest.id,
                                approverUserId = teknisiId,
                                approverName = teknisiName,
                                fromStatus = "Completed",
                                toStatus = "Closed",
                                notes = "Change request ditutup"
                            )
                            // Kirim notifikasi ke end user
                            notificationViewModel.createNotification(
                                userId = changeRequest.userId,
                                changeRequestId = changeRequest.id,
                                ticketId = changeRequest.ticketId,
                                fromStatus = "Completed",
                                toStatus = "Closed"
                            )
                            successMessage = "Change request berhasil ditutup"
                            showSuccessDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF607D8B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Close",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tutup Change Request",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Full Image Dialog
    if (showFullImage && changeRequest.photoPath != null) {
        val photoFile = File(changeRequest.photoPath)
        if (photoFile.exists()) {
            val bitmap = remember(changeRequest.photoPath) {
                BitmapFactory.decodeFile(changeRequest.photoPath)
            }

            bitmap?.let {
                AlertDialog(
                    onDismissRequest = { showFullImage = false },
                    confirmButton = {
                        TextButton(onClick = { showFullImage = false }) {
                            Text("Tutup")
                        }
                    },
                    text = {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Foto Bukti Full",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                )
            }
        }
    }
}