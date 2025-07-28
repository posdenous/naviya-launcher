package com.naviya.launcher.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.naviya.launcher.data.models.*

/**
 * Room TypeConverter for crash recovery-related complex data objects
 * Converts complex objects to/from JSON strings for database storage
 */
class CrashRecoveryConverter {
    
    private val gson = Gson()
    
    // CrashTracking converter
    @TypeConverter
    fun fromCrashTracking(value: CrashTracking?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toCrashTracking(value: String?): CrashTracking? {
        return value?.let { gson.fromJson(it, CrashTracking::class.java) }
    }
    
    // CrashRecord List converter
    @TypeConverter
    fun fromCrashRecordList(value: List<CrashRecord>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toCrashRecordList(value: String?): List<CrashRecord>? {
        return value?.let {
            val listType = object : TypeToken<List<CrashRecord>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    // RecoveryMode converter
    @TypeConverter
    fun fromRecoveryMode(value: RecoveryMode?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toRecoveryMode(value: String?): RecoveryMode? {
        return value?.let { gson.fromJson(it, RecoveryMode::class.java) }
    }
    
    // SafeTiles converter
    @TypeConverter
    fun fromSafeTiles(value: SafeTiles?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toSafeTiles(value: String?): SafeTiles? {
        return value?.let { gson.fromJson(it, SafeTiles::class.java) }
    }
    
    // RecoveryAssistance converter
    @TypeConverter
    fun fromRecoveryAssistance(value: RecoveryAssistance?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toRecoveryAssistance(value: String?): RecoveryAssistance? {
        return value?.let { gson.fromJson(it, RecoveryAssistance::class.java) }
    }
    
    // ExitConditions converter
    @TypeConverter
    fun fromExitConditions(value: ExitConditions?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toExitConditions(value: String?): ExitConditions? {
        return value?.let { gson.fromJson(it, ExitConditions::class.java) }
    }
    
    // PostRecovery converter
    @TypeConverter
    fun fromPostRecovery(value: PostRecovery?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toPostRecovery(value: String?): PostRecovery? {
        return value?.let { gson.fromJson(it, PostRecovery::class.java) }
    }
    
    // SystemIntegration converter
    @TypeConverter
    fun fromSystemIntegration(value: SystemIntegration?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toSystemIntegration(value: String?): SystemIntegration? {
        return value?.let { gson.fromJson(it, SystemIntegration::class.java) }
    }
    
    // RecoveryAnalytics converter
    @TypeConverter
    fun fromRecoveryAnalytics(value: RecoveryAnalytics?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toRecoveryAnalytics(value: String?): RecoveryAnalytics? {
        return value?.let { gson.fromJson(it, RecoveryAnalytics::class.java) }
    }
}
