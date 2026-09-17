package com.example.moodyday.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.auth.SessionManager
import com.example.moodyday.data.local.AppDatabase
import com.example.moodyday.data.local.entities.AlertRuleEntity
import com.example.moodyday.data.remote.RetrofitProvider
import com.example.moodyday.data.remote.dto.WeatherResponse
import com.example.moodyday.data.repository.AlertRuleRepository
import com.example.moodyday.data.repository.SavedCityRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ActiveAlert(
    val title: String,
    val description: String,
    val severity: String, // HIGH, MODERATE
    val endsIn: String
)

data class AlertsUiState(
    val isLoading: Boolean = false,
    val activeAlerts: List<ActiveAlert> = emptyList(),
    val customRules: List<AlertRuleEntity> = emptyList(),
    val globalNotificationsEnabled: Boolean = true,
    val error: String? = null
)

class AlertsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AlertRuleRepository(db.alertRuleDao())
    private val savedCityRepository = SavedCityRepository(db.savedCityDao())

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState

    private val userId = SessionManager.currentUserId ?: ""
    private var currentWeather: WeatherResponse? = null

    private var currentCityId: Long? = null
    private var rulesJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getRules(userId).collectLatest { rules ->
                _uiState.value = _uiState.value.copy(customRules = rules)

                currentWeather?.let { weather ->
                    generateAndCheckAlerts(weather, rules)
                }
            }
        }
    }

    fun loadAlertsForCity(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Look up the real database ID for this city
                val savedCity = savedCityRepository.findByCoordinates(userId, lat, lon)
                currentCityId = savedCity?.id

                // Fetch the weather
                val weather = RetrofitProvider.weatherApi.getWeather(lat, lon)
                currentWeather = weather

                // 3. Observe rules exclusively for this city
                observeRulesForCurrentCity()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    private fun observeRulesForCurrentCity() {
        rulesJob?.cancel()
        rulesJob = viewModelScope.launch {
            repository.getRules(userId).collectLatest { allRules ->
                // Display the user's custom rules
                _uiState.value = _uiState.value.copy(customRules = allRules)

                currentWeather?.let { weather ->
                    generateAndCheckAlerts(weather, allRules)
                }
            }
        }
    }

    private fun generateAndCheckAlerts(weather: WeatherResponse, rules: List<AlertRuleEntity>) {
        val alerts = mutableListOf<ActiveAlert>()

        // 1. Built-in Alerts
        val maxTemp = weather.daily?.temperature_2m_max?.firstOrNull() ?: 0.0
        when {
            maxTemp >= 38 -> alerts.add(
                ActiveAlert(
                    title = "Extreme Heat Warning",
                    description = "Temperatures expected to reach ${maxTemp.toInt()}°C. Prolonged exposure can cause heat stroke. Stay indoors and stay hydrated.",
                    severity = "HIGH SEVERITY",
                    endsIn = "Today"
                )
            )
            maxTemp >= 33 -> alerts.add(
                ActiveAlert(
                    title = "Heat Advisory",
                    description = "Temperatures expected to reach ${maxTemp.toInt()}°C. Stay hydrated and avoid prolonged sun exposure.",
                    severity = "MODERATE",
                    endsIn = "Today"
                )
            )
        }

        val windSpeed = weather.current.wind_speed_10m
        if (windSpeed >= 40) {
            alerts.add(
                ActiveAlert(
                    title = "High Wind Warning",
                    description = "Wind speeds of ${windSpeed.toInt()} km/h expected. Secure loose outdoor items.",
                    severity = "MODERATE",
                    endsIn = "Today"
                )
            )
        }

        val rainChance = weather.daily?.precipitation_probability_max?.firstOrNull() ?: 0
        if (rainChance >= 70) {
            alerts.add(
                ActiveAlert(
                    title = "Heavy Rain Expected",
                    description = "$rainChance% chance of rain today. Flooding possible in low-lying areas.",
                    severity = "MODERATE",
                    endsIn = "Today"
                )
            )
        }

        // 2. Custom Rule Alerts
        val triggeredCustomAlerts = rules.filter { it.isEnabled }.mapNotNull { rule ->
            val currentTemp = weather.current.temperature_2m
            val isTemp = rule.condition.contains("Temp", ignoreCase = true)
            val isRain = rule.condition.contains("Rain", ignoreCase = true) || rule.condition.contains("Precip", ignoreCase = true)
            val isWind = rule.condition.contains("Wind", ignoreCase = true)

            val triggered = when {
                isTemp -> currentTemp >= rule.threshold
                isRain -> rainChance >= rule.threshold
                isWind -> weather.current.wind_speed_10m >= rule.threshold
                else -> false
            }

            if (triggered) {
                val description = when {
                    isTemp -> "Current temperature is ${currentTemp.toInt()}°C, exceeding your limit of ${rule.threshold.toInt()}°C."
                    isRain -> "Precipitation probability is $rainChance%, exceeding your threshold of ${rule.threshold.toInt()}%."
                    isWind -> "Wind speed is ${weather.current.wind_speed_10m.toInt()} km/h, exceeding your limit of ${rule.threshold.toInt()} km/h."
                    else -> "Condition exceeded your set threshold of ${rule.threshold.toInt()}."
                }

                ActiveAlert(
                    title = rule.condition,
                    description = description,
                    severity = "CUSTOM",
                    endsIn = "Active now"
                )
            } else null
        }

        alerts.addAll(triggeredCustomAlerts)

        _uiState.value = _uiState.value.copy(
            activeAlerts = alerts,
            isLoading = false
        )
    }

    fun addRule(condition: String, threshold: Double) {
        viewModelScope.launch {
            // Fall back to cityId = 1L if the city is an unsaved dynamic lookup
            val targetCityId = currentCityId ?: 1L

            val newRule = AlertRuleEntity(
                userId = userId,
                cityId = targetCityId,
                condition = condition,
                threshold = threshold,
                isEnabled = true
            )
            repository.addRule(newRule)
        }
    }

    fun toggleRule(rule: AlertRuleEntity) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteRule(rule: AlertRuleEntity) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun toggleGlobalNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(globalNotificationsEnabled = enabled)
    }
}