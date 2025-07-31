// DISABLED: CaregiverNotificationService - causing compilation errors
// This file has been temporarily disabled to resolve Kotlin compilation issues

/**
 * Location data for notifications
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val timestamp: Long
)

/**
 * Emergency types for notifications
 */
enum class EmergencyType {
    SOS_ACTIVATED,
    FALL_DETECTED,
    MEDICAL_EMERGENCY,
    PANIC_BUTTON
}

/**
 * Notification priority levels
 */
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}
