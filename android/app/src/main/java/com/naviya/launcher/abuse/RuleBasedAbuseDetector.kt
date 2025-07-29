package com.naviya.launcher.abuse

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.naviya.launcher.contacts.data.ContactDao
import com.naviya.launcher.caregiver.CaregiverPermissionManager
import com.naviya.launcher.emergency.EmergencyService
import java.util.concurrent.TimeUnit

/**
 * Rule-Based Abuse Detection System
 * 
 * Uses sophisticated rules and pattern analysis to detect caregiver abuse
 * without requiring complex ML infrastructure. Designed for reliability,
 * transparency, and offline operation.
 */
@Singleton
class RuleBasedAbuseDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactDao: ContactDao,
    private val caregiverPermissionManager: CaregiverPermissionManager,
    private val emergencyService: EmergencyService,
    private val abuseDao: AbuseDetectionDao
) {
    
    companion object {
        private const val TAG = "RuleBasedAbuseDetector"
        
        // Time windows for pattern analysis
        private val HOUR_MS = TimeUnit.HOURS.toMillis(1)
        private val DAY_MS = TimeUnit.DAYS.toMillis(1)
        private val WEEK_MS = TimeUnit.DAYS.toMillis(7)
        
        // Risk score thresholds
        private const val LOW_RISK_THRESHOLD = 25
        private const val MEDIUM_RISK_THRESHOLD = 50
        private const val HIGH_RISK_THRESHOLD = 80
        private const val CRITICAL_RISK_THRESHOLD = 100
        
        // Pattern detection thresholds
        private const val MAX_BLOCKED_ATTEMPTS_PER_HOUR = 3
        private const val MAX_BLOCKED_ATTEMPTS_PER_DAY = 10
        private const val MAX_PERMISSION_ESCALATIONS_PER_DAY = 2
        private const val SUSPICIOUS_NIGHT_ACTIVITY_THRESHOLD = 5 // 11PM - 6AM
    }
    
    private val _currentRiskAssessments = MutableStateFlow<Map<String, AbuseRiskAssessment>>(emptyMap())
    val currentRiskAssessments: StateFlow<Map<String, AbuseRiskAssessment>> = _currentRiskAssessments.asStateFlow()
    
    private val _recentAlerts = MutableStateFlow<List<AbuseAlert>>(emptyList())
    val recentAlerts: StateFlow<List<AbuseAlert>> = _recentAlerts.asStateFlow()
    
    /**
     * Analyze caregiver behavior and calculate abuse risk score
     */
    suspend fun analyzeCaregiver(
        caregiverId: String,
        userId: String,
        triggerEvent: AbuseDetectionTrigger? = null
    ): AbuseRiskAssessment {
        
        val currentTime = System.currentTimeMillis()
        
        // Collect behavioral data
        val behaviorData = collectCaregiverBehaviorData(caregiverId, userId, currentTime)
        
        // Apply detection rules
        val riskFactors = applyDetectionRules(behaviorData, triggerEvent)
        
        // Calculate overall risk score
        val totalRiskScore = riskFactors.sumOf { it.score }
        val riskLevel = determineRiskLevel(totalRiskScore)
        
        // Create risk assessment
        val assessment = AbuseRiskAssessment(
            assessmentId = java.util.UUID.randomUUID().toString(),
            caregiverId = caregiverId,
            userId = userId,
            riskScore = totalRiskScore,
            riskLevel = riskLevel,
            riskFactors = riskFactors,
            triggerEvent = triggerEvent,
            assessmentTimestamp = currentTime,
            behaviorData = behaviorData
        )
        
        // Store assessment
        abuseDao.insertRiskAssessment(assessment)
        
        // Update current assessments
        val currentAssessments = _currentRiskAssessments.value.toMutableMap()
        currentAssessments[caregiverId] = assessment
        _currentRiskAssessments.value = currentAssessments
        
        // Generate alerts if necessary
        if (riskLevel >= AbuseRiskLevel.MEDIUM) {
            generateAbuseAlert(assessment)
        }
        
        return assessment
    }
    
    /**
     * Collect comprehensive behavioral data for analysis
     */
    private suspend fun collectCaregiverBehaviorData(
        caregiverId: String,
        userId: String,
        currentTime: Long
    ): CaregiverBehaviorData {
        
        // Contact manipulation attempts
        val contactAttempts = contactDao.getRecentContactModificationAttempts(
            caregiverId = caregiverId,
            userId = userId,
            timeWindowMs = WEEK_MS
        )
        
        // Permission-related activities
        val permissionHistory = caregiverPermissionManager.getPermissionHistory(
            caregiverId = caregiverId,
            sinceTime = currentTime - WEEK_MS
        )
        
        // Emergency system interactions
        val emergencyInteractions = emergencyService.getCaregiverEmergencyInteractions(
            caregiverId = caregiverId,
            userId = userId,
            sinceTime = currentTime - WEEK_MS
        )
        
        // Previous risk assessments
        val previousAssessments = abuseDao.getRecentRiskAssessments(
            caregiverId = caregiverId,
            userId = userId,
            sinceTime = currentTime - WEEK_MS
        )
        
        return CaregiverBehaviorData(
            caregiverId = caregiverId,
            userId = userId,
            analysisTimeWindow = WEEK_MS,
            contactModificationAttempts = contactAttempts,
            permissionHistory = permissionHistory,
            emergencyInteractions = emergencyInteractions,
            previousAssessments = previousAssessments,
            dataCollectionTimestamp = currentTime
        )
    }
    
    /**
     * Apply rule-based detection logic to identify abuse patterns
     */
    private fun applyDetectionRules(
        behaviorData: CaregiverBehaviorData,
        triggerEvent: AbuseDetectionTrigger?
    ): List<AbuseRiskFactor> {
        
        val riskFactors = mutableListOf<AbuseRiskFactor>()
        
        // Rule 1: Contact Manipulation Patterns
        riskFactors.addAll(analyzeContactManipulationPatterns(behaviorData))
        
        // Rule 2: Permission Escalation Attempts
        riskFactors.addAll(analyzePermissionEscalationPatterns(behaviorData))
        
        // Rule 3: Temporal Behavior Patterns
        riskFactors.addAll(analyzeTemporalPatterns(behaviorData))
        
        // Rule 4: Emergency System Abuse
        riskFactors.addAll(analyzeEmergencySystemAbuse(behaviorData))
        
        // Rule 5: Escalating Behavior Patterns
        riskFactors.addAll(analyzeEscalatingBehavior(behaviorData))
        
        // Rule 6: Trigger Event Analysis
        triggerEvent?.let { event ->
            riskFactors.addAll(analyzeTriggerEvent(event, behaviorData))
        }
        
        return riskFactors
    }
    
    /**
     * Rule 1: Analyze contact manipulation patterns
     */
    private fun analyzeContactManipulationPatterns(
        behaviorData: CaregiverBehaviorData
    ): List<AbuseRiskFactor> {
        
        val riskFactors = mutableListOf<AbuseRiskFactor>()
        val attempts = behaviorData.contactModificationAttempts
        
        // Multiple blocked contact removal attempts
        val blockedRemovalAttempts = attempts.count { 
            it.action.name.contains("REMOVE") && 
            it.result.name == "BLOCKED_BY_PROTECTION" 
        }
        
        if (blockedRemovalAttempts >= 3) {
            riskFactors.add(AbuseRiskFactor(
                factorType = AbuseRiskFactorType.CONTACT_MANIPULATION,
                description = "Multiple attempts to remove contacts (social isolation pattern)",
                score = minOf(blockedRemovalAttempts * 15, 50),
                severity = if (blockedRemovalAttempts >= 5) AbuseSeverity.HIGH else AbuseSeverity.MEDIUM,
                evidence = mapOf(
                    "blocked_removal_attempts" to blockedRemovalAttempts,
                    "pattern" to "social_isolation_attempt"
                )
            ))
        }
        
        // Attempts to block emergency contacts
        val emergencyContactTamperAttempts = attempts.count { 
            it.contactInfo?.relationship?.contains("emergency") == true &&
            it.result.name == "BLOCKED_BY_PROTECTION"
        }
        
        if (emergencyContactTamperAttempts > 0) {
            riskFactors.add(AbuseRiskFactor(
                factorType = AbuseRiskFactorType.EMERGENCY_CONTACT_TAMPERING,
                description = "Attempts to tamper with emergency contacts",
                score = emergencyContactTamperAttempts * 25,
                severity = AbuseSeverity.HIGH,
                evidence = mapOf(
                    "emergency_tamper_attempts" to emergencyContactTamperAttempts,
                    "pattern" to "safety_system_compromise"
                )
            ))
        }
        
        // Rapid succession of blocked attempts (burst pattern)
        val recentAttempts = attempts.filter { 
            System.currentTimeMillis() - it.timestamp < HOUR_MS 
        }
        
        if (recentAttempts.size >= MAX_BLOCKED_ATTEMPTS_PER_HOUR) {
            riskFactors.add(AbuseRiskFactor(
                factorType = AbuseRiskFactorType.BURST_ACTIVITY,
                description = "Rapid succession of blocked attempts (aggressive behavior)",
                score = 30,
                severity = AbuseSeverity.MEDIUM,
                evidence = mapOf(
                    "attempts_in_hour" to recentAttempts.size,
                    "pattern" to "aggressive_burst"
                )
            ))
        }
        
        return riskFactors
    }
    
    /**
     * Rule 2: Analyze permission escalation patterns
     */
    private fun analyzePermissionEscalationPatterns(
        behaviorData: CaregiverBehaviorData
    ): List<AbuseRiskFactor> {
        
        val riskFactors = mutableListOf<AbuseRiskFactor>()
        val permissionHistory = behaviorData.permissionHistory
        
        // Repeated permission escalation requests
        val escalationAttempts = permissionHistory.count { 
            it.actionType.name.contains("REQUEST_PERMISSION") &&
            it.result == "DENIED"
        }
        
        if (escalationAttempts >= MAX_PERMISSION_ESCALATIONS_PER_DAY) {
            riskFactors.add(AbuseRiskFactor(
                factorType = AbuseRiskFactorType.PERMISSION_ESCALATION,
                description = "Repeated attempts to gain additional permissions",
                score = escalationAttempts * 10,
                severity = if (escalationAttempts >= 5) AbuseSeverity.HIGH else AbuseSeverity.MEDIUM,
                evidence = mapOf(
                    "escalation_attempts" to escalationAttempts,
                    "pattern" to "control_escalation"
                )
            ))
        }
        
        // Attempts to gain sensitive permissions
        val sensitivePermissionAttempts = permissionHistory.count { 
            it.permissionChanged in listOf(
                "access_location", "access_contacts", "modify_emergency_settings",
                "disable_panic_mode", "access_call_logs"
            )
        }
        
        if (sensitivePermissionAttempts > 0) {
            riskFactors.add(AbuseRiskFactor(
                factorType = AbuseRiskFactorType.SENSITIVE_PERMISSION_REQUEST,
                description = "Attempts to access sensitive user data or safety features",
                score = sensitivePermissionAttempts * 20,
                severity = AbuseSeverity.HIGH,
                evidence = mapOf(
                    "sensitive_attempts" to sensitivePermissionAttempts,
                    "pattern" to "privacy_invasion_attempt"
                )
            ))
        }
        
        return riskFactors
    }
    
    /**
     * Rule 3: Analyze temporal behavior patterns
     */
    private fun analyzeTemporalPatterns(
        behaviorData: CaregiverBehaviorData
    ): List<AbuseRiskFactor> {
        
        val riskFactors = mutableListOf<AbuseRiskFactor>()
        
        // Night-time activity (11 PM - 6 AM)
        val nightTimeAttempts = behaviorData.contactModificationAttempts.count { attempt ->
            val hour = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(attempt.timestamp),
                java.time.ZoneId.systemDefault()
            ).hour
            hour >= 23 || hour <= 6
        }
        
        if (nightTimeAttempts >= SUSPICIOUS_NIGHT_ACTIVITY_THRESHOLD) {
            riskFactors.add(AbuseRiskFactor(
                factorType = AbuseRiskFactorType.SUSPICIOUS_TIMING,
                description = "Unusual activity during night hours when user likely asleep",
                score = nightTimeAttempts * 8,
                severity = AbuseSeverity.MEDIUM,
                evidence = mapOf(
                    "night_attempts" to nightTimeAttempts,
                    "pattern" to "covert_manipulation"
                )
            ))
        }
        
        // Weekend/holiday activity spikes
        val weekendAttempts = behaviorData.contactModificationAttempts.count { attempt ->
            val dayOfWeek = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(attempt.timestamp),
                java.time.ZoneId.systemDefault()
            ).dayOfWeek
            dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY
        }
        
        val totalAttempts = behaviorData.contactModificationAttempts.size
        if (totalAttempts > 0 && (weekendAttempts.toFloat() / totalAttempts) > 0.6f) {
            riskFactors.add(AbuseRiskFactor(
                factorType = AbuseRiskFactorType.SUSPICIOUS_TIMING,
                description = "High concentration of activity during weekends (when user may be isolated)",
                score = 20,
                severity = AbuseSeverity.LOW,
                evidence = mapOf(
                    "weekend_concentration" to (weekendAttempts.toFloat() / totalAttempts),
                    "pattern" to "isolation_exploitation"
                )
            ))
        }
        
        return riskFactors
    }
    
    /**
     * Rule 4: Analyze emergency system abuse
     */
    private fun analyzeEmergencySystemAbuse(
        behaviorData: CaregiverBehaviorData
    ): List<AbuseRiskFactor> {
        
        val riskFactors = mutableListOf<AbuseRiskFactor>()
        val emergencyInteractions = behaviorData.emergencyInteractions
        
        // Attempts to disable or modify emergency features
        val emergencyDisableAttempts = emergencyInteractions.count { 
            it.actionType == "DISABLE_EMERGENCY_BUTTON" || 
            it.actionType == "MODIFY_EMERGENCY_CONTACTS"
        }
        
        if (emergencyDisableAttempts > 0) {
            riskFactors.add(AbuseRiskFactor(
                factorType = AbuseRiskFactorType.SAFETY_SYSTEM_TAMPERING,
                description = "Attempts to disable or modify emergency safety features",
                score = emergencyDisableAttempts * 40,
                severity = AbuseSeverity.CRITICAL,
                evidence = mapOf(
                    "disable_attempts" to emergencyDisableAttempts,
                    "pattern" to "safety_compromise"
                )
            ))
        }
        
        // Excessive emergency system queries (surveillance)
        val emergencyQueries = emergencyInteractions.count { 
            it.actionType == "QUERY_EMERGENCY_STATUS" 
        }
        
        if (emergencyQueries > 20) { // More than ~3 per day
            riskFactors.add(AbuseRiskFactor(
                factorType = AbuseRiskFactorType.SURVEILLANCE_PATTERN,
                description = "Excessive monitoring of emergency system status",
                score = 15,
                severity = AbuseSeverity.LOW,
                evidence = mapOf(
                    "query_count" to emergencyQueries,
                    "pattern" to "excessive_surveillance"
                )
            ))
        }
        
        return riskFactors
    }
    
    /**
     * Rule 5: Analyze escalating behavior patterns
     */
    private fun analyzeEscalatingBehavior(
        behaviorData: CaregiverBehaviorData
    ): List<AbuseRiskFactor> {
        
        val riskFactors = mutableListOf<AbuseRiskFactor>()
        val previousAssessments = behaviorData.previousAssessments
        
        if (previousAssessments.size >= 2) {
            // Check if risk scores are increasing over time
            val sortedAssessments = previousAssessments.sortedBy { it.assessmentTimestamp }
            val recentScores = sortedAssessments.takeLast(3).map { it.riskScore }
            
            var isEscalating = true
            for (i in 1 until recentScores.size) {
                if (recentScores[i] <= recentScores[i-1]) {
                    isEscalating = false
                    break
                }
            }
            
            if (isEscalating && recentScores.last() > recentScores.first() + 20) {
                riskFactors.add(AbuseRiskFactor(
                    factorType = AbuseRiskFactorType.ESCALATING_BEHAVIOR,
                    description = "Behavior patterns show escalating risk over time",
                    score = 25,
                    severity = AbuseSeverity.HIGH,
                    evidence = mapOf(
                        "score_trend" to recentScores,
                        "pattern" to "escalating_abuse"
                    )
                ))
            }
        }
        
        return riskFactors
    }
    
    /**
     * Rule 6: Analyze specific trigger events
     */
    private fun analyzeTriggerEvent(
        triggerEvent: AbuseDetectionTrigger,
        behaviorData: CaregiverBehaviorData
    ): List<AbuseRiskFactor> {
        
        val riskFactors = mutableListOf<AbuseRiskFactor>()
        
        when (triggerEvent.eventType) {
            TriggerEventType.MULTIPLE_BLOCKED_ATTEMPTS -> {
                riskFactors.add(AbuseRiskFactor(
                    factorType = AbuseRiskFactorType.TRIGGER_EVENT,
                    description = "Triggered by multiple blocked attempts in short timeframe",
                    score = 20,
                    severity = AbuseSeverity.MEDIUM,
                    evidence = mapOf(
                        "trigger_event" to triggerEvent.eventType.name,
                        "event_data" to triggerEvent.eventData
                    )
                ))
            }
            
            TriggerEventType.EMERGENCY_CONTACT_TAMPERING -> {
                riskFactors.add(AbuseRiskFactor(
                    factorType = AbuseRiskFactorType.TRIGGER_EVENT,
                    description = "Triggered by attempt to tamper with emergency contacts",
                    score = 40,
                    severity = AbuseSeverity.HIGH,
                    evidence = mapOf(
                        "trigger_event" to triggerEvent.eventType.name,
                        "event_data" to triggerEvent.eventData
                    )
                ))
            }
            
            TriggerEventType.PANIC_MODE_ACTIVATION -> {
                riskFactors.add(AbuseRiskFactor(
                    factorType = AbuseRiskFactorType.TRIGGER_EVENT,
                    description = "Analysis triggered by user panic mode activation",
                    score = 30,
                    severity = AbuseSeverity.HIGH,
                    evidence = mapOf(
                        "trigger_event" to triggerEvent.eventType.name,
                        "panic_context" to triggerEvent.eventData
                    )
                ))
            }
        }
        
        return riskFactors
    }
    
    /**
     * Determine risk level based on total score
     */
    private fun determineRiskLevel(totalScore: Int): AbuseRiskLevel {
        return when {
            totalScore >= CRITICAL_RISK_THRESHOLD -> AbuseRiskLevel.CRITICAL
            totalScore >= HIGH_RISK_THRESHOLD -> AbuseRiskLevel.HIGH
            totalScore >= MEDIUM_RISK_THRESHOLD -> AbuseRiskLevel.MEDIUM
            totalScore >= LOW_RISK_THRESHOLD -> AbuseRiskLevel.LOW
            else -> AbuseRiskLevel.MINIMAL
        }
    }
    
    /**
     * Generate abuse alert based on risk assessment
     */
    private suspend fun generateAbuseAlert(assessment: AbuseRiskAssessment) {
        val alert = AbuseAlert(
            alertId = java.util.UUID.randomUUID().toString(),
            caregiverId = assessment.caregiverId,
            userId = assessment.userId,
            riskLevel = assessment.riskLevel,
            alertType = determineAlertType(assessment),
            alertMessage = generateAlertMessage(assessment),
            riskFactors = assessment.riskFactors,
            recommendedActions = generateRecommendedActions(assessment),
            alertTimestamp = System.currentTimeMillis(),
            requiresImmedateAction = assessment.riskLevel >= AbuseRiskLevel.HIGH
        )
        
        // Store alert
        abuseDao.insertAbuseAlert(alert)
        
        // Update recent alerts
        val recentAlerts = _recentAlerts.value.toMutableList()
        recentAlerts.add(0, alert) // Add to beginning
        if (recentAlerts.size > 10) {
            recentAlerts.removeAt(recentAlerts.size - 1) // Keep only 10 most recent
        }
        _recentAlerts.value = recentAlerts
        
        // Trigger alert notifications
        triggerAlertNotifications(alert)
    }
    
    /**
     * Determine appropriate alert type based on risk factors
     */
    private fun determineAlertType(assessment: AbuseRiskAssessment): AbuseAlertType {
        val riskFactorTypes = assessment.riskFactors.map { it.factorType }.toSet()
        
        return when {
            AbuseRiskFactorType.SAFETY_SYSTEM_TAMPERING in riskFactorTypes -> AbuseAlertType.SAFETY_COMPROMISE
            AbuseRiskFactorType.EMERGENCY_CONTACT_TAMPERING in riskFactorTypes -> AbuseAlertType.EMERGENCY_SYSTEM_ABUSE
            AbuseRiskFactorType.CONTACT_MANIPULATION in riskFactorTypes -> AbuseAlertType.SOCIAL_ISOLATION_ATTEMPT
            AbuseRiskFactorType.ESCALATING_BEHAVIOR in riskFactorTypes -> AbuseAlertType.ESCALATING_ABUSE_PATTERN
            else -> AbuseAlertType.GENERAL_ABUSE_CONCERN
        }
    }
    
    /**
     * Generate human-readable alert message
     */
    private fun generateAlertMessage(assessment: AbuseRiskAssessment): String {
        val primaryRiskFactor = assessment.riskFactors.maxByOrNull { it.score }
        
        return when (assessment.riskLevel) {
            AbuseRiskLevel.CRITICAL -> "CRITICAL: Immediate intervention required. ${primaryRiskFactor?.description}"
            AbuseRiskLevel.HIGH -> "HIGH RISK: Concerning behavior patterns detected. ${primaryRiskFactor?.description}"
            AbuseRiskLevel.MEDIUM -> "MEDIUM RISK: Potentially problematic behavior. ${primaryRiskFactor?.description}"
            AbuseRiskLevel.LOW -> "LOW RISK: Minor concerning patterns. ${primaryRiskFactor?.description}"
            else -> "Monitoring caregiver behavior patterns."
        }
    }
    
    /**
     * Generate recommended actions based on risk assessment
     */
    private fun generateRecommendedActions(assessment: AbuseRiskAssessment): List<String> {
        val actions = mutableListOf<String>()
        
        when (assessment.riskLevel) {
            AbuseRiskLevel.CRITICAL -> {
                actions.add("Contact elder rights advocate immediately")
                actions.add("Consider temporary restriction of caregiver permissions")
                actions.add("Document all evidence for potential legal action")
                actions.add("Ensure user safety and access to help")
            }
            
            AbuseRiskLevel.HIGH -> {
                actions.add("Notify elder rights advocate")
                actions.add("Increase monitoring frequency")
                actions.add("Review and potentially restrict caregiver permissions")
                actions.add("Schedule wellness check with user")
            }
            
            AbuseRiskLevel.MEDIUM -> {
                actions.add("Monitor caregiver behavior more closely")
                actions.add("Consider user education about warning signs")
                actions.add("Review caregiver permission levels")
                actions.add("Schedule routine check-in with user")
            }
            
            AbuseRiskLevel.LOW -> {
                actions.add("Continue routine monitoring")
                actions.add("Log patterns for trend analysis")
                actions.add("Consider caregiver education resources")
            }
            
            else -> {
                actions.add("Continue standard monitoring")
            }
        }
        
        return actions
    }
    
    /**
     * Trigger appropriate notifications based on alert level
     */
    private suspend fun triggerAlertNotifications(alert: AbuseAlert) {
        when (alert.riskLevel) {
            AbuseRiskLevel.CRITICAL, AbuseRiskLevel.HIGH -> {
                // Notify elder rights advocate
                emergencyService.notifyElderRightsAdvocate(
                    userId = alert.userId,
                    alertType = alert.alertType,
                    message = alert.alertMessage,
                    urgency = if (alert.riskLevel == AbuseRiskLevel.CRITICAL) "IMMEDIATE" else "HIGH"
                )
                
                // Notify user (if safe to do so)
                if (alert.alertType != AbuseAlertType.SAFETY_COMPROMISE) {
                    emergencyService.notifyUserOfConcern(
                        userId = alert.userId,
                        message = "We've detected some concerning patterns and want to ensure your safety. Please contact your elder rights advocate if you need help."
                    )
                }
            }
            
            AbuseRiskLevel.MEDIUM -> {
                // Schedule delayed notification to elder rights advocate
                emergencyService.scheduleElderRightsNotification(
                    userId = alert.userId,
                    alertType = alert.alertType,
                    message = alert.alertMessage,
                    delayHours = 24
                )
            }
            
            else -> {
                // Log only, no immediate notifications
            }
        }
    }
    
    /**
     * Get current risk assessment for a caregiver
     */
    fun getCurrentRiskAssessment(caregiverId: String): AbuseRiskAssessment? {
        return _currentRiskAssessments.value[caregiverId]
    }
    
    /**
     * Get recent alerts for monitoring
     */
    fun getRecentAlerts(): List<AbuseAlert> {
        return _recentAlerts.value
    }
    
    /**
     * Manual trigger for abuse analysis (e.g., from panic mode)
     */
    suspend fun triggerManualAnalysis(
        caregiverId: String,
        userId: String,
        triggerReason: String
    ): AbuseRiskAssessment {
        val trigger = AbuseDetectionTrigger(
            eventType = TriggerEventType.MANUAL_TRIGGER,
            eventData = mapOf("reason" to triggerReason),
            timestamp = System.currentTimeMillis()
        )
        
        return analyzeCaregiver(caregiverId, userId, trigger)
    }
}
