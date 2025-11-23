// ===== Sync Manager untuk koordinasi sync =====
// File: app/src/main/java/com/example/saktinocompose/utils/SyncManager.kt

package com.example.saktinocompose.utils

import android.content.Context
import com.example.saktinocompose.network.ApiConfig
import com.example.saktinocompose.worker.SyncWorker
import kotlinx.coroutines.flow.first

/**
 * Manager untuk mengelola sinkronisasi data
 */
class SyncManager(private val context: Context) {

    private val sessionManager = SessionManager(context)
    private val networkMonitor = NetworkMonitor(context)

    /**
     * Initialize sync (panggil saat app start)
     */
    suspend fun initializeSync() {
        // Skip jika offline mode
        if (ApiConfig.IS_OFFLINE_MODE) return

        // Check jika user logged in
        val isLoggedIn = sessionManager.isLoggedIn.first()
        if (!isLoggedIn) return

        // Check jika sync enabled
        val isSyncEnabled = sessionManager.isSyncEnabled.first()
        if (!isSyncEnabled) return

        // Schedule periodic sync
        SyncWorker.schedulePeriodicSync(context)
    }

    /**
     * Force sync now
     */
    fun syncNow() {
        if (!ApiConfig.IS_OFFLINE_MODE && networkMonitor.isNetworkAvailable()) {
            SyncWorker.syncNow(context)
        }
    }

    /**
     * Stop sync
     */
    fun stopSync() {
        SyncWorker.cancelSync(context)
    }

    /**
     * Toggle sync on/off
     */
    suspend fun toggleSync(enabled: Boolean) {
        sessionManager.setSyncEnabled(enabled)
        if (enabled) {
            initializeSync()
        } else {
            stopSync()
        }
    }

    /**
     * Get last sync info
     */
    suspend fun getLastSyncTimestamp(): Long? {
        return sessionManager.lastSyncTimestamp.first()
    }
}

// ===== CARA PENGGUNAAN =====

/*
1. Di Application class atau MainActivity onCreate:

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize sync saat app start
        lifecycleScope.launch {
            val syncManager = SyncManager(this@MyApplication)
            syncManager.initializeSync()
        }
    }
}

2. Di Settings/Profile untuk toggle sync:

val syncManager = SyncManager(context)

// Enable sync
lifecycleScope.launch {
    syncManager.toggleSync(true)
}

// Disable sync
lifecycleScope.launch {
    syncManager.toggleSync(false)
}

// Manual sync
syncManager.syncNow()

3. Monitor network status:

val networkMonitor = NetworkMonitor(context)

LaunchedEffect(Unit) {
    networkMonitor.observeNetworkStatus().collect { status ->
        when (status) {
            NetworkStatus.Available -> {
                // Show online indicator
                // Trigger sync if needed
            }
            NetworkStatus.Unavailable -> {
                // Show offline indicator
                // Switch to offline mode
            }
        }
    }
}
*/