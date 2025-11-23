// Background Sync Worker untuk sinkronisasi otomatis
// File: app/src/main/java/com/example/saktinocompose/worker/SyncWorker.kt

package com.example.saktinocompose.worker

import android.content.Context
import androidx.work.*
import com.example.saktinocompose.data.AppDatabase
import com.example.saktinocompose.network.ApiConfig
import com.example.saktinocompose.repository.ChangeRequestRepository
import com.example.saktinocompose.utils.SessionManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Worker untuk background sync data dengan API
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val sessionManager = SessionManager(context)
    private val database = AppDatabase.getDatabase(context)
    private val changeRequestRepository = ChangeRequestRepository(database.changeRequestDao())

    override suspend fun doWork(): Result {
        return try {
            // Check jika mode offline atau sync disabled
            if (ApiConfig.IS_OFFLINE_MODE) {
                return Result.success()
            }

            val isSyncEnabled = sessionManager.isSyncEnabled.first()
            if (!isSyncEnabled) {
                return Result.success()
            }

            // Check token
            val token = sessionManager.authToken.first()
            if (token.isNullOrEmpty()) {
                return Result.failure()
            }

            // Lakukan sync
            val syncResult = changeRequestRepository.syncFromApi()

            when (syncResult) {
                is com.example.saktinocompose.network.Result.Success -> {
                    // Update last sync timestamp
                    sessionManager.saveLastSyncTimestamp(System.currentTimeMillis())
                    Result.success()
                }
                is com.example.saktinocompose.network.Result.Error -> {
                    // Retry dengan exponential backoff
                    Result.retry()
                }
                else -> Result.failure()
            }
        } catch (e: Exception) {
            // Log error
            println("SyncWorker error: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "SyncWorkManager"
        const val SYNC_INTERVAL_HOURS = 2L

        /**
         * Schedule periodic sync
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Hanya saat ada internet
                .setRequiresBatteryNotLow(true) // Hanya saat baterai tidak low
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                SYNC_INTERVAL_HOURS,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        /**
         * One-time sync
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
        }

        /**
         * Cancel sync
         */
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}