package com.example.moodyday.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.remote.RetrofitProvider
import com.example.moodyday.data.remote.WeatherCodeTranslator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// CITY IS HARD CODED FOR TESTING, WILL REMOVE IN THE FUTURE
data class HomeUiState(
    val isLoading: Boolean = true,
    val cityName: String = "Kuala Lumpur",
    val temperature: Double? = null,
    val feelsLike: Double? = null,      // feelsLike = Temperature HUMANS feels (apparent temperature)
    val humidity: Double? = null,
    val windSpeed: Double? = null,
    val uvIndex: Double? = null,
    val condition: String = "",
    val conditionEmoji: String = "",
    val error: String? = null

)

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
                val response = RetrofitProvider.weatherApi.getWeather(lat = lat, lon = lon)
                _uiState.value = HomeUiState(
                    isLoading = false,
                    cityName = cityName,
                    temperature = response.current.temperature_2m,
                    feelsLike = response.current.apparent_temperature,
                    humidity = response.current.relative_humidity_2m,
                    windSpeed = response.current.wind_speed_10m,
                    uvIndex = response.daily?.uv_index_max?.firstOrNull(),
                    condition = WeatherCodeTranslator.toDescription(response.current.weather_code),
                    conditionEmoji = WeatherCodeTranslator.toEmoji(response.current.weather_code)
                )
            } catch (e : Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to Load Weather")
            }
        }
    }
}