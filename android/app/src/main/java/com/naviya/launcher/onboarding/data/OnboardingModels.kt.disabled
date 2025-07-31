package com.naviya.launcher.onboarding.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Onboarding state tracking for family-assisted setup
 */
@Entity(tableName = "onboarding_state")
data class OnboardingState(
    @PrimaryKey
    val onboardingId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val currentStep: String, // OnboardingStep as string
    val isAssistedSetup: Boolean = false,
    val assistantName: String? = null,
    val assistantRelationship: String? = null,
    
    // Setup timing and progress
    val setupStartTime: Long = System.currentTimeMillis(),
    val lastStepTimestamp: Long = System.currentTimeMillis(),
    val estimatedCompletionTime: Long? = null,
    
    // Family-friendly setup flags
    val familyAssistedSetup: Boolean = false,
    val professionalInstallationSkipped: Boolean = false,
    val skipReason: String? = null,
    
    // Consent and witness tracking
    val userConsentGiven: Boolean = false,
    val consentWitnessName: String? = null,
    val consentTimestamp: Long? = null,
    
    // Completion tracking
    val onboardingCompleted: Boolean = false,
    val completionTimestamp: Long? = null,
    val setupMethod: String = "family_assisted", // family_assisted, professional, self_service
    
    // Error handling
    val hasErrors: Boolean = false,
    val lastError: String? = null,
    val errorCount: Int = 0,
    
    // Metadata
    val deviceInfo: String? = null,
    val appVersion: String? = null,
    val androidVersion: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Family setup session for tracking multi-person setup
 */
@Entity(tableName = "family_setup_session")
data class FamilySetupSession(
    @PrimaryKey
    val sessionId: String = UUID.randomUUID().toString(),
    
    val elderlyUserId: String,
    val elderlyUserName: String,
    val familyAssistantName: String,
    val assistantRelationship: String,
    
    // Session management
    val sessionStartTime: Long = System.currentTimeMillis(),
    val sessionTimeoutTime: Long = System.currentTimeMillis() + 1800000L, // 30 minutes
    val isActive: Boolean = true,
    
    // Setup preferences
    val setupLocation: String? = null, // "home", "family_member_home", "care_facility"
    val setupContext: String? = null, // "initial_setup", "device_replacement", "assistance_request"
    
    // Progress tracking
    val stepsCompleted: List<String> = emptyList(), // OnboardingStep names
    val currentStepStartTime: Long = System.currentTimeMillis(),
    val totalTimeSpentMs: Long = 0,
    
    // Family dynamics
    val multipleAssistants: Boolean = false,
    val assistantNames: List<String> = emptyList(),
    val primaryAssistant: String = familyAssistantName,
    
    // Completion
    val sessionCompleted: Boolean = false,
    val completionTimestamp: Long? = null,
    val sessionResult: String? = null, // "completed", "timeout", "cancelled", "error"
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Onboarding preferences specific to elderly users
 */
@Entity(tableName = "onboarding_preferences")
data class OnboardingPreferences(
    @PrimaryKey
    val preferencesId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    
    // Accessibility preferences
    val fontScale: Float = 1.6f, // Minimum 1.6x for elderly
    val iconScale: Float = 1.4f,
    val highContrastEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val slowAnimationsEnabled: Boolean = true,
    val ttsEnabled: Boolean = false, // User choice
    
    // Layout preferences
    val preferredMode: String = "COMFORT", // ToggleMode as string
    val emergencyButtonAlwaysVisible: Boolean = true,
    val simpleNavigationEnabled: Boolean = true,
    val largeButtonsEnabled: Boolean = true,
    
    // Language and localization
    val preferredLanguage: String = "en",
    val rtlSupported: Boolean = false,
    val voiceCommandsEnabled: Boolean = false,
    
    // Privacy and safety preferences
    val shareUsageData: Boolean = false, // Opt-in only
    val allowCaregiverNotifications: Boolean = false, // Opt-in only
    val emergencyLocationSharing: Boolean = true, // Default enabled for safety
    
    // Onboarding experience preferences
    val skipTutorials: Boolean = false,
    val showHelpHints: Boolean = true,
    val allowAssistedSetup: Boolean = true,
    
    // Family context
    val hasFamily: Boolean = false,
    val familyTechSupport: Boolean = false,
    val livesAlone: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Setup validation results
 */
@Entity(tableName = "setup_validation")
data class SetupValidation(
    @PrimaryKey
    val validationId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val sessionId: String,
    
    // Validation checks
    val emergencyContactsValid: Boolean = false,
    val accessibilitySettingsValid: Boolean = false,
    val permissionsGranted: Boolean = false,
    val caregiverSetupValid: Boolean = true, // Default true since it's optional
    
    // Required validations
    val hasEmergencyContacts: Boolean = false,
    val emergencyContactCount: Int = 0,
    val hasValidPhoneNumbers: Boolean = false,
    
    // Optional validations
    val caregiverPaired: Boolean = false,
    val caregiverPermissionsSet: Boolean = false,
    val familyNotificationSetup: Boolean = false,
    
    // System validations
    val deviceCompatible: Boolean = true,
    val androidVersionSupported: Boolean = true,
    val requiredPermissionsAvailable: Boolean = true,
    
    // Overall validation
    val allRequiredValid: Boolean = false,
    val allOptionalValid: Boolean = false,
    val readyForCompletion: Boolean = false,
    
    // Validation details
    val validationErrors: List<String> = emptyList(),
    val validationWarnings: List<String> = emptyList(),
    val validationNotes: List<String> = emptyList(),
    
    val validatedAt: Long = System.currentTimeMillis()
)

/**
 * Family assistance tracking
 */
@Entity(tableName = "family_assistance")
data class FamilyAssistance(
    @PrimaryKey
    val assistanceId: String = UUID.randomUUID().toString(),
    
    val elderlyUserId: String,
    val assistantName: String,
    val assistantRelationship: String,
    
    // Assistance details
    val assistanceType: String, // "initial_setup", "ongoing_support", "troubleshooting"
    val assistanceStartTime: Long = System.currentTimeMillis(),
    val assistanceEndTime: Long? = null,
    val assistanceDurationMs: Long = 0,
    
    // Actions performed
    val actionsPerformed: List<String> = emptyList(),
    val stepsCompleted: List<String> = emptyList(),
    val issuesResolved: List<String> = emptyList(),
    
    // Assistance quality
    val elderlyUserSatisfaction: Int? = null, // 1-5 scale
    val assistanceEffectiveness: Int? = null, // 1-5 scale
    val wouldRecommend: Boolean? = null,
    
    // Follow-up
    val followUpNeeded: Boolean = false,
    val followUpReason: String? = null,
    val followUpScheduled: Long? = null,
    
    // Notes and feedback
    val assistantNotes: String? = null,
    val elderlyUserFeedback: String? = null,
    val systemObservations: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Onboarding analytics for improving the process
 */
@Entity(tableName = "onboarding_analytics")
data class OnboardingAnalytics(
    @PrimaryKey
    val analyticsId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val sessionId: String,
    
    // Timing analytics
    val totalSetupTimeMs: Long = 0,
    val averageStepTimeMs: Long = 0,
    val longestStepTimeMs: Long = 0,
    val shortestStepTimeMs: Long = 0,
    
    // Step analytics
    val stepsCompleted: Int = 0,
    val stepsSkipped: Int = 0,
    val stepsRepeated: Int = 0,
    val mostDifficultStep: String? = null,
    val easiestStep: String? = null,
    
    // Error analytics
    val errorsEncountered: Int = 0,
    val errorTypes: List<String> = emptyList(),
    val errorResolutionTimeMs: Long = 0,
    val helpRequestCount: Int = 0,
    
    // Assistance analytics
    val assistanceRequired: Boolean = false,
    val assistanceEffective: Boolean = false,
    val assistanceTimeMs: Long = 0,
    
    // Outcome analytics
    val setupCompleted: Boolean = false,
    val setupAbandoned: Boolean = false,
    val setupRestarted: Boolean = false,
    val userSatisfaction: Int? = null, // 1-5 scale
    
    // Device and context
    val deviceType: String? = null,
    val androidVersion: String? = null,
    val setupLocation: String? = null,
    val timeOfDay: String? = null,
    
    // Privacy-compliant analytics (no personal data)
    val userAgeRange: String? = null, // "60-70", "70-80", "80+"
    val techExperience: String? = null, // "none", "basic", "intermediate"
    val assistanceType: String? = null, // "family", "professional", "none"
    
    val recordedAt: Long = System.currentTimeMillis()
)
