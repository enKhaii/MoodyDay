package com.example.moodyday.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.local.entities.SavedCityEntity
import com.example.moodyday.data.remote.RetrofitProvider
import com.example.moodyday.data.remote.dto.GeocodingResult
import com.example.moodyday.data.repository.SavedCityRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CitySearchViewModel(
    private val repository: SavedCityRepository,
    private val userId: String
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _searchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResults: StateFlow<List<GeocodingResult>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    val savedCities: StateFlow<List<SavedCityEntity>> = repository.getCities(userId)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if(newQuery.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(400)          // debounce - wait for typing to pause before connect the API
            _isSearching.value = true
            try {
                val response = RetrofitProvider.geocodingApi.searchCity(newQuery)
                _searchResults.value = response.results ?: emptyList()
            } catch(e : Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun saveCity(result: GeocodingResult) {
        viewModelScope.launch {
            repository.addCity(
                SavedCityEntity(
                    userId = userId,
                    cityName = result.name,
                    country = result.country,
                    countryCode = result.countryCode,
                    lat = result.latitude,
                    lon = result.longitude,
                    sortOrder = savedCities.value.size
                )
            )
            _query.value = ""
            _searchResults.value = emptyList()
        }
    }

    fun deleteCity(city: SavedCityEntity) {
        viewModelScope.launch {
            repository.deleteCity(city)
        }
    }

    @SuppressLint("MissingPermission")  // permission is checked in the screen before calling this
    fun useCurrentLocation(context: Context, onResult: (name: String, lat: Double, lon: Double) -> Unit) {
        viewModelScope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val location = client.lastLocation.await()
                if (location != null) {
                    val cityName = try {
                        val response = RetrofitProvider.nominatimApi.reverseGeocode(location.latitude, location.longitude)
                        response.address?.city ?: response.address?.town ?: response.address?.village ?: "Current Location"
                    } catch (e: Exception) {
                        "Current Location" // fallback if reverse geocoding fails
                    }
                    onResult(cityName, location.latitude, location.longitude)
                }
            } catch (e: Exception) { /* location unavailable */ }
        }
    }
}