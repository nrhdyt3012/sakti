// File: app/src/main/java/com/example/saktinocompose/teknisi/pages/CmdbAsetSearchDialog.kt
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
import com.example.saktinocompose.network.dto.CmdbAssetData
import com.example.saktinocompose.viewmodel.CmdbViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmdbAsetSearchDialog(
    selectedKodeBmd: String,
    onDismiss: () -> Unit,
    onSelect: (kodeBmd: String, namaAsset: String) -> Unit,
    viewModel: CmdbViewModel = viewModel()
) {
    val assets by viewModel.filteredAssets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        if (assets.isEmpty() && !isLoading) {
            viewModel.loadAssets()
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pilih Aset Terdampak",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dari CMDB Database",
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
                    placeholder = { Text("Cari kode BMD, nama asset, atau kategori...") },
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Error",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                                Text(
                                    errorMsg,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.clearError()
                                    viewModel.refresh()
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Loading State
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color(0xFF384E66)
                            )
                            Text("Memuat data aset...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
                // Empty State
                else if (assets.isEmpty() && error == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Text(
                                if (searchQuery.isBlank()) "Tidak ada data aset" else "Tidak ada hasil",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                // Assets List
                else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(assets) { asset ->
                            CmdbAssetCard(
                                asset = asset,
                                isSelected = asset.kodeBmd == selectedKodeBmd,
                                onClick = {
                                    asset.kodeBmd?.let { onSelect(it, asset.namaAsset) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CmdbAssetCard(
    asset: CmdbAssetData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // ✅ VALIDASI: Skip asset dengan kode_bmd null
    if (asset.kodeBmd.isNullOrBlank()) {
        return // Jangan render asset tanpa kode BMD
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            // Kode BMD Badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF384E66)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = asset.kodeBmd ?: "N/A",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Asset Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = asset.namaAsset,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 2
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    asset.kategori?.let { kategori ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = kategori,
                                fontSize = 10.sp,
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    asset.merkType?.let { merk ->
                        if (merk.isNotBlank()) {
                            Text(
                                text = merk,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }
                }

                asset.lokasi?.let { lokasi ->
                    if (lokasi.isNotBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = lokasi,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Selected Indicator
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