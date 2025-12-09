package com.example.saktinocompose.repository

import android.util.Log
import com.example.saktinocompose.data.model.User
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository() {

    suspend fun login(username: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.authService.login(
                    LoginRequest(username, password)
                )

                val body = response.body()

                // ✔ VALIDASI SUKSES LOGIN
                if (response.isSuccessful && body?.success == true) {

                    val userData = body.data.user
                    val token = body.data.token

                    // ✔ SIMPAN TOKEN
                    RetrofitClient.updateAuthToken(token)

                    // ✔ KONVERSI KE ENTITY ANDROID
                    val user = User(
                        id = userData.id,
                        username = userData.username,
                        name = userData.name,
                        passwordHash = "",
                        role = userData.role,
                        instansi = userData.instansi
                    )

                    return@withContext Result.Success(user)
                }

                // ❌ LOGIN GAGAL
                val message = body?.message ?: "Login gagal"
                return@withContext Result.Error(Exception(message), message)

            } catch (e: Exception) {
                return@withContext Result.Error(
                    e,
                    "Network error: ${e.message}."
                )
            }
        }
    }

    suspend fun getProfile(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                    ?: return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )

                // ✅ JANGAN tambah "Bearer" di sini
                // authInterceptor sudah menambahkannya
                val response = RetrofitClient.authService.getProfile() // ← Tanpa "Bearer"

                val body = response.body()

                if (response.isSuccessful && body?.status == "success") {
                    val u = body.data
                    val user = User(
                        id = u!!.id,
                        username = u.username,
                        name = u.username,
                        passwordHash = "",
                        role = u.role,
                        instansi = u.instansi
                    )
                    return@withContext Result.Success(user)
                }

                val errorMsg = body?.message ?: "Failed to get profile"
                return@withContext Result.Error(Exception(errorMsg), errorMsg)

            } catch (e: Exception) {
                return@withContext Result.Error(
                    e, "Network error: ${e.message}"
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
