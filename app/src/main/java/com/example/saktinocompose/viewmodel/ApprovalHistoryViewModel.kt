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

    fun getApprovalHistoryByChangeRequest(changeRequestId: Int): Flow<List<ApprovalHistory>> {
        return approvalHistoryDao.getApprovalHistoryByChangeRequest(changeRequestId)
    }

    fun addApprovalHistory(
        changeRequestId: String,
        approverUserId: Int,
        approverName: String,
        fromStatus: String,
        toStatus: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val approvalHistory = ApprovalHistory(
                changeRequestId = changeRequestId,
                approverUserId = approverUserId,
                approverName = approverName,
                fromStatus = fromStatus,
                toStatus = toStatus,
                notes = notes
            )
            approvalHistoryDao.insertApprovalHistory(approvalHistory)
        }
    }
}