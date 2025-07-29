package com.naviya.launcher.contacts.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import java.util.UUID

/**
 * Protected contact entity - prevents caregiver abuse through contact manipulation
 */
@Entity(tableName = "protected_contacts")
data class ProtectedContact(
    @PrimaryKey
    val contactId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    
    @Embedded
    val contactInfo: ContactInfo,
    
    // Contact classification
    val contactType: String, // emergency, elder_rights_advocate, family, medical, user_added
    val protectionLevel: ProtectionLevel,
    
    // Audit trail
    val addedBy: String, // userId or caregiverId who added this contact
    val addedTimestamp: Long = System.currentTimeMillis(),
    val lastModifiedBy: String? = null,
    val lastModifiedTimestamp: Long? = null,
    
    // Protection flags
    val isProtected: Boolean = true,
    val canBeRemovedByCaregiver: Boolean = false,
    val canBeModifiedByCaregiver: Boolean = false,
    val requiresUserApprovalForChanges: Boolean = true,
    
    // User consent tracking
    val userApproved: Boolean = false,
    val approvalTimestamp: Long? = null,
    val approvalWitnessId: String? = null,
    
    // System flags
    val systemContact: Boolean = false, // Elder rights advocate, emergency services
    val emergencyContact: Boolean = false,
    val primaryEmergencyContact: Boolean = false,
    
    // Status
    val isActive: Boolean = true,
    val isBlocked: Boolean = false,
    val blockedBy: String? = null,
    val blockedTimestamp: Long? = null,
    
    // Metadata
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Contact information embedded in protected contacts
 */
data class ContactInfo(
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val relationship: String,
    val address: String? = null,
    val notes: String? = null,
    val photoUri: String? = null
)

/**
 * Pending contact requests from caregivers
 */
@Entity(tableName = "pending_contact_requests")
data class PendingContactRequest(
    @PrimaryKey
    val requestId: String = UUID.randomUUID().toString(),
    
    val caregiverId: String,
    val userId: String,
    
    @Embedded
    val contactInfo: ContactInfo,
    
    // Request details
    val requestType: ContactRequestType,
    val requestReason: String? = null,
    val requestMessage: String? = null,
    val requestTimestamp: Long = System.currentTimeMillis(),
    
    // Status tracking
    val status: RequestStatus = RequestStatus.PENDING_USER_APPROVAL,
    val priority: RequestPriority = RequestPriority.NORMAL,
    
    // User response
    val userResponse: Boolean? = null,
    val userNote: String? = null,
    val responseTimestamp: Long? = null,
    
    // Expiration
    val expiresAt: Long = System.currentTimeMillis() + 604800000L, // 7 days
    val isExpired: Boolean = false,
    
    // Metadata
    val deviceInfo: String? = null,
    val appVersion: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Contact modification attempts for audit trail
 */
@Entity(tableName = "contact_modification_attempts")
data class ContactModificationAttempt(
    @PrimaryKey
    val attemptId: String = UUID.randomUUID().toString(),
    
    val caregiverId: String?, // null if user-initiated
    val userId: String,
    
    // Action details
    val action: ContactAction,
    val contactInfo: ContactInfo? = null,
    val targetContactId: String? = null,
    
    // Result
    val result: ContactActionResult,
    val resultMessage: String? = null,
    val errorDetails: String? = null,
    
    // Context
    val initiatedByUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String? = null,
    
    // Security context
    val deviceInfo: String? = null,
    val appVersion: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    
    // Abuse detection flags
    val flaggedAsAbusive: Boolean = false,
    val abuseScore: Float = 0.0f,
    val abuseReasons: List<String> = emptyList(),
    
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Contact addition request from caregivers
 */
data class ContactAdditionRequest(
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val relationship: String,
    val reason: String? = null,
    val message: String? = null,
    val priority: RequestPriority = RequestPriority.NORMAL
) {
    fun toContactInfo(): ContactInfo {
        return ContactInfo(
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            relationship = relationship,
            notes = message
        )
    }
}

/**
 * Contact protection rules configuration
 */
@Entity(tableName = "contact_protection_rules")
data class ContactProtectionRules(
    @PrimaryKey
    val ruleId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    
    // Protection settings
    val allowCaregiverContactAddition: Boolean = true,
    val requireUserApprovalForAdditions: Boolean = true,
    val allowCaregiverContactModification: Boolean = false,
    val allowCaregiverContactRemoval: Boolean = false,
    val allowCaregiverContactBlocking: Boolean = false,
    
    // Emergency contact protection
    val protectEmergencyContacts: Boolean = true,
    val protectElderRightsAdvocate: Boolean = true,
    val protectFamilyContacts: Boolean = true,
    val protectMedicalContacts: Boolean = true,
    
    // Notification settings
    val notifyUserOfContactRequests: Boolean = true,
    val notifyUserOfBlockedAttempts: Boolean = true,
    val notifyElderAdvocateOfAbuse: Boolean = true,
    
    // Abuse detection settings
    val enableAbuseDetection: Boolean = true,
    val abuseDetectionSensitivity: Float = 0.7f, // 0.0 - 1.0
    val maxBlockedAttemptsPerHour: Int = 3,
    val maxBlockedAttemptsPerDay: Int = 10,
    
    // Auto-response settings
    val autoRejectRemovalRequests: Boolean = true,
    val autoRejectBlockingRequests: Boolean = true,
    val autoApprovalForTrustedCaregivers: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Contact protection statistics for monitoring
 */
@Entity(tableName = "contact_protection_stats")
data class ContactProtectionStats(
    @PrimaryKey
    val statsId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val caregiverId: String? = null,
    
    // Time period
    val periodStart: Long,
    val periodEnd: Long,
    val periodType: String, // daily, weekly, monthly
    
    // Attempt statistics
    val totalAttempts: Int = 0,
    val blockedAttempts: Int = 0,
    val approvedRequests: Int = 0,
    val rejectedRequests: Int = 0,
    val pendingRequests: Int = 0,
    
    // Action breakdown
    val additionAttempts: Int = 0,
    val removalAttempts: Int = 0,
    val blockingAttempts: Int = 0,
    val modificationAttempts: Int = 0,
    
    // Abuse indicators
    val suspiciousActivityCount: Int = 0,
    val abuseFlags: Int = 0,
    val highRiskPatterns: List<String> = emptyList(),
    
    // Response times
    val averageUserResponseTimeMs: Long = 0,
    val longestPendingRequestMs: Long = 0,
    
    val recordedAt: Long = System.currentTimeMillis()
)

/**
 * Emergency contact backup for system protection
 */
@Entity(tableName = "emergency_contact_backup")
data class EmergencyContactBackup(
    @PrimaryKey
    val backupId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val originalContactId: String,
    
    @Embedded
    val contactInfo: ContactInfo,
    
    // Backup metadata
    val backupReason: String, // user_deletion, caregiver_attempt, system_protection
    val backupTimestamp: Long = System.currentTimeMillis(),
    val restorable: Boolean = true,
    val restoreExpiresAt: Long = System.currentTimeMillis() + 2592000000L, // 30 days
    
    // Protection context
    val deletedBy: String? = null,
    val deletionAttemptBy: String? = null,
    val protectionTriggered: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Contact access log for detailed audit trail
 */
@Entity(tableName = "contact_access_log")
data class ContactAccessLog(
    @PrimaryKey
    val logId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val accessorId: String, // userId or caregiverId
    val contactId: String,
    
    // Access details
    val accessType: ContactAccessType,
    val accessMethod: String, // app, api, sync, etc.
    val accessResult: String, // success, blocked, error
    
    // Context
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String? = null,
    val deviceInfo: String? = null,
    val appVersion: String? = null,
    
    // Security
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val securityFlags: List<String> = emptyList(),
    
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Enums for contact protection system
 */
enum class ProtectionLevel {
    USER_CONTROLLED,      // User has full control
    SYSTEM_PROTECTED,     // System-added, high protection
    EMERGENCY_PROTECTED,  // Emergency contacts, special protection
    ADVOCATE_PROTECTED    // Elder rights advocate, maximum protection
}

enum class ContactRequestType {
    ADD_CONTACT,
    MODIFY_CONTACT,
    SUGGEST_REMOVAL // Caregiver can suggest but not enforce
}

enum class RequestStatus {
    PENDING_USER_APPROVAL,
    APPROVED,
    REJECTED,
    EXPIRED,
    CANCELLED
}

enum class RequestPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

enum class ContactAction {
    ADD_CONTACT,
    REMOVE_CONTACT,
    BLOCK_CONTACT,
    UNBLOCK_CONTACT,
    MODIFY_CONTACT,
    RESPOND_TO_REQUEST,
    VIEW_CONTACT,
    EXPORT_CONTACTS
}

enum class ContactActionResult {
    SUCCESS,
    BLOCKED_BY_PROTECTION,
    PERMISSION_DENIED,
    PENDING_USER_APPROVAL,
    USER_APPROVED,
    USER_REJECTED,
    ERROR,
    EXPIRED
}

enum class ContactAccessType {
    VIEW,
    EDIT,
    DELETE,
    CALL,
    MESSAGE,
    EMAIL,
    EXPORT,
    SYNC
}
