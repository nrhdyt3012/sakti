// File: app/src/main/java/com/example/saktinocompose/teknisi/pages/CMDBSubKategoriCRListPage.kt
// ✅ NEW: Show Change Requests filtered by CMDB sub-kategori

package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import com.example.saktinocompose.viewmodel.CmdbViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMDBSubKategoriCRListPage(
    subKategori: String,
    teknisiId: String,
    teknisiName: String,
    onBackClick: () -> Unit,
    onDetailClick: (ChangeRequest) -> Unit,
    modifier: Modifier = Modifier,
    cmdbViewModel: CmdbViewModel = viewModel(),
    crViewModel: ChangeRequestViewModel = viewModel()
) {
    val allChangeRequests by crViewModel.getAllChangeRequests().collectAsState(initial = emptyList())
    val cmdbAssets by cmdbViewModel.assets.collectAsState()

    // ✅ Get all kode_bmd from this sub-kategori
    val assetIdsInSubKategori = remember(cmdbAssets, subKategori) {
        cmdbAssets
            .filter { it.subKategori == subKategori }
            .mapNotNull { it.kodeBmd }
            .toSet()
    }

    // ✅ Filter CRs that contain any asset from this sub-kategori
    val filteredRequests = remember(allChangeRequests, assetIdsInSubKategori) {
        allChangeRequests.filter { cr ->
            // Parse asetTerdampak - format bisa "kodeBmd" atau "kodeBmd:nama"
            val asetId = if (cr.asetTerdampak.contains(":")) {
                cr.asetTerdampak.split(":").getOrNull(0)?.trim()
            } else {
                cr.asetTerdampak.trim()
            }

            // Check if this CR's asset is in our sub-kategori
            asetId in assetIdsInSubKategori
        }.sortedByDescending { it.createdAt }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = subKategori,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Change Requests",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF384E66),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF384E66)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Change Requests",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${filteredRequests.size}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Requests",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Empty State
            if (filteredRequests.isEmpty()) {
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "No Data",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "No change requests for this category",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Change requests will appear here when assets from \"$subKategori\" are involved",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredRequests) { request ->
                        CMDBChangeRequestCard(
                            changeRequest = request,
                            onDetailClick = { onDetailClick(request) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CMDBChangeRequestCard(
    changeRequest: ChangeRequest,
    onDetailClick: () -> Unit
) {
    val statusColor = when (changeRequest.status) {
        "Submitted" -> Color(0xFF9E9E9E)
        "Need Approval" -> Color(0xFFFFA726)
        "Reviewed" -> Color(0xFF2196F3)
        "Revision" -> Color(0xFFFF9800)
        "Approved" -> Color(0xFF4CAF50)
        "Scheduled" -> Color(0xFFFF9800)
        "Implementing" -> Color(0xFFFF5722)
        "Completed" -> Color(0xFF4CAF50)
        "Failed" -> Color(0xFFD32F2F)
        "Closed" -> Color(0xFF607D8B)
        else -> Color.Gray
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val createdDate = try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = isoFormat.parse(changeRequest.createdAt)
        dateFormat.format(date ?: Date())
    } catch (e: Exception) {
        changeRequest.createdAt
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = changeRequest.ticketId,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor
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

            // Title
            Text(
                text = changeRequest.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Type:",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = getJenisPerubahanColor(changeRequest.jenisPerubahan)
                            .copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = changeRequest.jenisPerubahan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = getJenisPerubahanColor(changeRequest.jenisPerubahan),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Asset:",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = changeRequest.asetTerdampak,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Created:",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = createdDate,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = onDetailClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF37474F)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Detail",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Detail & Take Action", fontSize = 13.sp)
            }
        }
    }
}