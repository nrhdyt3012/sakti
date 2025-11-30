package com.example.saktinocompose

import android.app.Application
import com.example.saktinocompose.network.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // ✅ Initialize RetrofitClient with context
        RetrofitClient.initialize(this)
    }
}