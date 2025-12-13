package com.example.saktinocompose.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.repository.ChangeRequestRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChangeRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChangeRequestRepository()

    private val _changeRequests = MutableStateFlow<List<ChangeRequest>>(emptyList())
    val changeRequests = _changeRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        viewModelScope.launch {
            delay(500) // Wait for token
            refreshData()
        }
    }

    /**
     * ✅ FIXED: Refresh dengan token validation
     */
    fun refreshData() {
        viewModelScope.launch {
            val token = RetrofitClient.authToken

            Log.d("ChangeRequestVM", """
            ========== REFRESH DATA ==========
            Token available: ${token != null}
            Token value: ${token?.take(20)}...
            ==================================
        """.trimIndent())

            if (token == null) {
                _error.value = "Please login to view data"
                Log.e("ChangeRequestVM", "❌ No token available")
                return@launch
            }

            // ✅ PERBAIKAN: Verify token dengan server dulu
            if (!RetrofitClient.verifyTokenWithServer()) {
                _error.value = "Token invalid. Please login again."
                RetrofitClient.clearAuthToken()
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            Log.d("ChangeRequestVM", "🔄 Starting API fetch...")

            when (val result = repository.fetchNonEmergencyChangeRequests()) {
                is Result.Success -> {
                    _changeRequests.value = result.data
                    _error.value = null
                    Log.d("ChangeRequestVM", "✅ Fetched ${result.data.size} non-emergency items")
                }
                is Result.Error -> {
                    val errorMsg = result.message ?: "Unknown error"
                    _error.value = errorMsg
                    Log.e("ChangeRequestVM", "❌ Fetch error: $errorMsg")

                    if (errorMsg.contains("401") ||
                        errorMsg.contains("Token") ||
                        errorMsg.contains("Session")) {
                        RetrofitClient.clearAuthToken()
                    }
                }
                else -> {
                    _error.value = "Failed to fetch data"
                }
            }

            _isLoading.value = false
        }
    }
    private suspend fun ensureTokenLoaded() {
        if (RetrofitClient.authToken == null) {
            val context = getApplication<Application>().applicationContext
            val sessionManager = com.example.saktinocompose.utils.SessionManager(context)

            val token = sessionManager.authToken.first()
            if (token != null) {
                RetrofitClient.updateAuthToken(token)
                Log.d("ChangeRequestVM", "🔄 Token restored: ${token.take(20)}...")
                delay(200) // Wait for propagation
            }
        }
    }

    /**
     * ✅ NEW: Token validation helper
     */
    private fun isTokenValid(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false

            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val json = org.json.JSONObject(payload)

            val exp = json.getLong("exp")
            val now = System.currentTimeMillis() / 1000

            exp > now
        } catch (e: Exception) {
            Log.e("ChangeRequestVM", "Token validation error", e)
            false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getAllChangeRequests() = changeRequests

    fun getChangeRequestsByStatus(status: String): List<ChangeRequest> {
        return _changeRequests.value.filter { it.status == status }
    }

    fun getChangeRequestById(crId: String): ChangeRequest? {
        return _changeRequests.value.find { it.id == crId }
    }

    fun updateFullChangeRequest(updatedRequest: ChangeRequest) {
        viewModelScope.launch {
            _isLoading.value = true

            // ✅ FIXED: Update lokal dulu untuk responsif UI
            val currentList = _changeRequests.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == updatedRequest.id }

            if (index != -1) {
                currentList[index] = updatedRequest
                _changeRequests.value = currentList
            }

            // ✅ FIXED: Setelah update lokal, refresh dari API untuk data terbaru
            when (val result = repository.updateChangeRequest(updatedRequest)) {
                is Result.Success -> {
                    // ✅ Refresh data dari API untuk sinkronisasi
                    delay(500) // Tunggu sebentar agar API ter-update
                    refreshData() // Ambil data terbaru dari API
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    fun updateChangeRequestStatus(changeRequest: ChangeRequest, newStatus: String) {
        val updated = changeRequest.copy(
            status = newStatus,
            updatedAt = java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date())
        )
        updateFullChangeRequest(updated)
    }
}