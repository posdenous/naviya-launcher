package com.naviya.launcher.caregiver.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Data models for offline-first caregiver connectivity system
 * Supports opportunistic sync, heartbeat monitoring, and multi-channel emergency alerts
 */

@Entity(tableName = "caregiver_connections")
@TypeConverters(CaregiverConnectivityConverters::class)
data class CaregiverConnection(
    @PrimaryKey
    val connectionId: String,
    val userId: String,
    val caregiverId: String,
    val caregiverName: String,
    val caregiverEmail: String,
    val caregiverPhone: String,
    val connectionStatus: CaregiverConnectionStatus,
    val connectionType: CaregiverConnectionType,
    val permissions: List<CaregiverPermission>,
    val lastHeartbeat: Long,
    val lastSyncTime: Long,
    val connectionQuality: ConnectionQuality,
    val emergencyContactPriority: Int = 0, // 0 = not emergency contact, 1+ = priority order
    val isActive: Boolean = true,
    val createdTimestamp: Long,
    val lastModifiedTimestamp: Long,
    val offlineCapabilities: List<OfflineCapability> = emptyList(),
    val syncPreferences: CaregiverSyncPreferences,
    val emergencyAlertPreferences: EmergencyAlertPreferences
)

/**
 * Caregiver sync preferences for offline-first operation
 */
data class CaregiverSyncPreferences(
    val syncFrequency: SyncFrequency = SyncFrequency.MODERATE,
    val syncOnlyOnWifi: Boolean = false,
    val syncCriticalDataOnly: Boolean = false,
    val maxSyncRetries: Int = 3,
    val syncTimeoutSeconds: Int = 30,
    val enableOpportunisticSync: Boolean = true,
    val syncCategories: List<SyncCategory> = listOf(
        SyncCategory.EMERGENCY_EVENTS,
        SyncCategory.HEALTH_STATUS,
        SyncCategory.APP_USAGE_SUMMARY
    )
)

/**
 * Emergency alert preferences for multi-channel communication
 */
data class EmergencyAlertPreferences(
    val primaryChannel: EmergencyChannel = EmergencyChannel.SMS,
    val secondaryChannel: EmergencyChannel = EmergencyChannel.PUSH_NOTIFICATION,
    val enableVoiceCall: Boolean = true,
    val enableSMS: Boolean = true,
    val enablePushNotification: Boolean = true,
    val enableEmail: Boolean = false,
    val alertPriorities: Map<EmergencyEventType, EmergencyAlertPriority> = mapOf(
        EmergencyEventType.PANIC_MODE_ACTIVATED to EmergencyAlertPriority.CRITICAL,
        EmergencyEventType.ABUSE_DETECTED to EmergencyAlertPriority.HIGH,
        EmergencyEventType.CONTACT_PROTECTION_VIOLATED to EmergencyAlertPriority.MEDIUM,
        EmergencyEventType.SYSTEM_HEALTH_CRITICAL to EmergencyAlertPriority.LOW
    ),
    val quietHoursStart: Int = 22, // 10 PM
    val quietHoursEnd: Int = 7,    // 7 AM
    val respectQuietHours: Boolean = true,
    val emergencyOverrideQuietHours: Boolean = true
)

/**
 * Emergency alert for multi-channel communication
 */
@Entity(tableName = "emergency_alerts")
@TypeConverters(CaregiverConnectivityConverters::class)
data class EmergencyAlert(
    @PrimaryKey
    val alertId: String,
    val userId: String,
    val eventType: EmergencyEventType,
    val priority: EmergencyAlertPriority,
    val message: String,
    val timestamp: Long,
    val channels: MutableList<EmergencyChannelResult>,
    val status: EmergencyAlertStatus,
    val targetCaregivers: List<String> = emptyList(),
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val nextRetryTime: Long? = null,
    val resolvedTimestamp: Long? = null,
    val escalatedToElderRights: Boolean = false,
    val escalationTimestamp: Long? = null
)

/**
 * Result of emergency alert through specific channel
 */
data class EmergencyChannelResult(
    val channel: EmergencyChannel,
    val caregiverId: String,
    val success: Boolean,
    val message: String,
    val timestamp: Long,
    val deliveryConfirmation: String? = null,
    val responseReceived: Boolean = false,
    val responseTimestamp: Long? = null,
    val responseContent: String? = null
)

/**
 * Heartbeat monitoring data
 */
@Entity(tableName = "caregiver_heartbeats")
data class CaregiverHeartbeat(
    @PrimaryKey
    val heartbeatId: String,
    val userId: String,
    val caregiverId: String,
    val timestamp: Long,
    val success: Boolean,
    val responseTime: Long, // milliseconds
    val networkType: String,
    val connectionQuality: ConnectionQuality,
    val errorMessage: String? = null
)

/**
 * Sync operation tracking
 */
@Entity(tableName = "sync_operations")
@TypeConverters(CaregiverConnectivityConverters::class)
data class SyncOperation(
    @PrimaryKey
    val syncId: String,
    val userId: String,
    val caregiverId: String,
    val syncType: SyncType,
    val startTimestamp: Long,
    val endTimestamp: Long? = null,
    val status: SyncStatus,
    val dataCategories: List<SyncCategory>,
    val recordsSynced: Int = 0,
    val bytesTransferred: Long = 0,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val networkQuality: ConnectionQuality
)

/**
 * Offline data queue for when connectivity is limited
 */
@Entity(tableName = "offline_data_queue")
@TypeConverters(CaregiverConnectivityConverters::class)
data class OfflineDataQueue(
    @PrimaryKey
    val queueId: String,
    val userId: String,
    val dataType: OfflineDataType,
    val priority: OfflineDataPriority,
    val data: String, // JSON serialized data
    val timestamp: Long,
    val targetCaregivers: List<String>,
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val nextRetryTime: Long? = null,
    val expirationTime: Long? = null,
    val requiresAcknowledgment: Boolean = false
)

/**
 * System connectivity state
 */
data class CaregiverConnectivityState(
    val isOnline: Boolean = false,
    val networkState: NetworkState = NetworkState.UNKNOWN,
    val connectionQuality: ConnectionQuality = ConnectionQuality.UNKNOWN,
    val connectedCaregivers: Int = 0,
    val onlineCaregivers: Int = 0,
    val lastSyncTime: Long = 0,
    val lastOnlineTime: Long = 0,
    val lastOfflineTime: Long = 0,
    val offlineCapabilities: List<OfflineCapability> = emptyList(),
    val pendingSyncItems: Int = 0,
    val queuedEmergencyAlerts: Int = 0
)

/**
 * Caregiver connectivity status summary
 */
data class CaregiverConnectivityStatus(
    val totalCaregivers: Int,
    val onlineCaregivers: Int,
    val offlineCaregivers: Int,
    val lastSyncTime: Long,
    val isEmergencyCapable: Boolean,
    val networkState: NetworkState
)

/**
 * Emergency event from the system
 */
data class EmergencyEvent(
    val eventId: String,
    val userId: String,
    val type: EmergencyEventType,
    val message: String,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Result of sync operation
 */
data class SyncResult(
    val success: Boolean,
    val message: String,
    val timestamp: Long,
    val recordsSynced: Int = 0,
    val errors: List<String> = emptyList()
)

/**
 * Result of emergency alert
 */
data class EmergencyAlertResult(
    val success: Boolean,
    val channelsUsed: List<EmergencyChannel>,
    val message: String,
    val alertId: String? = null
)

/**
 * Failed escalation tracking
 */
@Entity(tableName = "failed_escalations")
data class FailedEscalation(
    @PrimaryKey
    val escalationId: String,
    val originalAlertId: String,
    val userId: String,
    val failureReason: String,
    val timestamp: Long,
    val requiresManualIntervention: Boolean = false,
    val resolved: Boolean = false,
    val resolutionTimestamp: Long? = null
)

/**
 * Sync failure tracking
 */
@Entity(tableName = "sync_failures")
data class SyncFailure(
    @PrimaryKey
    val failureId: String,
    val timestamp: Long,
    val error: String,
    val retryScheduled: Boolean = false,
    val resolved: Boolean = false
)

// ==================== ENUMS ====================

enum class CaregiverConnectionStatus {
    ONLINE,     // Actively connected and responsive
    LIMITED,    // Connected but with limited functionality
    OFFLINE,    // Not currently connected
    ERROR,      // Connection error state
    UNKNOWN     // Status not yet determined
}

enum class CaregiverConnectionType {
    PRIMARY,    // Primary caregiver with full access
    SECONDARY,  // Secondary caregiver with limited access
    EMERGENCY,  // Emergency contact only
    FAMILY,     // Family member with basic access
    HEALTHCARE, // Healthcare provider
    ADVOCATE    // Elder rights advocate
}

enum class NetworkState {
    CONNECTED,    // Full internet connectivity
    LIMITED,      // Limited connectivity (e.g., captive portal)
    DISCONNECTED, // No network connectivity
    UNKNOWN       // Network state unknown
}

enum class ConnectionQuality {
    HIGH,    // WiFi or strong cellular
    MEDIUM,  // Moderate cellular connection
    LOW,     // Weak cellular connection
    UNKNOWN  // Quality not determined
}

enum class OfflineCapability {
    LOCAL_NOTIFICATIONS,     // Can send local notifications
    SMS_EMERGENCY_ALERTS,    // Can send SMS without internet
    LOCAL_DATA_STORAGE,      // Can store data locally
    PANIC_MODE,              // Panic mode works offline
    CONTACT_PROTECTION,      // Contact protection active
    ABUSE_DETECTION,         // Abuse detection works offline
    BASIC_APP_FUNCTIONALITY, // Core app features available
    EMERGENCY_CALLING        // Can make emergency calls
}

enum class SyncFrequency {
    REALTIME,   // Immediate sync when possible
    FREQUENT,   // Every 5 minutes
    MODERATE,   // Every 15 minutes
    INFREQUENT, // Every hour
    MANUAL      // Only when manually triggered
}

enum class SyncCategory {
    EMERGENCY_EVENTS,      // Panic mode, alerts, etc.
    HEALTH_STATUS,         // Basic health indicators
    APP_USAGE_SUMMARY,     // High-level app usage
    LOCATION_APPROXIMATE,  // Approximate location only
    CONTACT_CHANGES,       // Changes to contacts
    SYSTEM_HEALTH,         // Device health status
    ABUSE_ALERTS,          // Abuse detection alerts
    MEDICATION_REMINDERS   // Medication compliance
}

enum class EmergencyChannel {
    SMS,                // Text message
    VOICE_CALL,         // Phone call
    PUSH_NOTIFICATION,  // App push notification
    EMAIL,              // Email notification
    LOCAL_NOTIFICATION, // Local device notification
    BACKUP_SMS          // SMS to backup contacts
}

enum class EmergencyEventType {
    PANIC_MODE_ACTIVATED,           // User activated panic mode
    ABUSE_DETECTED,                 // Abuse pattern detected
    CONTACT_PROTECTION_VIOLATED,    // Emergency contacts tampered with
    SYSTEM_HEALTH_CRITICAL,         // System health issue
    MEDICATION_MISSED,              // Medication not taken
    FALL_DETECTED,                  // Fall detection triggered
    UNUSUAL_ACTIVITY,               // Unusual app usage pattern
    SYSTEM_TEST                     // Test alert
}

enum class EmergencyAlertPriority {
    CRITICAL,   // Immediate response required
    HIGH,       // Response within 15 minutes
    MEDIUM,     // Response within 1 hour
    LOW         // Response within 24 hours
}

enum class EmergencyAlertStatus {
    PENDING,    // Alert being sent
    SENT,       // Successfully sent
    DELIVERED,  // Delivery confirmed
    FAILED,     // Failed to send
    RESPONDED,  // Caregiver responded
    RESOLVED    // Issue resolved
}

enum class SyncType {
    FULL,           // Complete data sync
    INCREMENTAL,    // Only changes since last sync
    CRITICAL,       // Critical data only
    EMERGENCY,      // Emergency data only
    MANUAL,         // User-initiated sync
    OPPORTUNISTIC   // Automatic when connection available
}

enum class SyncStatus {
    PENDING,     // Sync queued
    IN_PROGRESS, // Sync in progress
    COMPLETED,   // Sync completed successfully
    FAILED,      // Sync failed
    CANCELLED,   // Sync cancelled
    PARTIAL      // Sync partially completed
}

enum class OfflineDataType {
    EMERGENCY_EVENT,    // Emergency event data
    HEALTH_UPDATE,      // Health status update
    APP_USAGE,          // App usage data
    LOCATION_UPDATE,    // Location update
    SYSTEM_LOG,         // System log entry
    USER_ACTION,        // User action log
    ABUSE_ALERT         // Abuse detection alert
}

enum class OfflineDataPriority {
    CRITICAL,   // Must be synced immediately when online
    HIGH,       // Sync as soon as possible
    MEDIUM,     // Sync during normal sync cycle
    LOW         // Sync when convenient
}

enum class SystemMode {
    ONLINE,     // Full online functionality
    OFFLINE,    // Offline mode active
    LIMITED,    // Limited connectivity mode
    EMERGENCY   // Emergency-only mode
}

enum class CaregiverPermission {
    VIEW_LOCATION,          // Can view approximate location
    VIEW_APP_USAGE,         // Can view app usage summary
    VIEW_HEALTH_STATUS,     // Can view basic health indicators
    RECEIVE_EMERGENCY_ALERTS, // Receives emergency notifications
    MODIFY_SETTINGS,        // Can modify some settings
    VIEW_CONTACTS,          // Can view contact list
    EMERGENCY_OVERRIDE,     // Can override in emergencies
    FULL_ACCESS            // Full caregiver access
}

// ==================== TYPE CONVERTERS ====================

class CaregiverConnectivityConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromPermissionList(permissions: List<CaregiverPermission>): String {
        return gson.toJson(permissions)
    }

    @TypeConverter
    fun toPermissionList(permissionsString: String): List<CaregiverPermission> {
        val listType = object : TypeToken<List<CaregiverPermission>>() {}.type
        return gson.fromJson(permissionsString, listType)
    }

    @TypeConverter
    fun fromOfflineCapabilityList(capabilities: List<OfflineCapability>): String {
        return gson.toJson(capabilities)
    }

    @TypeConverter
    fun toOfflineCapabilityList(capabilitiesString: String): List<OfflineCapability> {
        val listType = object : TypeToken<List<OfflineCapability>>() {}.type
        return gson.fromJson(capabilitiesString, listType)
    }

    @TypeConverter
    fun fromSyncPreferences(preferences: CaregiverSyncPreferences): String {
        return gson.toJson(preferences)
    }

    @TypeConverter
    fun toSyncPreferences(preferencesString: String): CaregiverSyncPreferences {
        return gson.fromJson(preferencesString, CaregiverSyncPreferences::class.java)
    }

    @TypeConverter
    fun fromEmergencyAlertPreferences(preferences: EmergencyAlertPreferences): String {
        return gson.toJson(preferences)
    }

    @TypeConverter
    fun toEmergencyAlertPreferences(preferencesString: String): EmergencyAlertPreferences {
        return gson.fromJson(preferencesString, EmergencyAlertPreferences::class.java)
    }

    @TypeConverter
    fun fromEmergencyChannelResultList(results: List<EmergencyChannelResult>): String {
        return gson.toJson(results)
    }

    @TypeConverter
    fun toEmergencyChannelResultList(resultsString: String): List<EmergencyChannelResult> {
        val listType = object : TypeToken<List<EmergencyChannelResult>>() {}.type
        return gson.fromJson(resultsString, listType)
    }

    @TypeConverter
    fun fromSyncCategoryList(categories: List<SyncCategory>): String {
        return gson.toJson(categories)
    }

    @TypeConverter
    fun toSyncCategoryList(categoriesString: String): List<SyncCategory> {
        val listType = object : TypeToken<List<SyncCategory>>() {}.type
        return gson.fromJson(categoriesString, listType)
    }

    @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return gson.toJson(strings)
    }

    @TypeConverter
    fun toStringList(stringsString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringsString, listType)
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toStringMap(mapString: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(mapString, mapType)
    }

    @TypeConverter
    fun fromEmergencyEventPriorityMap(map: Map<EmergencyEventType, EmergencyAlertPriority>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toEmergencyEventPriorityMap(mapString: String): Map<EmergencyEventType, EmergencyAlertPriority> {
        val mapType = object : TypeToken<Map<EmergencyEventType, EmergencyAlertPriority>>() {}.type
        return gson.fromJson(mapString, mapType)
    }
}
