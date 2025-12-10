package com.example.saktinocompose.repository

import android.util.Log
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChangeRequestRepository {

    suspend fun fetchFromApi(
        page: Int? = null,
        limit: Int? = null,
        status: String? = null,
        type: String? = null
    ): Result<List<ChangeRequest>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken

                Log.d("ChangeRequestRepo", """
                    🔄 Fetching change requests:
                    - Token: ${token?.take(20)}...
                    ${if (page != null || limit != null || status != null || type != null) {
                    """
                        - Filters: page=$page, limit=$limit, status=$status, type=$type
                        """.trimIndent()
                } else {
                    "- Fetching ALL data (no filters)"
                }}
                """.trimIndent())

                if (token == null) {
                    Log.e("ChangeRequestRepo", "No token available")
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required. Please login again."
                    )
                }

                val response = RetrofitClient.changeRequestService.getChangeRequests(
                    page = page,
                    limit = limit,
                    status = status,
                    type = type
                )

                Log.d("ChangeRequestRepo", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiRequests = response.body()?.data ?: emptyList()
                    Log.d("ChangeRequestRepo", "Fetched ${apiRequests.size} requests")

                    val changeRequests = apiRequests.map { apiDataToChangeRequest(it) }
                    Result.Success(changeRequests)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = response.body()?.message ?: errorBody ?: "Failed to fetch data"
                    Log.e("ChangeRequestRepo", "API Error: $errorMessage")

                    if (response.code() == 401) {
                        RetrofitClient.clearAuthToken()
                        return@withContext Result.Error(
                            Exception("Token expired"),
                            "Session expired. Please login again."
                        )
                    }

                    Result.Error(Exception("Fetch failed"), errorMessage)
                }
            } catch (e: Exception) {
                Log.e("ChangeRequestRepo", "Exception during fetch", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    suspend fun fetchAll(): Result<List<ChangeRequest>> {
        return fetchFromApi()
    }

    suspend fun fetchByStatus(status: String): Result<List<ChangeRequest>> {
        return fetchFromApi(status = status)
    }

    suspend fun fetchByType(type: String): Result<List<ChangeRequest>> {
        return fetchFromApi(type = type)
    }

    suspend fun updateChangeRequest(changeRequest: ChangeRequest): Result<ChangeRequest> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                // TODO: Implement API call untuk update
                Result.Success(changeRequest)
            } catch (e: Exception) {
                Result.Error(e, "Update failed: ${e.message}")
            }
        }
    }

    suspend fun fetchNonEmergencyChangeRequests(): Result<List<ChangeRequest>> {
        return when (val result = fetchFromApi()) {
            is Result.Success -> {
                val filtered = result.data.filter {
                    it.jenisPerubahan.uppercase() != "EMERGENCY"
                }
                Log.d("ChangeRequestRepo", "Filtered out Emergency. Total: ${filtered.size}")
                Result.Success(filtered)
            }
            is Result.Error -> result
            else -> result
        }
    }
    /**
     * ✅ UPDATED: Mapping dengan Ticket ID yang benar
     */
    private fun apiDataToChangeRequest(apiData: ChangeRequestApiData): ChangeRequest {
        Log.d("ChangeRequestRepo", """
            Mapping CR:
            - CR ID: ${apiData.crId}
            - Ticket ID: ${apiData.tiketId}
            - Status: ${apiData.status}
        """.trimIndent())

        return ChangeRequest(
            id = apiData.crId,
            // ✅ PERBAIKAN: Gunakan tiketId dari API, bukan crId
            ticketId = apiData.tiketId ?: apiData.crId,
            type = apiData.type ?: apiData.changeType ?: "Standard",
            title = apiData.title ?: "No Title",
            description = apiData.description ?: "",
            assetId = apiData.assetId ?: "",
            asetTerdampak = apiData.assetId ?: "",
            relasiConfigurationItem = "",
            rencanaImplementasi = apiData.mitigationPlan ?: "",
            usulanJadwal = apiData.targetCompletion ?: "",
            rollbackPlan = apiData.rollbackPlan ?: "",
            assignedTeknisiName = apiData.picImplementation,
            scheduledDate = apiData.scheduleImplementation,
            scheduleStart = apiData.scheduleStart,
            scheduleEnd = apiData.scheduleEnd,
            scoreImpact = apiData.scoreImpact ?: 0,
            scoreLikelihood = apiData.scoreLikelihood ?: 0,
            scoreRisk = apiData.scoreRisk ?: 0,
            riskLevel = apiData.riskLevel ?: "Low",
            postImpact = apiData.postImpact,
            postLikelihood = apiData.postLikelihood,
            postResidualScore = apiData.postResidualScore,
            postRiskLevel = apiData.postRiskLevel,
            implementationResult = apiData.implementationResult,
            // ✅ PERBAIKAN: Map status dengan benar termasuk "NEED APPROVAL"
            status = mapApiStatusToLocalStatus(apiData.status, apiData.approvalStatus),
            approvalStatus = apiData.approvalStatus,
            createdAt = apiData.createdAt,
            updatedAt = apiData.updatedAt,
            dinas = apiData.dinas,
            impactDesc = apiData.impactDesc,
            controlExisting = apiData.controlExisting,
            controlEffectiveness = apiData.controlEffectiveness,
            mitigationPlan = apiData.mitigationPlan,
            jenisPerubahan = apiData.changeType ?: apiData.type ?: "Standard",
            skorEksposur = calculateExposure(
                apiData.scoreImpact,
                apiData.scoreLikelihood,
                apiData.scoreRisk
            )
        )
    }

    private fun calculateExposure(impact: Int?, likelihood: Int?, totalRisk: Int?): Int {
        if (impact == null || likelihood == null || totalRisk == null) return 1
        if (impact == 0 || likelihood == 0) return 1

        val baseRisk = impact * likelihood
        if (baseRisk == 0) return 1

        val calculatedExposure = (totalRisk.toDouble() / baseRisk.toDouble()).toInt()
        return calculatedExposure.coerceIn(1, 4)
    }

    /**
     * ✅ UPDATED: Tambah mapping untuk "NEED APPROVAL"
     */
    private fun mapApiStatusToLocalStatus(apiStatus: String, approvalStatus: String?): String {
        // ✅ PERBAIKAN: Check approval_status dulu
        if (approvalStatus == "NEED APPROVAL") {
            return "Need Approval"
        }

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
            "EMERGENCY" -> "Emergency"  // ✅ TAMBAH: Handle emergency status
            else -> apiStatus
        }
    }
}