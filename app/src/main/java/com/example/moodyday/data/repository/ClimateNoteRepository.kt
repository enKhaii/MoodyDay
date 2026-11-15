package com.example.moodyday.data.repository

import com.example.moodyday.data.local.dao.ClimateNoteDao
import com.example.moodyday.data.local.entities.ClimateNoteEntity
import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class ClimateNoteRemote(
    val id: Long? = null,
    val user_id: String,
    val city_id: Long,
    val note_date: String,
    val note_text: String
)

class ClimateNoteRepository(private val dao: ClimateNoteDao) {
    fun getNotesForCity(userId: String, cityId: Long): Flow<List<ClimateNoteEntity>> =
        dao.getNotesForCity(userId, cityId)

    suspend fun getNoteForDate(userId: String, cityId: Long, noteDate: String): ClimateNoteEntity? =
        dao.getNoteForDate(userId, cityId, noteDate)

    /** Inserts a new note, or updates the existing one for that city+date if
     *  present, then best-effort syncs to Supabase. Returns the local row id. */
    suspend fun saveNote(userId: String, cityId: Long, noteDate: String, noteText: String): Long {
        val existing = dao.getNoteForDate(userId, cityId, noteDate)

        val localId = if (existing != null) {
            dao.updateNote(existing.copy(noteText = noteText))
            existing.id
        } else {
            dao.insertNote(
                ClimateNoteEntity(
                    userId = userId,
                    cityId = cityId,
                    noteDate = noteDate,
                    noteText = noteText
                )
            )
        }

        try {
            val remote = supabase.from("climate_notes").insert(
                ClimateNoteRemote(
                    user_id = userId,
                    city_id = cityId,
                    note_date = noteDate,
                    note_text = noteText
                )
            ) { select() }.decodeSingle<ClimateNoteRemote>()

            dao.updateSupabaseId(localId, remote.id)
        } catch (e: Exception) {
            // offline — stays local only, supabaseId remains null (or unchanged)
        }

        return localId
    }

    suspend fun deleteNote(note: ClimateNoteEntity) {
        dao.deleteNote(note)
        note.supabaseId?.let { id ->
            try {
                supabase.from("climate_notes").delete { filter { eq("id", id) } }
            } catch (e: Exception) { /* will retry later */ }
        }
    }
}