// File: app/src/main/java/com/example/saktinocompose/repository/ScheduleRepository.kt
package com.example.saktinocompose.repository

import android.util.Log
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.ScheduleRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleRepository {

    /**
     * Submit schedule implementation ke server
     * POST /change-requests/{cr_id}/schedule
     */
    suspend fun scheduleImplementation(
        crId: String,
        tanggalImplementasi: String  // Format: "2025-11-30T07:26:24.580Z"
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

                val request = ScheduleRequest(tanggalImplementasi = tanggalImplementasi)

                Log.d("ScheduleRepo", """
                    📤 Scheduling Implementation:
                    - CR ID: $crId
                    - Date: $tanggalImplementasi
                """.trimIndent())

                val response = RetrofitClient.changeRequestService.scheduleImplementation(
                    id = crId,
                    request = request
                )

                Log.d("ScheduleRepo", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("ScheduleRepo", "✅ Schedule submitted successfully")
                    Result.Success(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = errorBody ?: "Failed to schedule implementation"
                    Log.e("ScheduleRepo", "❌ API Error: $errorMsg")

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
                Log.e("ScheduleRepo", "❌ Exception during scheduling", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }
}