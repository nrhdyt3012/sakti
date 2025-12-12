// File: app/src/main/java/com/example/saktinocompose/network/api/ChangeRequestApiService.kt
package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ChangeRequestApiService {

    @GET("change-requests")
    suspend fun getChangeRequests(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null
    ): Response<ChangeRequestListResponse>

    @PUT("change-requests/{cr_id}/review")
    suspend fun submitReview(
        @Path("cr_id") crId: String,
        @Body request: SubmittedReviewRequest
    ): Response<ApiResponse<Any>>

    @PUT("change-requests/{id}/inspection")
    suspend fun submitInspection(
        @Path("id") id: String,
        @Body request: InspectionRequest
    ): Response<ApiResponse<Any>>

    // ✅ NEW: Upload inspection photo
    @Multipart
    @POST("change-requests/{cr_id}/inspection/photo")
    suspend fun uploadInspectionPhoto(
        @Path("cr_id") crId: String,
        @Part photo: MultipartBody.Part
    ): Response<InspectionPhotoResponse>

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