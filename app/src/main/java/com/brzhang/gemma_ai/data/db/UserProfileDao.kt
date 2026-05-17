package com.brzhang.gemma_ai.data.db

import androidx.room.*
import com.brzhang.gemma_ai.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getProfile(userId: String = "default"): UserProfile?

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun observeProfile(userId: String = "default"): Flow<UserProfile?>
}
