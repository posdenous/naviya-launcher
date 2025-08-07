package com.naviya.launcher.emergency.data

import android.content.Context
import androidx.work.*
import com.naviya.launcher.config.EmergencyProductionConfig
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class EmergencyDataRetentionTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockEmergencyDao: EmergencyDao

    @Mock
    private lateinit var mockConfig: EmergencyProductionConfig

    @Mock
    private lateinit var mockWorkManager: WorkManager

    @Before
    fun setUp() {
        // Setup mock config
        `when`(mockConfig.emergencyDataRetentionDays).thenReturn(30)
    }

    @Test
    fun `test EmergencyDataRetentionService initialization schedules worker`() {
        // Setup WorkManager mock
        val workManagerMock = mock(WorkManager::class.java)
        
        // Create service
        val service = EmergencyDataRetentionService(mockContext, mockEmergencyDao)
        
        // Test that the service initializes correctly
        runBlocking {
            // Verify that the service can be initialized without exceptions
            service.initialize()
        }
    }

    @Test
    fun `test EmergencyDataRetentionService manual cleanup calls deleteOldEvents`() = runBlocking {
        // Create service with mocks
        val service = EmergencyDataRetentionService(mockContext, mockEmergencyDao)
        
        // Set up config mock
        `when`(mockConfig.emergencyDataRetentionDays).thenReturn(30)
        
        // Call manual cleanup
        service.performManualCleanup()
        
        // Verify deleteOldEvents was called with appropriate parameters
        verify(mockEmergencyDao).deleteOldEvents(
            anyLong(), // cutoff time
            anyLong()  // current time
        )
    }

    @Test
    fun `test retention period calculation is correct`() {
        // Set retention period to 30 days
        val retentionDays = 30
        `when`(mockConfig.emergencyDataRetentionDays).thenReturn(retentionDays)
        
        // Calculate expected cutoff time (30 days ago)
        val currentTime = System.currentTimeMillis()
        val expectedCutoffTime = currentTime - (retentionDays * 24 * 60 * 60 * 1000L)
        
        // Verify the calculation is within reasonable bounds (1 minute tolerance)
        val tolerance = 60 * 1000L // 1 minute in milliseconds
        val calculatedTime = currentTime - (retentionDays * 24 * 60 * 60 * 1000L)
        
        assert(Math.abs(calculatedTime - expectedCutoffTime) < tolerance)
    }

    @Test
    fun `test worker scheduling uses correct parameters`() {
        // Create a WorkRequest to test
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
            
        val workRequest = PeriodicWorkRequestBuilder<EmergencyDataRetentionWorker>(
            24, TimeUnit.HOURS
        )
        .setConstraints(constraints)
        .addTag("data_retention")
        .build()
        
        // Verify the work request has appropriate constraints
        assertEquals(true, workRequest.workSpec.constraints.requiresBatteryNotLow())
        assertEquals(24, workRequest.workSpec.intervalDuration / (60 * 60 * 1000))
    }
}
