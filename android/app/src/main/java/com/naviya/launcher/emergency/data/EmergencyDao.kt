package com.naviya.launcher.emergency.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Emergency functionality
 * Follows Windsurf rules for Room database usage and offline-first functionality
 */
@Dao
interface EmergencyDao {
    
    // Emergency Contacts CRUD operations
    @Query("SELECT * FROM emergency_contacts WHERE is_active = 1 ORDER BY priority ASC")
    fun getAllActiveContacts(): Flow<List<EmergencyContact>>
    
    @Query("SELECT * FROM emergency_contacts WHERE is_active = 1 ORDER BY priority ASC")
    suspend fun getAllActiveContactsSync(): List<EmergencyContact>
    
    @Query("SELECT * FROM emergency_contacts WHERE is_primary_caregiver = 1 AND is_active = 1 LIMIT 1")
    suspend fun getPrimaryCaregiver(): EmergencyContact?
    
    @Query("SELECT * FROM emergency_contacts WHERE is_emergency_service = 1 AND is_active = 1 ORDER BY priority ASC LIMIT 1")
    suspend fun getEmergencyService(): EmergencyContact?
    
    @Query("SELECT * FROM emergency_contacts WHERE priority = 1 AND is_active = 1 LIMIT 1")
    suspend fun getHighestPriorityContact(): EmergencyContact?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<EmergencyContact>)
    
    @Update
    suspend fun updateContact(contact: EmergencyContact)
    
    @Query("UPDATE emergency_contacts SET last_contacted = :timestamp WHERE id = :contactId")
    suspend fun updateLastContacted(contactId: String, timestamp: Long)
    
    @Query("DELETE FROM emergency_contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)
    
    @Query("UPDATE emergency_contacts SET is_active = 0 WHERE id = :contactId")
    suspend fun deactivateContact(contactId: String)
    
    // Emergency Events logging (required by Windsurf audit rules)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyEvent(event: EmergencyEvent)
    
    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEmergencyEvents(): Flow<List<EmergencyEvent>>
    
    @Query("SELECT * FROM emergency_events WHERE event_type = :eventType ORDER BY timestamp DESC LIMIT 10")
    suspend fun getEventsByType(eventType: EmergencyEventType): List<EmergencyEvent>
    
    @Query("SELECT COUNT(*) FROM emergency_events WHERE event_type = 'SOS_ACTIVATED' AND timestamp > :since")
    suspend fun getSOSActivationCount(since: Long): Int
    
    @Query("SELECT * FROM emergency_events WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getEventsAfter(since: Long): List<EmergencyEvent>
    
    // Offline emergency support queries
    @Query("SELECT * FROM emergency_contacts WHERE is_active = 1 AND (is_emergency_service = 1 OR is_primary_caregiver = 1) ORDER BY priority ASC")
    suspend fun getCriticalContacts(): List<EmergencyContact>
    
    @Query("SELECT COUNT(*) FROM emergency_contacts WHERE is_active = 1")
    suspend fun getActiveContactCount(): Int
    
    // Cleanup old events (privacy compliance)
    @Query("DELETE FROM emergency_events WHERE timestamp < :cutoffTime")
    suspend fun deleteOldEvents(cutoffTime: Long)
    
    // Statistics for caregiver dashboard
    @Query("""
        SELECT event_type, COUNT(*) as count 
        FROM emergency_events 
        WHERE timestamp > :since 
        GROUP BY event_type
    """)
    suspend fun getEventStatistics(since: Long): List<EventStatistic>
}

/**
 * Data class for event statistics
 */
data class EventStatistic(
    val event_type: EmergencyEventType,
    val count: Int
)
