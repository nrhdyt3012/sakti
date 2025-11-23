// 5. Updated ProfileViewModel dengan Repository
// File: app/src/main/java/com/example/saktinocompose/viewmodel/ProfileViewModel.kt

package com.example.saktinocompose.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.network.RetrofitClient
import com.example.saktinocompose.repository.AuthRepository
import com.example.saktinocompose.utils.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()

    // ===== BARU: Repository untuk handle API =====
    private val authRepository = AuthRepository(userDao)

    /**
     * Logout dengan support API
     */
    suspend fun logout(context: Context) {
        // Logout via repository (akan call API jika online)
        authRepository.logout()

        // Clear session manager
        val sessionManager = SessionManager(context)
        sessionManager.clearSession()

        // Clear retrofit token
        RetrofitClient.clearAuthToken()
    }
}