package com.naviya.launcher.data.models

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.util.*

/**
 * Unit tests for NotificationState data model
 * Tests unread notifications tile combining missed calls and SMS
 */
class NotificationStateTest {

    private lateinit var testNotificationState: NotificationState
    private lateinit var testUserId: String

    @Before
    fun setUp() {
        testUserId = UUID.randomUUID().toString()
        
        testNotificationState = NotificationState(
            userId = testUserId,
            unreadSummary = UnreadSummary(),
            missedCalls = createTestMissedCalls(),
            unreadSms = createTestUnreadSms(),
            priorityContacts = createTestPriorityContacts(),
            tileDisplay = TileDisplaySettings(),
            offlineState = OfflineNotificationState(),
            caregiverIntegration = CaregiverNotificationSettings(),
            privacySettings = NotificationPrivacySettings(),
            performanceMetrics = NotificationPerformanceMetrics()
        )
    }

    @Test
    fun `test unread summary calculation with priority weighting`() {
        val summary = testNotificationState.unreadSummary
        
        // Test basic counts
        assertEquals("Should have 2 missed calls", 2, summary.totalMissedCalls)
        assertEquals("Should have 3 unread SMS", 3, summary.totalUnreadSms)
        assertEquals("Should have 5 total unread", 5, summary.totalUnread)
        
        // Test priority weighting (emergency ×3, caregiver ×2, normal ×1)
        // 1 emergency call (×3) + 1 normal call (×1) + 1 caregiver SMS (×2) + 2 normal SMS (×1) = 8
        assertEquals("Priority weighted count should be 8", 8, summary.priorityWeightedCount)
        
        // Test display count (max 99)
        assertEquals("Display count should be 5", 5, summary.displayCount)
        assertEquals("Display text should be '5'", "5", summary.displayText)
    }

    @Test
    fun `test missed calls tracking`() {
        val missedCalls = testNotificationState.missedCalls
        
        assertEquals("Should have 2 missed calls", 2, missedCalls.size)
        
        // Test emergency call
        val emergencyCall = missedCalls.find { it.contactType == ContactType.EMERGENCY }
        assertNotNull("Should have emergency call", emergencyCall)
        assertEquals("Emergency call should have priority weight 3", 3, emergencyCall!!.priorityWeight)
        assertTrue("Emergency call should be marked as emergency", emergencyCall.isEmergencyContact)
        
        // Test normal call
        val normalCall = missedCalls.find { it.contactType == ContactType.NORMAL }
        assertNotNull("Should have normal call", normalCall)
        assertEquals("Normal call should have priority weight 1", 1, normalCall!!.priorityWeight)
        assertFalse("Normal call should not be marked as emergency", normalCall.isEmergencyContact)
    }

    @Test
    fun `test unread SMS tracking`() {
        val unreadSms = testNotificationState.unreadSms
        
        assertEquals("Should have 3 unread SMS", 3, unreadSms.size)
        
        // Test caregiver SMS
        val caregiverSms = unreadSms.find { it.contactType == ContactType.CAREGIVER }
        assertNotNull("Should have caregiver SMS", caregiverSms)
        assertEquals("Caregiver SMS should have priority weight 2", 2, caregiverSms!!.priorityWeight)
        assertTrue("Caregiver SMS should be marked as caregiver", caregiverSms.isCaregiverContact)
        
        // Test normal SMS
        val normalSms = unreadSms.filter { it.contactType == ContactType.NORMAL }
        assertEquals("Should have 2 normal SMS", 2, normalSms.size)
        normalSms.forEach { sms ->
            assertEquals("Normal SMS should have priority weight 1", 1, sms.priorityWeight)
            assertFalse("Normal SMS should not be marked as caregiver", sms.isCaregiverContact)
        }
    }

    @Test
    fun `test priority contacts configuration`() {
        val priorityContacts = testNotificationState.priorityContacts
        
        assertEquals("Should have 3 priority contacts", 3, priorityContacts.size)
        
        // Test emergency contact
        val emergencyContact = priorityContacts.find { it.contactType == ContactType.EMERGENCY }
        assertNotNull("Should have emergency contact", emergencyContact)
        assertEquals("Emergency contact should have weight 3", 3, emergencyContact!!.priorityWeight)
        assertTrue("Emergency contact should be enabled", emergencyContact.isEnabled)
        
        // Test caregiver contact
        val caregiverContact = priorityContacts.find { it.contactType == ContactType.CAREGIVER }
        assertNotNull("Should have caregiver contact", caregiverContact)
        assertEquals("Caregiver contact should have weight 2", 2, caregiverContact!!.priorityWeight)
        assertTrue("Caregiver contact should be enabled", caregiverContact.isEnabled)
        
        // Test normal contact
        val normalContact = priorityContacts.find { it.contactType == ContactType.NORMAL }
        assertNotNull("Should have normal contact", normalContact)
        assertEquals("Normal contact should have weight 1", 1, normalContact!!.priorityWeight)
        assertTrue("Normal contact should be enabled", normalContact.isEnabled)
    }

    @Test
    fun `test tile display settings for elderly users`() {
        val tileDisplay = testNotificationState.tileDisplay
        
        // Test elderly-friendly defaults
        assertTrue("Badge should be shown", tileDisplay.showBadge)
        assertTrue("Count should be shown", tileDisplay.showCount)
        assertEquals("Max display count should be 99", 99, tileDisplay.maxDisplayCount)
        assertEquals("Overflow indicator should be '99+'", "99+", tileDisplay.overflowIndicator)
        
        // Test icon and colors
        assertEquals("Icon should be envelope", "envelope", tileDisplay.iconName)
        assertEquals("Badge color should be red for visibility", "#FF0000", tileDisplay.badgeColor)
        assertEquals("Text color should be white for contrast", "#FFFFFF", tileDisplay.textColor)
        
        // Test accessibility
        assertTrue("High contrast should be enabled", tileDisplay.highContrastEnabled)
        assertTrue("Large badge should be enabled", tileDisplay.largeBadgeEnabled)
        assertEquals("Badge size should be 24dp for elderly users", 24, tileDisplay.badgeSizeDp)
    }

    @Test
    fun `test offline notification state`() {
        val offlineState = testNotificationState.offlineState
        
        // Test offline capabilities
        assertTrue("Offline access should be enabled", offlineState.offlineAccessEnabled)
        assertTrue("Local caching should be enabled", offlineState.localCachingEnabled)
        assertTrue("Sync when online should be enabled", offlineState.syncWhenOnlineEnabled)
        
        // Test cache settings
        assertEquals("Cache duration should be 72 hours", 72, offlineState.cacheDurationHours)
        assertEquals("Max cached items should be 1000", 1000, offlineState.maxCachedItems)
        
        // Test offline indicators
        assertTrue("Offline indicator should be shown", offlineState.showOfflineIndicator)
        assertEquals("Offline indicator text should be 'Offline'", "Offline", offlineState.offlineIndicatorText)
    }

    @Test
    fun `test caregiver integration settings`() {
        val caregiverIntegration = testNotificationState.caregiverIntegration
        
        // Test caregiver access
        assertTrue("Caregiver access should be enabled", caregiverIntegration.caregiverAccessEnabled)
        assertTrue("Caregiver notifications should be enabled", caregiverIntegration.caregiverNotificationsEnabled)
        assertTrue("Remote viewing should be enabled", caregiverIntegration.remoteViewingEnabled)
        
        // Test privacy controls
        assertFalse("Full message content should be disabled by default", caregiverIntegration.shareFullMessageContent)
        assertTrue("Contact names should be shared", caregiverIntegration.shareContactNames)
        assertTrue("Timestamps should be shared", caregiverIntegration.shareTimestamps)
        
        // Test notification settings
        assertTrue("High priority alerts should be enabled", caregiverIntegration.highPriorityAlertsEnabled)
        assertEquals("Alert threshold should be 5", 5, caregiverIntegration.alertThreshold)
        assertEquals("Alert delay should be 30 minutes", 30, caregiverIntegration.alertDelayMinutes)
    }

    @Test
    fun `test privacy settings compliance`() {
        val privacySettings = testNotificationState.privacySettings
        
        // Test GDPR compliance
        assertTrue("Data consent should be required", privacySettings.dataConsentRequired)
        assertTrue("User control should be enabled", privacySettings.userControlEnabled)
        assertTrue("Data deletion should be enabled", privacySettings.dataDeletionEnabled)
        
        // Test content privacy
        assertFalse("Message preview should be disabled by default", privacySettings.showMessagePreview)
        assertTrue("Contact names should be shown", privacySettings.showContactNames)
        assertFalse("Phone numbers should be hidden by default", privacySettings.showPhoneNumbers)
        
        // Test retention settings
        assertEquals("Data retention should be 90 days", 90, privacySettings.dataRetentionDays)
        assertTrue("Auto cleanup should be enabled", privacySettings.autoCleanupEnabled)
        
        // Test audit settings
        assertTrue("Audit logging should be enabled", privacySettings.auditLoggingEnabled)
        assertEquals("Audit retention should be 365 days", 365, privacySettings.auditRetentionDays)
    }

    @Test
    fun `test performance metrics tracking`() {
        val performanceMetrics = testNotificationState.performanceMetrics
        
        // Test response times
        assertTrue("Response time should be reasonable", performanceMetrics.averageResponseTimeMs < 1000)
        assertTrue("Update frequency should be reasonable", performanceMetrics.updateFrequencySeconds >= 30)
        
        // Test resource usage
        assertTrue("Memory usage should be reasonable", performanceMetrics.memoryUsageMb < 50)
        assertTrue("Battery impact should be low", performanceMetrics.batteryImpactPercent < 5.0)
        
        // Test error tracking
        assertTrue("Error rate should be low", performanceMetrics.errorRate < 0.01)
        assertEquals("Crash count should be 0", 0, performanceMetrics.crashCount)
        
        // Test cache performance
        assertTrue("Cache hit rate should be high", performanceMetrics.cacheHitRate > 0.8)
        assertTrue("Sync success rate should be high", performanceMetrics.syncSuccessRate > 0.95)
    }

    @Test
    fun `test notification count overflow handling`() {
        // Test with high counts
        val highCountSummary = UnreadSummary(
            totalMissedCalls = 150,
            totalUnreadSms = 200,
            totalUnread = 350,
            priorityWeightedCount = 500
        )
        
        assertEquals("Display count should be capped at 99", 99, highCountSummary.displayCount)
        assertEquals("Display text should show overflow", "99+", highCountSummary.displayText)
        assertTrue("Should indicate overflow", highCountSummary.hasOverflow)
    }

    @Test
    fun `test state synchronization metadata`() {
        // Test sync metadata
        assertNotNull("Last sync should not be null", testNotificationState.lastSyncTimestamp)
        assertEquals("Sync status should be synced", SyncStatus.SYNCED, testNotificationState.syncStatus)
        assertNull("Sync error should be null when synced", testNotificationState.syncError)
        
        // Test version tracking
        assertEquals("Schema version should be 1.0.0", "1.0.0", testNotificationState.schemaVersion)
        assertTrue("Version should be greater than 0", testNotificationState.version > 0)
    }

    /**
     * Helper methods to create test data
     */
    private fun createTestMissedCalls(): List<MissedCall> {
        return listOf(
            MissedCall(
                contactId = "emergency_contact",
                contactName = "Emergency Services",
                phoneNumber = "+49-112",
                timestamp = Date(),
                contactType = ContactType.EMERGENCY,
                priorityWeight = 3,
                isEmergencyContact = true
            ),
            MissedCall(
                contactId = "normal_contact",
                contactName = "Anna Schmidt",
                phoneNumber = "+49-123-456789",
                timestamp = Date(),
                contactType = ContactType.NORMAL,
                priorityWeight = 1,
                isEmergencyContact = false
            )
        )
    }

    private fun createTestUnreadSms(): List<UnreadSms> {
        return listOf(
            UnreadSms(
                contactId = "caregiver_contact",
                contactName = "Maria (Caregiver)",
                phoneNumber = "+49-987-654321",
                messagePreview = "How are you feeling today?",
                timestamp = Date(),
                contactType = ContactType.CAREGIVER,
                priorityWeight = 2,
                isCaregiverContact = true
            ),
            UnreadSms(
                contactId = "normal_contact_1",
                contactName = "Hans Mueller",
                phoneNumber = "+49-111-222333",
                messagePreview = "See you tomorrow",
                timestamp = Date(),
                contactType = ContactType.NORMAL,
                priorityWeight = 1,
                isCaregiverContact = false
            ),
            UnreadSms(
                contactId = "normal_contact_2",
                contactName = "Petra Weber",
                phoneNumber = "+49-444-555666",
                messagePreview = "Happy birthday!",
                timestamp = Date(),
                contactType = ContactType.NORMAL,
                priorityWeight = 1,
                isCaregiverContact = false
            )
        )
    }

    private fun createTestPriorityContacts(): List<PriorityContact> {
        return listOf(
            PriorityContact(
                contactId = "emergency_contact",
                contactName = "Emergency Services",
                phoneNumber = "+49-112",
                contactType = ContactType.EMERGENCY,
                priorityWeight = 3,
                isEnabled = true
            ),
            PriorityContact(
                contactId = "caregiver_contact",
                contactName = "Maria (Caregiver)",
                phoneNumber = "+49-987-654321",
                contactType = ContactType.CAREGIVER,
                priorityWeight = 2,
                isEnabled = true
            ),
            PriorityContact(
                contactId = "normal_contact",
                contactName = "Anna Schmidt",
                phoneNumber = "+49-123-456789",
                contactType = ContactType.NORMAL,
                priorityWeight = 1,
                isEnabled = true
            )
        )
    }
}
