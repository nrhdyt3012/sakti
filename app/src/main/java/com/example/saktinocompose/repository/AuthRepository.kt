package com.example.saktinocompose.repository

import com.example.saktinocompose.data.dao.UserDao
import com.example.saktinocompose.data.entity.User
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val userDao: UserDao
) {

    /**
     * ✅ Login via API only (Online-only mode)
     */
    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.authService.login(
                    LoginRequest(email, password)
                )

                if (response.isSuccessful && response.body()?.status == "success") {
                    val userData = response.body()?.data

                    if (userData != null) {
                        // Save token
                        RetrofitClient.updateAuthToken(userData.token)

                        // Convert API response to local User entity
                        val user = User(
                            id = userData.id,
                            username = userData.username,
                            name = userData.name,
                            passwordHash = "", // Not needed from API
                            role = userData.role
                        )

                        return@withContext Result.Success(user)
                    } else {
                        return@withContext Result.Error(
                            Exception("Invalid response"),
                            "User data not found"
                        )
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Login failed"
                    return@withContext Result.Error(Exception(errorMessage), errorMessage)
                }
            } catch (e: Exception) {
                return@withContext Result.Error(
                    e,
                    "Network error: ${e.message}. Please check your internet connection."
                )
            }
        }
    }
    suspend fun getProfile(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val response = RetrofitClient.authService.getProfile("Bearer $token")

                if (response.isSuccessful && response.body()?.status == "success") {
                    val userData = response.body()?.data

                    if (userData != null) {
                        val user = User(
                            id = userData.id,
                            username = userData.username,
                            name = userData.username,
                            passwordHash = "",
                            role = userData.role
                        )
                        return@withContext Result.Success(user)
                    } else {
                        return@withContext Result.Error(
                            Exception("Invalid response"),
                            "Profile data not found"
                        )
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to get profile"
                    return@withContext Result.Error(Exception(errorMessage), errorMessage)
                }
            } catch (e: Exception) {
                return@withContext Result.Error(
                    e,
                    "Network error: ${e.message}"
                )
            }
        }
    }

    suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitClient.clearAuthToken()
                Result.Success(true)
            } catch (e: Exception) {
                RetrofitClient.clearAuthToken()
                Result.Success(true)
            }
        }
    }
}