package com.example.calmlift.data.repository

import com.example.calmlift.data.model.Exercise
import com.example.calmlift.data.model.ExerciseAlternative
import com.example.calmlift.data.model.LiftingTempo
import com.example.calmlift.data.model.UnilateralSide
import com.example.calmlift.data.model.WorkoutTemplate

object PrebuiltWorkouts {

    val routines = listOf(
        WorkoutTemplate(
            id = "routine_upper_push",
            title = "Upper Push Script",
            subtitle = "Machines only • Zero setup anxiety",
            estimatedMinutes = 32,
            rationale = "Fixed path of motion machines remove stabilizer anxiety and fear of dropping weights.",
            exercises = listOf(
                Exercise(
                    id = "ex_chest_press",
                    name = "Chest Press Machine",
                    muscleGroup = "Chest & Front Delts",
                    equipment = "Pin-Loaded Machine",
                    targetSets = 3,
                    targetReps = "8-10 reps",
                    defaultWeightKg = 35.0,
                    tempo = LiftingTempo(2, 1, 1, "Lower slowly for 2s • Smooth push"),
                    formCue = "Adjust seat so handles align with mid-chest. Plant feet flat.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_pec_deck", "Pec Deck Machine", "Machine", "No free weights, great chest isolation"),
                        ExerciseAlternative("alt_incline_press", "Incline Machine Press", "Machine", "Higher angle, identical pin setup")
                    )
                ),
                Exercise(
                    id = "ex_db_lateral",
                    name = "Unilateral DB Lateral Raise",
                    muscleGroup = "Side Deltoids",
                    equipment = "Dumbbells",
                    targetSets = 3,
                    targetReps = "12-15 reps",
                    defaultWeightKg = 6.0,
                    isUnilateral = true,
                    primarySide = UnilateralSide.LEFT,
                    tempo = LiftingTempo(2, 0, 1, "Raise smooth • 2s controlled descent"),
                    formCue = "Lead with your elbows. Slight lean forward removes neck tension.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_cable_lateral", "Cable Lateral Raise", "Cable Tower", "Constant smooth tension throughout arc"),
                        ExerciseAlternative("alt_machine_lateral", "Lateral Raise Machine", "Machine", "Arm pads support elbows completely")
                    )
                ),
                Exercise(
                    id = "ex_tricep_pushdown",
                    name = "Cable Tricep Pushdown",
                    muscleGroup = "Triceps",
                    equipment = "Cable Tower (Rope or Straight Bar)",
                    targetSets = 3,
                    targetReps = "10-12 reps",
                    defaultWeightKg = 18.0,
                    tempo = LiftingTempo(2, 1, 1, "Lock elbows at ribs • Control return 2s"),
                    formCue = "Keep upper arms stationary. Only forearms pivot down.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_machine_tricep", "Seated Tricep Dip Machine", "Machine", "Seated locked position, no balance required"),
                        ExerciseAlternative("alt_overhead_tricep", "Cable Overhead Extension", "Cable", "Stretches long head of triceps")
                    )
                ),
                Exercise(
                    id = "ex_pec_fly",
                    name = "Pec Fly Machine",
                    muscleGroup = "Chest (Stretch)",
                    equipment = "Seated Fly Machine",
                    targetSets = 2,
                    targetReps = "12-15 reps",
                    defaultWeightKg = 25.0,
                    tempo = LiftingTempo(3, 1, 1, "Deep 3s stretch • Squeeze pads together"),
                    formCue = "Soft bend in elbows. Stop when you feel a comfortable chest stretch.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_cable_crossover", "Cable Chest Fly", "Cables", "Adjust pulleys to chest height"),
                        ExerciseAlternative("alt_pushups", "Incline Push-Up on Bench", "Bodyweight", "Gentle, self-regulated angle")
                    )
                )
            )
        ),
        WorkoutTemplate(
            id = "routine_upper_pull",
            title = "Upper Pull & Arms",
            subtitle = "Back & Biceps • Low-fatigue machine flow",
            estimatedMinutes = 30,
            rationale = "Safe, seated postures keep heart rate steady and eliminate lower back fatigue.",
            exercises = listOf(
                Exercise(
                    id = "ex_lat_pulldown",
                    name = "Lat Pulldown Machine",
                    muscleGroup = "Upper Back & Lats",
                    equipment = "Cable Machine with Thigh Pads",
                    targetSets = 3,
                    targetReps = "8-10 reps",
                    defaultWeightKg = 40.0,
                    tempo = LiftingTempo(2, 1, 1, "Pull to chin • 2s controlled stretch up"),
                    formCue = "Tuck your thighs snugly under pads. Pull with your elbows.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_assisted_pullup", "Assisted Pull-Up Machine", "Machine", "Counter-weight platform provides full safety"),
                        ExerciseAlternative("alt_underhand_pulldown", "Reverse-Grip Pulldown", "Cable", "Narrow grip eases shoulder mobility")
                    )
                ),
                Exercise(
                    id = "ex_seated_cable_row",
                    name = "Seated Cable Row",
                    muscleGroup = "Mid-Back & Rhomboids",
                    equipment = "Low Cable Row",
                    targetSets = 3,
                    targetReps = "10-12 reps",
                    defaultWeightKg = 35.0,
                    tempo = LiftingTempo(2, 1, 1, "2s release forward • Squeeze shoulder blades"),
                    formCue = "Sit upright with soft knees. Don't swing backwards.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_chest_supported_row", "Chest-Supported Machine Row", "Machine", "Pad on chest eliminates spine stress entirely"),
                        ExerciseAlternative("alt_single_db_row", "Bench-Supported DB Row", "Dumbbell", "Knee on bench gives 3 points of balance")
                    )
                ),
                Exercise(
                    id = "ex_unilateral_curl",
                    name = "Unilateral DB Bicep Curl",
                    muscleGroup = "Biceps",
                    equipment = "Dumbbells",
                    targetSets = 3,
                    targetReps = "10 reps each",
                    defaultWeightKg = 8.0,
                    isUnilateral = true,
                    primarySide = UnilateralSide.LEFT, // Non-dominant left side first
                    tempo = LiftingTempo(2, 0, 1, "Up clean • 2s smooth lowering"),
                    formCue = "Log left arm first. App automatically auto-balances your right side.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_cable_bicep", "Cable Straight Bar Curl", "Cable", "Smooth continuous cable glide"),
                        ExerciseAlternative("alt_preacher_machine", "Machine Preacher Curl", "Machine", "Arms rested safely on incline pad")
                    )
                ),
                Exercise(
                    id = "ex_rear_delt_machine",
                    name = "Reverse Pec Deck",
                    muscleGroup = "Rear Shoulders & Posture",
                    equipment = "Machine",
                    targetSets = 2,
                    targetReps = "12-15 reps",
                    defaultWeightKg = 20.0,
                    tempo = LiftingTempo(2, 1, 1, "Push handles out • Controlled return"),
                    formCue = "Chest pressed into pad. Feel rear shoulders contracting.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_cable_face_pull", "Cable Face Pull", "Cables", "Pull rope toward bridge of nose")
                    )
                )
            )
        ),
        WorkoutTemplate(
            id = "routine_lower_gentle",
            title = "Lower Body Machine Safe",
            subtitle = "Zero spinal compression • Knee-friendly",
            estimatedMinutes = 28,
            rationale = "Leg press and curl machines isolate prime movers without complex squat balance.",
            exercises = listOf(
                Exercise(
                    id = "ex_leg_press",
                    name = "Seated Leg Press",
                    muscleGroup = "Quads & Glutes",
                    equipment = "Incline or Horizontal Leg Press",
                    targetSets = 3,
                    targetReps = "10-12 reps",
                    defaultWeightKg = 60.0,
                    tempo = LiftingTempo(2, 1, 1, "Lower knees smoothly • Do not lock knees at top"),
                    formCue = "Feet shoulder-width on platform. Keep lower back pinned to backrest.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_hack_squat", "Hack Squat Machine", "Machine", "Shoulder pads support torso"),
                        ExerciseAlternative("alt_goblet_squat", "Light Dumbbell Goblet Squat", "Dumbbell", "Hold light DB close to chest")
                    )
                ),
                Exercise(
                    id = "ex_leg_curl",
                    name = "Seated Leg Curl",
                    muscleGroup = "Hamstrings",
                    equipment = "Seated Hamstring Machine",
                    targetSets = 3,
                    targetReps = "10-12 reps",
                    defaultWeightKg = 30.0,
                    tempo = LiftingTempo(2, 1, 1, "Pull heels down • 2s return up"),
                    formCue = "Adjust lap pad snug against thighs. Squeeze hamstrings.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_lying_leg_curl", "Lying Leg Curl", "Machine", "Prone position on bench")
                    )
                ),
                Exercise(
                    id = "ex_leg_ext",
                    name = "Leg Extension Machine",
                    muscleGroup = "Quadriceps",
                    equipment = "Pin-Loaded Knee Extension",
                    targetSets = 3,
                    targetReps = "12-15 reps",
                    defaultWeightKg = 25.0,
                    tempo = LiftingTempo(2, 1, 1, "Extend legs • Pause 1s at top • 2s down"),
                    formCue = "Align machine pivot axis with your knee joint.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_step_ups", "Low Box Step-Ups", "Box", "Low impact step progression")
                    )
                ),
                Exercise(
                    id = "ex_calf_raise",
                    name = "Seated Calf Raise",
                    muscleGroup = "Calves",
                    equipment = "Machine",
                    targetSets = 2,
                    targetReps = "15 reps",
                    defaultWeightKg = 20.0,
                    tempo = LiftingTempo(2, 2, 1, "Full ankle stretch at bottom • High squeeze"),
                    formCue = "Pad rests on lower thighs. Deep calf stretch at bottom.",
                    alternatives = listOf(
                        ExerciseAlternative("alt_leg_press_calves", "Toe Press on Leg Press", "Machine", "Press with balls of feet")
                    )
                )
            )
        )
    )

    fun getById(id: String): WorkoutTemplate =
        routines.firstOrNull { it.id == id } ?: routines.first()
}
