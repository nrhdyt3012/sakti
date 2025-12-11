package com.example.saktinocompose.teknisi.pages

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.saktinocompose.data.model.AsetData
import com.example.saktinocompose.data.model.AsetHelper
import com.example.saktinocompose.utils.NetworkHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyFormPage(
    userId: String,
    userName: String,
    onFormSubmitted: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChangeRequestViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // ✅ CHECK INTERNET CONNECTION
    var isOnline by remember { mutableStateOf(NetworkHelper.isInternetAvailable(context)) }
    var showOfflineDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            isOnline = NetworkHelper.isInternetAvailable(context)
            kotlinx.coroutines.delay(3000)
        }
    }

    // ✅ OFFLINE DIALOG
    if (showOfflineDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "No Internet",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "No Internet Connection",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This feature requires an internet connection.")
                    Text(
                        "Please check your connection and try again.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOfflineDialog = false
                        isOnline = NetworkHelper.isInternetAvailable(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfflineDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ✅ FORM STATES
    val idPerubahan = remember { UUID.randomUUID().toString() }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var asetTerdampakId by remember { mutableStateOf("") }
    var asetTerdampakNama by remember { mutableStateOf("") }
    var selectedRelasiCI by remember { mutableStateOf<List<AsetData>>(emptyList()) }
    var rencanaImplementasi by remember { mutableStateOf("") }
    var usulanJadwal by remember { mutableStateOf("") }
    var rencanaRollback by remember { mutableStateOf("") }
    var estimasiBiaya by remember { mutableStateOf("") }
    var estimasiWaktu by remember { mutableStateOf("") }
    var skorDampak by remember { mutableIntStateOf(0) }
    var skorKemungkinan by remember { mutableIntStateOf(0) }
    var skorEksposur by remember { mutableIntStateOf(0) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var showDampakDropdown by remember { mutableStateOf(false) }
    var showKemungkinanDropdown by remember { mutableStateOf(false) }
    var showEksposurDropdown by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var showAsetSearchDialog by remember { mutableStateOf(false) }
    var showRelasiDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val skorRisiko = skorDampak * skorKemungkinan * skorEksposur
    val levelRisiko = calculateRiskLevel(skorDampak, skorKemungkinan)
    val levelColor = getRiskLevelColor(levelRisiko)

    // Date Picker
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

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success!") },
            text = { Text("Emergency change request has been successfully submitted") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
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

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
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

    // Aset Search Dialog
    if (showAsetSearchDialog) {
        CmdbAsetSearchDialog(
            selectedKodeBmd = asetTerdampakId,
            onDismiss = { showAsetSearchDialog = false },
            onSelect = { kodeBmd, namaAsset ->
                asetTerdampakId = kodeBmd  // Dari CMDB
                asetTerdampakNama = namaAsset  // Dari CMDB
                showAsetSearchDialog = false
            }
        )
    }

    // Relasi CI Dialog
    if (showRelasiDialog) {
        CmdbRelasiCIDialog(
            selectedRelasi = selectedRelasiCI,
            onDismiss = { showRelasiDialog = false },
            onSave = { relasiList ->
                selectedRelasiCI = relasiList
                showRelasiDialog = false
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
                    Text(
                        "Emergency Form",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (!isOnline) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFD32F2F)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CloudOff,
                                    contentDescription = "Offline",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Text(
                                    "Offline",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFFF5722)
            )
        )

        // ✅ OFFLINE WARNING BANNER
        if (!isOnline) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "You are currently offline",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Please connect to the internet to submit the form",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Emergency Badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF5722).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Emergency",
                                tint = Color(0xFFFF5722)
                            )
                            Column {
                                Text(
                                    "Emergency Change Request",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5722)
                                )
                                Text(
                                    "Fill in all required fields marked with *",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // Change ID
                    Text(
                        "Change ID (Auto-generated)",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = "ID",
                                tint = Color(0xFF2196F3)
                            )
                            Column {
                                Text(
                                    "Unique Change ID:",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    idPerubahan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    }

                    // Request Title
                    Text(
                        "Change Request Title *",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = {
                            Text(
                                "Explain request title",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Description
                    Text(
                        "Description *",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = {
                            Text(
                                "Explain change description",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Affected Asset
                    Text(
                        "Affected Asset *",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (asetTerdampakNama.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF384E66).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF384E66)
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = asetTerdampakId,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Selected assets:",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = asetTerdampakNama,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                IconButton(onClick = { showAsetSearchDialog = true }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Change",
                                        tint = Color(0xFF384E66)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showAsetSearchDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF384E66)
                        )
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (asetTerdampakNama.isBlank())
                                "Search for Affected Assets"
                            else
                                "Change Affected Assets",
                            fontSize = 14.sp
                        )
                    }

                    // CI Relationship
                    Text(
                        "Configuration Item Relationship *",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Select other assets affected by this asset's change.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    if (selectedRelasiCI.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF384E66).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedRelasiCI.size} relations selected",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    IconButton(onClick = { showRelasiDialog = true }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = Color(0xFF384E66)
                                        )
                                    }
                                }

                                selectedRelasiCI.forEach { aset ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFF384E66)
                                                ),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = aset.id,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = aset.nama,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (aset.tipeRelasi.isNotBlank()) {
                                                    Text(
                                                        text = aset.tipeRelasi,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF2196F3),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showRelasiDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF384E66)
                        )
                    ) {
                        Icon(
                            Icons.Default.AccountTree,
                            contentDescription = "Relations",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedRelasiCI.isEmpty())
                                "Select CI Relations"
                            else
                                "Edit CI Relations (${selectedRelasiCI.size})",
                            fontSize = 14.sp
                        )
                    }

                    // Implementation Plan
                    Text(
                        "Implementation Plan *",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = rencanaImplementasi,
                        onValueChange = { rencanaImplementasi = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = {
                            Text(
                                "Explain the implementation plan",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Proposed Schedule
                    Text(
                        "Proposed Schedule *",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = usulanJadwal,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        placeholder = {
                            Text(
                                "Choose date",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Choose date",
                                modifier = Modifier.clickable { datePickerDialog.show() }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Rollback Plan
                    Text(
                        "Rollback Plan *",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = rencanaRollback,
                        onValueChange = { rencanaRollback = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = {
                            Text(
                                "Explain the rollback plan",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Cost Estimate
                    Text(
                        "Cost Estimate *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = estimasiBiaya,
                        onValueChange = { estimasiBiaya = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Example: Rp 5.000.000",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Time Estimate
                    Text(
                        "Time Estimate *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = estimasiWaktu,
                        onValueChange = { estimasiWaktu = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { // PART 1 sudah ada di artifact di atas

// PART 2 - Lanjutan dari placeholder Time Estimate:
                            Text(
                                "Example: 2 days / 4 hours",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Impact Score
                    Text(
                        "Impact Score *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
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
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showDampakDropdown,
                            onDismissRequest = { showDampakDropdown = false }
                        ) {
                            (1..5).forEach { score ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "$score - ${getImpactLabel(score)}",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        skorDampak = score
                                        showDampakDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Probability Score
                    Text(
                        "Probability Score *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
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
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showKemungkinanDropdown,
                            onDismissRequest = { showKemungkinanDropdown = false }
                        ) {
                            (1..5).forEach { score ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "$score - ${getProbabilityLabel(score)}",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        skorKemungkinan = score
                                        showKemungkinanDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Exposure Score
                    Text(
                        "Exposure Score *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
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
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showEksposurDropdown,
                            onDismissRequest = { showEksposurDropdown = false }
                        ) {
                            (1..4).forEach { score ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            score.toString(),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        skorEksposur = score
                                        showEksposurDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Risk Calculation Result
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
                                    text = "Risk Calculation Result",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Risk Score:",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "$skorRisiko",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Risk Level:",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
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

                    // Field Evidence Photo (Optional)
                    Text(
                        "Field Evidence Photo (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
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
                }
            }

            // ✅ SPACER AGAR BUTTON TIDAK TERTUTUP BOTTOM NAV
            Spacer(modifier = Modifier.height(16.dp))

            // ✅ SUBMIT BUTTON
            Button(
                onClick = {
                    // ✅ VALIDASI - Semua field kecuali foto
                    when {
                        title.isBlank() -> {
                            errorMessage = "Change Request Title is required"
                            showErrorDialog = true
                        }
                        description.isBlank() -> {
                            errorMessage = "Description is required"
                            showErrorDialog = true
                        }
                        asetTerdampakNama.isBlank() -> {
                            errorMessage = "Affected Asset must be filled in"
                            showErrorDialog = true
                        }
                        selectedRelasiCI.isEmpty() -> {
                            errorMessage = "Configuration Item Relation is required"
                            showErrorDialog = true
                        }
                        rencanaImplementasi.isBlank() -> {
                            errorMessage = "Implementation Plan is required"
                            showErrorDialog = true
                        }
                        usulanJadwal.isBlank() -> {
                            errorMessage = "Proposed Schedule is required"
                            showErrorDialog = true
                        }
                        rencanaRollback.isBlank() -> {
                            errorMessage = "Rollback Plan is required"
                            showErrorDialog = true
                        }
                        estimasiBiaya.isBlank() -> {
                            errorMessage = "Cost Estimate is required"
                            showErrorDialog = true
                        }
                        estimasiWaktu.isBlank() -> {
                            errorMessage = "Time Estimate is required"
                            showErrorDialog = true
                        }
                        skorDampak == 0 -> {
                            errorMessage = "Impact Score must be filled in"
                            showErrorDialog = true
                        }
                        skorKemungkinan == 0 -> {
                            errorMessage = "Probability Score must be filled in"
                            showErrorDialog = true
                        }
                        skorEksposur == 0 -> {
                            errorMessage = "Exposure Score must be filled in"
                            showErrorDialog = true
                        }
                        !isOnline -> {
                            showOfflineDialog = true
                        }
                        else -> {
                            // ✅ ALL VALID - SUBMIT
                            showSuccessDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOnline) Color(0xFFFF5722) else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = isOnline
            ) {
                if (!isOnline) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isOnline) "Submit Emergency Request" else "Offline - Cannot Submit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "* Required fields | Photo is optional",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )

            // ✅ EXTRA SPACER UNTUK BOTTOM NAV
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelasiCIDialog(
    selectedRelasi: List<AsetData>,
    onDismiss: () -> Unit,
    onSave: (List<AsetData>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val asetOptions = AsetHelper.getAllAsetKategori()
    val tempSelectedRelasi = remember {
        mutableStateListOf<AsetData>().apply { addAll(selectedRelasi) }
    }

    // State untuk edit tipe relasi
    var editingAsetId by remember { mutableStateOf<String?>(null) }
    var showTipeRelasiDropdown by remember { mutableStateOf(false) }

    val tipeRelasiOptions = listOf("INSTALLED_ON", "DEPENDS_ON", "CONNECTED_TO", "RUNS_ON")

    val filteredAsets = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            asetOptions
        } else {
            asetOptions.filter {
                it.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Relasi Configuration Item",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tempSelectedRelasi.size} chosen",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Click to search...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selected Items Preview
                if (tempSelectedRelasi.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF384E66).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Dipilih:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            tempSelectedRelasi.forEach { aset ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFF384E66)
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = aset.id,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = aset.nama,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Black,
                                                maxLines = 1
                                            )
                                            if (aset.tipeRelasi.isNotBlank()) {
                                                Text(
                                                    text = aset.tipeRelasi,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF2196F3),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        // Edit Tipe Relasi Button
                                        IconButton(
                                            onClick = {
                                                editingAsetId = aset.id
                                                showTipeRelasiDropdown = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit type",
                                                modifier = Modifier.size(16.dp),
                                                tint = Color(0xFF2196F3)
                                            )
                                        }

                                        // Remove Button
                                        IconButton(
                                            onClick = {
                                                tempSelectedRelasi.removeAll { it.id == aset.id }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Delete",
                                                modifier = Modifier.size(16.dp),
                                                tint = Color(0xFFD32F2F)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Aset List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredAsets) { asetNama ->
                        val generatedId = AsetHelper.generateAsetId(asetNama)
                        val isSelected = tempSelectedRelasi.any { it.id == generatedId }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isSelected) {
                                        // Add dengan tipe relasi default
                                        tempSelectedRelasi.add(
                                            AsetData(
                                                id = generatedId,
                                                nama = asetNama,
                                                tipeRelasi = "DEPENDS_ON" // Default
                                            )
                                        )
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    Color(0xFF384E66).copy(alpha = 0.15f)
                                else
                                    Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null
                                )

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF384E66)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = generatedId,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = asetNama,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        onSave(tempSelectedRelasi.toList())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF384E66)
                    ),
                    enabled = tempSelectedRelasi.isNotEmpty()
                ) {
                    Text("Sve (${tempSelectedRelasi.size} chosen)")
                }
            }
        }
    }

    // Dialog Edit Tipe Relasi
    if (showTipeRelasiDropdown && editingAsetId != null) {
        AlertDialog(
            onDismissRequest = {
                showTipeRelasiDropdown = false
                editingAsetId = null
            },
            title = { Text("Select Relationship Type") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tipeRelasiOptions.forEach { tipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val index =
                                        tempSelectedRelasi.indexOfFirst { it.id == editingAsetId }
                                    if (index != -1) {
                                        tempSelectedRelasi[index] = tempSelectedRelasi[index].copy(
                                            tipeRelasi = tipe
                                        )
                                    }
                                    showTipeRelasiDropdown = false
                                    editingAsetId = null
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tipe,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showTipeRelasiDropdown = false
                    editingAsetId = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsetSearchDialog(
    selectedAsetNama: String,
    onDismiss: () -> Unit,
    onSelect: (id: String, nama: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val asetOptions = AsetHelper.getAllAsetKategori()
    val filteredAsets = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            asetOptions
        } else {
            asetOptions.filter {
                it.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 550.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select the Asset to Repair",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Click to search...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredAsets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Result", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredAsets) { aset ->
                            val isSelected = selectedAsetNama == aset
                            val generatedId = AsetHelper.generateAsetId(aset)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(generatedId, aset)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        Color(0xFF384E66).copy(alpha = 0.15f)
                                    else
                                        Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF384E66)
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = generatedId,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }

                                    Text(
                                        text = aset,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF384E66),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
