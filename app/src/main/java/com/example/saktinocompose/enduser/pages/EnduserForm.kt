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

    var jenisPerubahan by remember { mutableStateOf(existingRequest?.jenisPerubahan ?: "") }
    var showJenisDropdown by remember { mutableStateOf(false) }
    var alasan by remember { mutableStateOf(existingRequest?.alasan ?: "") }
    var tujuan by remember { mutableStateOf(existingRequest?.tujuan ?: "") }
    var asetTerdampak by remember { mutableStateOf(existingRequest?.asetTerdampak ?: "") }
    var showAsetSearchDialog by remember { mutableStateOf(false) }
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

    // DAFTAR ASET - MASIH ADA DI SINI!
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

    // Dialog Pencarian Aset
    if (showAsetSearchDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredAsets = remember(searchQuery) {
            if (searchQuery.isBlank()) {
                asetOptions
            } else {
                asetOptions.filter {
                    it.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        Dialog(onDismissRequest = { showAsetSearchDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 550.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cari Aset Terdampak",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showAsetSearchDialog = false }) {
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
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Daftar Aset (${filteredAsets.size})",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (filteredAsets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = "No Result",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tidak ada hasil", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredAsets) { aset ->
                                val isSelected = asetTerdampak == aset

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            asetTerdampak = aset
                                            showAsetSearchDialog = false
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
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Inventory,
                                                contentDescription = null,
                                                tint = if (isSelected) Color(0xFF384E66) else Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = aset,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = Color.Black
                                            )
                                        }
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
                    // 1. Id Perubahan
                    Text("1. Id Perubahan *", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = alasan,
                        onValueChange = { alasan = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Buat Id perubahan") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
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
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
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
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 4. Aset Terdampak dengan Search Button
                    Text("4. Aset Terdampak (CI) *", fontWeight = FontWeight.SemiBold)

                    if (asetTerdampak.isNotBlank()) {
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Aset yang dipilih:",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = asetTerdampak,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
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
                            text = if (asetTerdampak.isBlank())
                                "Cari Aset Terdampak"
                            else
                                "Ganti Aset Terdampak",
                            fontSize = 14.sp
                        )
                    }

                    // 5-8 tetap sama seperti sebelumnya...
                    // (Rencana Implementasi, Usulan Jadwal, Rencana Rollback, Pilih Teknisi)

                    Text("5. Rencana Implementasi *", fontWeight = FontWeight.SemiBold)
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

                    Text("6. Usulan Jadwal *", fontWeight = FontWeight.SemiBold)
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

                    Text("7. Rencana Rollback *", fontWeight = FontWeight.SemiBold)
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

                    Text("8. Pilih Teknisi *", fontWeight = FontWeight.SemiBold)
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
                                            jenisPerubahan = jenisPerubahan,
                                            alasan = alasan,
                                            tujuan = tujuan,
                                            asetTerdampak = asetTerdampak,
                                            rencanaImplementasi = rencanaImplementasi,
                                            usulanJadwal = usulanJadwal,
                                            rencanaRollback = rencanaRollback,
                                            assignedTeknisiId = selectedTeknisiId,
                                            assignedTeknisiName = selectedTeknisiName
                                        )
                                    } else {
                                        viewModel.submitChangeRequest(
                                            userId = userId,
                                            jenisPerubahan = jenisPerubahan,
                                            alasan = alasan,
                                            tujuan = tujuan,
                                            asetTerdampak = asetTerdampak,
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