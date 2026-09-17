package com.example.moodyday.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_streaks")
data class UserStreakEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val currentStreak: Int = 0,
    val lastCompletedDate: String?
)