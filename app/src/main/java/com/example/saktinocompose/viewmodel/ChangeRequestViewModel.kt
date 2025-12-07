package com.example.saktinocompose.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.repository.ChangeRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangeRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChangeRequestRepository()

    // ✅ In-memory storage (tidak persist)
    private val _changeRequests = MutableStateFlow<List<ChangeRequest>>(emptyList())
    val changeRequests = _changeRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        // ✅ Auto refresh saat ViewModel dibuat
        if (RetrofitClient.authToken != null) {
            refreshData()
        }
    }

    /**
     * ✅ Refresh data dari API (tanpa caching)
     */
    fun refreshData() {
        viewModelScope.launch {
            val token = RetrofitClient.authToken
            if (token == null) {
                _error.value = "Please login to view data"
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            when (val result = repository.fetchFromApi()) {
                is Result.Success -> {
                    _changeRequests.value = result.data
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                    // ✅ Jika token expired, clear
                    if (result.message?.contains("401") == true) {
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

    fun clearError() {
        _error.value = null
    }

    /**
     * ✅ Get all change requests dari memory
     */
    fun getAllChangeRequests() = changeRequests

    /**
     * ✅ Filter by status
     */
    fun getChangeRequestsByStatus(status: String): List<ChangeRequest> {
        return _changeRequests.value.filter { it.status == status }
    }

    /**
     * ✅ Get by ID
     */
    fun getChangeRequestById(crId: String): ChangeRequest? {
        return _changeRequests.value.find { it.id == crId }
    }

    /**
     * ✅ Update change request (in-memory + API)
     */
    fun updateFullChangeRequest(updatedRequest: ChangeRequest) {
        viewModelScope.launch {
            _isLoading.value = true

            // Update di memory dulu
            val currentList = _changeRequests.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == updatedRequest.id }

            if (index != -1) {
                currentList[index] = updatedRequest
                _changeRequests.value = currentList
            }

            // TODO: Kirim update ke API
            when (val result = repository.updateChangeRequest(updatedRequest)) {
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
     * ✅ Update status only
     */
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