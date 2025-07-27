package com.naviya.launcher.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.naviya.launcher.data.converters.DateConverter
import com.naviya.launcher.data.converters.CrashRecoveryConverter
import java.util.Date
import java.util.UUID

/**
 * Data model for crash recovery state
 * Maps to crash_recovery_state.mcp.yaml schema
 */
@Entity(tableName = "crash_recovery_state")
@TypeConverters(DateConverter::class, CrashRecoveryConverter::class)
data class CrashRecoveryState(
    @PrimaryKey
    val recoveryId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    
    // Crash tracking and thresholds
    val crashTracking: CrashTracking,
    
    // Individual crash records
    val crashHistory: List<CrashRecord>,
    
    // Recovery mode state
    val recoveryMode: RecoveryMode,
    
    // Safe tiles configuration
    val safeTiles: SafeTiles,
    
    // Recovery assistance and guidance
    val recoveryAssistance: RecoveryAssistance,
    
    // Recovery exit conditions
    val exitConditions: ExitConditions,
    
    // Post-recovery monitoring
    val postRecovery: PostRecovery,
    
    // System integration settings
    val systemIntegration: SystemIntegration,
    
    // Analytics and improvement data
    val analytics: RecoveryAnalytics,
    
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
 * Crash tracking configuration
 */
data class CrashTracking(
    val currentCrashCount: Int = 0,
    val crashThreshold: Int = 3,
    val trackingPeriodHours: Int = 24,
    val trackingPeriodStart: Date = Date(),
    val lastCrashTimestamp: Date? = null,
    val crashThresholdExceeded: Boolean = false
)

/**
 * Individual crash record
 */
data class CrashRecord(
    val crashId: String = UUID.randomUUID().toString(),
    val crashTimestamp: Date,
    val crashType: CrashType,
    val severity: CrashSeverity,
    val stackTrace: String, // Truncated to 2000 chars
    val deviceState: DeviceState,
    val userActionsBeforeCrash: List<String>,
    val recoverySuccessful: Boolean = false,
    val caregiverNotified: Boolean = false
)

/**
 * Crash type enumeration
 */
enum class CrashType {
    LAUNCHER_CRASH,
    APP_CRASH,
    SYSTEM_CRASH,
    MEMORY_CRASH,
    ANR
}

/**
 * Crash severity levels
 */
enum class CrashSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Device state at time of crash
 */
data class DeviceState(
    val batteryLevel: Int,
    val memoryUsageMb: Int,
    val cpuUsagePercent: Int,
    val networkStatus: NetworkStatus
)

/**
 * Network status enumeration
 */
enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    LIMITED
}

/**
 * Recovery mode state
 */
data class RecoveryMode(
    val isActive: Boolean = false,
    val activationTimestamp: Date? = null,
    val activationTrigger: ActivationTrigger? = null,
    val activationReason: String? = null,
    val expectedDurationMinutes: Int = 60,
    val autoExitEnabled: Boolean = true,
    val manualExitAllowed: Boolean = false,
    val caregiverExitAllowed: Boolean = true
)

/**
 * Recovery mode activation triggers
 */
enum class ActivationTrigger {
    CRASH_THRESHOLD_EXCEEDED,
    MANUAL_LONG_PRESS,
    CAREGIVER_REMOTE_ACTIVATION,
    SYSTEM_INSTABILITY
}

/**
 * Safe tiles configuration for recovery mode
 */
data class SafeTiles(
    val mandatoryTiles: List<SafeTile>,
    val disabledFeatures: List<DisabledFeature>
)

/**
 * Individual safe tile configuration
 */
data class SafeTile(
    val position: Int, // 0-3 for 2x2 recovery grid
    val tileType: SafeTileType,
    val packageName: String? = null,
    val label: String,
    val alwaysFunctional: Boolean = true,
    val priority: SafeTilePriority = SafeTilePriority.HIGH
)

/**
 * Safe tile types for recovery mode
 */
enum class SafeTileType {
    PHONE_DIALER,
    SETTINGS_ACCESS,
    SOS_EMERGENCY_BUTTON,
    RECOVERY_HELP
}

/**
 * Safe tile priority levels
 */
enum class SafeTilePriority {
    LOWEST,
    LOW,
    MEDIUM,
    HIGH,
    HIGHEST
}

/**
 * Features disabled during recovery mode
 */
enum class DisabledFeature {
    APP_INSTALLATION,
    TILE_CUSTOMIZATION,
    CAREGIVER_PAIRING,
    ADVANCED_SETTINGS,
    NON_ESSENTIAL_APPS,
    LAUNCHER_CUSTOMIZATION,
    APP_WHITELIST_CHANGES
}

/**
 * Recovery assistance configuration
 */
data class RecoveryAssistance(
    val userGuidanceShown: Boolean = false,
    val guidanceMessage: String = "Your device had some problems. We've switched to safe mode to keep you safe.",
    val helpActionsAvailable: List<HelpAction>,
    val caregiverContacted: Boolean = false,
    val caregiverResponseReceived: Boolean = false,
    val userAssistanceRequested: Boolean = false
)

/**
 * Available help actions during recovery
 */
enum class HelpAction {
    CONTACT_CAREGIVER,
    WAIT_FOR_AUTO_RECOVERY,
    USE_BASIC_FUNCTIONS,
    RESTART_DEVICE,
    FACTORY_RESET_WARNING
}

/**
 * Exit conditions for recovery mode
 */
data class ExitConditions(
    val stabilityMonitoring: StabilityMonitoring,
    val exitApproval: ExitApproval
)

/**
 * System stability monitoring
 */
data class StabilityMonitoring(
    val monitoringActive: Boolean = true,
    val stabilityPeriodHours: Int = 24,
    val stabilityStartTimestamp: Date? = null,
    val crashesDuringMonitoring: Int = 0,
    val stabilityConfirmed: Boolean = false
)

/**
 * Exit approval configuration
 */
data class ExitApproval(
    val userRequestedExit: Boolean = false,
    val caregiverApprovedExit: Boolean = false,
    val automaticExitScheduled: Boolean = false,
    val scheduledExitTimestamp: Date? = null
)

/**
 * Post-recovery monitoring
 */
data class PostRecovery(
    val monitoringPeriodHours: Int = 48,
    val monitoringStartTimestamp: Date? = null,
    val crashSensitivityMultiplier: Float = 0.5f, // Lower threshold
    val crashesDuringMonitoring: Int = 0,
    val monitoringCompleted: Boolean = false,
    val userFeedbackCollected: Boolean = false,
    val systemOptimizationsApplied: Boolean = false
)

/**
 * System integration settings
 */
data class SystemIntegration(
    val caregiverNotifications: CaregiverNotifications,
    val emergencySystemIntegration: EmergencySystemIntegration
)

/**
 * Caregiver notification settings
 */
data class CaregiverNotifications(
    val crashAlertsEnabled: Boolean = true,
    val recoveryModeAlertsEnabled: Boolean = true,
    val exitNotificationsEnabled: Boolean = true,
    val lastNotificationTimestamp: Date? = null
)

/**
 * Emergency system integration
 */
data class EmergencySystemIntegration(
    val sosAlwaysFunctional: Boolean = true,
    val emergencyContactsAccessible: Boolean = true,
    val locationServicesActive: Boolean = true
)

/**
 * Recovery analytics and improvement data
 */
data class RecoveryAnalytics(
    val recoveryEffectiveness: RecoveryEffectiveness,
    val crashPatterns: CrashPatterns
)

/**
 * Recovery effectiveness metrics
 */
data class RecoveryEffectiveness(
    val successfulRecoveries: Int = 0,
    val failedRecoveries: Int = 0,
    val averageRecoveryDurationHours: Float = 0.0f,
    val userSatisfactionRating: Int? = null // 1-5 scale
)

/**
 * Crash pattern analysis
 */
data class CrashPatterns(
    val mostCommonCrashType: CrashType? = null,
    val crashTimePatterns: List<Int> = emptyList(), // Hours 0-23
    val problematicApps: List<String> = emptyList()
)
