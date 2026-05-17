package com.brzhang.gemma_ai.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.brzhang.gemma_ai.data.entity.ChatHistory
import com.brzhang.gemma_ai.data.entity.HealthRecord
import com.brzhang.gemma_ai.data.entity.UserProfile

@Database(
    entities = [UserProfile::class, HealthRecord::class, ChatHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "village_doc.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
