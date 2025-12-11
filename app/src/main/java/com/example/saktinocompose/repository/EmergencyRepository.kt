// File: app/src/main/java/com/example/saktinocompose/repository/EmergencyRepository.kt
package com.example.saktinocompose.repository

import android.util.Log
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.EmergencyRequest
import com.example.saktinocompose.network.dto.EmergencyData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencyRepository {

    suspend fun createEmergency(
        id: String,
        title: String,
        description: String,
        impactedAssets: List<String>,
        status: String,
        note: String
    ): Result<EmergencyData> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val request = EmergencyRequest(
                    id = id,
                    title = title,
                    description = description,
                    impactedAssets = impactedAssets,
                    status = status,
                    note = note
                )

                Log.d("EmergencyRepo", """
                    📤 Creating Emergency:
                    - ID: $id
                    - Title: $title
                    - Status: $status
                    - Assets: ${impactedAssets.size}
                """.trimIndent())

                val response = RetrofitClient.emergencyService.createEmergency(request)

                Log.d("EmergencyRepo", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    Log.d("EmergencyRepo", "✅ Emergency created: ${data.emergencyCode}")
                    Result.Success(data)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = response.body()?.message ?: errorBody ?: "Failed to create emergency"
                    Log.e("EmergencyRepo", "❌ API Error: $errorMsg")

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
                Log.e("EmergencyRepo", "❌ Exception", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }
}