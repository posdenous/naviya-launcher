package com.naviya.launcher.ethics

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data models for ethical control and user autonomy protection
 */

@Entity(tableName = "user_app_preferences")
data class UserAppPreferences(
    @PrimaryKey val userId: String,
    val allowedApps: Set<String> = emptySet(),
    val blockedApps: Set<String> = emptySet(),
    val protectedApps: Set<String> = emptySet(), // Apps user has marked as essential
    val lastUpdated: Date = Date(),
    val userConfirmed: Boolean = true
)

@Entity(tableName = "ethical_consent_log")
data class EthicalConsentLog(
    @PrimaryKey val consentId: String,
    val userId: String,
    val consentType: String, // ConsentType enum as string
    val packageName: String?,
    val caregiverId: String?,
    val consentGiven: Boolean,
    val consentTimestamp: Date,
    val witnessId: String? = null,
    val consentMethod: String, // "explicit_dialog", "voice_confirmation", etc.
    val expiryDate: Date? = null,
    val revokedAt: Date? = null,
    val revokedReason: String? = null
)

@Entity(tableName = "emergency_escape_log")
data class EmergencyEscapeLog(
    @PrimaryKey val escapeId: String,
    val userId: String,
    val triggerType: String, // EscapeTrigger enum as string
    val activatedAt: Date,
    val deactivatedAt: Date? = null,
    val reason: String,
    val elderAdvocateNotified: Boolean = false,
    val caregiverNotified: Boolean = false,
    val userConfirmed: Boolean = false
)

@Entity(tableName = "caregiver_action_audit")
data class CaregiverActionAudit(
    @PrimaryKey val actionId: String,
    val userId: String,
    val caregiverId: String,
    val actionType: String, // CaregiverAction enum as string
    val targetApp: String? = null,
    val actionTimestamp: Date,
    val actionResult: String, // CaregiverActionValidation enum as string
    val userNotified: Boolean = false,
    val userConsented: Boolean = false,
    val immutableHash: String, // Blockchain-style integrity protection
    val previousActionHash: String? = null
)

@Entity(tableName = "app_access_validation_log")
data class AppAccessValidationLog(
    @PrimaryKey val validationId: String,
    val userId: String,
    val packageName: String,
    val requestedBy: String, // "user" or caregiver ID
    val accessType: String, // AppAccessType enum as string
    val validationResult: String, // AppAccessValidation enum as string
    val validationTimestamp: Date,
    val userConsentRequired: Boolean = false,
    val userConsentGiven: Boolean? = null,
    val riskLevel: String = "LOW" // LOW, MEDIUM, HIGH, CRITICAL
)

/**
 * Elder rights advocate contact information
 * This contact cannot be modified by caregivers
 */
@Entity(tableName = "elder_rights_advocate")
data class ElderRightsAdvocate(
    @PrimaryKey val advocateId: String,
    val userId: String,
    val advocateName: String,
    val advocatePhone: String,
    val advocateEmail: String,
    val advocateOrganisation: String,
    val isActive: Boolean = true,
    val addedAt: Date = Date(),
    val lastContactAt: Date? = null,
    val cannotBeRemovedBy: Set<String> = emptySet() // Caregiver IDs who cannot remove this
)

/**
 * User autonomy settings - core user rights that cannot be overridden
 */
@Entity(tableName = "user_autonomy_settings")
data class UserAutonomySettings(
    @PrimaryKey val userId: String,
    val canControlOwnApps: Boolean = true,
    val canRevokePermissions: Boolean = true,
    val canAddRemoveCaregivers: Boolean = true,
    val canAccessEmergencyEscape: Boolean = true,
    val canContactElderAdvocate: Boolean = true,
    val requiresWitnessForChanges: Boolean = false,
    val lastUpdated: Date = Date(),
    val lockedByUser: Boolean = false, // User can lock these settings
    val unlockPin: String? = null
)
