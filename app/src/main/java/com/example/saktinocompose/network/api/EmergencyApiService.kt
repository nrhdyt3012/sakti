package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface EmergencyApiService {

    /**
     * Create emergency
     * POST /emergency
     */
    @POST("emergency")
    suspend fun createEmergency(
        @Body request: EmergencyRequest
    ): Response<EmergencyResponse>

    /**
     * Upload emergency photo
     * POST /emergency/photo
     */
    @Multipart
    @POST("emergency/photo")
    suspend fun uploadEmergencyPhoto(
        @Part photo: MultipartBody.Part
    ): Response<EmergencyPhotoResponse>
}