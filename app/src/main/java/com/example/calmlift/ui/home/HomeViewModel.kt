package com.example.calmlift.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmlift.data.healthconnect.HealthConnectAvailability
import com.example.calmlift.data.healthconnect.HealthConnectManager
import com.example.calmlift.data.healthconnect.HealthConnectRepository
import com.example.calmlift.data.healthconnect.RecoveryMetrics
import com.example.calmlift.data.model.MindsetQuote
import com.example.calmlift.data.model.MindsetQuotesDataSource
import com.example.calmlift.data.model.WorkoutSession
import com.example.calmlift.data.model.WorkoutTemplate
import com.example.calmlift.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val quote: MindsetQuote = MindsetQuotesDataSource.quotes.first(),
    val isQuotePinned: Boolean = true,
    val recoveryMetrics: RecoveryMetrics? = null,
    val templates: List<WorkoutTemplate> = emptyList(),
    val recentSessions: List<WorkoutSession> = emptyList(),
    val healthConnectAvailability: HealthConnectAvailability = HealthConnectAvailability.INSTALLED,
    val hasHealthPermissions: Boolean = false
)

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            templates = workoutRepository.getTemplates()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            // Observe session history
            workoutRepository.getSessionHistory().collect { sessions ->
                _uiState.update { it.copy(recentSessions = sessions) }
            }
        }

        viewModelScope.launch {
            val availability = healthConnectManager.checkAvailability()
            val hasPerms = healthConnectManager.hasAllPermissions()
            _uiState.update {
                it.copy(
                    healthConnectAvailability = availability,
                    hasHealthPermissions = hasPerms
                )
            }

            val recoveryResult = healthConnectRepository.getRecoveryMetrics()
            if (recoveryResult.isSuccess) {
                _uiState.update { it.copy(recoveryMetrics = recoveryResult.getOrNull()) }
            }
        }
    }

    fun refreshQuote() {
        _uiState.update { it.copy(quote = MindsetQuotesDataSource.getRandomQuote()) }
    }

    fun toggleQuotePinned() {
        _uiState.update { it.copy(isQuotePinned = !it.isQuotePinned) }
    }
}
