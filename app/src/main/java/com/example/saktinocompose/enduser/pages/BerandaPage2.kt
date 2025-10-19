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
fun BerandaPage2(
    userId: Int,
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
    var dampakRisiko by remember { mutableStateOf("") }
    var rencanaImplementasi by remember { mutableStateOf("") }
    var rencanaRollback by remember { mutableStateOf("") }
    var jadwal by remember { mutableStateOf("") }
    var pic by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val jenisOptions = listOf("Standar", "Minor", "Major", "Emergency")

    // Date Picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            jadwal = dateFormat.format(selectedCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() // Tanggal tidak boleh kurang dari hari ini
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Berhasil!") },
            text = { Text("Permohonan perubahan telah berhasil disubmit") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        // Reset form
                        jenisPerubahan = ""
                        alasan = ""
                        tujuan = ""
                        asetTerdampak = ""
                        dampakRisiko = ""
                        rencanaImplementasi = ""
                        rencanaRollback = ""
                        jadwal = ""
                        pic = ""
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState)
            .padding(top = 90.dp)
    ) {
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
                // 1. Jenis Perubahan (Dropdown)
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
                Text("3. Tujuan", fontWeight = FontWeight.SemiBold)
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

                // 4. Aset/CI Terdampak
                Text("4. Aset/CI Terdampak *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = asetTerdampak,
                    onValueChange = { asetTerdampak = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("Sebutkan aset yang terdampak") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 5. Dampak & Risiko
                Text("5. Dampak & Risiko", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = dampakRisiko,
                    onValueChange = { dampakRisiko = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Jelaskan dampak dan risiko (kualitatif/kuantitatif)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 6. Rencana Implementasi
                Text("6. Rencana Implementasi", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = rencanaImplementasi,
                    onValueChange = { rencanaImplementasi = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Jelaskan rencana implementasi") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 7. Rencana Rollback
                Text("7. Rencana Rollback *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = rencanaRollback,
                    onValueChange = { rencanaRollback = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Jelaskan rencana rollback") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 8. Jadwal
                Text("8. Jadwal *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = jadwal,
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

                // 9. PIC
                Text("9. PIC", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = pic,
                    onValueChange = { pic = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nama PIC") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Submit Button
                Button(
                    onClick = {
                        // Validasi
                        when {
                            jenisPerubahan.isBlank() -> {
                                errorMessage = "Jenis Perubahan wajib diisi"
                                showErrorDialog = true
                            }
                            alasan.isBlank() -> {
                                errorMessage = "Alasan wajib diisi"
                                showErrorDialog = true
                            }
                            asetTerdampak.isBlank() -> {
                                errorMessage = "Aset/CI Terdampak wajib diisi"
                                showErrorDialog = true
                            }
                            rencanaRollback.isBlank() -> {
                                errorMessage = "Rencana Rollback wajib diisi"
                                showErrorDialog = true
                            }
                            jadwal.isBlank() -> {
                                errorMessage = "Jadwal wajib diisi"
                                showErrorDialog = true
                            }
                            else -> {
                                // Submit
                                viewModel.submitChangeRequest(
                                    userId = userId,
                                    jenisPerubahan = jenisPerubahan,
                                    alasan = alasan,
                                    tujuan = tujuan,
                                    asetTerdampak = asetTerdampak,
                                    dampakRisiko = dampakRisiko,
                                    rencanaImplementasi = rencanaImplementasi,
                                    rencanaRollback = rencanaRollback,
                                    jadwal = jadwal,
                                    pic = pic
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