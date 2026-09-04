package com.example.moodyday.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.local.dao.GoalDao
import com.example.moodyday.data.local.dao.UserStreakDao
import com.example.moodyday.data.local.entities.GoalEntity
import com.example.moodyday.data.local.entities.UserStreakEntity
import com.example.moodyday.data.remote.WeatherCodeTranslator
import com.example.moodyday.ui.forecast.HourlyPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoalsViewModel(
    private val goalDao: GoalDao,
    private val userStreakDao: UserStreakDao,
    private val userId: String = "chongwc"
) : ViewModel() {

    val userStreak: StateFlow<UserStreakEntity> = userStreakDao.getUserStreak(userId)
        .map { it ?: UserStreakEntity(userId = userId, currentStreak = 0, lastCompletedDate = null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStreakEntity(userId = userId, currentStreak = 0, lastCompletedDate = null)
        )

    val goals: StateFlow<List<GoalEntity>> = goalDao.getGoalsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(title: String) {
        if (title.isBlank()) return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val mockHourlyData = listOf(
            HourlyPoint("1 PM", 88, 95), // Thunderstorm, hot
            HourlyPoint("4 PM", 75, 1),  // Partly cloudy, nice temp
            HourlyPoint("6 PM", 72, 0)   // Clear sky, nice temp
        )
        val recommendedTime = suggestBestTime(title, mockHourlyData)

        val newGoal = GoalEntity(
            userId = userId,
            cityId = null,
            text = title,
            weatherCondition = recommendedTime,
            isCompleted = false,
            createdAt = currentDate
        )
        viewModelScope.launch {
            goalDao.insertGoal(newGoal)
        }
    }

    fun toggleGoalCompletion(goalId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val goal = goals.value.find { it.id == goalId }
            if (goal != null) {
                goalDao.updateGoal(goal.copy(isCompleted = isCompleted))
            }
        }
    }


    private fun suggestBestTime(goalText: String, hourly: List<HourlyPoint>): String {
        if (hourly.isEmpty()) return "Anytime"

        val text = goalText.lowercase()
        val isOutdoor = listOf("walk", "run", "bike", "plant", "tree", "garden", "transport", "bus", "train").any { text.contains(it) }

        if (isOutdoor) {
            val bestHour = hourly.find { it.weatherCode in 0..3 && it.tempF < 86 }
            if (bestHour != null) {
                val weatherDesc = WeatherCodeTranslator.toDescription(bestHour.weatherCode)
                val emoji = WeatherCodeTranslator.toEmoji(bestHour.weatherCode)
                return "Best time: ${bestHour.label} ($emoji $weatherDesc, ${bestHour.tempF}°F)"
            } else {
                return "No ideal outdoor weather today. Bring an umbrella!"
            }
        }

        return "Indoor goal: Anytime is fine!"
    }
}