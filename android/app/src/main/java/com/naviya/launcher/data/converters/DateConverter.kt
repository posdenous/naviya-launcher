package com.naviya.launcher.data.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room TypeConverter for Date objects
 * Converts Date to/from Long timestamp for database storage
 */
class DateConverter {
    
    // @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    // @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
