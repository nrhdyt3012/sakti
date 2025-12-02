// File: app/src/main/java/com/example/saktinocompose/network/dto/SiladanDto.kt
package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Request untuk create change request di Siladan
 * POST /integrations/siladan/change-requests
 *
 * Ini adalah integrasi dengan aplikasi eksternal (Siladan)
 *
 * Contoh request body:
 * {
 *   "tiket_id": "CR-20251130-0001",
 *   "katalog_permintaan": "Perubahan Infrastruktur",
 *   "asset_id": "AST-001",
 *   "judul": "Upgrade Switch Core Gedung A",
 *   "deskripsi": "Melakukan upgrade switch core..."
 * }
 */
data class SiladanChangeRequestRequest(
    @SerializedName("tiket_id")
    val tiketId: String, // Ticket ID dari sistem SAKTI

    @SerializedName("katalog_permintaan")
    val katalogPermintaan: String, // Kategori/katalog permintaan

    @SerializedName("asset_id")
    val assetId: String, // ID aset yang akan diubah

    @SerializedName("judul")
    val judul: String, // Judul change request

    @SerializedName("deskripsi")
    val deskripsi: String // Deskripsi lengkap change request
) {
        /**
         * Katalog permintaan yang tersedia
         */
        object Katalog {
            const val PERUBAHAN_INFRASTRUKTUR = "Perubahan Infrastruktur"
            const val PERUBAHAN_APLIKASI = "Perubahan Aplikasi"
            const val PERUBAHAN_DATABASE = "Perubahan Database"
            const val PERUBAHAN_NETWORK = "Perubahan Network"
            const val PERUBAHAN_SECURITY = "Perubahan Security"
            const val PERUBAHAN_HARDWARE = "Perubahan Hardware"
            const val PERUBAHAN_SOFTWARE = "Perubahan Software"
            const val LAINNYA = "Lainnya"

            fun getAllKatalog(): List<String> {
                return listOf(
                    PERUBAHAN_INFRASTRUKTUR,
                    PERUBAHAN_APLIKASI,
                    PERUBAHAN_DATABASE,
                    PERUBAHAN_NETWORK,
                    PERUBAHAN_SECURITY,
                    PERUBAHAN_HARDWARE,
                    PERUBAHAN_SOFTWARE,
                    LAINNYA
                )
            }
        }
}

/**
 * Response dari Siladan integration
 * Contoh response ketika berhasil:
 * {
 *   "status": "success",
 *   "message": "Change request berhasil dibuat di Siladan",
 *   "data": { ... }
 * }
 */
data class SiladanChangeRequestResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: SiladanChangeRequestData?
)

/**
 * Data hasil create di Siladan
 */
data class SiladanChangeRequestData(
    @SerializedName("siladan_id")
    val siladanId: String?, // ID dari sistem Siladan

    @SerializedName("tiket_id")
    val tiketId: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("siladan_url")
    val siladanUrl: String? // URL untuk akses di sistem Siladan
)

/**
 * Builder untuk SiladanChangeRequestRequest
 */
data class SiladanRequestBuilder(
    var tiketId: String = "",
    var katalogPermintaan: String = "",
    var assetId: String = "",
    var judul: String = "",
    var deskripsi: String = ""
) {
    fun build(): SiladanChangeRequestRequest {
        return SiladanChangeRequestRequest(
            tiketId = tiketId,
            katalogPermintaan = katalogPermintaan,
            assetId = assetId,
            judul = judul,
            deskripsi = deskripsi
        )
    }

    /**
     * Validate apakah semua field required sudah terisi
     */
    fun isValid(): Boolean {
        return tiketId.isNotBlank() &&
                katalogPermintaan.isNotBlank() &&
                assetId.isNotBlank() &&
                judul.isNotBlank() &&
                deskripsi.isNotBlank()
    }

    /**
     * Set katalog dari predefined list
     */
    fun withKatalog(katalog: String): SiladanRequestBuilder {
        this.katalogPermintaan = katalog
        return this
    }
}

/**
 * Helper untuk mapping dari ChangeRequest ke Siladan
 */
object SiladanMapper {
    /**
     * Map jenis perubahan ke katalog Siladan
     */
    fun mapJenisToKatalog(jenisPerubahan: String, type: String): String {
        return when (type.lowercase()) {
            "network" -> SiladanChangeRequestRequest.Katalog.PERUBAHAN_NETWORK
            "hardware" -> SiladanChangeRequestRequest.Katalog.PERUBAHAN_HARDWARE
            "software", "application" -> SiladanChangeRequestRequest.Katalog.PERUBAHAN_APLIKASI
            "database" -> SiladanChangeRequestRequest.Katalog.PERUBAHAN_DATABASE
            "security" -> SiladanChangeRequestRequest.Katalog.PERUBAHAN_SECURITY
            else -> SiladanChangeRequestRequest.Katalog.PERUBAHAN_INFRASTRUKTUR
        }
    }

    /**
     * Format deskripsi untuk Siladan
     */
    fun formatDeskripsi(
        alasan: String,
        tujuan: String,
        rencanaImplementasi: String,
        rencanaRollback: String
    ): String {
        return buildString {
            appendLine("=== ALASAN ===")
            appendLine(alasan)
            appendLine()
            appendLine("=== TUJUAN ===")
            appendLine(tujuan)
            appendLine()
            appendLine("=== RENCANA IMPLEMENTASI ===")
            appendLine(rencanaImplementasi)
            appendLine()
            appendLine("=== RENCANA ROLLBACK ===")
            appendLine(rencanaRollback)
        }
    }
}