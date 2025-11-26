// 1. Updated ChangeRequestViewModel dengan Repository
// File: app/src/main/java/com/example/saktinocompose/viewmodel/ChangeRequestViewModel.kt

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

    // ===== BARU: Repository untuk handle API =====
    private val repository = ChangeRequestRepository(changeRequestDao)

    // State untuk tracking sync
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError = _syncError.asStateFlow()

    /**
     * Submit Change Request - Auto sync ke API jika online
     */
    fun submitChangeRequest(
        userId: Int,
        idPerubahan: String,  // ✅ UUID parameter
        jenisPerubahan: String,
        alasan: String,
        tujuan: String,
        asetTerdampak: String,
        rencanaImplementasi: String,
        usulanJadwal: String,
        rencanaRollback: String,
        assignedTeknisiId: Int?,
        assignedTeknisiName: String?
    ) {
        viewModelScope.launch {
            val ticketId = generateTicketId()
            val changeRequest = ChangeRequest(
                ticketId = ticketId,
                userId = userId,
                idPerubahan = idPerubahan,  // ✅ Set ID Perubahan
                jenisPerubahan = jenisPerubahan,
                alasan = alasan,
                tujuan = tujuan,
                asetTerdampak = asetTerdampak,
                rencanaImplementasi = rencanaImplementasi,
                usulanJadwal = usulanJadwal,
                rencanaRollback = rencanaRollback,
                assignedTeknisiId = assignedTeknisiId,
                assignedTeknisiName = assignedTeknisiName,
                status = "Submitted"
            )

            repository.submitChangeRequest(changeRequest)
        }
    }

    fun updateChangeRequestForRevision(
        existingRequest: ChangeRequest,
        idPerubahan: String,  // ✅ UUID parameter
        jenisPerubahan: String,
        alasan: String,
        tujuan: String,
        asetTerdampak: String,
        rencanaImplementasi: String,
        usulanJadwal: String,
        rencanaRollback: String,
        assignedTeknisiId: Int?,
        assignedTeknisiName: String?
    ) {
        viewModelScope.launch {
            val updated = existingRequest.copy(
                idPerubahan = idPerubahan,  // ✅ Keep same ID Perubahan
                jenisPerubahan = jenisPerubahan,
                alasan = alasan,
                tujuan = tujuan,
                asetTerdampak = asetTerdampak,
                rencanaImplementasi = rencanaImplementasi,
                usulanJadwal = usulanJadwal,
                rencanaRollback = rencanaRollback,
                assignedTeknisiId = assignedTeknisiId,
                assignedTeknisiName = assignedTeknisiName,
                status = "Submitted",
                revisionNotes = null,
                revisionCount = existingRequest.revisionCount + 1,
                updatedAt = System.currentTimeMillis()
            )

            repository.updateChangeRequest(updated)
        }
    }

    /**
     * Get Change Requests by User
     */
    fun getChangeRequestsByUser(userId: Int): Flow<List<ChangeRequest>> {
        return repository.getChangeRequestsByUser(userId)
    }

    /**
     * Get All Change Requests (untuk Teknisi)
     */
    fun getAllChangeRequests(): Flow<List<ChangeRequest>> {
        return repository.getAllChangeRequests()
    }

    /**
     * Get Change Requests by Status
     */
    fun getChangeRequestsByStatus(status: String): Flow<List<ChangeRequest>> {
        return changeRequestDao.getChangeRequestsByStatus(status)
    }

    /**
     * Get Change Request by ID
     */
    suspend fun getChangeRequestById(id: Int): ChangeRequest? {
        return repository.getChangeRequestById(id)
    }

    /**
     * Update Status - dengan sync ke API
     */
    fun updateChangeRequestStatus(
        changeRequest: ChangeRequest,
        newStatus: String,
        teknisiId: Int? = null,
        teknisiName: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            // Update via repository dengan sync ke API
            repository.updateStatus(
                changeRequest = changeRequest,
                newStatus = newStatus,
                teknisiId = teknisiId,
                teknisiName = teknisiName,
                notes = notes
            )
        }
    }

    /**
     * Update Full Change Request (untuk inspection, scheduling, dll)
     */
    fun updateFullChangeRequest(updatedRequest: ChangeRequest) {
        viewModelScope.launch {
            val updated = updatedRequest.copy(
                updatedAt = System.currentTimeMillis()
            )

            // Update via repository
            repository.updateChangeRequest(updated)
        }
    }

    /**
     * Manual Sync dari API
     */
    fun syncFromApi() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null

            when (val result = repository.syncFromApi()) {
                is Result.Success -> {
                    _syncError.value = null
                    // Data sudah ter-update di local DB
                    // Flow otomatis emit data baru
                }
                is Result.Error -> {
                    _syncError.value = result.message
                }
                else -> {
                    _syncError.value = "Sync failed"
                }
            }

            _isSyncing.value = false
        }
    }

    /**
     * Clear sync error
     */
    fun clearSyncError() {
        _syncError.value = null
    }

    // Private helper
    private suspend fun generateTicketId(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val count = changeRequestDao.getTodayRequestCount() + 1
        return "CR-$today-${count.toString().padStart(4, '0')}"
    }
}
