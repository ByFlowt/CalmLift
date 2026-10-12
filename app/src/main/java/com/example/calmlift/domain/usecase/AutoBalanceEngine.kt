package com.example.calmlift.domain.usecase

import com.example.calmlift.data.model.ExerciseSetLog
import com.example.calmlift.data.model.UnilateralSide

data class AutoBalanceResult(
    val finalWeightKg: Double,
    val finalReps: Int,
    val wasCapped: Boolean,
    val explanationMessage: String?
)

/**
 * Domain engine that guarantees muscle symmetry by eliminating dominant-side overtraining.
 * When performing unilateral exercises (e.g. unilateral dumbbell curls or split squats),
 * the user always logs the non-dominant side (Left) first.
 * The dominant side (Right) is capped strictly to the non-dominant side's achievements.
 */
object AutoBalanceEngine {

    fun balanceDominantSet(
        dominantInputWeight: Double,
        dominantInputReps: Int,
        nonDominantLoggedSet: ExerciseSetLog?
    ): AutoBalanceResult {
        if (nonDominantLoggedSet == null) {
            return AutoBalanceResult(
                finalWeightKg = dominantInputWeight,
                finalReps = dominantInputReps,
                wasCapped = false,
                explanationMessage = null
            )
        }

        var wasCapped = false
        var cappedWeight = dominantInputWeight
        var cappedReps = dominantInputReps

        // Check weight cap
        if (dominantInputWeight > nonDominantLoggedSet.weightKg) {
            cappedWeight = nonDominantLoggedSet.weightKg
            wasCapped = true
        }

        // Check reps cap
        if (dominantInputReps > nonDominantLoggedSet.reps) {
            cappedReps = nonDominantLoggedSet.reps
            wasCapped = true
        }

        val message = if (wasCapped) {
            "Auto-Balanced to ${cappedReps} reps @ ${cappedWeight}kg. Dominant arm capped to non-dominant side for perfect symmetry."
        } else {
            "Matched non-dominant side. Symmetrical load achieved!"
        }

        return AutoBalanceResult(
            finalWeightKg = cappedWeight,
            finalReps = cappedReps,
            wasCapped = wasCapped,
            explanationMessage = message
        )
    }
}
