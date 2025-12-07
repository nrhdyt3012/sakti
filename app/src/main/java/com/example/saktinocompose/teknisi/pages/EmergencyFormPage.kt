package com.example.saktinocompose.teknisi.pages

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.R
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.saktinocompose.data.model.AsetData
import com.example.saktinocompose.data.model.AsetHelper
import com.example.saktinocompose.data.model.Teknisi

import kotlin.compareTo
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyFormPage(
    userId: String,
    userName: String,
    existingRequest: ChangeRequest? = null,
    onFormSubmitted: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChangeRequestViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val idPerubahan = remember {
        existingRequest?.idPerubahan ?: UUID.randomUUID().toString()
    }

    var jenisPerubahan by remember { mutableStateOf(existingRequest?.jenisPerubahan ?: "") }
    var showJenisDropdown by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(existingRequest?.title ?: "") }
    var description by remember { mutableStateOf(existingRequest?.description ?: "") }

    // ✅ Field Aset
    var idAset by remember { mutableStateOf(existingRequest?.idAset ?: "") }
    var asetTerdampakId by remember { mutableStateOf("") }
    var asetTerdampakNama by remember { mutableStateOf("") }
    var estimasiBiaya by remember { mutableStateOf(existingRequest?.estimasiBiaya ?: "") }
    var estimasiWaktu by remember { mutableStateOf(existingRequest?.estimasiWaktu ?: "") }
    var skorDampak by remember { mutableIntStateOf(0) }
    var skorKemungkinan by remember { mutableIntStateOf(0) }
    var skorEksposur by remember { mutableIntStateOf(0) }
    var showEksposurDropdown by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val skorRisiko = skorDampak * skorKemungkinan * skorEksposur
    val levelRisiko = calculateRiskLevel(skorDampak, skorKemungkinan)

    var scheduledDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedRelasiCI by remember {
        mutableStateOf<List<AsetData>>(
            existingRequest?.let {
                AsetHelper.parseRelatedCI(it.relasiConfigurationItem)
            } ?: emptyList()
        )
    }

    val formattedAsetTerdampak = "$asetTerdampakId:$asetTerdampakNama"
    val formattedRelasiCI = AsetHelper.formatRelatedCI(selectedRelasiCI)

    // Load existing data
    LaunchedEffect(existingRequest) {
        existingRequest?.let { request ->
            val parts = request.asetTerdampak.split(":")
            if (parts.size == 2) {
                asetTerdampakId = parts[0]
                asetTerdampakNama = parts[1]
            } else {
                asetTerdampakNama = request.asetTerdampak
                asetTerdampakId = AsetHelper.generateAsetId(request.asetTerdampak)
            }
        }
    }

    var showAsetSearchDialog by remember { mutableStateOf(false) }
    var showRelasiDialog by remember { mutableStateOf(false) }

    var rencanaImplementasi by remember { mutableStateOf(existingRequest?.rencanaImplementasi ?: "") }
    var usulanJadwal by remember { mutableStateOf(existingRequest?.usulanJadwal ?: "") }
    var rencanaRollback by remember { mutableStateOf(existingRequest?.rollbackPlan ?: "") }
    var selectedTeknisiId: String? by remember { mutableStateOf(existingRequest?.assignedTeknisiId) }
    var selectedTeknisiName by remember { mutableStateOf<String?>(existingRequest?.assignedTeknisiName) }
    var showTeknisiDropdown by remember { mutableStateOf(false) }

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


    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val jenisOptions = listOf("Standar", "Minor", "Major", "Emergency")

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
            title = { Text(stringResource(R.string.success)) },
            text = {
                Text(if (existingRequest != null)
                    "The revised application has been successfully resubmitted."
                else
                    "Change request has been successfully submitted")
            },
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
            title = { Text(stringResource(R.string.error)) },
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

    // ✅ UPDATED: Dialog Pencarian Aset yang Diperbaiki
    if (showAsetSearchDialog) {
        AsetSearchDialog(
            selectedAsetNama = asetTerdampakNama,
            onDismiss = { showAsetSearchDialog = false },
            onSelect = { id, nama ->
                asetTerdampakId = id
                asetTerdampakNama = nama
                showAsetSearchDialog = false
            }
        )
    }

    // ✅ UPDATED: Dialog Relasi Configuration Item dengan Tipe Relasi
    if (showRelasiDialog) {
        RelasiCIDialog(
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
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = {
                Text(
                    if (existingRequest != null)
                        stringResource(R.string.revision_request)
                    else
                        stringResource(R.string.change_request_form),
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
            // Notifikasi Revisi
            if (existingRequest?.revisionNotes != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Revisi",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Catatan Revisi dari Teknisi:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = existingRequest.revisionNotes,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

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
                    // 0. ID Perubahan
                    Text(stringResource(R.string.change_id_auto), fontWeight = FontWeight.SemiBold)
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
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
                                        text = "Unique Change ID:",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = idPerubahan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3)
                                    )
                                }
                            }
                        }
                    }

                    // 1. Jenis Perubahan
                    Text(stringResource(R.string.change_type) + " *", fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = showJenisDropdown,
                        onExpandedChange = { showJenisDropdown = !showJenisDropdown }
                    ) {
                        OutlinedTextField(
                            value = jenisPerubahan,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Choose Change type") },
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
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

                    // 2. Judul Permintaan Perubahan
                    Text(stringResource(R.string.request_title) + " *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Explain request title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 3. Deskripsi
                    Text(stringResource(R.string.description) + " *"
                        , fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Explain change description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 4. ID Aset
                    Text(stringResource(R.string.asset_id) + " *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = idAset,
                        onValueChange = { idAset = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Example: AST-001, SRV-123, dll") },
                        leadingIcon = {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 5. Aset yang Diperbaiki
                    Text(stringResource(R.string.affected_asset) + " *", fontWeight = FontWeight.SemiBold)

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
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = asetTerdampakNama,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
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
                            contentDescription = "Cari",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (asetTerdampakNama.isBlank())
                                "Search for Repaired Assets"
                            else
                                "Change for Repaired Assets",
                            fontSize = 14.sp
                        )
                    }

                    // 6. Relasi Configuration Item
                    Text(stringResource(R.string.ci_relationship) + " *", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Select other assets affected by this asset's repair.",
                        fontSize = 12.sp,
                        color = Color.Gray
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
                                        text = "${selectedRelasiCI.size} relasi dipilih",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
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
                                            containerColor = Color.White
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
                                                    color = Color.Black
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
                            contentDescription = "Relasi",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedRelasiCI.isEmpty())
                                "Pilih Relasi CI"
                            else
                                "Edit Relasi CI (${selectedRelasiCI.size})",
                            fontSize = 14.sp
                        )
                    }

                    // 7. Rencana Implementasi
                    Text(stringResource(R.string.implementation_plan) + " *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = rencanaImplementasi,
                        onValueChange = { rencanaImplementasi = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Explain the implementation plan") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 8. Usulan Jadwal
                    Text(stringResource(R.string.proposed_schedule) + " *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = usulanJadwal,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        placeholder = { Text("Choose date") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Choose date",
                                modifier = Modifier.clickable { datePickerDialog.show() }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 9. Rencana Rollback
                    Text(stringResource(R.string.rollback_plan) + " *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = rencanaRollback,
                        onValueChange = { rencanaRollback = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Explain the rollback plan") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 10. Pilih Teknisi
                    Text(stringResource(R.string.select_technician) + " *", fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = showTeknisiDropdown,
                        onExpandedChange = { showTeknisiDropdown = !showTeknisiDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedTeknisiName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Technical") },
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showTeknisiDropdown,
                            onDismissRequest = { showTeknisiDropdown = false }
                        ) {
                            val teknisiList = listOf(
                                Teknisi(id = "1", name = "Bahli"),
                                Teknisi(id = "2", name = "Budi")
                            )

                            teknisiList.forEach { teknisi ->
                                DropdownMenuItem(
                                    text = { Text(teknisi.name) },
                                    onClick = {
                                        selectedTeknisiId = teknisi.id
                                        selectedTeknisiName = teknisi.name
                                        showTeknisiDropdown = false
                                    }
                                )
                            }

                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
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
                    Text(
                        text = stringResource(R.string.implementation_date) + " *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = scheduledDate,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Choose date") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // Catatan
                    Text(
                        text = stringResource(R.string.notes) + " (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Add a note for scheduling...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // Info peringatan
                    if (scheduledDate.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Implementasi Terjadwal",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3)
                                    )
                                    Text(
                                        text = scheduledDate,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
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
            }



                    Button(
                        onClick = {
                            when {
                                jenisPerubahan.isBlank() -> {
                                    errorMessage = "Change Type is mandatory"
                                    showErrorDialog = true
                                }
                                title.isBlank() -> {
                                    errorMessage = "Change Request Title is required."
                                    showErrorDialog = true
                                }
                                description.isBlank() -> {
                                    errorMessage = "Description is mandatory"
                                    showErrorDialog = true
                                }
                                idAset.isBlank() -> {
                                    errorMessage = "Asset ID is required"
                                    showErrorDialog = true
                                }
                                asetTerdampakNama.isBlank() -> {
                                    errorMessage = "Repaired Assets must be filled in"
                                    showErrorDialog = true
                                }
                                selectedRelasiCI.isEmpty() -> {
                                    errorMessage = "Configuration Item Relation is required"
                                    showErrorDialog = true
                                }
                                rencanaImplementasi.isBlank() -> {
                                    errorMessage = "Implementation Plan must be filled in"
                                    showErrorDialog = true
                                }
                                usulanJadwal.isBlank() -> {
                                    errorMessage = "Proposed Schedule must be filled in"
                                    showErrorDialog = true
                                }
                                rencanaRollback.isBlank() -> {
                                    errorMessage = "Rollback Plan is required"
                                    showErrorDialog = true
                                }
                                selectedTeknisiId == null -> {
                                    errorMessage = "Technician must be selected"
                                    showErrorDialog = true
                                }
                                else -> {
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
                            text = if (existingRequest != null) stringResource(R.string.submit) else stringResource(R.string.submit),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = stringResource(R.string.required_field),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
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