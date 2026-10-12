package com.example.calmlift.domain

import com.example.calmlift.data.model.ExerciseSetLog
import com.example.calmlift.data.model.UnilateralSide
import com.example.calmlift.domain.usecase.AutoBalanceEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoBalanceEngineTest {

    @Test
    fun `balanceDominantSet caps weight and reps when dominant side exceeds non-dominant side`() {
        // User logged 8 reps @ 12.0kg on non-dominant (Left) arm
        val leftLoggedSet = ExerciseSetLog(
            setNumber = 1,
            weightKg = 12.0,
            reps = 8,
            side = UnilateralSide.LEFT
        )

        // User attempts to log 10 reps @ 14.0kg on dominant (Right) arm
        val result = AutoBalanceEngine.balanceDominantSet(
            dominantInputWeight = 14.0,
            dominantInputReps = 10,
            nonDominantLoggedSet = leftLoggedSet
        )

        assertTrue(result.wasCapped)
        assertEquals(12.0, result.finalWeightKg, 0.01)
        assertEquals(8, result.finalReps)
        assertTrue(result.explanationMessage?.contains("Auto-Balanced") == true)
    }

    @Test
    fun `balanceDominantSet preserves values when dominant side is within non-dominant bounds`() {
        val leftLoggedSet = ExerciseSetLog(
            setNumber = 1,
            weightKg = 12.0,
            reps = 10,
            side = UnilateralSide.LEFT
        )

        val result = AutoBalanceEngine.balanceDominantSet(
            dominantInputWeight = 12.0,
            dominantInputReps = 10,
            nonDominantLoggedSet = leftLoggedSet
        )

        assertFalse(result.wasCapped)
        assertEquals(12.0, result.finalWeightKg, 0.01)
        assertEquals(10, result.finalReps)
    }
}
