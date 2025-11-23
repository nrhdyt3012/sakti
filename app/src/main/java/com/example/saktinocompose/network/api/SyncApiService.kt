// 3. Retrofit API Service untuk Sync Operations
// File: app/src/main/java/com/example/saktinocompose/network/api/SyncApiService.kt

package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface SyncApiService {

    // Sync change requests dari server ke local
    @GET("sync/change-requests")
    suspend fun syncChangeRequests(
        @Header("Authorization") token: String,
        @Query("last_sync") lastSync: Long? = null // Timestamp terakhir sync
    ): Response<ChangeRequestListResponse>

    // Push local changes ke server
    @POST("sync/change-requests/push")
    suspend fun pushChangeRequests(
        @Header("Authorization") token: String,
        @Body requests: List<ChangeRequestApiRequest>
    ): Response<ApiResponse<Any>>

    // Sync user data
    @GET("sync/users")
    suspend fun syncUsers(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<UserData>>>

    // Check sync status
    @GET("sync/status")
    suspend fun getSyncStatus(
        @Header("Authorization") token: String
    ): Response<ApiResponse<SyncStatusData>>
}

data class SyncStatusData(
    val last_sync: Long,
    val pending_changes: Int,
    val sync_required: Boolean
)