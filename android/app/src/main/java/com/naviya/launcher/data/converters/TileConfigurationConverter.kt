package com.naviya.launcher.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.naviya.launcher.data.models.*

/**
 * Room TypeConverter for complex data objects
 * Converts complex objects to/from JSON strings for database storage
 */
class TileConfigurationConverter {
    
    private val gson = Gson()
    
    // TileConfiguration List converter
    @TypeConverter
    fun fromTileConfigurationList(value: List<TileConfiguration>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toTileConfigurationList(value: String?): List<TileConfiguration>? {
        return value?.let {
            val listType = object : TypeToken<List<TileConfiguration>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    // GridConfiguration converter
    @TypeConverter
    fun fromGridConfiguration(value: GridConfiguration?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toGridConfiguration(value: String?): GridConfiguration? {
        return value?.let { gson.fromJson(it, GridConfiguration::class.java) }
    }
    
    // AccessibilitySettings converter
    @TypeConverter
    fun fromAccessibilitySettings(value: AccessibilitySettings?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toAccessibilitySettings(value: String?): AccessibilitySettings? {
        return value?.let { gson.fromJson(it, AccessibilitySettings::class.java) }
    }
    
    // PinProtectionSettings converter
    @TypeConverter
    fun fromPinProtectionSettings(value: PinProtectionSettings?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toPinProtectionSettings(value: String?): PinProtectionSettings? {
        return value?.let { gson.fromJson(it, PinProtectionSettings::class.java) }
    }
    
    // CrashRecoverySettings converter
    @TypeConverter
    fun fromCrashRecoverySettings(value: CrashRecoverySettings?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toCrashRecoverySettings(value: String?): CrashRecoverySettings? {
        return value?.let { gson.fromJson(it, CrashRecoverySettings::class.java) }
    }
    
    // OfflineModeSettings converter
    @TypeConverter
    fun fromOfflineModeSettings(value: OfflineModeSettings?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toOfflineModeSettings(value: String?): OfflineModeSettings? {
        return value?.let { gson.fromJson(it, OfflineModeSettings::class.java) }
    }
    
    // LocalizationSettings converter
    @TypeConverter
    fun fromLocalizationSettings(value: LocalizationSettings?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toLocalizationSettings(value: String?): LocalizationSettings? {
        return value?.let { gson.fromJson(it, LocalizationSettings::class.java) }
    }
    
    // String List converter
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, listType)
        }
    }
    
    // Enum converters
    @TypeConverter
    fun fromTileType(value: TileType?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toTileType(value: String?): TileType? {
        return value?.let { TileType.valueOf(it) }
    }
    
    @TypeConverter
    fun fromTilePriority(value: TilePriority?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toTilePriority(value: String?): TilePriority? {
        return value?.let { TilePriority.valueOf(it) }
    }
    
    @TypeConverter
    fun fromColorBlindnessType(value: ColorBlindnessType?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toColorBlindnessType(value: String?): ColorBlindnessType? {
        return value?.let { ColorBlindnessType.valueOf(it) }
    }
    
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? {
        return value?.let { SyncStatus.valueOf(it) }
    }
}
