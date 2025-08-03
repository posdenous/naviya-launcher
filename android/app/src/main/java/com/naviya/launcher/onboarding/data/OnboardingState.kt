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
    val currentStep: String,
    val onboardingCompleted: Boolean = false,
    val setupStartTime: Long = System.currentTimeMillis(),
    val completionTimestamp: Long? = null,
    val emergencyContactsAdded: Int = 0,
    val caregiverConfigured: Boolean = false,
    val accessibilitySettingsConfigured: Boolean = false,
    val professionalInstallationSkipped: Boolean = false,
    val skipReason: String = "",
    val familyAssistedSetup: Boolean = false,
    val hasErrors: Boolean = false,
    val lastError: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Onboarding steps for the 3-mode system
 */
enum class OnboardingStep {
    WELCOME,
    MODE_SELECTION,
    BASIC_PREFERENCES,
    EMERGENCY_CONTACTS,
    OPTIONAL_CAREGIVER,
    SKIP_PROFESSIONAL,
    COMPLETION,
    LAUNCHER_READY
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
