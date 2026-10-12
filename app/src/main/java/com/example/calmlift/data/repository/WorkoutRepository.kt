package com.example.calmlift.data.repository

import com.example.calmlift.data.model.WorkoutSession
import com.example.calmlift.data.model.WorkoutTemplate
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WorkoutRepository {
    fun getTemplates(): List<WorkoutTemplate>
    fun getTemplateById(id: String): WorkoutTemplate
    suspend fun saveSession(session: WorkoutSession)
    fun getSessionHistory(): Flow<List<WorkoutSession>>
}

class InMemoryWorkoutRepository : WorkoutRepository {

    private val sessionHistoryFlow = MutableStateFlow<List<WorkoutSession>>(
        listOf(
            // Seed a realistic past session for instant feedback
            WorkoutSession(
                id = UUID.randomUUID().toString(),
                templateId = "routine_upper_push",
                templateTitle = "Upper Push Script",
                startTime = Instant.now().minus(2, java.time.temporal.ChronoUnit.DAYS),
                endTime = Instant.now().minus(2, java.time.temporal.ChronoUnit.DAYS).plus(28, java.time.temporal.ChronoUnit.MINUTES),
                exercisesCompleted = 4,
                totalExercisesInRoutine = 4,
                estimatedCaloriesBurned = 145.0,
                isGuiltFreeExit = false
            ),
            WorkoutSession(
                id = UUID.randomUUID().toString(),
                templateId = "routine_lower_gentle",
                templateTitle = "Lower Body Machine Safe",
                startTime = Instant.now().minus(4, java.time.temporal.ChronoUnit.DAYS),
                endTime = Instant.now().minus(4, java.time.temporal.ChronoUnit.DAYS).plus(20, java.time.temporal.ChronoUnit.MINUTES),
                exercisesCompleted = 2,
                totalExercisesInRoutine = 4,
                estimatedCaloriesBurned = 98.0,
                isGuiltFreeExit = true // 2 out of 4 is still celebrated!
            )
        )
    )

    override fun getTemplates(): List<WorkoutTemplate> = PrebuiltWorkouts.routines

    override fun getTemplateById(id: String): WorkoutTemplate = PrebuiltWorkouts.getById(id)

    override suspend fun saveSession(session: WorkoutSession) {
        val currentList = sessionHistoryFlow.value.toMutableList()
        currentList.add(0, session)
        sessionHistoryFlow.value = currentList
    }

    override fun getSessionHistory(): Flow<List<WorkoutSession>> = sessionHistoryFlow.asStateFlow()
}
