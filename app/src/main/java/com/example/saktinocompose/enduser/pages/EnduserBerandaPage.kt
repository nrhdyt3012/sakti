package com.example.saktinocompose.enduser.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EnduserBerandaPage(
    userId: Int,
    userName: String,
    onCreateFormClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChangeRequestViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val changeRequests by viewModel.getChangeRequestsByUser(userId).collectAsState(initial = emptyList())

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

    val monthlyCount = changeRequests.count { cr ->
        val date = Date(cr.createdAt)
        val dateMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
        dateMonth == thisMonth
    }

    val weeklyCount = changeRequests.count { cr ->
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

        // Greeting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 70.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F)),
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
                    text = "Selamat datang di Sistem Manajemen Perubahan",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Summary Section
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
                modifier = Modifier.weight(1f),
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
                        text = monthlyCount.toString(),
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
                modifier = Modifier.weight(1f),
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
                        text = weeklyCount.toString(),
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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

        Button(
            onClick = onCreateFormClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF37474F)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(24.dp)
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