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
    val category: String = "Transport",
    val frequency: String = "Daily",
    val targetCo2: String = "2.0 kg CO₂",
    val reminderEnabled: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: String,
    val completedAt: String? = null
)