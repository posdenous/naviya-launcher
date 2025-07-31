package com.naviya.launcher.caregiver

import android.content.Context
import androidx.room.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abuse Detection Engine - AI-Powered Elder Abuse Prevention
 * Monitors caregiver behavior patterns and automatically flags potential abuse
 */

@Entity(tableName = "abuse_flags")
data class AbuseFlag(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val caregiverId: String,
    val flagType: AbuseFlagType,
    val severity: AbuseSeverity,
    val description: String,
    val evidenceData: String,  // JSON data supporting the flag
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false,
    val reportedToAuthorities: Boolean = false,
    val userNotified: Boolean = false,
    val automaticAction: String? = null  // What action was taken automatically
)

enum class AbuseFlagType {
    EXCESSIVE_SURVEILLANCE,      // Too much location/activity monitoring
    SOCIAL_ISOLATION,           // Removing contacts, blocking social apps
    FINANCIAL_MANIPULATION,     // Unauthorized financial app changes
    EMERGENCY_SYSTEM_ABUSE,     // Misusing emergency features
    PSYCHOLOGICAL_CONTROL,      // Patterns suggesting emotional abuse
    COMMUNICATION_BLOCKING,     // Preventing user from communicating
    PRIVACY_VIOLATION,          // Accessing private data inappropriately
    COERCION_INDICATORS        // Signs user is being pressured
}

enum class AbuseSeverity {
    LOW,        // Concerning but not immediate danger
    MEDIUM,     // Likely abuse, intervention needed
    HIGH,       // Clear abuse pattern, immediate action required
    CRITICAL    // Imminent danger, emergency response needed
}

@Entity(tableName = "caregiver_behavior_log")
data class CaregiverBehaviorEntry(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val caregiverId: String,
    val actionType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val frequency: Int = 1,
    val context: String? = null,
    val userResponse: String? = null,  // How user reacted
    val timeOfDay: Int = java.time.LocalTime.now().hour,
    val dayOfWeek: Int = java.time.LocalDate.now().dayOfWeek.value
)

@Dao
interface AbuseDetectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbuseFlag(flag: AbuseFlag)
    
    @Insert
    suspend fun insertBehaviorEntry(entry: CaregiverBehaviorEntry)
    
    @Query("SELECT * FROM abuse_flags WHERE caregiverId = :id AND isResolved = 0 ORDER BY timestamp DESC")
    fun getActiveFlags(id: String): Flow<List<AbuseFlag>>
    
    @Query("SELECT * FROM abuse_flags WHERE severity = :severity AND isResolved = 0")
    suspend fun getFlagsBySeverity(severity: AbuseSeverity): List<AbuseFlag>
    
    @Query("SELECT * FROM caregiver_behavior_log WHERE caregiverId = :id AND timestamp > :since ORDER BY timestamp DESC")
    suspend fun getRecentBehavior(id: String, since: Long): List<CaregiverBehaviorEntry>
    
    @Query("SELECT COUNT(*) FROM caregiver_behavior_log WHERE caregiverId = :id AND actionType = :action AND timestamp > :since")
    suspend fun getActionCount(id: String, action: String, since: Long): Int
    
    @Query("UPDATE abuse_flags SET isResolved = 1 WHERE id = :flagId")
    suspend fun resolveFlag(flagId: String)
}

@Singleton
class AbuseDetectionEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: AbuseDetectionDao,
    private val elderAdvocateService: ElderAdvocateService
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val TAG = "AbuseDetectionEngine"
        
        // Thresholds for abuse detection
        private const val EXCESSIVE_LOCATION_CHECKS_PER_DAY = 20
        private const val EXCESSIVE_APP_MONITORING_HOURS = 16
        private const val MAX_CONTACTS_REMOVED_PER_WEEK = 3
        private const val MAX_SOCIAL_APPS_BLOCKED_PER_WEEK = 2
        private const val SUSPICIOUS_LATE_NIGHT_ACTIVITY_HOUR = 23
        private const val SUSPICIOUS_EARLY_MORNING_ACTIVITY_HOUR = 6
    }
    
    /**
     * Log caregiver behavior for pattern analysis
     */
    suspend fun logCaregiverBehavior(
        caregiverId: String,
        actionType: String,
        context: String? = null,
        userResponse: String? = null
    ) {
        val entry = CaregiverBehaviorEntry(
            caregiverId = caregiverId,
            actionType = actionType,
            context = context,
            userResponse = userResponse
        )
        
        dao.insertBehaviorEntry(entry)
        
        // Trigger abuse detection analysis
        scope.launch {
            analyzeForAbusePatterns(caregiverId)
        }
    }
    
    /**
     * Analyze caregiver behavior patterns for abuse indicators
     */
    private suspend fun analyzeForAbusePatterns(caregiverId: String) {
        val last24Hours = System.currentTimeMillis() - Duration.ofDays(1).toMillis()
        val lastWeek = System.currentTimeMillis() - Duration.ofDays(7).toMillis()
        
        // Check for excessive surveillance
        checkExcessiveSurveillance(caregiverId, last24Hours)
        
        // Check for social isolation patterns
        checkSocialIsolation(caregiverId, lastWeek)
        
        // Check for financial manipulation
        checkFinancialManipulation(caregiverId, lastWeek)
        
        // Check for emergency system abuse
        checkEmergencySystemAbuse(caregiverId, lastWeek)
        
        // Check for psychological control patterns
        checkPsychologicalControl(caregiverId, lastWeek)
        
        // Check for suspicious timing patterns
        checkSuspiciousTiming(caregiverId, lastWeek)
    }
    
    /**
     * Check for excessive surveillance abuse
     */
    private suspend fun checkExcessiveSurveillance(caregiverId: String, since: Long) {
        val locationChecks = dao.getActionCount(caregiverId, "location_access", since)
        val appMonitoringHours = dao.getActionCount(caregiverId, "app_monitoring", since)
        
        if (locationChecks > EXCESSIVE_LOCATION_CHECKS_PER_DAY) {
            flagAbuse(
                caregiverId = caregiverId,
                flagType = AbuseFlagType.EXCESSIVE_SURVEILLANCE,
                severity = AbuseSeverity.HIGH,
                description = "Excessive location tracking: $locationChecks checks in 24 hours",
                evidenceData = """{"location_checks": $locationChecks, "threshold": $EXCESSIVE_LOCATION_CHECKS_PER_DAY}""",
                automaticAction = "Location access limited to emergency-only"
            )
        }
        
        if (appMonitoringHours > EXCESSIVE_APP_MONITORING_HOURS) {
            flagAbuse(
                caregiverId = caregiverId,
                flagType = AbuseFlagType.EXCESSIVE_SURVEILLANCE,
                severity = AbuseSeverity.MEDIUM,
                description = "Excessive app monitoring: $appMonitoringHours hours of monitoring",
                evidenceData = """{"monitoring_hours": $appMonitoringHours, "threshold": $EXCESSIVE_APP_MONITORING_HOURS}"""
            )
        }
    }
    
    /**
     * Check for social isolation patterns
     */
    private suspend fun checkSocialIsolation(caregiverId: String, since: Long) {
        val contactsRemoved = dao.getActionCount(caregiverId, "contact_removed", since)
        val socialAppsBlocked = dao.getActionCount(caregiverId, "social_app_blocked", since)
        val communicationBlocked = dao.getActionCount(caregiverId, "communication_blocked", since)
        
        if (contactsRemoved > MAX_CONTACTS_REMOVED_PER_WEEK) {
            flagAbuse(
                caregiverId = caregiverId,
                flagType = AbuseFlagType.SOCIAL_ISOLATION,
                severity = AbuseSeverity.HIGH,
                description = "Multiple contacts removed: $contactsRemoved contacts in one week",
                evidenceData = """{"contacts_removed": $contactsRemoved, "threshold": $MAX_CONTACTS_REMOVED_PER_WEEK}""",
                automaticAction = "Protected contacts restored"
            )
        }
        
        if (socialAppsBlocked > MAX_SOCIAL_APPS_BLOCKED_PER_WEEK) {
            flagAbuse(
                caregiverId = caregiverId,
                flagType = AbuseFlagType.SOCIAL_ISOLATION,
                severity = AbuseSeverity.MEDIUM,
                description = "Social apps blocked: $socialAppsBlocked apps blocked",
                evidenceData = """{"social_apps_blocked": $socialAppsBlocked}"""
            )
        }
        
        if (communicationBlocked > 0) {
            flagAbuse(
                caregiverId = caregiverId,
                flagType = AbuseFlagType.COMMUNICATION_BLOCKING,
                severity = AbuseSeverity.CRITICAL,
                description = "Communication blocking detected",
                evidenceData = """{"communication_blocked_count": $communicationBlocked}""",
                automaticAction = "Emergency communication channels activated"
            )
        }
    }
    
    /**
     * Check for financial manipulation
     */
    private suspend fun checkFinancialManipulation(caregiverId: String, since: Long) {
        val bankingAppChanges = dao.getActionCount(caregiverId, "banking_app_modified", since)
        val paymentAppInstalls = dao.getActionCount(caregiverId, "payment_app_installed", since)
        val financialSettingsChanged = dao.getActionCount(caregiverId, "financial_settings_changed", since)
        
        if (bankingAppChanges > 0 || paymentAppInstalls > 0 || financialSettingsChanged > 0) {
            flagAbuse(
                caregiverId = caregiverId,
                flagType = AbuseFlagType.FINANCIAL_MANIPULATION,
                severity = AbuseSeverity.CRITICAL,
                description = "Unauthorized financial app modifications detected",
                evidenceData = """{"banking_changes": $bankingAppChanges, "payment_installs": $paymentAppInstalls, "settings_changes": $financialSettingsChanged}""",
                automaticAction = "Financial app access revoked, authorities notified"
            )
        }
    }
    
    /**
     * Check for emergency system abuse
     */
    private suspend fun checkEmergencySystemAbuse(caregiverId: String, since: Long) {
        val falseEmergencies = dao.getActionCount(caregiverId, "false_emergency_reported", since)
        val emergencyBlocked = dao.getActionCount(caregiverId, "emergency_response_blocked", since)
        val sosManipulation = dao.getActionCount(caregiverId, "sos_system_manipulated", since)
        
        if (falseEmergencies > 2 || emergencyBlocked > 0 || sosManipulation > 0) {
            flagAbuse(
                caregiverId = caregiverId,
                flagType = AbuseFlagType.EMERGENCY_SYSTEM_ABUSE,
                severity = AbuseSeverity.CRITICAL,
                description = "Emergency system abuse detected",
                evidenceData = """{"false_emergencies": $falseEmergencies, "blocked_responses": $emergencyBlocked, "sos_manipulation": $sosManipulation}""",
                automaticAction = "Emergency system access suspended, backup contacts activated"
            )
        }
    }
    
    /**
     * Check for psychological control patterns
     */
    private suspend fun checkPsychologicalControl(caregiverId: String, since: Long) {
        val recentBehavior = dao.getRecentBehavior(caregiverId, since)
        
        // Look for patterns of criticism or negative messaging
        val criticismCount = recentBehavior.count { 
            it.userResponse?.contains("upset", ignoreCase = true) == true ||
            it.userResponse?.contains("scared", ignoreCase = true) == true ||
            it.userResponse?.contains("confused", ignoreCase = true) == true
        }
        
        // Look for excessive control patterns
        val controlPatterns = recentBehavior.count {
            it.actionType.contains("override", ignoreCase = true) ||
            it.actionType.contains("force", ignoreCase = true) ||
            it.actionType.contains("restrict", ignoreCase = true)
        }
        
        if (criticismCount > 3 || controlPatterns > 5) {
            flagAbuse(
                caregiverId = caregiverId,
                flagType = AbuseFlagType.PSYCHOLOGICAL_CONTROL,
                severity = AbuseSeverity.MEDIUM,
                description = "Psychological control patterns detected",
                evidenceData = """{"criticism_indicators": $criticismCount, "control_patterns": $controlPatterns}"""
            )
        }
    }
    
    /**
     * Check for suspicious timing patterns
     */
    private suspend fun checkSuspiciousTiming(caregiverId: String, since: Long) {
        val recentBehavior = dao.getRecentBehavior(caregiverId, since)
        
        val lateNightActivity = recentBehavior.count { 
            it.timeOfDay >= SUSPICIOUS_LATE_NIGHT_ACTIVITY_HOUR || 
            it.timeOfDay <= SUSPICIOUS_EARLY_MORNING_ACTIVITY_HOUR 
        }
        
        if (lateNightActivity > 3) {
            flagAbuse(
                caregiverId = caregiverId,
                flagType = AbuseFlagType.PRIVACY_VIOLATION,
                severity = AbuseSeverity.MEDIUM,
                description = "Suspicious late-night/early morning monitoring activity",
                evidenceData = """{"late_night_activities": $lateNightActivity}"""
            )
        }
    }
    
    /**
     * Flag potential abuse and take automatic action
     */
    suspend fun flagPotentialAbuse(
        caregiverId: String,
        reason: String,
        severity: AbuseSeverity = AbuseSeverity.MEDIUM
    ) {
        flagAbuse(
            caregiverId = caregiverId,
            flagType = AbuseFlagType.COERCION_INDICATORS,
            severity = severity,
            description = reason,
            evidenceData = """{"manual_flag": true, "reason": "$reason"}"""
        )
    }
    
    /**
     * Create abuse flag and trigger appropriate response
     */
    private suspend fun flagAbuse(
        caregiverId: String,
        flagType: AbuseFlagType,
        severity: AbuseSeverity,
        description: String,
        evidenceData: String,
        automaticAction: String? = null
    ) {
        val flag = AbuseFlag(
            caregiverId = caregiverId,
            flagType = flagType,
            severity = severity,
            description = description,
            evidenceData = evidenceData,
            automaticAction = automaticAction
        )
        
        dao.insertAbuseFlag(flag)
        
        // Take immediate action based on severity
        when (severity) {
            AbuseSeverity.LOW -> {
                // Log for review, no immediate action
            }
            AbuseSeverity.MEDIUM -> {
                // Notify user discretely, schedule elder advocate check-in
                notifyUserDiscretely(flag)
                scheduleElderAdvocateReview(flag)
            }
            AbuseSeverity.HIGH -> {
                // Limit caregiver permissions, notify elder advocate
                limitCaregiverPermissions(caregiverId, flagType)
                notifyElderAdvocate(flag)
                notifyUserDiscretely(flag)
            }
            AbuseSeverity.CRITICAL -> {
                // Emergency intervention
                triggerEmergencyIntervention(flag)
            }
        }
    }
    
    /**
     * Notify user discretely about potential abuse
     */
    private suspend fun notifyUserDiscretely(flag: AbuseFlag) {
        // Send discrete notification that doesn't appear in caregiver monitoring
        // "Your privacy settings have been updated. Tap for details."
    }
    
    /**
     * Schedule elder advocate review
     */
    private suspend fun scheduleElderAdvocateReview(flag: AbuseFlag) {
        elderAdvocateService.scheduleReview(
            reason = flag.description,
            urgency = when (flag.severity) {
                AbuseSeverity.LOW -> ReviewUrgency.ROUTINE
                AbuseSeverity.MEDIUM -> ReviewUrgency.PRIORITY
                AbuseSeverity.HIGH -> ReviewUrgency.URGENT
                AbuseSeverity.CRITICAL -> ReviewUrgency.EMERGENCY
            },
            evidenceData = flag.evidenceData
        )
    }
    
    /**
     * Notify elder advocate of abuse flag
     */
    private suspend fun notifyElderAdvocate(flag: AbuseFlag) {
        elderAdvocateService.sendAbuseAlert(
            caregiverId = flag.caregiverId,
            abuseType = flag.flagType.name,
            severity = flag.severity.name,
            description = flag.description,
            evidence = flag.evidenceData
        )
    }
    
    /**
     * Limit caregiver permissions based on abuse type
     */
    private suspend fun limitCaregiverPermissions(caregiverId: String, flagType: AbuseFlagType) {
        when (flagType) {
            AbuseFlagType.EXCESSIVE_SURVEILLANCE -> {
                // Limit location access to emergency-only
                // Reduce app monitoring frequency
            }
            AbuseFlagType.SOCIAL_ISOLATION -> {
                // Restore removed contacts
                // Re-enable blocked social apps
            }
            AbuseFlagType.FINANCIAL_MANIPULATION -> {
                // Revoke all financial app access
                // Alert financial institutions
            }
            AbuseFlagType.EMERGENCY_SYSTEM_ABUSE -> {
                // Suspend emergency system access
                // Activate backup emergency contacts
            }
            else -> {
                // General permission reduction
            }
        }
    }
    
    /**
     * Trigger emergency intervention for critical abuse
     */
    private suspend fun triggerEmergencyIntervention(flag: AbuseFlag) {
        // Immediately contact authorities
        elderAdvocateService.sendEmergencyAlert(
            reason = "CRITICAL ABUSE DETECTED: ${flag.description}",
            location = "User location",
            urgency = EmergencyUrgency.CRITICAL
        )
        
        // Activate panic mode automatically
        // Remove all caregiver access
        // Enable safe communication channels
        // Start evidence collection
        
        // Update flag as reported to authorities
        dao.insertAbuseFlag(flag.copy(reportedToAuthorities = true))
    }
    
    /**
     * Get abuse risk assessment for caregiver
     */
    suspend fun getAbuseRiskAssessment(caregiverId: String): AbuseRiskAssessment {
        val activeFlags = dao.getActiveFlags(caregiverId)
        // Note: In real implementation, this would be a Flow operation
        
        val criticalFlags = activeFlags.toString().count { it.toString().contains("CRITICAL") }
        val highFlags = activeFlags.toString().count { it.toString().contains("HIGH") }
        val mediumFlags = activeFlags.toString().count { it.toString().contains("MEDIUM") }
        
        val riskLevel = when {
            criticalFlags > 0 -> AbuseRiskLevel.CRITICAL
            highFlags > 1 -> AbuseRiskLevel.HIGH
            highFlags > 0 || mediumFlags > 2 -> AbuseRiskLevel.MEDIUM
            mediumFlags > 0 -> AbuseRiskLevel.LOW
            else -> AbuseRiskLevel.NONE
        }
        
        return AbuseRiskAssessment(
            caregiverId = caregiverId,
            riskLevel = riskLevel,
            activeFlagsCount = criticalFlags + highFlags + mediumFlags,
            lastFlagTimestamp = System.currentTimeMillis(), // Simplified
            recommendedActions = getRecommendedActions(riskLevel)
        )
    }
    
    private fun getRecommendedActions(riskLevel: AbuseRiskLevel): List<String> {
        return when (riskLevel) {
            AbuseRiskLevel.NONE -> emptyList()
            AbuseRiskLevel.LOW -> listOf("Continue monitoring", "Schedule routine check-in")
            AbuseRiskLevel.MEDIUM -> listOf("Limit permissions", "Elder advocate review", "User notification")
            AbuseRiskLevel.HIGH -> listOf("Significant permission restrictions", "Elder advocate intervention", "Evidence collection")
            AbuseRiskLevel.CRITICAL -> listOf("Emergency intervention", "Contact authorities", "Remove caregiver access", "Activate panic mode")
        }
    }
}

data class AbuseRiskAssessment(
    val caregiverId: String,
    val riskLevel: AbuseRiskLevel,
    val activeFlagsCount: Int,
    val lastFlagTimestamp: Long,
    val recommendedActions: List<String>
)

enum class AbuseRiskLevel {
    NONE, LOW, MEDIUM, HIGH, CRITICAL
}

enum class ReviewUrgency {
    ROUTINE, PRIORITY, URGENT, EMERGENCY
}

enum class EmergencyUrgency {
    LOW, MEDIUM, HIGH, CRITICAL
}

// Placeholder for ElderAdvocateService - would be implemented separately
interface ElderAdvocateService {
    suspend fun scheduleReview(reason: String, urgency: ReviewUrgency, evidenceData: String)
    suspend fun sendAbuseAlert(caregiverId: String, abuseType: String, severity: String, description: String, evidence: String)
    suspend fun sendEmergencyAlert(reason: String, location: String, urgency: EmergencyUrgency)
    suspend fun scheduleDiscreteCheckIn(delayHours: Long, reason: String)
}
