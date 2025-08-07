package com.naviya.launcher.elderrights.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Data models for elder rights advocate notification system
 * Supports multiple communication channels and escalation procedures
 */

@Entity(tableName = "elder_rights_notifications")
// // @TypeConverters(ElderRightsConverters::class)
data class ElderRightsNotification(
    @PrimaryKey
    val notificationId: String,
    val userId: String,
    val alertId: String,
    val advocateContact: ElderRightsAdvocateContact,
    val message: String,
    val priority: NotificationPriority,
    val timestamp: Long,
    val channels: List<ChannelResult>,
    val status: NotificationStatus,
    val escalationLevel: Int = 0,
    val followUpRequired: Boolean = false,
    val followUpTimestamp: Long? = null,
    val resolutionDetails: String? = null,
    val resolutionTimestamp: Long? = null
)

/**
 * Elder rights advocate contact information
 */
data class ElderRightsAdvocateContact(
    val advocateId: String = "advocate-${System.nanoTime()}",
    val name: String,
    val phoneNumber: String,
    val email: String,
    val organization: String,
    val specialization: String = "Elder Abuse Prevention",
    val availability: AdvocateAvailability = AdvocateAvailability.BUSINESS_HOURS,
    val preferredContactMethod: NotificationChannel = NotificationChannel.PHONE_CALL,
    val emergencyContact: Boolean = false,
    val certifications: List<String> = emptyList(),
    val languages: List<String> = listOf("English"),
    val serviceArea: String = "National"
)

/**
 * Result of notification attempt through specific channel
 */
data class ChannelResult(
    val channel: NotificationChannel,
    val success: Boolean,
    val message: String,
    val timestamp: Long,
    val retryCount: Int = 0,
    val deliveryConfirmation: String? = null,
    val responseReceived: Boolean = false,
    val responseTimestamp: Long? = null
)

/**
 * Notification result summary
 */
data class ElderRightsNotificationResult(
    val success: Boolean,
    val notificationId: String?,
    val channelsUsed: List<NotificationChannel>,
    val message: String,
    val escalationTriggered: Boolean = false,
    val followUpScheduled: Boolean = false,
    val estimatedResponseTime: Long? = null
)

/**
 * Escalation procedure configuration
 */
@Entity(tableName = "escalation_procedures")
data class EscalationProcedure(
    @PrimaryKey
    val procedureId: String,
    val userId: String,
    val triggerConditions: List<EscalationTrigger>,
    val escalationSteps: List<EscalationStep>,
    val isActive: Boolean = true,
    val createdTimestamp: Long,
    val lastModifiedTimestamp: Long
)

/**
 * Individual escalation step
 */
data class EscalationStep(
    val stepNumber: Int,
    val description: String,
    val triggerDelay: Long, // Milliseconds to wait before this step
    val actions: List<EscalationAction>,
    val requiredConfirmation: Boolean = false,
    val autoExecute: Boolean = true
)

/**
 * Escalation action to take
 */
data class EscalationAction(
    val actionType: EscalationActionType,
    val target: String, // Phone number, email, etc.
    val message: String,
    val priority: NotificationPriority
)

/**
 * Trigger condition for escalation
 */
data class EscalationTrigger(
    val triggerType: EscalationTriggerType,
    val threshold: Int,
    val timeWindow: Long, // Milliseconds
    val conditions: Map<String, String> = emptyMap()
)

/**
 * Follow-up tracking for notifications
 */
@Entity(tableName = "notification_followups")
data class NotificationFollowUp(
    @PrimaryKey
    val followUpId: String,
    val notificationId: String,
    val userId: String,
    val advocateId: String,
    val scheduledTimestamp: Long,
    val followUpType: FollowUpType,
    val status: FollowUpStatus,
    val attempts: Int = 0,
    val lastAttemptTimestamp: Long? = null,
    val completedTimestamp: Long? = null,
    val notes: String? = null
)

/**
 * Advocate response tracking
 */
@Entity(tableName = "advocate_responses")
data class AdvocateResponse(
    @PrimaryKey
    val responseId: String,
    val notificationId: String,
    val advocateId: String,
    val responseChannel: NotificationChannel,
    val responseTimestamp: Long,
    val responseType: ResponseType,
    val responseContent: String,
    val actionTaken: String? = null,
    val followUpRequired: Boolean = false,
    val caseStatus: CaseStatus
)

/**
 * Emergency contact registry
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey
    val contactId: String,
    val userId: String,
    val contactType: EmergencyContactType,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val relationship: String,
    val priority: Int, // 1 = highest priority
    val availability: ContactAvailability,
    val specialInstructions: String? = null,
    val isActive: Boolean = true,
    val lastContactedTimestamp: Long? = null,
    val responseHistory: List<ContactResponse> = emptyList()
)

/**
 * Contact response history
 */
data class ContactResponse(
    val timestamp: Long,
    val responseTime: Long, // Time to respond in milliseconds
    val successful: Boolean,
    val notes: String? = null
)

/**
 * Notification statistics and analytics
 */
@Entity(tableName = "notification_statistics")
data class NotificationStatistics(
    @PrimaryKey
    val statisticsId: String,
    val userId: String,
    val timeWindow: Long, // Milliseconds
    val totalNotifications: Int,
    val successfulNotifications: Int,
    val failedNotifications: Int,
    val averageResponseTime: Long,
    val channelEffectiveness: Map<NotificationChannel, Double>,
    val escalationRate: Double,
    val resolutionRate: Double,
    val generatedTimestamp: Long
)

// ==================== ENUMS ====================

enum class NotificationPriority {
    LOW,        // Routine monitoring alert
    MEDIUM,     // Concerning behavior detected
    HIGH,       // Significant abuse pattern identified
    IMMEDIATE   // Critical situation requiring immediate intervention
}

enum class NotificationStatus {
    PENDING,    // Notification being sent
    SENT,       // Successfully sent through at least one channel
    FAILED,     // All channels failed
    DELIVERED,  // Delivery confirmed
    RESPONDED,  // Advocate responded
    RESOLVED    // Issue resolved
}

enum class NotificationChannel {
    PHONE_CALL,         // Direct phone call
    SMS,                // Text message
    EMAIL,              // Email notification
    PUSH_NOTIFICATION,  // App push notification
    EMERGENCY_HOTLINE,  // National elder abuse hotline
    BACKUP_HOTLINE,     // Secondary hotline
    EMERGENCY_SERVICES, // 911/Emergency services
    IN_APP_MESSAGE,     // In-app messaging system
    POSTAL_MAIL         // Physical mail (for severe cases)
}

enum class AdvocateAvailability {
    ALWAYS,         // 24/7 availability
    BUSINESS_HOURS, // 9 AM - 5 PM weekdays
    EXTENDED_HOURS, // 8 AM - 8 PM weekdays
    EMERGENCY_ONLY, // Only for critical situations
    WEEKDAYS_ONLY   // Monday - Friday only
}

enum class EscalationTriggerType {
    MULTIPLE_CRITICAL_ALERTS,   // Multiple critical alerts in time window
    NO_RESPONSE_TIMEOUT,        // No response from advocate within time limit
    REPEATED_ABUSE_PATTERN,     // Same abuse pattern recurring
    EMERGENCY_SYSTEM_COMPROMISE, // Emergency systems being tampered with
    USER_PANIC_ACTIVATION,      // User activated panic mode
    CAREGIVER_ESCALATION        // Caregiver behavior escalating
}

enum class EscalationActionType {
    CONTACT_BACKUP_ADVOCATE,    // Contact secondary advocate
    CONTACT_EMERGENCY_SERVICES, // Call 911
    CONTACT_ADULT_PROTECTIVE,   // Contact Adult Protective Services
    CONTACT_FAMILY_MEMBER,      // Contact designated family member
    ACTIVATE_EMERGENCY_PROTOCOL, // Activate full emergency response
    DOCUMENT_FOR_LEGAL_ACTION   // Prepare legal documentation
}

enum class FollowUpType {
    WELLNESS_CHECK,     // Check on user's wellbeing
    RESPONSE_CONFIRMATION, // Confirm advocate received notification
    CASE_STATUS_UPDATE, // Update on case progress
    SYSTEM_TEST,        // Test notification system
    SCHEDULED_CONTACT   // Regularly scheduled contact
}

enum class FollowUpStatus {
    SCHEDULED,  // Follow-up scheduled
    IN_PROGRESS, // Follow-up in progress
    COMPLETED,  // Follow-up completed successfully
    FAILED,     // Follow-up failed
    CANCELLED   // Follow-up cancelled
}

enum class ResponseType {
    ACKNOWLEDGMENT,     // Advocate acknowledged alert
    ACTION_TAKEN,       // Advocate took specific action
    ESCALATION_REQUEST, // Advocate requests escalation
    CASE_CLOSED,        // Advocate closed the case
    MORE_INFO_NEEDED,   // Advocate needs more information
    REFERRAL_MADE       // Advocate made referral to other services
}

enum class CaseStatus {
    OPEN,           // Case is active
    INVESTIGATING,  // Under investigation
    ACTION_TAKEN,   // Action has been taken
    MONITORING,     // Ongoing monitoring
    RESOLVED,       // Case resolved
    CLOSED,         // Case closed
    REFERRED        // Referred to other agency
}

enum class EmergencyContactType {
    ELDER_RIGHTS_ADVOCATE,  // Primary elder rights advocate
    BACKUP_ADVOCATE,        // Secondary advocate
    FAMILY_MEMBER,          // Trusted family member
    FRIEND,                 // Trusted friend
    HEALTHCARE_PROVIDER,    // Doctor, nurse, etc.
    LEGAL_REPRESENTATIVE,   // Lawyer, legal aid
    SOCIAL_WORKER,          // Social services
    EMERGENCY_SERVICES      // 911, police, fire, medical
}

enum class ContactAvailability {
    ALWAYS_AVAILABLE,   // 24/7 availability
    BUSINESS_HOURS,     // Standard business hours
    EVENINGS_WEEKENDS,  // After hours and weekends
    EMERGENCY_ONLY,     // Only for emergencies
    LIMITED             // Limited availability
}

// ==================== TYPE CONVERTERS ====================

class ElderRightsConverters {
    private val gson = Gson()

    // @TypeConverter
    fun fromAdvocateContact(contact: ElderRightsAdvocateContact): String {
        return gson.toJson(contact)
    }

    // @TypeConverter
    fun toAdvocateContact(contactString: String): ElderRightsAdvocateContact {
        return gson.fromJson(contactString, ElderRightsAdvocateContact::class.java)
    }

    // @TypeConverter
    fun fromChannelResultList(channels: List<ChannelResult>): String {
        return gson.toJson(channels)
    }

    // @TypeConverter
    fun toChannelResultList(channelsString: String): List<ChannelResult> {
        val listType = object : TypeToken<List<ChannelResult>>() {}.type
        return gson.fromJson(channelsString, listType)
    }

    // @TypeConverter
    fun fromEscalationTriggerList(triggers: List<EscalationTrigger>): String {
        return gson.toJson(triggers)
    }

    // @TypeConverter
    fun toEscalationTriggerList(triggersString: String): List<EscalationTrigger> {
        val listType = object : TypeToken<List<EscalationTrigger>>() {}.type
        return gson.fromJson(triggersString, listType)
    }

    // @TypeConverter
    fun fromEscalationStepList(steps: List<EscalationStep>): String {
        return gson.toJson(steps)
    }

    // @TypeConverter
    fun toEscalationStepList(stepsString: String): List<EscalationStep> {
        val listType = object : TypeToken<List<EscalationStep>>() {}.type
        return gson.fromJson(stepsString, listType)
    }

    // @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return gson.toJson(strings)
    }

    // @TypeConverter
    fun toStringList(stringsString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringsString, listType)
    }

    // @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    // @TypeConverter
    fun toStringMap(mapString: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(mapString, mapType)
    }

    // @TypeConverter
    fun fromChannelEffectivenessMap(map: Map<NotificationChannel, Double>): String {
        return gson.toJson(map)
    }

    // @TypeConverter
    fun toChannelEffectivenessMap(mapString: String): Map<NotificationChannel, Double> {
        val mapType = object : TypeToken<Map<NotificationChannel, Double>>() {}.type
        return gson.fromJson(mapString, mapType)
    }

    // @TypeConverter
    fun fromContactResponseList(responses: List<ContactResponse>): String {
        return gson.toJson(responses)
    }

    // @TypeConverter
    fun toContactResponseList(responsesString: String): List<ContactResponse> {
        val listType = object : TypeToken<List<ContactResponse>>() {}.type
        return gson.fromJson(responsesString, listType)
    }
}
