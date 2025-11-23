// 1. API Configuration Class
// File: app/src/main/java/com/example/saktinocompose/network/ApiConfig.kt

package com.example.saktinocompose.network

object ApiConfig {
    // TODO: Ganti dengan URL API yang sebenarnya ketika sudah siap
    const val BASE_URL_AUTH = "https://api-auth.example.com/api/v1/" // API untuk Login/Auth
    const val BASE_URL_TEKNISI = "https://api-teknisi.example.com/api/v1/" // API untuk Teknisi
    const val BASE_URL_SYNC = "https://api-sync.example.com/api/v1/" // API untuk Sinkronisasi

    const val CONNECT_TIMEOUT = 30L // dalam detik
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Flag untuk mode offline/online
    var IS_OFFLINE_MODE = true // Set true untuk development, false untuk production
}