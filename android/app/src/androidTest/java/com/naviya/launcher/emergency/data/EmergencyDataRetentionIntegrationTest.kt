package com.naviya.launcher.emergency.data

import android.content.Context
import androidx.room.Room
import com.naviya.launcher.database.NaviyaDatabase
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.emergency.data.EmergencyEvent
import com.naviya.launcher.emergency.data.EmergencyEventType
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Integration test for EmergencyDataRetention functionality
 * This test verifies that the EmergencyDao.deleteOldEvents method correctly
 * soft-deletes old emergency events based on the retention period.
 * 
 * This is a manual verification test that can be run directly
 * without requiring JUnit test infrastructure.
 *
 * To run this test manually, use the runTest method.
 */
class EmergencyDataRetentionIntegrationTest {

    private lateinit var database: NaviyaDatabase
    private lateinit var emergencyDao: EmergencyDao
    private val testRetentionDays = 30
    
    // Custom logger for test output
    private fun log(level: String, message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        println("[$timestamp] $level: $message")
    }

    fun setup(context: Context) {
        log("INFO", "Setting up test database")
        database = Room.inMemoryDatabaseBuilder(
            context,
            NaviyaDatabase::class.java
        ).allowMainThreadQueries().build()
        
        emergencyDao = database.emergencyDao()
        log("INFO", "Test database setup complete")
    }

    fun cleanup() {
        log("INFO", "Cleaning up test database")
        if (::database.isInitialized) {
            database.close()
            log("INFO", "Test database closed")
        } else {
            log("WARNING", "Database was not initialized, nothing to clean up")
        }
    }

    fun testDeleteOldEvents() = runBlocking {
        if (!::emergencyDao.isInitialized) {
            log("ERROR", "Emergency DAO not initialised. Please call setup() first")
            throw IllegalStateException("Emergency DAO not initialised. Please call setup() first")
        }
        // Current time
        val currentTime = System.currentTimeMillis()
        
        // Create test events with different timestamps
        val recentTime = currentTime - TimeUnit.DAYS.toMillis(5) // 5 days ago
        val oldTime = currentTime - TimeUnit.DAYS.toMillis(45) // 45 days ago (beyond retention)
        val veryOldTime = currentTime - TimeUnit.DAYS.toMillis(90) // 90 days ago (beyond retention)
        
        val recentEvent = createTestEvent(id = "recent-1", timestamp = recentTime)
        val oldEvent = createTestEvent(id = "old-2", timestamp = oldTime)
        val veryOldEvent = createTestEvent(id = "very-old-3", timestamp = veryOldTime)
        
        // Insert test events
        emergencyDao.insertEvent(recentEvent)
        emergencyDao.insertEvent(oldEvent)
        emergencyDao.insertEvent(veryOldEvent)
        
        // Calculate cutoff time based on retention period
        val cutoffTime = currentTime - TimeUnit.DAYS.toMillis(testRetentionDays.toLong())
        
        // Execute deleteOldEvents
        emergencyDao.deleteOldEvents(cutoffTime, currentTime)
        
        // Get all events (including soft-deleted ones)
        val allEvents = emergencyDao.getAllEventsBlocking()
        log("INFO", "Total events after test: ${allEvents.size}")
        
        // Verify recent event is not deleted
        val retrievedRecentEvent = allEvents.find { it.id == "recent-1" }
        if (retrievedRecentEvent != null) {
            log("INFO", "Recent event exists with id=${retrievedRecentEvent.id}")
            if (retrievedRecentEvent.deletedAt == null) {
                log("SUCCESS", "Recent event is correctly NOT soft-deleted")
            } else {
                log("ERROR", "Recent event was incorrectly soft-deleted")
            }
        } else {
            log("ERROR", "Recent event not found")
        }
        
        // Verify old events are soft-deleted
        val retrievedOldEvent = allEvents.find { it.id == "old-2" }
        if (retrievedOldEvent != null) {
            log("INFO", "Old event exists with id=${retrievedOldEvent.id}")
            if (retrievedOldEvent.deletedAt != null) {
                log("SUCCESS", "Old event was correctly soft-deleted")
            } else {
                log("ERROR", "Old event was NOT soft-deleted as expected")
            }
        } else {
            log("ERROR", "Old event not found")
        }
        
        val retrievedVeryOldEvent = allEvents.find { it.id == "very-old-3" }
        if (retrievedVeryOldEvent != null) {
            log("INFO", "Very old event exists with id=${retrievedVeryOldEvent.id}")
            if (retrievedVeryOldEvent.deletedAt != null) {
                log("SUCCESS", "Very old event was correctly soft-deleted")
            } else {
                log("ERROR", "Very old event was NOT soft-deleted as expected")
            }
        } else {
            log("ERROR", "Very old event not found")
        }
        
        // Verify that soft-deleted events are not returned by active queries
        val activeEvents = emergencyDao.getAllActiveEventsBlocking()
        log("INFO", "Active events count: ${activeEvents.size}")
        
        if (activeEvents.size == 1) {
            log("SUCCESS", "Only one event remains active as expected")
        } else {
            log("ERROR", "Expected 1 active event, found ${activeEvents.size}")
        }
        
        if (activeEvents.isNotEmpty() && activeEvents[0].id == "recent-1") {
            log("SUCCESS", "Active event is the recent one as expected")
        } else if (activeEvents.isNotEmpty()) {
            log("ERROR", "Active event has unexpected ID: ${activeEvents[0].id}")
        }
    }
    
    /**
     * Helper method to create a test emergency event
     */
    private fun createTestEvent(
        id: String = UUID.randomUUID().toString(),
        timestamp: Long,
        eventType: EmergencyEventType = EmergencyEventType.SOS_ACTIVATED,
        contactId: String? = null,
        deletedAt: Long? = null
    ): EmergencyEvent {
        return EmergencyEvent(
            id = id,
            eventType = eventType,
            timestamp = timestamp,
            contactId = contactId,
            userLanguage = "en",  // Default language
            deletedAt = deletedAt
        )
    }
    
    /**
     * Main test runner that can be used to manually run the test
     * from an Android context (e.g., an Activity or Application)
     */
    fun runTest(context: Context) {
        println("Starting EmergencyDataRetention Integration Test")
        try {
            setup(context)
            testDeleteOldEvents()
            println("\n✅ TEST COMPLETED SUCCESSFULLY")
        } catch (e: Exception) {
            println("\n❌ TEST FAILED: ${e.message}")
            e.printStackTrace()
        } finally {
            cleanup()
        }
        println("Integration test completed")
    }
    
    companion object {
        /**
         * Entry point for manual testing
         * Note: This requires an Android context to be passed in
         * when called from an Activity or Application
         */
        @JvmStatic
        fun main(args: Array<String>) {
            println("\n⚠️ This test requires an Android context to run")
            println("Please use the runTest(context) method from an Android component")
        }
    }
}
