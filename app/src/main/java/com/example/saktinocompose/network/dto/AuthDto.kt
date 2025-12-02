// 2. Data Transfer Objects (DTO) untuk API Request/Response
// File: app/src/main/java/com/example/saktinocompose/network/dto/AuthDto.kt

package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

// Login Response
data class LoginResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: UserData?
)

data class UserData(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("role")
    val role: String, // TEKNISI, END_USER, etc

    @SerializedName("status")
    val status: String, // ACTIVE, INACTIVE

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("agency_id")
    val agencyId: Int? = null,

    @SerializedName("token")
    val token: String,

)

data class LoginErrorResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("details")
    val details: Any?
)

data class ProfileResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: UserData?
)