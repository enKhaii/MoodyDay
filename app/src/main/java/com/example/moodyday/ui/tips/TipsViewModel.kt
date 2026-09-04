package com.example.moodyday.ui.tips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.local.AppDatabase
import com.example.moodyday.data.local.entities.GoalEntity
import com.example.moodyday.data.remote.RetrofitProvider
import com.example.moodyday.data.remote.dto.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ClimateTip(
    val id: String,
    val title: String,
    val description: String,
    val icon: String, // e.g., "sunny", "rain", "wind"
    val category: String, // e.g., "HIGH SOLAR POTENTIAL", "TEMPERATURE ALERT"
    val impact: String = "",
    val steps: List<String> = emptyList(),
    val isCompleted: Boolean = false
)

data class TipsUiState(
    val isLoading: Boolean = false,
    val weeklyGoalProgress: Int = 0,
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
        observeGoalProgress()
    }

    fun loadTipsForCity(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // fetch completed goals from Room for today
                val completedTitles = getCompletedTodayTipTitles()
                // fetch remote weather
                val weather = RetrofitProvider.weatherApi.getWeather(lat, lon)

                generateTips(weather, completedTitles)
                generateOutfitRecommendation(weather)

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun generateTips(weather: WeatherResponse, completedTitles: Set<String>) {
        val tips = mutableListOf<ClimateTip>()

        val maxTemp = weather.daily?.temperature_2m_max?.firstOrNull() ?: weather.current.temperature_2m
        val rainChance = weather.daily?.precipitation_probability_max?.firstOrNull() ?: 0
        val windSpeed = weather.current.wind_speed_10m
        val weatherCode = weather.current.weather_code

        fun isDone(checkTitle: String) = completedTitles.contains("Climate Tip: $checkTitle")

        // 1. Extreme Heat Alert Tip
        if (maxTemp >= 33) {
            tips.add(
                ClimateTip(
                    id = "heat_safety",
                    title = "Stay Hydrated & Pre-Cool",
                    description = "Temperatures will peak around ${maxTemp.toInt()}°C. Close blinds during peak daylight and drink water regularly to avoid heat stress.",
                    icon = "temp",
                    category = "TEMPERATURE ALERT",
                    impact = "Reduces grid strain during peak hours and prevents heat exhaustion.",
                    steps = listOf(
                        "Keep curtains drawn facing direct sun",
                        "Hydrate before feeling thirsty",
                        "Set AC thermostat to 24°C–26°C to balance comfort and efficiency"
                    ),
                    isCompleted = isDone("Stay Hydrated & Pre-Cool")
                )
            )
        }

        // 2. Heavy Rain / Storm Alert Tip
        if (rainChance >= 60 || weather.current.weather_code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82)) {
            tips.add(
                ClimateTip(
                    id = "rain_safety",
                    title = "Rain Preparation & Garden Care",
                    description = "High probability of rain ($rainChance%). Pause automated sprinklers and clear outdoor drainage pathways to avoid localized waterlogging.",
                    icon = "rain",
                    category = "PRECIPITATION ALERT",
                    impact = "Saves up to 1,000 liters of treated tap water per household garden.",
                    steps = listOf(
                        "Turn off automatic sprinkler timers",
                        "Check storm drains for leaf debris",
                        "Collect runoff for indoor plants"
                    ),
                    isCompleted = isDone("Rain Preparation & Garden Care")
                )
            )
        }

        // 3. High Wind Alert Tip
        if (windSpeed >= 30) {
            tips.add(
                ClimateTip(
                    id = "wind_safety",
                    title = "Secure Loose Outdoor Items",
                    description = "Wind gusts are hitting ${windSpeed.toInt()} km/h. Anchor balcony items, patio furniture, and avoid parking beneath weak tree branches.",
                    icon = "wind",
                    category = "WIND ADVISORY",
                    impact = "Prevents property damage and reduces urban debris during sudden gusts.",
                    steps = listOf(
                        "Bring potted plants indoors or place against interior walls",
                        "Fold and latch patio umbrellas or sunshades",
                        "Ensure loose trash bins are weighted down"
                    ),
                    isCompleted = isDone("Secure Loose Outdoor Items")
                )
            )
        }

        // 4. Solar / Eco Tip for Clear Days
        if (weather.current.weather_code <= 3 && rainChance < 30) {
            tips.add(
                ClimateTip(
                    id = "dryer_solar",
                    title = "Skip the dryer today",
                    description = "With clear skies and low humidity expected, air-drying laundry saves energy while reducing indoor appliance heat.",
                    icon = "sunny",
                    category = "HIGH SOLAR POTENTIAL",
                    impact = "Saves ~3.3 kg of CO2 emissions and lowers your monthly electric bill.",
                    steps = listOf(
                        "Hang clothes outdoors or near open airflow",
                        "Shake garments out to reduce ironing needs",
                        "Bring clothes in before evening humidity rises"
                    ),
                    isCompleted = isDone("Skip the dryer today")
                )
            )
        }

        _uiState.value = _uiState.value.copy(tips = tips)
    }

    private fun generateOutfitRecommendation(weather: WeatherResponse) {
        val temp = weather.current.temperature_2m
        val rainChance = weather.daily?.precipitation_probability_max?.firstOrNull() ?: 0
        val isRaining = rainChance >= 50 || weather.current.weather_code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82)

        val baseOutfit = when {
            temp > 30 -> "Wear light, breathable cotton or linen fabrics. Wear UV-protective sunglasses and carry sunscreen."
            temp > 22 -> "A lightweight t-shirt with comfortable shorts or chinos is ideal for today's temperature."
            temp > 15 -> "Mild weather. A long-sleeve shirt or light cardigan/overshirt will keep you comfortable."
            else -> "Chilly conditions. Layer up with a warm fleece jacket, sweater, or windbreaker."
        }

        val rainNote = if (isRaining) " Bring a compact umbrella or waterproof shell jacket." else ""

        val title = when {
            isRaining -> "Rainy Day Attire"
            temp > 30 -> "Warm Weather Fit"
            temp < 16 -> "Cool Weather Layers"
            else -> "Comfortable Essentials"
        }

        _uiState.value = _uiState.value.copy(
            outfitTitle = title,
            outfitRecommendation = baseOutfit + rainNote
        )
    }

    private fun observeGoalProgress() {
        viewModelScope.launch {
            goalDao.getGoalsForUser(userId).collect { goals ->
                val startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
                val completedThisWeek = goals.count { goal ->
                    goal.isCompleted && runCatching {
                        LocalDateTime.parse(goal.createdAt).toLocalDate() >= startOfWeek
                    }.getOrDefault(false)
                }
                _uiState.value = _uiState.value.copy(weeklyGoalProgress = completedThisWeek)
            }
        }
    }

    fun markTipAsDone(tip: ClimateTip) {
        if(tip.isCompleted) return  // Prevent duplicate tips (cells)

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

            // Immediately flag the tip as completed in memory
            val updatedTips = _uiState.value.tips.map {
                if (it.id == tip.id) it.copy(isCompleted = true) else it
            }
            _uiState.value = _uiState.value.copy(tips = updatedTips)
        }
    }

    private suspend fun getCompletedTodayTipTitles(): Set<String> {
        val startOfToday = LocalDate.now().atStartOfDay()
        return try {
            // Collect current saved goals from Room
            val goals = goalDao.getGoalsForUser(userId).first()
            goals.filter { goal ->
                goal.isCompleted && runCatching {
                    LocalDateTime.parse(goal.createdAt) >= startOfToday
                }.getOrDefault(false)
            }.map { it.text }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}