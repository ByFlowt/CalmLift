package com.example.calmlift.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmlift.data.healthconnect.HealthConnectRepository
import com.example.calmlift.data.model.CompletedExerciseProgress
import com.example.calmlift.data.model.Exercise
import com.example.calmlift.data.model.ExerciseAlternative
import com.example.calmlift.data.model.ExerciseSetLog
import com.example.calmlift.data.model.UnilateralSide
import com.example.calmlift.data.model.WorkoutSession
import com.example.calmlift.data.model.WorkoutTemplate
import com.example.calmlift.data.repository.WorkoutRepository
import com.example.calmlift.domain.usecase.AutoBalanceEngine
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TunnelVisionViewModel(
    initialTemplate: WorkoutTemplate,
    private val workoutRepository: WorkoutRepository,
    private val healthConnectRepository: HealthConnectRepository
) : ViewModel() {

    private val sessionStartTime: Instant = Instant.now()

    private val _uiState = MutableStateFlow(
        TunnelVisionUiState(
            template = initialTemplate,
            currentExerciseIndex = 0,
            currentSetNumber = 1,
            inputWeightKg = initialTemplate.exercises.firstOrNull()?.defaultWeightKg ?: 20.0,
            inputReps = 10,
            currentSide = if (initialTemplate.exercises.firstOrNull()?.isUnilateral == true) UnilateralSide.LEFT else UnilateralSide.NONE
        )
    )
    val uiState: StateFlow<TunnelVisionUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<TunnelVisionUiEffect>()
    val effects: SharedFlow<TunnelVisionUiEffect> = _effects.asSharedFlow()

    private var timerJob: Job? = null

    fun onEvent(event: TunnelVisionUiEvent) {
        when (event) {
            TunnelVisionUiEvent.IncrementWeight -> {
                _uiState.update { it.copy(inputWeightKg = it.inputWeightKg + 2.5) }
            }
            TunnelVisionUiEvent.DecrementWeight -> {
                _uiState.update { it.copy(inputWeightKg = (it.inputWeightKg - 2.5).coerceAtLeast(0.0)) }
            }
            TunnelVisionUiEvent.IncrementReps -> {
                _uiState.update { it.copy(inputReps = it.inputReps + 1) }
            }
            TunnelVisionUiEvent.DecrementReps -> {
                _uiState.update { it.copy(inputReps = (it.inputReps - 1).coerceAtLeast(1)) }
            }
            TunnelVisionUiEvent.LogSet -> logCurrentSet()
            TunnelVisionUiEvent.SkipRest -> cancelRestTimer()
            TunnelVisionUiEvent.AddThirtySecondsRest -> addRestSeconds(30)
            TunnelVisionUiEvent.OpenSwapSheet -> _uiState.update { it.copy(isSwapSheetOpen = true) }
            TunnelVisionUiEvent.DismissSwapSheet -> _uiState.update { it.copy(isSwapSheetOpen = false) }
            is TunnelVisionUiEvent.SelectSwapAlternative -> swapCurrentExercise(event.alternative)
            TunnelVisionUiEvent.OpenGuiltFreeExit -> _uiState.update { it.copy(isGuiltFreeExitDialogOpen = true) }
            TunnelVisionUiEvent.DismissGuiltFreeExit -> _uiState.update { it.copy(isGuiltFreeExitDialogOpen = false) }
            TunnelVisionUiEvent.ConfirmGuiltFreeExit -> finishSession(isGuiltFree = true)
        }
    }

    private fun logCurrentSet() {
        val state = _uiState.value
        val currentEx = state.currentExercise

        if (state.isCurrentUnilateral) {
            if (state.currentSide == UnilateralSide.LEFT) {
                // Log non-dominant side (Left)
                val loggedLeft = ExerciseSetLog(
                    setNumber = state.currentSetNumber,
                    weightKg = state.inputWeightKg,
                    reps = state.inputReps,
                    side = UnilateralSide.LEFT
                )
                // Switch to Right side, auto-populate capped target values
                _uiState.update {
                    it.copy(
                        currentSide = UnilateralSide.RIGHT,
                        nonDominantLoggedSet = loggedLeft,
                        autoBalanceNotice = "Auto-Balanced: Capped to ${loggedLeft.reps} reps @ ${loggedLeft.weightKg}kg to protect symmetry."
                    )
                }
                return
            } else {
                // Logging dominant side (Right) -> Auto-balance check
                val balanceResult = AutoBalanceEngine.balanceDominantSet(
                    dominantInputWeight = state.inputWeightKg,
                    dominantInputReps = state.inputReps,
                    nonDominantLoggedSet = state.nonDominantLoggedSet
                )

                val loggedRight = ExerciseSetLog(
                    setNumber = state.currentSetNumber,
                    weightKg = balanceResult.finalWeightKg,
                    reps = balanceResult.finalReps,
                    side = UnilateralSide.RIGHT,
                    autoBalanced = balanceResult.wasCapped
                )

                advanceSetOrExercise(listOfNotNull(state.nonDominantLoggedSet, loggedRight))
            }
        } else {
            // Bilateral exercise
            val loggedSet = ExerciseSetLog(
                setNumber = state.currentSetNumber,
                weightKg = state.inputWeightKg,
                reps = state.inputReps
            )
            advanceSetOrExercise(listOf(loggedSet))
        }
    }

    private fun advanceSetOrExercise(newLoggedSets: List<ExerciseSetLog>) {
        val state = _uiState.value
        val currentEx = state.currentExercise

        // Append to logs
        val updatedLogs = state.completedExercisesLogs.toMutableList()
        val existingIndex = updatedLogs.indexOfFirst { it.exerciseId == currentEx.id }
        if (existingIndex >= 0) {
            val existing = updatedLogs[existingIndex]
            updatedLogs[existingIndex] = existing.copy(setsLogged = existing.setsLogged + newLoggedSets)
        } else {
            updatedLogs.add(
                CompletedExerciseProgress(
                    exerciseId = currentEx.id,
                    exerciseName = currentEx.name,
                    setsLogged = newLoggedSets
                )
            )
        }

        if (state.currentSetNumber < currentEx.targetSets) {
            // Next set in same exercise
            _uiState.update {
                it.copy(
                    currentSetNumber = it.currentSetNumber + 1,
                    currentSide = if (currentEx.isUnilateral) UnilateralSide.LEFT else UnilateralSide.NONE,
                    nonDominantLoggedSet = null,
                    autoBalanceNotice = null,
                    completedExercisesLogs = updatedLogs
                )
            }
            startRestTimer(60)
        } else {
            // Exercise completed! Advance to next exercise
            val nextIndex = state.currentExerciseIndex + 1
            if (nextIndex < state.totalExercises) {
                val nextEx = state.template.exercises[nextIndex]
                _uiState.update {
                    it.copy(
                        currentExerciseIndex = nextIndex,
                        currentSetNumber = 1,
                        inputWeightKg = nextEx.defaultWeightKg,
                        inputReps = 10,
                        currentSide = if (nextEx.isUnilateral) UnilateralSide.LEFT else UnilateralSide.NONE,
                        nonDominantLoggedSet = null,
                        autoBalanceNotice = null,
                        completedExercisesLogs = updatedLogs
                    )
                }
                startRestTimer(90)
            } else {
                // Whole routine finished!
                _uiState.update { it.copy(completedExercisesLogs = updatedLogs) }
                finishSession(isGuiltFree = false)
            }
        }
    }

    private fun swapCurrentExercise(alternative: ExerciseAlternative) {
        val state = _uiState.value
        val currentEx = state.currentExercise

        // Create replacement exercise keeping targets intact
        val replacement = currentEx.copy(
            id = alternative.id,
            name = alternative.name,
            equipment = alternative.machineType,
            formCue = "Substituted: ${alternative.whyItWorks}",
            alternatives = currentEx.alternatives.filter { it.id != alternative.id }
        )

        val updatedExercises = state.template.exercises.toMutableList()
        updatedExercises[state.currentExerciseIndex] = replacement

        val updatedTemplate = state.template.copy(exercises = updatedExercises)

        _uiState.update {
            it.copy(
                template = updatedTemplate,
                isSwapSheetOpen = false
            )
        }
    }

    private fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isRestTimerActive = true,
                restSecondsRemaining = seconds,
                restTotalSeconds = seconds
            )
        }

        timerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _uiState.update { it.copy(restSecondsRemaining = remaining) }
            }
            // Timer finished
            _effects.emit(TunnelVisionUiEffect.TriggerRestVibration)
            _uiState.update { it.copy(isRestTimerActive = false) }
        }
    }

    private fun addRestSeconds(extraSeconds: Int) {
        val currentRemaining = _uiState.value.restSecondsRemaining
        val currentTotal = _uiState.value.restTotalSeconds
        _uiState.update {
            it.copy(
                restSecondsRemaining = currentRemaining + extraSeconds,
                restTotalSeconds = currentTotal + extraSeconds
            )
        }
    }

    private fun cancelRestTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRestTimerActive = false) }
    }

    private fun finishSession(isGuiltFree: Boolean) {
        val state = _uiState.value
        timerJob?.cancel()

        val completedExercisesCount = state.completedExercisesLogs.size
        val durationMins = java.time.Duration.between(sessionStartTime, Instant.now()).toMinutes().coerceAtLeast(1)
        val estimatedKcal = (durationMins * 5.2).coerceAtLeast(35.0)

        val session = WorkoutSession(
            id = UUID.randomUUID().toString(),
            templateId = state.template.id,
            templateTitle = state.template.title,
            startTime = sessionStartTime,
            endTime = Instant.now(),
            exercisesCompleted = completedExercisesCount,
            totalExercisesInRoutine = state.totalExercises,
            logs = state.completedExercisesLogs,
            estimatedCaloriesBurned = estimatedKcal,
            isGuiltFreeExit = isGuiltFree
        )

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isWorkoutFinished = true,
                    isGuiltFreeExitDialogOpen = false,
                    isHealthConnectSyncing = true
                )
            }

            // Save to local repository
            workoutRepository.saveSession(session)

            // Write to Health Connect
            val healthResult = healthConnectRepository.writeWorkoutSession(session)
            _uiState.update {
                it.copy(
                    isHealthConnectSyncing = false,
                    healthConnectSyncSuccess = healthResult.isSuccess
                )
            }
        }
    }
}
