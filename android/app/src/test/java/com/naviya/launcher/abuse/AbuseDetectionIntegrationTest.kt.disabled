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
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

/**
 * Integration tests for abuse detection system with other security components
 * Tests complete workflows from detection to response
 */
@RunWith(AndroidJUnit4::class)
class AbuseDetectionIntegrationTest {

    private lateinit var database: NaviyaDatabase
    private lateinit var context: Context
    
    // Real DAOs for integration testing
    private lateinit var abuseDao: AbuseDetectionDao
    private lateinit var contactDao: ContactDao
    private lateinit var emergencyDao: EmergencyDao
    private lateinit var caregiverDao: CaregiverDao
    
    // Real managers for integration testing
    private lateinit var abuseDetector: RuleBasedAbuseDetector
    private lateinit var contactProtectionManager: ContactProtectionManager
    private lateinit var caregiverPermissionManager: CaregiverPermissionManager
    private lateinit var emergencyService: EmergencyService

    private val testUserId = "integration-user-123"
    private val testCaregiverId = "integration-caregiver-456"
    private val elderRightsAdvocateId = "elder-rights-advocate-789"
    private val currentTime = System.currentTimeMillis()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(context, NaviyaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        // Initialize DAOs
        abuseDao = database.abuseDetectionDao()
        contactDao = database.contactDao()
        emergencyDao = database.emergencyDao()
        caregiverDao = database.caregiverDao()
        
        // Initialize real managers for integration testing
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
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== CONTACT PROTECTION INTEGRATION TESTS ====================

    @Test
    fun `test abuse detection triggered by contact protection violations`() = runTest {
        // Given: Setup user with protected contacts
        setupUserWithProtectedContacts()
        
        // When: Caregiver attempts to remove multiple protected contacts
        repeat(4) { index ->
            val result = contactProtectionManager.attemptContactRemoval(
                caregiverId = testCaregiverId,
                userId = testUserId,
                contactId = "protected-contact-$index",
                userApproval = false
            )
            
            // Should be blocked by protection
            assertFalse("Contact removal should be blocked", result.success)
            assertEquals("Should be blocked by protection", 
                        "BLOCKED_BY_PROTECTION", result.reason)
        }
        
        // Then: Abuse detection should identify social isolation pattern
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        assertTrue("Should detect abuse pattern", assessment.riskScore > 0)
        assertTrue("Should be at least medium risk", assessment.riskLevel >= AbuseRiskLevel.MEDIUM)
        
        val contactManipulationFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.CONTACT_MANIPULATION 
        }
        assertNotNull("Should detect contact manipulation", contactManipulationFactor)
        assertTrue("Should identify social isolation pattern", 
                  contactManipulationFactor?.evidence?.get("pattern") == "social_isolation_attempt")
    }

    @Test
    fun `test emergency contact tampering triggers critical alert`() = runTest {
        // Given: Setup user with emergency contacts
        setupUserWithEmergencyContacts()
        
        // When: Caregiver attempts to tamper with emergency contacts
        val emergencyContactResult = contactProtectionManager.attemptContactRemoval(
            caregiverId = testCaregiverId,
            userId = testUserId,
            contactId = "emergency-contact-911",
            userApproval = false
        )
        
        val elderAdvocateResult = contactProtectionManager.attemptContactRemoval(
            caregiverId = testCaregiverId,
            userId = testUserId,
            contactId = elderRightsAdvocateId,
            userApproval = false
        )
        
        // Should be blocked
        assertFalse("Emergency contact removal should be blocked", emergencyContactResult.success)
        assertFalse("Elder advocate removal should be blocked", elderAdvocateResult.success)
        
        // Then: Should trigger critical abuse alert
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        assertEquals("Should be critical risk", AbuseRiskLevel.CRITICAL, assessment.riskLevel)
        
        val tamperFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.EMERGENCY_CONTACT_TAMPERING 
        }
        assertNotNull("Should detect emergency contact tampering", tamperFactor)
        assertEquals("Should be critical severity", AbuseSeverity.CRITICAL, tamperFactor?.severity)
        
        // Should generate immediate alert
        val activeAlerts = abuseDao.getActiveAlerts(testUserId)
        assertTrue("Should generate active alert", activeAlerts.isNotEmpty())
        assertTrue("Should require immediate action", 
                  activeAlerts.any { it.requiresImmedateAction })
    }

    @Test
    fun `test contact protection audit trail integration`() = runTest {
        // Given: Setup user and contacts
        setupUserWithProtectedContacts()
        
        // When: Multiple contact manipulation attempts
        repeat(3) { index ->
            contactProtectionManager.attemptContactRemoval(
                caregiverId = testCaregiverId,
                userId = testUserId,
                contactId = "contact-$index",
                userApproval = false
            )
        }
        
        // Then: Audit trail should be captured and used in abuse detection
        val auditEntries = contactDao.getContactModificationAttempts(
            testCaregiverId, 
            testUserId,
            TimeUnit.DAYS.toMillis(1)
        )
        
        assertEquals("Should record all attempts", 3, auditEntries.size)
        assertTrue("All attempts should be blocked", 
                  auditEntries.all { it.result.name.contains("BLOCKED") })
        
        // Abuse detection should use this audit data
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        assertTrue("Should detect pattern from audit trail", assessment.riskScore > 0)
    }

    // ==================== EMERGENCY SYSTEM INTEGRATION TESTS ====================

    @Test
    fun `test panic mode activation triggers abuse analysis`() = runTest {
        // Given: Setup user and caregiver with some suspicious activity
        setupUserWithSuspiciousActivity()
        
        // When: User activates panic mode
        val panicTrigger = AbuseDetectionTrigger(
            eventType = TriggerEventType.PANIC_MODE_ACTIVATION,
            eventData = mapOf(
                "panic_reason" to "user_distress",
                "activation_method" to "triple_tap"
            ),
            severity = TriggerSeverity.HIGH
        )
        
        // Then: Should trigger immediate abuse analysis
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId, panicTrigger)
        
        assertTrue("Should analyze caregiver behavior", assessment.riskScore > 0)
        
        val triggerFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.TRIGGER_EVENT 
        }
        assertNotNull("Should analyze trigger event", triggerFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, triggerFactor?.severity)
        assertTrue("Should reference panic context", 
                  triggerFactor?.evidence?.containsKey("panic_context") == true)
    }

    @Test
    fun `test emergency system tampering detection`() = runTest {
        // Given: Setup emergency system
        setupEmergencySystem()
        
        // When: Caregiver attempts to disable emergency features
        val disableAttempt1 = emergencyService.attemptEmergencySystemModification(
            caregiverId = testCaregiverId,
            userId = testUserId,
            action = "DISABLE_EMERGENCY_BUTTON",
            userApproval = false
        )
        
        val disableAttempt2 = emergencyService.attemptEmergencySystemModification(
            caregiverId = testCaregiverId,
            userId = testUserId,
            action = "MODIFY_EMERGENCY_CONTACTS",
            userApproval = false
        )
        
        // Should be blocked
        assertFalse("Emergency system modification should be blocked", disableAttempt1.success)
        assertFalse("Emergency contact modification should be blocked", disableAttempt2.success)
        
        // Then: Should detect safety system tampering
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        val tamperFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.SAFETY_SYSTEM_TAMPERING 
        }
        assertNotNull("Should detect safety system tampering", tamperFactor)
        assertEquals("Should be critical severity", AbuseSeverity.CRITICAL, tamperFactor?.severity)
        assertTrue("Should identify safety compromise", 
                  tamperFactor?.evidence?.get("pattern") == "safety_compromise")
    }

    @Test
    fun `test elder rights advocate notification integration`() = runTest {
        // Given: Critical abuse situation
        setupCriticalAbuseSituation()
        
        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        // Then: Should be critical risk and trigger elder rights notification
        assertEquals("Should be critical risk", AbuseRiskLevel.CRITICAL, assessment.riskLevel)
        
        // Should generate alert with elder rights notification
        val activeAlerts = abuseDao.getActiveAlerts(testUserId)
        val criticalAlert = activeAlerts.find { it.riskLevel == AbuseRiskLevel.CRITICAL }
        
        assertNotNull("Should generate critical alert", criticalAlert)
        assertTrue("Should require immediate action", criticalAlert?.requiresImmedateAction == true)
        
        // Should contain elder rights advocate in recommended actions
        assertTrue("Should recommend elder rights contact", 
                  criticalAlert?.recommendedActions?.any { 
                      it.contains("elder rights advocate", ignoreCase = true) 
                  } == true)
    }

    // ==================== CAREGIVER PERMISSION INTEGRATION TESTS ====================

    @Test
    fun `test permission escalation triggers abuse detection`() = runTest {
        // Given: Setup caregiver with minimal permissions
        setupCaregiverWithMinimalPermissions()
        
        // When: Caregiver repeatedly requests permission escalation
        repeat(4) { index ->
            val result = caregiverPermissionManager.requestPermissionEscalation(
                caregiverId = testCaregiverId,
                userId = testUserId,
                permission = "sensitive_permission_$index",
                userApproval = false
            )
            
            assertFalse("Permission escalation should be denied", result.granted)
        }
        
        // Then: Should detect permission escalation pattern
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        val escalationFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.PERMISSION_ESCALATION 
        }
        assertNotNull("Should detect permission escalation", escalationFactor)
        assertTrue("Should be medium+ severity", escalationFactor?.severity != AbuseSeverity.MINIMAL)
        assertTrue("Should identify control escalation pattern", 
                  escalationFactor?.evidence?.get("pattern") == "control_escalation")
    }

    @Test
    fun `test sensitive permission requests trigger high risk`() = runTest {
        // Given: Setup caregiver
        setupCaregiverWithMinimalPermissions()
        
        // When: Caregiver requests sensitive permissions
        val sensitivePermissions = listOf(
            "access_location",
            "access_contacts", 
            "disable_panic_mode",
            "modify_emergency_contacts"
        )
        
        sensitivePermissions.forEach { permission ->
            caregiverPermissionManager.requestPermissionEscalation(
                caregiverId = testCaregiverId,
                userId = testUserId,
                permission = permission,
                userApproval = false
            )
        }
        
        // Then: Should detect sensitive permission abuse
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        val sensitivePermissionFactor = assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.SENSITIVE_PERMISSION_REQUEST 
        }
        assertNotNull("Should detect sensitive permission requests", sensitivePermissionFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, sensitivePermissionFactor?.severity)
        assertTrue("Should identify privacy invasion attempt", 
                  sensitivePermissionFactor?.evidence?.get("pattern") == "privacy_invasion_attempt")
    }

    // ==================== ESCALATING BEHAVIOR INTEGRATION TESTS ====================

    @Test
    fun `test escalating abuse pattern over multiple days`() = runTest {
        // Given: Setup user and caregiver
        setupUserWithProtectedContacts()
        
        // Day 1: Minor violations
        simulateDay1Violations()
        val day1Assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        abuseDao.insertRiskAssessment(day1Assessment)
        
        // Day 2: Moderate violations
        simulateDay2Violations()
        val day2Assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        abuseDao.insertRiskAssessment(day2Assessment)
        
        // Day 3: Severe violations
        simulateDay3Violations()
        val day3Assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        // Then: Should detect escalating behavior pattern
        assertTrue("Day 1 should be low risk", day1Assessment.riskLevel <= AbuseRiskLevel.LOW)
        assertTrue("Day 2 should be medium risk", day2Assessment.riskLevel >= AbuseRiskLevel.MEDIUM)
        assertTrue("Day 3 should be high risk", day3Assessment.riskLevel >= AbuseRiskLevel.HIGH)
        
        val escalatingFactor = day3Assessment.riskFactors.find { 
            it.factorType == AbuseRiskFactorType.ESCALATING_BEHAVIOR 
        }
        assertNotNull("Should detect escalating behavior", escalatingFactor)
        assertEquals("Should be high severity", AbuseSeverity.HIGH, escalatingFactor?.severity)
    }

    @Test
    fun `test complete abuse workflow from detection to resolution`() = runTest {
        // Given: Critical abuse situation
        setupCriticalAbuseSituation()
        
        // When: Analyzing caregiver behavior
        val assessment = abuseDetector.analyzeCaregiver(testCaregiverId, testUserId)
        
        // Then: Complete workflow should execute
        assertEquals("Should be critical risk", AbuseRiskLevel.CRITICAL, assessment.riskLevel)
        
        // Should generate alert
        val activeAlerts = abuseDao.getActiveAlerts(testUserId)
        val criticalAlert = activeAlerts.find { it.riskLevel == AbuseRiskLevel.CRITICAL }
        assertNotNull("Should generate critical alert", criticalAlert)
        
        // Should trigger immediate notifications
        assertTrue("Should require immediate action", criticalAlert?.requiresImmedateAction == true)
        
        // Should recommend specific actions
        val recommendedActions = criticalAlert?.recommendedActions ?: emptyList()
        assertTrue("Should recommend elder rights contact", 
                  recommendedActions.any { it.contains("elder rights", ignoreCase = true) })
        assertTrue("Should recommend permission restriction", 
                  recommendedActions.any { it.contains("restrict", ignoreCase = true) })
        
        // When: Alert is resolved
        criticalAlert?.let { alert ->
            abuseDao.updateAlertResolution(
                alert.alertId,
                "Resolved by elder rights advocate intervention",
                currentTime + TimeUnit.HOURS.toMillis(2)
            )
            abuseDao.updateAlertStatus(alert.alertId, AbuseAlertStatus.RESOLVED)
        }
        
        // Then: Alert should be marked as resolved
        val resolvedAlert = criticalAlert?.let { abuseDao.getAbuseAlert(it.alertId) }
        assertEquals("Should be resolved", AbuseAlertStatus.RESOLVED, resolvedAlert?.status)
        assertNotNull("Should have resolution details", resolvedAlert?.resolutionDetails)
    }

    // ==================== HELPER METHODS ====================

    private suspend fun setupUserWithProtectedContacts() {
        // Create protected contacts
        repeat(5) { index ->
            contactDao.insertProtectedContact(
                ProtectedContact(
                    contactId = "protected-contact-$index",
                    userId = testUserId,
                    name = "Protected Contact $index",
                    phoneNumber = "+123456789$index",
                    contactType = ContactType.FAMILY,
                    protectionLevel = ContactProtectionLevel.STANDARD,
                    canBeRemovedByCaregiver = false,
                    canBeBlockedByCaregiver = false
                )
            )
        }
        
        // Add elder rights advocate
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

    private suspend fun setupUserWithEmergencyContacts() {
        // Emergency contacts
        contactDao.insertProtectedContact(
            ProtectedContact(
                contactId = "emergency-contact-911",
                userId = testUserId,
                name = "Emergency Services",
                phoneNumber = "911",
                contactType = ContactType.EMERGENCY,
                protectionLevel = ContactProtectionLevel.MAXIMUM,
                canBeRemovedByCaregiver = false,
                canBeBlockedByCaregiver = false
            )
        )
        
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

    private suspend fun setupUserWithSuspiciousActivity() {
        setupUserWithProtectedContacts()
        
        // Create some suspicious contact attempts
        repeat(2) { index ->
            contactDao.insertContactModificationAttempt(
                ContactModificationAttempt(
                    attemptId = "suspicious-$index",
                    caregiverId = testCaregiverId,
                    userId = testUserId,
                    action = ContactAction.REMOVE_CONTACT,
                    result = ContactActionResult.BLOCKED_BY_PROTECTION,
                    contactInfo = ContactInfo("Suspicious Contact $index", "+123", "friend"),
                    timestamp = currentTime - TimeUnit.HOURS.toMillis(index.toLong())
                )
            )
        }
    }

    private suspend fun setupEmergencySystem() {
        // Setup emergency system configuration
        emergencyDao.insertEmergencyConfiguration(
            EmergencyConfiguration(
                userId = testUserId,
                emergencyButtonEnabled = true,
                panicModeEnabled = true,
                elderRightsAdvocateContact = elderRightsAdvocateId
            )
        )
    }

    private suspend fun setupCriticalAbuseSituation() {
        setupUserWithEmergencyContacts()
        
        // Multiple emergency contact tampering attempts
        repeat(2) { index ->
            contactDao.insertContactModificationAttempt(
                ContactModificationAttempt(
                    attemptId = "critical-emergency-$index",
                    caregiverId = testCaregiverId,
                    userId = testUserId,
                    action = ContactAction.REMOVE_CONTACT,
                    result = ContactActionResult.BLOCKED_BY_PROTECTION,
                    contactInfo = ContactInfo("Emergency Contact", "911", "emergency"),
                    timestamp = currentTime - TimeUnit.MINUTES.toMillis(index.toLong() * 10)
                )
            )
        }
        
        // Emergency system tampering attempts
        repeat(2) { index ->
            emergencyDao.insertEmergencyInteraction(
                EmergencyInteraction(
                    interactionId = "critical-tampering-$index",
                    caregiverId = testCaregiverId,
                    userId = testUserId,
                    actionType = "DISABLE_EMERGENCY_BUTTON",
                    actionResult = "BLOCKED",
                    timestamp = currentTime - TimeUnit.MINUTES.toMillis(index.toLong() * 5)
                )
            )
        }
    }

    private suspend fun setupCaregiverWithMinimalPermissions() {
        caregiverDao.insertCaregiverProfile(
            CaregiverProfile(
                caregiverId = testCaregiverId,
                userId = testUserId,
                name = "Test Caregiver",
                permissions = CaregiverPermissions(
                    canViewLocation = false,
                    canModifyContacts = false,
                    canAccessEmergencySystem = false,
                    canModifySettings = false
                )
            )
        )
    }

    private suspend fun simulateDay1Violations() {
        // Minor violations - 1 contact attempt
        contactDao.insertContactModificationAttempt(
            ContactModificationAttempt(
                attemptId = "day1-violation",
                caregiverId = testCaregiverId,
                userId = testUserId,
                action = ContactAction.REMOVE_CONTACT,
                result = ContactActionResult.BLOCKED_BY_PROTECTION,
                contactInfo = ContactInfo("Day 1 Contact", "+123", "friend"),
                timestamp = currentTime - TimeUnit.DAYS.toMillis(2)
            )
        )
    }

    private suspend fun simulateDay2Violations() {
        // Moderate violations - 3 contact attempts + 1 permission request
        repeat(3) { index ->
            contactDao.insertContactModificationAttempt(
                ContactModificationAttempt(
                    attemptId = "day2-violation-$index",
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

    private suspend fun simulateDay3Violations() {
        // Severe violations - 5 contact attempts + emergency tampering
        repeat(5) { index ->
            contactDao.insertContactModificationAttempt(
                ContactModificationAttempt(
                    attemptId = "day3-violation-$index",
                    caregiverId = testCaregiverId,
                    userId = testUserId,
                    action = ContactAction.REMOVE_CONTACT,
                    result = ContactActionResult.BLOCKED_BY_PROTECTION,
                    contactInfo = ContactInfo("Day 3 Contact $index", "+123", "friend"),
                    timestamp = currentTime - TimeUnit.HOURS.toMillis(index.toLong())
                )
            )
        }
        
        // Emergency system tampering
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
}
