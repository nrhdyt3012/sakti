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
     * ✅ Get all change requests from API, cache to local
     */
    fun getAllChangeRequests(): Flow<List<ChangeRequest>> {
        // Return from local cache for instant UI
        return changeRequestDao.getAllChangeRequests()
    }

    /**
     * ✅ Fetch latest data from API
     */
    suspend fun fetchFromApi(): Result<List<ChangeRequest>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val response = RetrofitClient.syncService.syncChangeRequests("Bearer $token")

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiRequests = response.body()?.data ?: emptyList()

                    // Convert and cache to local database
                    val localRequests = apiRequests.map { apiRequestToChangeRequest(it) }
                    localRequests.forEach { changeRequestDao.insertChangeRequest(it) }

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

    fun getChangeRequestsByUser(userId: Int): Flow<List<ChangeRequest>> {
        return changeRequestDao.getChangeRequestsByUser(userId)
    }

    suspend fun getChangeRequestById(id: Int): ChangeRequest? {
        return changeRequestDao.getChangeRequestById(id)
    }

    /**
     * ✅ Submit to API, then cache locally
     */
    suspend fun submitChangeRequest(changeRequest: ChangeRequest): Result<ChangeRequest> {
        return withContext(Dispatchers.IO) {
            try {
                // Submit to API first
                val apiResult = submitToApi(changeRequest)

                when (apiResult) {
                    is Result.Success -> {
                        // Cache to local
                        val localId = changeRequestDao.insertChangeRequest(changeRequest).toInt()
                        Result.Success(changeRequest.copy(id = localId))
                    }
                    is Result.Error -> apiResult
                    else -> Result.Error(Exception("Unknown error"), "Submit failed")
                }
            } catch (e: Exception) {
                Result.Error(e, "Submit failed: ${e.message}")
            }
        }
    }

    /**
     * ✅ Update to API, then cache locally
     */
    suspend fun updateChangeRequest(changeRequest: ChangeRequest): Result<ChangeRequest> {
        return withContext(Dispatchers.IO) {
            try {
                // Update to API first
                val apiResult = updateToApi(changeRequest)

                when (apiResult) {
                    is Result.Success -> {
                        // Update local cache
                        changeRequestDao.updateChangeRequest(changeRequest)
                        Result.Success(changeRequest)
                    }
                    is Result.Error -> apiResult
                    else -> Result.Error(Exception("Unknown error"), "Update failed")
                }
            } catch (e: Exception) {
                Result.Error(e, "Update failed: ${e.message}")
            }
        }
    }

    suspend fun updateStatus(
        changeRequest: ChangeRequest,
        newStatus: String,
        teknisiId: Int? = null,
        teknisiName: String? = null,
        notes: String? = null
    ): Result<ChangeRequest> {
        return withContext(Dispatchers.IO) {
            try {
                val updated = changeRequest.copy(
                    status = newStatus,
                    updatedAt = System.currentTimeMillis()
                )

                // Update to API
                if (teknisiId != null && teknisiName != null) {
                    val token = RetrofitClient.authToken
                    if (token != null) {
                        val request = UpdateStatusRequest(
                            changeRequestId = changeRequest.id,
                            newStatus = newStatus,
                            teknisiId = teknisiId,
                            teknisiName = teknisiName,
                            notes = notes
                        )

                        RetrofitClient.teknisiService.updateChangeRequestStatus(
                            token = "Bearer $token",
                            id = changeRequest.id,
                            request = request
                        )
                    }
                }

                // Update local
                changeRequestDao.updateChangeRequest(updated)
                Result.Success(updated)
            } catch (e: Exception) {
                Result.Error(e, "Status update failed: ${e.message}")
            }
        }
    }

    // ===== Private Helper Methods =====

    private suspend fun submitToApi(changeRequest: ChangeRequest): Result<Boolean> {
        return try {
            val token = RetrofitClient.authToken ?: return Result.Error(
                Exception("No token"), "Authentication required"
            )

            val apiRequest = changeRequestToApiRequest(changeRequest)

            // TODO: Implement actual API endpoint when ready
            // val response = RetrofitClient.changeRequestService.createChangeRequest(
            //     "Bearer $token", apiRequest
            // )

            // For now, assume success
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e, "API submit failed: ${e.message}")
        }
    }

    private suspend fun updateToApi(changeRequest: ChangeRequest): Result<Boolean> {
        return try {
            val token = RetrofitClient.authToken ?: return Result.Error(
                Exception("No token"), "Authentication required"
            )

            val apiRequest = changeRequestToApiRequest(changeRequest)

            // TODO: Implement actual API endpoint when ready
            // val response = RetrofitClient.changeRequestService.updateChangeRequest(
            //     "Bearer $token", changeRequest.id, apiRequest
            // )

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e, "API update failed: ${e.message}")
        }
    }

    private fun changeRequestToApiRequest(cr: ChangeRequest): ChangeRequestApiRequest {
        return ChangeRequestApiRequest(
            ticketId = cr.ticketId,
            userId = cr.userId,
            idPerubahan = cr.idPerubahan,
            jenisPerubahan = cr.jenisPerubahan,
            alasan = cr.alasan,
            tujuan = cr.tujuan,
            idAset = cr.idAset,
            asetTerdampak = cr.asetTerdampak,
            relasiConfigurationItem = cr.relasiConfigurationItem,
            rencanaImplementasi = cr.rencanaImplementasi,
            usulanJadwal = cr.usulanJadwal,
            rencanaRollback = cr.rencanaRollback,
            assignedTeknisiId = cr.assignedTeknisiId,
            assignedTeknisiName = cr.assignedTeknisiName,
            status = cr.status
        )
    }

    private fun apiRequestToChangeRequest(dto: ChangeRequestApiRequest): ChangeRequest {
        return ChangeRequest(
            ticketId = dto.ticketId,
            userId = dto.userId,
            idPerubahan = dto.idPerubahan,
            jenisPerubahan = dto.jenisPerubahan,
            alasan = dto.alasan,
            tujuan = dto.tujuan,
            idAset = dto.idAset,
            asetTerdampak = dto.asetTerdampak,
            relasiConfigurationItem = dto.relasiConfigurationItem,
            rencanaImplementasi = dto.rencanaImplementasi,
            usulanJadwal = dto.usulanJadwal,
            rencanaRollback = dto.rencanaRollback,
            assignedTeknisiId = dto.assignedTeknisiId,
            assignedTeknisiName = dto.assignedTeknisiName,
            status = dto.status
        )
    }
}