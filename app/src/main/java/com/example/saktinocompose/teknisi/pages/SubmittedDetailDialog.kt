package com.example.saktinocompose.teknisi.pages

import android.app.DatePickerDialog
import android.util.Log
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

private const val TAG = "SubmittedDetailDialog"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmittedDetailDialog(
    changeRequest: ChangeRequest,
    onDismiss: () -> Unit,
    onSave: (
        crId: String,
        description: String,
        impactedAssets: List<String>,
        ciId: String,
        usulanJadwal: String
    ) -> Unit
) {
    val context = LocalContext.current

    // State Management
    var description by remember { mutableStateOf(changeRequest.description) }
    var selectedImpactedAssets by remember { mutableStateOf<List<AsetData>>(emptyList()) }
    var selectedRelasiCI by remember { mutableStateOf<List<AsetData>>(emptyList()) }
    var proposedSchedule by remember { mutableStateOf(changeRequest.usulanJadwal) }
    var showAsetSearchDialog by remember { mutableStateOf(false) }
    var showRelasiDialog by remember { mutableStateOf(false) }

    // Date Picker Configuration
    val datePickerDialog = remember {
        createDatePickerDialog(
            context = context,
            onDateSelected = { selectedDate ->
                proposedSchedule = selectedDate
            }
        )
    }

    // Dialog Handlers
    HandleAsetSearchDialog(
        show = showAsetSearchDialog,
        selectedAssets = selectedImpactedAssets,
        onDismiss = { showAsetSearchDialog = false },
        onSave = { assetList ->
            selectedImpactedAssets = assetList
            showAsetSearchDialog = false
            logSelectedAssets(assetList, "Impacted Assets")
        }
    )

    HandleRelasiDialog(
        show = showRelasiDialog,
        selectedRelasi = selectedRelasiCI,
        onDismiss = { showRelasiDialog = false },
        onSave = { relasiList ->
            selectedRelasiCI = relasiList
            showRelasiDialog = false
            logSelectedRelasi(relasiList)
        }
    )

    // Main Dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle() },
        text = {
            DialogContent(
                changeRequest = changeRequest,
                description = description,
                onDescriptionChange = { description = it },
                selectedImpactedAssets = selectedImpactedAssets,
                onEditImpactedAssets = { showAsetSearchDialog = true },
                selectedRelasiCI = selectedRelasiCI,
                onEditRelasiCI = { showRelasiDialog = true },
                proposedSchedule = proposedSchedule,
                onShowDatePicker = { datePickerDialog.show() }
            )
        },
        confirmButton = {
            ConfirmButton(
                enabled = isFormValid(
                    description,
                    selectedImpactedAssets,
                    selectedRelasiCI,
                    proposedSchedule
                ),
                onClick = {
                    handleSubmit(
                        changeRequest = changeRequest,
                        description = description,
                        selectedImpactedAssets = selectedImpactedAssets,
                        selectedRelasiCI = selectedRelasiCI,
                        proposedSchedule = proposedSchedule,
                        onSave = onSave
                    )
                }
            )
        },
        dismissButton = {
            DismissButton(onClick = onDismiss)
        }
    )
}

// ============================================================================
// Composable Components
// ============================================================================

@Composable
private fun DialogTitle() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Complete Details",
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Complete Request Details",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DialogContent(
    changeRequest: ChangeRequest,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedImpactedAssets: List<AsetData>,
    onEditImpactedAssets: () -> Unit,
    selectedRelasiCI: List<AsetData>,
    onEditRelasiCI: () -> Unit,
    proposedSchedule: String,
    onShowDatePicker: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(crId = changeRequest.id)

        DescriptionSection(
            description = description,
            onDescriptionChange = onDescriptionChange
        )

        ImpactedAssetsSection(
            selectedAssets = selectedImpactedAssets,
            onEdit = onEditImpactedAssets
        )

        RelasiCISection(
            selectedRelasi = selectedRelasiCI,
            onEdit = onEditRelasiCI
        )

        ScheduleSection(
            proposedSchedule = proposedSchedule,
            onShowDatePicker = onShowDatePicker
        )
    }
}

@Composable
private fun InfoCard(crId: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "CR ID: $crId",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lengkapi informasi sebelum melanjutkan ke review",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    SectionTitle(number = "1", title = "Deskripsi *")
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        placeholder = { Text("Jelaskan deskripsi perubahan secara detail...") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun ImpactedAssetsSection(
    selectedAssets: List<AsetData>,
    onEdit: () -> Unit
) {
    SectionTitle(number = "2", title = "Aset Terdampak * (Bisa pilih multiple)")

    if (selectedAssets.isNotEmpty()) {
        AssetListCard(
            assets = selectedAssets,
            onEdit = onEdit,
            showRelationType = false
        )
    }

    SelectButton(
        text = if (selectedAssets.isEmpty())
            "Pilih Aset Terdampak"
        else "Edit Aset Terdampak (${selectedAssets.size})",
        icon = Icons.Default.Search,
        onClick = onEdit
    )
}

@Composable
private fun RelasiCISection(
    selectedRelasi: List<AsetData>,
    onEdit: () -> Unit
) {
    SectionTitle(number = "3", title = "Relasi Configuration Item *")
    Text(
        text = "Pilih aset lain yang terpengaruh perubahan ini",
        fontSize = 12.sp,
        color = Color.Gray
    )

    if (selectedRelasi.isNotEmpty()) {
        AssetListCard(
            assets = selectedRelasi,
            onEdit = onEdit,
            showRelationType = true
        )
    }

    SelectButton(
        text = if (selectedRelasi.isEmpty())
            "Pilih Relasi CI"
        else "Edit Relasi CI (${selectedRelasi.size})",
        icon = Icons.Default.AccountTree,
        onClick = onEdit
    )
}

@Composable
private fun ScheduleSection(
    proposedSchedule: String,
    onShowDatePicker: () -> Unit
) {
    SectionTitle(number = "4", title = "Usulan Jadwal *")
    OutlinedTextField(
        value = proposedSchedule,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Pilih tanggal implementasi") },
        leadingIcon = {
            Icon(Icons.Default.CalendarToday, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onShowDatePicker) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun SectionTitle(number: String, title: String) {
    Text(
        text = "$number. $title",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun AssetListCard(
    assets: List<AsetData>,
    onEdit: () -> Unit,
    showRelationType: Boolean
) {
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
            AssetListHeader(
                count = assets.size,
                onEdit = onEdit
            )

            assets.forEach { asset ->
                AssetItem(
                    asset = asset,
                    showRelationType = showRelationType
                )
            }
        }
    }
}

@Composable
private fun AssetListHeader(
    count: Int,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$count ${if (count == 1) "aset" else "aset"} terpilih",
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color(0xFF384E66)
            )
        }
    }
}

@Composable
private fun AssetItem(
    asset: AsetData,
    showRelationType: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!asset.kodeBmd.isNullOrBlank()) {
                KodeBmdBadge(kodeBmd = asset.kodeBmd)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.nama,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                asset.getMerkModel()?.let { merkModel ->
                    Text(
                        text = merkModel,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                if (showRelationType && asset.tipeRelasi.isNotBlank()) {
                    Text(
                        text = asset.tipeRelasi,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun KodeBmdBadge(kodeBmd: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF384E66)
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = kodeBmd,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}



@Composable
private fun SelectButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF384E66)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ConfirmButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text("Simpan & Lanjut ke Review")
    }
}

@Composable
private fun DismissButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text("Batal")
    }
}

// ============================================================================
// Dialog Handlers
// ============================================================================

@Composable
private fun HandleAsetSearchDialog(
    show: Boolean,
    selectedAssets: List<AsetData>,
    onDismiss: () -> Unit,
    onSave: (List<AsetData>) -> Unit
) {
    if (show) {
        CmdbRelasiCIDialog(
            selectedRelasi = selectedAssets,
            onDismiss = onDismiss,
            onSave = onSave
        )
    }
}

@Composable
private fun HandleRelasiDialog(
    show: Boolean,
    selectedRelasi: List<AsetData>,
    onDismiss: () -> Unit,
    onSave: (List<AsetData>) -> Unit
) {
    if (show) {
        CmdbRelasiCIDialog(
            selectedRelasi = selectedRelasi,
            onDismiss = onDismiss,
            onSave = onSave
        )
    }
}

// ============================================================================
// Helper Functions
// ============================================================================

private fun createDatePickerDialog(
    context: android.content.Context,
    onDateSelected: (String) -> Unit
): DatePickerDialog {
    val calendar = Calendar.getInstance()
    return DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            onDateSelected(dateFormat.format(selectedCalendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }
}

private fun isFormValid(
    description: String,
    impactedAssets: List<AsetData>,
    relasiCI: List<AsetData>,
    schedule: String
): Boolean {
    return description.isNotBlank() &&
            impactedAssets.isNotEmpty() &&
            relasiCI.isNotEmpty() &&
            schedule.isNotBlank()
}

private fun handleSubmit(
    changeRequest: ChangeRequest,
    description: String,
    selectedImpactedAssets: List<AsetData>,
    selectedRelasiCI: List<AsetData>,
    proposedSchedule: String,
    onSave: (String, String, List<String>, String, String) -> Unit
) {
    val impactedAssetsIds = selectedImpactedAssets.map { it.id }
    val ciId = selectedRelasiCI.firstOrNull()?.id ?: ""

    Log.d(TAG, """
        📤 Submitting Change Request:
        - CR ID: ${changeRequest.id}
        - Impacted Assets IDs: $impactedAssetsIds
        - CI ID: $ciId
        - Description: ${description.take(50)}${if (description.length > 50) "..." else ""}
        - Schedule: $proposedSchedule
    """.trimIndent())

    onSave(
        changeRequest.id,
        description,
        impactedAssetsIds,
        ciId,
        proposedSchedule
    )
}

private fun logSelectedAssets(assets: List<AsetData>, type: String) {
    Log.d(TAG, """
        ✅ Selected $type:
        ${assets.joinToString("\n") {
        "  - ID: ${it.id}, Kode: ${it.kodeBmd}, Nama: ${it.nama}"
    }}
    """.trimIndent())
}

private fun logSelectedRelasi(relasi: List<AsetData>) {
    Log.d(TAG, """
        ✅ Selected CI Relations:
        ${relasi.joinToString("\n") {
        "  - ID: ${it.id}, Kode: ${it.kodeBmd}, Relasi: ${it.tipeRelasi}"
    }}
    """.trimIndent())
}