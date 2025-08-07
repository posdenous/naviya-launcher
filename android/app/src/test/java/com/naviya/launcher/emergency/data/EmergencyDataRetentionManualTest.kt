package com.naviya.launcher.emergency.data

import android.content.Context
import android.util.Log
import com.naviya.launcher.config.EmergencyProductionConfig
import com.naviya.launcher.data.NaviyaDatabase
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manual test class for EmergencyDataRetention functionality
 * 
 * This is not a JUnit test but a manual verification script that can be run
 * to verify the functionality of the EmergencyDataRetention implementation.
 * 
 * To use:
 * 1. Add some old emergency events to the database
 * 2. Run the main function
 * 3. Verify that old events are soft-deleted
 */
object EmergencyDataRetentionManualTest {
    
    // Custom logger since we can't use Android's Log in a JVM test
    private fun log(level: String, message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        println("[$timestamp] $level: $message")
    }
    
    /**
     * Test the EmergencyDataRetentionWorker functionality
     */
    fun testRetentionWorker(context: Context) {
        log("INFO", "Starting manual test of EmergencyDataRetentionWorker")
        
        // Get database and DAO
        val database = NaviyaDatabase.getDatabase(context)
        val emergencyDao = database.emergencyDao()
        
        // Get configuration
        val config = EmergencyProductionConfig(context)
        val retentionDays = config.emergencyDataRetentionDays
        
        log("INFO", "Current retention period: $retentionDays days")
        
        // Calculate cutoff time
        val currentTime = System.currentTimeMillis()
        val cutoffTime = currentTime - (retentionDays * 24 * 60 * 60 * 1000L)
        val cutoffDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(cutoffTime))
        
        log("INFO", "Current time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(currentTime))}")
        log("INFO", "Cutoff time: $cutoffDate")
        
        // Count events before deletion
        runBlocking {
            val allEvents = emergencyDao.getAllEventsBlocking()
            val oldEvents = allEvents.filter { it.timestamp < cutoffTime }
            
            log("INFO", "Total events before deletion: ${allEvents.size}")
            log("INFO", "Old events (older than cutoff): ${oldEvents.size}")
            
            // Execute the deletion
            log("INFO", "Executing deleteOldEvents...")
            emergencyDao.deleteOldEvents(cutoffTime, currentTime)
            
            // Count events after deletion
            val allEventsAfter = emergencyDao.getAllEventsBlocking()
            val deletedEvents = allEventsAfter.filter { it.deletedAt != null }
            
            log("INFO", "Total events after deletion: ${allEventsAfter.size}")
            log("INFO", "Soft-deleted events: ${deletedEvents.size}")
            
            // Verify that old events are now marked as deleted
            val stillVisible = allEventsAfter.filter { it.timestamp < cutoffTime && it.deletedAt == null }
            
            if (stillVisible.isEmpty()) {
                log("SUCCESS", "All old events were properly soft-deleted!")
            } else {
                log("ERROR", "${stillVisible.size} old events were not soft-deleted!")
                stillVisible.forEach {
                    log("ERROR", "Event ID: ${it.id}, Timestamp: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.timestamp))}")
                }
            }
        }
        
        log("INFO", "Manual test completed")
    }
    
    /**
     * Test the EmergencyDataRetentionService functionality
     */
    fun testRetentionService(context: Context) {
        log("INFO", "Starting manual test of EmergencyDataRetentionService")
        
        // Get database and DAO
        val database = NaviyaDatabase.getDatabase(context)
        val emergencyDao = database.emergencyDao()
        
        // Create service
        val service = EmergencyDataRetentionService(context, emergencyDao)
        
        // Initialize service (schedules worker)
        service.initialize()
        log("INFO", "Service initialized, worker scheduled")
        
        // Perform manual cleanup
        runBlocking {
            log("INFO", "Performing manual cleanup...")
            service.performManualCleanup()
            log("INFO", "Manual cleanup completed")
        }
        
        log("INFO", "Manual test completed")
    }
}
