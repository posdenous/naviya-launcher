package com.naviya.launcher.data.models

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.util.*

/**
 * Unit tests for LauncherState data model
 * Tests elderly user accessibility defaults and grid configuration
 */
class LauncherStateTest {

    private lateinit var defaultLauncherState: LauncherState
    private lateinit var testUserId: String

    @Before
    fun setUp() {
        testUserId = UUID.randomUUID().toString()
        
        // Create default launcher state for elderly users
        defaultLauncherState = LauncherState(
            userId = testUserId,
            gridConfiguration = GridConfiguration(),
            tileLayout = createDefaultTileLayout(),
            accessibilitySettings = AccessibilitySettings(),
            pinProtection = PinProtectionSettings(),
            crashRecovery = CrashRecoverySettings(),
            offlineMode = OfflineModeSettings(),
            localization = LocalizationSettings(),
            androidVersion = "13",
            appVersion = "1.0.0",
            deviceModel = "Samsung Galaxy A54"
        )
    }

    @Test
    fun `test default grid configuration for elderly users`() {
        val gridConfig = defaultLauncherState.gridConfiguration
        
        // Verify 2x3 grid layout
        assertEquals("Grid should have 2 rows", 2, gridConfig.rows)
        assertEquals("Grid should have 3 columns", 3, gridConfig.columns)
        assertEquals("Grid should have 6 total tiles", 6, gridConfig.totalTiles)
        
        // Verify elderly-friendly sizing
        assertEquals("Icons should be 64dp for elderly users", 64, gridConfig.iconSizeDp)
        assertEquals("Padding should be 16dp", 16, gridConfig.paddingDp)
        assertEquals("Spacing should be 16dp", 16, gridConfig.spacingDp)
        assertEquals("Margins should be 24dp", 24, gridConfig.marginDp)
    }

    @Test
    fun `test accessibility defaults for elderly users`() {
        val accessibility = defaultLauncherState.accessibilitySettings
        
        // Verify elderly-specific defaults
        assertEquals("Font scale should be 1.6x for elderly users", 1.6f, accessibility.fontScale, 0.01f)
        assertEquals("Touch targets should be 48dp minimum", 48, accessibility.minimumTouchTargetDp)
        assertTrue("High contrast should be enabled by default", accessibility.highContrastEnabled)
        assertTrue("Large icons should be enabled", accessibility.largeIconsEnabled)
        assertTrue("Haptic feedback should be enabled", accessibility.hapticFeedbackEnabled)
        assertTrue("TTS should be enabled by default", accessibility.ttsEnabled)
        assertTrue("Slow animations should be enabled", accessibility.slowAnimationsEnabled)
        
        // Verify audio feedback is disabled by default (can be overwhelming)
        assertFalse("Audio feedback should be disabled by default", accessibility.audioFeedbackEnabled)
        assertFalse("Voice input should be disabled by default", accessibility.voiceInputEnabled)
    }

    @Test
    fun `test PIN protection settings for elderly users`() {
        val pinSettings = defaultLauncherState.pinProtection
        
        // Verify PIN is enabled for security
        assertTrue("PIN protection should be enabled", pinSettings.pinEnabled)
        assertEquals("PIN should be 4 digits by default", 4, pinSettings.pinLength)
        assertEquals("Max failed attempts should be 3", 3, pinSettings.maxFailedAttempts)
        assertEquals("Lockout duration should be 15 minutes", 15, pinSettings.lockoutDurationMinutes)
        assertEquals("Session duration should be 5 minutes", 5, pinSettings.sessionDurationMinutes)
        
        // Verify emergency bypass is enabled
        assertTrue("Emergency bypass should be enabled", pinSettings.emergencyBypassEnabled)
        assertTrue("Recovery method should be enabled", pinSettings.recoveryMethodEnabled)
        
        // Verify biometric is disabled by default (elderly users prefer PIN)
        assertFalse("Biometric fallback should be disabled by default", pinSettings.biometricFallbackEnabled)
    }

    @Test
    fun `test crash recovery settings`() {
        val crashSettings = defaultLauncherState.crashRecovery
        
        // Verify 3-crash threshold
        assertEquals("Crash threshold should be 3", 3, crashSettings.crashThreshold)
        assertEquals("Tracking period should be 24 hours", 24, crashSettings.trackingPeriodHours)
        
        // Verify recovery features are enabled
        assertTrue("Recovery mode should be enabled", crashSettings.recoveryModeEnabled)
        assertTrue("Safe tiles should be enabled", crashSettings.safeTilesEnabled)
        assertTrue("Caregiver notification should be enabled", crashSettings.caregiverNotificationEnabled)
        assertTrue("Auto recovery should be enabled", crashSettings.autoRecoveryEnabled)
        
        // Verify post-recovery monitoring
        assertEquals("Post-recovery monitoring should be 48 hours", 48, crashSettings.postRecoveryMonitoringHours)
    }

    @Test
    fun `test offline mode settings`() {
        val offlineSettings = defaultLauncherState.offlineMode
        
        // Verify offline capabilities are enabled
        assertTrue("Offline mode should be enabled", offlineSettings.offlineModeEnabled)
        assertTrue("Local data caching should be enabled", offlineSettings.localDataCachingEnabled)
        assertTrue("Offline indicator should be enabled", offlineSettings.offlineIndicatorEnabled)
        assertTrue("Sync when online should be enabled", offlineSettings.syncWhenOnlineEnabled)
        
        // Verify timeout settings
        assertEquals("Offline timeout should be 72 hours", 72, offlineSettings.offlineTimeoutHours)
        
        // Verify essential functions only is disabled by default
        assertFalse("Essential functions only should be disabled by default", offlineSettings.essentialFunctionsOnly)
    }

    @Test
    fun `test multilingual localization settings`() {
        val localization = defaultLauncherState.localization
        
        // Verify default language
        assertEquals("Default language should be English", "en", localization.currentLanguage)
        
        // Verify all 5 supported languages
        val expectedLanguages = listOf("en", "de", "tr", "ar", "uk")
        assertEquals("Should support 5 languages", expectedLanguages, localization.supportedLanguages)
        
        // Verify RTL support for Arabic
        assertTrue("RTL should be supported", localization.rtlSupported)
        
        // Verify European defaults
        assertEquals("Date format should be European", "dd/MM/yyyy", localization.dateFormat)
        assertEquals("Time format should be 24-hour", "HH:mm", localization.timeFormat)
        assertEquals("Currency should be EUR", "EUR", localization.currencyCode)
    }

    @Test
    fun `test tile layout validation`() {
        val tileLayout = defaultLauncherState.tileLayout
        
        // Verify we have tiles for 2x3 grid
        assertTrue("Should have tiles", tileLayout.isNotEmpty())
        assertTrue("Should not exceed 6 tiles for 2x3 grid", tileLayout.size <= 6)
        
        // Verify essential tiles are present
        val tileTypes = tileLayout.map { it.tileType }
        assertTrue("Should have phone dialer", tileTypes.contains(TileType.PHONE_DIALER))
        assertTrue("Should have SMS messages", tileTypes.contains(TileType.SMS_MESSAGES))
        assertTrue("Should have settings", tileTypes.contains(TileType.SETTINGS))
        assertTrue("Should have SOS emergency", tileTypes.contains(TileType.SOS_EMERGENCY))
        
        // Verify tile positions are valid
        tileLayout.forEach { tile ->
            assertTrue("Tile position should be 0-5", tile.position in 0..5)
            assertTrue("Tile should be visible by default", tile.isVisible)
            assertTrue("Tile should be enabled by default", tile.isEnabled)
        }
    }

    @Test
    fun `test state metadata`() {
        // Verify schema version
        assertEquals("Schema version should be 1.0.0", "1.0.0", defaultLauncherState.schemaVersion)
        
        // Verify required metadata is present
        assertNotNull("User ID should not be null", defaultLauncherState.userId)
        assertNotNull("Android version should not be null", defaultLauncherState.androidVersion)
        assertNotNull("App version should not be null", defaultLauncherState.appVersion)
        assertNotNull("Device model should not be null", defaultLauncherState.deviceModel)
        assertNotNull("Created at should not be null", defaultLauncherState.createdAt)
        assertNotNull("Updated at should not be null", defaultLauncherState.updatedAt)
        
        // Verify sync status default
        assertEquals("Sync status should be pending by default", SyncStatus.PENDING, defaultLauncherState.syncStatus)
    }

    @Test
    fun `test launcher state immutability`() {
        val originalState = defaultLauncherState
        
        // Create modified state
        val modifiedState = originalState.copy(
            accessibilitySettings = originalState.accessibilitySettings.copy(fontScale = 2.0f)
        )
        
        // Verify original state is unchanged
        assertEquals("Original font scale should be unchanged", 1.6f, originalState.accessibilitySettings.fontScale, 0.01f)
        assertEquals("Modified font scale should be changed", 2.0f, modifiedState.accessibilitySettings.fontScale, 0.01f)
    }

    @Test
    fun `test color blindness support`() {
        val accessibility = defaultLauncherState.accessibilitySettings
        
        // Verify default is no color blindness support
        assertEquals("Default should be no color blindness support", ColorBlindnessType.NONE, accessibility.colorBlindnessSupport)
        
        // Test all color blindness types are available
        val protanopiaSettings = accessibility.copy(colorBlindnessSupport = ColorBlindnessType.PROTANOPIA)
        assertEquals("Should support protanopia", ColorBlindnessType.PROTANOPIA, protanopiaSettings.colorBlindnessSupport)
        
        val deuteranopiaSettings = accessibility.copy(colorBlindnessSupport = ColorBlindnessType.DEUTERANOPIA)
        assertEquals("Should support deuteranopia", ColorBlindnessType.DEUTERANOPIA, deuteranopiaSettings.colorBlindnessSupport)
        
        val tritanopiaSettings = accessibility.copy(colorBlindnessSupport = ColorBlindnessType.TRITANOPIA)
        assertEquals("Should support tritanopia", ColorBlindnessType.TRITANOPIA, tritanopiaSettings.colorBlindnessSupport)
    }

    /**
     * Helper method to create default tile layout for testing
     */
    private fun createDefaultTileLayout(): List<TileConfiguration> {
        return listOf(
            TileConfiguration(
                position = 0,
                tileType = TileType.PHONE_DIALER,
                customLabel = "Phone",
                priority = TilePriority.HIGHEST
            ),
            TileConfiguration(
                position = 1,
                tileType = TileType.SMS_MESSAGES,
                customLabel = "Messages",
                priority = TilePriority.HIGH
            ),
            TileConfiguration(
                position = 2,
                tileType = TileType.SETTINGS,
                customLabel = "Settings",
                priority = TilePriority.HIGH
            ),
            TileConfiguration(
                position = 3,
                tileType = TileType.CAMERA,
                customLabel = "Camera",
                priority = TilePriority.NORMAL
            ),
            TileConfiguration(
                position = 4,
                tileType = TileType.UNREAD_NOTIFICATIONS,
                customLabel = "Unread",
                priority = TilePriority.HIGH
            ),
            TileConfiguration(
                position = 5,
                tileType = TileType.SOS_EMERGENCY,
                customLabel = "Emergency",
                priority = TilePriority.EMERGENCY
            )
        )
    }
}
