package com.example.moodyday.ui.weather

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SelectedCity(
    val name: String,
    val lat: Double,
    val lon: Double
)

class SelectedCityViewModel : ViewModel() {

    // CITY HARD CODED FOR TESTING PURPOSE
    private val _selectedCity = MutableStateFlow(
        SelectedCity(name = "Kuala Lumpur", lat = 3.140853, lon = 101.693207)
    )
    val selectedCity: StateFlow<SelectedCity> = _selectedCity

    fun selectCity(city: SelectedCity) {
        _selectedCity.value = city
    }
}