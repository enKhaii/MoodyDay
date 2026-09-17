package com.example.moodyday.data.local.dao

import androidx.room.*
import com.example.moodyday.data.local.entities.AlertRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AlertRuleEntity): Long

    @Query("SELECT * FROM alert_rules WHERE userId = :userId")
    fun getRulesForUser(userId: String): Flow<List<AlertRuleEntity>>

    @Update
    suspend fun updateRule(rule: AlertRuleEntity)

    @Delete
    suspend fun deleteRule(rule: AlertRuleEntity)

    @Query("UPDATE alert_rules SET supabaseId = :supabaseId WHERE id = :localId")
    suspend fun updateSupabaseId(localId: Long, supabaseId: Long?)
}