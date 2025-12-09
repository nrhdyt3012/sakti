package com.example.saktinocompose.data.model

data class User(
    val id: String,
    val username: String,
    val name: String,
    val passwordHash: String,
    val role: String,
    val instansi: String,
)