// 1. Updated EnduserBerandaPage dengan Network Status
// File: app/src/main/java/com/example/saktinocompose/enduser/pages/EnduserBerandaPage.kt

package com.example.saktinocompose.enduser.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun EnduserBerandaPage(
    userId: Int,
    userName: String,
    onCreateFormClick: () -> Unit,
    onFilterClick: (FilterType, List<ChangeRequest>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: ChangeRequestViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val changeRequestsRaw by viewModel.getChangeRequestsByUser(userId).collectAsState(initial = emptyList())
    val context = LocalContext.current

    // ===== BARU: Network Status =====
    val networkMonitor = remember { NetworkMonitor(context) }
    val networkStatus by networkMonitor.observeNetworkStatus()
        .collectAsState(initial = NetworkStatus.Unavailable)

//    val isOnline = networkStatus is NetworkStatus.Available && !ApiConfig.IS_OFFLINE_MODE

    val changeRequests = remember(changeRequestsRaw) {
        changeRequestsRaw.sortedByJenisPriority()
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

    val monthlyRequests = changeRequests.filter { cr ->
        val date = Date(cr.createdAt)
        val dateMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
        dateMonth == thisMonth
    }

    val weeklyRequests = changeRequests.filter { cr ->
        val date = Date(cr.createdAt)
        date.after(thisWeek) || date == thisWeek
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ===== BARU: Network Status Indicator =====
//        if (!isOnline) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 70.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(12.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.CloudOff,
//                        contentDescription = null,
//                        tint = Color(0xFFFF9800),
//                        modifier = Modifier.size(20.dp)
//                    )
//                    Column {
//                        Text(
//                            text = "Mode Offline",
//                            fontSize = 13.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFFFF9800)
//                        )
//                        Text(
//                            text = "Data akan disinkronkan saat online",
//                            fontSize = 11.sp,
//                            color = Color.Gray
//                        )
//                    }
//                }
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//        }

        // Greeting Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top =  0.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF384E66)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hai, $userName!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selamat datang di Sistem Manajemen Perubahan",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // ===== BARU: Online Indicator Icon =====
//                    if (isOnline) {
//                        Icon(
//                            imageVector = Icons.Default.CloudDone,
//                            contentDescription = "Online",
//                            tint = Color(0xFF4CAF50),
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Summary Section (sama seperti sebelumnya)
        Text(
            text = "Summary Perubahan",
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
                        onFilterClick(FilterType.MONTHLY, monthlyRequests)
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
                        onFilterClick(FilterType.WEEKLY, weeklyRequests)
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
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Perubahan (sama seperti sebelumnya)
        Text(
            text = "Status Perubahan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        val statusCounts = changeRequests.groupingBy { it.status }.eachCount()
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
            val statusRequests = changeRequests.filter { it.status == status }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (count > 0) {
                            onFilterClick(FilterType.STATUS(status), statusRequests)
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

        Spacer(modifier = Modifier.height(24.dp))

        // Create Button
        Button(
            onClick = onCreateFormClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF384E66)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Buat Pengajuan Baru",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
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

sealed class FilterType {
    object MONTHLY : FilterType()
    object WEEKLY : FilterType()
    data class STATUS(val status: String) : FilterType()

    fun getTitle(): String = when (this) {
        is MONTHLY -> "Pengajuan Bulan Ini"
        is WEEKLY -> "Pengajuan Minggu Ini"
        is STATUS -> "Status: ${this.status}"
    }
}