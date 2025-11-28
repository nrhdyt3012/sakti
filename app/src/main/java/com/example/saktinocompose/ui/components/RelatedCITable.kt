package com.example.saktinocompose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.example.saktinocompose.data.model.AsetData
import com.example.saktinocompose.data.model.AsetHelper

/**
 * Komponen untuk menampilkan Related CI dalam bentuk tabel
 */
@Composable
fun RelatedCITable(
    relasiConfigurationItem: String,
    modifier: Modifier = Modifier
) {
    val relasiList = AsetHelper.parseRelatedCI(relasiConfigurationItem)

    if (relasiList.isEmpty()) {
        Text(
            text = "Tidak ada relasi CI",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = modifier
        )
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF384E66))
                    .padding(12.dp)
            ) {
                Text(
                    text = "ID Aset",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(0.3f)
                )
                Text(
                    text = "Nama Configuration Item",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(0.7f)
                )
            }

            // Data Rows
            relasiList.forEachIndexed { index, aset ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFE0E0E0)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) Color.White else Color(0xFFF5F5F5)
                        )
                        .padding(12.dp)
                ) {
                    // ID Column
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF384E66)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Text(
                            text = aset.id,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Nama Column
                    Text(
                        text = aset.nama,
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(0.7f)
                    )
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
    val id = if (parts.size == 2) parts[0] else ""
    val nama = if (parts.size == 2) parts[1] else asetTerdampak

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