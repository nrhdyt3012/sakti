package com.example.saktinocompose.enduser.pages

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.saktinocompose.data.model.AsetData
import com.example.saktinocompose.data.model.AsetHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnduserForm(
    userId: Int,
    userName: String,
    existingRequest: com.example.saktinocompose.data.entity.ChangeRequest? = null,
    onFormSubmitted: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChangeRequestViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val database = AppDatabase.getDatabase(context)

    // ✅ ID Perubahan - auto-generated atau dari existing
    val idPerubahan = remember {
        existingRequest?.idPerubahan ?: UUID.randomUUID().toString()
    }

    var jenisPerubahan by remember { mutableStateOf(existingRequest?.jenisPerubahan ?: "") }
    var showJenisDropdown by remember { mutableStateOf(false) }
    var alasan by remember { mutableStateOf(existingRequest?.alasan ?: "") }
    var tujuan by remember { mutableStateOf(existingRequest?.tujuan ?: "") }

    // ✅ Field Aset
    var idAset by remember { mutableStateOf(existingRequest?.idAset ?: "") }
    var asetTerdampakId by remember { mutableStateOf("") }
    var asetTerdampakNama by remember { mutableStateOf("") }

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
    var rencanaRollback by remember { mutableStateOf(existingRequest?.rencanaRollback ?: "") }
    var selectedTeknisiId by remember { mutableStateOf<Int?>(existingRequest?.assignedTeknisiId) }
    var selectedTeknisiName by remember { mutableStateOf<String?>(existingRequest?.assignedTeknisiName) }
    var showTeknisiDropdown by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val teknisiList by database.userDao().getAllTeknisi().collectAsState(initial = emptyList())

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
            title = { Text("Berhasil!") },
            text = {
                Text(if (existingRequest != null)
                    "Revisi permohonan telah berhasil disubmit ulang"
                else
                    "Permohonan perubahan telah berhasil disubmit")
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
                        "Revisi Permohonan"
                    else
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
                    Text("ID Perubahan (Auto-generated)", fontWeight = FontWeight.SemiBold)
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
                                        text = "ID Unik Perubahan:",
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
                    Text("2. Judul Permintaan Perubahan *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = alasan,
                        onValueChange = { alasan = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Jelaskan judul permintaan perubahan") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 3. Deskripsi
                    Text("3. Deskripsi *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = tujuan,
                        onValueChange = { tujuan = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Jelaskan deskripsi perubahan") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 4. ID Aset
                    Text("4. ID Aset *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = idAset,
                        onValueChange = { idAset = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Contoh: AST-001, SRV-123, dll") },
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
                    Text("5. Aset yang Diperbaiki (CI) *", fontWeight = FontWeight.SemiBold)

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
                                            text = "Aset yang dipilih:",
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
                                        contentDescription = "Ganti",
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
                                "Cari Aset yang Diperbaiki"
                            else
                                "Ganti Aset yang Diperbaiki",
                            fontSize = 14.sp
                        )
                    }

                    // 6. Relasi Configuration Item
                    Text("6. Relasi Configuration Item *", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Pilih aset lain yang terpengaruh oleh perbaikan aset ini",
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
                    Text("7. Rencana Implementasi *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = rencanaImplementasi,
                        onValueChange = { rencanaImplementasi = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Jelaskan rencana implementasi") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 8. Usulan Jadwal
                    Text("8. Usulan Jadwal *", fontWeight = FontWeight.SemiBold)
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
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 9. Rencana Rollback
                    Text("9. Rencana Rollback *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = rencanaRollback,
                        onValueChange = { rencanaRollback = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Jelaskan rencana rollback") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 10. Pilih Teknisi
                    Text("10. Pilih Teknisi *", fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = showTeknisiDropdown,
                        onExpandedChange = { showTeknisiDropdown = !showTeknisiDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedTeknisiName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Teknisi") },
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

                    Button(
                        onClick = {
                            when {
                                jenisPerubahan.isBlank() -> {
                                    errorMessage = "Jenis Perubahan wajib diisi"
                                    showErrorDialog = true
                                }
                                alasan.isBlank() -> {
                                    errorMessage = "Judul Permintaan Perubahan wajib diisi"
                                    showErrorDialog = true
                                }
                                tujuan.isBlank() -> {
                                    errorMessage = "Deskripsi wajib diisi"
                                    showErrorDialog = true
                                }
                                idAset.isBlank() -> {
                                    errorMessage = "ID Aset wajib diisi"
                                    showErrorDialog = true
                                }
                                asetTerdampakNama.isBlank() -> {
                                    errorMessage = "Aset yang Diperbaiki wajib diisi"
                                    showErrorDialog = true
                                }
                                selectedRelasiCI.isEmpty() -> {
                                    errorMessage = "Relasi Configuration Item wajib diisi"
                                    showErrorDialog = true
                                }
                                rencanaImplementasi.isBlank() -> {
                                    errorMessage = "Rencana Implementasi wajib diisi"
                                    showErrorDialog = true
                                }
                                usulanJadwal.isBlank() -> {
                                    errorMessage = "Usulan Jadwal wajib diisi"
                                    showErrorDialog = true
                                }
                                rencanaRollback.isBlank() -> {
                                    errorMessage = "Rencana Rollback wajib diisi"
                                    showErrorDialog = true
                                }
                                selectedTeknisiId == null -> {
                                    errorMessage = "Teknisi wajib dipilih"
                                    showErrorDialog = true
                                }
                                else -> {
                                    if (existingRequest != null) {
                                        viewModel.updateChangeRequestForRevision(
                                            existingRequest = existingRequest,
                                            idPerubahan = idPerubahan,
                                            jenisPerubahan = jenisPerubahan,
                                            alasan = alasan,
                                            tujuan = tujuan,
                                            idAset = idAset,
                                            asetTerdampak = formattedAsetTerdampak,
                                            relasiConfigurationItem = formattedRelasiCI,
                                            rencanaImplementasi = rencanaImplementasi,
                                            usulanJadwal = usulanJadwal,
                                            rencanaRollback = rencanaRollback,
                                            assignedTeknisiId = selectedTeknisiId,
                                            assignedTeknisiName = selectedTeknisiName
                                        )
                                    } else {
                                        viewModel.submitChangeRequest(
                                            userId = userId,
                                            idPerubahan = idPerubahan,
                                            jenisPerubahan = jenisPerubahan,
                                            alasan = alasan,
                                            tujuan = tujuan,
                                            idAset = idAset,
                                            asetTerdampak = formattedAsetTerdampak,
                                            relasiConfigurationItem = formattedRelasiCI,
                                            rencanaImplementasi = rencanaImplementasi,
                                            usulanJadwal = usulanJadwal,
                                            rencanaRollback = rencanaRollback,
                                            assignedTeknisiId = selectedTeknisiId,
                                            assignedTeknisiName = selectedTeknisiName
                                        )
                                    }
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
                            text = if (existingRequest != null) "Submit Revisi" else "Submit Permohonan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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

// ✅ KOMPONEN BARU: Dialog Pencarian Aset (Lebih Rapi)
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
                        text = "Pilih Aset yang Diperbaiki",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ketik untuk mencari...") },
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
                        Text("Tidak ada hasil", color = Color.Gray)
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
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

// ✅ KOMPONEN BARU: Dialog Relasi CI dengan Tipe Relasi
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
                            text = "${tempSelectedRelasi.size} dipilih",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ketik untuk mencari...") },
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
                                                contentDescription = "Edit Tipe",
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
                                                contentDescription = "Hapus",
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
                    Text("Simpan (${tempSelectedRelasi.size} dipilih)")
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
            title = { Text("Pilih Tipe Relasi") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tipeRelasiOptions.forEach { tipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val index = tempSelectedRelasi.indexOfFirst { it.id == editingAsetId }
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
                    Text("Batal")
                }
            }
        )
    }
}