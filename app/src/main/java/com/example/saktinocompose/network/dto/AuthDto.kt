// 2. Data Transfer Objects (DTO) untuk API Request/Response
// File: app/src/main/java/com/example/saktinocompose/network/dto/AuthDto.kt

package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

// Login Response
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: UserData?
)

data class UserData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("token")
    val token: String? // JWT token untuk autentikasi
)