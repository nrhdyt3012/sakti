package com.example.saktinocompose.repository

import android.util.Log
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.ImpactedAssetItem
import com.example.saktinocompose.network.dto.InspectionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InspectionRepository {

    /**
     * Submit inspection ke server
     * POST /change-requests/{id}/inspection
     */
    suspend fun submitInspection(
        crId: String,
        jenisPerubahan: String,
        alasan: String,
        tujuan: String,
        ciId: String,
        impactedAssets: List<String>,  // ✅ CHANGED: List of asset IDs
        rencanaImplementasi: String,
        usulanJadwal: String,
        rencanaRollback: String,
        skorDampak: Int,
        skorKemungkinan: Int,
        skorExposure: Int
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                // ✅ Convert List<String> to List<ImpactedAssetItem>
                val impactedAssetItems = impactedAssets.map {
                    ImpactedAssetItem(assetId = it)
                }

                val request = InspectionRequest(
                    jenisPerubahan = jenisPerubahan,
                    alasan = alasan,
                    tujuan = tujuan,
                    ciId = ciId,
                    impactedAssets = impactedAssetItems,  // ✅ CHANGED
                    rencanaImplementasi = rencanaImplementasi,
                    usulanJadwal = usulanJadwal,
                    rencanaRollback = rencanaRollback,
                    skorDampak = skorDampak,
                    skorKemungkinan = skorKemungkinan,
                    skorExposure = skorExposure
                )

                Log.d(
                    "InspectionRepo", """
                    📤 Submitting Inspection:
                    - CR ID: $crId
                    - Jenis: $jenisPerubahan
                    - Impacted Assets: ${impactedAssets.size} items
                    - Dampak: $skorDampak
                    - Kemungkinan: $skorKemungkinan
                    - Exposure: $skorExposure
                """.trimIndent()
                )

                val response = RetrofitClient.changeRequestService.submitInspection(
                    id = crId,
                    request = request
                )

                Log.d("InspectionRepo", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("InspectionRepo", "✅ Inspection submitted successfully")
                    Result.Success(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = errorBody ?: "Failed to submit inspection"
                    Log.e("InspectionRepo", "❌ API Error: $errorMsg")

                    if (response.code() == 401) {
                        RetrofitClient.clearAuthToken()
                        return@withContext Result.Error(
                            Exception("Token expired"),
                            "Session expired. Please login again."
                        )
                    }

                    Result.Error(Exception(errorMsg), errorMsg)
                }
            } catch (e: Exception) {
                Log.e("InspectionRepo", "❌ Exception during inspection", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }
}