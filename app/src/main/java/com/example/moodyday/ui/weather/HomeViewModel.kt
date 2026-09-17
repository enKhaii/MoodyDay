package com.example.moodyday.ui.weather

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.remote.RetrofitProvider
import com.example.moodyday.data.remote.WeatherCodeTranslator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val cityName: String = "Kuala Lumpur",
    val countryCode: String? = null,
    val temperature: Double? = null,
    val feelsLike: Double? = null,      // feelsLike = Temperature HUMANS feels (apparent temperature)
    val humidity: Double? = null,
    val windSpeed: Double? = null,
    val uvIndex: Double? = null,
    val condition: String = "",
    val conditionEmoji: String = "",
    val error: String? = null,
    val insight: String? = null,
    val insightText: String? = null,
    val insightType: InsightType = InsightType.NEUTRAL,
    val weatherIcon: ImageVector = Icons.Default.WbSunny
)

enum class InsightType { WARMER, COOLER, NEUTRAL }

class HomeViewModel : ViewModel() {
    // Private Mutable_State - only ViewModel can change
    private val _uiState = MutableStateFlow(HomeUiState())

    // Public READ-ONLY view of the state for the UI
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadWeather(lat = 3.140853, lon = 101.693207, cityName = "Kuala Lumpur")
    }

    fun loadWeather (
        lat: Double,
        lon: Double,
        cityName: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Weather API
                val weatherResponse = RetrofitProvider.weatherApi.getWeather(lat = lat, lon = lon)

                // Nominatim API
                val geoResponse = RetrofitProvider.nominatimApi.reverseGeocode(lat, lon)
                val countryCode = geoResponse.address?.countryCode

                val insight = weatherResponse.daily?.let { daily ->
                    if (daily.temperature_2m_max.size >= 2) {
                        val yesterday = daily.temperature_2m_max[0]
                        val today = daily.temperature_2m_max[1]
                        val diff = today - yesterday
                        when {
                            diff > 2 -> "%.1f°C warmer than yesterday".format(diff)
                            diff < -2 -> "%.1f°C cooler than yesterday".format(-diff)
                            else -> "Similar to yesterday"
                        }
                    } else null
                }

                val (insightText, insightType) = weatherResponse.daily?.let { daily ->
                    if (daily.temperature_2m_max.size >= 2) {
                        val yesterday = daily.temperature_2m_max[0]
                        val today = daily.temperature_2m_max[1]
                        val diff = today - yesterday
                        when {
                            diff > 2 -> "%.1f°C warmer than yesterday".format(diff) to InsightType.WARMER
                            diff < -2 -> "%.1f°C cooler than yesterday".format(-diff) to InsightType.COOLER
                            else -> "Similar to yesterday's temperature" to InsightType.NEUTRAL
                        }
                    } else null to InsightType.NEUTRAL
                } ?: (null to InsightType.NEUTRAL)

                val nowHour = java.time.LocalTime.now().hour
                val isNight = nowHour >= 19 || nowHour < 7

                _uiState.value = HomeUiState(
                    isLoading = false,
                    cityName = cityName,
                    countryCode = countryCode,
                    temperature = weatherResponse.current.temperature_2m,
                    feelsLike = weatherResponse.current.apparent_temperature,
                    humidity = weatherResponse.current.relative_humidity_2m,
                    windSpeed = weatherResponse.current.wind_speed_10m,
                    uvIndex = weatherResponse.daily?.uv_index_max?.firstOrNull(),
                    condition = WeatherCodeTranslator.toDescription(weatherResponse.current.weather_code),
                    conditionEmoji = WeatherCodeTranslator.toEmoji(weatherResponse.current.weather_code, isNight = isNight),
                    insight = insight,
                    insightText = insightText,
                    insightType = insightType,
                    weatherIcon = WeatherCodeTranslator.toIcon(weatherResponse.current.weather_code, isNight = isNight),
                )
            } catch (e : Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to Load Weather")
            }
        }
    }
}