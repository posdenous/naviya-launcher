package com.naviya.launcher.onboarding.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.naviya.launcher.toggle.ToggleMode

/**
 * Data model for onboarding state in the 3-mode launcher system
 * Tracks user progress through the onboarding flow
 */
@Entity(tableName = "onboarding_state")
data class OnboardingState(
    @PrimaryKey
    val userId: String,
    val currentStep: OnboardingStep,
    val selectedMode: ToggleMode? = null,
    val isCompleted: Boolean = false,
    val startTimestamp: Long = System.currentTimeMillis(),
    val completionTimestamp: Long? = null,
    val emergencyContactsAdded: Int = 0,
    val caregiverConfigured: Boolean = false,
    val accessibilitySettingsConfigured: Boolean = false
)

/**
 * Onboarding steps for the 3-mode system
 */
enum class OnboardingStep {
    WELCOME,
    MODE_SELECTION,
    EMERGENCY_CONTACTS,
    CAREGIVER_SETUP,
    ACCESSIBILITY_SETUP,
    COMPLETED
}

/**
 * Emergency contact data model
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
