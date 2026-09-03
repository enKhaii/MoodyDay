package com.example.moodyday.data

import android.content.Context
import com.example.moodyday.data.local.AppDatabase
import com.example.moodyday.data.repository.SavedCityRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.getDatabase(context)

    val savedCityRepository = SavedCityRepository(database.savedCityDao())
    // val goalRepository = GoalRepository(database.goalDao())
    // val climateNoteRepository = ClimateNoteRepository(database.climateNoteDao())
}