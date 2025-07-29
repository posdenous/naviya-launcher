package com.naviya.launcher.data.dao

import androidx.room.*
import com.naviya.launcher.security.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for security audit operations
 * Handles all database operations for mode switching security and audit logging
 */
@Dao
interface SecurityAuditDao {
    
    // Mode Switch Audit Operations
    @Insert
    suspend fun logModeSwitch(audit: ModeSwitchAudit)
    
    @Query("SELECT * FROM mode_switch_audit WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentModeSwitches(userId: String = "default", limit: Int = 10): List<ModeSwitchAudit>
    
    @Query("SELECT COUNT(*) FROM mode_switch_audit WHERE timestamp > :since")
    suspend fun getModeSwitchesInLastHour(since: Long = System.currentTimeMillis() - (60 * 60 * 1000)): Int
    
    @Query("SELECT * FROM mode_switch_audit WHERE requestedBy = :requestedBy AND timestamp > :since")
    suspend fun getModeSwitchesByRequester(requestedBy: String, since: Long): List<ModeSwitchAudit>
    
    // Security Events Operations
    @Insert
    suspend fun logSecurityEvent(event: SecurityEvent)
    
    @Query("SELECT * FROM security_events WHERE userId = :userId AND eventType = :eventType ORDER BY timestamp DESC")
    suspend fun getSecurityEventsByType(userId: String, eventType: String): List<SecurityEvent>
    
    @Query("SELECT COUNT(*) FROM security_events WHERE requestedBy = :requestedBy AND eventType = 'SUSPICIOUS_ACTIVITY' AND timestamp > :since")
    suspend fun getRecentSuspiciousEvents(requestedBy: String = "", since: Long = System.currentTimeMillis() - (24 * 60 * 60 * 1000)): Int
    
    @Query("SELECT * FROM security_events WHERE eventType = 'SUSPICIOUS_ACTIVITY' AND resolved = 0 ORDER BY timestamp DESC")
    suspend fun getUnresolvedSuspiciousEvents(): List<SecurityEvent>
    
    // Authentication Attempts Operations
    @Insert
    suspend fun logAuthenticationAttempt(attempt: AuthenticationAttempt)
    
    @Query("SELECT COUNT(*) FROM authentication_attempts WHERE requestedBy = :requestedBy AND success = 0 AND timestamp > :since")
    suspend fun getFailedAuthAttempts(requestedBy: String, since: Long): Int
    
    @Query("SELECT * FROM authentication_attempts WHERE userId = :userId ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentAuthAttempts(userId: String): List<AuthenticationAttempt>
    
    // Elderly Consent Operations
    @Insert
    suspend fun logElderlyConsent(consent: ElderlyConsentLog)
    
    @Query("SELECT * FROM elderly_consent_log WHERE userId = :userId AND consentType = :consentType AND consentGiven = 1 AND (expiryTimestamp IS NULL OR expiryTimestamp > :currentTime) AND revokedAt IS NULL")
    suspend fun hasElderlyConsentForComplexity(userId: String = "default", consentType: String, currentTime: Long = System.currentTimeMillis()): ElderlyConsentLog?
    
    @Query("SELECT COUNT(*) > 0 FROM elderly_consent_log WHERE targetMode = :targetMode AND consentGiven = 1 AND timestamp > :since AND revokedAt IS NULL")
    suspend fun hasUserConsentForModeSwitch(targetMode: String, since: Long): Boolean
    
    @Query("UPDATE elderly_consent_log SET revokedAt = :revokedAt, revokedReason = :reason WHERE consentId = :consentId")
    suspend fun revokeElderlyConsent(consentId: String, revokedAt: Long, reason: String)
    
    // System Lockout Operations
    @Insert
    suspend fun logSystemLockout(lockout: SystemLockoutLog)
    
    @Query("SELECT * FROM system_lockout_log WHERE userId = :userId AND lockoutEndTime IS NULL ORDER BY lockoutStartTime DESC LIMIT 1")
    suspend fun getCurrentLockout(userId: String = "default"): SystemLockoutLog?
    
    @Query("UPDATE system_lockout_log SET lockoutEndTime = :endTime, unlockMethod = :method WHERE lockoutId = :lockoutId")
    suspend fun endSystemLockout(lockoutId: String, endTime: Long, method: String)
    
    // Caregiver Token Validation Operations
    @Insert
    suspend fun logTokenValidation(validation: CaregiverTokenValidation)
    
    @Query("SELECT * FROM caregiver_token_validation WHERE caregiverId = :caregiverId AND isValid = 1 AND expiryTimestamp > :currentTime AND revokedAt IS NULL ORDER BY validationTimestamp DESC LIMIT 1")
    suspend fun validateCaregiverToken(caregiverId: String, currentTime: Long = System.currentTimeMillis()): CaregiverTokenValidation?
    
    @Query("UPDATE caregiver_token_validation SET revokedAt = :revokedAt, revokedReason = :reason WHERE caregiverId = :caregiverId")
    suspend fun revokeCaregiverTokens(caregiverId: String, revokedAt: Long, reason: String)
    
    // Emergency Escape Detection
    @Query("SELECT COUNT(*) > 0 FROM security_events WHERE eventType = 'EMERGENCY_ESCAPE' AND timestamp > :since")
    suspend fun hasRecentEmergencyEscape(since: Long = System.currentTimeMillis() - (60 * 60 * 1000)): Boolean
    
    // Comprehensive Security Overview
    @Query("""
        SELECT 
            se.eventType,
            COUNT(*) as eventCount,
            MAX(se.timestamp) as lastOccurrence
        FROM security_events se 
        WHERE se.userId = :userId 
            AND se.timestamp > :since 
        GROUP BY se.eventType 
        ORDER BY eventCount DESC
    """)
    suspend fun getSecuritySummary(userId: String = "default", since: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)): List<SecuritySummary>
    
    // Real-time monitoring flows
    @Query("SELECT * FROM security_events WHERE eventType IN ('SUSPICIOUS_ACTIVITY', 'SYSTEM_LOCKED', 'EMERGENCY_ESCAPE') ORDER BY timestamp DESC")
    fun observeCriticalSecurityEvents(): Flow<List<SecurityEvent>>
    
    @Query("SELECT * FROM mode_switch_audit WHERE result != 'APPROVED' ORDER BY timestamp DESC")
    fun observeBlockedModeSwitches(): Flow<List<ModeSwitchAudit>>
    
    // Cleanup operations for GDPR compliance
    @Query("DELETE FROM mode_switch_audit WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldModeSwitchAudits(cutoffTime: Long)
    
    @Query("DELETE FROM security_events WHERE timestamp < :cutoffTime AND resolved = 1")
    suspend fun cleanupResolvedSecurityEvents(cutoffTime: Long)
    
    @Query("DELETE FROM authentication_attempts WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldAuthAttempts(cutoffTime: Long)
    
    // Helper methods for security validation
    suspend fun validateCaregiverToken(caregiverId: String, tokenHash: String): Boolean {
        val validation = validateCaregiverToken(caregiverId)
        return validation?.tokenHash == tokenHash
    }
    
    suspend fun hasElderlyConsentForComplexity(targetMode: String): Boolean {
        val consent = hasElderlyConsentForComplexity(
            userId = "default", 
            consentType = "MODE_COMPLEXITY"
        )
        return consent != null
    }
    
    suspend fun getRecentSecurityEvents(requestedBy: String): List<SecurityEvent> {
        return getSecurityEventsByType("default", "MODE_SWITCH")
            .filter { it.requestedBy == requestedBy }
            .filter { it.timestamp > System.currentTimeMillis() - (24 * 60 * 60 * 1000) }
    }
}

/**
 * Data class for security summary queries
 */
data class SecuritySummary(
    val eventType: String,
    val eventCount: Int,
    val lastOccurrence: Long
)
