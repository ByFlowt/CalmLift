package com.example.calmlift.data.model

import java.time.Instant

/**
 * Cadence tempo for mindful, anxiety-free execution.
 * 2-second eccentric (lowering), 1-second pause at bottom, 1-second concentric (push/pull).
 */
data class LiftingTempo(
    val eccentricSeconds: Int = 2,
    val pauseSeconds: Int = 1,
    val concentricSeconds: Int = 1,
    val cueText: String = "Control down (2s) • Drive up (1s)"
)

enum class UnilateralSide {
    NONE,
    LEFT,   // Non-dominant first by default
    RIGHT   // Dominant second (auto-balanced to match Left)
}

data class ExerciseAlternative(
    val id: String,
    val name: String,
    val machineType: String,
    val whyItWorks: String
)

data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val targetSets: Int,
    val targetReps: String, // e.g. "8-10 reps"
    val defaultWeightKg: Double,
    val isUnilateral: Boolean = false,
    val primarySide: UnilateralSide = if (isUnilateral) UnilateralSide.LEFT else UnilateralSide.NONE,
    val tempo: LiftingTempo = LiftingTempo(),
    val formCue: String,
    val alternatives: List<ExerciseAlternative> = emptyList()
)

data class ExerciseSetLog(
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val side: UnilateralSide = UnilateralSide.NONE,
    val autoBalanced: Boolean = false,
    val timestamp: Instant = Instant.now()
)

data class CompletedExerciseProgress(
    val exerciseId: String,
    val exerciseName: String,
    val setsLogged: List<ExerciseSetLog> = emptyList()
)

data class WorkoutTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val estimatedMinutes: Int,
    val rationale: String,
    val exercises: List<Exercise> // Strictly capped at 3-4 exercises for zero overwhelm
)

data class WorkoutSession(
    val id: String,
    val templateId: String,
    val templateTitle: String,
    val startTime: Instant,
    val endTime: Instant? = null,
    val exercisesCompleted: Int,
    val totalExercisesInRoutine: Int,
    val logs: List<CompletedExerciseProgress> = emptyList(),
    val estimatedCaloriesBurned: Double = 0.0,
    val isGuiltFreeExit: Boolean = false
) {
    val durationMinutes: Long
        get() = if (endTime != null) {
            java.time.Duration.between(startTime, endTime).toMinutes().coerceAtLeast(1)
        } else 0
}
