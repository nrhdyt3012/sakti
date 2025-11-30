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

                if (response.isSuccessful && response.body()?.success == true) {
                    val userData = response.body()?.data

                    if (userData != null) {
                        // Save token
                        RetrofitClient.updateAuthToken(userData.token)

                        // Convert API response to local User entity
                        val user = User(
                            id = userData.id,
                            email = userData.email,
                            name = userData.name,
                            passwordHash = "", // Not needed from API
                            role = userData.role
                        )

                        // Cache to local database
                        syncUserToLocal(user)

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

    private suspend fun syncUserToLocal(user: User) {
        val existingUser = userDao.getUserById(user.id)
        if (existingUser != null) {
            userDao.updateUser(user)
        } else {
            userDao.insertUser(user)
        }
    }

    suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitClient.authToken?.let { token ->
                    RetrofitClient.authService.logout("Bearer $token")
                }
                RetrofitClient.clearAuthToken()
                Result.Success(true)
            } catch (e: Exception) {
                RetrofitClient.clearAuthToken()
                Result.Success(true)
            }
        }
    }
}