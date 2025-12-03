package com.example.saktinocompose.repository

import android.util.Log
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
     * ✅ FIXED: Fetch dari API dengan error handling yang lebih baik
     */
    suspend fun fetchFromApi(
        status: String? = null,
        deskripsi: String? = null
    ): Result<List<ChangeRequest>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken

                Log.d("ChangeRequestRepo", "Fetching with token: ${token?.take(20)}...")

                if (token == null) {
                    Log.e("ChangeRequestRepo", "No token available")
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required. Please login again."
                    )
                }

                val response = RetrofitClient.changeRequestService.getChangeRequests(
                    "Bearer $token",
                )

                Log.d("ChangeRequestRepo", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body()?.status == "success") {
                    val apiRequests = response.body()?.data ?: emptyList()

                    Log.d("ChangeRequestRepo", "Fetched ${apiRequests.size} requests")

                    // ✅ Convert API response ke Entity
                    val localRequests = apiRequests.map { apiDataToChangeRequest(it) }

                    // ✅ Cache ke database (replace all)
                    changeRequestDao.clearAll()
                    changeRequestDao.insertAll(localRequests)

                    Result.Success(localRequests)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = response.body()?.message ?: errorBody ?: "Failed to fetch data"

                    Log.e("ChangeRequestRepo", "API Error: $errorMessage")

                    // ✅ Check for auth error
                    if (response.code() == 401) {
                        RetrofitClient.clearAuthToken()
                        return@withContext Result.Error(
                            Exception("Token expired"),
                            "Session expired. Please login again."
                        )
                    }

                    Result.Error(
                        Exception("Fetch failed"),
                        errorMessage
                    )
                }
            } catch (e: Exception) {
                Log.e("ChangeRequestRepo", "Exception during fetch", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    /**
     * ✅ IMPROVED: Mapping dari API response ke local entity
     * Sesuai dengan struktur JSON yang Anda berikan
     */
    private fun apiDataToChangeRequest(apiData: ChangeRequestApiData): ChangeRequest {
        Log.d("ChangeRequestRepo", "Mapping CR: ${apiData.crId}")

        return ChangeRequest(
            id = apiData.crId,
            ticketId = apiData.ticketId ?: apiData.crId,

            // ✅ Type dari API response
            type = apiData.type ?: apiData.changeType ?: "Standard",

            // ✅ Title dan Description
            title = apiData.title ?: "No Title",
            description = apiData.description ?: "",

            // ✅ Asset - handle null values
            assetId = apiData.assetId ?: "",
            asetTerdampak = apiData.assetId ?: "",
            relasiConfigurationItem = "",

            // ✅ Implementation
            rencanaImplementasi = apiData.mitigationPlan ?: "",
            usulanJadwal = apiData.targetCompletion ?: "",
            rollbackPlan = apiData.rollbackPlan ?: "",

            // ✅ Technician
            assignedTeknisiName = apiData.picImplementation,
            scheduledDate = apiData.scheduleImplementation,

            // ✅ Schedule
            scheduleStart = apiData.scheduleStart,
            scheduleEnd = apiData.scheduleEnd,

            // ✅ Risk scores (handle null)
            scoreImpact = apiData.scoreImpact ?: 0,
            scoreLikelihood = apiData.scoreLikelihood ?: 0,
            scoreRisk = apiData.scoreRisk ?: 0,
            riskLevel = apiData.riskLevel ?: "Low",

            // ✅ Post-implementation
            postImpact = apiData.postImpact,
            postLikelihood = apiData.postLikelihood,
            postResidualScore = apiData.postResidualScore,
            postRiskLevel = apiData.postRiskLevel,
            implementationResult = apiData.implementationResult,

            // ✅ Status mapping
            status = mapApiStatusToLocalStatus(apiData.status),
            approvalStatus = apiData.approvalStatus,

            // ✅ Timestamps
            createdAt = apiData.createdAt,
            updatedAt = apiData.updatedAt,

            // ✅ Additional fields
            dinas = apiData.dinas,
            impactDesc = apiData.impactDesc,
            controlExisting = apiData.controlExisting,
            controlEffectiveness = apiData.controlEffectiveness,
            mitigationPlan = apiData.mitigationPlan,

            // ✅ CRITICAL: Field yang diperlukan UI
            jenisPerubahan = apiData.changeType ?: apiData.type ?: "Standard",
            skorEksposur = calculateExposure(
                apiData.scoreImpact,
                apiData.scoreLikelihood,
                apiData.scoreRisk
            )
        )
    }

    /**
     * ✅ Calculate exposure from existing scores
     */
    private fun calculateExposure(impact: Int?, likelihood: Int?, totalRisk: Int?): Int {
        if (impact == null || likelihood == null || totalRisk == null) return 1
        if (impact == 0 || likelihood == 0) return 1

        val baseRisk = impact * likelihood
        if (baseRisk == 0) return 1

        // Formula: totalRisk = impact * likelihood * exposure
        // exposure = totalRisk / (impact * likelihood)
        val calculatedExposure = (totalRisk.toDouble() / baseRisk.toDouble()).toInt()
        return calculatedExposure.coerceIn(1, 4)
    }

    /**
     * ✅ Map API status to local status
     */
    private fun mapApiStatusToLocalStatus(apiStatus: String): String {
        return when (apiStatus.uppercase()) {
            "SUBMITTED", "PENDING" -> "Submitted"
            "REVIEWED", "IN_REVIEW" -> "Reviewed"
            "REVISION", "NEED_REVISION" -> "Revision"
            "APPROVED" -> "Approved"
            "SCHEDULED" -> "Scheduled"
            "IMPLEMENTING", "IN_PROGRESS" -> "Implementing"
            "COMPLETED", "DONE" -> "Completed"
            "FAILED", "REJECTED" -> "Failed"
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