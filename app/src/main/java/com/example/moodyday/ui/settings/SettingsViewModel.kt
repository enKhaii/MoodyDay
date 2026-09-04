package com.example.moodyday.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.auth.SessionManager
import com.example.moodyday.data.local.AppDatabase
import com.example.moodyday.data.local.dao.UserSettingsDao
import com.example.moodyday.data.local.entities.UserSettingsEntity
import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class SettingsViewModel(
    application: Application,
    private val settingsDao: UserSettingsDao = AppDatabase.getDatabase(application).userSettingsDao(),
    private val userId: String = supabase.auth.currentUserOrNull()?.id ?: SessionManager.currentUserId ?: ""
) : AndroidViewModel(application) {

    private val goalDao = AppDatabase.getDatabase(application).goalDao()

    val settingsState: StateFlow<UserSettingsEntity> = settingsDao.getUserSettings(userId)
        .map { it ?: UserSettingsEntity(userId = userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity(userId = userId))

    val userEmail: String = supabase.auth.currentUserOrNull()?.email ?: ""

    private val _completedGoalsCount = MutableStateFlow(0)
    val completedGoalsCount: StateFlow<Int> = _completedGoalsCount

    init {
        observeMonthlyGoals()
    }

    private fun observeMonthlyGoals() {
        viewModelScope.launch {
            goalDao.getGoalsForUser(userId).collect { goals ->
                val startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay()
                val monthlyCount = goals.count { goal ->
                    goal.isCompleted && runCatching {
                        LocalDateTime.parse(goal.createdAt) >= startOfMonth
                    }.getOrDefault(false)
                }
                _completedGoalsCount.value = monthlyCount
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            settingsDao.insertOrUpdateSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            settingsDao.insertOrUpdateSettings(current.copy(theme = if (isDark) "dark" else "system"))
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