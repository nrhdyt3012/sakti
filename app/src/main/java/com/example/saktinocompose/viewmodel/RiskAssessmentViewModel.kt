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

    fun getRiskAssessmentFlow(changeRequestId: String): Flow<RiskAssessment?> {
        return riskAssessmentDao.getRiskAssessmentFlow(changeRequestId)
    }

    suspend fun getRiskAssessment(changeRequestId: String): RiskAssessment? {
        return riskAssessmentDao.getRiskAssessmentByChangeRequest(changeRequestId)
    }

    fun saveRiskAssessment(
        changeRequestId: String,  // ✅ Already String
        teknisiId: String,  // ✅ Changed from Int to String
        teknisiName: String,
        skorDampak: Int,
        skorKemungkinan: Int,
        skorEksposur: Int,
        skorRisiko: Int,
        levelRisiko: String
    ) {
        viewModelScope.launch {
            val existing = riskAssessmentDao.getRiskAssessmentByChangeRequest(changeRequestId)

            val teknisiIdInt = teknisiId.toIntOrNull() ?: 0  // ✅ Convert to Int

            if (existing != null) {
                val updated = existing.copy(
                    teknisiId = teknisiIdInt,
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
                val riskAssessment = RiskAssessment(
                    changeRequestId = changeRequestId,
                    teknisiId = teknisiIdInt,
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