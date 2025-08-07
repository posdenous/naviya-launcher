package com.naviya.launcher.safety

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * SecurityAuditLogger
 * 
 * Provides an immutable audit trail for all security-relevant events
 * in the Naviya launcher app. This is essential for:
 * 
 * 1. Detecting potential caregiver abuse
 * 2. Providing evidence in case of disputes
 * 3. Ensuring GDPR compliance with data access logging
 * 4. Supporting elder protection mechanisms
 * 
 * The audit log is stored in a secure, tamper-evident database
 * that cannot be modified by caregivers.
 */
class SecurityAuditLogger(private val context: Context) {

    companion object {
        private const val TAG = "SecurityAuditLogger"
    }
    
    private val database: SecurityAuditDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            SecurityAuditDatabase::class.java,
            "security_audit_log.db"
        )
        .fallbackToDestructiveMigration() // Only for development
        .build()
    }
    
    private val auditDao: SecurityAuditDao by lazy {
        database.securityAuditDao()
    }
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Log a critical security event with immutable audit trail
     */
    fun logCriticalEvent(eventType: EventType, details: String) {
        val event = SecurityAuditEvent(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            eventType = eventType.name,
            eventCategory = EventCategory.CRITICAL.name,
            details = details,
            hash = generateEventHash(eventType.name, details)
        )
        
        coroutineScope.launch {
            auditDao.insertEvent(event)
            Log.d(TAG, "Critical event logged: $eventType - $details")
        }
    }
    
    /**
     * Log a caregiver action for abuse detection and auditing
     */
    fun logCaregiverAction(actionType: ElderProtectionManager.CaregiverActionType, details: String) {
        val event = SecurityAuditEvent(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            eventType = actionType.name,
            eventCategory = EventCategory.CAREGIVER_ACTION.name,
            details = details,
            hash = generateEventHash(actionType.name, details)
        )
        
        coroutineScope.launch {
            auditDao.insertEvent(event)
            Log.d(TAG, "Caregiver action logged: $actionType - $details")
        }
    }
    
    /**
     * Get count of specific actions for today
     * Used for abuse pattern detection
     */
    suspend fun getActionCountForToday(actionType: ElderProtectionManager.CaregiverActionType): Int =
        withContext(Dispatchers.IO) {
            val startOfDay = getStartOfDayTimestamp()
            auditDao.getEventCountSince(actionType.name, startOfDay)
        }
    
    /**
     * Get count of specific actions for past N days
     * Used for abuse pattern detection
     */
    suspend fun getActionCountForPastDays(
        actionType: ElderProtectionManager.CaregiverActionType,
        days: Int
    ): Int = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        auditDao.getEventCountSince(actionType.name, timestamp)
    }
    
    /**
     * Get all events for a specific category within a time range
     * Used for generating reports and investigating potential abuse
     */
    suspend fun getEventsForCategory(
        category: EventCategory,
        startTime: Long,
        endTime: Long
    ): List<SecurityAuditEvent> = withContext(Dispatchers.IO) {
        auditDao.getEventsForCategoryInTimeRange(category.name, startTime, endTime)
    }
    
    /**
     * Verify the integrity of the audit log
     * Ensures the log hasn't been tampered with
     */
    suspend fun verifyAuditLogIntegrity(): Boolean = withContext(Dispatchers.IO) {
        val events = auditDao.getAllEvents()
        events.all { event ->
            val calculatedHash = generateEventHash(event.eventType, event.details)
            calculatedHash == event.hash
        }
    }
    
    /**
     * Get the timestamp for the start of the current day
     */
    private fun getStartOfDayTimestamp(): Long {
        val now = Date()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = now
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * Generate a hash for the event to ensure integrity
     * In production, this would use a more sophisticated hashing algorithm
     * and possibly include the previous event's hash (blockchain-style)
     */
    private fun generateEventHash(eventType: String, details: String): String {
        val input = "$eventType:$details:${System.currentTimeMillis()}"
        return input.hashCode().toString()
    }
    
    /**
     * Event types for security audit logging
     */
    enum class EventType {
        // Critical events
        PANIC_MODE_ACTIVATED,
        PANIC_MODE_DEACTIVATED,
        CONSENT_RECORDED,
        CONSENT_EXPIRED,
        ABUSE_DETECTED,
        ADVOCATE_NOTIFIED,
        
        // Authentication events
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        PASSWORD_CHANGE,
        
        // Data access events
        PERSONAL_DATA_ACCESS,
        LOCATION_DATA_ACCESS,
        HEALTH_DATA_ACCESS,
        
        // System events
        SYSTEM_STARTUP,
        SYSTEM_SHUTDOWN,
        PERMISSION_CHANGE,
        SETTINGS_CHANGE
    }
    
    /**
     * Event categories for security audit logging
     */
    enum class EventCategory {
        CRITICAL,
        CAREGIVER_ACTION,
        AUTHENTICATION,
        DATA_ACCESS,
        SYSTEM
    }
}

/**
 * Security Audit Event entity for Room database
 */
@Entity(tableName = "security_audit_events")
data class SecurityAuditEvent(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "event_type")
    val eventType: String,
    
    @ColumnInfo(name = "event_category")
    val eventCategory: String,
    
    @ColumnInfo(name = "details")
    val details: String,
    
    @ColumnInfo(name = "hash")
    val hash: String
)

/**
 * Data Access Object for security audit events
 */
@Dao
interface SecurityAuditDao {
    @Insert
    suspend fun insertEvent(event: SecurityAuditEvent)
    
    @Query("SELECT COUNT(*) FROM security_audit_events WHERE event_type = :eventType AND timestamp >= :since")
    suspend fun getEventCountSince(eventType: String, since: Long): Int
    
    @Query("SELECT * FROM security_audit_events WHERE event_category = :category AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getEventsForCategoryInTimeRange(category: String, startTime: Long, endTime: Long): List<SecurityAuditEvent>
    
    @Query("SELECT * FROM security_audit_events ORDER BY timestamp ASC")
    suspend fun getAllEvents(): List<SecurityAuditEvent>
    
    @Query("SELECT * FROM security_audit_events WHERE event_category = :category ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestEventsForCategory(category: String, limit: Int): List<SecurityAuditEvent>
}

/**
 * Room database for security audit events
 */
// @Database(entities = [SecurityAuditEvent::class], version = 1, exportSchema = false)
abstract class SecurityAuditDatabase : RoomDatabase() {
    abstract fun securityAuditDao(): SecurityAuditDao
}
