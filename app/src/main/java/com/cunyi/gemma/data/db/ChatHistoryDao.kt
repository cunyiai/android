package com.cunyi.gemma.data.db

import androidx.room.*
import com.cunyi.gemma.data.entity.ChatHistory

@Dao
interface ChatHistoryDao {

    @Insert
    suspend fun insert(chat: ChatHistory): Long

    @Query("SELECT * FROM chat_history WHERE userId = :userId ORDER BY timestamp ASC")
    fun getAllHistory(userId: String = "default"): kotlinx.coroutines.flow.Flow<List<ChatHistory>>

    @Query("SELECT * FROM chat_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(userId: String = "default", limit: Int = 20): List<ChatHistory>

    @Query("DELETE FROM chat_history WHERE userId = :userId")
    suspend fun clearAll(userId: String = "default")
}
