package com.example.saktinocompose.network

import android.content.Context
import android.util.Log
import com.example.saktinocompose.network.api.*
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://sakti-backend-674826252080.asia-southeast2.run.app/"

    // ✅ CRITICAL: Volatile untuk thread-safety
    @Volatile
    var authToken: String? = null
        private set

    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ✅ FIXED: Auth Interceptor yang lebih robust
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // ✅ CRITICAL: Ambil token saat request, bukan saat build
        val currentToken = authToken

        val newRequest = if (currentToken != null) {
            Log.d("RetrofitClient", "🔑 Adding token to request: ${currentToken.take(20)}...")

            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $currentToken")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()
        } else {
            Log.w("RetrofitClient", "⚠️ No token available for request")
            originalRequest
        }

        chain.proceed(newRequest)
    }

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

    // ✅ CRITICAL: Lazy initialization agar interceptor selalu fresh
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val changeRequestService: ChangeRequestApiService by lazy {
        retrofit.create(ChangeRequestApiService::class.java)
    }

    val siladanService: SiladanApiService by lazy {
        retrofit.create(SiladanApiService::class.java)
    }

    // ✅ FIXED: Synchronized token update
    @Synchronized
    fun updateAuthToken(token: String?) {
        authToken = token
        Log.d("RetrofitClient", "✅ Token updated: ${token?.take(20)}...")

        // ✅ Decode dan verify token
        token?.let { verifyToken(it) }
    }

    @Synchronized
    fun clearAuthToken() {
        authToken = null
        Log.d("RetrofitClient", "🗑️ Token cleared")
    }

    // ✅ BONUS: Token verification helper
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
        } catch (e: Exception) {
            Log.e("RetrofitClient", "❌ Token verification failed", e)
        }
    }
}