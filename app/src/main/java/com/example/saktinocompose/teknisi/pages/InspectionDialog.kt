package com.example.saktinocompose.teknisi.pages

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.example.saktinocompose.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.repository.InspectionRepository
import com.example.saktinocompose.network.Result
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

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

    // ✅ NEW: Loading & Error states
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

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            photoBitmap = loadBitmapFromUri(context, it)
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let {
                photoUri = it
                photoBitmap = loadBitmapFromUri(context, it)
            }
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = createImageFile(context)
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
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Ticket: ${changeRequest.ticketId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF384E66)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text =  stringResource(R.string.perform_inspection),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

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
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Black) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
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
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color.Black)
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
                        value = if (skorDampak == 0) "" else "$skorDampak - ${getImpactLabel(skorDampak)}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Impact score") },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Black) },
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
                        value = if (skorKemungkinan == 0) "" else "$skorKemungkinan - ${getProbabilityLabel(skorKemungkinan)}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Probability Score") },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Black) },
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

                // 6. Skor Eksposur (BARU)
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
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Black) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
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

                // Risk Result (Auto calculated)
                if (skorDampak > 0 && skorKemungkinan > 0 && skorEksposur > 0) {
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
                                text = stringResource(R.string.risk_calculation_result),
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
                                Text("Risk Level:", fontSize = 13.sp)
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

                // 7. Foto Bukti Lapangan
                Text(
                    text = stringResource(R.string.field_evidence_photo) + " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                if (photoBitmap != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = photoBitmap!!.asImageBitmap(),
                                contentDescription = "Preview Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(
                                onClick = {
                                    photoUri = null
                                    photoBitmap = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Red.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete Photo",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showImagePickerDialog = true },
                        modifier = Modifier.fillMaxWidth()
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
            // 3 Tombol Aksi
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tombol SETUJU (Approve)
                Button(
                    onClick = {
                        if (estimasiBiaya.isNotBlank() &&
                            estimasiWaktu.isNotBlank() &&
                            skorDampak > 0 &&
                            skorKemungkinan > 0 &&
                            skorEksposur > 0 &&
                            photoUri != null
                        ) {
                            isSubmitting = true

                            scope.launch {
                                try {
                                    // ✅ Parse estimasi ke Double
                                    val biayaDouble = estimasiBiaya.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
                                    val waktuDouble = estimasiWaktu.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

                                    // ✅ Submit ke server
                                    val result = inspectionRepository.submitInspection(
                                        crId = changeRequest.id,
                                        jenisPerubahan = jenisPerubahan,
                                        alasan = changeRequest.description,
                                        tujuan = changeRequest.title,
                                        ciId = changeRequest.relasiConfigurationItem,
                                        asetTerdampakId = changeRequest.asetTerdampak,
                                        rencanaImplementasi = changeRequest.rencanaImplementasi,
                                        usulanJadwal = changeRequest.usulanJadwal,
                                        rencanaRollback = changeRequest.rollbackPlan,
                                        estimasiBiaya = biayaDouble,
                                        estimasiWaktu = waktuDouble,
                                        skorDampak = skorDampak,
                                        skorKemungkinan = skorKemungkinan,
                                        skorExposure = skorEksposur
                                    )

                                    when (result) {
                                        is Result.Success -> {
                                            // ✅ Save photo locally
                                            val savedPhotoPath = photoUri?.let { uri ->
                                                if (uri.toString().startsWith("file://")) {
                                                    changeRequest.photoPath
                                                } else {
                                                    savePhotoToInternalStorage(context, uri)
                                                }
                                            }

                                            // ✅ Call onSave untuk update UI
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
                                                savedPhotoPath,
                                                ""
                                            )
                                        }
                                        is Result.Error -> {
                                            errorMessage = result.message ?: "Failed to submit inspection"
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
                                }
                            }
                        }
                    },
                    enabled = estimasiBiaya.isNotBlank() &&
                            estimasiWaktu.isNotBlank() &&
                            skorDampak > 0 &&
                            skorKemungkinan > 0 &&
                            skorEksposur > 0 &&
                            photoUri != null &&
                            !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
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
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp)
    )
}

// Helper functions tetap sama
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    return File.createTempFile(
        "INSPECTION_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun savePhotoToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "INSPECTION_${timeStamp}.jpg"
        val file = File(context.filesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}