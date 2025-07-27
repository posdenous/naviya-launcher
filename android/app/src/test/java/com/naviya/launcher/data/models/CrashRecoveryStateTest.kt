package com.naviya.launcher.data.models

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.util.*

/**
 * Unit tests for CrashRecoveryState data model
 * Tests 3-crash threshold and safe mode recovery for elderly users
 */
class CrashRecoveryStateTest {

    private lateinit var testCrashRecoveryState: CrashRecoveryState
    private lateinit var testUserId: String

    @Before
    fun setUp() {
        testUserId = UUID.randomUUID().toString()
        
        testCrashRecoveryState = CrashRecoveryState(
            userId = testUserId,
            crashTracking = CrashTracking(),
            crashHistory = createTestCrashHistory(),
            recoveryMode = RecoveryModeSettings(),
            safeTiles = createTestSafeTiles(),
            recoveryAssistance = RecoveryAssistanceSettings(),
            exitConditions = ExitConditions(),
            postRecoveryMonitoring = PostRecoveryMonitoring(),
            systemIntegration = SystemIntegrationSettings(),
            analytics = CrashAnalytics()
        )
    }

    @Test
    fun `test crash threshold detection`() {
        val crashTracking = testCrashRecoveryState.crashTracking
        
        // Test 3-crash threshold
        assertEquals("Crash threshold should be 3", 3, crashTracking.crashThreshold)
        assertEquals("Current crash count should be 2", 2, crashTracking.currentCrashCount)
        assertEquals("Tracking period should be 24 hours", 24, crashTracking.trackingPeriodHours)
        
        // Test threshold not reached yet
        assertFalse("Threshold should not be reached with 2 crashes", crashTracking.thresholdReached)
        assertFalse("Recovery mode should not be active", crashTracking.recoveryModeActive)
        
        // Test threshold reached
        val thresholdReached = crashTracking.copy(currentCrashCount = 3)
        assertTrue("Threshold should be reached with 3 crashes", thresholdReached.thresholdReached)
        
        // Test reset after tracking period
        assertTrue("Should reset after tracking period", crashTracking.shouldResetAfterPeriod)
    }

    @Test
    fun `test crash history tracking`() {
        val crashHistory = testCrashRecoveryState.crashHistory
        
        assertEquals("Should have 2 crash entries", 2, crashHistory.size)
        
        // Test crash entry details
        val recentCrash = crashHistory.first()
        assertNotNull("Crash timestamp should not be null", recentCrash.timestamp)
        assertEquals("Crash type should be APP_CRASH", CrashType.APP_CRASH, recentCrash.crashType)
        assertTrue("Error message should not be empty", recentCrash.errorMessage.isNotEmpty())
        assertTrue("Stack trace should not be empty", recentCrash.stackTrace.isNotEmpty())
        
        // Test crash severity
        assertEquals("Recent crash should be HIGH severity", CrashSeverity.HIGH, recentCrash.severity)
        assertTrue("Should be user-facing crash", recentCrash.isUserFacing)
        assertTrue("Should be recoverable", recentCrash.isRecoverable)
        
        // Test older crash
        val olderCrash = crashHistory.last()
        assertEquals("Older crash should be MEDIUM severity", CrashSeverity.MEDIUM, olderCrash.severity)
        assertEquals("Older crash should be SYSTEM_ERROR", CrashType.SYSTEM_ERROR, olderCrash.crashType)
    }

    @Test
    fun `test recovery mode settings for elderly users`() {
        val recoveryMode = testCrashRecoveryState.recoveryMode
        
        // Test recovery mode is enabled
        assertTrue("Recovery mode should be enabled", recoveryMode.recoveryModeEnabled)
        assertTrue("Auto activation should be enabled", recoveryMode.autoActivationEnabled)
        assertTrue("Safe tiles should be enabled", recoveryMode.safeTilesEnabled)
        assertTrue("Simplified UI should be enabled", recoveryMode.simplifiedUiEnabled)
        
        // Test elderly-specific settings
        assertTrue("Large icons should be enabled for elderly users", recoveryMode.largeIconsEnabled)
        assertTrue("High contrast should be enabled", recoveryMode.highContrastEnabled)
        assertTrue("Reduced animations should be enabled", recoveryMode.reducedAnimationsEnabled)
        assertTrue("Audio feedback should be enabled", recoveryMode.audioFeedbackEnabled)
        
        // Test timeout settings
        assertEquals("Auto exit timeout should be 60 minutes", 60, recoveryMode.autoExitTimeoutMinutes)
        assertEquals("User confirmation timeout should be 30 seconds", 30, recoveryMode.userConfirmationTimeoutSeconds)
        
        // Test caregiver integration
        assertTrue("Caregiver notification should be enabled", recoveryMode.caregiverNotificationEnabled)
        assertTrue("Remote assistance should be enabled", recoveryMode.remoteAssistanceEnabled)
    }

    @Test
    fun `test safe tiles configuration`() {
        val safeTiles = testCrashRecoveryState.safeTiles
        
        assertEquals("Should have 4 safe tiles", 4, safeTiles.size)
        
        // Test essential safe tiles are present
        val tileTypes = safeTiles.map { it.tileType }
        assertTrue("Should have phone dialer", tileTypes.contains(TileType.PHONE_DIALER))
        assertTrue("Should have settings", tileTypes.contains(TileType.SETTINGS))
        assertTrue("Should have SOS emergency", tileTypes.contains(TileType.SOS_EMERGENCY))
        assertTrue("Should have help/support", tileTypes.contains(TileType.HELP_SUPPORT))
        
        // Test safe tile properties
        safeTiles.forEach { tile ->
            assertTrue("Safe tile should be enabled", tile.isEnabled)
            assertTrue("Safe tile should be visible", tile.isVisible)
            assertEquals("Safe tile should have emergency priority", TilePriority.EMERGENCY, tile.priority)
            assertTrue("Safe tile should be crash-safe", tile.isCrashSafe)
            assertFalse("Safe tile should not be removable", tile.isRemovable)
        }
        
        // Test 2x2 grid layout for recovery mode
        val positions = safeTiles.map { it.position }
        assertEquals("Positions should be 0,1,2,3 for 2x2 grid", listOf(0, 1, 2, 3), positions.sorted())
    }

    @Test
    fun `test recovery assistance for elderly users`() {
        val recoveryAssistance = testCrashRecoveryState.recoveryAssistance
        
        // Test assistance features
        assertTrue("Step-by-step guidance should be enabled", recoveryAssistance.stepByStepGuidanceEnabled)
        assertTrue("Voice instructions should be enabled", recoveryAssistance.voiceInstructionsEnabled)
        assertTrue("Visual indicators should be enabled", recoveryAssistance.visualIndicatorsEnabled)
        assertTrue("Caregiver contact should be enabled", recoveryAssistance.caregiverContactEnabled)
        
        // Test multilingual support
        assertEquals("Should support 5 languages", 5, recoveryAssistance.supportedLanguages.size)
        val expectedLanguages = listOf("en", "de", "tr", "ar", "uk")
        assertEquals("Should support correct languages", expectedLanguages, recoveryAssistance.supportedLanguages)
        
        // Test assistance timeout
        assertEquals("Assistance timeout should be 300 seconds", 300, recoveryAssistance.assistanceTimeoutSeconds)
        assertEquals("Max assistance steps should be 10", 10, recoveryAssistance.maxAssistanceSteps)
        
        // Test emergency escalation
        assertTrue("Emergency escalation should be enabled", recoveryAssistance.emergencyEscalationEnabled)
        assertEquals("Escalation delay should be 180 seconds", 180, recoveryAssistance.escalationDelaySeconds)
    }

    @Test
    fun `test exit conditions from recovery mode`() {
        val exitConditions = testCrashRecoveryState.exitConditions
        
        // Test stability requirements
        assertEquals("Stability period should be 30 minutes", 30, exitConditions.stabilityPeriodMinutes)
        assertEquals("Required stable operations should be 10", 10, exitConditions.requiredStableOperations)
        assertEquals("Max allowed errors should be 0", 0, exitConditions.maxAllowedErrors)
        
        // Test user confirmation
        assertTrue("User confirmation should be required", exitConditions.userConfirmationRequired)
        assertTrue("Caregiver approval should be required", exitConditions.caregiverApprovalRequired)
        assertFalse("Auto exit should be disabled by default", exitConditions.autoExitEnabled)
        
        // Test safety checks
        assertTrue("System health check should be required", exitConditions.systemHealthCheckRequired)
        assertTrue("Memory check should be required", exitConditions.memoryCheckRequired)
        assertTrue("Performance check should be required", exitConditions.performanceCheckRequired)
        
        // Test gradual transition
        assertTrue("Gradual transition should be enabled", exitConditions.gradualTransitionEnabled)
        assertEquals("Transition steps should be 3", 3, exitConditions.transitionSteps)
        assertEquals("Step duration should be 60 seconds", 60, exitConditions.stepDurationSeconds)
    }

    @Test
    fun `test post-recovery monitoring`() {
        val postRecoveryMonitoring = testCrashRecoveryState.postRecoveryMonitoring
        
        // Test monitoring period
        assertEquals("Monitoring period should be 48 hours", 48, postRecoveryMonitoring.monitoringPeriodHours)
        assertTrue("Enhanced monitoring should be enabled", postRecoveryMonitoring.enhancedMonitoringEnabled)
        assertTrue("Frequent health checks should be enabled", postRecoveryMonitoring.frequentHealthChecksEnabled)
        
        // Test monitoring frequency
        assertEquals("Health check frequency should be 5 minutes", 5, postRecoveryMonitoring.healthCheckFrequencyMinutes)
        assertEquals("Performance check frequency should be 15 minutes", 15, postRecoveryMonitoring.performanceCheckFrequencyMinutes)
        assertEquals("Stability check frequency should be 30 minutes", 30, postRecoveryMonitoring.stabilityCheckFrequencyMinutes)
        
        // Test alert thresholds
        assertEquals("Error threshold should be 1", 1, postRecoveryMonitoring.errorThreshold)
        assertEquals("Performance degradation threshold should be 20%", 20, postRecoveryMonitoring.performanceDegradationThreshold)
        assertEquals("Memory usage threshold should be 80%", 80, postRecoveryMonitoring.memoryUsageThreshold)
        
        // Test caregiver notifications
        assertTrue("Caregiver alerts should be enabled", postRecoveryMonitoring.caregiverAlertsEnabled)
        assertEquals("Alert frequency should be 60 minutes", 60, postRecoveryMonitoring.alertFrequencyMinutes)
    }

    @Test
    fun `test system integration settings`() {
        val systemIntegration = testCrashRecoveryState.systemIntegration
        
        // Test Android integration
        assertTrue("System crash handler should be enabled", systemIntegration.systemCrashHandlerEnabled)
        assertTrue("ANR detection should be enabled", systemIntegration.anrDetectionEnabled)
        assertTrue("Memory monitoring should be enabled", systemIntegration.memoryMonitoringEnabled)
        assertTrue("Battery monitoring should be enabled", systemIntegration.batteryMonitoringEnabled)
        
        // Test Firebase integration
        assertTrue("Firebase crashlytics should be enabled", systemIntegration.firebaseCrashlyticsEnabled)
        assertTrue("Remote logging should be enabled", systemIntegration.remoteLoggingEnabled)
        assertTrue("Real-time monitoring should be enabled", systemIntegration.realTimeMonitoringEnabled)
        
        // Test accessibility integration
        assertTrue("TalkBack integration should be enabled", systemIntegration.talkBackIntegrationEnabled)
        assertTrue("Switch access integration should be enabled", systemIntegration.switchAccessIntegrationEnabled)
        assertTrue("Voice access integration should be enabled", systemIntegration.voiceAccessIntegrationEnabled)
        
        // Test emergency services integration
        assertTrue("Emergency services integration should be enabled", systemIntegration.emergencyServicesIntegrationEnabled)
        assertEquals("Emergency contact should be set", "+49-112", systemIntegration.emergencyContactNumber)
    }

    @Test
    fun `test crash analytics and reporting`() {
        val analytics = testCrashRecoveryState.analytics
        
        // Test analytics collection
        assertTrue("Analytics should be enabled", analytics.analyticsEnabled)
        assertTrue("Crash reporting should be enabled", analytics.crashReportingEnabled)
        assertTrue("Performance metrics should be enabled", analytics.performanceMetricsEnabled)
        assertTrue("User behavior tracking should be enabled", analytics.userBehaviorTrackingEnabled)
        
        // Test privacy compliance
        assertTrue("Data anonymization should be enabled", analytics.dataAnonymizationEnabled)
        assertTrue("User consent should be required", analytics.userConsentRequired)
        assertFalse("Personal data collection should be disabled", analytics.personalDataCollectionEnabled)
        
        // Test reporting frequency
        assertEquals("Reporting frequency should be daily", ReportingFrequency.DAILY, analytics.reportingFrequency)
        assertEquals("Data retention should be 90 days", 90, analytics.dataRetentionDays)
        
        // Test metrics
        assertTrue("Recovery success rate should be high", analytics.recoverySuccessRate > 0.9)
        assertTrue("Average recovery time should be reasonable", analytics.averageRecoveryTimeMinutes < 10)
        assertTrue("User satisfaction should be high", analytics.userSatisfactionScore > 4.0)
    }

    @Test
    fun `test crash recovery state transitions`() {
        val initialState = testCrashRecoveryState
        
        // Test normal to recovery mode transition
        assertFalse("Should not be in recovery mode initially", initialState.crashTracking.recoveryModeActive)
        
        val recoveryState = initialState.copy(
            crashTracking = initialState.crashTracking.copy(
                currentCrashCount = 3,
                thresholdReached = true,
                recoveryModeActive = true
            )
        )
        
        assertTrue("Should be in recovery mode after threshold", recoveryState.crashTracking.recoveryModeActive)
        assertTrue("Threshold should be reached", recoveryState.crashTracking.thresholdReached)
        
        // Test recovery to normal transition
        val normalState = recoveryState.copy(
            crashTracking = recoveryState.crashTracking.copy(
                recoveryModeActive = false,
                currentCrashCount = 0,
                thresholdReached = false
            )
        )
        
        assertFalse("Should not be in recovery mode after exit", normalState.crashTracking.recoveryModeActive)
        assertFalse("Threshold should not be reached after reset", normalState.crashTracking.thresholdReached)
    }

    /**
     * Helper methods to create test data
     */
    private fun createTestCrashHistory(): List<CrashEntry> {
        return listOf(
            CrashEntry(
                timestamp = Date(),
                crashType = CrashType.APP_CRASH,
                errorMessage = "NullPointerException in LauncherActivity",
                stackTrace = "java.lang.NullPointerException\n\tat com.naviya.launcher.ui.launcher.LauncherActivity.onCreate(LauncherActivity.kt:45)",
                severity = CrashSeverity.HIGH,
                isUserFacing = true,
                isRecoverable = true,
                recoveryAction = "Restart activity with safe defaults"
            ),
            CrashEntry(
                timestamp = Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                crashType = CrashType.SYSTEM_ERROR,
                errorMessage = "OutOfMemoryError",
                stackTrace = "java.lang.OutOfMemoryError: Failed to allocate memory",
                severity = CrashSeverity.MEDIUM,
                isUserFacing = false,
                isRecoverable = true,
                recoveryAction = "Clear cache and restart"
            )
        )
    }

    private fun createTestSafeTiles(): List<SafeTileConfiguration> {
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
