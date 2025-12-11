// File: app/src/main/java/com/example/saktinocompose/network/dto/CmdbDto.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Response dari GET /cmdb/assets
 */
data class CmdbAssetsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<CmdbAssetData>?
)

/**
 * Data aset dari CMDB
 */
data class CmdbAssetData(
    @SerializedName("kode_bmd")
    val kodeBmd: String?,  // ✅ NULLABLE

    @SerializedName("nama_asset")
    val namaAsset: String,

    @SerializedName("merk_type")
    val merkType: String?,

    @SerializedName("kategori")
    val kategori: String?,

    @SerializedName("sub_kategori")
    val subKategori: String?,

    @SerializedName("kondisi")
    val kondisi: String?,

    @SerializedName("lokasi")
    val lokasi: String?,

    @SerializedName("penanggung_jawab")
    val penanggungJawab: String?,

    @SerializedName("tahun_perolehan")
    val tahunPerolehan: String?,

    @SerializedName("nilai_perolehan")
    val nilaiPerolehan: Double?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
) {
    /**
     * Convert ke AsetData untuk compatibility dengan existing code
     */
    fun toAsetData(tipeRelasi: String = ""): com.example.saktinocompose.data.model.AsetData {
        return com.example.saktinocompose.data.model.AsetData(
            id = kodeBmd ?: "N/A",  // ✅ HANDLE NULL
            nama = namaAsset,
            tipeRelasi = tipeRelasi
        )
    }

    /**
     * Check if asset is valid (has required fields)
     */
    fun isValid(): Boolean {
        return !kodeBmd.isNullOrBlank() && namaAsset.isNotBlank()
    }
}

/**
 * Helper untuk format display
 */
object CmdbAssetHelper {
    fun formatAssetDisplay(asset: CmdbAssetData): String {
        return buildString {
            append(asset.namaAsset)
            asset.merkType?.let {
                if (it.isNotBlank()) append(" - $it")
            }
        }
    }

    fun formatAssetDetail(asset: CmdbAssetData): String {
        return buildString {
            append("${asset.kodeBmd} - ${asset.namaAsset}")
            asset.kategori?.let {
                if (it.isNotBlank()) append(" | $it")
            }
            asset.lokasi?.let {
                if (it.isNotBlank()) append(" | Lokasi: $it")
            }
        }
    }

    /**
     * Filter assets by search query
     */
    fun filterAssets(
        assets: List<CmdbAssetData>,
        query: String
    ): List<CmdbAssetData> {
        if (query.isBlank()) return assets

        val lowerQuery = query.lowercase()
        return assets.filter {
            it.kodeBmd?.lowercase()!!.contains(lowerQuery) ||
                    it.namaAsset.lowercase().contains(lowerQuery) ||
                    it.merkType?.lowercase()?.contains(lowerQuery) == true ||
                    it.kategori?.lowercase()?.contains(lowerQuery) == true
        }
    }

    /**
     * Group assets by category
     */
    fun groupByCategory(assets: List<CmdbAssetData>): Map<String, List<CmdbAssetData>> {
        return assets.groupBy { it.kategori ?: "Tidak ada kategori" }
    }
}