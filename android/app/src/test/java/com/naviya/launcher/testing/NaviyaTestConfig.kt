package com.naviya.launcher.testing

import com.naviya.launcher.core.NaviyaConstants
import com.naviya.launcher.toggle.ToggleMode
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.After
import org.junit.Before

/**
 * Standardized test configuration for Naviya 3-Mode Launcher
 * Ensures consistent testing patterns across all test classes
 * Uses MockK as the standard mocking framework
 */
abstract class NaviyaTestConfig {
    
    @Before
    open fun setUp() {
        MockKAnnotations.init(this)
        setupTestEnvironment()
    }
    
    @After
    open fun tearDown() {
        clearAllMocks()
        cleanupTestEnvironment()
    }
    
    private fun setupTestEnvironment() {
        // Set test-specific configurations
        System.setProperty("naviya.test.mode", "true")
        System.setProperty("naviya.test.timeout", NaviyaConstants.Testing.TEST_TIMEOUT_MS.toString())
    }
    
    private fun cleanupTestEnvironment() {
        // Clean up test environment
        System.clearProperty("naviya.test.mode")
        System.clearProperty("naviya.test.timeout")
    }
    
    // Standard test data for 3-mode system
    companion object TestData {
        val TEST_MODES = listOf(
            ToggleMode.ESSENTIAL,
            ToggleMode.COMFORT,
            ToggleMode.CONNECTED
        )
        
        const val TEST_USER_ID = NaviyaConstants.Testing.TEST_USER_ID
        const val TEST_CAREGIVER_ID = NaviyaConstants.Testing.TEST_CAREGIVER_ID
        
        // Standard test assertions for elderly-friendly UI
        fun assertElderlyFriendlyTiming(actualTimeMs: Long, maxAllowedMs: Long, operation: String) {
            assert(actualTimeMs <= maxAllowedMs) {
                "$operation took ${actualTimeMs}ms, exceeding elderly-friendly limit of ${maxAllowedMs}ms"
            }
        }
        
        fun assertAccessibilityCompliance(touchTargetDp: Int, fontScale: Float, operation: String) {
            assert(touchTargetDp >= NaviyaConstants.UI.MIN_TOUCH_TARGET_DP) {
                "$operation touch target ${touchTargetDp}dp below minimum ${NaviyaConstants.UI.MIN_TOUCH_TARGET_DP}dp"
            }
            
            assert(fontScale >= NaviyaConstants.UI.MIN_FONT_SCALE) {
                "$operation font scale ${fontScale} below minimum ${NaviyaConstants.UI.MIN_FONT_SCALE}"
            }
        }
        
        fun assertModeGridConstraints(mode: ToggleMode) {
            when (mode) {
                ToggleMode.ESSENTIAL -> {
                    assert(mode.maxTiles == NaviyaConstants.Modes.ESSENTIAL_MAX_TILES)
                    assert(mode.gridColumns == NaviyaConstants.Modes.ESSENTIAL_GRID_COLUMNS)
                    assert(mode.gridRows == NaviyaConstants.Modes.ESSENTIAL_GRID_ROWS)
                }
                ToggleMode.COMFORT -> {
                    assert(mode.maxTiles == NaviyaConstants.Modes.COMFORT_MAX_TILES)
                    assert(mode.gridColumns == NaviyaConstants.Modes.COMFORT_GRID_COLUMNS)
                    assert(mode.gridRows == NaviyaConstants.Modes.COMFORT_GRID_ROWS)
                }
                ToggleMode.CONNECTED -> {
                    assert(mode.maxTiles == NaviyaConstants.Modes.CONNECTED_MAX_TILES)
                    assert(mode.gridColumns == NaviyaConstants.Modes.CONNECTED_GRID_COLUMNS)
                    assert(mode.gridRows == NaviyaConstants.Modes.CONNECTED_GRID_ROWS)
                }
            }
        }
    }
}
