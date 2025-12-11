// File: app/src/main/java/com/example/saktinocompose/viewmodel/CmdbViewModel.kt
package com.example.saktinocompose.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.network.Result
import com.example.saktinocompose.network.dto.CmdbAssetData
import com.example.saktinocompose.repository.CmdbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CmdbViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CmdbRepository()

    private val _assets = MutableStateFlow<List<CmdbAssetData>>(emptyList())
    val assets = _assets.asStateFlow()

    private val _filteredAssets = MutableStateFlow<List<CmdbAssetData>>(emptyList())
    val filteredAssets = _filteredAssets.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadAssets()
    }

    /**
     * Load all assets from API
     */
    fun loadAssets(
        kategori: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d("CmdbViewModel", "🔄 Loading assets...")

            when (val result = repository.fetchAssets(
                kategori = kategori,
                status = status
            )) {
                is Result.Success -> {
                    // ✅ FILTER: Hanya ambil asset yang valid (punya kode_bmd)
                    val validAssets = result.data.filter { it.isValid() }
                    _assets.value = validAssets
                    _filteredAssets.value = validAssets
                    _error.value = null

                    val invalidCount = result.data.size - validAssets.size
                    if (invalidCount > 0) {
                        Log.w("CmdbViewModel", "⚠️ Filtered out $invalidCount invalid assets (null kode_bmd)")
                    }
                    Log.d("CmdbViewModel", "✅ Loaded ${validAssets.size} valid assets")
                }
                is Result.Error -> {
                    val errorMsg = result.message ?: "Failed to load assets"
                    _error.value = errorMsg
                    Log.e("CmdbViewModel", "❌ Error: $errorMsg")
                }
                else -> {
                    _error.value = "Unknown error"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Load categories
     */
    fun loadCategories() {
        viewModelScope.launch {
            when (val result = repository.getAllCategories()) {
                is Result.Success -> {
                    _categories.value = result.data
                    Log.d("CmdbViewModel", "✅ Loaded ${result.data.size} categories")
                }
                is Result.Error -> {
                    Log.e("CmdbViewModel", "Failed to load categories: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * Search assets locally
     */
    fun searchAssets(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _filteredAssets.value = _assets.value
        } else {
            val lowerQuery = query.lowercase()
            _filteredAssets.value = _assets.value.filter {
                it.kodeBmd?.lowercase()!!.contains(lowerQuery) ||
                        it.namaAsset.lowercase().contains(lowerQuery) ||
                        it.merkType?.lowercase()?.contains(lowerQuery) == true ||
                        it.kategori?.lowercase()?.contains(lowerQuery) == true
            }
        }

        Log.d("CmdbViewModel", "Search '$query': ${_filteredAssets.value.size} results")
    }

    /**
     * Filter by category
     */
    fun filterByCategory(kategori: String?) {
        if (kategori == null) {
            _filteredAssets.value = _assets.value
        } else {
            _filteredAssets.value = _assets.value.filter { it.kategori == kategori }
        }
    }

    /**
     * Refresh data
     */
    fun refresh() {
        loadAssets()
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get asset by kode BMD
     */
    fun getAssetByKode(kodeBmd: String): CmdbAssetData? {
        return _assets.value.find { it.kodeBmd == kodeBmd }
    }

    /**
     * Clear search
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _filteredAssets.value = _assets.value
    }
}