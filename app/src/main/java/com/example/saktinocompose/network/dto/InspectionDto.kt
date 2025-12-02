package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

// InspectionDto.kt - DTO untuk Inspection
data class InspectionRequest(
    @SerializedName("jenis_perubahan")
    val jenisPerubahan: String,
    @SerializedName("alasan")
    val alasan: String,
    @SerializedName("tujuan")
    val tujuan: String,
    @SerializedName("ci_id")
    val ciId: String,
    @SerializedName("aset_terdampak_id")
    val asetTerdampakId: String,
    @SerializedName("rencana_implementasi")
    val rencanaImplementasi: String,
    @SerializedName("usulan_jadwal")
    val usulanJadwal: String, // ISO 8601 format
    @SerializedName("rencana_rollback")
    val rencanaRollback: String,
    @SerializedName("estimasi_biaya")
    val estimasiBiaya: Double,
    @SerializedName("estimasi_waktu")
    val estimasiWaktu: Double,
    @SerializedName("skor_dampak")
    val skorDampak: Int,
    @SerializedName("skor_kemungkinan")
    val skorKemungkinan: Int,
    @SerializedName("skor_exposure")
    val skorExposure: Int
)

/**
 * Data untuk build InspectionRequest dari ChangeRequest local
 */
data class InspectionData(
    val jenisPerubahan: String,
    val alasan: String,
    val tujuan: String,
    val ciId: String,
    val asetTerdampakId: String,
    val rencanaImplementasi: String,
    val usulanJadwal: String,
    val rencanaRollback: String,
    val estimasiBiaya: Double,
    val estimasiWaktu: Double,
    val skorDampak: Int,
    val skorKemungkinan: Int,
    val skorExposure: Int
) {
    fun toRequest(): InspectionRequest {
        return InspectionRequest(
            jenisPerubahan = jenisPerubahan,
            alasan = alasan,
            tujuan = tujuan,
            ciId = ciId,
            asetTerdampakId = asetTerdampakId,
            rencanaImplementasi = rencanaImplementasi,
            usulanJadwal = usulanJadwal,
            rencanaRollback = rencanaRollback,
            estimasiBiaya = estimasiBiaya,
            estimasiWaktu = estimasiWaktu,
            skorDampak = skorDampak,
            skorKemungkinan = skorKemungkinan,
            skorExposure = skorExposure
        )
    }
}
