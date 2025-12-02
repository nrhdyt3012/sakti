package com.example.saktinocompose.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.repository.ChangeRequestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChangeRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val changeRequestDao = database.changeRequestDao()
    private val repository = ChangeRequestRepository(changeRequestDao)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        // ✅ Auto refresh jika ada token
        if (RetrofitClient.authToken != null) {
            Log.d("ChangeRequestVM", "Token found, auto-refreshing data...")
            refreshData()
        } else {
            Log.w("ChangeRequestVM", "No token found, skipping auto-refresh")
        }
    }

    /**
     * ✅ Refresh data from API with better error handling
     */
    fun refreshData() {
        viewModelScope.launch {
            // ✅ Check token before fetching
            val token = RetrofitClient.authToken
            if (token == null) {
                Log.w("ChangeRequestVM", "No token, cannot refresh")
                _error.value = "Please login to view data"
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            Log.d("ChangeRequestVM", "Starting refresh with token: ${token.take(20)}...")

            when (val result = repository.fetchFromApi()) {
                is Result.Success -> {
                    Log.d("ChangeRequestVM", "Refresh successful: ${result.data.size} items")
                    _error.value = null
                }
                is Result.Error -> {
                    val errorMsg = result.message ?: "Failed to fetch data"
                    Log.e("ChangeRequestVM", "Refresh error: $errorMsg")
                    _error.value = errorMsg

                    // ✅ Check if token expired
                    if (errorMsg.contains("401") || errorMsg.contains("Token") || errorMsg.contains("Session")) {
                        Log.e("ChangeRequestVM", "Token expired, clearing...")
                        RetrofitClient.clearAuthToken()
                    }
                }
                else -> {
                    Log.e("ChangeRequestVM", "Unknown refresh result")
                    _error.value = "Failed to fetch data"
                }
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    // ===== Read Operations =====

    fun getAllChangeRequests(): Flow<List<ChangeRequest>> {
        return repository.getAllChangeRequests()
    }

    fun getChangeRequestsByStatus(status: String): Flow<List<ChangeRequest>> {
        return repository.getChangeRequestsByStatus(status)
    }

    suspend fun getChangeRequestById(crId: String): ChangeRequest? {
        return repository.getChangeRequestById(crId)
    }

    // ===== Update Operations =====

    fun updateChangeRequestStatus(
        changeRequest: ChangeRequest,
        newStatus: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val updated = changeRequest.copy(
                status = newStatus,
                updatedAt = getCurrentIsoTimestamp()
            )

            when (val result = repository.updateChangeRequest(updated)) {
                is Result.Error -> {
                    _error.value = result.message
                }
                else -> {
                    _error.value = null
                }
            }

            _isLoading.value = false
        }
    }

    fun updateFullChangeRequest(updatedRequest: ChangeRequest) {
        viewModelScope.launch {
            _isLoading.value = true

            val updated = updatedRequest.copy(
                updatedAt = getCurrentIsoTimestamp()
            )

            when (val result = repository.updateChangeRequest(updated)) {
                is Result.Error -> {
                    _error.value = result.message
                }
                else -> {
                    _error.value = null
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * ✅ Helper untuk generate ISO 8601 timestamp
     */
    private fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}