package com.example.saktinocompose.enduser.pages

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnduserForm(
    userId: Int,
    userName: String,
    onFormSubmitted: () -> Unit = {},
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
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Form Permohonan Perubahan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

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
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
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
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
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
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
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
                                viewModel.submitChangeRequest(
                                    userId = userId,
                                    jenisPerubahan = jenisPerubahan,
                                    alasan = alasan,
                                    tujuan = tujuan,
                                    asetTerdampak = asetTerdampak,
                                    usulanJadwal = usulanJadwal
                                )
                                showSuccessDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF37474F)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Submit Permohonan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
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