package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ChangeRequestApiService {

    // ✅ FIXED: Add query parameters
    @GET("change-requests")
    suspend fun getChangeRequests(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null
    ): Response<ChangeRequestListResponse>

    @PUT("change-requests/{id}/inspection")
    suspend fun submitInspection(
        @Path("id") id: String,
        @Body request: InspectionRequest
    ): Response<ApiResponse<Any>>

    @POST("change-requests/{id}/schedule")
    suspend fun scheduleImplementation(
        @Path("id") id: String,
        @Body request: ScheduleRequest
    ): Response<ApiResponse<Any>>

    @POST("change-requests/{id}/implementation")
    suspend fun completeImplementation(
        @Path("id") id: String,
        @Body request: ImplementationRequest
    ): Response<ApiResponse<Any>>
}