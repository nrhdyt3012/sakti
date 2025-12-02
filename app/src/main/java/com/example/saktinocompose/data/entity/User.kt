package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = false)
    val id: String,  // ✅ Changed from Int to String untuk consistency dengan API
    val username: String,
    val name: String,
    val passwordHash: String,
    val role: String
)