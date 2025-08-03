package com.naviya.launcher.emergency

import android.content.Context
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
 * Integration tests for Medical Emergency System
 * Tests the integration between emergency response and medical compliance
 */
class MedicalEmergencyIntegrationTest {

    private lateinit var context: Context
    private lateinit var emergencyDao: EmergencyDao
    private lateinit var emergencyService: EmergencyService
    private lateinit var medicalEmergencyIntegration: SimpleMedicalEmergencyIntegration

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        context = mockk(relaxed = true)
        emergencyDao = mockk(relaxed = true)
        emergencyService = mockk(relaxed = true)
        
        medicalEmergencyIntegration = SimpleMedicalEmergencyIntegration(
            context = context,
            emergencyDao = emergencyDao,
            emergencyService = emergencyService
        )
        
        // Setup common mocks
        coEvery { emergencyDao.getAllActiveContactsSync() } returns listOf(
            EmergencyContact(
                contactId = "contact1",
                name = "Emergency Contact",
                phoneNumber = "911",
                relationship = "Emergency Services",
                isPrimary = true,
                isActive = true
            )
        )
        
        coEvery { emergencyDao.getActiveContactCount() } returns 1
        coEvery { emergencyService.isEmergencySystemReady() } returns true
        coEvery { emergencyService.activateSOS(any(), any()) } returns EmergencyResult.Success(
            results = emptyList(),
            responseTimeMs = 500L
        )
        
        coEvery { emergencyDao.insertEmergencyEvent(any()) } just Runs
        coEvery { emergencyDao.getRecentEmergencyEvents() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `activateMedicalEmergency should activate emergency for cardiac event`() = runTest {
        // Given
        val userId = "test_user"
        val emergencyType = MedicalEmergencyType.CARDIAC_EVENT
        val symptoms = listOf("chest pain", "difficulty breathing")

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType,
            symptoms = symptoms
        )

        // Then
        assertTrue("Medical emergency should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be critical priority", "CRITICAL", successResult.responseProtocol.priority)
        assertTrue("Should require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
        assertTrue("Response time should be reasonable", successResult.responseTimeMs < 5000)
        
        // Verify emergency service was called
        coVerify { emergencyService.activateSOS(any(), any()) }
        
        // Verify emergency event was logged
        coVerify { 
            emergencyDao.insertEmergencyEvent(
                match { event ->
                    event.eventType == EmergencyEventType.SOS_ACTIVATED &&
                    event.userId == userId &&
                    event.notes?.contains("Medical Emergency: $emergencyType") == true
                }
            )
        }
    }

    @Test
    fun `activateMedicalEmergency should handle fall with injury appropriately`() = runTest {
        // Given
        val userId = "test_user"
        val emergencyType = MedicalEmergencyType.FALL_WITH_INJURY

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType
        )

        // Then
        assertTrue("Medical emergency should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be high priority", "HIGH", successResult.responseProtocol.priority)
        assertTrue("Should require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
        
        // Verify emergency service was called
        coVerify { emergencyService.activateSOS(any(), any()) }
    }

    @Test
    fun `activateMedicalEmergency should handle medication emergency with medium priority`() = runTest {
        // Given
        val userId = "test_user"
        val emergencyType = MedicalEmergencyType.MEDICATION_EMERGENCY

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType
        )

        // Then
        assertTrue("Medical emergency should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be medium priority", "MEDIUM", successResult.responseProtocol.priority)
        assertFalse("Should not require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
    }

    @Test
    fun `activateMedicalEmergency should handle cognitive crisis appropriately`() = runTest {
        // Given
        val userId = "test_user"
        val emergencyType = MedicalEmergencyType.COGNITIVE_CRISIS

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType
        )

        // Then
        assertTrue("Medical emergency should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be medium priority", "MEDIUM", successResult.responseProtocol.priority)
        assertFalse("Should not require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
    }

    @Test
    fun `activateMedicalEmergency should handle general medical with low priority`() = runTest {
        // Given
        val userId = "test_user"
        val emergencyType = MedicalEmergencyType.GENERAL_MEDICAL

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType
        )

        // Then
        assertTrue("Medical emergency should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be low priority", "LOW", successResult.responseProtocol.priority)
        assertFalse("Should not require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
    }

    @Test
    fun `activateMedicalEmergency should handle emergency service failure gracefully`() = runTest {
        // Given
        val userId = "test_user"
        val emergencyType = MedicalEmergencyType.CARDIAC_EVENT
        
        coEvery { emergencyService.activateSOS(any(), any()) } throws RuntimeException("Emergency service failed")

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType
        )

        // Then
        assertTrue("Should return error result", result is SimpleMedicalEmergencyResult.Error)
        
        val errorResult = result as SimpleMedicalEmergencyResult.Error
        assertTrue("Error message should contain failure info", 
            errorResult.message.contains("Emergency service failed"))
    }

    @Test
    fun `isMedicalEmergencySystemReady should return true when system is ready`() = runTest {
        // Given
        coEvery { emergencyDao.getActiveContactCount() } returns 2
        coEvery { emergencyService.isEmergencySystemReady() } returns true

        // When
        val isReady = medicalEmergencyIntegration.isMedicalEmergencySystemReady()

        // Then
        assertTrue("System should be ready", isReady)
    }

    @Test
    fun `isMedicalEmergencySystemReady should return false when no contacts available`() = runTest {
        // Given
        coEvery { emergencyDao.getActiveContactCount() } returns 0
        coEvery { emergencyService.isEmergencySystemReady() } returns true

        // When
        val isReady = medicalEmergencyIntegration.isMedicalEmergencySystemReady()

        // Then
        assertFalse("System should not be ready without contacts", isReady)
    }

    @Test
    fun `isMedicalEmergencySystemReady should return false when emergency service not ready`() = runTest {
        // Given
        coEvery { emergencyDao.getActiveContactCount() } returns 2
        coEvery { emergencyService.isEmergencySystemReady() } returns false

        // When
        val isReady = medicalEmergencyIntegration.isMedicalEmergencySystemReady()

        // Then
        assertFalse("System should not be ready when emergency service not ready", isReady)
    }

    @Test
    fun `getMedicalEmergencyStats should return correct statistics`() = runTest {
        // Given
        val medicalEvents = listOf(
            EmergencyEvent(
                eventType = EmergencyEventType.SOS_ACTIVATED,
                userId = "user1",
                userLanguage = "en",
                wasOffline = false,
                notes = "Medical Emergency: CARDIAC_EVENT, Symptoms: chest pain",
                timestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
            ),
            EmergencyEvent(
                eventType = EmergencyEventType.SOS_ACTIVATED,
                userId = "user1",
                userLanguage = "en",
                wasOffline = false,
                notes = "Medical Emergency: FALL_WITH_INJURY, Symptoms: ",
                timestamp = System.currentTimeMillis() - 7200000 // 2 hours ago
            )
        )
        
        coEvery { emergencyDao.getRecentEmergencyEvents() } returns flowOf(medicalEvents)
        coEvery { emergencyDao.getActiveContactCount() } returns 3

        // When
        val stats = medicalEmergencyIntegration.getMedicalEmergencyStats()

        // Then
        assertEquals("Should count medical emergencies correctly", 2, stats.totalMedicalEmergencies)
        assertNotNull("Should have last medical emergency timestamp", stats.lastMedicalEmergency)
        assertTrue("System should be ready", stats.systemReady)
        assertEquals("Should have correct contact count", 3, stats.activeContacts)
    }

    @Test
    fun `response time should be within acceptable limits for all emergency types`() = runTest {
        val emergencyTypes = listOf(
            MedicalEmergencyType.CARDIAC_EVENT,
            MedicalEmergencyType.FALL_WITH_INJURY,
            MedicalEmergencyType.MEDICATION_EMERGENCY,
            MedicalEmergencyType.COGNITIVE_CRISIS,
            MedicalEmergencyType.GENERAL_MEDICAL
        )

        for (emergencyType in emergencyTypes) {
            // When
            val result = medicalEmergencyIntegration.activateMedicalEmergency(
                userId = "test_user",
                emergencyType = emergencyType
            )

            // Then
            assertTrue("Emergency $emergencyType should succeed", 
                result is SimpleMedicalEmergencyResult.Success)
            
            val successResult = result as SimpleMedicalEmergencyResult.Success
            assertTrue("Response time for $emergencyType should be under 5 seconds", 
                successResult.responseTimeMs < 5000)
        }
    }

    @Test
    fun `medical context logging should include all relevant information`() = runTest {
        // Given
        val userId = "test_user"
        val emergencyType = MedicalEmergencyType.CARDIAC_EVENT
        val symptoms = listOf("chest pain", "shortness of breath", "dizziness")

        // When
        medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType,
            symptoms = symptoms
        )

        // Then
        coVerify(exactly = 2) { emergencyDao.insertEmergencyEvent(any()) }
        
        // Verify main emergency event
        coVerify { 
            emergencyDao.insertEmergencyEvent(
                match { event ->
                    event.eventType == EmergencyEventType.SOS_ACTIVATED &&
                    event.userId == userId &&
                    event.notes?.contains("Medical Emergency: $emergencyType") == true &&
                    event.notes?.contains("Symptoms: ${symptoms.joinToString()}") == true
                }
            )
        }
        
        // Verify medical context event
        coVerify { 
            emergencyDao.insertEmergencyEvent(
                match { event ->
                    event.eventType == EmergencyEventType.SOS_ACTIVATED &&
                    event.userId == userId &&
                    event.notes?.contains("Medical Context") == true &&
                    event.notes?.contains("Type: $emergencyType") == true &&
                    event.notes?.contains("Priority: CRITICAL") == true
                }
            )
        }
    }
}
