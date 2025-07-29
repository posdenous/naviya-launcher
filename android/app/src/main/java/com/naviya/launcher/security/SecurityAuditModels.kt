package com.naviya.launcher.security

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Security audit data models for tracking and preventing mode switching abuse
 */

@Entity(tableName = "mode_switch_audit")
data class ModeSwitchAudit(
    @PrimaryKey val auditId: String,
    val userId: String,
    val fromMode: String,
    val toMode: String,
    val requestedBy: String, // "user", caregiver ID, or "system"
    val result: String, // APPROVED, BLOCKED, etc.
    val authenticationMethod: String? = null,
    val ipAddress: String? = null,
    val deviceFingerprint: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val riskLevel: String = "LOW",
    val blockingReason: String? = null
)

@Entity(tableName = "security_events")
data class SecurityEvent(
    @PrimaryKey val eventId: String,
    val userId: String,
    val eventType: String, // SUSPICIOUS_ACTIVITY, SYSTEM_LOCKED, etc.
    val requestedBy: String,
    val details: String,
    val riskLevel: String,
    val timestamp: Long = System.currentTimeMillis(),
    val resolved: Boolean = false,
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null
)

@Entity(tableName = "authentication_attempts")
data class AuthenticationAttempt(
    @PrimaryKey val attemptId: String,
    val userId: String,
    val requestedBy: String,
    val authMethod: String, // PIN, BIOMETRIC, TOKEN
    val success: Boolean,
    val failureReason: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val ipAddress: String? = null,
    val deviceInfo: String? = null
)

@Entity(tableName = "elderly_consent_log")
data class ElderlyConsentLog(
    @PrimaryKey val consentId: String,
    val userId: String,
    val consentType: String, // MODE_COMPLEXITY, SURVEILLANCE_ENABLE, etc.
    val targetMode: String,
    val consentGiven: Boolean,
    val consentMethod: String, // EXPLICIT_DIALOG, VOICE_CONFIRMATION, etc.
    val witnessId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val expiryTimestamp: Long? = null,
    val revokedAt: Long? = null,
    val revokedReason: String? = null
)

@Entity(tableName = "system_lockout_log")
data class SystemLockoutLog(
    @PrimaryKey val lockoutId: String,
    val userId: String,
    val lockoutReason: String,
    val triggeredBy: String,
    val lockoutStartTime: Long = System.currentTimeMillis(),
    val lockoutEndTime: Long? = null,
    val lockoutDurationMinutes: Int,
    val unlockMethod: String? = null, // AUTO_TIMEOUT, MANUAL_OVERRIDE, etc.
    val elderAdvocateNotified: Boolean = false
)

@Entity(tableName = "caregiver_token_validation")
data class CaregiverTokenValidation(
    @PrimaryKey val validationId: String,
    val caregiverId: String,
    val tokenHash: String,
    val isValid: Boolean,
    val validationTimestamp: Long = System.currentTimeMillis(),
    val expiryTimestamp: Long,
    val revokedAt: Long? = null,
    val revokedReason: String? = null
)
