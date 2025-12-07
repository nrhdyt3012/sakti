package com.example.saktinocompose.data.model

data class AsetData(
    val id: String,
    val nama: String,
    val tipeRelasi: String = ""
)

object AsetHelper {
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

    private val counterMap = mutableMapOf<String, Int>()

    fun generateAsetId(namaAset: String): String {
        val prefix = asetPrefixMap[namaAset] ?: "AST"
        val counter = counterMap.getOrDefault(prefix, 0) + 1
        counterMap[prefix] = counter

        return "$prefix${counter.toString().padStart(3, '0')}"
    }

    fun getAllAsetKategori(): List<String> {
        return asetPrefixMap.keys.toList()
    }

    fun parseRelatedCI(relatedCIString: String): List<AsetData> {
        if (relatedCIString.isBlank()) return emptyList()

        return relatedCIString.split(",").mapNotNull { item ->
            val parts = item.trim().split(":")
            when {
                parts.size >= 3 -> AsetData(
                    id = parts[0].trim(),
                    nama = parts[1].trim(),
                    tipeRelasi = parts[2].trim()
                )
                parts.size == 2 -> AsetData(
                    id = parts[0].trim(),
                    nama = parts[1].trim(),
                    tipeRelasi = "DEPENDS_ON" // Default tipe relasi untuk data lama
                )
                else -> null
            }
        }
    }

    fun formatRelatedCI(asetList: List<AsetData>): String {
        return asetList.joinToString(", ") { "${it.id}:${it.nama}:${it.tipeRelasi}" }
    }

    fun resetCounters() {
        counterMap.clear()
    }
}