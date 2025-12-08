package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface SiladanApiService {

    @POST("integrations/siladan/change-requests")
    suspend fun createSiladanChangeRequest(
        @Body request: SiladanChangeRequestRequest
    ): Response<ApiResponse<Any>>
}