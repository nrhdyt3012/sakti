package com.example.saktinocompose.repository

import com.example.saktinocompose.data.dao.ChangeRequestDao
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChangeRequestRepository(
    private val changeRequestDao: ChangeRequestDao
) {

    /**
     * ✅ Fetch change requests from API
     */
    suspend fun fetchFromApi(status: String? = null, deskripsi: String? = null): Result<List<ChangeRequest>> {
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

                    // Convert and cache to local database
                    val localRequests = apiRequests.map { apiDataToChangeRequest(it) }
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

    /**
     * ✅ Submit inspection ke API
     */
    suspend fun submitInspection(
        crId: String,
        jenisPerubahan: String,
        alasan: String,
        tujuan: String,
        ciId: String,
        asetTerdampakId: String,
        rencanaImplementasi: String,
        usulanJadwal: String,
        rencanaRollback: String,
        estimasiBiaya: Double,
        estimasiWaktu: Double,
        skorDampak: Int,
        skorKemungkinan: Int,
        skorExposure: Int
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val request = InspectionRequest(
                    jenisPerubahan = jenisPerubahan,
                    alasan = alasan,
                    tujuan = tujuan,
                    ciId = ciId,
                    asetTerdampakId = asetTerdampakId,
                    rencanaImplementasi = rencanaImplementasi,
                    usulanJadwal = convertToIso8601(usulanJadwal),
                    rencanaRollback = rencanaRollback,
                    estimasiBiaya = estimasiBiaya,
                    estimasiWaktu = estimasiWaktu,
                    skorDampak = skorDampak,
                    skorKemungkinan = skorKemungkinan,
                    skorExposure = skorExposure
                )

                val response = RetrofitClient.changeRequestService.submitInspection(
                    "Bearer $token",
                    crId,
                    request
                )

                if (response.isSuccessful && response.body()?.status == "success") {
                    Result.Success(true)
                } else {
                    Result.Error(
                        Exception("Inspection failed"),
                        response.body()?.message ?: "Failed to submit inspection"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }


    /**
     * ✅ Schedule implementation ke API
     */
    suspend fun scheduleImplementation(
        crId: String,
        tanggalImplementasi: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val request = ScheduleRequest(
                    tanggalImplementasi = convertToIso8601(tanggalImplementasi)
                )

                val response = RetrofitClient.changeRequestService.scheduleImplementation(
                    "Bearer $token",
                    crId,
                    request
                )

                if (response.isSuccessful && response.body()?.status == "success") {
                    Result.Success(true)
                } else {
                    Result.Error(
                        Exception("Scheduling failed"),
                        response.body()?.message ?: "Failed to schedule implementation"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }
    /**
     * ✅ Complete implementation ke API
     */
    suspend fun completeImplementation(
        crId: String,
        dampakSetelahMitigasi: Int,
        kemungkinanSetelahMitigasi: Int,
        exposureSetelahMitigasi: Int,
        keterangan: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val request = ImplementationRequest(
                    dampakSetelahMitigasi = dampakSetelahMitigasi,
                    kemungkinanSetelahMitigasi = kemungkinanSetelahMitigasi,
                    exposureSetelahMitigasi = exposureSetelahMitigasi,
                    keterangan = keterangan,
                    status = "COMPLETED"
                )

                val response = RetrofitClient.changeRequestService.completeImplementation(
                    "Bearer $token",
                    crId,
                    request
                )

                if (response.isSuccessful && response.body()?.status == "success") {
                    Result.Success(true)
                } else {
                    Result.Error(
                        Exception("Implementation failed"),
                        response.body()?.message ?: "Failed to complete implementation"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }


    /**
     * ✅ Create Siladan Change Request
     */
    suspend fun createSiladanChangeRequest(
        tiketId: String,
        katalogPermintaan: String,
        assetId: String,
        judul: String,
        deskripsi: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken
                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val request = SiladanChangeRequestRequest(
                    tiketId = tiketId,
                    katalogPermintaan = katalogPermintaan,
                    assetId = assetId,
                    judul = judul,
                    deskripsi = deskripsi
                )

                val response = RetrofitClient.siladanService.createSiladanChangeRequest(
                    "Bearer $token",
                    request
                )

                if (response.isSuccessful && response.body()?.status == "success") {
                    Result.Success(true)
                } else {
                    Result.Error(
                        Exception("Siladan request failed"),
                        response.body()?.message ?: "Failed to create Siladan request"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    // ===== Local Database Operations =====

    fun getAllChangeRequests(): Flow<List<ChangeRequest>> {
        return changeRequestDao.getAllChangeRequests()
    }

    fun getChangeRequestsByUser(userId: Int): Flow<List<ChangeRequest>> {
        return changeRequestDao.getChangeRequestsByUser(userId)
    }

    suspend fun getChangeRequestById(id: Int): ChangeRequest? {
        return changeRequestDao.getChangeRequestById(id)
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

    suspend fun submitChangeRequest(changeRequest: ChangeRequest): Result<ChangeRequest> {
        return withContext(Dispatchers.IO) {
            try {
                val localId = changeRequestDao.insertChangeRequest(changeRequest).toInt()
                Result.Success(changeRequest.copy(id = localId))
            } catch (e: Exception) {
                Result.Error(e, "Submit failed: ${e.message}")
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
                changeRequestDao.updateChangeRequest(updated)
                Result.Success(updated)
            } catch (e: Exception) {
                Result.Error(e, "Status update failed: ${e.message}")
            }
        }
    }

    // ===== Helper Functions =====

    private fun convertToIso8601(dateString: String): String {
        return try {
            // Input format: "yyyy-MM-dd"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)

            // Output format: ISO 8601
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    private fun apiDataToChangeRequest(apiData: ChangeRequestApiData): ChangeRequest {
        return ChangeRequest(
            id = 0, // Local ID
            ticketId = apiData.ticketId ?: apiData.crId,
            userId = 0, // Will be set from session
            idPerubahan = apiData.crId,
            jenisPerubahan = apiData.type,
            alasan = apiData.title,
            tujuan = apiData.description ?: "",
            idAset = apiData.assetId ?: "",
            asetTerdampak = "",
            relasiConfigurationItem = "",
            rencanaImplementasi = "",
            usulanJadwal = apiData.scheduleImplementation ?: "",
            rencanaRollback = apiData.rollbackPlan ?: "",
            assignedTeknisiId = null,
            assignedTeknisiName = apiData.picImplementation,
            photoPath = null,
            estimasiBiaya = null,
            estimasiWaktu = null,
            scheduledDate = apiData.scheduleStart,
            scheduledTimestamp = null,
            skorEksposur = null,
            dampakSetelahMitigasi = apiData.postImpact,
            kemungkinanSetelahMitigasi = apiData.postLikelihood,
            exposur = null,
            skorResidual = apiData.postResidualScore,
            levelRisikoResidual = apiData.postRiskLevel,
            keteranganHasilImplementasi = apiData.implementationResult,
            revisionNotes = null,
            revisionCount = 0,
            status = mapApiStatusToLocalStatus(apiData.status),
            createdAt = parseIso8601ToTimestamp(apiData.createdAt),
            updatedAt = parseIso8601ToTimestamp(apiData.updatedAt)
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

    private fun parseIso8601ToTimestamp(iso8601: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.parse(iso8601)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}