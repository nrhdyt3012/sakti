package com.example.saktinocompose.enduser.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TrackingItem(
    val username: String,
    val status: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPage2(
    modifier: Modifier = Modifier,
    filterStatus: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var filteredItems by remember { mutableStateOf<List<TrackingItem>>(emptyList()) }

    // Data dummy
    val allItems = remember {
        listOf(
            TrackingItem("user001", "Perlu Ditinjau", "2024-01-15 10:30"),
            TrackingItem("user002", "Sedang Ditinjau", "2024-01-15 11:00"),
            TrackingItem("user003", "Selesai Ditinjau", "2024-01-15 12:15"),
            TrackingItem("user004", "Perlu Ditinjau", "2024-01-15 13:20"),
            TrackingItem("user005", "Sedang Ditinjau", "2024-01-15 14:45"),
            TrackingItem("admin", "Perlu Ditinjau", "2024-01-15 15:00"),
            TrackingItem("john_doe", "Selesai Ditinjau", "2024-01-15 16:30"),
            TrackingItem("jane_smith", "Perlu Ditinjau", "2024-01-15 17:00")
        )
    }

    // Hitung jumlah per status
    val perluDitinjau = allItems.count { it.status == "Perlu Ditinjau" }
    val sedangDitinjau = allItems.count { it.status == "Sedang Ditinjau" }
    val selesaiDitinjau = allItems.count { it.status == "Selesai Ditinjau" }

    // Apply filter from Database page or search
    LaunchedEffect(searchQuery, filterStatus) {
        filteredItems = when {
            // Jika ada filter status dari Database page
            filterStatus != null && searchQuery.isBlank() -> {
                allItems.filter { it.status == filterStatus }
            }
            // Jika user melakukan pencarian
            searchQuery.isNotBlank() -> {
                val baseFilter = if (filterStatus != null) {
                    allItems.filter { it.status == filterStatus }
                } else {
                    allItems
                }
                baseFilter.filter {
                    it.username.contains(searchQuery, ignoreCase = true)
                }
            }
            // Jika tidak ada filter sama sekali
            else -> emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(start = 16.dp, top = 40.dp, end = 16.dp)
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when(filterStatus) {
                    "Perlu Ditinjau" -> Color(0xFFFF9800)
                    "Sedang Ditinjau" -> Color(0xFF2196F3)
                    "Selesai Ditinjau" -> Color(0xFF4CAF50)
                    else -> Color.Gray
                }
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Filter: $filterStatus",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp)
            )
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
            text = "Tracking Status",
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
                placeholder = { Text("Masukkan Username") },
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
                onClick = {
                    // Trigger filter ulang dengan searchQuery saat ini
                    val baseFilter = if (filterStatus != null) {
                        allItems.filter { it.status == filterStatus }
                    } else {
                        allItems
                    }

                    filteredItems = if (searchQuery.isBlank()) {
                        if (filterStatus != null) {
                            baseFilter
                        } else {
                            emptyList()
                        }
                    } else {
                        baseFilter.filter {
                            it.username.contains(searchQuery, ignoreCase = true)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF37474F)
                ),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Submit")
            }
        }

        // Daftar hasil pencarian (scrollable penuh)
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 12.dp)
        ) {
            if (filteredItems.isNotEmpty()) {
                item {
                    Text(
                        text = if (filterStatus != null) {
                            "Hasil untuk \"$filterStatus\""
                        } else {
                            "Hasil Pencarian"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(filteredItems.size) { index ->
                    TrackingResultCard(filteredItems[index])
                }
            } else if (filterStatus != null || searchQuery.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when {
                                searchQuery.isNotBlank() -> "Tidak ada data untuk username \"$searchQuery\""
                                filterStatus != null -> "Tidak ada data untuk status \"$filterStatus\""
                                else -> "Tidak ada data"
                            },
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
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
fun TrackingResultCard(item: TrackingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Username:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = item.username,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = item.status,
                    fontSize = 14.sp,
                    color = when(item.status) {
                        "Perlu Ditinjau" -> Color(0xFFFF9800)
                        "Sedang Ditinjau" -> Color(0xFF2196F3)
                        "Selesai Ditinjau" -> Color(0xFF4CAF50)
                        else -> Color.Black
                    },
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Waktu:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = item.timestamp,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}