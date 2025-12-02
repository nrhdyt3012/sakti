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

        lifecycleScope.launch {
            val userSession = sessionManager.userSession.first()

            if (userSession.isLoggedIn &&
                userSession.userId != null &&
                userSession.email != null &&
                userSession.name != null &&
                userSession.role != null
            ) {

                userSession.authToken?.let { token ->
                    RetrofitClient.updateAuthToken(token)
                }

                // ❌ HAPUS syncManager.initializeSync()

                navigateToHome(
                    userId = userSession.userId,
                    email = userSession.email,
                    name = userSession.name,
                    role = userSession.role
                )
            } else {
                showLoginScreen()
            }
        }
    }

    private fun showLoginScreen() {
        setContent {
            LoginScreen(
                onLoginSuccess = { userId, email, name, role, token ->
                    lifecycleScope.launch {
                        sessionManager.saveSession(
                            userId = userId,
                            email = email,
                            name = name,
                            role = role,
                            authToken = token
                        )

                        token?.let { RetrofitClient.updateAuthToken(it) }

                        // ❌ HAPUS syncManager.initializeSync()

                        navigateToHome(userId, email, name, role)
                    }
                }
            )
        }
    }

    private fun navigateToHome(userId: Int, email: String, name: String, role: String) {
        // Hanya Teknisi yang bisa login
        if (role.uppercase() != "TEKNISI") {
            Toast.makeText(this, "Access denied. Technician only.", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, TeknisiActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.putExtra("USER_EMAIL", email)
        intent.putExtra("USER_NAME", name)
        intent.putExtra("USER_ROLE", role)
        startActivity(intent)
        finish()
    }
}