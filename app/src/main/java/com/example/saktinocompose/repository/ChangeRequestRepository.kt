package com.example.saktinocompose.repository

import com.example.saktinocompose.data.dao.ChangeRequestDao
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChangeRequestRepository(
    private val changeRequestDao: ChangeRequestDao
) {

    /**
     * ✅ Fetch dari API dan cache ke database
     */
    suspend fun fetchFromApi(
        status: String? = null,
        deskripsi: String? = null
    ): Result<List<ChangeRequest>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val response = RetrofitClient.changeRequestService.getChangeRequests(
                    "Bearer $token",
                    status,
                    deskripsi
                )

                if (response.isSuccessful && response.body()?.status == "success") {
                    val apiRequests = response.body()?.data ?: emptyList()

                    // ✅ Convert API response ke Entity
                    val localRequests = apiRequests.map { apiDataToChangeRequest(it) }

                    // ✅ Cache ke database (replace all)
                    changeRequestDao.clearAll()
                    changeRequestDao.insertAll(localRequests)

                    Result.Success(localRequests)
                } else {
                    Result.Error(
                        Exception("Fetch failed"),
                        response.body()?.message ?: "Failed to fetch data"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    /**
     * ✅ Mapping dari API response ke local entity
     */
    private fun apiDataToChangeRequest(apiData: ChangeRequestApiData): ChangeRequest {
        return ChangeRequest(
            id = apiData.crId,
            ticketId = apiData.ticketId ?: apiData.crId,
            type = apiData.type,
            title = apiData.title,
            description = apiData.description ?: "",

            // Asset
            assetId = apiData.assetId ?: "",
            asetTerdampak = apiData.assetId ?: "",  // Fallback
            relasiConfigurationItem = "",  // ⚠️ Tidak ada di API, biarkan kosong

            // Implementation
            rencanaImplementasi = apiData.mitigationPlan ?: "",
            usulanJadwal = apiData.targetCompletion ?: "",
            rollbackPlan = apiData.rollbackPlan ?: "",

            // Technician
            assignedTeknisiName = apiData.picImplementation,
            scheduledDate = apiData.scheduleImplementation,

            // Inspection
            scheduleStart = apiData.scheduleStart,
            scheduleEnd = apiData.scheduleEnd,
            scoreImpact = apiData.scoreImpact,

            // Schedule
            scoreLikelihood = apiData.scoreLikelihood,
            scoreRisk = apiData.scoreRisk,
            riskLevel = apiData.riskLevel,

            // Risk
            postImpact = apiData.postImpact,
            postLikelihood = apiData.postLikelihood,
            postResidualScore = apiData.postResidualScore,
            postRiskLevel = apiData.postRiskLevel,

            // Post-implementation
            implementationResult = apiData.implementationResult,
            status = mapApiStatusToLocalStatus(apiData.status),
            approvalStatus = apiData.approvalStatus,
            createdAt = apiData.createdAt,
            updatedAt = apiData.updatedAt,

            // Status
            dinas = apiData.dinas,
            impactDesc = apiData.impactDesc,
            controlExisting = apiData.controlExisting,
            controlEffectiveness = apiData.controlEffectiveness,

            // Additional
            mitigationPlan = apiData.mitigationPlan,
        )
    }

    private fun mapApiStatusToLocalStatus(apiStatus: String): String {
        return when (apiStatus.uppercase()) {
            "SUBMITTED" -> "Submitted"
            "REVIEWED" -> "Reviewed"
            "APPROVED" -> "Approved"
            "SCHEDULED" -> "Scheduled"
            "IMPLEMENTING" -> "Implementing"
            "COMPLETED" -> "Completed"
            "FAILED" -> "Failed"
            "CLOSED" -> "Closed"
            else -> apiStatus
        }
    }

    // ===== Local Database Operations =====

    fun getAllChangeRequests(): Flow<List<ChangeRequest>> {
        return changeRequestDao.getAllChangeRequests()
    }

    fun getChangeRequestsByStatus(status: String): Flow<List<ChangeRequest>> {
        return changeRequestDao.getChangeRequestsByStatus(status)
    }

    suspend fun getChangeRequestById(crId: String): ChangeRequest? {
        return changeRequestDao.getChangeRequestById(crId)
    }

    suspend fun updateChangeRequest(changeRequest: ChangeRequest): Result<ChangeRequest> {
        return withContext(Dispatchers.IO) {
            try {
                changeRequestDao.updateChangeRequest(changeRequest)
                Result.Success(changeRequest)
            } catch (e: Exception) {
                Result.Error(e, "Update failed: ${e.message}")
            }
        }
    }
}