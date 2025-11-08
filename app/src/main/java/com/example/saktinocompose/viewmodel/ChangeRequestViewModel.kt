package com.example.saktinocompose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.data.entity.ChangeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChangeRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val changeRequestDao = database.changeRequestDao()

    fun submitChangeRequest(
        userId: Int,
        jenisPerubahan: String,
        alasan: String,
        tujuan: String,
        asetTerdampak: String,
        usulanJadwal: String,
        photoPath: String? = null
    ) {
        viewModelScope.launch {
            val ticketId = generateTicketId()
            val changeRequest = ChangeRequest(
                ticketId = ticketId,
                userId = userId,
                jenisPerubahan = jenisPerubahan,
                alasan = alasan,
                tujuan = tujuan,
                asetTerdampak = asetTerdampak,
                usulanJadwal = usulanJadwal,
                photoPath = photoPath,
                status = "Submitted"
            )
            changeRequestDao.insertChangeRequest(changeRequest)
        }
    }

    fun getChangeRequestsByUser(userId: Int): Flow<List<ChangeRequest>> {
        return changeRequestDao.getChangeRequestsByUser(userId)
    }

    fun getAllChangeRequests(): Flow<List<ChangeRequest>> {
        return changeRequestDao.getAllChangeRequests()
    }

    fun getChangeRequestsByStatus(status: String): Flow<List<ChangeRequest>> {
        return changeRequestDao.getChangeRequestsByStatus(status)
    }

    private suspend fun generateTicketId(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val count = changeRequestDao.getTodayRequestCount() + 1
        return "CR-$today-${count.toString().padStart(4, '0')}"
    }

    fun updateChangeRequestStatus(changeRequest: ChangeRequest, newStatus: String) {
        viewModelScope.launch {
            val updated = changeRequest.copy(
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            changeRequestDao.updateChangeRequest(updated)
        }
    }
}