// File: app/src/main/java/com/example/saktinocompose/teknisi/pages/InspectionDialog.kt
// ✅ FIXED VERSION - Parsing impacted assets dan CI ID dengan benar

package com.example.saktinocompose.teknisi.pages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.saktinocompose.R
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.repository.InspectionPhotoRepository
import com.example.saktinocompose.repository.InspectionRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.saktinocompose.utils.PhotoHelper


enum class InspectionAction {
    APPROVE, REJECT, REVISE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionDialog(
    changeRequest: ChangeRequest,
    onDismiss: () -> Unit,
    onSave: (
        action: InspectionAction,
        jenisPerubahan: String,
        estimasiBiaya: String,
        estimasiWaktu: String,
        skorDampak: Int,
        skorKemungkinan: Int,
        skorEksposur: Int,
        skorRisiko: Int,
        levelRisiko: String,
        photoPath: String?,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val inspectionRepository = remember { InspectionRepository() }
    val photoRepository = remember { InspectionPhotoRepository() }

    // Form states
    var jenisPerubahan by remember { mutableStateOf(changeRequest.jenisPerubahan) }
    var showJenisDropdown by remember { mutableStateOf(false) }
    var estimasiBiaya by remember { mutableStateOf(changeRequest.estimasiBiaya ?: "") }
    var estimasiWaktu by remember { mutableStateOf(changeRequest.estimasiWaktu ?: "") }
    var skorDampak by remember { mutableIntStateOf(0) }
    var skorKemungkinan by remember { mutableIntStateOf(0) }
    var skorEksposur by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var showDampakDropdown by remember { mutableStateOf(false) }
    var showKemungkinanDropdown by remember { mutableStateOf(false) }
    var showEksposurDropdown by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var uploadedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Load existing photo if available
    LaunchedEffect(changeRequest.photoPath) {
        changeRequest.photoPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                photoBitmap = BitmapFactory.decodeFile(path)
                photoUri = Uri.fromFile(file)
            }
        }
    }

    val skorRisiko = skorDampak * skorKemungkinan * skorEksposur
    val levelRisiko = calculateRiskLevel(skorDampak, skorKemungkinan)
    val levelColor = getRiskLevelColor(levelRisiko)

    val jenisOptions = listOf("Standar", "Minor", "Major", "Emergency")

    // ✅ FIXED: Helper function untuk parse impacted assets dengan benar
    fun parseImpactedAssets(asetTerdampak: String): List<String> {
        if (asetTerdampak.isBlank()) return emptyList()

        return try {
            // Format bisa:
            // 1. "kode_bmd" - single asset
            // 2. "kode_bmd1,kode_bmd2,kode_bmd3" - multiple assets
            // 3. "kode_bmd:nama:tipe" - dengan detail (ambil kode_bmd saja)
            // 4. "kode_bmd1:nama1:tipe1,kode_bmd2:nama2:tipe2" - multiple dengan detail

            asetTerdampak.split(",").mapNotNull { item ->
                val trimmed = item.trim()
                if (trimmed.contains(":")) {
                    // Format "kode_bmd:nama:tipe" -> ambil kode_bmd saja
                    trimmed.split(":").firstOrNull()?.trim()
                } else {
                    // Format "kode_bmd" langsung
                    trimmed.takeIf { it.isNotBlank() }
                }
            }.distinct() // Remove duplicates
        } catch (e: Exception) {
            Log.e("InspectionDialog", "❌ Error parsing impacted assets: ${e.message}")
            emptyList()
        }
    }

    // ✅ FIXED: Helper function untuk parse CI ID dengan benar
    fun parseCIId(relasiCI: String): String {
        if (relasiCI.isBlank()) return ""

        return try {
            // Format bisa:
            // 1. "kode_bmd" - single CI
            // 2. "kode_bmd1,kode_bmd2" - multiple CIs (ambil yang pertama)
            // 3. "kode_bmd:nama:tipe" - dengan detail (ambil kode_bmd)
            // 4. "kode_bmd1:nama1:tipe1,kode_bmd2:nama2:tipe2" - multiple dengan detail

            val firstCI = relasiCI.split(",").firstOrNull()?.trim() ?: ""

            if (firstCI.contains(":")) {
                // Format "kode_bmd:nama:tipe" -> ambil kode_bmd saja
                firstCI.split(":").firstOrNull()?.trim() ?: ""
            } else {
                // Format "kode_bmd" langsung
                firstCI
            }
        } catch (e: Exception) {
            Log.e("InspectionDialog", "❌ Error parsing CI ID: ${e.message}")
            ""
        }
    }

    // ✅ FIXED: Helper function untuk format usulan jadwal
    fun formatUsulanJadwal(jadwal: String): String {
        if (jadwal.isBlank()) return ""

        return try {
            // Input bisa dalam format:
            // 1. "2025-12-20" -> sudah benar
            // 2. "20/12/2025" -> perlu convert
            // 3. "2025-12-20T07:26:24.580Z" -> perlu extract date saja

            when {
                jadwal.contains("T") -> {
                    // ISO 8601 format -> ambil date saja
                    jadwal.split("T").firstOrNull() ?: jadwal
                }

                jadwal.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                    // Already in correct format
                    jadwal
                }

                jadwal.contains("/") -> {
                    // Format dd/MM/yyyy -> convert to yyyy-MM-dd
                    val parts = jadwal.split("/")
                    if (parts.size == 3) {
                        "${parts[2]}-${parts[1]}-${parts[0]}"
                    } else {
                        jadwal
                    }
                }

                else -> jadwal
            }
        } catch (e: Exception) {
            Log.e("InspectionDialog", "❌ Error formatting usulan jadwal: ${e.message}")
            jadwal
        }
    }

    fun uploadPhoto(uri: Uri) {
        isUploadingPhoto = true
        uploadError = null

        scope.launch {
            try {
                when (val result = photoRepository.uploadInspectionPhoto(
                    crId = changeRequest.id,
                    photoUri = uri,
                    context = context
                )) {
                    is Result.Success -> {
                        uploadedPhotoUrl = result.data
                        isUploadingPhoto = false
                        Log.d("InspectionDialog", "✅ Photo uploaded: ${result.data}")
                    }

                    is Result.Error -> {
                        uploadError = result.message ?: "Failed to upload photo"
                        isUploadingPhoto = false
                        Log.e("InspectionDialog", "❌ Upload failed: ${result.message}")
                    }

                    else -> {
                        uploadError = "Unknown error"
                        isUploadingPhoto = false
                    }
                }
            } catch (e: Exception) {
                uploadError = "Error: ${e.message}"
                isUploadingPhoto = false
                Log.e("InspectionDialog", "❌ Exception", e)
            }
        }
    }

    // Gallery Launcher
    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            photoBitmap = PhotoHelper.loadBitmapFromUri(context, it)  // ✅ Gunakan PhotoHelper
            uploadPhoto(it)
        }
    }

// Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let {
                photoUri = it
                photoBitmap = PhotoHelper.loadBitmapFromUri(context, it)  // ✅ Gunakan PhotoHelper
                uploadPhoto(it)
            }
        }
    }

// Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = PhotoHelper.createImageFile(context, "INSPECTION")  // ✅ Gunakan PhotoHelper
            tempPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(tempPhotoUri!!)
        }
    }

    // Image Picker Dialog
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Pilih Sumber Foto") },
            text = { Text("Pilih dari mana Anda ingin mengambil foto bukti lapangan") },
            confirmButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }) {
                    Icon(Icons.Default.Camera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kamera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galeri")
                }
            }
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(
                    Icons.Default.Error,
                    "Error",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Error", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(errorMessage)

                    // ✅ TAMBAH: Debug info
                    if (errorMessage.contains("ci_id") || errorMessage.contains("400")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Debug Info:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            "Aset Terdampak: ${changeRequest.asetTerdampak}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            "CI Relasi: ${changeRequest.relasiConfigurationItem}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Usulan Jadwal: ${changeRequest.usulanJadwal}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("OK")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = "Inspection",
                    tint = Color(0xFF384E66)
                )
                Text(
                    text = stringResource(R.string.inspection_review),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 550.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF384E66).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Ticket: ${changeRequest.ticketId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF384E66)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.perform_inspection),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // [REST OF THE UI CODE REMAINS THE SAME...]
                // 1. Edit Jenis Perubahan
                Text(
                    text = "1. Change Type ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                ExposedDropdownMenuBox(
                    expanded = showJenisDropdown,
                    onExpandedChange = { showJenisDropdown = !showJenisDropdown }
                ) {
                    OutlinedTextField(
                        value = jenisPerubahan,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Change Type") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                null,
                                tint = Color.Black
                            )
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showJenisDropdown,
                        onDismissRequest = { showJenisDropdown = false }
                    ) {
                        jenisOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    jenisPerubahan = option
                                    showJenisDropdown = false
                                }
                            )
                        }
                    }
                }

                // 2. Estimasi Biaya
                Text(
                    text = stringResource(R.string.cost_estimate) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                OutlinedTextField(
                    value = estimasiBiaya,
                    onValueChange = { estimasiBiaya = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Example: Rp 5.000.000") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 3. Estimasi Waktu
                Text(
                    text = stringResource(R.string.time_estimate) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                OutlinedTextField(
                    value = estimasiWaktu,
                    onValueChange = { estimasiWaktu = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Example: 2 hari / 4 jam") },
                    leadingIcon = {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Black)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 4. Skor Dampak
                Text(
                    text = stringResource(R.string.impact_score) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                ExposedDropdownMenuBox(
                    expanded = showDampakDropdown,
                    onExpandedChange = { showDampakDropdown = !showDampakDropdown }
                ) {
                    OutlinedTextField(
                        value = if (skorDampak == 0) "" else "$skorDampak - ${
                            getImpactLabel(
                                skorDampak
                            )
                        }",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Impact score") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                null,
                                tint = Color.Black
                            )
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
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

                // 5. Skor Kemungkinan
                Text(
                    text = stringResource(R.string.probability_score) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                ExposedDropdownMenuBox(
                    expanded = showKemungkinanDropdown,
                    onExpandedChange = { showKemungkinanDropdown = !showKemungkinanDropdown }
                ) {
                    OutlinedTextField(
                        value = if (skorKemungkinan == 0) "" else "$skorKemungkinan - ${
                            getProbabilityLabel(
                                skorKemungkinan
                            )
                        }",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Probability Score") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                null,
                                tint = Color.Black
                            )
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
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

                // 6. Skor Eksposur
                Text(
                    text = stringResource(R.string.exposure_score) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                ExposedDropdownMenuBox(
                    expanded = showEksposurDropdown,
                    onExpandedChange = { showEksposurDropdown = !showEksposurDropdown }
                ) {
                    OutlinedTextField(
                        value = if (skorEksposur == 0) "" else skorEksposur.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose exposure score (1-4)") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                null,
                                tint = Color.Black
                            )
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showEksposurDropdown,
                        onDismissRequest = { showEksposurDropdown = false }
                    ) {
                        (1..4).forEach { score ->
                            DropdownMenuItem(
                                text = { Text(score.toString()) },
                                onClick = {
                                    skorEksposur = score
                                    showEksposurDropdown = false
                                }
                            )
                        }
                    }
                }

                // Risk Result
                if (skorDampak > 0 && skorKemungkinan > 0 && skorEksposur > 0) {
                    HorizontalDivider()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.risk_calculation_result),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Skor Risiko:", fontSize = 13.sp)
                                Text("$skorRisiko", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Risk Level:", fontSize = 13.sp)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = levelColor),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = levelRisiko,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Foto Bukti Lapangan
                Text(
                    text = stringResource(R.string.field_evidence_photo) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                if (photoBitmap != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = photoBitmap!!.asImageBitmap(),
                                contentDescription = "Preview Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (isUploadingPhoto) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 3.dp
                                        )
                                        Text(
                                            "Uploading photo...",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            if (uploadedPhotoUrl != null && !isUploadingPhoto) {
                                Card(
                                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFF4CAF50
                                        )
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            "Uploaded",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Uploaded",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    photoUri = null
                                    photoBitmap = null
                                    uploadedPhotoUrl = null
                                    uploadError = null
                                },
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                    .background(Color.Red.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, "Delete Photo", tint = Color.White)
                            }
                        }
                    }


                    if (uploadError != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFD32F2F).copy(
                                    alpha = 0.1f
                                )
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Upload Failed",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                    Text(
                                        uploadError!!,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        photoUri?.let { uploadPhoto(it) }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showImagePickerDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUploadingPhoto
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Photo")
                    }
                } else {
                    OutlinedButton(
                        onClick = { showImagePickerDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add evidence photo")
                    }
                }

                // 8. Catatan
                Text(
                    text = stringResource(R.string.approve),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Explain the reason for rejection or revision if necessary...") },
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
                    if (estimasiBiaya.isNotBlank() &&
                        estimasiWaktu.isNotBlank() &&
                        skorDampak > 0 &&
                        skorKemungkinan > 0 &&
                        skorEksposur > 0 &&
                        uploadedPhotoUrl != null
                    ) {
                        isSubmitting = true

                        scope.launch {
                            try {
                                // ✅ FIXED: Parse impacted assets dengan benar
                                val impactedAssets =
                                    parseImpactedAssets(changeRequest.asetTerdampak)

                                // ✅ FIXED: Parse CI ID dengan benar
                                val ciId = parseCIId(changeRequest.relasiConfigurationItem)

                                // ✅ FIXED: Format usulan jadwal dengan benar
                                val usulanJadwal = formatUsulanJadwal(changeRequest.usulanJadwal)

                                // ✅ VALIDASI: Pastikan semua data ada
                                when {
                                    impactedAssets.isEmpty() -> {
                                        errorMessage =
                                            "Impacted assets tidak valid. Data: '${changeRequest.asetTerdampak}'"
                                        showErrorDialog = true
                                        isSubmitting = false
                                        return@launch
                                    }

                                    ciId.isBlank() -> {
                                        errorMessage =
                                            "CI ID tidak valid. Data: '${changeRequest.relasiConfigurationItem}'"
                                        showErrorDialog = true
                                        isSubmitting = false
                                        return@launch
                                    }

                                    usulanJadwal.isBlank() -> {
                                        errorMessage =
                                            "Usulan jadwal tidak valid. Data: '${changeRequest.usulanJadwal}'"
                                        showErrorDialog = true
                                        isSubmitting = false
                                        return@launch
                                    }
                                }

                                Log.d(
                                    "InspectionDialog", """
                            📤 Submitting inspection:
                            - CR ID: ${changeRequest.id}
                            - Jenis: $jenisPerubahan
                            - Impacted Assets (parsed): $impactedAssets
                            - Impacted Assets (raw): ${changeRequest.asetTerdampak}
                            - CI ID (parsed): $ciId
                            - CI ID (raw): ${changeRequest.relasiConfigurationItem}
                            - Usulan Jadwal (parsed): $usulanJadwal
                            - Usulan Jadwal (raw): ${changeRequest.usulanJadwal}
                            - Description: ${changeRequest.description}
                            - Title: ${changeRequest.title}
                            - Rencana Impl: ${changeRequest.rencanaImplementasi}
                            - Rollback: ${changeRequest.rollbackPlan}
                        """.trimIndent()
                                )

                                // Submit inspection
                                val result = inspectionRepository.submitInspection(
                                    crId = changeRequest.id,
                                    jenisPerubahan = jenisPerubahan,
                                    alasan = changeRequest.description,
                                    tujuan = changeRequest.title,
                                    ciId = ciId,  // ✅ FIXED
                                    impactedAssets = impactedAssets,  // ✅ FIXED
                                    rencanaImplementasi = changeRequest.rencanaImplementasi,
                                    usulanJadwal = usulanJadwal,  // ✅ FIXED
                                    rencanaRollback = changeRequest.rollbackPlan,
                                    skorDampak = skorDampak,
                                    skorKemungkinan = skorKemungkinan,
                                    skorExposure = skorEksposur
                                )

                                when (result) {
                                    is Result.Success -> {
                                        // Save local photo path
                                        val savedPhotoPath = photoUri?.let { uri ->
                                            if (uri.toString().startsWith("file://")) {
                                                changeRequest.photoPath
                                            } else {
                                                PhotoHelper.savePhotoToInternalStorage(  // ✅ Gunakan PhotoHelper
                                                    context,
                                                    uri,
                                                    "INSPECTION" )                                           }

                                        }

                                        onSave(
                                            InspectionAction.APPROVE,
                                            jenisPerubahan,
                                            estimasiBiaya,
                                            estimasiWaktu,
                                            skorDampak,
                                            skorKemungkinan,
                                            skorEksposur,
                                            skorRisiko,
                                            levelRisiko,
                                            uploadedPhotoUrl,
                                            ""
                                        )
                                    }

                                    is Result.Error -> {
                                        errorMessage =
                                            result.message ?: "Failed to submit inspection"
                                        showErrorDialog = true
                                        isSubmitting = false
                                    }

                                    else -> {
                                        errorMessage = "Unknown error occurred"
                                        showErrorDialog = true
                                        isSubmitting = false
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.message}"
                                showErrorDialog = true
                                isSubmitting = false
                                Log.e("InspectionDialog", "❌ Exception", e)
                            }
                        }
                    }
                },
                enabled = estimasiBiaya.isNotBlank() &&
                        estimasiWaktu.isNotBlank() &&
                        skorDampak > 0 &&
                        skorKemungkinan > 0 &&
                        skorEksposur > 0 &&
                        uploadedPhotoUrl != null &&
                        !isSubmitting &&
                        !isUploadingPhoto,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting...")
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit")
                }
            }
        }
    )
}

private fun parseImpactedAssets(asetTerdampak: String): List<String> {
    if (asetTerdampak.isBlank()) return emptyList()

    return try {
        asetTerdampak.split(",").mapNotNull { item ->
            val trimmed = item.trim()
            if (trimmed.contains(":")) {
                // Format "kode_bmd:nama:tipe" -> ambil kode_bmd
                trimmed.split(":").firstOrNull()?.trim()
            } else {
                // Format "kode_bmd" langsung
                trimmed.takeIf { it.isNotBlank() }
            }
        }.distinct()
    } catch (e: Exception) {
        Log.e("InspectionDialog", "Error parsing impacted assets: ${e.message}")
        emptyList()
    }
}

/**
 * ✅ Parse CI ID dari berbagai format
 */
private fun parseCIId(relasiCI: String): String {
    if (relasiCI.isBlank()) return ""

    return try {
        val firstCI = relasiCI.split(",").firstOrNull()?.trim() ?: ""

        if (firstCI.contains(":")) {
            // Format "kode_bmd:nama:tipe" -> ambil kode_bmd
            firstCI.split(":").firstOrNull()?.trim() ?: ""
        } else {
            // Format "kode_bmd" langsung
            firstCI
        }
    } catch (e: Exception) {
        Log.e("InspectionDialog", "Error parsing CI ID: ${e.message}")
        ""
    }
}

/**
 * ✅ Format usulan jadwal ke format yyyy-MM-dd
 */
private fun formatUsulanJadwal(jadwal: String): String {
    if (jadwal.isBlank()) return ""

    return try {
        when {
            jadwal.contains("T") -> {
                // ISO 8601 -> extract date only
                jadwal.split("T").firstOrNull() ?: jadwal
            }
            jadwal.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                // Already correct format
                jadwal
            }
            jadwal.contains("/") -> {
                // dd/MM/yyyy -> convert to yyyy-MM-dd
                val parts = jadwal.split("/")
                if (parts.size == 3) {
                    "${parts[2]}-${parts[1]}-${parts[0]}"
                } else {
                    jadwal
                }
            }
            else -> jadwal
        }
    } catch (e: Exception) {
        Log.e("InspectionDialog", "Error formatting usulan jadwal: ${e.message}")
        jadwal
    }
}



