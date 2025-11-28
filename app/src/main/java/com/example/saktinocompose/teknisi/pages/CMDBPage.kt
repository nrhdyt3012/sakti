package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel

data class CMDBCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun CMDBPage(
    onCategoryClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChangeRequestViewModel = viewModel()
) {
    val allChangeRequests by viewModel.getAllChangeRequests().collectAsState(initial = emptyList())

    val categories = listOf(
        CMDBCategory("Aset Perangkat Keras", Icons.Default.Computer, Color(0xFF2196F3)),
        CMDBCategory("Aplikasi/Service", Icons.Default.Apps, Color(0xFF4CAF50)),
        CMDBCategory("OS/Build", Icons.Default.Build, Color(0xFFFF9800)),
        CMDBCategory("Jaringan (switch/router/AP)", Icons.Default.Router, Color(0xFF9C27B0)),
        CMDBCategory("Database/Instance", Icons.Default.Storage, Color(0xFFE91E63)),
        CMDBCategory("Sertifikat", Icons.Default.Security, Color(0xFF00BCD4)),
        CMDBCategory("VM/Container", Icons.Default.CloudQueue, Color(0xFF673AB7)),
        CMDBCategory("Endpoint", Icons.Default.DeviceHub, Color(0xFF795548))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "CMDB - Configuration Items",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Kelola item konfigurasi berdasarkan kategori",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        categories.forEach { category ->
            // ✅ FIXED: Hitung dengan mempertimbangkan format "id:nama"
            val count = allChangeRequests.count { cr ->
                val asetNama = if (cr.asetTerdampak.contains(":")) {
                    cr.asetTerdampak.split(":").getOrNull(1)?.trim() ?: cr.asetTerdampak
                } else {
                    cr.asetTerdampak
                }
                asetNama == category.name
            }

            CMDBCategoryCard(
                category = category,
                count = count,
                onClick = { onCategoryClick(category.name) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun CMDBCategoryCard(
    category: CMDBCategory,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                Card(
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = category.color.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = category.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = category.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$count item",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.Gray
            )
        }
    }
}