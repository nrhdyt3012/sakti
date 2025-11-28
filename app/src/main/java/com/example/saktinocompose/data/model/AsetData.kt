package com.example.saktinocompose.data.model

/**
 * Data class untuk Aset dengan ID otomatis
 */
data class AsetData(
    val id: String,
    val nama: String
)

/**
 * Helper object untuk generate ID dan data aset
 */
object AsetHelper {

    // Map kategori aset ke prefix ID
    private val asetPrefixMap = mapOf(
        "Aset Perangkat Keras" to "APK",
        "Aplikasi/Service" to "APS",
        "OS/Build" to "OSB",
        "Jaringan (switch/router/AP)" to "JRG",
        "Database/Instance" to "DBS",
        "Sertifikat" to "SRT",
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
            if (parts.size == 2) {
                AsetData(
                    id = parts[0].trim(),
                    nama = parts[1].trim()
                )
            } else null
        }
    }

    /**
     * Convert list AsetData menjadi string untuk disimpan
     * Format output: "id1:nama1, id2:nama2, id3:nama3"
     */
    fun formatRelatedCI(asetList: List<AsetData>): String {
        return asetList.joinToString(", ") { "${it.id}:${it.nama}" }
    }

    /**
     * Reset counter (untuk testing)
     */
    fun resetCounters() {
        counterMap.clear()
    }
}