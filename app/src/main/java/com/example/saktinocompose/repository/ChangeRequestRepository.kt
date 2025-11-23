// 2. Repository untuk Change Request
// File: app/src/main/java/com/example/saktinocompose/repository/ChangeRequestRepository.kt

package com.example.saktinocompose.repository

import com.example.saktinocompose.data.dao.ChangeRequestDao
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.network.ApiConfig
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
     * Get all change requests - support offline/online
     */
    fun getAllChangeRequests(): Flow<List<ChangeRequest>> {
        // Untuk sekarang, tetap gunakan Flow dari local database
        // Nanti bisa ditambahkan logic untuk sync dari API
        return changeRequestDao.getAllChangeRequests()
    }

    /**
     * Get change requests by user
     */
    fun getChangeRequestsByUser(userId: Int): Flow<List<ChangeRequest>> {
        return changeRequestDao.getChangeRequestsByUser(userId)
    }

    /**
     * Get change request by ID
     */
    suspend fun getChangeRequestById(id: Int): ChangeRequest? {
        return changeRequestDao.getChangeRequestById(id)
    }

    /**
     * Submit change request - support offline/online
     */
    suspend fun submitChangeRequest(changeRequest: ChangeRequest): Result<ChangeRequest> {
        return withContext(Dispatchers.IO) {
            try {
                // Simpan ke local database dulu
                val localId = changeRequestDao.insertChangeRequest(changeRequest).toInt()
                val savedRequest = changeRequest.copy(id = localId)

                // Jika online mode, sync ke API
                if (!ApiConfig.IS_OFFLINE_MODE) {
                    syncChangeRequestToApi(savedRequest)
                }

                Result.Success(savedRequest)
            } catch (e: Exception) {
                Result.Error(e, "Gagal submit change request: ${e.message}")
            }
        }
    }

    /**
     * Update change request - support offline/online
     */
    suspend fun updateChangeRequest(changeRequest: ChangeRequest): Result<ChangeRequest> {
        return withContext(Dispatchers.IO) {
            try {
                // Update local database
                changeRequestDao.updateChangeRequest(changeRequest)

                // Jika online mode, sync ke API
                if (!ApiConfig.IS_OFFLINE_MODE) {
                    syncChangeRequestToApi(changeRequest)
                }

                Result.Success(changeRequest)
            } catch (e: Exception) {
                Result.Error(e, "Gagal update change request: ${e.message}")
            }
        }
    }

    /**
     * Update status change request
     */
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

                // Update local
                changeRequestDao.updateChangeRequest(updated)

                // Sync ke API jika online
                if (!ApiConfig.IS_OFFLINE_MODE && teknisiId != null && teknisiName != null) {
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

                Result.Success(updated)
            } catch (e: Exception) {
                Result.Error(e, "Gagal update status: ${e.message}")
            }
        }
    }

    /**
     * Sync change request to API
     */
    private suspend fun syncChangeRequestToApi(changeRequest: ChangeRequest) {
        try {
            val token = RetrofitClient.authToken ?: return

            val apiRequest = changeRequestToApiRequest(changeRequest)

            // TODO: Implement API call ketika endpoint sudah siap
            // RetrofitClient.teknisiService.createOrUpdateChangeRequest(...)

        } catch (e: Exception) {
            // Log error tapi jangan throw, karena data sudah tersimpan di local
            println("Failed to sync to API: ${e.message}")
        }
    }

    /**
     * Sync change requests dari API ke local database
     */
    suspend fun syncFromApi(): Result<List<ChangeRequest>> {
        return withContext(Dispatchers.IO) {
            try {
                if (ApiConfig.IS_OFFLINE_MODE) {
                    return@withContext Result.Error(
                        Exception("Offline mode"),
                        "Tidak bisa sync dalam mode offline"
                    )
                }

                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Token tidak ditemukan"
                    )
                }

                val response = RetrofitClient.syncService.syncChangeRequests("Bearer $token")

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiRequests = response.body()?.data ?: emptyList()

                    // Convert dan simpan ke local database
                    val localRequests = apiRequests.map { apiRequestToChangeRequest(it) }
                    localRequests.forEach { changeRequestDao.insertChangeRequest(it) }

                    Result.Success(localRequests)
                } else {
                    Result.Error(
                        Exception("Sync failed"),
                        response.body()?.message ?: "Gagal sync data"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Gagal sync dari API: ${e.message}")
            }
        }
    }

    /**
     * Convert Entity to DTO
     */
    private fun changeRequestToApiRequest(cr: ChangeRequest): ChangeRequestApiRequest {
        return ChangeRequestApiRequest(
            ticketId = cr.ticketId,
            userId = cr.userId,
            jenisPerubahan = cr.jenisPerubahan,
            alasan = cr.alasan,
            tujuan = cr.tujuan,
            asetTerdampak = cr.asetTerdampak,
            rencanaImplementasi = cr.rencanaImplementasi,
            usulanJadwal = cr.usulanJadwal,
            rencanaRollback = cr.rencanaRollback,
            assignedTeknisiId = cr.assignedTeknisiId,
            assignedTeknisiName = cr.assignedTeknisiName,
            status = cr.status
        )
    }

    /**
     * Convert DTO to Entity
     */
    private fun apiRequestToChangeRequest(dto: ChangeRequestApiRequest): ChangeRequest {
        return ChangeRequest(
            ticketId = dto.ticketId,
            userId = dto.userId,
            jenisPerubahan = dto.jenisPerubahan,
            alasan = dto.alasan,
            tujuan = dto.tujuan,
            asetTerdampak = dto.asetTerdampak,
            rencanaImplementasi = dto.rencanaImplementasi,
            usulanJadwal = dto.usulanJadwal,
            rencanaRollback = dto.rencanaRollback,
            assignedTeknisiId = dto.assignedTeknisiId,
            assignedTeknisiName = dto.assignedTeknisiName,
            status = dto.status
        )
    }
}