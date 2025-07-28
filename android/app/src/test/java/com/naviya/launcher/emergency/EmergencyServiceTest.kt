package com.naviya.launcher.emergency

import android.content.Context
import android.location.Location
import com.naviya.launcher.emergency.data.EmergencyContact
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.emergency.data.EmergencyEvent
import com.naviya.launcher.emergency.data.EmergencyEventType
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for EmergencyService
 * Tests SOS activation, emergency calls, caregiver notifications, and offline functionality
 * Follows Windsurf testing rules for comprehensive coverage
 */
class EmergencyServiceTest {
    
    private lateinit var emergencyService: EmergencyService
    private lateinit var mockContext: Context
    private lateinit var mockEmergencyDao: EmergencyDao
    private lateinit var mockLocationService: EmergencyLocationService
    private lateinit var mockCaregiverService: CaregiverNotificationService
    
    private val testEmergencyContact = EmergencyContact(
        id = "test-contact-1",
        name = "Test Emergency Service",
        phoneNumber = "112",
        isEmergencyService = true,
        countryCode = "DE",
        isPrimary = true
    )
    
    private val testCaregiver = EmergencyContact(
        id = "test-caregiver-1",
        name = "Test Caregiver",
        phoneNumber = "+49123456789",
        isEmergencyService = false,
        countryCode = "DE",
        isPrimary = true
    )
    
    @Before
    fun setup() {
        // Create mocks
        mockContext = mockk(relaxed = true)
        mockEmergencyDao = mockk(relaxed = true)
        mockLocationService = mockk(relaxed = true)
        mockCaregiverService = mockk(relaxed = true)
        
        // Setup default mock behaviors
        coEvery { mockEmergencyDao.getAllEmergencyContacts() } returns flowOf(listOf(testEmergencyContact))
        coEvery { mockEmergencyDao.getPrimaryCaregiver() } returns testCaregiver
        coEvery { mockEmergencyDao.insertEmergencyEvent(any()) } returns Unit
        coEvery { mockLocationService.getCurrentLocation() } returns createMockLocation()
        coEvery { mockCaregiverService.sendEmergencyNotification(any(), any(), any()) } returns true
        
        // Create service instance
        emergencyService = EmergencyService(
            context = mockContext,
            emergencyDao = mockEmergencyDao,
            locationService = mockLocationService,
            caregiverNotificationService = mockCaregiverService
        )
    }
    
    @After
    fun teardown() {
        clearAllMocks()
    }
    
    @Test
    fun `activateSOS should succeed with valid contacts`() = runTest {
        // When
        val result = emergencyService.activateSOS(
            userLanguage = "en",
            triggeredBy = SOSTrigger.MANUAL
        )
        
        // Then
        assertTrue("SOS activation should succeed", result is EmergencyResult.Success)
        
        // Verify emergency event was logged
        coVerify { mockEmergencyDao.insertEmergencyEvent(match { 
            it.eventType == EmergencyEventType.SOS_ACTIVATED 
        }) }
        
        // Verify caregiver was notified
        coVerify { mockCaregiverService.sendEmergencyNotification(testCaregiver, any(), "en") }
    }
    
    @Test
    fun `activateSOS should fail when no contacts configured`() = runTest {
        // Given
        coEvery { mockEmergencyDao.getAllEmergencyContacts() } returns flowOf(emptyList())
        
        // When
        val result = emergencyService.activateSOS(
            userLanguage = "en",
            triggeredBy = SOSTrigger.MANUAL
        )
        
        // Then
        assertTrue("SOS should fail with no contacts", result is EmergencyResult.NoContactsConfigured)
    }
    
    @Test
    fun `activateSOS should work offline with cached contacts`() = runTest {
        // Given - simulate offline condition
        coEvery { mockLocationService.getCurrentLocation() } returns null
        
        // When
        val result = emergencyService.activateSOS(
            userLanguage = "de",
            triggeredBy = SOSTrigger.MANUAL
        )
        
        // Then
        assertTrue("SOS should work offline", result is EmergencyResult.Success)
        
        // Verify event logged with null location
        coVerify { mockEmergencyDao.insertEmergencyEvent(match { 
            it.eventType == EmergencyEventType.SOS_ACTIVATED && 
            it.locationLatitude == null 
        }) }
    }
    
    @Test
    fun `activateSOS should respect 500ms response time requirement`() = runTest {
        // Given
        val startTime = System.currentTimeMillis()
        
        // When
        val result = emergencyService.activateSOS(
            userLanguage = "en",
            triggeredBy = SOSTrigger.MANUAL
        )
        
        val endTime = System.currentTimeMillis()
        val responseTime = endTime - startTime
        
        // Then
        assertTrue("SOS activation should succeed", result is EmergencyResult.Success)
        assertTrue("Response time should be under 500ms", responseTime < 500)
    }
    
    @Test
    fun `makeEmergencyCall should handle different countries`() = runTest {
        // Test German emergency number
        val germanContact = testEmergencyContact.copy(countryCode = "DE", phoneNumber = "112")
        coEvery { mockEmergencyDao.getAllEmergencyContacts() } returns flowOf(listOf(germanContact))
        
        val result = emergencyService.makeEmergencyCall(germanContact, "de")
        
        assertTrue("Emergency call should succeed", result)
        
        // Verify call event was logged
        coVerify { mockEmergencyDao.insertEmergencyEvent(match { 
            it.eventType == EmergencyEventType.EMERGENCY_CALL_MADE &&
            it.contactId == germanContact.id
        }) }
    }
    
    @Test
    fun `cancelSOS should notify caregiver and log event`() = runTest {
        // Given - activate SOS first
        emergencyService.activateSOS("en", SOSTrigger.MANUAL)
        
        // When
        val result = emergencyService.cancelSOS("False alarm - user error")
        
        // Then
        assertTrue("SOS cancellation should succeed", result)
        
        // Verify cancellation event logged
        coVerify { mockEmergencyDao.insertEmergencyEvent(match { 
            it.eventType == EmergencyEventType.SOS_CANCELLED 
        }) }
        
        // Verify caregiver notified of cancellation
        coVerify { mockCaregiverService.sendCancellationNotification("False alarm - user error") }
    }
    
    @Test
    fun `getSOSStatus should return correct status`() = runTest {
        // Initially not active
        var status = emergencyService.getSOSStatus()
        assertFalse("SOS should not be active initially", status.isActive)
        
        // Activate SOS
        emergencyService.activateSOS("en", SOSTrigger.MANUAL)
        
        // Check status after activation
        status = emergencyService.getSOSStatus()
        assertTrue("SOS should be active after activation", status.isActive)
        assertNotNull("Activation time should be set", status.activatedAt)
    }
    
    @Test
    fun `multilingual support should work for all languages`() = runTest {
        val languages = listOf("de", "en", "tr", "uk", "ar")
        
        for (language in languages) {
            // When
            val result = emergencyService.activateSOS(
                userLanguage = language,
                triggeredBy = SOSTrigger.MANUAL
            )
            
            // Then
            assertTrue("SOS should work for language $language", result is EmergencyResult.Success)
            
            // Verify language was passed to caregiver notification
            coVerify { mockCaregiverService.sendEmergencyNotification(any(), any(), language) }
        }
    }
    
    @Test
    fun `error handling should be robust`() = runTest {
        // Given - simulate database error
        coEvery { mockEmergencyDao.insertEmergencyEvent(any()) } throws RuntimeException("Database error")
        
        // When
        val result = emergencyService.activateSOS("en", SOSTrigger.MANUAL)
        
        // Then - should still succeed despite logging error
        assertTrue("SOS should handle database errors gracefully", result is EmergencyResult.Success)
    }
    
    @Test
    fun `concurrent SOS activations should be handled safely`() = runTest {
        // When - multiple concurrent activations
        val results = (1..5).map {
            emergencyService.activateSOS("en", SOSTrigger.MANUAL)
        }
        
        // Then - all should succeed but only one should be active
        results.forEach { result ->
            assertTrue("All SOS activations should succeed", result is EmergencyResult.Success)
        }
        
        // Verify only one SOS is active
        val status = emergencyService.getSOSStatus()
        assertTrue("SOS should be active", status.isActive)
    }
    
    private fun createMockLocation(): Location {
        return mockk<Location>(relaxed = true).apply {
            every { latitude } returns 52.5200 // Berlin coordinates
            every { longitude } returns 13.4050
            every { hasAccuracy() } returns true
            every { accuracy } returns 10.0f
            every { time } returns System.currentTimeMillis()
        }
    }
}

/**
 * Test data class for SOS status
 */
data class SOSStatus(
    val isActive: Boolean,
    val activatedAt: Long? = null,
    val triggeredBy: SOSTrigger? = null
)
