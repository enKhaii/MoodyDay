package com.example.moodyday.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_rules")
data class AlertRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supabaseId: Long? = null,
    val userId: String,
    val cityId: Long,
    val condition: String,
    val threshold: Double,
    val isEnabled: Boolean = true
)