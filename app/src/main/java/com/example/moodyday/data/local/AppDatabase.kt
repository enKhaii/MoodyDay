package com.example.moodyday.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.moodyday.data.local.dao.GoalDao
import com.example.moodyday.data.local.dao.SavedCityDao
import com.example.moodyday.data.local.dao.UserSettingsDao
import com.example.moodyday.data.local.dao.UserStreakDao
import com.example.moodyday.data.local.entities.AlertRuleEntity
import com.example.moodyday.data.local.entities.ClimateNoteEntity
import com.example.moodyday.data.local.entities.GoalEntity
import com.example.moodyday.data.local.entities.SavedCityEntity
import com.example.moodyday.data.local.entities.UserSettingsEntity
import com.example.moodyday.data.local.entities.UserStreakEntity

@Database(
    entities = [
        SavedCityEntity::class,
        ClimateNoteEntity::class,
        AlertRuleEntity::class,
        GoalEntity::class,
        UserSettingsEntity::class,
        UserStreakEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // ADD YOUR OWN DAO FILE IN local/dao THEN UNCOMMENT YOUR DAO!!!!!
    abstract fun savedCityDao(): SavedCityDao
//    abstract fun climateNoteDao(): ClimateNoteDao
//    abstract fun alertRuleDao(): AlertRuleDao
    abstract fun goalDao(): GoalDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun userStreakDao(): UserStreakDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moodyday_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}