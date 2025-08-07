package com.naviya.launcher.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.naviya.launcher.toggle.ToggleMode

/**
 * Room database type converters for the Naviya launcher app.
 * Focused on essential converters for the 3-mode launcher system.
 */
class SharedTypeConverters {
    // Initialize Gson instance to fix unresolved reference errors
    private val gson = Gson()

    // ToggleMode converters for the 3-mode system (ESSENTIAL, COMFORT, CONNECTED)
    @TypeConverter
    fun fromToggleMode(mode: ToggleMode): String {
        return mode.name
    }

    @TypeConverter
    fun toToggleMode(modeName: String): ToggleMode {
        return ToggleMode.valueOf(modeName)
    }

    // List<String> converters (used for various purposes including unread notifications)
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Int List converters (used for notification counts)
    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toIntList(json: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Boolean converters
    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value == 1
    }

    // Map<String, Int> converters (for unread counts by type)
    @TypeConverter
    fun fromStringIntMap(map: Map<String, Int>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toStringIntMap(json: String): Map<String, Int> {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}
