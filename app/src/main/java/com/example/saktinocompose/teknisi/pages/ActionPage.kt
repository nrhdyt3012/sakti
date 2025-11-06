package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPage(
    modifier: Modifier = Modifier,
    filterStatus: String? = null,
    userId: Int = 0,
    userName: String = "",
    onReviewClick: (ChangeRequest) -> Unit = {},
    viewModel: ChangeRequestViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val allChangeRequests by viewModel.getAllChangeRequests().collectAsState(initial = emptyList())

    // Filter berdasarkan status jika ada
    val filteredByStatus = remember(allChangeRequests, filterStatus) {
        if (filterStatus != null) {
            allChangeRequests.filter { it.status == filterStatus }
        } else {
            allChangeRequests
        }
    }

    // Filter berdasarkan search query (ticket ID)
    val filteredItems = remember(filteredByStatus, searchQuery) {
        if (searchQuery.isBlank()) {
            filteredByStatus
        } else {
            filteredByStatus.filter {
                it.ticketId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Hitung jumlah per status dari semua requests
    val perluDitinjau = allChangeRequests.count { it.status == "Submitted" }
    val sedangDitinjau = allChangeRequests.count { it.status == "In-Review" }
    val selesaiDitinjau = allChangeRequests.count {
        it.status in listOf("Approved", "Scheduled", "Implementing", "Completed", "Closed")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(start = 16.dp, top = 40.dp, end = 16.dp)
    ) {
        // Filter indicator
        if (filterStatus != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when(filterStatus) {
                        "Submitted" -> Color(0xFFFF9800)
                        "In-Review" -> Color(0xFF2196F3)
                        else -> Color(0xFF4CAF50)
                    }
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
                    Text(
                        text = "Filter: $filterStatus",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredByStatus.size} item",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Status Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusCard("Perlu Ditinjau", perluDitinjau, Modifier.weight(1f))
            StatusCard("Sedang Ditinjau", sedangDitinjau, Modifier.weight(1f))
            StatusCard("Selesai Ditinjau", selesaiDitinjau, Modifier.weight(1f))
        }

        // Judul
        Text(
            text = "Tracking Permohonan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Bar + Tombol Submit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Masukkan Ticket ID") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF37474F),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Button(
                onClick = { /* Search automatically handled by remember */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF37474F)
                ),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Cari")
            }
        }

        // Daftar hasil
        if (filteredItems.isEmpty() && (filterStatus != null || searchQuery.isNotBlank())) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "No Data",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when {
                            searchQuery.isNotBlank() -> "Tidak ditemukan tiket dengan ID \"$searchQuery\""
                            filterStatus != null -> "Tidak ada data untuk status \"$filterStatus\""
                            else -> "Tidak ada data"
                        },
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (filteredItems.isNotEmpty()) {
            Text(
                text = if (filterStatus != null || searchQuery.isNotBlank()) {
                    "Hasil Pencarian (${filteredItems.size})"
                } else {
                    "Semua Permohonan (${filteredItems.size})"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
            ) {
                items(filteredItems) { changeRequest ->
                    ChangeRequestActionCard(
                        changeRequest = changeRequest,
                        onClick = { onReviewClick(changeRequest) }
                    )
                }
            }
        } else {
            // Tampilan awal: instruksi
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Info",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF37474F)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gunakan filter status atau cari ticket ID",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun StatusCard(title: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF263238)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChangeRequestActionCard(
    changeRequest: ChangeRequest,
    onClick: () -> Unit
) {
    val statusColor = when (changeRequest.status) {
        "Submitted" -> Color(0xFF9E9E9E)
        "In-Review" -> Color(0xFF2196F3)
        "Approved" -> Color(0xFF4CAF50)
        "Scheduled" -> Color(0xFFFF9800)
        "Implementing" -> Color(0xFFFF5722)
        "Completed" -> Color(0xFF4CAF50)
        "Failed" -> Color(0xFFD32F2F)
        "Closed" -> Color(0xFF607D8B)
        else -> Color.Gray
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val createdDate = dateFormat.format(Date(changeRequest.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = changeRequest.ticketId,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = changeRequest.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                modifier = Modifier,
                thickness = DividerDefaults.Thickness,
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Jenis:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = changeRequest.jenisPerubahan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Aset:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = changeRequest.asetTerdampak,
                    fontSize = 13.sp,
                    color = Color.Black,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dibuat:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = createdDate,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button Review
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF37474F)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Review Permohonan", fontSize = 14.sp)
            }
        }
    }
}
