package com.example.saktinocompose.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.teknisi.TeknisiActivity
import com.example.saktinocompose.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity: ComponentActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        lifecycleScope.launch {
            val userSession = sessionManager.userSession.first()

            // ✅ CHECK TOKEN EXPIRY dengan toleransi lebih besar
            val isTokenValid = userSession.authToken?.let { token ->
                checkTokenValidity(token)
            } ?: false

            Log.d("LoginActivity", """
            ========== SESSION CHECK ==========
            isLoggedIn: ${userSession.isLoggedIn}
            hasToken: ${userSession.authToken != null}
            isTokenValid: $isTokenValid
            ===================================
        """.trimIndent())

            if (userSession.isLoggedIn && isTokenValid) {
                userSession.authToken?.let { token ->
                    RetrofitClient.updateAuthToken(token)
                }

                navigateToHome(
                    userId = userSession.userId.toString(),
                    email = userSession.email!!,
                    name = userSession.name!!,
                    role = userSession.role!!
                )
            } else {
                // ✅ Clear invalid session
                if (userSession.isLoggedIn && !isTokenValid) {
                    sessionManager.clearSession()
                    RetrofitClient.clearAuthToken()
                }
                showLoginScreen()
            }
        }
    }

    // ✅ PERBAIKAN: Helper function dengan toleransi waktu lebih besar
    private fun checkTokenValidity(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("LoginActivity", "Invalid token format")
                return false
            }

            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val json = org.json.JSONObject(payload)

            val exp = json.getLong("exp")
            val now = System.currentTimeMillis() / 1000

            // ✅ PERBAIKAN UTAMA: Tambahkan buffer 5 MENIT (300 detik)
            // Ini untuk mengatasi perbedaan waktu server-client
            val bufferSeconds = 300L
            val isValid = exp > (now + bufferSeconds)

            Log.d("LoginActivity", """
                Token Validation (with 5min buffer):
                - Exp: $exp (${java.util.Date(exp * 1000)})
                - Now: $now (${java.util.Date(now * 1000)})
                - Buffer: $bufferSeconds seconds
                - Valid: $isValid
                - Time left: ${exp - now} seconds (${(exp - now) / 60} minutes)
            """.trimIndent())

            isValid
        } catch (e: Exception) {
            Log.e("LoginActivity", "Token validation failed", e)
            false
        }
    }

    private fun showLoginScreen() {
        setContent {
            LoginScreen(
                onLoginSuccess = { userId, email, name, role, token ->
                    lifecycleScope.launch {
                        try {
                            token?.let {
                                RetrofitClient.updateAuthToken(it)
                                Log.d("LoginActivity", "✅ Token set to Retrofit: ${it.take(20)}...")
                            }

                            // ✅ TAMBAHKAN: Delay untuk memastikan token ter-set dengan benar
                            delay(500)

                            withContext(Dispatchers.IO) {
                                sessionManager.saveSession(
                                    userId = userId,
                                    email = email,
                                    name = name,
                                    role = role.uppercase().trim(),
                                    authToken = token
                                )
                                Log.d("LoginActivity", "✅ Session saved")
                            }

                            // ✅ Tunggu sebentar lagi
                            delay(300)

                            withContext(Dispatchers.Main) {
                                navigateToHome(userId, email, name, role.uppercase().trim())
                            }

                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login error: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            )
        }
    }

    private fun navigateToHome(userId: String, email: String, name: String, role: String) {
        val normalizedRole = role.uppercase().trim()

        if (normalizedRole != "TEKNISI") {
            Log.e("LoginActivity", "Access denied. Role: '$role' (normalized: '$normalizedRole')")

            Toast.makeText(
                this,
                "Access denied. Technician only. Your role: $normalizedRole",
                Toast.LENGTH_LONG
            ).show()

            lifecycleScope.launch {
                sessionManager.clearSession()
                RetrofitClient.clearAuthToken()
            }
            return
        }

        val intent = Intent(this, TeknisiActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("USER_EMAIL", email)
            putExtra("USER_NAME", name)
            putExtra("USER_ROLE", normalizedRole)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
}