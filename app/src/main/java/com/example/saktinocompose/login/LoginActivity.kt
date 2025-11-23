// ===== Updated LoginActivity untuk save token =====
// File: app/src/main/java/com/example/saktinocompose/login/LoginActivity.kt

package com.example.saktinocompose.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.saktinocompose.enduser.EnduserActivity
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.teknisi.TeknisiActivity
import com.example.saktinocompose.utils.SessionManager
import com.example.saktinocompose.utils.SyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity: ComponentActivity(){
    private lateinit var sessionManager: SessionManager
    private lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        syncManager = SyncManager(this)

        // Cek apakah user sudah login
        lifecycleScope.launch {
            val userSession = sessionManager.userSession.first()

            if (userSession.isLoggedIn &&
                userSession.userId != null &&
                userSession.email != null &&
                userSession.name != null &&
                userSession.role != null) {

                // Set token jika ada
                userSession.authToken?.let { token ->
                    RetrofitClient.setAuthToken(token)
                }

                // Initialize sync
                syncManager.initializeSync()

                // User sudah login, langsung navigasi ke halaman sesuai role
                navigateToHome(
                    userId = userSession.userId,
                    email = userSession.email,
                    name = userSession.name,
                    role = userSession.role
                )
            } else {
                // User belum login, tampilkan login screen
                showLoginScreen()
            }
        }
    }

    private fun showLoginScreen() {
        setContent {
            LoginScreen(
                onLoginSuccess = { userId, email, name, role, token ->
                    // Simpan session dengan token
                    lifecycleScope.launch {
                        sessionManager.saveSession(
                            userId = userId,
                            email = email,
                            name = name,
                            role = role,
                            authToken = token
                        )

                        // Set token ke RetrofitClient
                        token?.let { RetrofitClient.setAuthToken(it) }

                        // Initialize sync
                        syncManager.initializeSync()

                        navigateToHome(userId, email, name, role)
                    }
                }
            )
        }
    }

    private fun navigateToHome(userId: Int, email: String, name: String, role: String) {
        val intent = when (role.uppercase()) {
            "TEKNISI" -> Intent(this, TeknisiActivity::class.java)
            "END_USER" -> Intent(this, EnduserActivity::class.java)
            else -> return
        }

        intent.putExtra("USER_ID", userId)
        intent.putExtra("USER_EMAIL", email)
        intent.putExtra("USER_NAME", name)
        intent.putExtra("USER_ROLE", role)
        startActivity(intent)
        finish()
    }
}