package com.example.moodyday.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.auth.SessionManager
import com.example.moodyday.data.local.AppDatabase
import com.example.moodyday.data.local.entities.UserSettingsEntity
import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.auth.auth
import com.example.moodyday.data.repository.SavedCityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val settingsDao = db.userSettingsDao()
    private val goalDao = db.goalDao()
    private val cityDao = db.savedCityDao()
    private val savedCityRepository = SavedCityRepository(cityDao)

    private val userId: String = SessionManager.getActiveUserId()

    val settingsState: StateFlow<UserSettingsEntity> = settingsDao.getUserSettings(userId)
        .map { it ?: UserSettingsEntity(userId = userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity(userId = userId))

    val userEmail: String = supabase.auth.currentUserOrNull()?.email ?: "User"

    private val _completedGoalsCount = MutableStateFlow(0)
    val completedGoalsCount: StateFlow<Int> = _completedGoalsCount

    private val _savedCitiesCount = MutableStateFlow(0)
    val savedCitiesCount: StateFlow<Int> = _savedCitiesCount

    init {
        observeMonthlyGoals()
        observeSavedCities()
        viewModelScope.launch {
            try {
                savedCityRepository.fetchFromRemote(userId)
            } catch (e: Exception) {
                // Ignore offline error
            }
        }
    }

    private fun observeMonthlyGoals() {
        viewModelScope.launch {
            goalDao.getGoalsForUser(userId).collect { goals ->
                val startOfMonth = LocalDate.now().withDayOfMonth(1)
                val monthlyCount = goals.count { goal ->
                    goal.isCompleted && runCatching {
                        val dateStr = (goal.completedAt ?: goal.createdAt).substringBefore("T")
                        LocalDate.parse(dateStr) >= startOfMonth
                    }.getOrDefault(false)
                }
                _completedGoalsCount.value = monthlyCount
            }
        }
    }

    private fun observeSavedCities() {
        viewModelScope.launch {
            try {
                cityDao.getCitiesForUser(userId).collect { cities ->
                    _savedCitiesCount.value = cities.size
                }
            } catch (e: Exception) {
                _savedCitiesCount.value = 0
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            settingsDao.insertOrUpdateSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            settingsDao.deleteSettings(userId)
            _completedGoalsCount.value = 0
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
            } catch (_: Exception) { }
            onLoggedOut()
        }
    }
}