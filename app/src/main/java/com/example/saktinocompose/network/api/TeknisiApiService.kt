// 2. Retrofit API Service untuk Teknisi Operations
// File: app/src/main/java/com/example/saktinocompose/network/api/TeknisiApiService.kt

package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface TeknisiApiService {

    // Get all change requests (untuk teknisi)
    @GET("change-requests")
    suspend fun getAllChangeRequests(
        @Header("Authorization") token: String
    ): Response<ChangeRequestListResponse>

    // Get change request by ID
    @GET("change-requests/{id}")
    suspend fun getChangeRequestById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ChangeRequestApiResponse>

    // Update status change request
    @PUT("change-requests/{id}/status")
    suspend fun updateChangeRequestStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateStatusRequest
    ): Response<ChangeRequestApiResponse>

    // Submit risk assessment dengan foto
    @Multipart
    @POST("risk-assessments")
    suspend fun submitRiskAssessment(
        @Header("Authorization") token: String,
        @Part("change_request_id") changeRequestId: RequestBody,
        @Part("teknisi_id") teknisiId: RequestBody,
        @Part("teknisi_name") teknisiName: RequestBody,
        @Part("skor_dampak") skorDampak: RequestBody,
        @Part("skor_kemungkinan") skorKemungkinan: RequestBody,
        @Part("skor_eksposur") skorEksposur: RequestBody,
        @Part("skor_risiko") skorRisiko: RequestBody,
        @Part("level_risiko") levelRisiko: RequestBody,
        @Part("estimasi_biaya") estimasiBiaya: RequestBody,
        @Part("estimasi_waktu") estimasiWaktu: RequestBody,
        @Part photo: MultipartBody.Part? // File foto
    ): Response<RiskAssessmentApiResponse>

    // Update risk assessment
    @PUT("risk-assessments/{id}")
    suspend fun updateRiskAssessment(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: RiskAssessmentApiRequest
    ): Response<RiskAssessmentApiResponse>

    // Schedule implementation
    @PUT("change-requests/{id}/schedule")
    suspend fun scheduleImplementation(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: Map<String, Any>
    ): Response<ChangeRequestApiResponse>

    // Complete implementation
    @PUT("change-requests/{id}/complete")
    suspend fun completeImplementation(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: Map<String, Any>
    ): Response<ChangeRequestApiResponse>
}