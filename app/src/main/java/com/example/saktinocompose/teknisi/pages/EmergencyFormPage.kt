// File: app/src/main/java/com/example/saktinocompose/teknisi/pages/EmergencyFormPage.kt
// ✅ FIXED VERSION - State Management & 3-Column Asset Table

package com.example.saktinocompose.teknisi.pages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.repository.EmergencyPhotoRepository
import com.example.saktinocompose.repository.EmergencyRepository
import com.example.saktinocompose.utils.NetworkHelper
import com.example.saktinocompose.utils.PhotoHelper
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ✅ NEW: Data class untuk simpan state asset
@androidx.compose.runtime.Stable
data class SelectedAssetState(
    val id: String,
    val kodeBmd: String,
    val nama: String
) {
    companion object {
        fun fromString(str: String): SelectedAssetState? {
            if (str.isBlank()) return null
            val parts = str.split("|")
            return if (parts.size >= 3) {
                SelectedAssetState(
                    id = parts[0],
                    kodeBmd = parts[1],
                    nama = parts[2]
                )
            } else null
        }
    }

    override fun toString(): String = "$id|$kodeBmd|$nama"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyFormPage(
    userId: String,
    userName: String,
    onFormSubmitted: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val emergencyRepository = remember { EmergencyRepository() }
    val photoRepository = remember { EmergencyPhotoRepository() }
    val scrollState = rememberScrollState()

    // Internet Check
    var isOnline by remember { mutableStateOf(NetworkHelper.isInternetAvailable(context)) }
    var showOfflineDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            isOnline = NetworkHelper.isInternetAvailable(context)
            kotlinx.coroutines.delay(3000)
        }
    }

    // ✅ FIXED: Use rememberSaveable untuk persist state saat navigasi
    val emergencyId = rememberSaveable { UUID.randomUUID().toString() }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    // ✅ FIXED: Simpan asset sebagai string serialized
    var selectedAssetString by rememberSaveable { mutableStateOf("") }
    val selectedAsset = remember(selectedAssetString) {
        SelectedAssetState.fromString(selectedAssetString)
    }

    var selectedStatus by rememberSaveable { mutableStateOf("COMPLETED") }
    var note by rememberSaveable { mutableStateOf("") }

    // Photo States (tidak bisa pakai rememberSaveable untuk URI/Bitmap)
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // Dialog States
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var showAsetSearchDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showBackConfirmDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // ✅ NEW: Check if form has data
    val hasFormData = remember(title, description, selectedAssetString, note, photoUri) {
        title.isNotBlank() ||
                description.isNotBlank() ||
                selectedAssetString.isNotBlank() ||
                note.isNotBlank() ||
                photoUri != null
    }

    // ✅ NEW: Function to clear all form data
    fun clearFormData() {
        title = ""
        description = ""
        selectedAssetString = ""
        selectedStatus = "COMPLETED"
        note = ""
        photoUri = null
        photoBitmap = null
        uploadedPhotoUrl = null
        uploadError = null
    }

    // ✅ FIXED: Back handler dengan konfirmasi jika ada data
    BackHandler {
        if (hasFormData && !isSubmitting) {
            showBackConfirmDialog = true
        } else {
            onBackClick()
        }
    }

    // ✅ NEW: Back Confirmation Dialog
    if (showBackConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBackConfirmDialog = false },
            icon = {
                Icon(Icons.Default.Warning, "Warning", tint = Color(0xFFFF9800), modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Discard Changes?", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
            },
            text = {
                Text("You have unsaved changes. Are you sure you want to leave? All data will be lost.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBackConfirmDialog = false
                        clearFormData()
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Yes, Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    fun uploadPhoto(uri: Uri) {
        isUploadingPhoto = true
        uploadError = null

        scope.launch {
            try {
                when (val result = photoRepository.uploadEmergencyPhoto(
                    photoUri = uri,
                    context = context
                )) {
                    is Result.Success -> {
                        uploadedPhotoUrl = result.data.url
                        isUploadingPhoto = false
                    }
                    is Result.Error -> {
                        uploadError = result.message ?: "Failed to upload photo"
                        isUploadingPhoto = false
                    }
                    else -> {
                        uploadError = "Unknown error"
                        isUploadingPhoto = false
                    }
                }
            } catch (e: Exception) {
                uploadError = "Error: ${e.message}"
                isUploadingPhoto = false
            }
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            photoBitmap = PhotoHelper.loadBitmapFromUri(context, it)
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
                photoBitmap = PhotoHelper.loadBitmapFromUri(context, it)
                uploadPhoto(it)
            }
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = PhotoHelper.createImageFile(context, "EMERGENCY")
            tempPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(tempPhotoUri!!)
        }
    }

    // Dialogs
    if (showOfflineDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineDialog = false },
            icon = {
                Icon(Icons.Default.CloudOff, "Offline", tint = Color(0xFFD32F2F), modifier = Modifier.size(48.dp))
            },
            title = { Text("No Internet Connection", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This feature requires an internet connection.")
                    Text("Please check your connection and try again.", fontSize = 13.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOfflineDialog = false
                        isOnline = NetworkHelper.isInternetAvailable(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfflineDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ✅ FIXED: Success Dialog dengan auto-clear form
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                clearFormData()
                onFormSubmitted()
            },
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, "Success", tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                    Text("Success!")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Emergency has been successfully submitted")
                    Text("Form will be cleared automatically.", fontSize = 13.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        clearFormData()
                        onFormSubmitted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
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

    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Select Photo Source") },
            text = { Text("Choose where to get the photo") },
            confirmButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }) {
                    Icon(Icons.Default.Camera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }
        )
    }

    // ✅ FIXED: Asset Search Dialog dengan 3-column data
    if (showAsetSearchDialog) {
        CmdbAsetSearchDialog(
            selectedKodeBmd = selectedAsset?.kodeBmd ?: "",
            onDismiss = { showAsetSearchDialog = false },
            onSelect = { id, namaAsset, kodeBmd ->
                // ✅ Save all 3 fields
                selectedAssetString = SelectedAssetState(
                    id = id,
                    kodeBmd = kodeBmd ?: id,
                    nama = namaAsset
                ).toString()
                showAsetSearchDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Emergency Form", fontWeight = FontWeight.Bold, color = Color.White)

                    // ✅ NEW: Indicator jika ada unsaved changes
                    if (hasFormData) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, "Unsaved", modifier = Modifier.size(14.dp), tint = Color(0xFF000000))
                                Text("Unsaved", fontSize = 11.sp, color = Color(0xFF000000), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!isOnline) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudOff, "Offline", modifier = Modifier.size(14.dp), tint = Color.White)
                                Text("Offline", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (hasFormData && !isSubmitting) {
                        showBackConfirmDialog = true
                    } else {
                        onBackClick()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF5722))
        )

        // Offline Warning
        if (!isOnline) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, "Warning", tint = Color(0xFFFF9800), modifier = Modifier.size(32.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("You are currently offline", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Please connect to the internet to submit", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Emergency Badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5722).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, "Emergency", tint = Color(0xFFFF5722))
                            Column {
                                Text("Emergency", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5722))
                                Text("Fill all required fields marked with *", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Emergency ID
                    Text("Emergency ID (Auto-generated)", fontWeight = FontWeight.SemiBold)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Fingerprint, "ID", tint = Color(0xFF2196F3))
                            Column {
                                Text("Unique ID:", fontSize = 11.sp, color = Color.Gray)
                                Text(emergencyId, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
                            }
                        }
                    }

                    // Title
                    Text("Title *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        placeholder = { Text("Emergency title...") }
                    )

                    // Description
                    Text("Description *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Describe the emergency...") }
                    )

                    // ✅ FIXED: Impacted Asset dengan 3 kolom
                    Text("Impacted Asset *", fontWeight = FontWeight.SemiBold)
                    if (selectedAsset != null) {
                        ThreeColumnAssetTable(asset = selectedAsset)
                    }

                    OutlinedButton(
                        onClick = { showAsetSearchDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedAsset == null) "Search Asset" else "Change Asset")
                    }

                    // Status Dropdown
                    Text("Status *", fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = showStatusDropdown,
                        onExpandedChange = { showStatusDropdown = !showStatusDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Status") },
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("COMPLETED") },
                                onClick = {
                                    selectedStatus = "COMPLETED"
                                    showStatusDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("FAILED") },
                                onClick = {
                                    selectedStatus = "FAILED"
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }

                    // Note
                    Text("Note *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Add notes about the emergency...") }
                    )

                    // Photo Section (unchanged from your original code)
                    Text("Evidence Photo (Optional)", fontWeight = FontWeight.SemiBold)
                    if (photoBitmap != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    bitmap = photoBitmap!!.asImageBitmap(),
                                    contentDescription = "Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                if (isUploadingPhoto) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                                            Text("Uploading...", color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }

                                if (uploadedPhotoUrl != null && !isUploadingPhoto) {
                                    Card(
                                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, "Uploaded", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Text("Uploaded", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                    Icon(Icons.Default.Close, "Delete", tint = Color.White)
                                }
                            }
                        }

                        if (uploadError != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Error, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Upload Failed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                        Text(uploadError!!, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = { photoUri?.let { uploadPhoto(it) } }) {
                                        Icon(Icons.Default.Refresh, "Retry", tint = Color(0xFFD32F2F))
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
                            Icon(Icons.Default.Edit, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Photo")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showImagePickerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddAPhoto, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add evidence photo")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    when {
                        title.isBlank() -> {
                            errorMessage = "Title is required"
                            showErrorDialog = true
                        }
                        description.isBlank() -> {
                            errorMessage = "Description is required"
                            showErrorDialog = true
                        }
                        selectedAsset == null -> {
                            errorMessage = "Impacted asset is required"
                            showErrorDialog = true
                        }
                        note.isBlank() -> {
                            errorMessage = "Note is required"
                            showErrorDialog = true
                        }
                        !isOnline -> {
                            showOfflineDialog = true
                        }
                        isUploadingPhoto -> {
                            errorMessage = "Please wait for photo upload to complete"
                            showErrorDialog = true
                        }
                        else -> {
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val result = emergencyRepository.createEmergency(
                                        id = emergencyId,
                                        title = title,
                                        description = description,
                                        impactedAssets = listOf(selectedAsset!!.id),
                                        status = selectedStatus,
                                        note = note
                                    )

                                    when (result) {
                                        is Result.Success -> {
                                            isSubmitting = false
                                            showSuccessDialog = true
                                        }
                                        is Result.Error -> {
                                            isSubmitting = false
                                            errorMessage = result.message ?: "Failed to submit"
                                            showErrorDialog = true
                                        }
                                        else -> {
                                            isSubmitting = false
                                            errorMessage = "Unknown error"
                                            showErrorDialog = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    isSubmitting = false
                                    errorMessage = "Error: ${e.message}"
                                    showErrorDialog = true
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOnline && !isSubmitting && !isUploadingPhoto) Color(0xFFFF5722) else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = isOnline && !isSubmitting && !isUploadingPhoto
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                } else if (isUploadingPhoto) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading Photo...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    if (!isOnline) {
                        Icon(Icons.Default.CloudOff, "Offline", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (isOnline) "Submit Emergency" else "Offline - Cannot Submit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text(
                "* Required fields | Photo is optional",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ✅ NEW: 3-Column Asset Table Component
@Composable
private fun ThreeColumnAssetTable(
    asset: SelectedAssetState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF384E66).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            Text(
                "Selected Asset:",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(12.dp)
            )

            // Table
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF384E66))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Asset ID",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            text = "Kode BMD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = "Asset Name",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))

                    // Scrollable Content Row
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Asset ID Badge
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.width(80.dp)
                        ) {
                            Text(
                                text = asset.id.take(8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        // Kode BMD Badge
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF384E66)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text(
                                text = asset.kodeBmd,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }

                        // Asset name (scrollable)
                        Text(
                            text = asset.nama,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.widthIn(min = 200.dp)
                        )
                    }

                    // Scroll hint if name is long
                    if (asset.nama.length > 20) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.SwipeLeft,
                                    contentDescription = "Scroll",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    "Swipe to see full name",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}