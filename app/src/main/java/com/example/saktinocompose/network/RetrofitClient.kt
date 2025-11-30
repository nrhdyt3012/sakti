package com.example.saktinocompose.network

import android.content.Context
import com.example.saktinocompose.network.api.*
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    var authToken: String? = null
    private var applicationContext: Context? = null

    // ✅ Initialize dengan context
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

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

    // ✅ Tambahkan Internet Check Interceptor
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                // ✅ Add internet check interceptor first
                applicationContext?.let {
                    addInterceptor(InternetCheckInterceptor(it))
                }
            }
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofitAuth = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL_AUTH)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val retrofitTeknisi = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL_TEKNISI)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val retrofitSync = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL_SYNC)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authService: AuthApiService by lazy {
        retrofitAuth.create(AuthApiService::class.java)
    }

    val teknisiService: TeknisiApiService by lazy {
        retrofitTeknisi.create(TeknisiApiService::class.java)
    }

    val syncService: SyncApiService by lazy {
        retrofitSync.create(SyncApiService::class.java)
    }

    fun updateAuthToken(token: String?) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }
}