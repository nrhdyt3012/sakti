// 1. Updated BerandaPage dengan Sync Button
// File: app/src/main/java/com/example/saktinocompose/teknisi/pages/BerandaPage.kt

package com.example.saktinocompose.teknisi.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.network.ApiConfig
import com.example.saktinocompose.utils.NetworkMonitor
import com.example.saktinocompose.utils.NetworkStatus
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BerandaPage(
    userName: String = "Teknisi",
    onDetailClick: (ChangeRequest) -> Unit = {},
    onFilterClick: (TeknisiFilterType, List<ChangeRequest>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: ChangeRequestViewModel = viewModel()
) {
    val context = LocalContext.current
    val allChangeRequestsRaw by viewModel.getAllChangeRequests().collectAsState(initial = emptyList())

    // ===== BARU: Sync State =====
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncError by viewModel.syncError.collectAsState()

    // ===== BARU: Network Status Monitor =====
    val networkMonitor = remember { NetworkMonitor(context) }
    val networkStatus by networkMonitor.observeNetworkStatus()
        .collectAsState(initial = NetworkStatus.Unavailable)

    val isOnline = networkStatus is NetworkStatus.Available && !ApiConfig.IS_OFFLINE_MODE

    val allChangeRequests = remember(allChangeRequestsRaw) {
        allChangeRequestsRaw.sortedByJenisPriority()
    }

    val thisMonth = remember {
        val calendar = Calendar.getInstance()
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
    }

    val thisWeek = remember {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.time
    }

    val monthlyRequests = allChangeRequests.filter { cr ->
        val date = Date(cr.createdAt)
        val dateMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
        dateMonth == thisMonth
    }

    val weeklyRequests = allChangeRequests.filter { cr ->
        val date = Date(cr.createdAt)
        date.after(thisWeek) || date == thisWeek
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // ===== BARU: Network Status Banner =====
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.1f)
                else Color(0xFFFF9800).copy(alpha = 0.1f)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (isOnline) "Online Mode" else "Offline Mode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                        Text(
                            text = if (isOnline) "Terhubung ke server" else "Menggunakan data lokal",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Sync Button (hanya muncul jika online)
                if (isOnline) {
                    IconButton(
                        onClick = {
                            if (!isSyncing) {
                                viewModel.syncFromApi()
                            }
                        },
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF4CAF50)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
        }

        // ===== BARU: Sync Error Message =====
        syncError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sync Error: $error",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearSyncError() }) {
                        Text("OK", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Greeting (sama seperti sebelumnya)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF384E66)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Hai, $userName!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Panel Teknisi - Manajemen Perubahan",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Summary Section (sama seperti sebelumnya)
        Text(
            text = "Summary Pengajuan (Semua User)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onFilterClick(TeknisiFilterType.MONTHLY, monthlyRequests)
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = monthlyRequests.size.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bulan Ini",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onFilterClick(TeknisiFilterType.WEEKLY, weeklyRequests)
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = weeklyRequests.size.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Minggu Ini",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Pengajuan (sama seperti sebelumnya)
        Text(
            text = "Status Pengajuan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        val statusCounts = allChangeRequests.groupingBy { it.status }.eachCount()
        val allStatuses = listOf(
            "Submitted" to Color(0xFF9E9E9E),
            "Reviewed" to Color(0xFF2196F3),
            "Revision" to Color(0xFFFF9800),
            "Approved" to Color(0xFF4CAF50),
            "Scheduled" to Color(0xFFFF9800),
            "Implementing" to Color(0xFFFF5722),
            "Completed" to Color(0xFF4CAF50),
            "Failed" to Color(0xFFD32F2F),
            "Closed" to Color(0xFF607D8B)
        )

        allStatuses.forEach { (status, color) ->
            val count = statusCounts[status] ?: 0
            val statusRequests = allChangeRequests.filter { it.status == status }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (count > 0) {
                            onFilterClick(TeknisiFilterType.STATUS(status), statusRequests)
                        } else {
                            Toast
                                .makeText(
                                    context,
                                    "Tidak ada pengajuan dengan status $status",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.size(12.dp),
                            colors = CardDefaults.cardColors(containerColor = color),
                            shape = RoundedCornerShape(50)
                        ) {}
                        Text(
                            text = status,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = count.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// Helper functions tetap sama
fun getJenisPerubahanColor(jenis: String): Color {
    return when (jenis) {
        "Emergency" -> Color(0xFFD32F2F)
        "Major" -> Color(0xFFFF9800)
        "Minor" -> Color(0xFF2196F3)
        "Standar" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

fun getJenisPriority(jenis: String): Int {
    return when (jenis) {
        "Emergency" -> 1
        "Major" -> 2
        "Minor" -> 3
        "Standar" -> 4
        else -> 5
    }
}

fun List<ChangeRequest>.sortedByJenisPriority(): List<ChangeRequest> {
    return this.sortedBy { getJenisPriority(it.jenisPerubahan) }
}

sealed class TeknisiFilterType {
    object MONTHLY : TeknisiFilterType()
    object WEEKLY : TeknisiFilterType()
    data class STATUS(val status: String) : TeknisiFilterType()

    fun getTitle(): String = when (this) {
        is MONTHLY -> "Pengajuan Bulan Ini"
        is WEEKLY -> "Pengajuan Minggu Ini"
        is STATUS -> "Status: ${this.status}"
    }
}