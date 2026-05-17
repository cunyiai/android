package com.brzhang.gemma_ai.data.db

import androidx.room.*
import com.brzhang.gemma_ai.data.entity.HealthRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {

    @Insert
    suspend fun insert(record: HealthRecord): Long

    @Query("SELECT * FROM health_record WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(userId: String = "default", limit: Int = 50): List<HealthRecord>

    @Query("SELECT * FROM health_record WHERE userId = :userId AND recordType = :type ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentByType(type: String, userId: String = "default", limit: Int = 20): List<HealthRecord>

    @Query("SELECT * FROM health_record WHERE userId = :userId AND timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun getByDateRange(from: Long, to: Long, userId: String = "default"): List<HealthRecord>

    @Query("SELECT * FROM health_record WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeAll(userId: String = "default"): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_record WHERE userId = :userId AND recordType = :type ORDER BY timestamp DESC LIMIT 3")
    suspend fun getLast3ByType(type: String, userId: String = "default"): List<HealthRecord>
}
