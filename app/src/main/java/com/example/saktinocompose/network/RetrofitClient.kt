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
    private const val BASE_URL = "https://sakti-backend-674826252080.asia-southeast2.run.app/"

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

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // ✅ Service untuk Authentication
    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    // ✅ Service untuk Change Request
    val changeRequestService: ChangeRequestApiService by lazy {
        retrofit.create(ChangeRequestApiService::class.java)
    }

    // ✅ Service untuk Siladan Integration
    val siladanService: SiladanApiService by lazy {
        retrofit.create(SiladanApiService::class.java)
    }
    fun updateAuthToken(token: String?) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }
}