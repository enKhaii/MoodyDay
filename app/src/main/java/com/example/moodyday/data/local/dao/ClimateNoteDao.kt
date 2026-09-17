package com.example.moodyday.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moodyday.data.local.entities.ClimateNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClimateNoteDao {
    @Insert
    suspend fun insertNote(note: ClimateNoteEntity): Long

    @Query("SELECT * FROM climate_notes WHERE userId = :userId AND cityId = :cityId ORDER BY noteDate DESC")
    fun getNotesForCity(userId: String, cityId: Long): Flow<List<ClimateNoteEntity>>

    @Query("SELECT * FROM climate_notes WHERE userId = :userId AND cityId = :cityId AND noteDate = :noteDate LIMIT 1")
    suspend fun getNoteForDate(userId: String, cityId: Long, noteDate: String): ClimateNoteEntity?

    @Update
    suspend fun updateNote(note: ClimateNoteEntity)

    @Delete
    suspend fun deleteNote(note: ClimateNoteEntity)

    @Query("UPDATE climate_notes SET supabaseId = :supabaseId WHERE id = :localId")
    suspend fun updateSupabaseId(localId: Long, supabaseId: Long?)
}
