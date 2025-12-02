package com.example.saktinocompose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.network.Result
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

    // ✅ Loading state untuk UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        // ✅ Auto-fetch data when ViewModel created
        refreshData()
    }

    /**
     * ✅ Refresh data from API
     */
    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.fetchFromApi()) {
                is Result.Success -> {
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
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


    fun getAllChangeRequests(): Flow<List<ChangeRequest>> {
        return repository.getAllChangeRequests()
    }

    fun getChangeRequestsByStatus(status: String): Flow<List<ChangeRequest>> {
        return changeRequestDao.getChangeRequestsByStatus(status)
    }

    suspend fun getChangeRequestById(id: Int): ChangeRequest? {
        return repository.getChangeRequestById(id)
    }

    fun updateChangeRequestStatus(
        changeRequest: ChangeRequest,
        newStatus: String,
        teknisiId: Int? = null,
        teknisiName: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.updateStatus(
                changeRequest = changeRequest,
                newStatus = newStatus,
                teknisiId = teknisiId,
                teknisiName = teknisiName,
                notes = notes
            )) {
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
                updatedAt = System.currentTimeMillis()
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

    private suspend fun generateTicketId(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val count = changeRequestDao.getTodayRequestCount() + 1
        return "CR-$today-${count.toString().padStart(4, '0')}"
    }
}