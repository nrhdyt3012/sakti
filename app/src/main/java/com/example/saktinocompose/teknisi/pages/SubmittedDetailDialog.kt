package com.example.saktinocompose.teknisi.pages

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.saktinocompose.data.model.AsetData
import com.example.saktinocompose.data.model.ChangeRequest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmittedDetailDialog(
    changeRequest: ChangeRequest,
    onDismiss: () -> Unit,
    onSave: (
        crId: String,
        description: String,
        asetTerdampakId: String,
        ciId: String,
        usulanJadwal: String
    ) -> Unit
) {
    val context = LocalContext.current

    // Form States
    var description by remember { mutableStateOf(changeRequest.description) }
    var asetTerdampakId by remember { mutableStateOf("") }
    var asetTerdampakNama by remember { mutableStateOf("") }
    var selectedRelasiCI by remember { mutableStateOf<List<AsetData>>(emptyList()) }
    var proposedSchedule by remember { mutableStateOf(changeRequest.usulanJadwal) }

    var showAsetSearchDialog by remember { mutableStateOf(false) }
    var showRelasiDialog by remember { mutableStateOf(false) }

    // Date Picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            proposedSchedule = dateFormat.format(selectedCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Complete Details",
                    tint = Color(0xFF2196F3)
                )
                Text(
                    text = "Complete Request Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "CR ID: ${changeRequest.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please complete the required information before proceeding to review",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // 1. Description
                Text(
                    text = "1. Description *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Explain the change description in detail...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // 2. Affected Asset
                Text(
                    text = "2. Affected Asset *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
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
                                        text = "Selected:",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = asetTerdampakNama,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            IconButton(onClick = { showAsetSearchDialog = true }) {
                                Icon(Icons.Default.Edit, "Change", tint = Color(0xFF384E66))
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
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (asetTerdampakNama.isBlank()) "Search Affected Asset" else "Change Affected Asset",
                        fontSize = 14.sp
                    )
                }

                // 3. CI Relationship
                Text(
                    text = "3. Configuration Item Relationship *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Select other assets affected by this change",
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
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${selectedRelasiCI.size} relations selected",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                IconButton(onClick = { showRelasiDialog = true }) {
                                    Icon(Icons.Default.Edit, "Edit", tint = Color(0xFF384E66))
                                }
                            }

                            selectedRelasiCI.forEach { aset ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(10.dp),
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
                                                aset.id,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                aset.nama,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (aset.tipeRelasi.isNotBlank()) {
                                                Text(
                                                    aset.tipeRelasi,
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
                    Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedRelasiCI.isEmpty())
                            "Select CI Relations"
                        else
                            "Edit CI Relations (${selectedRelasiCI.size})",
                        fontSize = 14.sp
                    )
                }

                // 4. Proposed Schedule
                Text(
                    text = "4. Proposed Schedule *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = proposedSchedule,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Select implementation date") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() &&
                        asetTerdampakNama.isNotBlank() &&
                        selectedRelasiCI.isNotEmpty() &&
                        proposedSchedule.isNotBlank()
                    ) {
                        val asetTerdampakFormatted = "$asetTerdampakId:$asetTerdampakNama"
                        // ✅ FORMAT BARU
                        val ciIds = selectedRelasiCI.joinToString(",") { it.id }

                        onSave(
                            changeRequest.id,
                            description,
                            asetTerdampakId,   // "APK001"
                            ciIds,             // "APK002,APK003"
                            proposedSchedule
                        )
                    }
                },
                enabled = description.isNotBlank() &&
                        asetTerdampakNama.isNotBlank() &&
                        selectedRelasiCI.isNotEmpty() &&
                        proposedSchedule.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("Save & Continue to Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}