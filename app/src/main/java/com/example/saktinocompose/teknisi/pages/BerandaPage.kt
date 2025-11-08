package com.example.saktinocompose.teknisi.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
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
    val allChangeRequests by viewModel.getAllChangeRequests().collectAsState(initial = emptyList())
    val context = LocalContext.current

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

    // Ambil 5 pengajuan terbaru
    val recentRequests = allChangeRequests.sortedByDescending { it.createdAt }.take(5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // Greeting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
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

        // Summary Section
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
            "In-Review" to Color(0xFF2196F3),
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

        // Recent Requests
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Pengajuan Terbaru",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black
//            )
//            Text(
//                text = "Maks 5 item",
//                fontSize = 12.sp,
//                color = Color.Gray
//            )
//        }

        Spacer(modifier = Modifier.height(12.dp))

//        if (recentRequests.isEmpty()) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 16.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(32.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Assessment,
//                        contentDescription = "No Data",
//                        modifier = Modifier.size(48.dp),
//                        tint = Color.Gray
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//                    Text(
//                        text = "Belum ada pengajuan",
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//                }
//            }
//        } else {
//            recentRequests.forEach { request ->
//                RecentRequestCard(
//                    changeRequest = request,
//                    onDetailClick = { onDetailClick(request) }
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//        }
//
//        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun RecentRequestCard(
    changeRequest: ChangeRequest,
    onDetailClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val createdDate = dateFormat.format(Date(changeRequest.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = changeRequest.ticketId,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (changeRequest.status) {
                            "Submitted" -> Color(0xFF9E9E9E)
                            "In-Review" -> Color(0xFF2196F3)
                            "Approved" -> Color(0xFF4CAF50)
                            else -> Color.Gray
                        }
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = changeRequest.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Jenis: ${changeRequest.jenisPerubahan}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Diajukan: $createdDate",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDetailClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF384E66)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Detail & Take Action", fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

// Enum untuk tipe filter Teknisi
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