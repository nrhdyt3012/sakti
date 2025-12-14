// File: app/src/main/java/com/example/saktinocompose/ui/components/RelatedCITable.kt
package com.example.saktinocompose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log

@Composable
fun RelatedCITable(
    relasiConfigurationItem: String,
    modifier: Modifier = Modifier
) {
    // ✅ FIXED: Handle berbagai format CI relationship
    val ciList = parseRelatedCI(relasiConfigurationItem)

    if (ciList.isEmpty()) {
        Text(
            text = "No CI relationship",
            fontSize= 14.sp,
        color = Color.Gray,
        modifier = modifier
        )
        return
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

/**
 * ✅ FIXED: Parse CI relationship dari berbagai format
 */
private fun parseRelatedCI(ciString: String): List<CIData> {
    if (ciString.isBlank()) return emptyList()

    return try {
        // Format bisa:
        // 1. Single: "BMD-TI-001"
        // 2. Multiple: "BMD-TI-001,BMD-NON-002"
        // 3. With details: "BMD-TI-001:Server:DEPENDS_ON"

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

/**
 * ✅ FIXED: Display Aset Terdampak dengan parsing yang benar
 */
@Composable
fun AsetTerdampakDisplay(
    asetTerdampak: String,
    modifier: Modifier = Modifier
) {
    if (asetTerdampak.isBlank()) {
        Text(
            text = "No impacted asset",
            fontSize= 14.sp,
        color = Color.Gray,
        modifier = modifier
        )
        return
    }

    // ✅ FIXED: Handle multiple assets
    val assets = asetTerdampak.split(",").map { it.trim() }.filter { it.isNotBlank() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        assets.forEach { asset ->
            // Parse format "id:nama" atau "id"
            val parts = asset.split(":")
            val id = if (parts.isNotEmpty()) parts[0].trim() else asset
            val nama = if (parts.size > 1) parts[1].trim() else asset

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                Text(
                    text = nama,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}