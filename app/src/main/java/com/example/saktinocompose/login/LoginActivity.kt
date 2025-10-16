package com.example.saktinocompose.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.saktinocompose.enduser.EnduserActivity
import com.example.saktinocompose.teknisi.TeknisiActivity
import com.example.saktinocompose.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity: ComponentActivity(){
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Cek apakah user sudah login
        lifecycleScope.launch {
            val userSession = sessionManager.userSession.first()

            if (userSession.isLoggedIn && userSession.email != null && userSession.role != null) {
                // User sudah login, langsung navigasi ke halaman sesuai role
                navigateToHome(userSession.email, userSession.role)
            } else {
                // User belum login, tampilkan login screen
                showLoginScreen()
            }
        }
    }

    private fun showLoginScreen() {
        setContent {
            LoginScreen(
                onLoginSuccess = { email, role ->
                    // Simpan session
                    lifecycleScope.launch {
                        sessionManager.saveSession(email, role.name)
                        navigateToHome(email, role.name)
                    }
                }
            )
        }
    }

    private fun navigateToHome(email: String, role: String) {
        val intent = when (role.uppercase()) {
            "TEKNISI" -> Intent(this, TeknisiActivity::class.java)
            "END_USER" -> Intent(this, EnduserActivity::class.java)
            else -> return
        }

        intent.putExtra("USER_EMAIL", email)
        intent.putExtra("USER_ROLE", role)
        startActivity(intent)
        finish()
    }
}