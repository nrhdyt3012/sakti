// File: app/src/main/java/com/example/saktinocompose/network/dto/CmdbDto.kt
// ✅ FIXED: Using 'id' as primary identifier for API calls

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
 * ✅ FIXED: Data aset dari CMDB dengan ID sebagai primary key
 */
data class CmdbAssetData(
    // ✅ PRIMARY KEY - Digunakan untuk API calls (impacted_assets, ci_id)
    @SerializedName("id")
    val id: String,  // UUID dari database

    // ✅ DISPLAY DATA - Untuk UI
    @SerializedName("kode_bmd")
    val kodeBmd: String?,

    @SerializedName("nama_aset")
    val namaAsset: String,

    @SerializedName("merk")
    val merk: String?,

    @SerializedName("model")
    val model: String?,

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

    @SerializedName("tanggal_perolehan")
    val tanggalPerolehan: String?,

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
     * ✅ FIXED: Convert ke AsetData menggunakan ID sebagai primary key
     */
    fun toAsetData(tipeRelasi: String = ""): com.example.saktinocompose.data.model.AsetData {
        return com.example.saktinocompose.data.model.AsetData(
            id = id,  // ✅ CHANGED: Use UUID id, not kode_bmd
            nama = namaAsset,
            tipeRelasi = tipeRelasi,
            kodeBmd = kodeBmd,  // ✅ NEW: Simpan kode_bmd untuk display
            merk = merk,
            model = model
        )
    }

    /**
     * ✅ Check if asset is valid (has required fields)
     */
    fun isValid(): Boolean {
        // Check id (primary key)
        if (id.isBlank()) return false

        // Check nama_aset (required for display)
        if (namaAsset.isBlank()) return false

        return true
    }

    /**
     * ✅ Get display name with kode BMD
     */
    fun getDisplayName(): String {
        return if (!kodeBmd.isNullOrBlank()) {
            "$kodeBmd - $namaAsset"
        } else {
            namaAsset
        }
    }

    /**
     * ✅ Get merk/type info
     */
    val merkType: String?
        get() = when {
            !merk.isNullOrBlank() && !model.isNullOrBlank() -> "$merk $model"
            !merk.isNullOrBlank() -> merk
            !model.isNullOrBlank() -> model
            else -> null
        }
}

/**
 * Helper untuk format display
 */
object CmdbAssetHelper {
    /**
     * Format asset untuk display di UI
     */
    fun formatAssetDisplay(asset: CmdbAssetData): String {
        return buildString {
            append(asset.namaAsset)
            asset.merkType?.let {
                if (it.isNotBlank()) append(" - $it")
            }
        }
    }

    /**
     * Format asset detail lengkap
     */
    fun formatAssetDetail(asset: CmdbAssetData): String {
        return buildString {
            // Tampilkan kode BMD jika ada
            if (!asset.kodeBmd.isNullOrBlank()) {
                append(asset.kodeBmd)
                append(" - ")
            }

            append(asset.namaAsset)

            asset.kategori?.let {
                if (it.isNotBlank()) append(" | $it")
            }

            asset.lokasi?.let {
                if (it.isNotBlank()) append(" | Lokasi: $it")
            }
        }
    }

    /**
     * ✅ FIXED: Filter assets by search query
     */
    fun filterAssets(
        assets: List<CmdbAssetData>,
        query: String
    ): List<CmdbAssetData> {
        if (query.isBlank()) return assets

        val lowerQuery = query.lowercase()
        return assets.filter { asset ->
            asset.id.lowercase().contains(lowerQuery) ||
                    asset.kodeBmd?.lowercase()?.contains(lowerQuery) == true ||
                    asset.namaAsset.lowercase().contains(lowerQuery) ||
                    asset.merkType?.lowercase()?.contains(lowerQuery) == true ||
                    asset.kategori?.lowercase()?.contains(lowerQuery) == true ||
                    asset.subKategori?.lowercase()?.contains(lowerQuery) == true
        }
    }

    /**
     * ✅ Group assets by category
     */
    fun groupByCategory(assets: List<CmdbAssetData>): Map<String, List<CmdbAssetData>> {
        return assets.groupBy { it.kategori ?: "Tidak ada kategori" }
    }

    /**
     * ✅ Group assets by sub-category
     */
    fun groupBySubCategory(assets: List<CmdbAssetData>): Map<String, List<CmdbAssetData>> {
        return assets.groupBy { it.subKategori ?: "Tidak ada sub-kategori" }
    }
}