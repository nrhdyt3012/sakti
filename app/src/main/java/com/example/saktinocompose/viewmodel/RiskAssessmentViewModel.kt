package com.example.saktinocompose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.data.entity.RiskAssessment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RiskAssessmentViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val riskAssessmentDao = database.riskAssessmentDao()

    fun getRiskAssessmentFlow(changeRequestId: Int): Flow<RiskAssessment?> {
        return riskAssessmentDao.getRiskAssessmentFlow(changeRequestId)
    }

    suspend fun getRiskAssessment(changeRequestId: Int): RiskAssessment? {
        return riskAssessmentDao.getRiskAssessmentByChangeRequest(changeRequestId)
    }

    fun saveRiskAssessment(
        changeRequestId: String,
        teknisiId: Int,
        teknisiName: String,
        skorDampak: Int,
        skorKemungkinan: Int,
        skorEksposur: Int,
        skorRisiko: Int,
        levelRisiko: String
    ) {
        viewModelScope.launch {
            val existing = riskAssessmentDao.getRiskAssessmentByChangeRequest(changeRequestId)

            if (existing != null) {
                // Update existing
                val updated = existing.copy(
                    teknisiId = teknisiId,
                    teknisiName = teknisiName,
                    skorDampak = skorDampak,
                    skorKemungkinan = skorKemungkinan,
                    skorEksposur = skorEksposur,
                    skorRisiko = skorRisiko,
                    levelRisiko = levelRisiko,
                    createdAt = System.currentTimeMillis()
                )
                riskAssessmentDao.updateRiskAssessment(updated)
            } else {
                // Insert new
                val riskAssessment = RiskAssessment(
                    changeRequestId = changeRequestId,
                    teknisiId = teknisiId,
                    teknisiName = teknisiName,
                    skorDampak = skorDampak,
                    skorKemungkinan = skorKemungkinan,
                    skorEksposur = skorEksposur,
                    skorRisiko = skorRisiko,
                    levelRisiko = levelRisiko
                )
                riskAssessmentDao.insertRiskAssessment(riskAssessment)
            }
        }
    }
}