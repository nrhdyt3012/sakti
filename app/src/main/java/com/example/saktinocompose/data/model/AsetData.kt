// File: app/src/main/java/com/example/saktinocompose/data/model/AsetData.kt
// ✅ FIXED: Support for CMDB ID + display fields

package com.example.saktinocompose.data.model

/**
 * ✅ FIXED: Model untuk aset dengan ID sebagai primary key
 */
data class AsetData(
    val id: String,              // ✅ UUID dari CMDB (untuk API calls)
    val nama: String,            // Nama aset
    val tipeRelasi: String = "", // DEPENDS_ON, INSTALLED_ON, etc.

    // ✅ NEW: Additional fields untuk display
    val kodeBmd: String? = null, // Kode BMD untuk display
    val merk: String? = null,    // Merk untuk display
    val model: String? = null    // Model untuk display
) {
    /**
     * ✅ Get display text with kode BMD
     */
    fun getDisplayText(): String {
        return if (!kodeBmd.isNullOrBlank()) {
            "$kodeBmd - $nama"
        } else {
            nama
        }
    }

    /**
     * ✅ Get merk/model info
     */
    fun getMerkModel(): String? {
        return when {
            !merk.isNullOrBlank() && !model.isNullOrBlank() -> "$merk $model"
            !merk.isNullOrBlank() -> merk
            !model.isNullOrBlank() -> model
            else -> null
        }
    }
}

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

    /**
     * ✅ FIXED: Parse related CI dari string
     * Format: "id:nama:tipeRelasi:kodeBmd" atau "id:nama:tipeRelasi"
     */
    fun parseRelatedCI(relatedCIString: String): List<AsetData> {
        if (relatedCIString.isBlank()) return emptyList()

        return relatedCIString.split(",").mapNotNull { item ->
            val parts = item.trim().split(":")
            when {
                parts.size >= 4 -> AsetData(
                    id = parts[0].trim(),
                    nama = parts[1].trim(),
                    tipeRelasi = parts[2].trim(),
                    kodeBmd = parts[3].trim()
                )
                parts.size == 3 -> AsetData(
                    id = parts[0].trim(),
                    nama = parts[1].trim(),
                    tipeRelasi = parts[2].trim()
                )
                parts.size == 2 -> AsetData(
                    id = parts[0].trim(),
                    nama = parts[1].trim(),
                    tipeRelasi = "DEPENDS_ON"
                )
                else -> null
            }
        }
    }

    /**
     * ✅ FIXED: Format related CI ke string
     * Format: "id:nama:tipeRelasi:kodeBmd"
     */
    fun formatRelatedCI(asetList: List<AsetData>): String {
        return asetList.joinToString(", ") { aset ->
            buildString {
                append("${aset.id}:${aset.nama}:${aset.tipeRelasi}")
                if (!aset.kodeBmd.isNullOrBlank()) {
                    append(":${aset.kodeBmd}")
                }
            }
        }
    }

    fun resetCounters() {
        counterMap.clear()
    }
}