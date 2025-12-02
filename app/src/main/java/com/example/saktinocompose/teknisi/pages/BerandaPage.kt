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
import com.example.saktinocompose.data.entity.ChangeRequest
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

    // ✅ Data dari ViewModel
    val allChangeRequestsRaw by viewModel.getAllChangeRequests()
        .collectAsState(initial = emptyList())

    // ✅ Loading & Error states
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // ✅ Pull-to-refresh state
    val refreshing by remember { derivedStateOf { isLoading } }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { viewModel.refreshData() }
    )
    // ✅ Show error untuk token invalid
    LaunchedEffect(error) {
        if (error?.contains("401") == true || error?.contains("Token") == true) {
            Toast.makeText(
                context,
                "Session expired. Please login again.",
                Toast.LENGTH_LONG
            ).show()
        }
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
        val date = Date(cr.createdAt)
        val dateMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
        dateMonth == thisMonth
    }

    val weeklyRequests = allChangeRequests.filter { cr ->
        val date = Date(cr.createdAt)
        date.after(thisWeek) || date == thisWeek
    }

    // ========================================
    // ✅ MULAI DARI SINI - BOX WRAPPER
    // ========================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState) // ← Pull refresh gesture
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // ✅ Error message jika ada
            // POPUP ERROR DIALOG
            if (error != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    },
                    title = {
                        Text(
                            text = "Error",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    },
                    text = {
                        Text(
                            text = error ?: "",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    },
                    containerColor = Color.White
                )
            }


            // ========================================
            // ✅ GREETING CARD (existing code tetap sama)
            // ========================================
            Card(
                modifier = Modifier.fillMaxWidth()
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

            // ========================================
            // ✅ SUMMARY SECTION (existing code tetap sama)
            // ========================================
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

            // ========================================
            // ✅ STATUS CHANGES (existing code tetap sama)
            // ========================================
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

        // ========================================
        // ✅ PULL REFRESH INDICATOR - DI SINI!
        // Letaknya di LUAR Column, tapi DALAM Box
        // Aligned ke TopCenter
        // ========================================
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = Color(0xFF384E66)
        )
    }
    // ========================================
    // ✅ AKHIR BOX WRAPPER
    // ========================================
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