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
                    // ✅ CRITICAL: Set token BEFORE navigation
                    RetrofitClient.updateAuthToken(token)
                    // ✅ Wait a bit to ensure token is set
                    delay(100)
                }

                navigateToHome(
                    userId = userSession.userId.toString(),
                    email = userSession.email!!,
                    name = userSession.name!!,
                    role = userSession.role!!
                )
            } else {
                if (userSession.isLoggedIn && !isTokenValid) {
                    sessionManager.clearSession()
                    RetrofitClient.clearAuthToken()
                }
                showLoginScreen()
            }
        }
    }

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

            // ✅ PERBAIKAN: Kurangi buffer atau hapus
            val bufferSeconds = 60L  // Dari 300L (5 menit) jadi 60L (1 menit)
            val isValid = exp > (now + bufferSeconds)

            Log.d("LoginActivity", """
            Token Validation:
            - Exp: $exp (${java.util.Date(exp * 1000)})
            - Now: $now (${java.util.Date(now * 1000)})
            - Valid: $isValid
            - Time left: ${(exp - now) / 60} minutes
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
                            // ✅ PERBAIKAN: Simpan ke SessionManager DULU
                            token?.let {
                                // 1. Save to SessionManager FIRST (persistent)
                                sessionManager.saveSession(
                                    userId = userId,
                                    email = email,
                                    name = name,
                                    role = role.uppercase().trim(),
                                    authToken = it
                                )
                                Log.d("LoginActivity", "✅ Step 1: Session saved with token")

                                // 2. Wait for SessionManager to write
                                delay(1000)

                                // 3. Set to RetrofitClient (memory cache)
                                RetrofitClient.updateAuthToken(it)
                                Log.d("LoginActivity", "✅ Step 2: Token set to Retrofit")

                                // 4. Wait for token to propagate
                                delay(500)
                            }

                            Log.d("LoginActivity", "✅ Step 3: Navigating to home")
                            navigateToHome(userId, email, name, role.uppercase().trim())

                        } catch (e: Exception) {
                            Log.e("LoginActivity", "❌ Login error", e)
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
            Log.e("LoginActivity", "Access denied. Role: '$normalizedRole'")

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