package com.naviya.launcher.abuse

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.naviya.launcher.abuse.data.*
import com.naviya.launcher.caregiver.CaregiverPermissionManager
import com.naviya.launcher.contacts.data.ContactDao
import com.naviya.launcher.emergency.EmergencyService
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

/**
 * Comprehensive unit tests for RuleBasedAbuseDetector
 * Tests all detection rules, risk scoring, and alert generation
 */
@RunWith(MockitoJUnitRunner::class)
class RuleBasedAbuseDetectorTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var contactDao: ContactDao

    @Mock
    private lateinit var caregiverPermissionManager: CaregiverPermissionManager

    @Mock
    private lateinit var emergencyService: EmergencyService

    @Mock
    private lateinit var abuseDao: AbuseDetectionDao

    private lateinit var abuseDetector: RuleBasedAbuseDetector

    private val testUserId = "test-user-123"
    private val testCaregiverId = "test-caregiver-456"
    private val currentTime = System.currentTimeMillis()

    @Before
    fun setUp() {
        abuseDetector = RuleBasedAbuseDetector(
            context = context,
            contactDao = contactDao,
            caregiverPermissionManager = caregiverPermissionManager,
            emergencyService = emergencyService,
            abuseDao = abuseDao
        )
    }

    // ==================== RULE 1: CONTACT MANIPULATION TESTS ====================

    @Test
    fun `test contact manipulation detection - multiple blocked removal attempts`() = runTest {
        // Given: Multiple blocked contact removal attempts
        val blockedAttempts = createMultipleBlockedContactAttempts(5, ContactAction.REMOVE_CONTACT)
        val behaviorData = createBehaviorData(contactAttempts = blockedAttempts)
        
        `when`(contactDao.getRecentContactModificationAttempts(any(), any(), any()))
            .thenReturn(blockedAttempts)
        `when`(caregiverPermissionManager.getPermissionHistory(any(), any()))
            .thenReturn(emptyList())
        `when`(emergencyService.getCaregiverEmergencyInteractions(any(), any(), any()))
            .thenReturn(emptyList())
        `when`(abuseDao.getRecentRiskAssessments(any(), any(), any()))
            .thenReturn(emptyList())

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect social isolation pattern
        assertTrue("Should generate risk assessment", assessment.riskScore > 0)
        assertTrue("Should be at least medium risk", assessment.riskLevel >= AbuseRiskLevel.MEDIUM)
        
        val contactManipulationFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.CONTACT_MANIPULATION 
        }
        assertNotNull("Should detect contact manipulation", contactManipulationFactor)
        assertEquals("Should calculate correct score", 75, contactManipulationFactor?.score) // 5 * 15 = 75
        assertTrue("Should identify social isolation pattern", 
                  contactManipulationFactor?.evidence?.get("pattern") == "social_isolation_attempt")
    }

    @Test
    fun `test emergency contact tampering detection - critical risk`() = runTest {
        // Given: Attempts to tamper with emergency contacts
        val emergencyTamperAttempts = listOf(
            createContactAttempt(
                action = ContactAction.REMOVE_CONTACT,
                result = ContactActionResult.BLOCKED_BY_PROTECTION,
                contactInfo = ContactInfo("Emergency Contact", "+911", "emergency")
            ),
            createContactAttempt(
                action = ContactAction.BLOCK_CONTACT,
                result = ContactActionResult.BLOCKED_BY_PROTECTION,
                contactInfo = ContactInfo("Emergency Doctor", "+123", "emergency")
            )
        )
        
        setupMockBehaviorData(contactAttempts = emergencyTamperAttempts)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect emergency contact tampering
        val tamperFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.EMERGENCY_CONTACT_TAMPERING 
        }
        assertNotNull("Should detect emergency contact tampering", tamperFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, tamperFactor?.severity)
        assertEquals("Should calculate correct score", 50, tamperFactor?.score) // 2 * 25 = 50
        assertTrue("Should identify safety system compromise", 
                  tamperFactor?.evidence?.get("pattern") == "safety_system_compromise")
    }

    @Test
    fun `test burst activity detection - rapid succession of attempts`() = runTest {
        // Given: Multiple attempts within one hour
        val burstAttempts = createBurstActivityAttempts(4) // Above threshold of 3
        setupMockBehaviorData(contactAttempts = burstAttempts)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect burst activity pattern
        val burstFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.BURST_ACTIVITY 
        }
        assertNotNull("Should detect burst activity", burstFactor)
        assertEquals("Should be medium severity", AbuseSeverity.MEDIUM, burstFactor?.severity)
        assertEquals("Should calculate correct score", 30, burstFactor?.score)
        assertTrue("Should identify aggressive burst pattern", 
                  burstFactor?.evidence?.get("pattern") == "aggressive_burst")
    }

    // ==================== RULE 2: PERMISSION ESCALATION TESTS ====================

    @Test
    fun `test permission escalation detection - repeated denied requests`() = runTest {
        // Given: Multiple permission escalation attempts
        val permissionHistory = createPermissionEscalationAttempts(4) // Above threshold of 2
        setupMockBehaviorData(permissionHistory = permissionHistory)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect permission escalation
        val escalationFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.PERMISSION_ESCALATION 
        }
        assertNotNull("Should detect permission escalation", escalationFactor)
        assertEquals("Should be medium severity", AbuseSeverity.MEDIUM, escalationFactor?.severity)
        assertEquals("Should calculate correct score", 40, escalationFactor?.score) // 4 * 10 = 40
        assertTrue("Should identify control escalation pattern", 
                  escalationFactor?.evidence?.get("pattern") == "control_escalation")
    }

    @Test
    fun `test sensitive permission request detection - privacy invasion`() = runTest {
        // Given: Attempts to access sensitive permissions
        val sensitivePermissionHistory = listOf(
            createPermissionHistoryEntry("access_location", "DENIED"),
            createPermissionHistoryEntry("access_contacts", "DENIED"),
            createPermissionHistoryEntry("disable_panic_mode", "DENIED")
        )
        setupMockBehaviorData(permissionHistory = sensitivePermissionHistory)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect sensitive permission requests
        val sensitivePermissionFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.SENSITIVE_PERMISSION_REQUEST 
        }
        assertNotNull("Should detect sensitive permission requests", sensitivePermissionFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, sensitivePermissionFactor?.severity)
        assertEquals("Should calculate correct score", 60, sensitivePermissionFactor?.score) // 3 * 20 = 60
        assertTrue("Should identify privacy invasion attempt", 
                  sensitivePermissionFactor?.evidence?.get("pattern") == "privacy_invasion_attempt")
    }

    // ==================== RULE 3: TEMPORAL PATTERN TESTS ====================

    @Test
    fun `test night-time activity detection - covert manipulation`() = runTest {
        // Given: Multiple attempts during night hours (11 PM - 6 AM)
        val nightTimeAttempts = createNightTimeAttempts(6) // Above threshold of 5
        setupMockBehaviorData(contactAttempts = nightTimeAttempts)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect suspicious timing
        val timingFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.SUSPICIOUS_TIMING &&
            it.evidence["pattern"] == "covert_manipulation"
        }
        assertNotNull("Should detect night-time activity", timingFactor)
        assertEquals("Should be medium severity", AbuseSeverity.MEDIUM, timingFactor?.severity)
        assertEquals("Should calculate correct score", 48, timingFactor?.score) // 6 * 8 = 48
    }

    @Test
    fun `test weekend activity concentration - isolation exploitation`() = runTest {
        // Given: High concentration of weekend activity (70% of total)
        val weekendAttempts = createWeekendAttempts(7) // 7 weekend attempts
        val weekdayAttempts = createWeekdayAttempts(3) // 3 weekday attempts
        val allAttempts = weekendAttempts + weekdayAttempts // 70% weekend concentration
        
        setupMockBehaviorData(contactAttempts = allAttempts)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect weekend concentration
        val timingFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.SUSPICIOUS_TIMING &&
            it.evidence["pattern"] == "isolation_exploitation"
        }
        assertNotNull("Should detect weekend concentration", timingFactor)
        assertEquals("Should be low severity", AbuseSeverity.LOW, timingFactor?.severity)
        assertEquals("Should calculate correct score", 20, timingFactor?.score)
    }

    // ==================== RULE 4: EMERGENCY SYSTEM ABUSE TESTS ====================

    @Test
    fun `test emergency system tampering - safety compromise`() = runTest {
        // Given: Attempts to disable emergency features
        val emergencyInteractions = listOf(
            EmergencyInteraction(
                actionType = "DISABLE_EMERGENCY_BUTTON",
                actionResult = "BLOCKED"
            ),
            EmergencyInteraction(
                actionType = "MODIFY_EMERGENCY_CONTACTS",
                actionResult = "BLOCKED"
            )
        )
        setupMockBehaviorData(emergencyInteractions = emergencyInteractions)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect safety system tampering
        val tamperFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.SAFETY_SYSTEM_TAMPERING 
        }
        assertNotNull("Should detect safety system tampering", tamperFactor)
        assertEquals("Should be critical severity", AbuseSeverity.CRITICAL, tamperFactor?.severity)
        assertEquals("Should calculate correct score", 80, tamperFactor?.score) // 2 * 40 = 80
        assertTrue("Should identify safety compromise", 
                  tamperFactor?.evidence?.get("pattern") == "safety_compromise")
    }

    @Test
    fun `test excessive surveillance detection - monitoring abuse`() = runTest {
        // Given: Excessive emergency system queries (25 queries > 20 threshold)
        val surveillanceInteractions = (1..25).map {
            EmergencyInteraction(
                actionType = "QUERY_EMERGENCY_STATUS",
                actionResult = "SUCCESS"
            )
        }
        setupMockBehaviorData(emergencyInteractions = surveillanceInteractions)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect surveillance pattern
        val surveillanceFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.SURVEILLANCE_PATTERN 
        }
        assertNotNull("Should detect surveillance pattern", surveillanceFactor)
        assertEquals("Should be low severity", AbuseSeverity.LOW, surveillanceFactor?.severity)
        assertEquals("Should calculate correct score", 15, surveillanceFactor?.score)
        assertTrue("Should identify excessive surveillance", 
                  surveillanceFactor?.evidence?.get("pattern") == "excessive_surveillance")
    }

    // ==================== RULE 5: ESCALATING BEHAVIOR TESTS ====================

    @Test
    fun `test escalating behavior detection - increasing risk over time`() = runTest {
        // Given: Previous assessments showing escalating risk scores
        val previousAssessments = listOf(
            createRiskAssessment(riskScore = 20, timestamp = currentTime - TimeUnit.DAYS.toMillis(3)),
            createRiskAssessment(riskScore = 45, timestamp = currentTime - TimeUnit.DAYS.toMillis(2)),
            createRiskAssessment(riskScore = 65, timestamp = currentTime - TimeUnit.DAYS.toMillis(1))
        )
        setupMockBehaviorData(previousAssessments = previousAssessments)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should detect escalating behavior
        val escalatingFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.ESCALATING_BEHAVIOR 
        }
        assertNotNull("Should detect escalating behavior", escalatingFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, escalatingFactor?.severity)
        assertEquals("Should calculate correct score", 25, escalatingFactor?.score)
        assertTrue("Should identify escalating abuse pattern", 
                  escalatingFactor?.evidence?.get("pattern") == "escalating_abuse")
    }

    // ==================== RULE 6: TRIGGER EVENT TESTS ====================

    @Test
    fun `test trigger event analysis - panic mode activation`() = runTest {
        // Given: Panic mode activation trigger
        val panicTrigger = AbuseDetectionTrigger(
            eventType = TriggerEventType.PANIC_MODE_ACTIVATION,
            eventData = mapOf("panic_reason" to "user_distress"),
            severity = TriggerSeverity.HIGH
        )
        setupMockBehaviorData()

        // When: Analyzing caregiver behavior with trigger
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId, panicTrigger)

        // Then: Should analyze trigger event
        val triggerFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.TRIGGER_EVENT 
        }
        assertNotNull("Should analyze trigger event", triggerFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, triggerFactor?.severity)
        assertEquals("Should calculate correct score", 30, triggerFactor?.score)
        assertTrue("Should reference panic context", 
                  triggerFactor?.evidence?.containsKey("panic_context") == true)
    }

    @Test
    fun `test emergency contact tampering trigger - critical response`() = runTest {
        // Given: Emergency contact tampering trigger
        val tamperTrigger = AbuseDetectionTrigger(
            eventType = TriggerEventType.EMERGENCY_CONTACT_TAMPERING,
            eventData = mapOf("contact_id" to "emergency-123"),
            severity = TriggerSeverity.CRITICAL
        )
        setupMockBehaviorData()

        // When: Analyzing caregiver behavior with trigger
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId, tamperTrigger)

        // Then: Should analyze trigger event with high score
        val triggerFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.TRIGGER_EVENT 
        }
        assertNotNull("Should analyze trigger event", triggerFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, triggerFactor?.severity)
        assertEquals("Should calculate correct score", 40, triggerFactor?.score)
    }

    // ==================== RISK LEVEL DETERMINATION TESTS ====================

    @Test
    fun `test risk level determination - minimal risk`() = runTest {
        // Given: Low risk behavior (score = 15)
        val minimalAttempts = createMultipleBlockedContactAttempts(1, ContactAction.REMOVE_CONTACT)
        setupMockBehaviorData(contactAttempts = minimalAttempts)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should be minimal risk
        assertTrue("Should be minimal or low risk", assessment.riskLevel <= AbuseRiskLevel.LOW)
        assertTrue("Should have low risk score", assessment.riskScore < 25)
    }

    @Test
    fun `test risk level determination - critical risk`() = runTest {
        // Given: High risk behavior (emergency tampering + escalation + burst)
        val criticalBehavior = createCriticalRiskBehavior()
        setupMockBehaviorData(
            contactAttempts = criticalBehavior.contactAttempts,
            permissionHistory = criticalBehavior.permissionHistory,
            emergencyInteractions = criticalBehavior.emergencyInteractions
        )

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should be critical risk
        assertEquals("Should be critical risk", AbuseRiskLevel.CRITICAL, assessment.riskLevel)
        assertTrue("Should have high risk score", assessment.riskScore >= 100)
    }

    // ==================== ALERT GENERATION TESTS ====================

    @Test
    fun `test alert generation - medium risk triggers alert`() = runTest {
        // Given: Medium risk behavior
        val mediumRiskAttempts = createMultipleBlockedContactAttempts(4, ContactAction.REMOVE_CONTACT)
        setupMockBehaviorData(contactAttempts = mediumRiskAttempts)

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should generate alert
        assertTrue("Should be medium risk or higher", assessment.riskLevel >= AbuseRiskLevel.MEDIUM)
        verify(abuseDao).insertAbuseAlert(any())
    }

    @Test
    fun `test alert generation - critical risk triggers immediate action`() = runTest {
        // Given: Critical risk behavior
        val criticalBehavior = createCriticalRiskBehavior()
        setupMockBehaviorData(
            contactAttempts = criticalBehavior.contactAttempts,
            emergencyInteractions = criticalBehavior.emergencyInteractions
        )

        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)

        // Then: Should generate critical alert and trigger notifications
        assertEquals("Should be critical risk", AbuseRiskLevel.CRITICAL, assessment.riskLevel)
        verify(abuseDao).insertAbuseAlert(argThat { alert ->
            alert.requiresImmedateAction && alert.riskLevel == AbuseRiskLevel.CRITICAL
        })
        verify(emergencyService).notifyElderRightsAdvocate(
            eq(testUserId), any(), any(), eq("IMMEDIATE")
        )
    }

    // ==================== HELPER METHODS ====================

    private fun createMultipleBlockedContactAttempts(
        count: Int, 
        action: ContactAction
    ): List<ContactModificationAttempt> {
        return (1..count).map { index ->
            createContactAttempt(
                attemptId = "attempt-$index",
                action = action,
                result = ContactActionResult.BLOCKED_BY_PROTECTION
            )
        }
    }

    private fun createContactAttempt(
        attemptId: String = "test-attempt",
        action: ContactAction = ContactAction.REMOVE_CONTACT,
        result: ContactActionResult = ContactActionResult.BLOCKED_BY_PROTECTION,
        contactInfo: ContactInfo? = null,
        timestamp: Long = currentTime
    ): ContactModificationAttempt {
        return ContactModificationAttempt(
            attemptId = attemptId,
            caregiverId = testCaregiverId,
            userId = testUserId,
            action = action,
            result = result,
            contactInfo = contactInfo ?: ContactInfo("Test Contact", "+123", "friend"),
            timestamp = timestamp
        )
    }

    private fun createBurstActivityAttempts(count: Int): List<ContactModificationAttempt> {
        val recentTime = currentTime - TimeUnit.MINUTES.toMillis(30) // 30 minutes ago
        return (1..count).map { index ->
            createContactAttempt(
                attemptId = "burst-$index",
                timestamp = recentTime + (index * 1000) // 1 second apart
            )
        }
    }

    private fun createPermissionEscalationAttempts(count: Int): List<PermissionHistoryEntry> {
        return (1..count).map { index ->
            createPermissionHistoryEntry(
                permission = "elevated_access_$index",
                result = "DENIED"
            )
        }
    }

    private fun createPermissionHistoryEntry(
        permission: String,
        result: String
    ): PermissionHistoryEntry {
        return PermissionHistoryEntry(
            actionType = PermissionActionType.REQUEST_PERMISSION,
            permissionChanged = permission,
            oldValue = "false",
            newValue = "true",
            result = result
        )
    }

    private fun createNightTimeAttempts(count: Int): List<ContactModificationAttempt> {
        return (1..count).map { index ->
            // Create timestamps for 2 AM (night time)
            val nightTime = currentTime - TimeUnit.HOURS.toMillis(22) + (index * TimeUnit.MINUTES.toMillis(10))
            createContactAttempt(
                attemptId = "night-$index",
                timestamp = nightTime
            )
        }
    }

    private fun createWeekendAttempts(count: Int): List<ContactModificationAttempt> {
        return (1..count).map { index ->
            // Create Saturday timestamps
            val saturdayTime = currentTime - TimeUnit.DAYS.toMillis(1) + (index * TimeUnit.HOURS.toMillis(1))
            createContactAttempt(
                attemptId = "weekend-$index",
                timestamp = saturdayTime
            )
        }
    }

    private fun createWeekdayAttempts(count: Int): List<ContactModificationAttempt> {
        return (1..count).map { index ->
            // Create Tuesday timestamps
            val tuesdayTime = currentTime - TimeUnit.DAYS.toMillis(5) + (index * TimeUnit.HOURS.toMillis(1))
            createContactAttempt(
                attemptId = "weekday-$index",
                timestamp = tuesdayTime
            )
        }
    }

    private fun createRiskAssessment(
        riskScore: Int,
        timestamp: Long
    ): AbuseRiskAssessment {
        return AbuseRiskAssessment(
            caregiverId = testCaregiverId,
            userId = testUserId,
            riskScore = riskScore,
            riskLevel = when {
                riskScore >= 100 -> AbuseRiskLevel.CRITICAL
                riskScore >= 80 -> AbuseRiskLevel.HIGH
                riskScore >= 50 -> AbuseRiskLevel.MEDIUM
                riskScore >= 25 -> AbuseRiskLevel.LOW
                else -> AbuseRiskLevel.MINIMAL
            },
            riskFactors = emptyList(),
            assessmentTimestamp = timestamp,
            behaviorData = createBehaviorData()
        )
    }

    private fun createCriticalRiskBehavior(): CriticalRiskBehavior {
        return CriticalRiskBehavior(
            contactAttempts = createMultipleBlockedContactAttempts(6, ContactAction.REMOVE_CONTACT) +
                            listOf(createContactAttempt(
                                action = ContactAction.REMOVE_CONTACT,
                                result = ContactActionResult.BLOCKED_BY_PROTECTION,
                                contactInfo = ContactInfo("Emergency", "+911", "emergency")
                            )),
            permissionHistory = createPermissionEscalationAttempts(3) +
                              listOf(createPermissionHistoryEntry("disable_panic_mode", "DENIED")),
            emergencyInteractions = listOf(
                EmergencyInteraction("DISABLE_EMERGENCY_BUTTON", "BLOCKED"),
                EmergencyInteraction("MODIFY_EMERGENCY_CONTACTS", "BLOCKED")
            )
        )
    }

    private fun createBehaviorData(
        contactAttempts: List<ContactModificationAttempt> = emptyList(),
        permissionHistory: List<PermissionHistoryEntry> = emptyList(),
        emergencyInteractions: List<EmergencyInteraction> = emptyList(),
        previousAssessments: List<AbuseRiskAssessment> = emptyList()
    ): CaregiverBehaviorData {
        return CaregiverBehaviorData(
            caregiverId = testCaregiverId,
            userId = testUserId,
            analysisTimeWindow = TimeUnit.DAYS.toMillis(7),
            contactModificationAttempts = contactAttempts,
            permissionHistory = permissionHistory,
            emergencyInteractions = emergencyInteractions,
            previousAssessments = previousAssessments
        )
    }

    private fun setupMockBehaviorData(
        contactAttempts: List<ContactModificationAttempt> = emptyList(),
        permissionHistory: List<PermissionHistoryEntry> = emptyList(),
        emergencyInteractions: List<EmergencyInteraction> = emptyList(),
        previousAssessments: List<AbuseRiskAssessment> = emptyList()
    ) {
        `when`(contactDao.getRecentContactModificationAttempts(any(), any(), any()))
            .thenReturn(contactAttempts)
        `when`(caregiverPermissionManager.getPermissionHistory(any(), any()))
            .thenReturn(permissionHistory)
        `when`(emergencyService.getCaregiverEmergencyInteractions(any(), any(), any()))
            .thenReturn(emergencyInteractions)
        `when`(abuseDao.getRecentRiskAssessments(any(), any(), any()))
            .thenReturn(previousAssessments)
    }

    private data class CriticalRiskBehavior(
        val contactAttempts: List<ContactModificationAttempt>,
        val permissionHistory: List<PermissionHistoryEntry>,
        val emergencyInteractions: List<EmergencyInteraction>
    )
}
