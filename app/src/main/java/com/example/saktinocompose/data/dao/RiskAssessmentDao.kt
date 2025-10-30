package com.example.saktinocompose.data.dao

import androidx.room.*
import com.example.saktinocompose.data.entity.RiskAssessment
import kotlinx.coroutines.flow.Flow

@Dao
interface RiskAssessmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskAssessment(riskAssessment: RiskAssessment): Long

    @Query("SELECT * FROM risk_assessments WHERE changeRequestId = :changeRequestId LIMIT 1")
    suspend fun getRiskAssessmentByChangeRequest(changeRequestId: Int): RiskAssessment?

    @Query("SELECT * FROM risk_assessments WHERE changeRequestId = :changeRequestId")
    fun getRiskAssessmentFlow(changeRequestId: Int): Flow<RiskAssessment?>

    @Update
    suspend fun updateRiskAssessment(riskAssessment: RiskAssessment)

    @Delete
    suspend fun deleteRiskAssessment(riskAssessment: RiskAssessment)
}