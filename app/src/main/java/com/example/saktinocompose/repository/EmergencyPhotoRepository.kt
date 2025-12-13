// File: app/src/main/java/com/example/saktinocompose/repository/EmergencyPhotoRepository.kt
package com.example.saktinocompose.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.EmergencyPhotoUploadData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class EmergencyPhotoRepository {

    /**
     * Upload emergency photo to server
     * POST /emergency/photo
     */
    suspend fun uploadEmergencyPhoto(
        photoUri: Uri,
        context: Context
    ): Result<EmergencyPhotoUploadData> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                // Convert URI to File
                val photoFile = uriToFile(context, photoUri)
                    ?: return@withContext Result.Error(
                        Exception("File conversion failed"),
                        "Failed to process photo"
                    )

                // Create RequestBody
                val requestBody = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData(
                    "photo",
                    photoFile.name,
                    requestBody
                )

                Log.d("EmergencyPhotoRepo", """
                    📤 Uploading emergency photo:
                    - File: ${photoFile.name}
                    - Size: ${photoFile.length()} bytes
                """.trimIndent())

                // Upload to server
                val response = RetrofitClient.emergencyService.uploadEmergencyPhoto(photoPart)

                Log.d("EmergencyPhotoRepo", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data

                    if (data != null) {
                        Log.d("EmergencyPhotoRepo", "✅ Photo uploaded: ${data.url}")
                        Result.Success(data)
                    } else {
                        Result.Error(
                            Exception("No data in response"),
                            "Upload successful but no data returned"
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = response.body()?.message ?: errorBody ?: "Upload failed"
                    Log.e("EmergencyPhotoRepo", "❌ Upload error: $errorMsg")

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
                Log.e("EmergencyPhotoRepo", "❌ Exception during upload", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    /**
     * Convert URI to File
     */
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val tempFile = File.createTempFile(
                "emergency_photo_",
                ".jpg",
                context.cacheDir
            )

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            Log.e("EmergencyPhotoRepo", "Failed to convert URI to File", e)
            null
        }
    }
}