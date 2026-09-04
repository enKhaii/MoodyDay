package com.example.moodyday.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.local.dao.GoalDao
import com.example.moodyday.data.local.dao.UserStreakDao
import com.example.moodyday.data.local.entities.GoalEntity
import com.example.moodyday.data.local.entities.UserStreakEntity
import com.example.moodyday.data.remote.RetrofitProvider
import com.example.moodyday.data.remote.WeatherApi
import com.example.moodyday.data.remote.WeatherCodeTranslator
import com.example.moodyday.ui.forecast.HourlyPoint
import com.example.moodyday.ui.weather.SelectedCityViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class GoalsViewModel(
    private val goalDao: GoalDao,
    private val userStreakDao: UserStreakDao,
    private val userId: String = "chongwc",
    private val weatherApi: WeatherApi = RetrofitProvider.weatherApi,
    private val selectedCityViewModel: SelectedCityViewModel? = null
) : ViewModel() {

    private var cachedHourlyPoints: List<HourlyPoint> = emptyList()

    val userStreak: StateFlow<UserStreakEntity> = userStreakDao.getUserStreak(userId)
        .map { entity ->
            if (entity == null) {
                UserStreakEntity(userId = userId, currentStreak = 0, lastCompletedDate = null)
            } else {
                val todayStr = LocalDate.now().toString()
                val yesterdayStr = LocalDate.now().minusDays(1).toString()
                if (entity.lastCompletedDate == todayStr || entity.lastCompletedDate == yesterdayStr) {
                    entity
                } else {
                    entity.copy(currentStreak = 0)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStreakEntity(userId = userId, currentStreak = 0, lastCompletedDate = null)
        )

    init {
        viewModelScope.launch {
            syncStreakIfNeeded()
        }
    }

    private suspend fun syncStreakIfNeeded() {
        val todayStr = LocalDate.now().toString()
        val yesterdayStr = LocalDate.now().minusDays(1).toString()
        val currentStreakEntity = userStreakDao.getUserStreak(userId).first()
        val allGoals = goalDao.getGoalsForUser(userId).first()
        val completedToday = allGoals.any {
            it.isCompleted && (it.completedAt?.startsWith(todayStr) == true || it.createdAt == todayStr)
        }

        if (completedToday && currentStreakEntity?.lastCompletedDate != todayStr) {
            val prevStreak = if (currentStreakEntity?.lastCompletedDate == yesterdayStr) {
                currentStreakEntity.currentStreak
            } else {
                0
            }
            userStreakDao.insertOrUpdateStreak(
                UserStreakEntity(
                    id = currentStreakEntity?.id ?: 0,
                    userId = userId,
                    currentStreak = prevStreak + 1,
                    lastCompletedDate = todayStr
                )
            )
        }
    }

    val goals: StateFlow<List<GoalEntity>> = goalDao.getGoalsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(
        title: String,
        category: String = "Transport",
        frequency: String = "Daily",
        targetCo2: String = "2.0 kg CO₂",
        reminderEnabled: Boolean = false
    ) {
        if (title.isBlank()) return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val hourly = getHourlyForecast()
            val recommendedTime = suggestBestTime(title, category, hourly)

            val newGoal = GoalEntity(
                userId = userId,
                cityId = null,
                text = title.trim(),
                weatherCondition = recommendedTime,
                category = category,
                frequency = frequency,
                targetCo2 = targetCo2,
                reminderEnabled = reminderEnabled,
                isCompleted = false,
                createdAt = currentDate,
                completedAt = null
            )
            goalDao.insertGoal(newGoal)
        }
    }

    fun updateGoal(
        goalId: Long,
        newText: String,
        category: String = "Transport",
        frequency: String = "Daily",
        targetCo2: String = "2.0 kg CO₂",
        reminderEnabled: Boolean = false
    ) {
        if (newText.isBlank()) return
        viewModelScope.launch {
            val goal = goals.value.find { it.id == goalId }
            if (goal != null) {
                val hourly = getHourlyForecast()
                val updatedRecommendation = suggestBestTime(newText, category, hourly)

                val updatedGoal = goal.copy(
                    text = newText.trim(),
                    weatherCondition = updatedRecommendation,
                    category = category,
                    frequency = frequency,
                    targetCo2 = targetCo2,
                    reminderEnabled = reminderEnabled
                )
                goalDao.updateGoal(updatedGoal)
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            goalDao.deleteGoalById(goalId)
        }
    }

    fun toggleGoalCompletion(goalId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val goal = goals.value.find { it.id == goalId } ?: return@launch
            val completedAt = if (isCompleted) LocalDateTime.now().toString() else null
            goalDao.updateGoal(goal.copy(isCompleted = isCompleted, completedAt = completedAt))

            updateStreakOnCompletionChange(isCompleted)
        }
    }

    private suspend fun updateStreakOnCompletionChange(isCompleted: Boolean) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val todayStr = today.toString()
        val yesterdayStr = yesterday.toString()

        val currentStreakEntity = userStreakDao.getUserStreak(userId).first()
        val currentStreak = currentStreakEntity?.currentStreak ?: 0
        val lastDate = currentStreakEntity?.lastCompletedDate

        if (isCompleted) {
            if (lastDate == todayStr) {
                // Streak already counted for today
                return
            }
            val newStreak = if (lastDate == yesterdayStr) {
                currentStreak + 1
            } else {
                1
            }
            userStreakDao.insertOrUpdateStreak(
                UserStreakEntity(
                    id = currentStreakEntity?.id ?: 0,
                    userId = userId,
                    currentStreak = newStreak,
                    lastCompletedDate = todayStr
                )
            )
        } else {
            // Check if there are still any other goals completed today
            val allUserGoals = goalDao.getGoalsForUser(userId).first()
            val hasOtherCompletedToday = allUserGoals.any {
                it.isCompleted && it.completedAt?.startsWith(todayStr) == true
            }
            if (!hasOtherCompletedToday && lastDate == todayStr) {
                val revertedStreak = (currentStreak - 1).coerceAtLeast(0)
                userStreakDao.insertOrUpdateStreak(
                    UserStreakEntity(
                        id = currentStreakEntity?.id ?: 0,
                        userId = userId,
                        currentStreak = revertedStreak,
                        lastCompletedDate = if (revertedStreak > 0) yesterdayStr else null
                    )
                )
            }
        }
    }

    private suspend fun getHourlyForecast(): List<HourlyPoint> {
        if (cachedHourlyPoints.isNotEmpty()) return cachedHourlyPoints
        return try {
            val lat = selectedCityViewModel?.selectedCity?.value?.lat ?: 3.140853
            val lon = selectedCityViewModel?.selectedCity?.value?.lon ?: 101.693207
            val weather = weatherApi.getWeather(lat, lon, pastDays = 0)
            val nowDateTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)

            val points = weather.hourly?.let { hourlyBlock ->
                hourlyBlock.time
                    .zip(hourlyBlock.temperature_2m)
                    .zip(hourlyBlock.weather_code) { (time, temp), code -> Triple(time, temp, code) }
                    .mapNotNull { (time, temp, code) ->
                        runCatching {
                            val dt = LocalDateTime.parse(time)
                            if (dt < nowDateTime) return@runCatching null
                            val hour24 = dt.hour
                            val period = if (hour24 >= 12) "PM" else "AM"
                            val hour12 = when {
                                hour24 == 0 -> 12
                                hour24 > 12 -> hour24 - 12
                                else -> hour24
                            }
                            HourlyPoint(
                                label = "$hour12 $period",
                                tempF = temp.roundToInt(),
                                weatherCode = code
                            )
                        }.getOrNull()
                    }.take(24)
            } ?: emptyList()

            cachedHourlyPoints = points
            points
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun suggestBestTime(goalText: String, category: String, hourly: List<HourlyPoint>): String {
        val text = goalText.lowercase()
        val isOutdoor = category.equals("Transport", ignoreCase = true) ||
                category.equals("Nature", ignoreCase = true) ||
                listOf("walk", "run", "bike", "cycle", "bus", "train", "plant", "tree", "garden", "outdoor", "park", "transit").any { text.contains(it) }

        if (isOutdoor) {
            if (hourly.isEmpty()) {
                return "Ideal when skies are clear & mild"
            }
            val bestHour = hourly.find { it.weatherCode in 0..3 && it.tempF < 34 }
            return if (bestHour != null) {
                val weatherDesc = WeatherCodeTranslator.toDescription(bestHour.weatherCode)
                val emoji = WeatherCodeTranslator.toEmoji(bestHour.weatherCode)
                "Best time: ${bestHour.label} ($emoji $weatherDesc, ${bestHour.tempF}°C)"
            } else {
                "Rain or heat forecast today. Carry an umbrella!"
            }
        }

        return when (category.lowercase()) {
            "energy" -> "Peak efficiency: turn off appliances (2 PM - 7 PM)"
            "food" -> "Daily habit: Great anytime!"
            "waste" -> "Zero-waste choice: Anytime today"
            "water" -> "Best time: Morning or evening conservation"
            else -> "Indoor goal: Anytime is fine!"
        }
    }
}