package com.example.saktinocompose.data.dao

import androidx.room.*
import com.example.saktinocompose.data.entity.ChangeRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface ChangeRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChangeRequest(changeRequest: ChangeRequest): Long

    @Query("SELECT * FROM change_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getChangeRequestsByUser(userId: Int): Flow<List<ChangeRequest>>

    @Query("SELECT * FROM change_requests ORDER BY createdAt DESC")
    fun getAllChangeRequests(): Flow<List<ChangeRequest>>

    @Query("SELECT * FROM change_requests WHERE status = :status ORDER BY createdAt DESC")
    fun getChangeRequestsByStatus(status: String): Flow<List<ChangeRequest>>

    @Query("SELECT * FROM change_requests WHERE id = :id LIMIT 1")
    suspend fun getChangeRequestById(id: Int): ChangeRequest?

    @Update
    suspend fun updateChangeRequest(changeRequest: ChangeRequest)

    @Delete
    suspend fun deleteChangeRequest(changeRequest: ChangeRequest)

    @Query("SELECT COUNT(*) FROM change_requests WHERE DATE(createdAt/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayRequestCount(): Int
}