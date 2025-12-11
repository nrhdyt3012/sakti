package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
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
}