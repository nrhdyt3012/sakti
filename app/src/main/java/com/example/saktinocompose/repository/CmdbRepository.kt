// File: app/src/main/java/com/example/saktinocompose/repository/CmdbRepository.kt
package com.example.saktinocompose.repository

import android.util.Log
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.network.dto.CmdbAssetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CmdbRepository {

    /**
     * Fetch all assets from CMDB
     */
    suspend fun fetchAssets(
        page: Int? = null,
        limit: Int? = null,
        kategori: String? = null,
        status: String? = null
    ): Result<List<CmdbAssetData>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken

                Log.d("CmdbRepository", """
                    🔄 Fetching CMDB assets:
                    - Token: ${token?.take(20)}...
                    - Page: $page, Limit: $limit
                    - Kategori: $kategori, Status: $status
                """.trimIndent())

                if (token == null) {
                    Log.e("CmdbRepository", "No token available")
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val response = RetrofitClient.cmdbService.getAssets(
                    page = page,
                    limit = limit,
                    kategori = kategori,
                    status = status
                )

                Log.d("CmdbRepository", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val assets = response.body()?.data ?: emptyList()
                    Log.d("CmdbRepository", "✅ Fetched ${assets.size} assets")
                    Result.Success(assets)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = response.body()?.message ?: errorBody ?: "Failed to fetch assets"
                    Log.e("CmdbRepository", "API Error: $errorMessage")

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
                Log.e("CmdbRepository", "Exception during fetch", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    /**
     * Search assets by query
     */
    suspend fun searchAssets(
        query: String,
        limit: Int? = null
    ): Result<List<CmdbAssetData>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = RetrofitClient.authToken

                if (token == null) {
                    return@withContext Result.Error(
                        Exception("No token"),
                        "Authentication required"
                    )
                }

                val response = RetrofitClient.cmdbService.searchAssets(
                    query = query,
                    limit = limit
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val assets = response.body()?.data ?: emptyList()
                    Log.d("CmdbRepository", "✅ Found ${assets.size} assets for query: $query")
                    Result.Success(assets)
                } else {
                    val errorMessage = response.body()?.message ?: "Search failed"
                    Result.Error(Exception("Search failed"), errorMessage)
                }
            } catch (e: Exception) {
                Log.e("CmdbRepository", "Search error", e)
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }

    /**
     * Get assets by category
     */
    suspend fun fetchAssetsByCategory(kategori: String): Result<List<CmdbAssetData>> {
        return fetchAssets(kategori = kategori)
    }

    /**
     * Get all categories (unique from assets)
     */
    suspend fun getAllCategories(): Result<List<String>> {
        return when (val result = fetchAssets()) {
            is Result.Success -> {
                val categories = result.data
                    .mapNotNull { it.kategori }
                    .distinct()
                    .sorted()
                Result.Success(categories)
            }
            is Result.Error -> result
            else -> Result.Error(Exception("Unknown error"), "Failed to fetch categories")
        }
    }
}