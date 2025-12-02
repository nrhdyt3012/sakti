package com.example.saktinocompose.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.teknisi.TeknisiActivity
import com.example.saktinocompose.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity: ComponentActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // ✅ Check session dulu
        lifecycleScope.launch {
            val userSession = sessionManager.userSession.first()

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
                    // ✅ Save session dulu, baru navigasi
                    lifecycleScope.launch {
                        try {
                            // 1. Save session
                            sessionManager.saveSession(
                                userId = userId,
                                email = email,
                                name = name,
                                role = role,
                                authToken = token
                            )

                            // 2. Update Retrofit token
                            token?.let { RetrofitClient.updateAuthToken(it) }

                            // 3. Navigate SETELAH save selesai
                            navigateToHome(userId, email, name, role)

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Error saving session: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }

    private fun navigateToHome(userId: String, email: String, name: String, role: String) {
        if (role.uppercase() != "TEKNISI") {
            Toast.makeText(this, "Access denied. Technician only.", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, TeknisiActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("USER_EMAIL", email)
            putExtra("USER_NAME", name)
            putExtra("USER_ROLE", role)
            // ✅ Clear back stack
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
}