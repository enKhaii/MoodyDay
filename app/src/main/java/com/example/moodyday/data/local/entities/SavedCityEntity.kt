package com.example.moodyday.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_cities")
data class SavedCityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supabaseId: Long? = null,
    val userId: String,
    val cityName: String,
    val country: String?,
    val lat: Double,
    val lon: Double,
    val isHome: Boolean = false,
    val sortOrder: Int = 0
)