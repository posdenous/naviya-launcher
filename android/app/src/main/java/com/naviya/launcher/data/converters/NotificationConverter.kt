package com.naviya.launcher.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.naviya.launcher.data.models.*

/**
 * Room TypeConverter for notification-related complex data objects
 * Converts complex objects to/from JSON strings for database storage
 */
class NotificationConverter {
    
    private val gson = Gson()
    
    // UnreadSummary converter
    @TypeConverter
    fun fromUnreadSummary(value: UnreadSummary?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toUnreadSummary(value: String?): UnreadSummary? {
        return value?.let { gson.fromJson(it, UnreadSummary::class.java) }
    }
    
    // MissedCall List converter
    @TypeConverter
    fun fromMissedCallList(value: List<MissedCall>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toMissedCallList(value: String?): List<MissedCall>? {
        return value?.let {
            val listType = object : TypeToken<List<MissedCall>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    // UnreadSms List converter
    @TypeConverter
    fun fromUnreadSmsList(value: List<UnreadSms>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toUnreadSmsList(value: String?): List<UnreadSms>? {
        return value?.let {
            val listType = object : TypeToken<List<UnreadSms>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    // PriorityContacts converter
    @TypeConverter
    fun fromPriorityContacts(value: PriorityContacts?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toPriorityContacts(value: String?): PriorityContacts? {
        return value?.let { gson.fromJson(it, PriorityContacts::class.java) }
    }
    
    // TileDisplay converter
    @TypeConverter
    fun fromTileDisplay(value: TileDisplay?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toTileDisplay(value: String?): TileDisplay? {
        return value?.let { gson.fromJson(it, TileDisplay::class.java) }
    }
    
    // OfflineState converter
    @TypeConverter
    fun fromOfflineState(value: OfflineState?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toOfflineState(value: String?): OfflineState? {
        return value?.let { gson.fromJson(it, OfflineState::class.java) }
    }
    
    // CaregiverIntegration converter
    @TypeConverter
    fun fromCaregiverIntegration(value: CaregiverIntegration?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toCaregiverIntegration(value: String?): CaregiverIntegration? {
        return value?.let { gson.fromJson(it, CaregiverIntegration::class.java) }
    }
    
    // NotificationPrivacySettings converter
    @TypeConverter
    fun fromNotificationPrivacySettings(value: NotificationPrivacySettings?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toNotificationPrivacySettings(value: String?): NotificationPrivacySettings? {
        return value?.let { gson.fromJson(it, NotificationPrivacySettings::class.java) }
    }
    
    // PerformanceMetrics converter
    @TypeConverter
    fun fromPerformanceMetrics(value: PerformanceMetrics?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toPerformanceMetrics(value: String?): PerformanceMetrics? {
        return value?.let { gson.fromJson(it, PerformanceMetrics::class.java) }
    }
}
