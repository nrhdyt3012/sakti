package com.example.saktinocompose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.model.RiskAssessment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RiskAssessmentViewModel(application: Application) : AndroidViewModel(application) {

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

            val teknisiIdInt = teknisiId.toIntOrNull() ?: 0  // ✅ Convert to Int

//            if (existing != null) {
//                val updated = existing.copy(
//                    teknisiId = teknisiIdInt,
//                    teknisiName = teknisiName,
//                    skorDampak = skorDampak,
//                    skorKemungkinan = skorKemungkinan,
//                    skorEksposur = skorEksposur,
//                    skorRisiko = skorRisiko,
//                    levelRisiko = levelRisiko,
//                    createdAt = System.currentTimeMillis()
//                )
//            } else {
//                val riskAssessment = RiskAssessment(
//                    changeRequestId = changeRequestId,
//                    teknisiId = teknisiIdInt,
//                    teknisiName = teknisiName,
//                    skorDampak = skorDampak,
//                    skorKemungkinan = skorKemungkinan,
//                    skorEksposur = skorEksposur,
//                    skorRisiko = skorRisiko,
//                    levelRisiko = levelRisiko
////                )
//            }
        }
    }
}