// File: app/src/main/java/com/example/saktinocompose/teknisi/pages/CmdbRelasiCIDialog.kt
package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.data.model.AsetData
import com.example.saktinocompose.network.dto.CmdbAssetData
import com.example.saktinocompose.viewmodel.CmdbViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmdbRelasiCIDialog(
    selectedRelasi: List<AsetData>,
    onDismiss: () -> Unit,
    onSave: (List<AsetData>) -> Unit,
    viewModel: CmdbViewModel = viewModel()
) {
    val assets by viewModel.filteredAssets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val tempSelectedRelasi = remember {
        mutableStateListOf<AsetData>().apply { addAll(selectedRelasi) }
    }

    var editingAssetId by remember { mutableStateOf<String?>(null) }
    var showTipeRelasiDropdown by remember { mutableStateOf(false) }

    val tipeRelasiOptions = listOf(
        "INSTALLED_ON",
        "DEPENDS_ON",
        "CONNECTED_TO",
        "RUNS_ON"
    )

    LaunchedEffect(Unit) {
        if (assets.isEmpty() && !isLoading) {
            viewModel.loadAssets()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
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
                            text = "${tempSelectedRelasi.size} dipilih dari CMDB",
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
                    onValueChange = { viewModel.searchAssets(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari aset untuk relasi...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
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
                                text = "Terpilih:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            tempSelectedRelasi.forEach { aset ->
                                SelectedRelasiCard(
                                    aset = aset,
                                    onEditType = {
                                        editingAssetId = aset.id
                                        showTipeRelasiDropdown = true
                                    },
                                    onRemove = {
                                        tempSelectedRelasi.removeAll { it.id == aset.id }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Error State
                error?.let { errorMsg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFD32F2F))
                            Text(errorMsg, fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Assets List
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF384E66))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(assets) { asset ->
                            // ✅ SKIP invalid assets
                            if (!asset.isValid()) return@items

                            val isSelected = tempSelectedRelasi.any { it.id == asset.kodeBmd }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isSelected && asset.kodeBmd != null) {
                                            tempSelectedRelasi.add(
                                                AsetData(
                                                    id = asset.kodeBmd,
                                                    nama = asset.namaAsset,
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
                                        asset?.kodeBmd?.let {
                                            Text(
                                                text = it,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = asset.namaAsset,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = Color.Black
                                        )
                                        asset.kategori?.let {
                                            Text(
                                                text = it,
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
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
    if (showTipeRelasiDropdown && editingAssetId != null) {
        AlertDialog(
            onDismissRequest = {
                showTipeRelasiDropdown = false
                editingAssetId = null
            },
            title = { Text("Pilih Tipe Relasi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tipeRelasiOptions.forEach { tipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val index = tempSelectedRelasi.indexOfFirst { it.id == editingAssetId }
                                    if (index != -1) {
                                        tempSelectedRelasi[index] = tempSelectedRelasi[index].copy(
                                            tipeRelasi = tipe
                                        )
                                    }
                                    showTipeRelasiDropdown = false
                                    editingAssetId = null
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
                    editingAssetId = null
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SelectedRelasiCard(
    aset: AsetData,
    onEditType: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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

            IconButton(
                onClick = onEditType,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit tipe",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF2196F3)
                )
            }

            IconButton(
                onClick = onRemove,
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