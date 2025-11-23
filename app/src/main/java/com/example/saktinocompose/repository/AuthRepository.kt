// 1. Repository Interface dan Implementation untuk Authentication
// File: app/src/main/java/com/example/saktinocompose/repository/AuthRepository.kt

package com.example.saktinocompose.repository

import com.example.saktinocompose.data.dao.UserDao
import com.example.saktinocompose.data.entity.User
import com.example.saktinocompose.network.ApiConfig
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.LoginRequest
import com.example.saktinocompose.network.dto.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AuthRepository(
    private val userDao: UserDao
) {

    /**
     * Login function yang support mode offline dan online
     */
    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                if (ApiConfig.IS_OFFLINE_MODE) {
                    // Mode Offline - gunakan database lokal
                    loginOffline(email, password)
                } else {
                    // Mode Online - gunakan API
                    loginOnline(email, password)
                }
            } catch (e: Exception) {
                Result.Error(e, "Login gagal: ${e.message}")
            }
        }
    }

    /**
     * Login Offline menggunakan database lokal
     */
    private suspend fun loginOffline(email: String, password: String): Result<User> {
        val user = userDao.getUserByEmail(email)
        val hashedPassword = hashPassword(password)

        return if (user != null && user.passwordHash == hashedPassword) {
            Result.Success(user)
        } else {
            Result.Error(Exception("Invalid credentials"), "Email atau password salah")
        }
    }

    /**
     * Login Online menggunakan API
     */
    private suspend fun loginOnline(email: String, password: String): Result<User> {
        try {
            val response = RetrofitClient.authService.login(
                LoginRequest(email, password)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data

                if (userData != null) {
                    // Save token untuk request selanjutnya
                    RetrofitClient.setAuthToken(userData.token)

                    // Convert API response ke local User entity
                    val user = User(
                        id = userData.id,
                        email = userData.email,
                        name = userData.name,
                        passwordHash = "", // Tidak perlu simpan password hash dari API
                        role = userData.role
                    )

                    // Sync ke database lokal
                    syncUserToLocal(user)

                    return Result.Success(user)
                } else {
                    return Result.Error(
                        Exception("Invalid response"),
                        "Data user tidak ditemukan"
                    )
                }
            } else {
                val errorMessage = response.body()?.message ?: "Login gagal"
                return Result.Error(Exception(errorMessage), errorMessage)
            }
        } catch (e: Exception) {
            // Fallback ke offline mode jika API error
            return loginOffline(email, password)
        }
    }

    /**
     * Sync user data ke database lokal
     */
    private suspend fun syncUserToLocal(user: User) {
        val existingUser = userDao.getUserById(user.id)
        if (existingUser != null) {
            userDao.updateUser(user)
        } else {
            userDao.insertUser(user)
        }
    }

    /**
     * Logout
     */
    suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!ApiConfig.IS_OFFLINE_MODE) {
                    // Call logout API jika online
                    RetrofitClient.authToken?.let { token ->
                        RetrofitClient.authService.logout("Bearer $token")
                    }
                }

                // Clear token
                RetrofitClient.clearAuthToken()

                Result.Success(true)
            } catch (e: Exception) {
                // Tetap clear token meskipun API error
                RetrofitClient.clearAuthToken()
                Result.Success(true)
            }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

