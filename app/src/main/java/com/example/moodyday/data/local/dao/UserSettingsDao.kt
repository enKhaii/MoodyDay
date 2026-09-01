package com.example.moodyday.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodyday.data.local.entities.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)

    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    fun getUserSettings(userId: String): Flow<UserSettingsEntity?>

    @Query("DELETE FROM user_settings WHERE userId = :userId")
    suspend fun deleteSettings(userId: String)
}