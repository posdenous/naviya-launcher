package com.naviya.launcher.emergency.data

import androidx.room.TypeConverter

/**
 * Type converters for Room database
 * Handles conversion between database types and application types
 * Required for proper Room operation with complex types
 */
class EmergencyTypeConverters {
    
    /**
     * Convert EmergencyEventType enum to String for database storage
     */
    @TypeConverter
    fun fromEmergencyEventType(eventType: EmergencyEventType): String {
        return eventType.name
    }
    
    /**
     * Convert String from database to EmergencyEventType enum
     */
    @TypeConverter
    fun toEmergencyEventType(value: String): EmergencyEventType {
        return try {
            EmergencyEventType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // Fallback for safety - important for elderly-focused applications
            EmergencyEventType.EMERGENCY_RESOLVED
        }
    }
}
