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

    @SerializedName("nama_aset")
    val namaAsset: String,  // ✅ NON-NULL (required)

    @SerializedName("merk_type")
    val merkType: String?,  // ✅ NULLABLE

    @SerializedName("kategori")
    val kategori: String?,  // ✅ NULLABLE

    @SerializedName("sub_kategori")
    val subKategori: String?,  // ✅ NULLABLE

    @SerializedName("kondisi")
    val kondisi: String?,  // ✅ NULLABLE

    @SerializedName("lokasi")
    val lokasi: String?,  // ✅ NULLABLE

    @SerializedName("penanggung_jawab")
    val penanggungJawab: String?,  // ✅ NULLABLE

    @SerializedName("tahun_perolehan")
    val tahunPerolehan: String?,  // ✅ NULLABLE

    @SerializedName("nilai_perolehan")
    val nilaiPerolehan: Double?,  // ✅ NULLABLE

    @SerializedName("status")
    val status: String?,  // ✅ NULLABLE

    @SerializedName("created_at")
    val createdAt: String?,  // ✅ NULLABLE

    @SerializedName("updated_at")
    val updatedAt: String?  // ✅ NULLABLE
) {
    /**
     * Convert ke AsetData untuk compatibility dengan existing code
     */
    fun toAsetData(tipeRelasi: String = ""): com.example.saktinocompose.data.model.AsetData {
        return com.example.saktinocompose.data.model.AsetData(
            id = kodeBmd ?: "UNKNOWN",  // ✅ Gunakan default jika null
            nama = namaAsset,
            tipeRelasi = tipeRelasi
        )
    }

    /**
     * Check if asset is valid (has required fields)
     */
    // ✅ CORRECT - Safe null check
    // ✅ BENAR - Explicit null check dulu
    fun isValid(): Boolean {
        // Check kodeBmd
        if (kodeBmd == null || kodeBmd.isBlank()) return false

        // Check namaAsset
        if (namaAsset.isBlank()) return false

        // Check subKategori - MUST check null first!
        if (subKategori == null || subKategori.isBlank()) return false

        return true
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
            append("${asset.kodeBmd ?: "N/A"} - ${asset.namaAsset}")
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
     * ✅ FIXED: Safe null handling
     */
    fun filterAssets(
        assets: List<CmdbAssetData>,
        query: String
    ): List<CmdbAssetData> {
        if (query.isBlank()) return assets

        val lowerQuery = query.lowercase()
        return assets.filter { asset ->
            asset.kodeBmd?.lowercase()?.contains(lowerQuery) == true ||
                    asset.namaAsset.lowercase().contains(lowerQuery) ||
                    asset.merkType?.lowercase()?.contains(lowerQuery) == true ||
                    asset.kategori?.lowercase()?.contains(lowerQuery) == true ||
                    asset.subKategori?.lowercase()?.contains(lowerQuery) == true
        }
    }

    /**
     * Group assets by category
     * ✅ FIXED: Safe null handling
     */
    fun groupByCategory(assets: List<CmdbAssetData>): Map<String, List<CmdbAssetData>> {
        return assets.groupBy { it.kategori ?: "Tidak ada kategori" }
    }
}