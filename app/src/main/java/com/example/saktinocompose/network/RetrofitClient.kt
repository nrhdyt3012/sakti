// 4. Retrofit Client Setup
package com.example.saktinocompose.network

import com.example.saktinocompose.network.api.*
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Auth Token Storage (nanti akan diambil dari SessionManager)
    var authToken: String? = null

    // Logging Interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Auth Interceptor untuk menambahkan token ke header
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val newRequest = request.newBuilder()
            .apply {
                authToken?.let {
                    addHeader("Authorization", "Bearer $it")
                }
                addHeader("Accept", "application/json")
                addHeader("Content-Type", "application/json")
            }
            .build()
        chain.proceed(newRequest)
    }

    // OkHttp Client
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    // Gson Converter
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Retrofit instance untuk Auth API
    private val retrofitAuth = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL_AUTH)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Retrofit instance untuk Teknisi API
    private val retrofitTeknisi = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL_TEKNISI)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Retrofit instance untuk Sync API
    private val retrofitSync = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL_SYNC)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Service instances
    val authService: AuthApiService by lazy {
        retrofitAuth.create(AuthApiService::class.java)
    }

    val teknisiService: TeknisiApiService by lazy {
        retrofitTeknisi.create(TeknisiApiService::class.java)
    }

    val syncService: SyncApiService by lazy {
        retrofitSync.create(SyncApiService::class.java)
    }

    // Function untuk set auth token
    fun updateAuthToken(token: String?) {
        authToken = token
    }

    // Function untuk clear auth token
    fun clearAuthToken() {
        authToken = null
    }
}