package com.naviya.launcher.abuse

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naviya.launcher.abuse.data.*
import com.naviya.launcher.caregiver.CaregiverPermissionManager
import com.naviya.launcher.caregiver.data.CaregiverDao
import com.naviya.launcher.contacts.ContactProtectionManager
import com.naviya.launcher.contacts.data.ContactDao
import com.naviya.launcher.database.NaviyaDatabase
import com.naviya.launcher.emergency.EmergencyService
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.notifications.NotificationService
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.concurrent.TimeUnit

/**
 * Complete workflow integration tests for abuse detection system
 * Tests end-to-end scenarios from abuse detection to elder rights notification
 */
@RunWith(AndroidJUnit4::class)
class CompleteAbuseWorkflowIntegrationTest {

    private lateinit var database: NaviyaDatabase
    private lateinit var context: Context
    
    // Real components for integration testing
    private lateinit var abuseDetector: RuleBasedAbuseDetector
    private lateinit var contactProtectionManager: ContactProtectionManager
    private lateinit var caregiverPermissionManager: CaregiverPermissionManager
    private lateinit var emergencyService: EmergencyService
    
    // DAOs
    private lateinit var abuseDao: AbuseDetectionDao
    private lateinit var contactDao: ContactDao
    private lateinit var emergencyDao: EmergencyDao
    private lateinit var caregiverDao: CaregiverDao
    
    // Mock external services
    @Mock
    private lateinit var notificationService: NotificationService

    private val testUserId = "workflow-user-123"
    private val testCaregiverId = "workflow-caregiver-456"
    private val elderRightsAdvocateId = "elder-rights-advocate-789"
    private val emergencyContactId = "emergency-contact-911"
    private val currentTime = System.currentTimeMillis()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        
        database = Room.inMemoryDatabaseBuilder(context, NaviyaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        // Initialize DAOs
        abuseDao = database.abuseDetectionDao()
        contactDao = database.contactDao()
        emergencyDao = database.emergencyDao()
        caregiverDao = database.caregiverDao()
        
        // Initialize services
        contactProtectionManager = ContactProtectionManager(context, contactDao)
        caregiverPermissionManager = CaregiverPermissionManager(context, caregiverDao)
        emergencyService = EmergencyService(context, emergencyDao, contactDao)
        
        abuseDetector = RuleBasedAbuseDetector(
            context = context,
            contactDao = contactDao,
            caregiverPermissionManager = caregiverPermissionManager,
            emergencyService = emergencyService,
            abuseDao = abuseDao
        )
        
        // Mock notification service
        notificationService = mock(NotificationService::class.java)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== COMPLETE WORKFLOW TESTS ====================

    @Test
    fun `test complete social isolation abuse workflow`() = runTest {
        // PHASE 1: Setup - Elderly user with family contacts and caregiver
        setupElderlyUserWithFamilyContacts()
        setupCaregiverWithLimitedPermissions()
        
        // PHASE 2: Abuse Pattern - Caregiver attempts social isolation
        val isolationAttempts = simulateSocialIsolationAttempts()
        
        // Verify contact protection blocks attempts
        isolationAttempts.forEach { result ->
            assertFalse("Contact removal should be blocked", result.success)
            assertEquals("Should be blocked by protection", "BLOCKED_BY_PROTECTION", result.reason)
        }
        
        // PHASE 3: Detection - Abuse detection identifies pattern
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        assertTrue("Should detect abuse pattern", assessment.riskScore >= 60) // 4 attempts * 15 = 60
        assertTrue("Should be medium+ risk", assessment.riskLevel >= AbuseRiskLevel.MEDIUM)
        
        val socialIsolationFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.CONTACT_MANIPULATION 
        }
        assertNotNull("Should detect social isolation", socialIsolationFactor)
        assertEquals("Should identify correct pattern", "social_isolation_attempt", 
                    socialIsolationFactor?.evidence?.get("pattern"))
        
        // PHASE 4: Alert Generation - System generates appropriate alert
        val activeAlerts = abuseDao.getActiveAlerts(testUserId)
        val isolationAlert = activeAlerts.find { 
            it.alertType == AbuseAlertType.PATTERN_DETECTED &&
            it.riskLevel >= AbuseRiskLevel.MEDIUM
        }
        
        assertNotNull("Should generate isolation alert", isolationAlert)
        assertTrue("Should contain social isolation in message", 
                  isolationAlert?.message?.contains("social isolation", ignoreCase = true) == true)
        
        // PHASE 5: Response - Recommended actions include elder rights contact
        val recommendedActions = isolationAlert?.recommendedActions ?: emptyList()
        assertTrue("Should recommend elder rights advocate contact", 
                  recommendedActions.any { it.contains("elder rights", ignoreCase = true) })
        assertTrue("Should recommend monitoring increase", 
                  recommendedActions.any { it.contains("monitor", ignoreCase = true) })
        
        // PHASE 6: Notification - Elder rights advocate is notified
        verify(notificationService, atLeastOnce()).notifyElderRightsAdvocate(
            eq(testUserId), 
            any(), 
            argThat { message -> message.contains("social isolation", ignoreCase = true) },
            eq("MEDIUM_PRIORITY")
        )
        
        // PHASE 7: Audit Trail - All actions are logged immutably
        val auditEntries = contactDao.getContactModificationAttempts(
            testCaregiverId, testUserId, TimeUnit.HOURS.toMillis(1)
        )
        assertEquals("Should log all attempts", 4, auditEntries.size)
        assertTrue("All should be blocked", auditEntries.all { 
            it.result == ContactActionResult.BLOCKED_BY_PROTECTION 
        })
    }

    @Test
    fun `test critical emergency contact tampering workflow`() = runTest {
        // PHASE 1: Setup - User with emergency contacts
        setupElderlyUserWithEmergencyContacts()
        setupCaregiverWithLimitedPermissions()
        
        // PHASE 2: Critical Abuse - Caregiver attempts emergency contact tampering
        val emergencyTamperResult = contactProtectionManager.attemptContactRemoval(
            caregiverId = testCaregiverId,
            userId = testUserId,
            contactId = emergencyContactId,
            userApproval = false
        )
        
        val advocateTamperResult = contactProtectionManager.attemptContactRemoval(
            caregiverId = testCaregiverId,
            userId = testUserId,
            contactId = elderRightsAdvocateId,
            userApproval = false
        )
        
        // Verify critical protection
        assertFalse("Emergency contact removal must be blocked", emergencyTamperResult.success)
        assertFalse("Elder advocate removal must be blocked", advocateTamperResult.success)
        
        // PHASE 3: Immediate Detection - Critical risk identified
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        assertEquals("Should be critical risk", AbuseRiskLevel.CRITICAL, assessment.riskLevel)
        assertTrue("Should have high risk score", assessment.riskScore >= 100)
        
        val tamperFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.EMERGENCY_CONTACT_TAMPERING 
        }
        assertNotNull("Should detect emergency tampering", tamperFactor)
        assertEquals("Should be critical severity", AbuseSeverity.CRITICAL, tamperFactor?.severity)
        
        // PHASE 4: Immediate Alert - Critical alert generated
        val criticalAlerts = abuseDao.getAlertsRequiringImmediateAction(testUserId)
        val emergencyTamperAlert = criticalAlerts.find { 
            it.riskLevel == AbuseRiskLevel.CRITICAL 
        }
        
        assertNotNull("Should generate critical alert", emergencyTamperAlert)
        assertTrue("Should require immediate action", emergencyTamperAlert?.requiresImmedateAction == true)
        assertTrue("Should mention emergency contact tampering", 
                  emergencyTamperAlert?.message?.contains("emergency contact", ignoreCase = true) == true)
        
        // PHASE 5: Emergency Response - Immediate elder rights notification
        verify(notificationService, atLeastOnce()).notifyElderRightsAdvocate(
            eq(testUserId), 
            any(), 
            argThat { message -> message.contains("emergency contact tampering", ignoreCase = true) },
            eq("IMMEDIATE")
        )
        
        // PHASE 6: Safety Measures - Caregiver permissions restricted
        val restrictedPermissions = caregiverPermissionManager.getCaregiverPermissions(testCaregiverId, testUserId)
        assertFalse("Should restrict contact access", restrictedPermissions.canModifyContacts)
        assertFalse("Should restrict emergency access", restrictedPermissions.canAccessEmergencySystem)
        
        // PHASE 7: Legal Evidence - Immutable audit trail preserved
        val criticalAuditEntries = contactDao.getContactModificationAttempts(
            testCaregiverId, testUserId, TimeUnit.HOURS.toMillis(1)
        )
        assertTrue("Should preserve evidence", criticalAuditEntries.isNotEmpty())
        assertTrue("Should mark as emergency contact tampering", 
                  criticalAuditEntries.any { it.contactInfo?.contactType == "emergency" })
    }

    @Test
    fun `test panic mode activation with suspicious caregiver behavior`() = runTest {
        // PHASE 1: Setup - User with some prior suspicious activity
        setupElderlyUserWithFamilyContacts()
        setupCaregiverWithSuspiciousHistory()
        
        // PHASE 2: User Distress - Panic mode activated
        val panicTrigger = AbuseDetectionTrigger(
            eventType = TriggerEventType.PANIC_MODE_ACTIVATION,
            eventData = mapOf(
                "panic_reason" to "user_distress",
                "activation_method" to "triple_tap",
                "user_location" to "home",
                "caregiver_present" to "true"
            ),
            severity = TriggerSeverity.HIGH
        )
        
        // PHASE 3: Immediate Analysis - Caregiver behavior analyzed in panic context
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId, panicTrigger)
        
        assertTrue("Should have elevated risk due to panic context", assessment.riskScore > 50)
        assertTrue("Should be high risk with panic trigger", assessment.riskLevel >= AbuseRiskLevel.HIGH)
        
        val triggerFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.TRIGGER_EVENT 
        }
        assertNotNull("Should analyze panic trigger", triggerFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, triggerFactor?.severity)
        assertTrue("Should reference caregiver presence", 
                  triggerFactor?.evidence?.get("caregiver_present") == "true")
        
        // PHASE 4: Emergency Alert - High priority alert generated
        val panicAlerts = abuseDao.getActiveAlerts(testUserId)
        val panicContextAlert = panicAlerts.find { 
            it.alertType == AbuseAlertType.TRIGGER_EVENT &&
            it.riskLevel >= AbuseRiskLevel.HIGH
        }
        
        assertNotNull("Should generate panic context alert", panicContextAlert)
        assertTrue("Should mention panic activation", 
                  panicContextAlert?.message?.contains("panic mode", ignoreCase = true) == true)
        
        // PHASE 5: Emergency Response - Multiple notification channels
        verify(notificationService, atLeastOnce()).notifyElderRightsAdvocate(
            eq(testUserId), 
            any(), 
            argThat { message -> message.contains("panic mode", ignoreCase = true) },
            eq("IMMEDIATE")
        )
        
        verify(notificationService, atLeastOnce()).notifyEmergencyContacts(
            eq(testUserId), 
            argThat { message -> message.contains("panic mode activated", ignoreCase = true) }
        )
        
        // PHASE 6: Safety Protocol - Caregiver monitoring increased
        val monitoringLevel = caregiverPermissionManager.getCaregiverMonitoringLevel(testCaregiverId, testUserId)
        assertEquals("Should increase monitoring", "HIGH", monitoringLevel)
        
        // PHASE 7: Documentation - Panic event documented with caregiver context
        val panicEvent = emergencyDao.getEmergencyEvent(testUserId, "PANIC_MODE_ACTIVATION")
        assertNotNull("Should document panic event", panicEvent)
        assertTrue("Should include caregiver context", 
                  panicEvent?.context?.contains("caregiver_present") == true)
    }

    @Test
    fun `test escalating abuse pattern over multiple days with intervention`() = runTest {
        // PHASE 1: Setup
        setupElderlyUserWithFamilyContacts()
        setupCaregiverWithLimitedPermissions()
        
        // PHASE 2: Day 1 - Minor violations
        simulateDay1MinorViolations()
        val day1Assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        abuseDao.insertRiskAssessment(day1Assessment)
        
        assertTrue("Day 1 should be low risk", day1Assessment.riskLevel <= AbuseRiskLevel.LOW)
        
        // PHASE 3: Day 2 - Moderate violations
        simulateDay2ModerateViolations()
        val day2Assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        abuseDao.insertRiskAssessment(day2Assessment)
        
        assertTrue("Day 2 should be medium risk", day2Assessment.riskLevel >= AbuseRiskLevel.MEDIUM)
        
        // First intervention - Elder rights advocate contacted
        verify(notificationService, atLeastOnce()).notifyElderRightsAdvocate(
            eq(testUserId), any(), any(), eq("MEDIUM_PRIORITY")
        )
        
        // PHASE 4: Day 3 - Severe violations despite intervention
        simulateDay3SevereViolations()
        val day3Assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        assertTrue("Day 3 should be high/critical risk", day3Assessment.riskLevel >= AbuseRiskLevel.HIGH)
        
        // Should detect escalating pattern
        val escalatingFactor = day3Assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.ESCALATING_BEHAVIOR 
        }
        assertNotNull("Should detect escalating behavior", escalatingFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, escalatingFactor?.severity)
        
        // PHASE 5: Escalation Response - More aggressive intervention
        val escalationAlerts = abuseDao.getActiveAlerts(testUserId)
        val escalationAlert = escalationAlerts.find { 
            it.alertType == AbuseAlertType.ESCALATION_DETECTED 
        }
        
        assertNotNull("Should generate escalation alert", escalationAlert)
        assertTrue("Should require immediate action", escalationAlert?.requiresImmedateAction == true)
        
        // Should trigger emergency intervention
        verify(notificationService, atLeastOnce()).notifyElderRightsAdvocate(
            eq(testUserId), 
            any(), 
            argThat { message -> message.contains("escalating", ignoreCase = true) },
            eq("IMMEDIATE")
        )
        
        // PHASE 6: Protective Measures - Caregiver access restricted
        val finalPermissions = caregiverPermissionManager.getCaregiverPermissions(testCaregiverId, testUserId)
        assertFalse("Should revoke contact access", finalPermissions.canModifyContacts)
        assertFalse("Should revoke location access", finalPermissions.canViewLocation)
        
        // PHASE 7: Legal Documentation - Complete audit trail preserved
        val completeAuditTrail = contactDao.getContactModificationAttempts(
            testCaregiverId, testUserId, TimeUnit.DAYS.toMillis(3)
        )
        assertTrue("Should have complete audit trail", completeAuditTrail.size >= 10) // 1+3+6 attempts
        
        // Should show escalation pattern in timestamps
        val sortedAttempts = completeAuditTrail.sortedBy { it.timestamp }
        assertTrue("Should show increasing frequency", 
                  sortedAttempts.takeLast(6).size > sortedAttempts.take(4).size)
    }

    @Test
    fun `test false positive prevention and resolution workflow`() = runTest {
        // PHASE 1: Setup - Legitimate caregiver activity
        setupElderlyUserWithFamilyContacts()
        setupLegitimateCaregiver()
        
        // PHASE 2: Legitimate Activity - Caregiver makes reasonable requests
        val legitimateRequests = simulateLegitimateActivity()
        
        // Some requests approved by user
        legitimateRequests.take(2).forEach { result ->
            assertTrue("Legitimate requests should succeed", result.success)
        }
        
        // PHASE 3: Analysis - Should not trigger false alarms
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        assertTrue("Should be low risk for legitimate activity", assessment.riskLevel <= AbuseRiskLevel.LOW)
        assertTrue("Should have low risk score", assessment.riskScore < 25)
        
        // PHASE 4: No False Alerts - System should not generate inappropriate alerts
        val alerts = abuseDao.getActiveAlerts(testUserId)
        val falseAlerts = alerts.filter { it.riskLevel >= AbuseRiskLevel.MEDIUM }
        
        assertTrue("Should not generate false positive alerts", falseAlerts.isEmpty())
        
        // PHASE 5: Positive Reinforcement - Good caregiver behavior noted
        val caregiverStats = caregiverDao.getCaregiverStatistics(testCaregiverId, testUserId)
        assertTrue("Should track positive interactions", caregiverStats.approvedRequests > 0)
        assertTrue("Should have low violation rate", caregiverStats.violationRate < 0.1)
        
        // PHASE 6: System Learning - Detection rules should adapt
        val rulePerformance = abuseDao.getDetectionRulePerformance()
        assertTrue("Should track rule accuracy", rulePerformance.isNotEmpty())
        assertTrue("Should maintain high precision", 
                  rulePerformance.all { it.falsePositiveRate < 0.05 })
    }

    // ==================== HELPER METHODS ====================

    private suspend fun setupElderlyUserWithFamilyContacts() {
        // Family contacts
        val familyContacts = listOf(
            ProtectedContact(
                contactId = "daughter-contact",
                userId = testUserId,
                name = "Sarah (Daughter)",
                phoneNumber = "+1-555-0101",
                contactType = ContactType.FAMILY,
                protectionLevel = ContactProtectionLevel.STANDARD,
                canBeRemovedByCaregiver = false
            ),
            ProtectedContact(
                contactId = "son-contact",
                userId = testUserId,
                name = "Michael (Son)",
                phoneNumber = "+1-555-0102",
                contactType = ContactType.FAMILY,
                protectionLevel = ContactProtectionLevel.STANDARD,
                canBeRemovedByCaregiver = false
            ),
            ProtectedContact(
                contactId = "friend-contact",
                userId = testUserId,
                name = "Betty (Friend)",
                phoneNumber = "+1-555-0103",
                contactType = ContactType.FRIEND,
                protectionLevel = ContactProtectionLevel.STANDARD,
                canBeRemovedByCaregiver = false
            )
        )
        
        familyContacts.forEach { contactDao.insertProtectedContact(it) }
        
        // Elder rights advocate
        contactDao.insertProtectedContact(
            ProtectedContact(
                contactId = elderRightsAdvocateId,
                userId = testUserId,
                name = "Elder Rights Advocate",
                phoneNumber = "+1-800-ELDER-RIGHTS",
                contactType = ContactType.ELDER_RIGHTS_ADVOCATE,
                protectionLevel = ContactProtectionLevel.MAXIMUM,
                canBeRemovedByCaregiver = false,
                canBeBlockedByCaregiver = false
            )
        )
    }

    private suspend fun setupElderlyUserWithEmergencyContacts() {
        setupElderlyUserWithFamilyContacts()
        
        // Emergency contact
        contactDao.insertProtectedContact(
            ProtectedContact(
                contactId = emergencyContactId,
                userId = testUserId,
                name = "Emergency Services",
                phoneNumber = "911",
                contactType = ContactType.EMERGENCY,
                protectionLevel = ContactProtectionLevel.MAXIMUM,
                canBeRemovedByCaregiver = false,
                canBeBlockedByCaregiver = false
            )
        )
    }

    private suspend fun setupCaregiverWithLimitedPermissions() {
        caregiverDao.insertCaregiverProfile(
            CaregiverProfile(
                caregiverId = testCaregiverId,
                userId = testUserId,
                name = "Test Caregiver",
                permissions = CaregiverPermissions(
                    canViewLocation = false,
                    canModifyContacts = false,
                    canAccessEmergencySystem = false,
                    canModifySettings = false,
                    canViewAppUsage = true // Only basic monitoring allowed
                )
            )
        )
    }

    private suspend fun setupCaregiverWithSuspiciousHistory() {
        setupCaregiverWithLimitedPermissions()
        
        // Add some prior suspicious activity
        repeat(2) { index ->
            contactDao.insertContactModificationAttempt(
                ContactModificationAttempt(
                    attemptId = "prior-suspicious-$index",
                    caregiverId = testCaregiverId,
                    userId = testUserId,
                    action = ContactAction.REMOVE_CONTACT,
                    result = ContactActionResult.BLOCKED_BY_PROTECTION,
                    contactInfo = ContactInfo("Prior Contact $index", "+123", "friend"),
                    timestamp = currentTime - TimeUnit.DAYS.toMillis(7) + TimeUnit.HOURS.toMillis(index.toLong())
                )
            )
        }
    }

    private suspend fun setupLegitimateCaregiver() {
        caregiverDao.insertCaregiverProfile(
            CaregiverProfile(
                caregiverId = testCaregiverId,
                userId = testUserId,
                name = "Legitimate Caregiver",
                permissions = CaregiverPermissions(
                    canViewLocation = true, // Approved by user
                    canModifyContacts = false,
                    canAccessEmergencySystem = false,
                    canModifySettings = false,
                    canViewAppUsage = true
                ),
                trustLevel = CaregiverTrustLevel.HIGH,
                verificationStatus = CaregiverVerificationStatus.VERIFIED
            )
        )
    }

    private suspend fun simulateSocialIsolationAttempts(): List<ContactModificationResult> {
        val results = mutableListOf<ContactModificationResult>()
        
        // Attempt to remove family contacts
        val contactsToRemove = listOf("daughter-contact", "son-contact", "friend-contact", "neighbor-contact")
        
        contactsToRemove.forEach { contactId ->
            val result = contactProtectionManager.attemptContactRemoval(
                caregiverId = testCaregiverId,
                userId = testUserId,
                contactId = contactId,
                userApproval = false
            )
            results.add(result)
        }
        
        return results
    }

    private suspend fun simulateDay1MinorViolations() {
        contactDao.insertContactModificationAttempt(
            ContactModificationAttempt(
                attemptId = "day1-minor",
                caregiverId = testCaregiverId,
                userId = testUserId,
                action = ContactAction.REMOVE_CONTACT,
                result = ContactActionResult.BLOCKED_BY_PROTECTION,
                contactInfo = ContactInfo("Day 1 Contact", "+123", "friend"),
                timestamp = currentTime - TimeUnit.DAYS.toMillis(2)
            )
        )
    }

    private suspend fun simulateDay2ModerateViolations() {
        repeat(3) { index ->
            contactDao.insertContactModificationAttempt(
                ContactModificationAttempt(
                    attemptId = "day2-moderate-$index",
                    caregiverId = testCaregiverId,
                    userId = testUserId,
                    action = ContactAction.REMOVE_CONTACT,
                    result = ContactActionResult.BLOCKED_BY_PROTECTION,
                    contactInfo = ContactInfo("Day 2 Contact $index", "+123", "friend"),
                    timestamp = currentTime - TimeUnit.DAYS.toMillis(1) - TimeUnit.HOURS.toMillis(index.toLong())
                )
            )
        }
    }

    private suspend fun simulateDay3SevereViolations() {
        repeat(6) { index ->
            contactDao.insertContactModificationAttempt(
                ContactModificationAttempt(
                    attemptId = "day3-severe-$index",
                    caregiverId = testCaregiverId,
                    userId = testUserId,
                    action = ContactAction.REMOVE_CONTACT,
                    result = ContactActionResult.BLOCKED_BY_PROTECTION,
                    contactInfo = ContactInfo("Day 3 Contact $index", "+123", "friend"),
                    timestamp = currentTime - TimeUnit.HOURS.toMillis(index.toLong())
                )
            )
        }
        
        // Add emergency system tampering
        emergencyDao.insertEmergencyInteraction(
            EmergencyInteraction(
                interactionId = "day3-emergency-tampering",
                caregiverId = testCaregiverId,
                userId = testUserId,
                actionType = "DISABLE_EMERGENCY_BUTTON",
                actionResult = "BLOCKED",
                timestamp = currentTime - TimeUnit.MINUTES.toMillis(30)
            )
        )
    }

    private suspend fun simulateLegitimateActivity(): List<ContactModificationResult> {
        val results = mutableListOf<ContactModificationResult>()
        
        // Legitimate requests with user approval
        repeat(2) { index ->
            val result = contactProtectionManager.attemptContactAddition(
                caregiverId = testCaregiverId,
                userId = testUserId,
                contactInfo = ContactInfo("Healthcare Provider $index", "+1-555-HEALTH", "healthcare"),
                userApproval = true
            )
            results.add(result)
        }
        
        return results
    }
}
