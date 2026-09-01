package com.example.moodyday.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moodyday.data.local.entities.SavedCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCityDao {
    @Insert
    suspend fun insertCity(city: SavedCityEntity): Long

    @Query("SELECT * FROM saved_cities WHERE userId = :userId ORDER BY sortOrder ASC")
    fun getCitiesForUser(userId: String): Flow<List<SavedCityEntity>>

    @Query("SELECT * FROM saved_cities WHERE userId = :userId AND isHome = 1 LIMIT 1")
    suspend fun getHomeCity(userId: String): SavedCityEntity?

    @Update
    suspend fun updateCity(city: SavedCityEntity)

    @Delete
    suspend fun deleteCity(city: SavedCityEntity)

    @Query("UPDATE saved_cities SET supabaseId = :supabaseId WHERE id = :localId")
    suspend fun updateSupabaseId(localId: Long, supabaseId: Long?)
}