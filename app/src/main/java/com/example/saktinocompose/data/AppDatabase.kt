package com.example.saktinocompose.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.saktinocompose.data.dao.ApprovalHistoryDao
import com.example.saktinocompose.data.dao.ChangeRequestDao
import com.example.saktinocompose.data.dao.NotificationDao
import com.example.saktinocompose.data.dao.RiskAssessmentDao
import com.example.saktinocompose.data.dao.UserDao
import com.example.saktinocompose.data.entity.ApprovalHistory
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.data.entity.Notification
import com.example.saktinocompose.data.entity.RiskAssessment
import com.example.saktinocompose.data.entity.User



@Database(
    entities = [User::class, ChangeRequest::class, ApprovalHistory::class, RiskAssessment::class, Notification::class],
    version = 18,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun changeRequestDao(): ChangeRequestDao
    abstract fun approvalHistoryDao(): ApprovalHistoryDao
    abstract fun riskAssessmentDao(): RiskAssessmentDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sakti_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}