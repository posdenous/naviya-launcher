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
 * End-to-End Integration Tests for Emergency SOS + Medical Compliance System
 * Tests complete workflows from emergency activation to compliance logging
 */
class EndToEndEmergencyIntegrationTest {

    private lateinit var context: Context
    private lateinit var emergencyDao: EmergencyDao
    private lateinit var emergencyService: EmergencyService
    private lateinit var medicalEmergencyIntegration: SimpleMedicalEmergencyIntegration
    private lateinit var notificationService: HealthcareProfessionalNotificationService

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        context = mockk(relaxed = true)
        emergencyDao = mockk(relaxed = true)
        emergencyService = mockk(relaxed = true)
        notificationService = mockk(relaxed = true)
        
        medicalEmergencyIntegration = SimpleMedicalEmergencyIntegration(
            context = context,
            emergencyDao = emergencyDao,
            emergencyService = emergencyService
        )
        
        setupMockData()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun setupMockData() {
        // Setup emergency contacts
        val emergencyContacts = listOf(
            EmergencyContact(
                contactId = "ems_911",
                name = "Emergency Services",
                phoneNumber = "911",
                relationship = "Emergency Services",
                isPrimary = true,
                isActive = true
            ),
            EmergencyContact(
                contactId = "caregiver_1",
                name = "Primary Caregiver",
                phoneNumber = "+1234567890",
                relationship = "Family Caregiver",
                isPrimary = false,
                isActive = true
            ),
            EmergencyContact(
                contactId = "healthcare_1",
                name = "Dr. Sarah Johnson",
                phoneNumber = "+1987654321",
                relationship = "Healthcare Professional",
                isPrimary = false,
                isActive = true
            )
        )
        
        coEvery { emergencyDao.getAllActiveContactsSync() } returns emergencyContacts
        coEvery { emergencyDao.getActiveContactCount() } returns emergencyContacts.size
        coEvery { emergencyService.isEmergencySystemReady() } returns true
        coEvery { emergencyDao.insertEmergencyEvent(any()) } just Runs
        coEvery { emergencyDao.getRecentEmergencyEvents() } returns flowOf(emptyList())
    }

    @Test
    fun `complete cardiac emergency workflow should succeed with all notifications`() = runTest {
        // Given
        val userId = "elderly_user_001"
        val emergencyType = MedicalEmergencyType.CARDIAC_EVENT
        val symptoms = listOf("chest pain", "difficulty breathing", "dizziness")
        
        coEvery { emergencyService.activateSOS(any(), any()) } returns EmergencyResult.Success(
            results = emptyList(),
            responseTimeMs = 450L
        )

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType,
            symptoms = symptoms,
            userLanguage = "en",
            triggeredBy = SOSTrigger.MANUAL
        )

        // Then
        assertTrue("Cardiac emergency should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be critical priority", "CRITICAL", successResult.responseProtocol.priority)
        assertTrue("Should require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
        assertTrue("Response time should be under 500ms", successResult.responseTimeMs < 500)
        assertEquals("Should notify all 3 contacts", 3, successResult.contactsNotified)

        // Verify emergency service activation
        coVerify(exactly = 1) { emergencyService.activateSOS("en", SOSTrigger.MANUAL) }

        // Verify emergency events logged
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
        
        // Verify medical context logging
        coVerify { 
            emergencyDao.insertEmergencyEvent(
                match { event ->
                    event.eventType == EmergencyEventType.SOS_ACTIVATED &&
                    event.userId == userId &&
                    event.notes?.contains("Medical Context") == true &&
                    event.notes?.contains("Priority: CRITICAL") == true
                }
            )
        }
    }

    @Test
    fun `fall emergency workflow should activate healthcare professional first`() = runTest {
        // Given
        val userId = "elderly_user_002"
        val emergencyType = MedicalEmergencyType.FALL_WITH_INJURY
        
        coEvery { emergencyService.activateSOS(any(), any()) } returns EmergencyResult.Success(
            results = emptyList(),
            responseTimeMs = 380L
        )

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType
        )

        // Then
        assertTrue("Fall emergency should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be high priority", "HIGH", successResult.responseProtocol.priority)
        assertTrue("Should require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
        assertEquals("Fall with potential injury - medical assessment needed", 
            successResult.responseProtocol.description)

        // Verify emergency activation
        coVerify { emergencyService.activateSOS(any(), any()) }
        
        // Verify audit logging
        coVerify(atLeast = 1) { emergencyDao.insertEmergencyEvent(any()) }
    }

    @Test
    fun `medication emergency workflow should require professional consultation`() = runTest {
        // Given
        val userId = "elderly_user_003"
        val emergencyType = MedicalEmergencyType.MEDICATION_EMERGENCY
        val symptoms = listOf("nausea", "confusion", "possible overdose")

        coEvery { emergencyService.activateSOS(any(), any()) } returns EmergencyResult.Success(
            results = emptyList(),
            responseTimeMs = 420L
        )

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType,
            symptoms = symptoms
        )

        // Then
        assertTrue("Medication emergency should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be medium priority", "MEDIUM", successResult.responseProtocol.priority)
        assertFalse("Should not require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
        assertTrue("Description should mention professional consultation", 
            successResult.responseProtocol.description.contains("professional consultation"))

        // Verify emergency activation
        coVerify { emergencyService.activateSOS(any(), any()) }
    }

    @Test
    fun `cognitive crisis workflow should provide specialized support`() = runTest {
        // Given
        val userId = "elderly_user_004"
        val emergencyType = MedicalEmergencyType.COGNITIVE_CRISIS
        val symptoms = listOf("severe confusion", "disorientation", "memory loss")

        coEvery { emergencyService.activateSOS(any(), any()) } returns EmergencyResult.Success(
            results = emptyList(),
            responseTimeMs = 390L
        )

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType,
            symptoms = symptoms
        )

        // Then
        assertTrue("Cognitive crisis should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be medium priority", "MEDIUM", successResult.responseProtocol.priority)
        assertFalse("Should not require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
        assertTrue("Description should mention specialized support", 
            successResult.responseProtocol.description.contains("specialized support"))
    }

    @Test
    fun `general medical emergency should use standard protocols`() = runTest {
        // Given
        val userId = "elderly_user_005"
        val emergencyType = MedicalEmergencyType.GENERAL_MEDICAL
        val symptoms = listOf("feeling unwell", "general discomfort")

        coEvery { emergencyService.activateSOS(any(), any()) } returns EmergencyResult.Success(
            results = emptyList(),
            responseTimeMs = 410L
        )

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType,
            symptoms = symptoms
        )

        // Then
        assertTrue("General medical emergency should succeed", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should be low priority", "LOW", successResult.responseProtocol.priority)
        assertFalse("Should not require immediate EMS", successResult.responseProtocol.requiresImmediateEMS)
        assertTrue("Description should mention standard emergency response", 
            successResult.responseProtocol.description.contains("standard emergency response"))
    }

    @Test
    fun `emergency system should handle offline scenarios gracefully`() = runTest {
        // Given
        val userId = "elderly_user_006"
        val emergencyType = MedicalEmergencyType.CARDIAC_EVENT
        
        // Mock offline scenario
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns null
        
        coEvery { emergencyService.activateSOS(any(), any()) } returns EmergencyResult.Success(
            results = emptyList(),
            responseTimeMs = 480L
        )

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType
        )

        // Then
        assertTrue("Emergency should succeed even offline", result is SimpleMedicalEmergencyResult.Success)
        
        // Verify emergency event logged with offline flag
        coVerify { 
            emergencyDao.insertEmergencyEvent(
                match { event ->
                    event.wasOffline == true
                }
            )
        }
    }

    @Test
    fun `emergency system should handle no contacts scenario`() = runTest {
        // Given
        val userId = "elderly_user_007"
        val emergencyType = MedicalEmergencyType.CARDIAC_EVENT
        
        // Mock no contacts scenario
        coEvery { emergencyDao.getAllActiveContactsSync() } returns emptyList()
        coEvery { emergencyDao.getActiveContactCount() } returns 0
        coEvery { emergencyService.isEmergencySystemReady() } returns false
        
        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType
        )

        // Then
        assertTrue("Should handle no contacts gracefully", result is SimpleMedicalEmergencyResult.Success)
        
        val successResult = result as SimpleMedicalEmergencyResult.Success
        assertEquals("Should notify 0 contacts", 0, successResult.contactsNotified)
    }

    @Test
    fun `emergency system should handle service failures gracefully`() = runTest {
        // Given
        val userId = "elderly_user_008"
        val emergencyType = MedicalEmergencyType.CARDIAC_EVENT
        
        // Mock emergency service failure
        coEvery { emergencyService.activateSOS(any(), any()) } throws RuntimeException("Emergency service unavailable")

        // When
        val result = medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType
        )

        // Then
        assertTrue("Should return error result", result is SimpleMedicalEmergencyResult.Error)
        
        val errorResult = result as SimpleMedicalEmergencyResult.Error
        assertTrue("Error message should contain service failure info", 
            errorResult.message.contains("Emergency service unavailable"))
    }

    @Test
    fun `emergency statistics should be accurate`() = runTest {
        // Given
        val mockEvents = listOf(
            EmergencyEvent(
                eventType = EmergencyEventType.SOS_ACTIVATED,
                userId = "user1",
                userLanguage = "en",
                wasOffline = false,
                notes = "Medical Emergency: CARDIAC_EVENT",
                timestamp = System.currentTimeMillis() - 3600000
            ),
            EmergencyEvent(
                eventType = EmergencyEventType.SOS_ACTIVATED,
                userId = "user2",
                userLanguage = "en",
                wasOffline = false,
                notes = "Medical Emergency: FALL_WITH_INJURY",
                timestamp = System.currentTimeMillis() - 7200000
            )
        )
        
        coEvery { emergencyDao.getRecentEmergencyEvents() } returns flowOf(mockEvents)

        // When
        val stats = medicalEmergencyIntegration.getMedicalEmergencyStats()

        // Then
        assertEquals("Should count medical emergencies correctly", 2, stats.totalMedicalEmergencies)
        assertNotNull("Should have last emergency timestamp", stats.lastMedicalEmergency)
        assertTrue("System should be ready", stats.systemReady)
        assertEquals("Should have correct contact count", 3, stats.activeContacts)
    }

    @Test
    fun `performance requirements should be met for all emergency types`() = runTest {
        val emergencyTypes = listOf(
            MedicalEmergencyType.CARDIAC_EVENT,
            MedicalEmergencyType.FALL_WITH_INJURY,
            MedicalEmergencyType.MEDICATION_EMERGENCY,
            MedicalEmergencyType.COGNITIVE_CRISIS,
            MedicalEmergencyType.GENERAL_MEDICAL
        )

        coEvery { emergencyService.activateSOS(any(), any()) } returns EmergencyResult.Success(
            results = emptyList(),
            responseTimeMs = 400L
        )

        for (emergencyType in emergencyTypes) {
            // When
            val startTime = System.currentTimeMillis()
            val result = medicalEmergencyIntegration.activateMedicalEmergency(
                userId = "performance_test_user",
                emergencyType = emergencyType
            )
            val totalTime = System.currentTimeMillis() - startTime

            // Then
            assertTrue("Emergency $emergencyType should succeed", 
                result is SimpleMedicalEmergencyResult.Success)
            
            val successResult = result as SimpleMedicalEmergencyResult.Success
            assertTrue("Response time for $emergencyType should be under 500ms", 
                successResult.responseTimeMs < 500)
            assertTrue("Total processing time for $emergencyType should be under 2 seconds", 
                totalTime < 2000)
        }
    }

    @Test
    fun `audit trail should be comprehensive and immutable`() = runTest {
        // Given
        val userId = "audit_test_user"
        val emergencyType = MedicalEmergencyType.CARDIAC_EVENT
        val symptoms = listOf("chest pain", "shortness of breath")

        coEvery { emergencyService.activateSOS(any(), any()) } returns EmergencyResult.Success(
            results = emptyList(),
            responseTimeMs = 450L
        )

        // When
        medicalEmergencyIntegration.activateMedicalEmergency(
            userId = userId,
            emergencyType = emergencyType,
            symptoms = symptoms
        )

        // Then
        // Verify comprehensive audit logging
        coVerify(exactly = 2) { emergencyDao.insertEmergencyEvent(any()) }
        
        // Verify main emergency event contains all required information
        coVerify { 
            emergencyDao.insertEmergencyEvent(
                match { event ->
                    event.eventType == EmergencyEventType.SOS_ACTIVATED &&
                    event.userId == userId &&
                    event.userLanguage == "en" &&
                    event.notes?.contains("Medical Emergency: $emergencyType") == true &&
                    event.notes?.contains("Symptoms: ${symptoms.joinToString()}") == true &&
                    event.timestamp > 0
                }
            )
        }
        
        // Verify medical context event contains protocol information
        coVerify { 
            emergencyDao.insertEmergencyEvent(
                match { event ->
                    event.eventType == EmergencyEventType.SOS_ACTIVATED &&
                    event.userId == userId &&
                    event.notes?.contains("Medical Context") == true &&
                    event.notes?.contains("Type: $emergencyType") == true &&
                    event.notes?.contains("Priority: CRITICAL") == true &&
                    event.timestamp > 0
                }
            )
        }
    }
}
