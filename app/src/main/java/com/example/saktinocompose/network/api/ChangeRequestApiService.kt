package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ChangeRequestApiService {

    @GET("change-requests")
    suspend fun getChangeRequests(
        @Header("Authorization") token: String,
    ): Response<ChangeRequestListResponse>

    @PUT("change-requests/{id}/inspection")
    suspend fun submitInspection(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: InspectionRequest
    ): Response<ApiResponse<Any>>

    @POST("change-requests/{id}/schedule")
    suspend fun scheduleImplementation(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ScheduleRequest
    ): Response<ApiResponse<Any>>

    @POST("change-requests/{id}/implementation")
    suspend fun completeImplementation(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ImplementationRequest
    ): Response<ApiResponse<Any>>
}