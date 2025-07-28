package com.naviya.launcher.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.naviya.launcher.data.converters.DateConverter
import com.naviya.launcher.data.converters.TileConfigurationConverter
import java.util.Date
import java.util.UUID

/**
 * Data model for launcher state persistence
 * Maps to launcher_state.mcp.yaml schema
 */
@Entity(tableName = "launcher_state")
// @TypeConverters(DateConverter::class, TileConfigurationConverter::class) // Temporarily disabled for build
data class LauncherState(
    @PrimaryKey
    val launcherId: String = UUID.randomUUID().toString(),
    
    val userId: String,
    
    // Grid configuration for 2x3 layout
    val gridConfiguration: GridConfiguration,
    
    // Tile layout and positioning
    val tileLayout: List<TileConfiguration>,
    
    // Accessibility settings for elderly users
    val accessibilitySettings: AccessibilitySettings,
    
    // PIN protection settings
    val pinProtection: PinProtectionSettings,
    
    // Crash recovery configuration
    val crashRecovery: CrashRecoverySettings,
    
    // Offline mode settings
    val offlineMode: OfflineModeSettings,
    
    // Localization settings
    val localization: LocalizationSettings,
    
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
 * Grid configuration for 2x3 launcher layout
 */
data class GridConfiguration(
    val rows: Int = 2,
    val columns: Int = 3,
    val totalTiles: Int = 6,
    val iconSizeDp: Int = 64,
    val paddingDp: Int = 16,
    val spacingDp: Int = 16,
    val marginDp: Int = 24
)

/**
 * Individual tile configuration
 */
data class TileConfiguration(
    val position: Int, // 0-5 for 2x3 grid
    val tileType: TileType,
    val packageName: String? = null,
    val customLabel: String? = null,
    val iconResource: String? = null,
    val isVisible: Boolean = true,
    val isEnabled: Boolean = true,
    val backgroundColor: String = "#F5F5F5",
    val textColor: String = "#212121",
    val priority: TilePriority = TilePriority.NORMAL
)

/**
 * Tile types supported by the launcher
 */
enum class TileType {
    PHONE_DIALER,
    SMS_MESSAGES,
    SETTINGS,
    CAMERA,
    UNREAD_NOTIFICATIONS,
    SOS_EMERGENCY,
    CUSTOM_APP,
    EMPTY_SLOT
}

/**
 * Tile priority levels
 */
enum class TilePriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST,
    EMERGENCY // For SOS and critical functions
}

/**
 * Accessibility settings for elderly users
 */
data class AccessibilitySettings(
    val fontScale: Float = 1.6f, // 1.6x larger text
    val minimumTouchTargetDp: Int = 48,
    val highContrastEnabled: Boolean = true,
    val largeIconsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val audioFeedbackEnabled: Boolean = false,
    val ttsEnabled: Boolean = true,
    val voiceInputEnabled: Boolean = false,
    val slowAnimationsEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false,
    val colorBlindnessSupport: ColorBlindnessType = ColorBlindnessType.NONE
)

/**
 * Color blindness support types
 */
enum class ColorBlindnessType {
    NONE,
    PROTANOPIA,
    DEUTERANOPIA,
    TRITANOPIA,
    MONOCHROMACY
}

/**
 * PIN protection settings
 */
data class PinProtectionSettings(
    val pinEnabled: Boolean = true,
    val pinLength: Int = 4,
    val maxFailedAttempts: Int = 3,
    val lockoutDurationMinutes: Int = 15,
    val sessionDurationMinutes: Int = 5,
    val emergencyBypassEnabled: Boolean = true,
    val biometricFallbackEnabled: Boolean = false,
    val recoveryMethodEnabled: Boolean = true
)

/**
 * Crash recovery settings
 */
data class CrashRecoverySettings(
    val crashThreshold: Int = 3,
    val trackingPeriodHours: Int = 24,
    val recoveryModeEnabled: Boolean = true,
    val safeTilesEnabled: Boolean = true,
    val caregiverNotificationEnabled: Boolean = true,
    val autoRecoveryEnabled: Boolean = true,
    val postRecoveryMonitoringHours: Int = 48
)

/**
 * Offline mode settings
 */
data class OfflineModeSettings(
    val offlineModeEnabled: Boolean = true,
    val localDataCachingEnabled: Boolean = true,
    val offlineIndicatorEnabled: Boolean = true,
    val essentialFunctionsOnly: Boolean = false,
    val syncWhenOnlineEnabled: Boolean = true,
    val offlineTimeoutHours: Int = 72
)

/**
 * Localization settings
 */
data class LocalizationSettings(
    val currentLanguage: String = "en",
    val supportedLanguages: List<String> = listOf("en", "de", "tr", "ar", "uk"),
    val rtlSupported: Boolean = true,
    val dateFormat: String = "dd/MM/yyyy",
    val timeFormat: String = "HH:mm",
    val numberFormat: String = "decimal",
    val currencyCode: String = "EUR"
)

/**
 * Sync status enumeration
 */
enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED,
    OFFLINE
}
