// Updated SessionManager untuk support JWT Token
// File: app/src/main/java/com/example/saktinocompose/utils/SessionManager.kt

package com.example.saktinocompose.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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

        // ===== BARU: Token Keys =====
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val TOKEN_EXPIRY_KEY = longPreferencesKey("token_expiry")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

        // ===== BARU: Sync Keys =====
        private val LAST_SYNC_KEY = longPreferencesKey("last_sync_timestamp")
        private val SYNC_ENABLED_KEY = booleanPreferencesKey("sync_enabled")
    }

    // Simpan session dengan token
    suspend fun saveSession(
        userId: Int,
        email: String,
        name: String,
        role: String,
        authToken: String? = null,
        refreshToken: String? = null,
        tokenExpiry: Long? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[EMAIL_KEY] = email
            preferences[NAME_KEY] = name
            preferences[ROLE_KEY] = role
            preferences[IS_LOGGED_IN_KEY] = "true"

            // Save tokens jika ada
            authToken?.let { preferences[AUTH_TOKEN_KEY] = it }
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            tokenExpiry?.let { preferences[TOKEN_EXPIRY_KEY] = it }
        }
    }

    // ===== BARU: Token Management =====

    // Save auth token
    suspend fun saveAuthToken(token: String, expiry: Long? = null) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            expiry?.let { preferences[TOKEN_EXPIRY_KEY] = it }
        }
    }

    // Get auth token
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }

    // Save refresh token
    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = token
        }
    }

    // Get refresh token
    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    // Check if token is expired
    suspend fun isTokenExpired(): Boolean {
        var isExpired = true
        context.dataStore.data.collect { preferences ->
            val expiry = preferences[TOKEN_EXPIRY_KEY]
            isExpired = if (expiry != null) {
                System.currentTimeMillis() > expiry
            } else {
                true // Jika tidak ada expiry, anggap expired
            }
        }
        return isExpired
    }

    // ===== BARU: Sync Management =====

    // Save last sync timestamp
    suspend fun saveLastSyncTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_KEY] = timestamp
        }
    }

    // Get last sync timestamp
    val lastSyncTimestamp: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_KEY]
    }

    // Enable/disable sync
    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_ENABLED_KEY] = enabled
        }
    }

    // Check if sync enabled
    val isSyncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SYNC_ENABLED_KEY] ?: true // Default true
    }

    // ===== Existing Methods (tetap ada) =====

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

    // Ambil semua data session sekaligus (UPDATED)
    data class UserSession(
        val userId: Int?,
        val email: String?,
        val name: String?,
        val role: String?,
        val isLoggedIn: Boolean,
        val authToken: String? = null,
        val refreshToken: String? = null,
        val tokenExpiry: Long? = null,
        val lastSync: Long? = null
    )

    val userSession: Flow<UserSession> = context.dataStore.data.map { preferences ->
        UserSession(
            userId = preferences[USER_ID_KEY],
            email = preferences[EMAIL_KEY],
            name = preferences[NAME_KEY],
            role = preferences[ROLE_KEY],
            isLoggedIn = preferences[IS_LOGGED_IN_KEY] == "true",
            authToken = preferences[AUTH_TOKEN_KEY],
            refreshToken = preferences[REFRESH_TOKEN_KEY],
            tokenExpiry = preferences[TOKEN_EXPIRY_KEY],
            lastSync = preferences[LAST_SYNC_KEY]
        )
    }
}

// ===== EXTENSION FUNCTIONS =====

// Helper untuk mendapatkan token dengan mudah
suspend fun SessionManager.getAuthTokenOrNull(): String? {
    var token: String? = null
    authToken.collect { token = it }
    return token
}

// Helper untuk check apakah user sudah login dan token valid
suspend fun SessionManager.isAuthenticated(): Boolean {
    var isAuth = false
    userSession.collect { session ->
        isAuth = session.isLoggedIn && !session.authToken.isNullOrEmpty()
    }
    return isAuth && !isTokenExpired()
}