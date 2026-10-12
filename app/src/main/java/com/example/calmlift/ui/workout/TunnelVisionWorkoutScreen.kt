package com.example.calmlift.ui.workout

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calmlift.data.model.Exercise
import com.example.calmlift.data.model.UnilateralSide
import com.example.calmlift.theme.ExpressiveShapes
import com.example.calmlift.theme.SageGreenOnPrimary
import com.example.calmlift.theme.SageGreenPrimary
import com.example.calmlift.theme.SymmetryBlueTertiary
import com.example.calmlift.theme.WarmAmberSecondary
import com.example.calmlift.ui.workout.components.ExerciseSwapSheet
import com.example.calmlift.ui.workout.components.GuiltFreeExitDialog
import com.example.calmlift.ui.workout.components.RestTimerSection
import com.example.calmlift.ui.workout.components.TactileStepper
import com.example.calmlift.ui.workout.components.TempoVisualizer

/**
 * Tunnel-Vision Workout Player.
 * Displays strictly ONE exercise at a time with large tactile buttons.
 * Removes all social metrics, complex analytics, or overwhelming lists.
 */
@Composable
fun TunnelVisionWorkoutScreen(
    viewModel: TunnelVisionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Observe side effects (e.g. vibration on rest timer end)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                TunnelVisionUiEffect.TriggerRestVibration -> {
                    try {
                        val vibrator = context.getSystemService(Vibrator::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val pattern = longArrayOf(0, 250, 150, 250)
                            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(400)
                        }
                    } catch (_: Exception) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                TunnelVisionUiEffect.NavigateHome -> onNavigateBack()
            }
        }
    }

    // Modal BottomSheet for "Machine Occupied" 1-tap swap
    if (state.isSwapSheetOpen) {
        ExerciseSwapSheet(
            currentExerciseName = state.currentExercise.name,
            alternatives = state.currentExercise.alternatives,
            onSelectAlternative = { alt ->
                viewModel.onEvent(TunnelVisionUiEvent.SelectSwapAlternative(alt))
            },
            onDismiss = {
                viewModel.onEvent(TunnelVisionUiEvent.DismissSwapSheet)
            }
        )
    }

    // Guilt-Free Exit Confirmation Dialog
    if (state.isGuiltFreeExitDialogOpen) {
        GuiltFreeExitDialog(
            completedExercises = state.completedExercisesLogs.size,
            totalExercises = state.totalExercises,
            onConfirmExit = {
                viewModel.onEvent(TunnelVisionUiEvent.ConfirmGuiltFreeExit)
            },
            onDismiss = {
                viewModel.onEvent(TunnelVisionUiEvent.DismissGuiltFreeExit)
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (state.isWorkoutFinished) {
            WorkoutCompletionView(
                state = state,
                onHomeClicked = onNavigateBack
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Top Minimal Header: Exercise Progress Dots & Guilt-Free Finish Button
                WorkoutTopBar(
                    currentExerciseIndex = state.currentExerciseIndex,
                    totalExercises = state.totalExercises,
                    onFinishEarlyClick = {
                        viewModel.onEvent(TunnelVisionUiEvent.OpenGuiltFreeExit)
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Rest Timer Section (Animated expansion when rest is active)
                AnimatedVisibility(
                    visible = state.isRestTimerActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        RestTimerSection(
                            secondsRemaining = state.restSecondsRemaining,
                            totalSeconds = state.restTotalSeconds,
                            onAddThirtySeconds = {
                                viewModel.onEvent(TunnelVisionUiEvent.AddThirtySecondsRest)
                            },
                            onSkipRest = {
                                viewModel.onEvent(TunnelVisionUiEvent.SkipRest)
                            }
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }

                // THE Tunnel-Vision Single Exercise View
                AnimatedContent(
                    targetState = state.currentExerciseIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "ExerciseTransition"
                ) { targetIndex ->
                    val exercise = state.template.exercises[targetIndex]
                    ExerciseFocusCard(
                        exercise = exercise,
                        state = state,
                        onSwapClick = { viewModel.onEvent(TunnelVisionUiEvent.OpenSwapSheet) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form Rhythm Guide (Tempo Visualizer)
                TempoVisualizer(tempo = state.currentExercise.tempo)

                Spacer(modifier = Modifier.height(16.dp))

                // Tactile Logging Steppers
                TactileStepper(
                    label = "Weight",
                    valueDisplay = "${state.inputWeightKg}",
                    unit = "kg",
                    onDecrement = { viewModel.onEvent(TunnelVisionUiEvent.DecrementWeight) },
                    onIncrement = { viewModel.onEvent(TunnelVisionUiEvent.IncrementWeight) },
                    isLockedOrCapped = state.isDominantSide && state.nonDominantLoggedSet != null,
                    cappedExplanation = if (state.isDominantSide) {
                        "Capped to match Left arm (${state.nonDominantLoggedSet?.weightKg} kg)"
                    } else null
                )

                Spacer(modifier = Modifier.height(12.dp))

                TactileStepper(
                    label = "Reps Completed",
                    valueDisplay = "${state.inputReps}",
                    unit = "reps",
                    onDecrement = { viewModel.onEvent(TunnelVisionUiEvent.DecrementReps) },
                    onIncrement = { viewModel.onEvent(TunnelVisionUiEvent.IncrementReps) },
                    isLockedOrCapped = state.isDominantSide && state.nonDominantLoggedSet != null,
                    cappedExplanation = if (state.isDominantSide) {
                        "Capped to match Left arm (${state.nonDominantLoggedSet?.reps} reps)"
                    } else null
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Big Primary Log Button
                LogSetPrimaryButton(
                    state = state,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(TunnelVisionUiEvent.LogSet)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun WorkoutTopBar(
    currentExerciseIndex: Int,
    totalExercises: Int,
    onFinishEarlyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Exercise Progress Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0 until totalExercises) {
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(if (i == currentExerciseIndex) 28.dp else 10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                i < currentExerciseIndex -> SageGreenPrimary
                                i == currentExerciseIndex -> SageGreenPrimary
                                else -> MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        )
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${currentExerciseIndex + 1} of $totalExercises",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Guilt-Free Exit Button
        Surface(
            modifier = Modifier
                .clip(ExpressiveShapes.PillShape)
                .clickable(onClick = onFinishEarlyClick),
            shape = ExpressiveShapes.PillShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
        ) {
            Text(
                text = "Finish & Save",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ExerciseFocusCard(
    exercise: Exercise,
    state: TunnelVisionUiState,
    onSwapClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.CardShape),
        shape = ExpressiveShapes.CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = exercise.equipment.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 1-Tap "Machine Occupied" Swap Button
                Surface(
                    modifier = Modifier
                        .clip(ExpressiveShapes.PillShape)
                        .clickable(onClick = onSwapClick),
                    shape = ExpressiveShapes.PillShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Machine",
                            tint = WarmAmberSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Machine Occupied?",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = WarmAmberSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Exercise Title
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Target Sets & Reps
            Text(
                text = "Target: ${exercise.targetSets} sets • ${exercise.targetReps}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = SageGreenPrimary
            )

            // Form Cue
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = exercise.formCue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Unilateral Status Banner (if unilateral)
            if (exercise.isUnilateral) {
                Spacer(modifier = Modifier.height(12.dp))
                UnilateralSideIndicator(
                    currentSide = state.currentSide,
                    autoBalanceNotice = state.autoBalanceNotice
                )
            }
        }
    }
}

@Composable
private fun UnilateralSideIndicator(
    currentSide: UnilateralSide,
    autoBalanceNotice: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SymmetryBlueTertiary.copy(alpha = 0.12f))
            .border(1.dp, SymmetryBlueTertiary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SymmetryBlueTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AUTO-BALANCE ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = SymmetryBlueTertiary
                )
            }

            // Current Arm Pill
            Surface(
                shape = ExpressiveShapes.PillShape,
                color = if (currentSide == UnilateralSide.LEFT) SageGreenPrimary else SymmetryBlueTertiary
            ) {
                Text(
                    text = if (currentSide == UnilateralSide.LEFT) "LEFT (NON-DOMINANT)" else "RIGHT (DOMINANT)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = if (currentSide == UnilateralSide.LEFT) SageGreenOnPrimary else Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        if (autoBalanceNotice != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = autoBalanceNotice,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LogSetPrimaryButton(
    state: TunnelVisionUiState,
    onClick: () -> Unit
) {
    val buttonText = buildString {
        append("LOG SET ${state.currentSetNumber} OF ${state.currentExercise.targetSets}")
        if (state.isCurrentUnilateral) {
            if (state.currentSide == UnilateralSide.LEFT) {
                append(" (LEFT ARM)")
            } else {
                append(" (RIGHT ARM • BALANCED)")
            }
        }
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = ExpressiveShapes.ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = SageGreenPrimary,
            contentColor = SageGreenOnPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
private fun WorkoutCompletionView(
    state: TunnelVisionUiState,
    onHomeClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SageGreenPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = SageGreenPrimary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Routine Complete",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You showed up, ran the script, and preserved mental energy.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Health Connect Status Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ExpressiveShapes.CardShape),
            shape = ExpressiveShapes.CardShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = SageGreenPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Android Health Connect",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (state.isHealthConnectSyncing) "Syncing exercise session..."
                        else if (state.healthConnectSyncSuccess) "Session & calories recorded successfully ✓"
                        else "Saved locally (Offline first)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.healthConnectSyncSuccess) SageGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onHomeClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = ExpressiveShapes.ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = SageGreenPrimary,
                contentColor = SageGreenOnPrimary
            )
        ) {
            Text(
                text = "Back to Sanctuary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
