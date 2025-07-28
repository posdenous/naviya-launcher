package com.naviya.launcher.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.naviya.launcher.data.converters.DateConverter
import com.naviya.launcher.data.converters.NotificationConverter
import java.util.Date
import java.util.UUID

/**
 * Data model for notification state (unread tile)
 * Maps to notification_state.mcp.yaml schema
 */
@Entity(tableName = "notification_state")
// @TypeConverters(DateConverter::class, NotificationConverter::class) // Temporarily disabled for build
data class NotificationState(
    @PrimaryKey
    val notificationId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    
    // Unread counts and aggregation
    val unreadSummary: UnreadSummary,
    
    // Recent missed calls for tile display
    val missedCalls: List<MissedCall>,
    
    // Recent unread SMS messages
    val unreadSms: List<UnreadSms>,
    
    // Priority contacts configuration
    val priorityContacts: PriorityContacts,
    
    // Tile display configuration
    val tileDisplay: TileDisplay,
    
    // Offline functionality state
    val offlineState: OfflineState,
    
    // Caregiver integration settings
    val caregiverIntegration: CaregiverIntegration,
    
    // Privacy settings
    val privacySettings: NotificationPrivacySettings,
    
    // Performance metrics
    val performanceMetrics: PerformanceMetrics,
    
    // State metadata
    val schemaVersion: String = "1.0.0",
    val androidVersion: String,
    val appVersion: String,
    val deviceModel: String,
    val lastBackupTimestamp: Date? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Summary of unread notifications
 */
data class UnreadSummary(
    val totalUnreadCount: Int = 0,
    val missedCallsCount: Int = 0,
    val unreadSmsCount: Int = 0,
    val priorityUnreadCount: Int = 0,
    val lastUpdated: Date = Date(),
    val calculationMethod: CalculationMethod = CalculationMethod.DEVICE_LOCAL
)

/**
 * Calculation method for unread counts
 */
enum class CalculationMethod {
    DEVICE_LOCAL,
    SERVER_SYNC,
    HYBRID
}

/**
 * Missed call information
 */
data class MissedCall(
    val callId: String,
    val callerNumber: String,
    val callerName: String? = null,
    val callTimestamp: Date,
    val callDuration: Int = 0, // seconds
    val isPriorityContact: Boolean = false,
    val contactType: ContactType = ContactType.UNKNOWN,
    val readStatus: Boolean = false,
    val urgencyLevel: UrgencyLevel = UrgencyLevel.NORMAL
)

/**
 * Unread SMS information
 */
data class UnreadSms(
    val smsId: String,
    val senderNumber: String,
    val senderName: String? = null,
    val messagePreview: String, // First 50 characters
    val messageTimestamp: Date,
    val isPriorityContact: Boolean = false,
    val contactType: ContactType = ContactType.UNKNOWN,
    val readStatus: Boolean = false,
    val urgencyLevel: UrgencyLevel = UrgencyLevel.NORMAL,
    val messageType: MessageType = MessageType.TEXT
)

/**
 * Contact type classification
 */
enum class ContactType {
    EMERGENCY_CONTACT,
    CAREGIVER,
    KNOWN_CONTACT,
    UNKNOWN
}

/**
 * Urgency level for notifications
 */
enum class UrgencyLevel {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

/**
 * Message type classification
 */
enum class MessageType {
    TEXT,
    MMS,
    GROUP,
    AUTOMATED
}

/**
 * Priority contacts configuration
 */
data class PriorityContacts(
    val emergencyContacts: List<EmergencyContact>,
    val caregivers: List<CaregiverContact>
)

/**
 * Emergency contact information
 */
data class EmergencyContact(
    val contactId: String,
    val phoneNumber: String,
    val contactName: String,
    val weightMultiplier: Int = 3 // 3x weight for emergency contacts
)

/**
 * Caregiver contact information
 */
data class CaregiverContact(
    val caregiverId: String,
    val phoneNumber: String,
    val contactName: String,
    val weightMultiplier: Int = 2 // 2x weight for caregivers
)

/**
 * Tile display configuration
 */
data class TileDisplay(
    val currentState: TileState = TileState.NO_UNREAD,
    val badgeCount: Int = 0,
    val displayOverflow: Boolean = false, // Show 99+ indicator
    val lastInteraction: Date? = null,
    val animationState: AnimationState = AnimationState.NONE,
    val backgroundColor: String = "#F5F5F5",
    val iconColor: String = "#757575"
)

/**
 * Tile state enumeration
 */
enum class TileState {
    NO_UNREAD,
    HAS_UNREAD,
    URGENT_UNREAD
}

/**
 * Animation state for tile
 */
enum class AnimationState {
    NONE,
    SUBTLE_PULSE,
    STRONGER_PULSE
}

/**
 * Offline functionality state
 */
data class OfflineState(
    val offlineModeActive: Boolean = false,
    val lastSyncTimestamp: Date? = null,
    val cachedDataValid: Boolean = true,
    val cacheExpiry: Date? = null,
    val localDataSources: LocalDataSources
)

/**
 * Local data source accessibility
 */
data class LocalDataSources(
    val callLogAccessible: Boolean = false,
    val smsAccessible: Boolean = false,
    val contactsAccessible: Boolean = false
)

/**
 * Caregiver integration settings
 */
data class CaregiverIntegration(
    val caregiverOnlineStatus: Boolean = false,
    val caregiverNotificationSent: Boolean = false,
    val caregiverAlertThreshold: Int = 10,
    val lastCaregiverSync: Date? = null,
    val caregiverPermissions: CaregiverPermissions
)

/**
 * Caregiver permissions for notification access
 */
data class CaregiverPermissions(
    val canSeeUnreadCount: Boolean = true,
    val canSeeCallerIds: Boolean = false,
    val canSeeMessagePreviews: Boolean = false,
    val canMarkAsRead: Boolean = false
)

/**
 * Privacy settings for notifications
 */
data class NotificationPrivacySettings(
    val messageContentCached: Boolean = false,
    val callerIdCached: Boolean = true,
    val dataEncryptionEnabled: Boolean = true,
    val autoCleanupEnabled: Boolean = true,
    val cleanupAfterDays: Int = 30
)

/**
 * Performance metrics for notification system
 */
data class PerformanceMetrics(
    val lastCalculationDurationMs: Long = 0,
    val cacheHitRate: Float = 0.0f,
    val averageResponseTimeMs: Long = 0,
    val errorCount: Int = 0,
    val lastErrorTimestamp: Date? = null,
    val lastErrorMessage: String? = null
)
