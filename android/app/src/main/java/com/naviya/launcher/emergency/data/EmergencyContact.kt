package com.naviya.launcher.emergency.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.UUID

/**
 * Emergency contact data model for elderly users
 * Follows Windsurf rules for data models and elderly accessibility
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    
    @ColumnInfo(name = "relationship")
    val relationship: String, // "caregiver", "family", "doctor", "emergency_service"
    
    @ColumnInfo(name = "priority")
    val priority: Int, // 1 = highest priority (called first)
    
    @ColumnInfo(name = "is_primary_caregiver")
    val isPrimaryCaregiver: Boolean = false,
    
    @ColumnInfo(name = "language_preference")
    val languagePreference: String = "en", // de, en, tr, uk, ar
    
    @ColumnInfo(name = "is_emergency_service")
    val isEmergencyService: Boolean = false,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_contacted")
    val lastContacted: Long? = null,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
) {
    companion object {
        // Default emergency services for different countries
        fun getDefaultEmergencyServices(countryCode: String): List<EmergencyContact> {
            return when (countryCode.uppercase()) {
                "DE" -> listOf(
                    EmergencyContact(
                        name = "Notruf",
                        phoneNumber = "112",
                        relationship = "emergency_service",
                        priority = 1,
                        isEmergencyService = true,
                        languagePreference = "de"
                    )
                )
                "TR" -> listOf(
                    EmergencyContact(
                        name = "Acil Servis",
                        phoneNumber = "112",
                        relationship = "emergency_service",
                        priority = 1,
                        isEmergencyService = true,
                        languagePreference = "tr"
                    )
                )
                "UA" -> listOf(
                    EmergencyContact(
                        name = "Екстрена служба",
                        phoneNumber = "112",
                        relationship = "emergency_service",
                        priority = 1,
                        isEmergencyService = true,
                        languagePreference = "uk"
                    )
                )
                else -> listOf(
                    EmergencyContact(
                        name = "Emergency Services",
                        phoneNumber = "112",
                        relationship = "emergency_service",
                        priority = 1,
                        isEmergencyService = true,
                        languagePreference = "en"
                    )
                )
            }
        }
    }
}

/**
 * Emergency event log for tracking SOS activations
 * Required by Windsurf security rules for audit logging
 */
@Entity(tableName = "emergency_events")
data class EmergencyEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "event_type")
    val eventType: EmergencyEventType,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "contact_id")
    val contactId: String? = null,
    
    @ColumnInfo(name = "location_latitude")
    val locationLatitude: Double? = null,
    
    @ColumnInfo(name = "location_longitude")
    val locationLongitude: Double? = null,
    
    @ColumnInfo(name = "was_offline")
    val wasOffline: Boolean = false,
    
    @ColumnInfo(name = "response_time_ms")
    val responseTimeMs: Long? = null,
    
    @ColumnInfo(name = "user_language")
    val userLanguage: String,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "was_successful")
    val wasSuccessful: Boolean = true
)

enum class EmergencyEventType {
    SOS_ACTIVATED,
    EMERGENCY_CALL_MADE,
    CAREGIVER_NOTIFIED,
    LOCATION_SHARED,
    OFFLINE_EMERGENCY,
    SOS_CANCELLED,
    EMERGENCY_RESOLVED
}
