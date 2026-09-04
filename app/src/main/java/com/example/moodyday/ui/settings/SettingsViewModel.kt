package com.example.moodyday.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.local.dao.UserSettingsDao
import com.example.moodyday.data.local.entities.UserSettingsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDao: UserSettingsDao,
    private val userId: String = "chongwc"
) : ViewModel() {

    val settingsState: StateFlow<UserSettingsEntity> = settingsDao.getUserSettings(userId)
        .map { it ?: UserSettingsEntity(userId = userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity(userId = userId))

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            settingsDao.insertOrUpdateSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            settingsDao.deleteSettings(userId)
        }
    }
}