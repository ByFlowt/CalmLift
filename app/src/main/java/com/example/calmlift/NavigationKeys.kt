package com.example.calmlift

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavKey : NavKey

@Serializable
data class WorkoutNavKey(val templateId: String) : NavKey
