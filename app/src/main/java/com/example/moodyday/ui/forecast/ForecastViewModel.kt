package com.example.moodyday.ui.forecast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.auth.SessionManager
import com.example.moodyday.data.local.AppDatabase
import com.example.moodyday.data.local.entities.SavedCityEntity
import com.example.moodyday.data.remote.RetrofitProvider
import com.example.moodyday.data.remote.dto.GeocodingResult
import com.example.moodyday.data.repository.ClimateNoteRepository
import com.example.moodyday.data.repository.ForecastRepository
import com.example.moodyday.data.repository.SavedCityRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForecastViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ForecastRepository = ForecastRepository(
        weatherApi = RetrofitProvider.weatherApi,
        archiveApi = RetrofitProvider.archiveApi,
        geocodingApi = RetrofitProvider.geocodingApi
    )
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val savedCityRepository = SavedCityRepository(db.savedCityDao())
    private val climateNoteRepository = ClimateNoteRepository(db.climateNoteDao())

    private val userId = SessionManager.currentUserId ?: ""

    private val _notes = MutableStateFlow<Map<String, String>>(emptyMap())
    val notes: StateFlow<Map<String, String>> = _notes.asStateFlow()

    private val _homeState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _historicalState = MutableStateFlow(HistoricalUiState(isLoading = true))
    val historicalState: StateFlow<HistoricalUiState> = _historicalState.asStateFlow()

    private val _citySuggestions = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val citySuggestions: StateFlow<List<GeocodingResult>> = _citySuggestions.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null
    private var notesJob: Job? = null

    private var lastLat: Double = 3.140853
    private var lastLon: Double = 101.693207
    private var lastCityName: String = "Kuala Lumpur"
    private var lastCountryCode: String? = "MY"

    // Null until this city has been saved to Room (lazily, the first time a
    // note is added — see ensureCitySaved()). Notes require a cityId, so this
    // also drives which city's notes we're currently observing.
    private var currentCityId: Long? = null

    /** Called on every keystroke in the search overlay. Debounces so we don't
     *  hit the geocoding API on every single character. */
    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _citySuggestions.value = emptyList()
            _isSearching.value = false
            return
        }
        _isSearching.value = true
        searchJob = viewModelScope.launch {
            delay(300) // debounce: wait for typing to pause
            val results = runCatching { repository.searchCities(query) }.getOrDefault(emptyList())
            _citySuggestions.value = results
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _citySuggestions.value = emptyList()
        _isSearching.value = false
    }

    /** User tapped a suggestion from the dropdown — we already have its lat/lon,
     *  so skip re-geocoding and load weather directly. */
    fun selectCity(result: GeocodingResult) {
        lastLat = result.latitude
        lastLon = result.longitude
        lastCityName = result.name
        lastCountryCode = result.countryCode
        clearSearch()
        onCityChanged()
    }

    fun loadCity(lat: Double, lon: Double, cityName: String, countryCode: String?) {
        if (lastLat == lat && lastLon == lon && lastCityName == cityName && lastCountryCode == countryCode && !_homeState.value.isLoading) {
            return
        }
        lastLat = lat
        lastLon = lon
        lastCityName = cityName
        lastCountryCode = countryCode
        onCityChanged()
    }

    fun loadCity(cityQuery: String) {
        _homeState.value = _homeState.value.copy(isLoading = true, error = null)
        _historicalState.value = _historicalState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            runCatching { repository.geocode(cityQuery) }
                .onSuccess { (lat, lon, label) ->
                    lastLat = lat
                    lastLon = lon
                    lastCityName = label.substringBefore(",")
                    lastCountryCode = label.substringAfterLast(",", "").trim()
                    onCityChanged()
                }
                .onFailure { e ->
                    _homeState.value = _homeState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    /** Adds or updates the note for [date], saving the city to Room first if it
     *  hasn't been saved yet — ClimateNoteEntity requires a cityId. */
    fun saveNote(date: String, text: String) {
        viewModelScope.launch {
            val cityId = ensureCitySaved()
            climateNoteRepository.saveNote(userId, cityId, date, text)
            // notesJob is already collecting this city's notes, so _notes
            // updates on its own once the Room write completes.
        }
    }

    fun deleteNote(date: String) {
        val cityId = currentCityId ?: return // nothing saved yet, nothing to delete
        viewModelScope.launch {
            climateNoteRepository.getNoteForDate(userId, cityId, date)?.let { note ->
                climateNoteRepository.deleteNote(note)
            }
        }
    }

    fun refresh() {
        loadHome()
        loadHistorical()
    }

    private fun onCityChanged() {
        _homeState.value = _homeState.value.copy(isLoading = true, error = null)
        _historicalState.value = _historicalState.value.copy(isLoading = true, error = null)
        loadHome()
        loadHistorical()

        viewModelScope.launch {
            // Check whether this city was already saved on a previous visit
            // (e.g. a note was added last time) so we resume observing its notes.
            currentCityId = savedCityRepository.findByCoordinates(userId, lastLat, lastLon)?.id
            observeNotesForCurrentCity()
        }
    }

    /** Returns the Room id for the currently-viewed city, saving it first if
     *  this is the first time a note has been added for it. */
    private suspend fun ensureCitySaved(): Long {
        currentCityId?.let { return it }

        val existing = savedCityRepository.findByCoordinates(userId, lastLat, lastLon)
        val cityId = existing?.id ?: savedCityRepository.addCity(
            SavedCityEntity(
                userId = userId,
                cityName = lastCityName,
                country = lastCountryCode,
                lat = lastLat,
                lon = lastLon
            )
        )

        currentCityId = cityId
        observeNotesForCurrentCity()
        return cityId
    }

    private fun observeNotesForCurrentCity() {
        notesJob?.cancel()
        val cityId = currentCityId
        if (cityId == null) {
            _notes.value = emptyMap()
            return
        }
        notesJob = viewModelScope.launch {
            climateNoteRepository.getNotesForCity(userId, cityId).collect { entities ->
                _notes.value = entities.associate { it.noteDate to it.noteText }
            }
        }
    }

    private fun loadHome() {
        viewModelScope.launch {
            runCatching { repository.getHomeUiState(lastLat, lastLon, lastCityName, lastCountryCode) }
                .onSuccess { _homeState.value = it }
                .onFailure { e ->
                    _homeState.value = _homeState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    private fun loadHistorical() {
        viewModelScope.launch {
            runCatching { repository.getHistoricalUiState(lastLat, lastLon) }
                .onSuccess { _historicalState.value = it }
                .onFailure { e ->
                    _historicalState.value = _historicalState.value.copy(isLoading = false, error = e.message)
                }
        }
    }
}