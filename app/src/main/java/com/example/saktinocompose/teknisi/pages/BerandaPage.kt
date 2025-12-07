// File: teknisi/pages/BerandaPage.kt
// Complete implementation dengan Pull-to-Refresh

package com.example.saktinocompose.teknisi.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.ui.components.NoDataCard
import com.example.saktinocompose.utils.NetworkHelper
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BerandaPage(
    userName: String = "Technician",
    onDetailClick: (ChangeRequest) -> Unit = {},
    onFilterClick: (TeknisiFilterType, List<ChangeRequest>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: ChangeRequestViewModel = viewModel()
) {
    val context = LocalContext.current
    val allChangeRequestsRaw by viewModel.getAllChangeRequests().collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val refreshing by remember { derivedStateOf { isLoading } }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            if (!NetworkHelper.isInternetAvailable(context)) {
                Toast.makeText(
                    context,
                    "No internet connection. Please connect to internet.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                viewModel.refreshData()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (allChangeRequestsRaw.isEmpty() && !isLoading) {
            if (NetworkHelper.isInternetAvailable(context)) {
                viewModel.refreshData()
            }
        }
    }

    LaunchedEffect(error) {
        error?.let { errorMsg ->
            val message = when {
                errorMsg.contains("Network error") || errorMsg.contains("No internet") ->
                    "No internet connection"
                errorMsg.contains("401") || errorMsg.contains("Token") || errorMsg.contains("Session") ->
                    "Session expired. Please login again."
                errorMsg.contains("timeout") ->
                    "Connection timeout. Please try again."
                else -> errorMsg
            }

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ✅ TAMBAHKAN PADDING TOP DI SINI
            Spacer(modifier = Modifier.height(80.dp)) // Ubah dari 50.dp ke 80.dp atau sesuai kebutuhan

            // ✅ ERROR CARD DENGAN PADDING TOP LEBIH BESAR
            if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,  // ✅ Tambah padding top
                            bottom = 16.dp
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Error",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = error ?: "",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }

            // ✅ Empty State
            if (allChangeRequestsRaw.isEmpty() && !isLoading) {
                NoDataCard(
                    onRefresh = {
                        if (NetworkHelper.isInternetAvailable(context)) {
                            viewModel.refreshData()
                        } else {
                            Toast.makeText(
                                context,
                                "No internet connection",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                return@Column
            }

            // Process data
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
                try {
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = isoFormat.parse(cr.createdAt)
                    val dateMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date ?: Date())
                    dateMonth == thisMonth
                } catch (e: Exception) {
                    false
                }
            }

            val weeklyRequests = allChangeRequests.filter { cr ->
                try {
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = isoFormat.parse(cr.createdAt)
                    date != null && (date.after(thisWeek) || date == thisWeek)
                } catch (e: Exception) {
                    false
                }
            }

            // Greeting Card
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
                        text = "Hi, $userName!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Technician Panel - Change Management",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Summary Section
            Text(
                text = "Request Summary (All Users)",
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
                            text = "This Month",
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
                            text = "This Week",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status Changes
            Text(
                text = "Status Changes",
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
                                        "No requests with status $status",
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

        // Pull Refresh Indicator
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = Color(0xFF384E66)
        )
    }
}

// ========================================
// ✅ HELPER FUNCTIONS (existing code tetap sama)
// ========================================

fun getJenisPerubahanColor(jenis: String): Color {
    return when (jenis) {
        "Emergency" -> Color(0xFFD32F2F)
        "Major" -> Color(0xFFFF9800)
        "Minor" -> Color(0xFF2196F3)
        "Standard" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

fun getJenisPriority(jenis: String): Int {
    return when (jenis) {
        "Emergency" -> 1
        "Major" -> 2
        "Minor" -> 3
        "Standard" -> 4
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
        is MONTHLY -> "Monthly Requests"
        is WEEKLY -> "Weekly Requests"
        is STATUS -> "Status: ${this.status}"
    }
}