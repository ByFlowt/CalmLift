package com.example.calmlift.ui.workout.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmlift.data.model.LiftingTempo
import com.example.calmlift.theme.ExpressiveShapes
import com.example.calmlift.theme.SageGreenPrimary
import com.example.calmlift.theme.WarmAmberSecondary

/**
 * Expressive Tempo Visualizer.
 * Inexperienced or anxious gym-goers tend to rush repetitions due to adrenaline.
 * This rhythmic breathing orb guides the 2-second eccentric lowering phase,
 * 1-second pause, and 1-second concentric push phase.
 */
@Composable
fun TempoVisualizer(
    tempo: LiftingTempo,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TempoPulse")

    // A full 4-second cycle: 0-2000ms (lower), 2000-3000ms (pause), 3000-4000ms (push)
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1.0f at 0 using FastOutSlowInEasing // start
                0.78f at 2000 using LinearEasing    // 2s slow descent (eccentric)
                0.78f at 3000 using LinearEasing    // 1s pause
                1.25f at 4000 using FastOutSlowInEasing // 1s explosive/smooth concentric
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbScale"
    )

    // Current phase text based on cyclic time
    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0
                2f at 2000
                3f at 3000
                4f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "CycleProgress"
    )

    val (phaseText, phaseColor) = when {
        cycleProgress < 2.0f -> Pair("LOWER DOWN (2s)", SageGreenPrimary)
        cycleProgress < 3.0f -> Pair("PAUSE & BREATHE", Color(0xFF93C5FD))
        else -> Pair("DRIVE UP (1s)", WarmAmberSecondary)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.CardShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = ExpressiveShapes.CardShape
            )
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Pulsing Orb
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scaleAnim)
                            .clip(CircleShape)
                            .background(phaseColor.copy(alpha = 0.28f))
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(phaseColor)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "FORM RHYTHM GUIDE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = phaseText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = phaseColor
                    )
                }
            }

            // Quick cadence pills (2 - 1 - 1)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TempoTag(number = "${tempo.eccentricSeconds}s", label = "Down")
                Text("•", color = MaterialTheme.colorScheme.outline)
                TempoTag(number = "${tempo.pauseSeconds}s", label = "Hold")
                Text("•", color = MaterialTheme.colorScheme.outline)
                TempoTag(number = "${tempo.concentricSeconds}s", label = "Up")
            }
        }
    }
}

@Composable
private fun TempoTag(number: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
