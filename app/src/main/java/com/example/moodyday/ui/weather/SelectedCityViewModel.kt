package com.example.moodyday.ui.weather

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SelectedCity(
    val name: String,
    val lat: Double,
    val lon: Double,
    val countryCode: String? = null
)

class SelectedCityViewModel : ViewModel() {

    // CITY HARD CODED AS DEFAULT CITY
    private val _selectedCity = MutableStateFlow(
        SelectedCity(name = "Kuala Lumpur", lat = 3.140853, lon = 101.693207, countryCode = "MY")
    )
    val selectedCity: StateFlow<SelectedCity> = _selectedCity

    fun selectCity(city: SelectedCity) {
        _selectedCity.value = city
    }
}