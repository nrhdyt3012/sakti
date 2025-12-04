package com.example.saktinocompose.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.data.entity.User
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginEvent {
    data class LoginSuccess(val user: User, val token: String? = null) : LoginEvent
    data class LoginError(val message: String) : LoginEvent
}

class LoginViewModelCompose(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val authRepository = AuthRepository(userDao)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login() {
        viewModelScope.launch {
            val username = _uiState.value.username.trim()
            val password = _uiState.value.password

            if (username.isEmpty() || password.isEmpty()) {
                _loginEvent.emit(LoginEvent.LoginError("Username dan Password tidak boleh kosong"))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            try {
                when (val result = authRepository.login(username, password)) {
                    is Result.Success -> {
                        val user = result.data
                        val token = RetrofitClient.authToken

                        Log.d("LoginViewModel", "✅ Login success, token: ${token?.take(20)}...")

                        // ✅ PERBAIKAN: Tunggu lebih lama untuk memastikan token ter-sync
                        delay(800)

                        _uiState.update { it.copy(isLoading = false) }
                        _loginEvent.emit(LoginEvent.LoginSuccess(user, token))
                    }
                    is Result.Error -> {
                        Log.e("LoginViewModel", "❌ Login error: ${result.message}")
                        _uiState.update { it.copy(isLoading = false) }
                        _loginEvent.emit(LoginEvent.LoginError(result.message ?: "Login gagal"))
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _loginEvent.emit(LoginEvent.LoginError("Login gagal"))
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ Login exception", e)
                _uiState.update { it.copy(isLoading = false) }
                _loginEvent.emit(LoginEvent.LoginError("Error: ${e.message}"))
            }
        }
    }
}