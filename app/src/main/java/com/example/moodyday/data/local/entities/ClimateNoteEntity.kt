package com.example.moodyday.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "climate_notes")
data class ClimateNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supabaseId: Long? = null,
    val userId: String,
    val cityId: Long,
    val noteDate: String,
    val noteText: String
)