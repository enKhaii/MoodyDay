package com.example.moodyday.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supabaseId: Long? = null,
    val userId: String,
    val cityId: Long?,
    val text: String,
    val weatherCondition: String?,
    val isCompleted: Boolean = false,
    val createdAt: String
)