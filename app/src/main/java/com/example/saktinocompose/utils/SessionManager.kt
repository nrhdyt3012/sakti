package com.example.saktinocompose.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val NAME_KEY = stringPreferencesKey("user_name")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val IS_LOGGED_IN_KEY = stringPreferencesKey("is_logged_in")
    }

    // Simpan session
    suspend fun saveSession(userId: Int, email: String, name: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[EMAIL_KEY] = email
            preferences[NAME_KEY] = name
            preferences[ROLE_KEY] = role
            preferences[IS_LOGGED_IN_KEY] = "true"
        }
    }

    // Ambil user ID
    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    // Ambil email
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    // Ambil nama
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[NAME_KEY]
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
        val userId: Int?,
        val email: String?,
        val name: String?,
        val role: String?,
        val isLoggedIn: Boolean
    )

    val userSession: Flow<UserSession> = context.dataStore.data.map { preferences ->
        UserSession(
            userId = preferences[USER_ID_KEY],
            email = preferences[EMAIL_KEY],
            name = preferences[NAME_KEY],
            role = preferences[ROLE_KEY],
            isLoggedIn = preferences[IS_LOGGED_IN_KEY] == "true"
        )
    }
}