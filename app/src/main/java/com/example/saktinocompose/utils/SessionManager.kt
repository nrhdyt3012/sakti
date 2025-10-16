package com.example.saktinocompose.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val IS_LOGGED_IN_KEY = stringPreferencesKey("is_logged_in")
    }

    // Simpan session
    suspend fun saveSession(email: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
            preferences[ROLE_KEY] = role
            preferences[IS_LOGGED_IN_KEY] = "true"
        }
    }

    // Ambil email
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    // Ambil role
    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ROLE_KEY]
    }

    // Cek status login
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] == "true"
    }

    // Hapus session (logout)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Ambil semua data session sekaligus
    data class UserSession(
        val email: String?,
        val role: String?,
        val isLoggedIn: Boolean
    )

    val userSession: Flow<UserSession> = context.dataStore.data.map { preferences ->
        UserSession(
            email = preferences[EMAIL_KEY],
            role = preferences[ROLE_KEY],
            isLoggedIn = preferences[IS_LOGGED_IN_KEY] == "true"
        )
    }
}