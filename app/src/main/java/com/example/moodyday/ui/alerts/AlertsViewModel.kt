package com.example.moodyday.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodyday.data.local.AppDatabase
import com.example.moodyday.data.local.entities.AlertRuleEntity
import com.example.moodyday.data.repository.AlertRuleRepository
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
    private val repository = AlertRuleRepository(AppDatabase.getDatabase(application).alertRuleDao())
    
    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState

    private val userId = "chongwc" // Use the same test user ID as AppNavGraph

    init {
        loadRules()
        generateActiveAlerts()
    }

    private fun loadRules() {
        viewModelScope.launch {
            repository.getRules(userId).collectLatest { rules ->
                _uiState.value = _uiState.value.copy(customRules = rules)
            }
        }
    }

    private fun generateActiveAlerts() {
        // Mocking some alerts for demonstration as per UI design
        _uiState.value = _uiState.value.copy(
            activeAlerts = listOf(
                ActiveAlert(
                    title = "Extreme Heat Warning",
                    description = "Temperatures expected to exceed 40°C. Prolonged exposure can cause heat stroke. Stay indoors and stay hydrated.",
                    severity = "HIGH SEVERITY",
                    endsIn = "Ends in 4h"
                ),
                ActiveAlert(
                    title = "Poor Air Quality",
                    description = "AQI is currently 152 (Unhealthy). Sensitive groups should outdoor exertion.",
                    severity = "MODERATE",
                    endsIn = "Ends tomorrow"
                )
            )
        )
    }

    fun addRule(condition: String, threshold: Double) {
        viewModelScope.launch {
            val newRule = AlertRuleEntity(
                userId = userId,
                cityId = 1, // Mock city ID
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