package com.naviya.launcher.abuse.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import java.util.UUID

/**
 * Abuse risk assessment result from rule-based analysis
 */
@Entity(tableName = "abuse_risk_assessments")
data class AbuseRiskAssessment(
    @PrimaryKey
    val assessmentId: String = UUID.randomUUID().toString(),
    
    val caregiverId: String,
    val userId: String,
    
    // Risk analysis results
    val riskScore: Int,
    val riskLevel: AbuseRiskLevel,
    val riskFactors: List<AbuseRiskFactor>,
    
    // Context
    val triggerEvent: AbuseDetectionTrigger? = null,
    val assessmentTimestamp: Long = System.currentTimeMillis(),
    
    // Supporting data
    val behaviorData: CaregiverBehaviorData,
    
    // Analysis metadata
    val analysisVersion: String = "1.0",
    val rulesApplied: List<String> = emptyList(),
    
    // Follow-up tracking
    val actionsTaken: List<String> = emptyList(),
    val followUpRequired: Boolean = false,
    val followUpDate: Long? = null,
    
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Individual risk factor identified by rule-based analysis
 */
data class AbuseRiskFactor(
    val factorId: String = UUID.randomUUID().toString(),
    val factorType: AbuseRiskFactorType,
    val description: String,
    val score: Int, // Contribution to overall risk score
    val severity: AbuseSeverity,
    val evidence: Map<String, Any> = emptyMap(),
    val confidence: Float = 1.0f, // 0.0 - 1.0
    val detectionRule: String = "rule_based_v1"
)

/**
 * Caregiver behavior data collected for analysis
 */
data class CaregiverBehaviorData(
    val caregiverId: String,
    val userId: String,
    val analysisTimeWindow: Long, // Time window in milliseconds
    
    // Contact-related behavior
    val contactModificationAttempts: List<ContactModificationAttempt>,
    val contactAccessPatterns: List<ContactAccessPattern> = emptyList(),
    
    // Permission-related behavior
    val permissionHistory: List<PermissionHistoryEntry>,
    val permissionEscalationAttempts: Int = 0,
    
    // Emergency system interactions
    val emergencyInteractions: List<EmergencyInteraction>,
    val panicModeInteractions: List<PanicModeInteraction> = emptyList(),
    
    // Temporal patterns
    val activityByHour: Map<Int, Int> = emptyMap(), // Hour -> activity count
    val activityByDayOfWeek: Map<Int, Int> = emptyMap(), // Day -> activity count
    
    // Historical context
    val previousAssessments: List<AbuseRiskAssessment>,
    val userComplaintHistory: List<UserComplaint> = emptyList(),
    
    // Metadata
    val dataCollectionTimestamp: Long = System.currentTimeMillis(),
    val dataQuality: DataQuality = DataQuality.COMPLETE
)

/**
 * Abuse alert generated from risk assessment
 */
@Entity(tableName = "abuse_alerts")
data class AbuseAlert(
    @PrimaryKey
    val alertId: String = UUID.randomUUID().toString(),
    
    val caregiverId: String,
    val userId: String,
    
    // Alert details
    val riskLevel: AbuseRiskLevel,
    val alertType: AbuseAlertType,
    val alertMessage: String,
    val riskFactors: List<AbuseRiskFactor>,
    
    // Response information
    val recommendedActions: List<String>,
    val requiresImmedateAction: Boolean = false,
    val escalationLevel: EscalationLevel = EscalationLevel.STANDARD,
    
    // Status tracking
    val alertStatus: AlertStatus = AlertStatus.ACTIVE,
    val acknowledgedBy: String? = null,
    val acknowledgedAt: Long? = null,
    val resolvedBy: String? = null,
    val resolvedAt: Long? = null,
    val resolutionNotes: String? = null,
    
    // Notifications
    val notificationsSent: List<NotificationRecord> = emptyList(),
    val elderRightsNotified: Boolean = false,
    val elderRightsNotificationTime: Long? = null,
    
    val alertTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Trigger event that initiated abuse detection analysis
 */
data class AbuseDetectionTrigger(
    val triggerId: String = UUID.randomUUID().toString(),
    val eventType: TriggerEventType,
    val eventData: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val severity: TriggerSeverity = TriggerSeverity.MEDIUM,
    val autoTriggered: Boolean = true
)

/**
 * Contact access pattern for behavioral analysis
 */
data class ContactAccessPattern(
    val contactId: String,
    val accessType: String, // view, edit, call, message
    val accessCount: Int,
    val lastAccessTime: Long,
    val accessFrequency: AccessFrequency,
    val suspiciousPatterns: List<String> = emptyList()
)

/**
 * Permission history entry for analysis
 */
data class PermissionHistoryEntry(
    val entryId: String = UUID.randomUUID().toString(),
    val actionType: PermissionActionType,
    val permissionChanged: String,
    val oldValue: String,
    val newValue: String,
    val result: String, // GRANTED, DENIED, REVOKED
    val requestReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Emergency system interaction for analysis
 */
data class EmergencyInteraction(
    val interactionId: String = UUID.randomUUID().toString(),
    val actionType: String, // QUERY_STATUS, MODIFY_CONTACTS, DISABLE_BUTTON, etc.
    val actionResult: String, // SUCCESS, BLOCKED, ERROR
    val actionContext: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Panic mode interaction for analysis
 */
data class PanicModeInteraction(
    val interactionId: String = UUID.randomUUID().toString(),
    val actionType: String, // QUERY_STATUS, ATTEMPT_DISABLE, VIEW_LOGS
    val blocked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * User complaint for historical context
 */
data class UserComplaint(
    val complaintId: String = UUID.randomUUID().toString(),
    val complaintType: String, // harassment, privacy_violation, control_abuse
    val description: String,
    val severity: ComplaintSeverity,
    val status: ComplaintStatus,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Notification record for alert tracking
 */
data class NotificationRecord(
    val notificationId: String = UUID.randomUUID().toString(),
    val recipient: String, // elder_rights_advocate, user, family_member
    val notificationType: String, // email, sms, app_notification
    val notificationStatus: String, // sent, delivered, failed
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Abuse detection rule configuration
 */
@Entity(tableName = "abuse_detection_rules")
data class AbuseDetectionRule(
    @PrimaryKey
    val ruleId: String = UUID.randomUUID().toString(),
    
    val ruleName: String,
    val ruleDescription: String,
    val ruleType: AbuseRiskFactorType,
    
    // Rule parameters
    val thresholds: Map<String, Int> = emptyMap(),
    val timeWindows: Map<String, Long> = emptyMap(),
    val scoreWeights: Map<String, Int> = emptyMap(),
    
    // Rule status
    val isActive: Boolean = true,
    val severity: AbuseSeverity = AbuseSeverity.MEDIUM,
    val confidence: Float = 1.0f,
    
    // Metadata
    val ruleVersion: String = "1.0",
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdBy: String = "system",
    
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Abuse detection statistics for monitoring system performance
 */
@Entity(tableName = "abuse_detection_stats")
data class AbuseDetectionStats(
    @PrimaryKey
    val statsId: String = UUID.randomUUID().toString(),
    
    val userId: String? = null, // null for system-wide stats
    val caregiverId: String? = null,
    
    // Time period
    val periodStart: Long,
    val periodEnd: Long,
    val periodType: String, // daily, weekly, monthly
    
    // Detection statistics
    val totalAnalyses: Int = 0,
    val alertsGenerated: Int = 0,
    val alertsByLevel: Map<String, Int> = emptyMap(),
    val falsePositives: Int = 0,
    val confirmedAbuseCases: Int = 0,
    
    // Rule performance
    val ruleEffectiveness: Map<String, Float> = emptyMap(),
    val mostTriggeredRules: List<String> = emptyList(),
    
    // Response statistics
    val averageResponseTime: Long = 0,
    val elderRightsNotifications: Int = 0,
    val userInterventions: Int = 0,
    
    val recordedAt: Long = System.currentTimeMillis()
)

/**
 * Enums for abuse detection system
 */
enum class AbuseRiskLevel {
    MINIMAL,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class AbuseRiskFactorType {
    CONTACT_MANIPULATION,
    EMERGENCY_CONTACT_TAMPERING,
    PERMISSION_ESCALATION,
    SENSITIVE_PERMISSION_REQUEST,
    BURST_ACTIVITY,
    SUSPICIOUS_TIMING,
    SURVEILLANCE_PATTERN,
    SAFETY_SYSTEM_TAMPERING,
    ESCALATING_BEHAVIOR,
    TRIGGER_EVENT
}

enum class AbuseSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class AbuseAlertType {
    GENERAL_ABUSE_CONCERN,
    SOCIAL_ISOLATION_ATTEMPT,
    EMERGENCY_SYSTEM_ABUSE,
    SAFETY_COMPROMISE,
    ESCALATING_ABUSE_PATTERN,
    SURVEILLANCE_ABUSE,
    PERMISSION_ABUSE
}

enum class TriggerEventType {
    MULTIPLE_BLOCKED_ATTEMPTS,
    EMERGENCY_CONTACT_TAMPERING,
    PANIC_MODE_ACTIVATION,
    PERMISSION_ESCALATION,
    MANUAL_TRIGGER,
    SCHEDULED_ANALYSIS,
    USER_COMPLAINT
}

enum class TriggerSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    INVESTIGATING,
    RESOLVED,
    FALSE_POSITIVE,
    ESCALATED
}

enum class EscalationLevel {
    STANDARD,
    URGENT,
    EMERGENCY,
    LEGAL_INTERVENTION
}

enum class AccessFrequency {
    RARE,
    OCCASIONAL,
    FREQUENT,
    EXCESSIVE
}

enum class PermissionActionType {
    REQUEST_PERMISSION,
    GRANT_PERMISSION,
    REVOKE_PERMISSION,
    ESCALATE_PERMISSION,
    QUERY_PERMISSION
}

enum class ComplaintSeverity {
    MINOR,
    MODERATE,
    SERIOUS,
    SEVERE
}

enum class ComplaintStatus {
    REPORTED,
    INVESTIGATING,
    SUBSTANTIATED,
    UNSUBSTANTIATED,
    RESOLVED
}

enum class DataQuality {
    COMPLETE,
    PARTIAL,
    LIMITED,
    INSUFFICIENT
}

/**
 * Contact modification attempt (imported from contacts package)
 */
data class ContactModificationAttempt(
    val attemptId: String,
    val caregiverId: String?,
    val userId: String,
    val action: ContactAction,
    val result: ContactActionResult,
    val contactInfo: ContactInfo? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val flaggedAsAbusive: Boolean = false
)

/**
 * Contact action enum (imported from contacts package)
 */
enum class ContactAction {
    ADD_CONTACT,
    REMOVE_CONTACT,
    BLOCK_CONTACT,
    UNBLOCK_CONTACT,
    MODIFY_CONTACT,
    VIEW_CONTACT
}

/**
 * Contact action result enum (imported from contacts package)
 */
enum class ContactActionResult {
    SUCCESS,
    BLOCKED_BY_PROTECTION,
    PERMISSION_DENIED,
    PENDING_USER_APPROVAL,
    USER_APPROVED,
    USER_REJECTED,
    ERROR
}

/**
 * Contact info (imported from contacts package)
 */
data class ContactInfo(
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val email: String? = null
)
