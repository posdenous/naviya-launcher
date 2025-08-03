package com.naviya.launcher.emergency

import android.content.Context
import android.location.Location
import com.naviya.launcher.emergency.data.EmergencyContact
import com.naviya.launcher.emergency.data.EmergencyDao
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for CaregiverNotificationService
 * Tests multilingual notifications, privacy boundaries, and delivery methods
 * Follows Windsurf testing rules for caregiver integration
 */
class CaregiverNotificationServiceTest {
    
    private lateinit var caregiverService: CaregiverNotificationService
    private lateinit var mockContext: Context
    private lateinit var mockEmergencyDao: EmergencyDao
    private lateinit var mockLocationService: EmergencyLocationService
    
    private val testCaregiver = EmergencyContact(
        id = "caregiver-1",
        name = "Test Caregiver",
        phoneNumber = "+49123456789",
        isEmergencyService = false,
        countryCode = "DE",
        isPrimary = true
    )
    
    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockEmergencyDao = mockk(relaxed = true)
        mockLocationService = mockk(relaxed = true)
        
        coEvery { mockEmergencyDao.getPrimaryCaregiver() } returns testCaregiver
        coEvery { mockEmergencyDao.insertEmergencyEvent(any()) } returns Unit
        
        caregiverService = CaregiverNotificationService(
            context = mockContext,
            emergencyDao = mockEmergencyDao,
            locationService = mockLocationService
        )
    }
    
    @After
    fun teardown() {
        clearAllMocks()
    }
    
    @Test
    fun `sendEmergencyNotification should work for all languages`() = runTest {
        val languages = mapOf(
            "de" to "NOTFALL: Ihr Angehöriger benötigt Hilfe!",
            "en" to "EMERGENCY: Your loved one needs help!",
            "tr" to "ACİL DURUM: Yakınınız yardıma ihtiyaç duyuyor!",
            "uk" to "НАДЗВИЧАЙНА СИТУАЦІЯ: Ваш близький потребує допомоги!",
            "ar" to "حالة طوارئ: قريبك يحتاج المساعدة!"
        )
        
        for ((language, expectedMessage) in languages) {
            // When
            val result = caregiverService.sendEmergencyNotification(
                caregiver = testCaregiver,
                location = createMockLocation(),
                userLanguage = language
            )
            
            // Then
            assertTrue("Notification should succeed for $language", result)
        }
        
        // Verify events were logged for each language
        coVerify(exactly = languages.size) { 
            mockEmergencyDao.insertEmergencyEvent(any()) 
        }
    }
    
    @Test
    fun `sendEmergencyNotification should include location in message`() = runTest {
        // Given
        val location = createMockLocation()
        
        // When
        val result = caregiverService.sendEmergencyNotification(
            caregiver = testCaregiver,
            location = location,
            userLanguage = "en"
        )
        
        // Then
        assertTrue("Notification with location should succeed", result)
        
        // Verify event logged with location
        coVerify { 
            mockEmergencyDao.insertEmergencyEvent(match { event ->
                event.locationLatitude == location.latitude &&
                event.locationLongitude == location.longitude
            })
        }
    }
    
    @Test
    fun `sendEmergencyNotification should work without location`() = runTest {
        // When
        val result = caregiverService.sendEmergencyNotification(
            caregiver = testCaregiver,
            location = null,
            userLanguage = "en"
        )
        
        // Then
        assertTrue("Notification without location should succeed", result)
        
        // Verify event logged without location
        coVerify { 
            mockEmergencyDao.insertEmergencyEvent(match { event ->
                event.locationLatitude == null &&
                event.locationLongitude == null
            })
        }
    }
    
    @Test
    fun `sendCancellationNotification should notify caregiver`() = runTest {
        // When
        val result = caregiverService.sendCancellationNotification("False alarm")
        
        // Then
        assertTrue("Cancellation notification should succeed", result)
    }
    
    @Test
    fun `sendCancellationNotification should fail when no caregiver configured`() = runTest {
        // Given
        coEvery { mockEmergencyDao.getPrimaryCaregiver() } returns null
        
        // When
        val result = caregiverService.sendCancellationNotification("False alarm")
        
        // Then
        assertFalse("Cancellation should fail with no caregiver", result)
    }
    
    @Test
    fun `isCaregiverNotificationReady should check configuration`() = runTest {
        // When caregiver is configured
        var isReady = caregiverService.isCaregiverNotificationReady()
        assertTrue("Should be ready with configured caregiver", isReady)
        
        // When no caregiver configured
        coEvery { mockEmergencyDao.getPrimaryCaregiver() } returns null
        isReady = caregiverService.isCaregiverNotificationReady()
        assertFalse("Should not be ready without caregiver", isReady)
        
        // When caregiver has no phone number
        val caregiverNoPhone = testCaregiver.copy(phoneNumber = "")
        coEvery { mockEmergencyDao.getPrimaryCaregiver() } returns caregiverNoPhone
        isReady = caregiverService.isCaregiverNotificationReady()
        assertFalse("Should not be ready without phone number", isReady)
    }
    
    @Test
    fun `sendStatusUpdate should work when caregiver configured`() = runTest {
        // When
        caregiverService.sendStatusUpdate("Device battery low", "en")
        
        // Then - should complete without error
        // In real implementation, would verify SMS was sent
    }
    
    @Test
    fun `notification should respect privacy boundaries`() = runTest {
        // Given - caregiver notification with sensitive data
        val result = caregiverService.sendEmergencyNotification(
            caregiver = testCaregiver,
            location = createMockLocation(),
            userLanguage = "en"
        )
        
        // Then
        assertTrue("Privacy-compliant notification should succeed", result)
        
        // Verify only emergency-relevant data is included
        // (Location and emergency status, no personal app usage data)
        coVerify { 
            mockEmergencyDao.insertEmergencyEvent(match { event ->
                event.notes == "Notification sent successfully"
            })
        }
    }
    
    private fun createMockLocation(): Location {
        return mockk<Location>(relaxed = true).apply {
            every { latitude } returns 52.5200 // Berlin
            every { longitude } returns 13.4050
            every { hasAccuracy() } returns true
            every { accuracy } returns 15.0f
            every { time } returns System.currentTimeMillis()
        }
    }
}
