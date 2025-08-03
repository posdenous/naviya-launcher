package com.naviya.launcher.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for analytics and usage tracking.
 * Follows Windsurf rules for privacy-compliant analytics and elderly user behavior tracking.
 */
@Dao
interface AnalyticsDao {
    
    /**
     * Insert app launch event for usage analytics
     */
    @Insert
    suspend fun insertAppLaunchEvent(event: AppLaunchEvent)
    
    /**
     * Insert mode change event for layout analytics
     */
    @Insert
    suspend fun insertModeChangeEvent(event: ModeChangeEvent)
    
    /**
     * Insert emergency activation event for safety analytics
     */
    @Insert
    suspend fun insertEmergencyEvent(event: AnalyticsEmergencyEvent)
    
    /**
     * Get app launch events for a specific time period
     */
    @Query("SELECT * FROM app_launch_events WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getAppLaunchEvents(startTime: Long, endTime: Long): List<AppLaunchEvent>
    
    /**
     * Get mode change events for layout optimization
     */
    @Query("SELECT * FROM mode_change_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getModeChangeEvents(startTime: Long): List<ModeChangeEvent>
    
    /**
     * Get emergency events for safety monitoring
     */
    @Query("SELECT * FROM emergency_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getEmergencyEvents(startTime: Long): List<AnalyticsEmergencyEvent>
    
    /**
     * Get most frequently used apps
     */
    @Query("SELECT package_name, COUNT(*) as launch_count FROM app_launch_events WHERE timestamp >= :startTime GROUP BY package_name ORDER BY launch_count DESC LIMIT :limit")
    suspend fun getMostUsedApps(startTime: Long, limit: Int): List<AppUsageStats>
    
    /**
     * Get mode usage statistics
     */
    @Query("SELECT to_mode, COUNT(*) as usage_count FROM mode_change_events WHERE timestamp >= :startTime GROUP BY to_mode ORDER BY usage_count DESC")
    suspend fun getModeUsageStats(startTime: Long): List<ModeUsageStats>
    
    /**
     * Clean up old analytics data (privacy compliance)
     */
    @Query("DELETE FROM app_launch_events WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldAppLaunchEvents(cutoffTime: Long)
    
    @Query("DELETE FROM mode_change_events WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldModeChangeEvents(cutoffTime: Long)
    
    @Query("DELETE FROM analytics_emergency_events WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldEmergencyEvents(cutoffTime: Long)
    
    /**
     * Helper method to insert app launch event with current timestamp
     */
    suspend fun insertAppLaunchEvent(packageName: String, timestamp: Long, mode: String) {
        insertAppLaunchEvent(
            AppLaunchEvent(
                packageName = packageName,
                timestamp = timestamp,
                mode = mode
            )
        )
    }
    
    /**
     * Helper method to insert mode change event with current timestamp
     */
    suspend fun insertModeChangeEvent(fromMode: String, toMode: String, timestamp: Long) {
        insertModeChangeEvent(
            ModeChangeEvent(
                fromMode = fromMode,
                toMode = toMode,
                timestamp = timestamp
            )
        )
    }
    
    /**
     * Helper method to insert emergency event with current timestamp
     */
    suspend fun insertEmergencyEvent(eventType: String, triggerSource: String, timestamp: Long) {
        insertEmergencyEvent(
            AnalyticsEmergencyEvent(
                eventType = eventType,
                triggerSource = triggerSource,
                timestamp = timestamp
            )
        )
    }
}

/**
 * App launch event entity for tracking app usage
 */
@Entity(tableName = "app_launch_events")
data class AppLaunchEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "package_name")
    val packageName: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "mode")
    val mode: String
)

/**
 * Mode change event entity for tracking layout preferences
 */
@Entity(tableName = "mode_change_events")
data class ModeChangeEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "from_mode")
    val fromMode: String,
    
    @ColumnInfo(name = "to_mode")
    val toMode: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)

/**
 * Emergency event entity for safety monitoring
 */
@Entity(tableName = "analytics_emergency_events")
data class AnalyticsEmergencyEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "event_type")
    val eventType: String, // "activation", "cancellation", "completion"
    
    @ColumnInfo(name = "trigger_source")
    val triggerSource: String, // "main_button", "voice_command", etc.
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "location_lat")
    val locationLat: Double? = null,
    
    @ColumnInfo(name = "location_lng")
    val locationLng: Double? = null,
    
    @ColumnInfo(name = "response_time_ms")
    val responseTimeMs: Long? = null
)

/**
 * App usage statistics result
 */
data class AppUsageStats(
    @ColumnInfo(name = "package_name")
    val packageName: String,
    
    @ColumnInfo(name = "launch_count")
    val launchCount: Int
)

/**
 * Mode usage statistics result
 */
data class ModeUsageStats(
    @ColumnInfo(name = "to_mode")
    val mode: String,
    
    @ColumnInfo(name = "usage_count")
    val usageCount: Int
)
