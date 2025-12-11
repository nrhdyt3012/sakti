package com.example.saktinocompose.network

import android.content.Context
import android.util.Log
import com.example.saktinocompose.network.api.*
import com.example.saktinocompose.utils.SessionManager
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.saktinocompose.network.api.NotificationApiService


object RetrofitClient {
    private const val BASE_URL = "https://sakti-backend-674826252080.asia-southeast2.run.app/"

    // ✅ CRITICAL: Volatile untuk thread-safety
    @Volatile
    var authToken: String? = null
        private set

    private var applicationContext: Context? = null
    private var sessionManager: SessionManager? = null

    /**
     * ✅ Initialize dengan context dan load token dari SessionManager
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        sessionManager = SessionManager(applicationContext!!)

        // ✅ Load token dari SessionManager saat init
        loadTokenFromSession()

        Log.d("RetrofitClient", "✅ RetrofitClient initialized")
    }

    /**
     * ✅ BARU: Load token dari SessionManager secara async
     */
    private fun loadTokenFromSession() {
        applicationContext?.let { context ->
            GlobalScope.launch {
                try {
                    val sm = SessionManager(context)
                    val token = sm.authToken.first()

                    if (token != null && authToken != token) {
                        authToken = token
                        Log.d("RetrofitClient", "🔄 Token restored from session: ${token.take(20)}...")
                        verifyToken(token)
                    }
                } catch (e: Exception) {
                    Log.e("RetrofitClient", "❌ Failed to load token from session", e)
                }
            }
        }
    }

    /**
     * ✅ BARU: Get token dengan fallback ke SessionManager (synchronous)
     */
    private fun getTokenWithFallback(): String? {
        // 1. Try memory cache first
        var currentToken = authToken

        // 2. If null, try to load from SessionManager
        if (currentToken == null && applicationContext != null) {
            try {
                currentToken = runBlocking {
                    val sm = SessionManager(applicationContext!!)
                    sm.authToken.first()
                }

                // Update memory cache if found
                if (currentToken != null) {
                    authToken = currentToken
                    Log.d("RetrofitClient", "🔄 Token loaded from SessionManager: ${currentToken.take(20)}...")
                }
            } catch (e: Exception) {
                Log.e("RetrofitClient", "❌ Failed to get token from SessionManager", e)
            }
        }

        return currentToken
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * ✅ FIXED: Auth Interceptor dengan fallback ke SessionManager
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val currentToken = getTokenWithFallback()

        val newRequest = if (currentToken != null) {
            // ✅ PERBAIKAN: Cek apakah token sudah punya "Bearer"
            val authHeader = if (currentToken.startsWith("Bearer ", ignoreCase = true)) {
                currentToken  // Sudah ada Bearer
            } else {
                "Bearer $currentToken"  // Tambah Bearer
            }

            Log.d("RetrofitClient", "🔑 Adding auth header: ${authHeader.take(30)}...")

            originalRequest.newBuilder()
                .addHeader("Authorization", authHeader)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()
        } else {
            Log.w("RetrofitClient", "⚠️ No token available for request")
            originalRequest
        }

        chain.proceed(newRequest)
    }

    /**
     * ✅ Build OkHttpClient dengan interceptors
     */
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                applicationContext?.let {
                    addInterceptor(InternetCheckInterceptor(it))
                }
            }
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor) // ✅ Auth interceptor terakhir
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    /**
     * ✅ CRITICAL: Lazy initialization agar interceptor selalu fresh
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ✅ API Services
    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val changeRequestService: ChangeRequestApiService by lazy {
        retrofit.create(ChangeRequestApiService::class.java)
    }

    val siladanService: SiladanApiService by lazy {
        retrofit.create(SiladanApiService::class.java)
    }
    val notificationService: NotificationApiService by lazy {
        retrofit.create(NotificationApiService::class.java)
    }
    val cmdbService: CmdbApiService by lazy {
        retrofit.create(CmdbApiService::class.java)
    }
    val emergencyService: EmergencyApiService by lazy {
        retrofit.create(EmergencyApiService::class.java)
    }

    /**
     * ✅ FIXED: Synchronized token update dengan SessionManager
     */
    /**
     * ✅ BARU: Sanitize token untuk menghindari double Bearer
     */
    private fun sanitizeToken(token: String): String {
        return token
            .replace("Bearer ", "", ignoreCase = true)
            .replace("bearer ", "", ignoreCase = true)
            .trim()
    }

    /**
     * ✅ UPDATE: updateAuthToken dengan sanitization
     */
    @Synchronized
    fun updateAuthToken(token: String?) {
        // ✅ Sanitize token sebelum disimpan
        val cleanToken = token?.let { sanitizeToken(it) }
        authToken = cleanToken

        Log.d("RetrofitClient", "✅ Token updated (clean): ${cleanToken?.take(20)}...")

        cleanToken?.let {
            verifyToken(it)

            applicationContext?.let { context ->
                GlobalScope.launch {
                    try {
                        val sm = SessionManager(context)
                        sm.saveAuthToken(it)
                        Log.d("RetrofitClient", "✅ Token saved to SessionManager")
                    } catch (e: Exception) {
                        Log.e("RetrofitClient", "❌ Failed to save token", e)
                    }
                }
            }
        }
    }

    /**
     * ✅ FIXED: Clear token dari memory dan SessionManager
     */
    @Synchronized
    fun clearAuthToken() {
        authToken = null
        Log.d("RetrofitClient", "🗑️ Token cleared from memory")

        // ✅ PERBAIKAN: Clear dari SessionManager juga
        applicationContext?.let { context ->
            GlobalScope.launch {
                try {
                    val sm = SessionManager(context)
                    sm.clearSession()
                    Log.d("RetrofitClient", "🗑️ Token cleared from SessionManager")
                } catch (e: Exception) {
                    Log.e("RetrofitClient", "❌ Failed to clear SessionManager", e)
                }
            }
        }
    }

    /**
     * ✅ BONUS: Token verification helper
     */
    private fun verifyToken(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("RetrofitClient", "❌ Invalid token format")
                return
            }

            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val json = org.json.JSONObject(payload)

            val exp = json.getLong("exp")
            val now = System.currentTimeMillis() / 1000
            val timeLeft = exp - now

            Log.d("RetrofitClient", """
                🔐 Token Info:
                - User ID: ${json.optString("user_id")}
                - Role: ${json.optString("role")}
                - Expires: ${java.util.Date(exp * 1000)}
                - Time left: ${timeLeft / 60} minutes
            """.trimIndent())

            if (timeLeft < 300) {
                Log.w("RetrofitClient", "⚠️ Token expires in less than 5 minutes!")
            }

            if (timeLeft < 0) {
                Log.e("RetrofitClient", "❌ Token already expired!")
                clearAuthToken()
            }
        } catch (e: Exception) {
            Log.e("RetrofitClient", "❌ Token verification failed", e)
        }
    }

    /**
     * ✅ BARU: Verify token dengan server sebelum digunakan
     */
    suspend fun verifyTokenWithServer(): Boolean {
        val token = getCurrentToken() ?: return false

        return try {
            withContext(Dispatchers.IO) {
                val response = authService.getProfile()
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("RetrofitClient", "❌ Token verification failed", e)
            false
        }
    }

    /**
     * ✅ BARU: Check if token is valid (not expired)
     */
    fun isTokenValid(): Boolean {
        val token = getTokenWithFallback() ?: return false

        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false

            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val json = org.json.JSONObject(payload)

            val exp = json.getLong("exp")
            val now = System.currentTimeMillis() / 1000

            exp > now
        } catch (e: Exception) {
            Log.e("RetrofitClient", "❌ Token validation failed", e)
            false
        }
    }

    /**
     * ✅ BARU: Force reload token dari SessionManager
     */
    suspend fun reloadTokenFromSession() {
        applicationContext?.let { context ->
            try {
                val sm = SessionManager(context)
                val token = sm.authToken.first()

                if (token != null) {
                    authToken = token
                    Log.d("RetrofitClient", "🔄 Token reloaded: ${token.take(20)}...")
                    verifyToken(token)
                } else {
                    Log.w("RetrofitClient", "⚠️ No token found in SessionManager")
                }
            } catch (e: Exception) {
                Log.e("RetrofitClient", "❌ Failed to reload token", e)
            }
        }
    }

    /**
     * ✅ BARU: Get current token (dengan fallback)
     */
    fun getCurrentToken(): String? {
        return getTokenWithFallback()
    }
}