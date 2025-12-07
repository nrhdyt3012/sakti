package com.example.saktinocompose.network.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)

data class LoginData(
    val token: String,
    val user: UserData
)

data class UserData(
    val id: String,
    val name: String,
    val username: String,
    val role: String,
    val status: String,
    val instansi: String
)

data class ProfileResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: UserData?
)