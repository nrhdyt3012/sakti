package com.example.saktinocompose.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.saktinocompose.data.dao.ApprovalHistoryDao
import com.example.saktinocompose.data.dao.ChangeRequestDao
import com.example.saktinocompose.data.dao.UserDao
import com.example.saktinocompose.data.entity.ApprovalHistory
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.data.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [User::class, ChangeRequest::class, ApprovalHistory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun changeRequestDao(): ChangeRequestDao
    abstract fun approvalHistoryDao(): ApprovalHistoryDao

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
                    email = "enduser@example.com",
                    name = "Andi",
                    passwordHash = hashPassword("password456"),
                    role = "END_USER"
                )
            )
        }
    }
}