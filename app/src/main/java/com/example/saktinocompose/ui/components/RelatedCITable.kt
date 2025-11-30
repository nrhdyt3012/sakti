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
import com.example.saktinocompose.data.model.AsetHelper

@Composable
fun RelatedCITable(
    relasiConfigurationItem: String,
    modifier: Modifier = Modifier
) {
    val relasiList = AsetHelper.parseRelatedCI(relasiConfigurationItem)

    if (relasiList.isEmpty()) {
        Text(
            text = "No CI relationship",
            fontSize = 14.sp,
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
        // Total lebar tabel BIAR HEADER DAN ROW SAMA EXACT
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

                // ===== HEADER =====
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

                // ===== ROW DATA =====
                relasiList.forEachIndexed { index, aset ->
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
                                text = aset.id,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = aset.nama,
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.width(200.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = aset.tipeRelasi,
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
 * Komponen untuk menampilkan Aset Terdampak dengan ID
 */
@Composable
fun AsetTerdampakDisplay(
    asetTerdampak: String,
    modifier: Modifier = Modifier
) {
    // Parse format "id:nama"
    val parts = asetTerdampak.split(":")
    val id = if (parts.size == 3) parts[0] else ""
    val nama = if (parts.size == 3) parts[1] else asetTerdampak


    Row(
        modifier = modifier.fillMaxWidth(),
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
            fontWeight = FontWeight.Medium
        )
    }
}