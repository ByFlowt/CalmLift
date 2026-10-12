package com.example.calmlift

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.calmlift.data.healthconnect.DefaultHealthConnectRepository
import com.example.calmlift.data.healthconnect.HealthConnectManager
import com.example.calmlift.data.repository.InMemoryWorkoutRepository
import com.example.calmlift.ui.home.HomeScreen
import com.example.calmlift.ui.home.HomeViewModel
import com.example.calmlift.ui.workout.TunnelVisionViewModel
import com.example.calmlift.ui.workout.TunnelVisionWorkoutScreen

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(HomeNavKey)

    // Repositories
    val healthConnectManager = remember { HealthConnectManager(context) }
    val healthConnectRepository = remember { DefaultHealthConnectRepository(healthConnectManager) }
    val workoutRepository = remember { InMemoryWorkoutRepository() }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeNavKey> {
                val homeViewModel = viewModel {
                    HomeViewModel(workoutRepository, healthConnectRepository, healthConnectManager)
                }
                HomeScreen(
                    viewModel = homeViewModel,
                    onStartWorkout = { template ->
                        backStack.add(WorkoutNavKey(template.id))
                    }
                )
            }
            entry<WorkoutNavKey> { key ->
                val template = workoutRepository.getTemplateById(key.templateId)
                val workoutViewModel = viewModel(key = key.templateId) {
                    TunnelVisionViewModel(template, workoutRepository, healthConnectRepository)
                }
                TunnelVisionWorkoutScreen(
                    viewModel = workoutViewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}
