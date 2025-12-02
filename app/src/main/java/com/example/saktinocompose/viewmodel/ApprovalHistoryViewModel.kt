package com.example.saktinocompose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.data.entity.ApprovalHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ApprovalHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val approvalHistoryDao = database.approvalHistoryDao()

    fun getApprovalHistoryByChangeRequest(changeRequestId: String): Flow<List<ApprovalHistory>> {
        return approvalHistoryDao.getApprovalHistoryByChangeRequest(changeRequestId)
    }

    fun addApprovalHistory(
        changeRequestId: String,  // ✅ Already String
        approverUserId: String,  // ✅ Changed from Int to String
        approverName: String,
        fromStatus: String,
        toStatus: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val approvalHistory = ApprovalHistory(
                changeRequestId = changeRequestId,
                approverUserId = approverUserId.toIntOrNull() ?: 0,  // ✅ Convert to Int for DB
                approverName = approverName,
                fromStatus = fromStatus,
                toStatus = toStatus,
                notes = notes
            )
            approvalHistoryDao.insertApprovalHistory(approvalHistory)
        }
    }
}