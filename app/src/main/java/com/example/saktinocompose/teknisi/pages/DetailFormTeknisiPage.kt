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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Image
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
import com.example.saktinocompose.viewmodel.RiskAssessmentViewModel
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
    viewModel: RiskAssessmentViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    val createdDate = dateFormat.format(Date(changeRequest.createdAt))
    val updatedDate = dateFormat.format(Date(changeRequest.updatedAt))

    var showRiskDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }

    val existingRiskAssessment by viewModel.getRiskAssessmentFlow(changeRequest.id)
        .collectAsState(initial = null)

    val isEmergency = changeRequest.jenisPerubahan == "Emergency"

    if (showRiskDialog) {
        RiskAssessmentDialog(
            onDismiss = { showRiskDialog = false },
            onSave = { skorDampak, skorKemungkinan, skorRisiko, levelRisiko ->
                viewModel.saveRiskAssessment(
                    changeRequestId = changeRequest.id,
                    teknisiId = teknisiId,
                    teknisiName = teknisiName,
                    skorDampak = skorDampak,
                    skorKemungkinan = skorKemungkinan,
                    skorRisiko = skorRisiko,
                    levelRisiko = levelRisiko
                )
                showRiskDialog = false
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
                showSuccessDialog = true
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Berhasil!") },
            text = {
                Text(
                    if (isEmergency)
                        "Status emergency change request berhasil diupdate"
                    else
                        "Risk Assessment berhasil disimpan"
                )
            },
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
                    DetailItem(label = "Usulan Jadwal", value = changeRequest.usulanJadwal)
                }
            }

            // Photo Card (if exists)
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
                                text = "Foto Bukti dari End User",
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
        }
    }
}