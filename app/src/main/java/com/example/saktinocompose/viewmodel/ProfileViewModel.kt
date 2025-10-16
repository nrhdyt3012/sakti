package com.example.saktinocompose.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.saktinocompose.utils.SessionManager

class ProfileViewModel : ViewModel() {

    suspend fun logout(context: Context) {
        val sessionManager = SessionManager(context)
        sessionManager.clearSession()
    }
}