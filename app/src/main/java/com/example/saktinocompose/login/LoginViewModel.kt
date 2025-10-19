package com.example.saktinocompose.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.data.entity.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest

enum class UserRole {
    TEKNISI,
    END_USER
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginEvent {
    data class LoginSuccess(val user: User) : LoginEvent
    data class LoginError(val message: String) : LoginEvent
}

class LoginViewModelCompose(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun login() {
        viewModelScope.launch {
            val email = _uiState.value.email.trim()
            val password = _uiState.value.password

            if (email.isEmpty() || password.isEmpty()) {
                _loginEvent.emit(LoginEvent.LoginError("Email dan Password tidak boleh kosong"))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            delay(1000) // Simulasi network delay

            try {
                val user = userDao.getUserByEmail(email)
                val hashedPassword = hashPassword(password)

                if (user != null && user.passwordHash == hashedPassword) {
                    _loginEvent.emit(LoginEvent.LoginSuccess(user))
                } else {
                    _loginEvent.emit(LoginEvent.LoginError("Email atau password tidak valid"))
                }
            } catch (e: Exception) {
                _loginEvent.emit(LoginEvent.LoginError("Terjadi kesalahan: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}