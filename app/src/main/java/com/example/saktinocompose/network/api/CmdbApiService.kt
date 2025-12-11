// File: app/src/main/java/com/example/saktinocompose/network/api/CmdbApiService.kt
package com.example.saktinocompose.network.api

import com.example.saktinocompose.network.dto.CmdbAssetsResponse
import retrofit2.Response
import retrofit2.http.*

interface CmdbApiService {

    /**
     * Get all CMDB assets
     * GET /cmdb/assets
     */
    @GET("cmdb/assets")
    suspend fun getAssets(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("kategori") kategori: String? = null,
        @Query("status") status: String? = null
    ): Response<CmdbAssetsResponse>

    /**
     * Search assets by query
     * GET /cmdb/assets/search
     */
    @GET("cmdb/assets/search")
    suspend fun searchAssets(
        @Query("q") query: String,
        @Query("limit") limit: Int? = null
    ): Response<CmdbAssetsResponse>
}