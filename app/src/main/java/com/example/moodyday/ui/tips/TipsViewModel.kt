package com.example.moodyday.ui.tips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.local.AppDatabase
import com.example.moodyday.data.local.entities.GoalEntity
import com.example.moodyday.data.remote.RetrofitProvider
import com.example.moodyday.data.remote.dto.WeatherResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ClimateTip(
    val id: String,
    val title: String,
    val description: String,
    val icon: String, // e.g., "sunny", "rain", "wind"
    val category: String, // e.g., "HIGH SOLAR POTENTIAL", "TEMPERATURE ALERT"
    val isCompleted: Boolean = false
)

data class TipsUiState(
    val isLoading: Boolean = false,
    val weeklyGoalProgress: Int = 2,
    val weeklyGoalTotal: Int = 5,
    val tips: List<ClimateTip> = emptyList(),
    val outfitRecommendation: String = "",
    val outfitTitle: String = "Outfit Recommendation",
    val error: String? = null
)

class TipsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val goalDao = db.goalDao()
    
    private val _uiState = MutableStateFlow(TipsUiState())
    val uiState: StateFlow<TipsUiState> = _uiState

    private val userId = "chongwc"

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Fetch weather for Kuala Lumpur as default
                val weather = RetrofitProvider.weatherApi.getWeather(3.140853, 101.693207)
                
                generateTips(weather)
                generateOutfitRecommendation(weather)
                observeGoalProgress()
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun generateTips(weather: WeatherResponse) {
        val tips = mutableListOf<ClimateTip>()
        
        // Logic for "Skip the dryer today" (Sunny and low humidity)
        if (weather.current.weather_code <= 3 && weather.current.relative_humidity_2m < 60) {
            tips.add(
                ClimateTip(
                    id = "dryer",
                    title = "Skip the dryer today",
                    description = "With clear skies and low humidity expected, line-drying clothes is highly efficient today, saving significant energy.",
                    icon = "sunny",
                    category = "HIGH SOLAR POTENTIAL"
                )
            )
        }

        // Logic for "Pre-cool your home" (High temp expected)
        val maxTemp = weather.daily?.temperature_2m_max?.firstOrNull() ?: 0.0
        if (maxTemp > 32) {
            tips.add(
                ClimateTip(
                    id = "precool",
                    title = "Pre-cool your home",
                    description = "Temperatures will peak at ${maxTemp}°C by 3 PM. Open windows now while it's cooler to delay AC usage.",
                    icon = "temp",
                    category = "TEMPERATURE ALERT"
                )
            )
        }

        // Logic for "Pause the sprinklers" (Rain expected)
        if (weather.current.weather_code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82)) {
            tips.add(
                ClimateTip(
                    id = "sprinklers",
                    title = "Pause the sprinklers",
                    description = "Rain is forecasted today. Your garden will receive sufficient natural hydration.",
                    icon = "rain",
                    category = "PRECIPITATION EXPECTED"
                )
            )
        }

        _uiState.value = _uiState.value.copy(tips = tips)
    }

    private fun generateOutfitRecommendation(weather: WeatherResponse) {
        val temp = weather.current.temperature_2m
        val recommendation = when {
            temp > 30 -> "Wear light, breathable cotton clothes. Don't forget your sunglasses and sunscreen!"
            temp > 22 -> "Comfortable t-shirt and light trousers should be perfect for this weather."
            temp > 15 -> "A light jacket or sweater over your t-shirt would be a good idea."
            else -> "It's a bit chilly. Make sure to wear a warm coat or layer up."
        }
        _uiState.value = _uiState.value.copy(outfitRecommendation = recommendation)
    }

    private fun observeGoalProgress() {
        viewModelScope.launch {
            goalDao.getGoalsForUser(userId).collect { goals ->
                val completedThisWeek = goals.count { it.isCompleted } // Simplified weekly check
                _uiState.value = _uiState.value.copy(weeklyGoalProgress = completedThisWeek)
            }
        }
    }

    fun markTipAsDone(tip: ClimateTip) {
        viewModelScope.launch {
            val goal = GoalEntity(
                userId = userId,
                cityId = 1,
                text = "Climate Tip: ${tip.title}",
                weatherCondition = tip.category,
                isCompleted = true,
                createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            goalDao.insertGoal(goal)
            
            // Update local tip state
            val updatedTips = _uiState.value.tips.map {
                if (it.id == tip.id) it.copy(isCompleted = true) else it
            }
            _uiState.value = _uiState.value.copy(tips = updatedTips)
        }
    }
}