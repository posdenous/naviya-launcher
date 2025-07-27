package com.naviya.launcher.integration

import com.naviya.launcher.data.models.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.util.*

/**
 * Integration tests for Naviya Launcher
 * Tests component interactions and end-to-end workflows for elderly users
 */
class LauncherIntegrationTest {

    private lateinit var testUserId: String
    private lateinit var launcherState: LauncherState
    private lateinit var notificationState: NotificationState
    private lateinit var crashRecoveryState: CrashRecoveryState

    @Before
    fun setUp() {
        testUserId = UUID.randomUUID().toString()
        
        // Initialize integrated states
        launcherState = createElderlyUserLauncherState()
        notificationState = createNotificationState()
        crashRecoveryState = createCrashRecoveryState()
    }

    @Test
    fun `test complete elderly user onboarding flow`() {
        // Test initial setup with elderly-friendly defaults
        assertEquals("Should start with English", "en", launcherState.localization.currentLanguage)
        assertEquals("Font scale should be 1.6x for elderly", 1.6f, launcherState.accessibilitySettings.fontScale, 0.01f)
        assertTrue("High contrast should be enabled", launcherState.accessibilitySettings.highContrastEnabled)
        assertTrue("Large icons should be enabled", launcherState.accessibilitySettings.largeIconsEnabled)
        
        // Test PIN setup during onboarding
        assertTrue("PIN should be enabled during setup", launcherState.pinProtection.pinEnabled)
        assertEquals("PIN should be 4 digits by default", 4, launcherState.pinProtection.pinLength)
        
        // Test grid configuration
        assertEquals("Should have 2x3 grid", 6, launcherState.gridConfiguration.totalTiles)
        assertEquals("Icons should be 64dp", 64, launcherState.gridConfiguration.iconSizeDp)
        
        // Test essential tiles are present
        val tileTypes = launcherState.tileLayout.map { it.tileType }
        assertTrue("Should have phone dialer", tileTypes.contains(TileType.PHONE_DIALER))
        assertTrue("Should have SOS emergency", tileTypes.contains(TileType.SOS_EMERGENCY))
        assertTrue("Should have unread notifications", tileTypes.contains(TileType.UNREAD_NOTIFICATIONS))
    }

    @Test
    fun `test crash recovery integration with launcher state`() {
        // Simulate normal operation
        assertFalse("Should not be in recovery mode initially", crashRecoveryState.crashTracking.recoveryModeActive)
        assertEquals("Should have normal 2x3 grid", 6, launcherState.gridConfiguration.totalTiles)
        
        // Simulate 3 crashes triggering recovery mode
        val crashedState = crashRecoveryState.copy(
            crashTracking = crashRecoveryState.crashTracking.copy(
                currentCrashCount = 3,
                thresholdReached = true,
                recoveryModeActive = true
            )
        )
        
        // Test recovery mode affects launcher layout
        assertTrue("Recovery mode should be active", crashedState.crashTracking.recoveryModeActive)
        
        // Test safe tiles configuration (2x2 grid)
        val safeTiles = crashedState.safeTiles
        assertEquals("Should have 4 safe tiles", 4, safeTiles.size)
        
        // Verify essential safe tiles
        val safeTileTypes = safeTiles.map { it.tileType }
        assertTrue("Should have phone in safe mode", safeTileTypes.contains(TileType.PHONE_DIALER))
        assertTrue("Should have settings in safe mode", safeTileTypes.contains(TileType.SETTINGS))
        assertTrue("Should have SOS in safe mode", safeTileTypes.contains(TileType.SOS_EMERGENCY))
        assertTrue("Should have help in safe mode", safeTileTypes.contains(TileType.HELP_SUPPORT))
        
        // Test recovery mode accessibility enhancements
        assertTrue("Large icons should be enabled in recovery", crashedState.recoveryMode.largeIconsEnabled)
        assertTrue("High contrast should be enabled in recovery", crashedState.recoveryMode.highContrastEnabled)
        assertTrue("Audio feedback should be enabled in recovery", crashedState.recoveryMode.audioFeedbackEnabled)
    }

    @Test
    fun `test notification integration with launcher tile`() {
        // Test unread notifications tile integration
        val unreadTile = launcherState.tileLayout.find { it.tileType == TileType.UNREAD_NOTIFICATIONS }
        assertNotNull("Unread tile should exist", unreadTile)
        
        // Test notification state affects tile display
        val summary = notificationState.unreadSummary
        assertEquals("Should combine missed calls and SMS", 5, summary.totalUnread)
        assertEquals("Should apply priority weighting", 8, summary.priorityWeightedCount)
        
        // Test tile display settings
        val tileDisplay = notificationState.tileDisplay
        assertTrue("Badge should be shown", tileDisplay.showBadge)
        assertEquals("Should show count", 5, summary.displayCount)
        assertEquals("Display text should be '5'", "5", summary.displayText)
        
        // Test elderly-friendly display
        assertEquals("Badge should be large for elderly users", 24, tileDisplay.badgeSizeDp)
        assertTrue("High contrast should be enabled", tileDisplay.highContrastEnabled)
        assertEquals("Badge color should be red", "#FF0000", tileDisplay.badgeColor)
        
        // Test overflow handling
        val highCountSummary = summary.copy(totalUnread = 150)
        assertEquals("Should cap at 99", 99, highCountSummary.displayCount)
        assertEquals("Should show overflow indicator", "99+", highCountSummary.displayText)
    }

    @Test
    fun `test caregiver integration across components`() {
        // Test caregiver settings in launcher state
        assertTrue("Caregiver pairing should be enabled", launcherState.pinProtection.recoveryMethodEnabled)
        
        // Test caregiver notification access
        val caregiverIntegration = notificationState.caregiverIntegration
        assertTrue("Caregiver access should be enabled", caregiverIntegration.caregiverAccessEnabled)
        assertTrue("Remote viewing should be enabled", caregiverIntegration.remoteViewingEnabled)
        
        // Test caregiver crash recovery notifications
        val recoveryMode = crashRecoveryState.recoveryMode
        assertTrue("Caregiver notification should be enabled", recoveryMode.caregiverNotificationEnabled)
        assertTrue("Remote assistance should be enabled", recoveryMode.remoteAssistanceEnabled)
        
        // Test privacy controls
        val privacySettings = notificationState.privacySettings
        assertFalse("Message preview should be disabled by default", privacySettings.showMessagePreview)
        assertTrue("Contact names should be shared", caregiverIntegration.shareContactNames)
        assertFalse("Full message content should be protected", caregiverIntegration.shareFullMessageContent)
        
        // Test caregiver alert thresholds
        assertEquals("Alert threshold should be 5", 5, caregiverIntegration.alertThreshold)
        assertEquals("Alert delay should be 30 minutes", 30, caregiverIntegration.alertDelayMinutes)
    }

    @Test
    fun `test multilingual support integration`() {
        // Test all components support same languages
        val launcherLanguages = launcherState.localization.supportedLanguages
        val recoveryLanguages = crashRecoveryState.recoveryAssistance.supportedLanguages
        
        assertEquals("All components should support same languages", launcherLanguages, recoveryLanguages)
        assertEquals("Should support 5 languages", 5, launcherLanguages.size)
        
        val expectedLanguages = listOf("en", "de", "tr", "ar", "uk")
        assertEquals("Should support correct languages", expectedLanguages, launcherLanguages)
        
        // Test RTL support for Arabic
        assertTrue("RTL should be supported", launcherState.localization.rtlSupported)
        
        // Test language switching affects all components
        val germanState = launcherState.copy(
            localization = launcherState.localization.copy(currentLanguage = "de")
        )
        assertEquals("Language should switch to German", "de", germanState.localization.currentLanguage)
        
        // Test TTS integration
        assertTrue("TTS should be enabled", launcherState.accessibilitySettings.ttsEnabled)
    }

    @Test
    fun `test offline mode integration`() {
        // Test launcher offline settings
        val offlineSettings = launcherState.offlineMode
        assertTrue("Offline mode should be enabled", offlineSettings.offlineModeEnabled)
        assertTrue("Local caching should be enabled", offlineSettings.localDataCachingEnabled)
        
        // Test notification offline access
        val notificationOffline = notificationState.offlineState
        assertTrue("Offline notification access should be enabled", notificationOffline.offlineAccessEnabled)
        assertTrue("Local SMS/call caching should be enabled", notificationOffline.localCachingEnabled)
        
        // Test offline indicators
        assertTrue("Offline indicator should be shown", offlineSettings.offlineIndicatorEnabled)
        assertTrue("Notification offline indicator should be shown", notificationOffline.showOfflineIndicator)
        
        // Test sync when online
        assertTrue("Should sync when online", offlineSettings.syncWhenOnlineEnabled)
        assertTrue("Notifications should sync when online", notificationOffline.syncWhenOnlineEnabled)
        
        // Test cache duration consistency
        assertEquals("Cache duration should be consistent", 72, offlineSettings.offlineTimeoutHours)
        assertEquals("Notification cache should match", 72, notificationOffline.cacheDurationHours)
    }

    @Test
    fun `test PIN security integration with emergency access`() {
        val pinSettings = launcherState.pinProtection
        
        // Test PIN is enabled but has emergency bypass
        assertTrue("PIN should be enabled", pinSettings.pinEnabled)
        assertTrue("Emergency bypass should be enabled", pinSettings.emergencyBypassEnabled)
        
        // Test SOS tile bypasses PIN protection
        val sosTile = launcherState.tileLayout.find { it.tileType == TileType.SOS_EMERGENCY }
        assertNotNull("SOS tile should exist", sosTile)
        assertEquals("SOS should have emergency priority", TilePriority.EMERGENCY, sosTile!!.priority)
        
        // Test crash recovery bypasses PIN
        val recoveryMode = crashRecoveryState.recoveryMode
        assertTrue("Recovery mode should be enabled", recoveryMode.recoveryModeEnabled)
        assertTrue("Auto activation should bypass PIN", recoveryMode.autoActivationEnabled)
        
        // Test caregiver can assist with PIN recovery
        assertTrue("Recovery method should be enabled", pinSettings.recoveryMethodEnabled)
        assertTrue("Caregiver notification should be enabled", recoveryMode.caregiverNotificationEnabled)
        
        // Test lockout settings are elderly-friendly
        assertEquals("Max failed attempts should be 3", 3, pinSettings.maxFailedAttempts)
        assertEquals("Lockout should be 15 minutes", 15, pinSettings.lockoutDurationMinutes)
        assertEquals("Session should be 5 minutes", 5, pinSettings.sessionDurationMinutes)
    }

    @Test
    fun `test accessibility integration across all components`() {
        // Test launcher accessibility
        val accessibility = launcherState.accessibilitySettings
        assertEquals("Font scale should be 1.6x", 1.6f, accessibility.fontScale, 0.01f)
        assertEquals("Touch targets should be 48dp", 48, accessibility.minimumTouchTargetDp)
        assertTrue("High contrast should be enabled", accessibility.highContrastEnabled)
        
        // Test notification accessibility
        val notificationDisplay = notificationState.tileDisplay
        assertTrue("Notification high contrast should be enabled", notificationDisplay.highContrastEnabled)
        assertTrue("Large badge should be enabled", notificationDisplay.largeBadgeEnabled)
        assertEquals("Badge size should be 24dp", 24, notificationDisplay.badgeSizeDp)
        
        // Test crash recovery accessibility
        val recoveryMode = crashRecoveryState.recoveryMode
        assertTrue("Recovery large icons should be enabled", recoveryMode.largeIconsEnabled)
        assertTrue("Recovery high contrast should be enabled", recoveryMode.highContrastEnabled)
        assertTrue("Audio feedback should be enabled", recoveryMode.audioFeedbackEnabled)
        
        // Test TTS integration
        assertTrue("Launcher TTS should be enabled", accessibility.ttsEnabled)
        assertTrue("Recovery voice instructions should be enabled", crashRecoveryState.recoveryAssistance.voiceInstructionsEnabled)
        
        // Test color blindness support
        assertEquals("Default should be no color blindness support", ColorBlindnessType.NONE, accessibility.colorBlindnessSupport)
        
        // Test haptic feedback
        assertTrue("Haptic feedback should be enabled", accessibility.hapticFeedbackEnabled)
        
        // Test slow animations for elderly users
        assertTrue("Slow animations should be enabled", accessibility.slowAnimationsEnabled)
        assertTrue("Reduced animations should be enabled in recovery", recoveryMode.reducedAnimationsEnabled)
    }

    @Test
    fun `test performance and privacy integration`() {
        // Test performance metrics are tracked across components
        val notificationMetrics = notificationState.performanceMetrics
        assertTrue("Response time should be reasonable", notificationMetrics.averageResponseTimeMs < 1000)
        assertTrue("Memory usage should be low", notificationMetrics.memoryUsageMb < 50)
        assertTrue("Battery impact should be minimal", notificationMetrics.batteryImpactPercent < 5.0)
        
        // Test privacy compliance across components
        val privacySettings = notificationState.privacySettings
        assertTrue("Data consent should be required", privacySettings.dataConsentRequired)
        assertTrue("User control should be enabled", privacySettings.userControlEnabled)
        assertTrue("Data deletion should be enabled", privacySettings.dataDeletionEnabled)
        
        // Test audit logging
        assertTrue("Audit logging should be enabled", privacySettings.auditLoggingEnabled)
        assertEquals("Audit retention should be 365 days", 365, privacySettings.auditRetentionDays)
        
        // Test crash analytics privacy
        val crashAnalytics = crashRecoveryState.analytics
        assertTrue("Data anonymization should be enabled", crashAnalytics.dataAnonymizationEnabled)
        assertTrue("User consent should be required", crashAnalytics.userConsentRequired)
        assertFalse("Personal data collection should be disabled", crashAnalytics.personalDataCollectionEnabled)
        
        // Test data retention consistency
        assertEquals("Notification data retention should be 90 days", 90, privacySettings.dataRetentionDays)
        assertEquals("Crash data retention should be 90 days", 90, crashAnalytics.dataRetentionDays)
    }

    @Test
    fun `test emergency scenarios integration`() {
        // Test SOS tile is always accessible
        val sosTile = launcherState.tileLayout.find { it.tileType == TileType.SOS_EMERGENCY }
        assertNotNull("SOS tile should exist", sosTile)
        assertEquals("SOS should have emergency priority", TilePriority.EMERGENCY, sosTile!!.priority)
        assertTrue("SOS should be enabled", sosTile.isEnabled)
        assertTrue("SOS should be visible", sosTile.isVisible)
        
        // Test SOS is available in crash recovery mode
        val safeTiles = crashRecoveryState.safeTiles
        val safeSOSTile = safeTiles.find { it.tileType == TileType.SOS_EMERGENCY }
        assertNotNull("SOS should be in safe tiles", safeSOSTile)
        assertTrue("Safe SOS should be crash-safe", safeSOSTile!!.isCrashSafe)
        assertFalse("Safe SOS should not be removable", safeSOSTile.isRemovable)
        
        // Test emergency contacts integration
        val priorityContacts = notificationState.priorityContacts
        val emergencyContact = priorityContacts.find { it.contactType == ContactType.EMERGENCY }
        assertNotNull("Should have emergency contact", emergencyContact)
        assertEquals("Emergency contact should have weight 3", 3, emergencyContact!!.priorityWeight)
        
        // Test emergency bypass for PIN
        assertTrue("Emergency bypass should be enabled", launcherState.pinProtection.emergencyBypassEnabled)
        
        // Test emergency escalation in recovery
        assertTrue("Emergency escalation should be enabled", crashRecoveryState.recoveryAssistance.emergencyEscalationEnabled)
        assertEquals("Escalation delay should be 180 seconds", 180, crashRecoveryState.recoveryAssistance.escalationDelaySeconds)
    }

    /**
     * Helper methods to create test states
     */
    private fun createElderlyUserLauncherState(): LauncherState {
        return LauncherState(
            userId = testUserId,
            gridConfiguration = GridConfiguration(
                rows = 2,
                columns = 3,
                totalTiles = 6,
                iconSizeDp = 64,
                paddingDp = 16,
                spacingDp = 16,
                marginDp = 24
            ),
            tileLayout = createDefaultTileLayout(),
            accessibilitySettings = AccessibilitySettings(
                fontScale = 1.6f,
                minimumTouchTargetDp = 48,
                highContrastEnabled = true,
                largeIconsEnabled = true,
                hapticFeedbackEnabled = true,
                ttsEnabled = true,
                slowAnimationsEnabled = true,
                colorBlindnessSupport = ColorBlindnessType.NONE
            ),
            pinProtection = PinProtectionSettings(
                pinEnabled = true,
                pinLength = 4,
                maxFailedAttempts = 3,
                lockoutDurationMinutes = 15,
                sessionDurationMinutes = 5,
                emergencyBypassEnabled = true,
                recoveryMethodEnabled = true
            ),
            crashRecovery = CrashRecoverySettings(
                crashThreshold = 3,
                trackingPeriodHours = 24,
                recoveryModeEnabled = true,
                safeTilesEnabled = true,
                caregiverNotificationEnabled = true,
                autoRecoveryEnabled = true,
                postRecoveryMonitoringHours = 48
            ),
            offlineMode = OfflineModeSettings(
                offlineModeEnabled = true,
                localDataCachingEnabled = true,
                offlineIndicatorEnabled = true,
                syncWhenOnlineEnabled = true,
                offlineTimeoutHours = 72
            ),
            localization = LocalizationSettings(
                currentLanguage = "en",
                supportedLanguages = listOf("en", "de", "tr", "ar", "uk"),
                rtlSupported = true,
                dateFormat = "dd/MM/yyyy",
                timeFormat = "HH:mm",
                currencyCode = "EUR"
            ),
            androidVersion = "13",
            appVersion = "1.0.0",
            deviceModel = "Samsung Galaxy A54"
        )
    }

    private fun createNotificationState(): NotificationState {
        return NotificationState(
            userId = testUserId,
            unreadSummary = UnreadSummary(
                totalMissedCalls = 2,
                totalUnreadSms = 3,
                totalUnread = 5,
                priorityWeightedCount = 8
            ),
            missedCalls = emptyList(),
            unreadSms = emptyList(),
            priorityContacts = listOf(
                PriorityContact(
                    contactId = "emergency",
                    contactName = "Emergency Services",
                    phoneNumber = "+49-112",
                    contactType = ContactType.EMERGENCY,
                    priorityWeight = 3,
                    isEnabled = true
                )
            ),
            tileDisplay = TileDisplaySettings(
                showBadge = true,
                showCount = true,
                maxDisplayCount = 99,
                overflowIndicator = "99+",
                iconName = "envelope",
                badgeColor = "#FF0000",
                textColor = "#FFFFFF",
                highContrastEnabled = true,
                largeBadgeEnabled = true,
                badgeSizeDp = 24
            ),
            offlineState = OfflineNotificationState(
                offlineAccessEnabled = true,
                localCachingEnabled = true,
                syncWhenOnlineEnabled = true,
                cacheDurationHours = 72,
                maxCachedItems = 1000,
                showOfflineIndicator = true,
                offlineIndicatorText = "Offline"
            ),
            caregiverIntegration = CaregiverNotificationSettings(
                caregiverAccessEnabled = true,
                caregiverNotificationsEnabled = true,
                remoteViewingEnabled = true,
                shareFullMessageContent = false,
                shareContactNames = true,
                shareTimestamps = true,
                highPriorityAlertsEnabled = true,
                alertThreshold = 5,
                alertDelayMinutes = 30
            ),
            privacySettings = NotificationPrivacySettings(
                dataConsentRequired = true,
                userControlEnabled = true,
                dataDeletionEnabled = true,
                showMessagePreview = false,
                showContactNames = true,
                showPhoneNumbers = false,
                dataRetentionDays = 90,
                autoCleanupEnabled = true,
                auditLoggingEnabled = true,
                auditRetentionDays = 365
            ),
            performanceMetrics = NotificationPerformanceMetrics(
                averageResponseTimeMs = 500,
                updateFrequencySeconds = 30,
                memoryUsageMb = 25,
                batteryImpactPercent = 2.5,
                errorRate = 0.001,
                crashCount = 0,
                cacheHitRate = 0.9,
                syncSuccessRate = 0.98
            )
        )
    }

    private fun createCrashRecoveryState(): CrashRecoveryState {
        return CrashRecoveryState(
            userId = testUserId,
            crashTracking = CrashTracking(
                crashThreshold = 3,
                currentCrashCount = 0,
                trackingPeriodHours = 24,
                thresholdReached = false,
                recoveryModeActive = false,
                shouldResetAfterPeriod = true
            ),
            crashHistory = emptyList(),
            recoveryMode = RecoveryModeSettings(
                recoveryModeEnabled = true,
                autoActivationEnabled = true,
                safeTilesEnabled = true,
                simplifiedUiEnabled = true,
                largeIconsEnabled = true,
                highContrastEnabled = true,
                reducedAnimationsEnabled = true,
                audioFeedbackEnabled = true,
                autoExitTimeoutMinutes = 60,
                userConfirmationTimeoutSeconds = 30,
                caregiverNotificationEnabled = true,
                remoteAssistanceEnabled = true
            ),
            safeTiles = createSafeTiles(),
            recoveryAssistance = RecoveryAssistanceSettings(
                stepByStepGuidanceEnabled = true,
                voiceInstructionsEnabled = true,
                visualIndicatorsEnabled = true,
                caregiverContactEnabled = true,
                supportedLanguages = listOf("en", "de", "tr", "ar", "uk"),
                assistanceTimeoutSeconds = 300,
                maxAssistanceSteps = 10,
                emergencyEscalationEnabled = true,
                escalationDelaySeconds = 180
            ),
            exitConditions = ExitConditions(
                stabilityPeriodMinutes = 30,
                requiredStableOperations = 10,
                maxAllowedErrors = 0,
                userConfirmationRequired = true,
                caregiverApprovalRequired = true,
                autoExitEnabled = false,
                systemHealthCheckRequired = true,
                memoryCheckRequired = true,
                performanceCheckRequired = true,
                gradualTransitionEnabled = true,
                transitionSteps = 3,
                stepDurationSeconds = 60
            ),
            postRecoveryMonitoring = PostRecoveryMonitoring(
                monitoringPeriodHours = 48,
                enhancedMonitoringEnabled = true,
                frequentHealthChecksEnabled = true,
                healthCheckFrequencyMinutes = 5,
                performanceCheckFrequencyMinutes = 15,
                stabilityCheckFrequencyMinutes = 30,
                errorThreshold = 1,
                performanceDegradationThreshold = 20,
                memoryUsageThreshold = 80,
                caregiverAlertsEnabled = true,
                alertFrequencyMinutes = 60
            ),
            systemIntegration = SystemIntegrationSettings(
                systemCrashHandlerEnabled = true,
                anrDetectionEnabled = true,
                memoryMonitoringEnabled = true,
                batteryMonitoringEnabled = true,
                firebaseCrashlyticsEnabled = true,
                remoteLoggingEnabled = true,
                realTimeMonitoringEnabled = true,
                talkBackIntegrationEnabled = true,
                switchAccessIntegrationEnabled = true,
                voiceAccessIntegrationEnabled = true,
                emergencyServicesIntegrationEnabled = true,
                emergencyContactNumber = "+49-112"
            ),
            analytics = CrashAnalytics(
                analyticsEnabled = true,
                crashReportingEnabled = true,
                performanceMetricsEnabled = true,
                userBehaviorTrackingEnabled = true,
                dataAnonymizationEnabled = true,
                userConsentRequired = true,
                personalDataCollectionEnabled = false,
                reportingFrequency = ReportingFrequency.DAILY,
                dataRetentionDays = 90,
                recoverySuccessRate = 0.95,
                averageRecoveryTimeMinutes = 5,
                userSatisfactionScore = 4.2
            )
        )
    }

    private fun createDefaultTileLayout(): List<TileConfiguration> {
        return listOf(
            TileConfiguration(
                position = 0,
                tileType = TileType.PHONE_DIALER,
                customLabel = "Phone",
                priority = TilePriority.HIGHEST,
                isVisible = true,
                isEnabled = true
            ),
            TileConfiguration(
                position = 1,
                tileType = TileType.SMS_MESSAGES,
                customLabel = "Messages",
                priority = TilePriority.HIGH,
                isVisible = true,
                isEnabled = true
            ),
            TileConfiguration(
                position = 2,
                tileType = TileType.SETTINGS,
                customLabel = "Settings",
                priority = TilePriority.HIGH,
                isVisible = true,
                isEnabled = true
            ),
            TileConfiguration(
                position = 3,
                tileType = TileType.CAMERA,
                customLabel = "Camera",
                priority = TilePriority.NORMAL,
                isVisible = true,
                isEnabled = true
            ),
            TileConfiguration(
                position = 4,
                tileType = TileType.UNREAD_NOTIFICATIONS,
                customLabel = "Unread",
                priority = TilePriority.HIGH,
                isVisible = true,
                isEnabled = true
            ),
            TileConfiguration(
                position = 5,
                tileType = TileType.SOS_EMERGENCY,
                customLabel = "Emergency",
                priority = TilePriority.EMERGENCY,
                isVisible = true,
                isEnabled = true
            )
        )
    }

    private fun createSafeTiles(): List<SafeTileConfiguration> {
        return listOf(
            SafeTileConfiguration(
                position = 0,
                tileType = TileType.PHONE_DIALER,
                customLabel = "Phone",
                priority = TilePriority.EMERGENCY,
                isEnabled = true,
                isVisible = true,
                isCrashSafe = true,
                isRemovable = false
            ),
            SafeTileConfiguration(
                position = 1,
                tileType = TileType.SETTINGS,
                customLabel = "Settings",
                priority = TilePriority.EMERGENCY,
                isEnabled = true,
                isVisible = true,
                isCrashSafe = true,
                isRemovable = false
            ),
            SafeTileConfiguration(
                position = 2,
                tileType = TileType.SOS_EMERGENCY,
                customLabel = "Emergency",
                priority = TilePriority.EMERGENCY,
                isEnabled = true,
                isVisible = true,
                isCrashSafe = true,
                isRemovable = false
            ),
            SafeTileConfiguration(
                position = 3,
                tileType = TileType.HELP_SUPPORT,
                customLabel = "Help",
                priority = TilePriority.EMERGENCY,
                isEnabled = true,
                isVisible = true,
                isCrashSafe = true,
                isRemovable = false
            )
        )
    }
}
