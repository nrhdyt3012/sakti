// 1. Retrofit API Service untuk Authentication
// File: app/src/main/java/com/example/saktinocompose/network/api/AuthApiService.kt

package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Any>>

    @GET("auth/verify")
    suspend fun verifyToken(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserData>>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): Response<LoginResponse>
}