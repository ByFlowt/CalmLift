package com.example.calmlift.ui.workout

import com.example.calmlift.data.model.CompletedExerciseProgress
import com.example.calmlift.data.model.Exercise
import com.example.calmlift.data.model.ExerciseAlternative
import com.example.calmlift.data.model.ExerciseSetLog
import com.example.calmlift.data.model.UnilateralSide
import com.example.calmlift.data.model.WorkoutTemplate

data class TunnelVisionUiState(
    val template: WorkoutTemplate,
    val currentExerciseIndex: Int = 0,
    val currentSetNumber: Int = 1,
    val inputWeightKg: Double = 0.0,
    val inputReps: Int = 10,
    val currentSide: UnilateralSide = UnilateralSide.NONE,
    val nonDominantLoggedSet: ExerciseSetLog? = null,
    val autoBalanceNotice: String? = null,
    val isRestTimerActive: Boolean = false,
    val restSecondsRemaining: Int = 0,
    val restTotalSeconds: Int = 60,
    val isSwapSheetOpen: Boolean = false,
    val isGuiltFreeExitDialogOpen: Boolean = false,
    val isWorkoutFinished: Boolean = false,
    val completedExercisesLogs: List<CompletedExerciseProgress> = emptyList(),
    val isHealthConnectSyncing: Boolean = false,
    val healthConnectSyncSuccess: Boolean = false
) {
    val currentExercise: Exercise
        get() = template.exercises.getOrNull(currentExerciseIndex) ?: template.exercises.first()

    val totalExercises: Int
        get() = template.exercises.size

    val isCurrentUnilateral: Boolean
        get() = currentExercise.isUnilateral

    val isDominantSide: Boolean
        get() = isCurrentUnilateral && currentSide == UnilateralSide.RIGHT
}

sealed interface TunnelVisionUiEvent {
    data object IncrementWeight : TunnelVisionUiEvent
    data object DecrementWeight : TunnelVisionUiEvent
    data object IncrementReps : TunnelVisionUiEvent
    data object DecrementReps : TunnelVisionUiEvent
    data object LogSet : TunnelVisionUiEvent
    data object SkipRest : TunnelVisionUiEvent
    data object AddThirtySecondsRest : TunnelVisionUiEvent
    data object OpenSwapSheet : TunnelVisionUiEvent
    data object DismissSwapSheet : TunnelVisionUiEvent
    data class SelectSwapAlternative(val alternative: ExerciseAlternative) : TunnelVisionUiEvent
    data object OpenGuiltFreeExit : TunnelVisionUiEvent
    data object DismissGuiltFreeExit : TunnelVisionUiEvent
    data object ConfirmGuiltFreeExit : TunnelVisionUiEvent
}

sealed interface TunnelVisionUiEffect {
    data object TriggerRestVibration : TunnelVisionUiEffect
    data object NavigateHome : TunnelVisionUiEffect
}
