package com.naviya.launcher.emergency.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Emergency functionality
 * Follows Windsurf rules for Room database usage and offline-first functionality
 */
@Dao
interface EmergencyDao {
    
    companion object {
        // SQL constants for commonly used WHERE clauses
        const val NOT_DELETED = "deleted_at IS NULL"
        const val ACTIVE_CONTACTS = "is_active = 1 AND $NOT_DELETED"
        const val PRIMARY_CAREGIVER = "is_primary_caregiver = 1"
        const val EMERGENCY_SERVICE = "is_emergency_service = 1"
        const val CRITICAL_CONTACTS = "$ACTIVE_CONTACTS AND ($PRIMARY_CAREGIVER OR $EMERGENCY_SERVICE)"
        const val ORDER_BY_PRIORITY = "ORDER BY priority ASC"
    }
    
    // Emergency Contacts CRUD operations
    @Query("SELECT * FROM emergency_contacts WHERE $ACTIVE_CONTACTS $ORDER_BY_PRIORITY")
    fun getAllActiveContacts(): Flow<List<EmergencyContact>>
    
    @Query("SELECT * FROM emergency_contacts WHERE $ACTIVE_CONTACTS $ORDER_BY_PRIORITY")
    suspend fun getAllActiveContactsBlocking(): List<EmergencyContact>
    
    @Query("SELECT * FROM emergency_contacts WHERE $PRIMARY_CAREGIVER AND $ACTIVE_CONTACTS LIMIT 1")
    suspend fun getPrimaryCaregiver(): EmergencyContact?
    
    @Query("SELECT * FROM emergency_contacts WHERE $EMERGENCY_SERVICE AND $ACTIVE_CONTACTS $ORDER_BY_PRIORITY LIMIT 1")
    suspend fun getEmergencyService(): EmergencyContact?
    
    @Query("SELECT * FROM emergency_contacts WHERE priority = 1 AND $ACTIVE_CONTACTS LIMIT 1")
    suspend fun getHighestPriorityContact(): EmergencyContact?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<EmergencyContact>)
    
    @Update
    suspend fun updateContact(contact: EmergencyContact)
    
    @Query("UPDATE emergency_contacts SET last_contacted = :timestamp WHERE id = :contactId")
    suspend fun updateLastContacted(contactId: String, timestamp: Long)
    
    /**
     * Soft delete a contact by setting the deleted_at timestamp
     * This preserves data for GDPR compliance while removing it from active use
     */
    @Query("UPDATE emergency_contacts SET deleted_at = :timestamp, is_active = 0 WHERE id = :contactId")
    suspend fun deleteContact(contactId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE emergency_contacts SET is_active = 0 WHERE id = :contactId")
    suspend fun deactivateContact(contactId: String)
    
    // Emergency Events logging (required by Windsurf audit rules)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyEvent(event: EmergencyEvent)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EmergencyEvent)
    
    @Query("SELECT * FROM emergency_events WHERE $NOT_DELETED ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEmergencyEvents(): Flow<List<EmergencyEvent>>
    
    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC")
    suspend fun getAllEventsBlocking(): List<EmergencyEvent>
    
    @Query("SELECT * FROM emergency_events WHERE $NOT_DELETED ORDER BY timestamp DESC")
    suspend fun getAllActiveEventsBlocking(): List<EmergencyEvent>
    
    @Query("SELECT * FROM emergency_events WHERE event_type = :eventType AND $NOT_DELETED ORDER BY timestamp DESC LIMIT 10")
    suspend fun getEventsByType(eventType: EmergencyEventType): List<EmergencyEvent>
    
    @Query("SELECT COUNT(*) FROM emergency_events WHERE event_type = 'SOS_ACTIVATED' AND timestamp > :since AND $NOT_DELETED")
    suspend fun getSOSActivationCount(since: Long): Int
    
    @Query("SELECT * FROM emergency_events WHERE timestamp > :since AND $NOT_DELETED ORDER BY timestamp DESC")
    suspend fun getEventsAfter(since: Long): List<EmergencyEvent>
    
    // Offline emergency support queries
    @Query("SELECT * FROM emergency_contacts WHERE $CRITICAL_CONTACTS $ORDER_BY_PRIORITY")
    suspend fun getCriticalContacts(): List<EmergencyContact>
    
    @Query("SELECT COUNT(*) FROM emergency_contacts WHERE $ACTIVE_CONTACTS")
    suspend fun getActiveContactCount(): Int
    
    // Cleanup old events (privacy compliance)
    /**
     * Soft delete old events by setting the deleted_at timestamp
     * This preserves data for GDPR compliance and audit requirements
     * while removing it from active queries
     */
    @Query("UPDATE emergency_events SET deleted_at = :currentTime WHERE timestamp < :cutoffTime AND deleted_at IS NULL")
    suspend fun deleteOldEvents(cutoffTime: Long, currentTime: Long = System.currentTimeMillis())
    
    // Statistics for caregiver dashboard
    @Query("""
        SELECT event_type, COUNT(*) as count 
        FROM emergency_events 
        WHERE timestamp > :since AND $NOT_DELETED
        GROUP BY event_type
    """)
    suspend fun getEventStatistics(since: Long): List<EventStatistic>
}

/**
 * Data class for event statistics
 * Properly annotated for Room to map SQL query results
 * Uses embedded annotations for proper type conversion
 */
class EventStatistic {
    @ColumnInfo(name = "event_type")
    lateinit var eventType: EmergencyEventType
    
    @ColumnInfo(name = "count")
    var count: Int = 0
}
