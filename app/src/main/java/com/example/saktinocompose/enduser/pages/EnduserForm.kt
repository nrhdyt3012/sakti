package com.example.saktinocompose.enduser.pages

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnduserForm(
    userId: Int,
    userName: String,
    onFormSubmitted: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChangeRequestViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var jenisPerubahan by remember { mutableStateOf("") }
    var showJenisDropdown by remember { mutableStateOf(false) }
    var alasan by remember { mutableStateOf("") }
    var tujuan by remember { mutableStateOf("") }
    var asetTerdampak by remember { mutableStateOf("") }
    var showAsetDropdown by remember { mutableStateOf(false) }
    var usulanJadwal by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val jenisOptions = listOf("Standar", "Minor", "Major", "Emergency")
    val asetOptions = listOf(
        "Aset Perangkat Keras",
        "Aplikasi/Service",
        "OS/Build",
        "Jaringan (switch/router/AP)",
        "Database/Instance",
        "Sertifikat",
        "VM/Container",
        "Endpoint"
    )

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
            // Create temp file and launch camera
            val photoFile = createImageFile(context)
            tempPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(tempPhotoUri!!)
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            usulanJadwal = dateFormat.format(selectedCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }

    // Image Picker Dialog
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Pilih Sumber Foto") },
            text = { Text("Pilih dari mana Anda ingin mengambil foto bukti") },
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

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Berhasil!") },
            text = { Text("Permohonan perubahan telah berhasil disubmit") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        jenisPerubahan = ""
                        alasan = ""
                        tujuan = ""
                        asetTerdampak = ""
                        usulanJadwal = ""
                        photoUri = null
                        photoBitmap = null
                        onFormSubmitted()
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

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Gagal") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
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
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Form Permohonan Perubahan",
                    fontWeight = FontWeight.Bold
                )
            },
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
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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
                    // 1. Jenis Perubahan
                    Text("1. Jenis Perubahan *", fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = showJenisDropdown,
                        onExpandedChange = { showJenisDropdown = !showJenisDropdown }
                    ) {
                        OutlinedTextField(
                            value = jenisPerubahan,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Jenis") },
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedPlaceholderColor = Color.Black,
                                unfocusedPlaceholderColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedLabelColor = Color(0xFF384E66),
                                unfocusedLabelColor = Color(0xFF384E66)
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

                    // 2. Alasan
                    Text("2. Alasan *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = alasan,
                        onValueChange = { alasan = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Jelaskan alasan perubahan") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color.Black,
                            unfocusedPlaceholderColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedLabelColor = Color(0xFF384E66),
                            unfocusedLabelColor = Color(0xFF384E66)
                        )
                    )

                    // 3. Tujuan
                    Text("3. Tujuan *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = tujuan,
                        onValueChange = { tujuan = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Jelaskan tujuan perubahan") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color.Black,
                            unfocusedPlaceholderColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedLabelColor = Color(0xFF384E66),
                            unfocusedLabelColor = Color(0xFF384E66)
                        )
                    )

                    // 4. Aset Terdampak
                    Text("4. Aset Terdampak (CI) *", fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = showAsetDropdown,
                        onExpandedChange = { showAsetDropdown = !showAsetDropdown }
                    ) {
                        OutlinedTextField(
                            value = asetTerdampak,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Aset") },
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedPlaceholderColor = Color.Black,
                                unfocusedPlaceholderColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedLabelColor = Color(0xFF384E66),
                                unfocusedLabelColor = Color(0xFF384E66)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showAsetDropdown,
                            onDismissRequest = { showAsetDropdown = false }
                        ) {
                            asetOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        asetTerdampak = option
                                        showAsetDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // 5. Usulan Jadwal
                    Text("5. Usulan Jadwal *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = usulanJadwal,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        placeholder = { Text("Pilih tanggal") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Pilih Tanggal",
                                modifier = Modifier.clickable { datePickerDialog.show() }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color.Black,
                            unfocusedPlaceholderColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedLabelColor = Color(0xFF384E66),
                            unfocusedLabelColor = Color(0xFF384E66)
                        )
                    )

                    // 6. Foto Bukti (Opsional)
                    Text("6. Foto Bukti (Opsional)", fontWeight = FontWeight.SemiBold)

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

                    Text(
                        text = "Foto bukti dapat membantu mempercepat proses review",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            when {
                                jenisPerubahan.isBlank() -> {
                                    errorMessage = "Jenis Perubahan wajib diisi"
                                    showErrorDialog = true
                                }
                                alasan.isBlank() -> {
                                    errorMessage = "Alasan wajib diisi"
                                    showErrorDialog = true
                                }
                                tujuan.isBlank() -> {
                                    errorMessage = "Tujuan wajib diisi"
                                    showErrorDialog = true
                                }
                                asetTerdampak.isBlank() -> {
                                    errorMessage = "Aset Terdampak wajib diisi"
                                    showErrorDialog = true
                                }
                                usulanJadwal.isBlank() -> {
                                    errorMessage = "Usulan Jadwal wajib diisi"
                                    showErrorDialog = true
                                }
                                else -> {
                                    // Save photo to internal storage if exists
                                    val savedPhotoPath = photoUri?.let { uri ->
                                        savePhotoToInternalStorage(context, uri)
                                    }

                                    viewModel.submitChangeRequest(
                                        userId = userId,
                                        jenisPerubahan = jenisPerubahan,
                                        alasan = alasan,
                                        tujuan = tujuan,
                                        asetTerdampak = asetTerdampak,
                                        usulanJadwal = usulanJadwal,
                                        photoPath = savedPhotoPath
                                    )
                                    showSuccessDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF384E66)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Submit Permohonan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }

                    Text(
                        text = "* Field wajib diisi",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Helper function to create image file
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

// Helper function to load bitmap from URI
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

// Helper function to save photo to internal storage
private fun savePhotoToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "CR_${timeStamp}.jpg"
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