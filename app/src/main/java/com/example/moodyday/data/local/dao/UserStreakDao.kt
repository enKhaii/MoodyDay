package com.example.moodyday.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodyday.data.local.entities.UserStreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStreakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: UserStreakEntity)

    @Query("SELECT * FROM user_streaks WHERE userId = :userId LIMIT 1")
    fun getUserStreak(userId: String): Flow<UserStreakEntity?>

    @Query("UPDATE user_streaks SET currentStreak = :streak, lastActiveDate = :date WHERE userId = :userId")
    suspend fun updateStreak(userId: String, streak: Int, date: String)

    @Query("DELETE FROM user_streaks WHERE userId = :userId")
    suspend fun deleteStreak(userId: String)
}