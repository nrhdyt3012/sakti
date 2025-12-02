package com.example.saktinocompose.data.dao

import androidx.room.*
import com.example.saktinocompose.data.entity.ApprovalHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ApprovalHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovalHistory(approvalHistory: ApprovalHistory): Long

    @Query("SELECT * FROM approval_history WHERE changeRequestId = :changeRequestId ORDER BY timestamp ASC")
    fun getApprovalHistoryByChangeRequest(changeRequestId: String): Flow<List<ApprovalHistory>>

    @Query("SELECT * FROM approval_history WHERE changeRequestId = :changeRequestId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestApprovalHistory(changeRequestId: String): ApprovalHistory?

    @Delete
    suspend fun deleteApprovalHistory(approvalHistory: ApprovalHistory)
}