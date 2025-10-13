package com.example.saktinocompose.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UserRole {
    TEKNISI,
    END_USER
}
// Data class untuk menampung semua state UI
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// Enum untuk role user


// Sealed interface untuk event sekali jalan (Single-time events)
sealed interface LoginEvent {
    data class LoginSuccess(val email: String, val role: UserRole) : LoginEvent
    data class LoginError(val message: String) : LoginEvent
}

class LoginViewModelCompose : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val validUsers = mapOf(
        "test@example.com" to Pair("password123", UserRole.TEKNISI),
        "enduser@example.com" to Pair("password456", UserRole.END_USER)
    )

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login() {
        viewModelScope.launch {
            val email = _uiState.value.email
            val password = _uiState.value.password

            if (email.isEmpty() || password.isEmpty()) {
                _loginEvent.emit(LoginEvent.LoginError("Email and Password cannot be empty."))
                return@launch
            }

            // Mulai loading
            _uiState.update { it.copy(isLoading = true) }

            // Simulasi proses network/authentication
            delay(2000)

            // Logika validasi dengan role
            val userCredentials = validUsers[email]

            if (userCredentials != null && userCredentials.first == password) {
                // Kirim event sukses dengan role
                _loginEvent.emit(LoginEvent.LoginSuccess(email, userCredentials.second))
            } else {
                // Kirim event error
                _loginEvent.emit(LoginEvent.LoginError("Email atau password tidak valid."))
            }

            // Selesai loading
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}