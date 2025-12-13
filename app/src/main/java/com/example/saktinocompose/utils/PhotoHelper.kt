// File: app/src/main/java/com/example/saktinocompose/utils/PhotoHelper.kt
package com.example.saktinocompose.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ Helper functions untuk photo operations
 * Accessible dari berbagai file
 */
object PhotoHelper {

    /**
     * Load bitmap from URI
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Create temporary image file
     */
    fun createImageFile(context: Context, prefix: String = "PHOTO"): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        return File.createTempFile("${prefix}_${timeStamp}_", ".jpg", storageDir)
    }

    /**
     * Save photo to internal storage
     */
    fun savePhotoToInternalStorage(context: Context, uri: Uri, prefix: String = "PHOTO"): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${prefix}_${timeStamp}.jpg"
            val file = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete photo file
     */
    fun deletePhotoFile(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}