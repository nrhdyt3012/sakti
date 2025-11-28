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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest


@Database(
    entities = [User::class, ChangeRequest::class, ApprovalHistory::class, RiskAssessment::class, Notification::class],
    version = 13,  // ✅ UPDATED VERSION untuk field idPerubahan
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
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.userDao())
                    }
                }
            }
        }

        private suspend fun populateDatabase(userDao: UserDao) {
            fun hashPassword(password: String): String {
                val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
                return bytes.joinToString("") { "%02x".format(it) }
            }

            // Insert Teknisi
            userDao.insertUser(
                User(
                    email = "test@example.com",
                    name = "Budi",
                    passwordHash = hashPassword("password123"),
                    role = "TEKNISI"
                )
            )

            userDao.insertUser(
                User(
                    email = "teknisi@example.com",
                    name = "Bahlil",
                    passwordHash = hashPassword("password111"),
                    role = "TEKNISI"
                )
            )

            // Insert 3 End Users
            userDao.insertUser(
                User(
                    email = "enduser1@example.com",
                    name = "Andi",
                    passwordHash = hashPassword("password456"),
                    role = "END_USER"
                )
            )
            userDao.insertUser(
                User(
                    email = "enduser2@example.com",
                    name = "Siti",
                    passwordHash = hashPassword("password789"),
                    role = "END_USER"
                )
            )
            userDao.insertUser(
                User(
                    email = "enduser3@example.com",
                    name = "Rudi",
                    passwordHash = hashPassword("password000"),
                    role = "END_USER"
                )
            )
        }
    }
}