package com.example.saktinocompose.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: String,
    val username: String,
    val name: String,
    val passwordHash: String,
    val role: String
)