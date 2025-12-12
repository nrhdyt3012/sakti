// File: app/src/main/java/com/example/saktinocompose/repository/InspectionPhotoRepository.kt
package com.example.saktinocompose.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class InspectionPhotoRepository {

    /**
     * Upload photo to server
     */
    suspend fun uploadInspectionPhoto(
        crId: String,
        photoUri: Uri,
        context: Context
    ): Result<String> {
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

                Log.d("InspectionPhotoRepo", """
                    📤 Uploading photo:
                    - CR ID: $crId
                    - File: ${photoFile.name}
                    - Size: ${photoFile.length()} bytes
                """.trimIndent())

                // Upload to server
                val response = RetrofitClient.changeRequestService.uploadInspectionPhoto(
                    crId = crId,
                    photo = photoPart
                )

                Log.d("InspectionPhotoRepo", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val photoUrl = response.body()!!.data?.url

                    if (photoUrl != null) {
                        Log.d("InspectionPhotoRepo", "✅ Photo uploaded: $photoUrl")
                        Result.Success(photoUrl)
                    } else {
                        Result.Error(
                            Exception("No URL in response"),
                            "Upload successful but no URL returned"
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = response.body()?.message ?: errorBody ?: "Upload failed"
                    Log.e("InspectionPhotoRepo", "❌ Upload error: $errorMsg")

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
                Log.e("InspectionPhotoRepo", "❌ Exception during upload", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    /**
     * Convert URI to File
     */
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            // Create temp file
            val tempFile = File.createTempFile(
                "inspection_photo_",
                ".jpg",
                context.cacheDir
            )

            // Copy from URI to temp file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            Log.e("InspectionPhotoRepo", "Failed to convert URI to File", e)
            null
        }
    }
}