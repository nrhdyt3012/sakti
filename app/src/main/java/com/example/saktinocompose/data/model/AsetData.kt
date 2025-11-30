package com.example.saktinocompose.data.model

/**
 * Data class untuk Aset dengan ID otomatis
 */
/**
 * Data class untuk Aset dengan ID otomatis dan tipe relasi
 */
data class AsetData(
    val id: String,
    val nama: String,
    val tipeRelasi: String = "" // INSTALLED_ON, DEPENDS_ON, CONNECTED_TO, RUNS_ON
)

/**
 * Helper object untuk generate ID dan data aset
 */
object AsetHelper {

    // Map kategori aset ke prefix ID
    private val asetPrefixMap = mapOf(
        "Hardware Assets" to "APK",
        "Application/Service" to "APS",
        "OS/Build" to "OSB",
        "Network (switch/router/AP)" to "JRG",
        "Database/Instance" to "DBS",
        "Certificate" to "SRT",
        "VM/Container" to "VMC",
        "Endpoint" to "EPT"
    )

    // Counter untuk setiap kategori (dalam aplikasi nyata, ini harus disimpan di database)
    private val counterMap = mutableMapOf<String, Int>()

    /**
     * Generate ID untuk aset berdasarkan nama kategori
     */
    fun generateAsetId(namaAset: String): String {
        val prefix = asetPrefixMap[namaAset] ?: "AST"
        val counter = counterMap.getOrDefault(prefix, 0) + 1
        counterMap[prefix] = counter

        return "$prefix${counter.toString().padStart(3, '0')}"
    }

    /**
     * Get semua kategori aset
     */
    fun getAllAsetKategori(): List<String> {
        return asetPrefixMap.keys.toList()
    }

    /**
     * Parse related CI string menjadi list AsetData
     * Format input: "id1:nama1, id2:nama2, id3:nama3"
     */

    fun parseRelatedCI(relatedCIString: String): List<AsetData> {
        if (relatedCIString.isBlank()) return emptyList()

        return relatedCIString.split(",").mapNotNull { item ->
            val parts = item.trim().split(":")
            when {
                // Format BARU: "id:nama:tipeRelasi"
                parts.size >= 3 -> AsetData(
                    id = parts[0].trim(),
                    nama = parts[1].trim(),
                    tipeRelasi = parts[2].trim()
                )
                // Format LAMA: "id:nama" (backward compatibility)
                parts.size == 2 -> AsetData(
                    id = parts[0].trim(),
                    nama = parts[1].trim(),
                    tipeRelasi = "DEPENDS_ON" // Default tipe relasi untuk data lama
                )
                // Format invalid, skip
                else -> null
            }
        }
    }
    /**
     * Convert list AsetData menjadi string untuk disimpan
     * Format output: "id1:nama1, id2:nama2, id3:nama3"
     */
    fun formatRelatedCI(asetList: List<AsetData>): String {
        return asetList.joinToString(", ") { "${it.id}:${it.nama}:${it.tipeRelasi}" }
    }

    /**
     * Reset counter (untuk testing)
     */
    fun resetCounters() {
        counterMap.clear()
    }
}