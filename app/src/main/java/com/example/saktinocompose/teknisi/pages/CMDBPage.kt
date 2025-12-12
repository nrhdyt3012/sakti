// File: app/src/main/java/com/example/saktinocompose/teknisi/pages/CMDBPage.kt
// ✅ UPDATED: Using real CMDB data from API with sub_kategori

package com.example.saktinocompose.teknisi.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.utils.NetworkHelper
import com.example.saktinocompose.viewmodel.CmdbViewModel

@Composable
fun CMDBPage(
    onCategoryClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CmdbViewModel = viewModel()
) {
    val context = LocalContext.current
    val assets by viewModel.assets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // ✅ CHECK INTERNET CONNECTION
    var isOnline by remember { mutableStateOf(NetworkHelper.isInternetAvailable(context)) }

    // ✅ Monitor connection
    LaunchedEffect(Unit) {
        while (true) {
            isOnline = NetworkHelper.isInternetAvailable(context)
            kotlinx.coroutines.delay(3000)
        }
    }

    // ✅ Group assets by sub_kategori
    val subKategoriGroups = remember(assets) {
        assets
            .filter { it.subKategori != null && it.subKategori.isNotBlank() }
            .groupBy { it.subKategori!! }
            .toSortedMap()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // ✅ HEADER WITH CONNECTION STATUS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CMDB Assets by Sub-Category",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .fillMaxWidth()
                )
                Text(
                    text = "Manage change requests by asset sub-category",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ OFFLINE WARNING
        if (!isOnline) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
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
                        Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            "No Internet Connection",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Data may not be up to date. Connect to refresh.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // ✅ ERROR STATE
        if (error != null && isOnline) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Error Loading Data",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            error ?: "",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        }

        // ✅ LOADING STATE
        if (isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        "Loading CMDB assets...",
                        fontSize = 14.sp,
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }

        // ✅ EMPTY STATE
        if (subKategoriGroups.isEmpty() && !isLoading) {
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
                        Icons.Default.Inventory,
                        contentDescription = "No Data",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Text(
                        "No CMDB assets available",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    if (isOnline) {
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF384E66)
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh")
                        }
                    }
                }
            }
        }

        // ✅ SUB-CATEGORIES LIST
        subKategoriGroups.entries.forEachIndexed { index, entry ->
            val subKategori = entry.key
            val assetsInCategory = entry.value

            SubKategoriCard(
                number = index + 1,
                subKategori = subKategori,
                assetCount = assetsInCategory.size,
                isOnline = isOnline,
                onClick = {
                    if (isOnline || assetsInCategory.isNotEmpty()) {
                        onCategoryClick(subKategori)
                    } else {
                        Toast.makeText(
                            context,
                            "No internet connection. Cannot load category data.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }


        Spacer(modifier = Modifier.height(80.dp))
    }
}
@Composable
fun SubKategoriCard(
    number: Int,
    subKategori: String,
    assetCount: Int,
    isOnline: Boolean,
    onClick: () -> Unit
) {
    // ✅ Get color based on sub-kategori
    val color = when {
        subKategori.contains("Hardware", ignoreCase = true) -> Color(0xFF2196F3)
        subKategori.contains("Application", ignoreCase = true) ||
                subKategori.contains("Software", ignoreCase = true) -> Color(0xFF4CAF50)
        subKategori.contains("Network", ignoreCase = true) -> Color(0xFF9C27B0)
        subKategori.contains("Database", ignoreCase = true) -> Color(0xFFE91E63)
        subKategori.contains("Server", ignoreCase = true) -> Color(0xFFFF9800)
        subKategori.contains("Security", ignoreCase = true) -> Color(0xFF00BCD4)
        else -> Color(0xFF607D8B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline || assetCount > 0)
                Color.White
            else
                Color.White.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Number Badge
                Card(
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = color.copy(
                            alpha = if (isOnline || assetCount > 0) 0.1f else 0.05f
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = color.copy(
                                alpha = if (isOnline || assetCount > 0) 1f else 0.5f
                            )
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subKategori,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOnline || assetCount > 0)
                            Color.Black
                        else
                            Color.Gray,
                        maxLines = 2
                    )
                    if (!isOnline && assetCount == 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = "Offline",
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = "Offline",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Arrow icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = if (isOnline || assetCount > 0)
                    Color.Gray
                else
                    Color.LightGray
            )
        }
    }
}