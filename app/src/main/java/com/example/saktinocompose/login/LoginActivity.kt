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

            // ✅ Log untuk debugging
            Log.d("LoginActivity", """
            Session check:
            - isLoggedIn: ${userSession.isLoggedIn}
            - userId: ${userSession.userId}
            - email: ${userSession.email}
            - role: ${userSession.role}
            - hasToken: ${userSession.authToken != null}
        """.trimIndent())

            if (userSession.isLoggedIn &&
                userSession.userId != null &&
                userSession.email != null &&
                userSession.name != null &&
                userSession.role != null
            ) {
                // ✅ Update token ke Retrofit
                userSession.authToken?.let { token ->
                    RetrofitClient.updateAuthToken(token)
                }

                // ✅ Langsung navigasi ke home
                navigateToHome(
                    userId = userSession.userId.toString(),
                    email = userSession.email,
                    name = userSession.name,
                    role = userSession.role
                )
            } else {
                // ✅ Tampilkan login screen
                showLoginScreen()
            }
        }
    }

    private fun showLoginScreen() {
        setContent {
            LoginScreen(
                onLoginSuccess = { userId, email, name, role, token ->
                    lifecycleScope.launch {
                        try {
                            // ✅ Pastikan semua operasi async selesai
                            withContext(Dispatchers.IO) {
                                // 1. Save session dengan konfirmasi
                                sessionManager.saveSession(
                                    userId = userId,
                                    email = email,
                                    name = name,
                                    role = role.uppercase().trim(), // ✅ Normalize role
                                    authToken = token
                                )

                                // 2. Update Retrofit token
                                token?.let { RetrofitClient.updateAuthToken(it) }
                            }

                            // ✅ Tunggu 100ms untuk memastikan save selesai
                            delay(100)

                            // 3. Navigate di Main thread
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

                                // ✅ Reset UI agar user bisa retry
                            }
                        }
                    }
                }
            )
        }
    }

    private fun navigateToHome(userId: String, email: String, name: String, role: String) {
        // ✅ Normalize dan validate role
        val normalizedRole = role.uppercase().trim()

        if (normalizedRole != "TEKNISI") {
            // ✅ Log untuk debugging
            Log.e("LoginActivity", "Access denied. Role: '$role' (normalized: '$normalizedRole')")

            Toast.makeText(
                this,
                "Access denied. Technician only. Your role: $normalizedRole",
                Toast.LENGTH_LONG
            ).show()

            // ✅ JANGAN langsung return, clear session dulu
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