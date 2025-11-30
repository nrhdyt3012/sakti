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
// ❌ HAPUS import SyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity: ComponentActivity(){
    private lateinit var sessionManager: SessionManager
    // ❌ HAPUS syncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        // ❌ HAPUS syncManager = SyncManager(this)

        lifecycleScope.launch {
            val userSession = sessionManager.userSession.first()

            if (userSession.isLoggedIn &&
                userSession.userId != null &&
                userSession.email != null &&
                userSession.name != null &&
                userSession.role != null) {

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