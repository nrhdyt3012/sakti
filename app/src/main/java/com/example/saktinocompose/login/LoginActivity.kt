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
            try {
                val userSession = sessionManager.userSession.first()
                val isTokenValid = userSession.authToken?.let { token ->
                    checkTokenValidity(token)
                } ?: false

                Log.d("LoginActivity", """
                ========== SESSION CHECK ==========
                isLoggedIn: ${userSession.isLoggedIn}
                hasToken: ${userSession.authToken != null}
                isTokenValid: $isTokenValid
                userId: ${userSession.userId}
                email: ${userSession.email}
                name: ${userSession.name}
                role: ${userSession.role}
                instansi: ${userSession.instansi}
                ===================================
            """.trimIndent())

                if (userSession.isLoggedIn && isTokenValid) {
                    userSession.authToken?.let { token ->
                        // Set token BEFORE navigation
                        RetrofitClient.updateAuthToken(token)
                        delay(100)
                    }

                    // ✅ PERBAIKAN: Pastikan semua field tidak null
                    val userId = userSession.userId ?: "0"
                    val email = userSession.email ?: "unknown@email.com"
                    val name = userSession.name ?: "Unknown User"
                    val role = userSession.role ?: "TEKNISI"
                    val instansi = userSession.instansi ?: "Unknown Agency"

                    navigateToHome(
                        userId = userId,
                        email = email,
                        name = name,
                        role = role,
                        instansi = instansi
                    )
                } else {
                    if (userSession.isLoggedIn && !isTokenValid) {
                        sessionManager.clearSession()
                        RetrofitClient.clearAuthToken()
                    }
                    showLoginScreen()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "❌ Error in onCreate", e)
                showLoginScreen()
            }
        }
    }

    private fun checkTokenValidity(token: String): Boolean {
        return try {
            val cleanToken = token
                .replace("Bearer ", "", ignoreCase = true)
                .trim()

            val parts = cleanToken.split(".")
            if (parts.size != 3) {
                Log.e("LoginActivity", "Invalid token format")
                return false
            }

            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val json = org.json.JSONObject(payload)

            val exp = json.getLong("exp")
            val now = System.currentTimeMillis() / 1000

            val bufferSeconds = 60L
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
                onLoginSuccess = { userId, email, name, role, instansi, token ->
                    lifecycleScope.launch {
                        try {
                            // ✅ PERBAIKAN: Validasi input dulu
                            if (userId.isBlank() || email.isBlank() || name.isBlank()) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Login data incomplete",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                return@launch
                            }

                            token?.let {
                                // 1. Save to SessionManager FIRST
                                sessionManager.saveSession(
                                    userId = userId,
                                    email = email,
                                    name = name,
                                    role = role.uppercase().trim(),
                                    instansi = instansi,
                                    authToken = it
                                )
                                Log.d("LoginActivity", "✅ Step 1: Session saved with token")

                                // 2. Wait for SessionManager to write
                                delay(1000)

                                // 3. Set to RetrofitClient
                                RetrofitClient.updateAuthToken(it)
                                Log.d("LoginActivity", "✅ Step 2: Token set to Retrofit")

                                // 4. Wait for token to propagate
                                delay(500)
                            }

                            Log.d("LoginActivity", "✅ Step 3: Navigating to home")
                            navigateToHome(userId, email, name, role.uppercase().trim(), instansi)

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

    private fun navigateToHome(
        userId: String,
        email: String,
        name: String,
        role: String,
        instansi: String
    ) {
        try {
            val normalizedRole = role.uppercase().trim()

            Log.d("LoginActivity", """
            ========== NAVIGATION DATA ==========
            userId: $userId
            email: $email
            name: $name
            role: $normalizedRole
            instansi: $instansi
            =====================================
            """.trimIndent())

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
                putExtra("USER_INSTANSI", instansi)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("LoginActivity", "❌ Navigation error", e)
            Toast.makeText(
                this,
                "Navigation error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}