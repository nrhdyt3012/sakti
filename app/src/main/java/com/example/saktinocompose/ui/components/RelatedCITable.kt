// File: app/src/main/java/com/example/saktinocompose/ui/components/RelatedCITable.kt
// ✅ FIXED: Horizontal Scroll untuk Aset Terdampak Display

package com.example.saktinocompose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log

// ✅ FIXED: Horizontal Scroll Version
@Composable
fun AsetTerdampakDisplay(
    asetTerdampak: String,
    modifier: Modifier = Modifier
) {
    if (asetTerdampak.isBlank()) {
        Text(
            text = "No impacted asset",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = modifier
        )
        return
    }

    // Parse multiple assets
    val assets = asetTerdampak.split(",").map { it.trim() }.filter { it.isNotBlank() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ✅ Hint untuk scroll horizontal
        if (assets.size > 2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.SwipeLeft,
                    contentDescription = "Swipe",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${assets.size} assets • Swipe to see more →",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ✅ FIXED: Horizontal Scrollable Container
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                assets.forEach { asset ->
                    AsetCard(asset = asset)
                }
            }
        }

        // ✅ Scroll indicator jika ada banyak item
        if (assets.size > 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(minOf(assets.size, 5)) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .padding(horizontal = 2.dp)
                            .background(
                                color = if (scrollState.value > index * 200)
                                    Color(0xFF384E66)
                                else
                                    Color.LightGray,
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun AsetCard(asset: String) {
    // Parse format "id:nama" atau "id"
    val parts = asset.split(":")
    val id = if (parts.isNotEmpty()) parts[0].trim() else asset
    val nama = if (parts.size > 1) parts[1].trim() else asset

    Card(
        modifier = Modifier
            .width(280.dp) // ✅ Fixed width untuk scroll horizontal
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF384E66).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (id.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF384E66)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = id,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nama,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
            }
        }
    }
}

// ✅ EXISTING: RelatedCITable with Horizontal Scroll (already good)
@Composable
fun RelatedCITable(
    relasiConfigurationItem: String,
    modifier: Modifier = Modifier
) {
    val ciList = parseRelatedCI(relasiConfigurationItem)

    if (ciList.isEmpty()) {
        Text(
            text = "No CI relationship",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ✅ Hint untuk scroll
        if (ciList.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.SwipeLeft,
                    contentDescription = "Swipe",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${ciList.size} CI relationships • Swipe to see more →",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth()
        ) {
            val totalWidth = 120.dp + 200.dp + 150.dp + 24.dp + 24.dp

            Card(
                modifier = Modifier.width(totalWidth),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                ) {
                    // HEADER
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF384E66))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Asset ID",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "CI Name",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.width(200.dp)
                        )
                        Text(
                            text = "Relationship Type",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    // ROW DATA
                    ciList.forEachIndexed { index, ci ->
                        if (index > 0) {
                            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                        }

                        Row(
                            modifier = Modifier
                                .background(if (index % 2 == 0) Color.White else Color(0xFFF5F5F5))
                                .padding(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF384E66)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text(
                                    text = ci.id,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = ci.nama,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.width(200.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = ci.tipeRelasi,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.width(150.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseRelatedCI(ciString: String): List<CIData> {
    if (ciString.isBlank()) return emptyList()

    return try {
        ciString.split(",").mapNotNull { item ->
            val trimmed = item.trim()
            if (trimmed.isBlank()) return@mapNotNull null

            val parts = trimmed.split(":")
            when {
                parts.size >= 3 -> CIData(
                    id = parts[0].trim(),
                    nama = parts[1].trim(),
                    tipeRelasi = parts[2].trim()
                )
                parts.size == 2 -> CIData(
                    id = parts[0].trim(),
                    nama = parts[1].trim(),
                    tipeRelasi = "DEPENDS_ON"
                )
                else -> CIData(
                    id = trimmed,
                    nama = trimmed,
                    tipeRelasi = "DEPENDS_ON"
                )
            }
        }
    } catch (e: Exception) {
        Log.e("RelatedCITable", "Error parsing CI relationship", e)
        emptyList()
    }
}

private data class CIData(
    val id: String,
    val nama: String,
    val tipeRelasi: String
)