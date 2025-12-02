package com.example.saktinocompose.data.dao

import androidx.room.*
import com.example.saktinocompose.data.entity.ChangeRequest
import kotlinx.coroutines.flow.Flow

// File: app/src/main/java/com/example/saktinocompose/data/dao/ChangeRequestDao.kt

@Dao
interface ChangeRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChangeRequest(changeRequest: ChangeRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(changeRequests: List<ChangeRequest>)

    @Query("SELECT * FROM change_requests ORDER BY createdAt DESC")
    fun getAllChangeRequests(): Flow<List<ChangeRequest>>

    @Query("SELECT * FROM change_requests WHERE status = :status ORDER BY createdAt DESC")
    fun getChangeRequestsByStatus(status: String): Flow<List<ChangeRequest>>

    @Query("SELECT * FROM change_requests WHERE id = :crId LIMIT 1")  // ✅ Pakai String
    suspend fun getChangeRequestById(crId: String): ChangeRequest?

    @Update
    suspend fun updateChangeRequest(changeRequest: ChangeRequest)

    @Delete
    suspend fun deleteChangeRequest(changeRequest: ChangeRequest)

    @Query("DELETE FROM change_requests")
    suspend fun clearAll()
}