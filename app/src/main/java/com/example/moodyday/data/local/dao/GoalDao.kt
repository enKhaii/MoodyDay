package com.example.moodyday.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moodyday.data.local.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert
    suspend fun insertGoal(goal: GoalEntity): Long

    @Query("SELECT * FROM goals WHERE userId = :userId OR userId = 'chongwc' OR userId = '' ORDER BY createdAt DESC")
    fun getGoalsForUser(userId: String): Flow<List<GoalEntity>>

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)

    @Query("UPDATE goals SET supabaseId = :supabaseId WHERE id = :localId")
    suspend fun updateSupabaseId(localId: Long, supabaseId: Long?)
}