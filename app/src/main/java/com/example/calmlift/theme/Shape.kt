package com.example.calmlift.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Shape tokens.
 * Characterized by hyper-rounded, comfortable tactile curves that reduce visual sharpness
 * and feel calm and inviting.
 */
val CalmLiftShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

object ExpressiveShapes {
    val ButtonShape: CornerBasedShape = RoundedCornerShape(32.dp)
    val CardShape: CornerBasedShape = RoundedCornerShape(28.dp)
    val PillShape: CornerBasedShape = RoundedCornerShape(percent = 50)
    val SheetShape: CornerBasedShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val StepperShape: CornerBasedShape = RoundedCornerShape(20.dp)
}
