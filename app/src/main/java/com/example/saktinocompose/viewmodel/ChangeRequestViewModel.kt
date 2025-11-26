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

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError = _syncError.asStateFlow()

    /**
     * ✅ UPDATED: Submit Change Request dengan 3 field aset
     */
    fun submitChangeRequest(
        userId: Int,
        idPerubahan: String,
        jenisPerubahan: String,
        alasan: String,
        tujuan: String,
        idAset: String,                    // ✅ BARU
        asetTerdampak: String,              // ✅ UPDATED
        relasiConfigurationItem: String,    // ✅ BARU
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
                idPerubahan = idPerubahan,
                jenisPerubahan = jenisPerubahan,
                alasan = alasan,
                tujuan = tujuan,
                idAset = idAset,
                asetTerdampak = asetTerdampak,
                relasiConfigurationItem = relasiConfigurationItem,
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

    /**
     * ✅ UPDATED: Update untuk revisi dengan 3 field aset
     */
    fun updateChangeRequestForRevision(
        existingRequest: ChangeRequest,
        idPerubahan: String,
        jenisPerubahan: String,
        alasan: String,
        tujuan: String,
        idAset: String,                    // ✅ BARU
        asetTerdampak: String,              // ✅ UPDATED
        relasiConfigurationItem: String,    // ✅ BARU
        rencanaImplementasi: String,
        usulanJadwal: String,
        rencanaRollback: String,
        assignedTeknisiId: Int?,
        assignedTeknisiName: String?
    ) {
        viewModelScope.launch {
            val updated = existingRequest.copy(
                idPerubahan = idPerubahan,
                jenisPerubahan = jenisPerubahan,
                alasan = alasan,
                tujuan = tujuan,
                idAset = idAset,
                asetTerdampak = asetTerdampak,
                relasiConfigurationItem = relasiConfigurationItem,
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

    fun getChangeRequestsByUser(userId: Int): Flow<List<ChangeRequest>> {
        return repository.getChangeRequestsByUser(userId)
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
            repository.updateStatus(
                changeRequest = changeRequest,
                newStatus = newStatus,
                teknisiId = teknisiId,
                teknisiName = teknisiName,
                notes = notes
            )
        }
    }

    fun updateFullChangeRequest(updatedRequest: ChangeRequest) {
        viewModelScope.launch {
            val updated = updatedRequest.copy(
                updatedAt = System.currentTimeMillis()
            )
            repository.updateChangeRequest(updated)
        }
    }

    fun syncFromApi() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null

            when (val result = repository.syncFromApi()) {
                is Result.Success -> {
                    _syncError.value = null
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

    fun clearSyncError() {
        _syncError.value = null
    }

    private suspend fun generateTicketId(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val count = changeRequestDao.getTodayRequestCount() + 1
        return "CR-$today-${count.toString().padStart(4, '0')}"
    }
}