package com.example.saktinocompose.teknisi.pages

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
import com.example.saktinocompose.data.entity.ChangeRequest
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
                    text = "Inspeksi & Review",
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
                            text = "Lakukan inspeksi dan pilih tindakan: Setuju, Reject, atau Revisi",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // 1. Edit Jenis Perubahan
                Text(
                    text = "1. Jenis Perubahan (dapat diedit)",
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
                        label = { Text("Jenis Perubahan") },
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
                    text = "2. Estimasi Biaya *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                OutlinedTextField(
                    value = estimasiBiaya,
                    onValueChange = { estimasiBiaya = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Contoh: Rp 5.000.000") },
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
                    text = "3. Estimasi Waktu *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                OutlinedTextField(
                    value = estimasiWaktu,
                    onValueChange = { estimasiWaktu = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Contoh: 2 hari / 4 jam") },
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
                    text = "4. Skor Dampak (Impact) *",
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
                        label = { Text("Pilih Skor Dampak") },
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
                    text = "5. Skor Kemungkinan (Probability) *",
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
                        label = { Text("Pilih Skor Kemungkinan") },
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
                    text = "6. Skor Eksposur *",
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
                        label = { Text("Pilih Skor Eksposur (1-5)") },
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
                        (1..5).forEach { score ->
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
                                text = "Hasil Perhitungan Risk",
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

                // 7. Foto Bukti Lapangan
                Text(
                    text = "7. Foto Bukti Lapangan *",
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
                                contentDescription = "Preview Foto",
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
                                    contentDescription = "Hapus Foto",
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
                        Text("Ganti Foto")
                    }
                } else {
                    OutlinedButton(
                        onClick = { showImagePickerDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Foto Bukti")
                    }
                }

                // 8. Catatan
                Text(
                    text = "8. Catatan (untuk Reject/Revisi)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Jelaskan alasan reject atau revisi jika diperlukan...") },
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
                            val savedPhotoPath = photoUri?.let { uri ->
                                if (uri.toString().startsWith("file://")) {
                                    changeRequest.photoPath
                                } else {
                                    savePhotoToInternalStorage(context, uri)
                                }
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
                                savedPhotoPath,
                                ""
                            )
                        }
                    },
                    enabled = estimasiBiaya.isNotBlank() &&
                            estimasiWaktu.isNotBlank() &&
                            skorDampak > 0 &&
                            skorKemungkinan > 0 &&
                            skorEksposur > 0 &&
                            photoUri != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Setuju (Approve)")
                }

                // Tombol REVISI
                OutlinedButton(
                    onClick = {
                        if (notes.isNotBlank()) {
                            onSave(
                                InspectionAction.REVISE,
                                jenisPerubahan,
                                estimasiBiaya,
                                estimasiWaktu,
                                skorDampak,
                                skorKemungkinan,
                                skorEksposur,
                                skorRisiko,
                                levelRisiko,
                                null,
                                notes
                            )
                        }
                    },
                    enabled = notes.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF9800)
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Minta Revisi")
                }

                // Tombol REJECT
                OutlinedButton(
                    onClick = {
                        if (notes.isNotBlank()) {
                            onSave(
                                InspectionAction.REJECT,
                                jenisPerubahan,
                                estimasiBiaya,
                                estimasiWaktu,
                                skorDampak,
                                skorKemungkinan,
                                skorEksposur,
                                skorRisiko,
                                levelRisiko,
                                null,
                                notes
                            )
                        }
                    },
                    enabled = notes.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reject")
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