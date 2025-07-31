package com.naviya.launcher.emergency

/**
 * Enum representing different types of medical emergencies
 * Used by SimpleMedicalEmergencyScreen for emergency classification
 */
enum class MedicalEmergencyType(
    val displayName: String,
    val iconResource: String,
    val priority: Int,
    val description: String
) {
    CHEST_PAIN(
        displayName = "Chest Pain",
        iconResource = "ic_heart",
        priority = 1,
        description = "Severe chest pain or pressure"
    ),
    BREATHING_DIFFICULTY(
        displayName = "Breathing Difficulty", 
        iconResource = "ic_lungs",
        priority = 1,
        description = "Difficulty breathing or shortness of breath"
    ),
    SEVERE_INJURY(
        displayName = "Severe Injury",
        iconResource = "ic_injury", 
        priority = 1,
        description = "Serious physical injury requiring immediate attention"
    ),
    STROKE_SYMPTOMS(
        displayName = "Stroke Symptoms",
        iconResource = "ic_brain",
        priority = 1, 
        description = "Signs of stroke (face drooping, arm weakness, speech difficulty)"
    ),
    FALL_INJURY(
        displayName = "Fall Injury",
        iconResource = "ic_fall",
        priority = 2,
        description = "Injury from falling"
    ),
    MEDICATION_REACTION(
        displayName = "Medication Reaction",
        iconResource = "ic_medication",
        priority = 2,
        description = "Adverse reaction to medication"
    ),
    // Additional enum values referenced by SimpleMedicalEmergencyScreen
    CARDIAC_EVENT(
        displayName = "Cardiac Event",
        iconResource = "ic_heart",
        priority = 1,
        description = "Heart-related emergency"
    ),
    FALL_WITH_INJURY(
        displayName = "Fall with Injury",
        iconResource = "ic_fall",
        priority = 2,
        description = "Fall resulting in injury"
    ),
    MEDICATION_EMERGENCY(
        displayName = "Medication Emergency",
        iconResource = "ic_medication",
        priority = 1,
        description = "Medication-related emergency"
    ),
    COGNITIVE_CRISIS(
        displayName = "Cognitive Crisis",
        iconResource = "ic_brain",
        priority = 2,
        description = "Cognitive or mental health crisis"
    ),
    GENERAL_MEDICAL(
        displayName = "General Medical",
        iconResource = "ic_medical",
        priority = 2,
        description = "General medical emergency"
    )
}
