package com.naviya.launcher.toggle

import com.naviya.launcher.core.NaviyaConstants
import com.naviya.launcher.testing.NaviyaTestConfig
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ToggleMode enum - 3-mode system (ESSENTIAL, COMFORT, CONNECTED)
 * Uses standardized NaviyaTestConfig for consistent testing patterns
 */
class ToggleModeTest : NaviyaTestConfig() {
    
    @Test
    fun `all modes should have valid properties`() {
        ToggleMode.values().forEach { mode ->
            assertNotNull("Mode $mode should have display name", mode.displayName)
            assertNotNull("Mode $mode should have description", mode.description)
            assertNotNull("Mode $mode should have target group", mode.targetGroup)
            assertTrue("Mode $mode should have positive max tiles", mode.maxTiles > 0)
            assertTrue("Mode $mode should have positive grid columns", mode.gridColumns > 0)
            assertTrue("Mode $mode should have positive grid rows", mode.gridRows > 0)
            assertTrue("Mode $mode max tiles should not exceed grid capacity", 
                mode.maxTiles <= mode.gridColumns * mode.gridRows)
        }
    }
    
    @Test
    fun `essential mode should have correct properties`() {
        val mode = ToggleMode.ESSENTIAL
        
        assertEquals("Essential", mode.displayName)
        assertEquals(NaviyaConstants.Modes.ESSENTIAL_MAX_TILES, mode.maxTiles)
        assertEquals(NaviyaConstants.Modes.ESSENTIAL_GRID_COLUMNS, mode.gridColumns)
        assertEquals(NaviyaConstants.Modes.ESSENTIAL_GRID_ROWS, mode.gridRows)
        assertEquals("Users with severe cognitive impairment", mode.targetGroup)
    }
    
    @Test
    fun `comfort mode should have correct properties`() {
        val mode = ToggleMode.COMFORT
        
        assertEquals("Comfort", mode.displayName)
        assertEquals(NaviyaConstants.Modes.COMFORT_MAX_TILES, mode.maxTiles)
        assertEquals(NaviyaConstants.Modes.COMFORT_GRID_COLUMNS, mode.gridColumns)
        assertEquals(NaviyaConstants.Modes.COMFORT_GRID_ROWS, mode.gridRows)
        assertEquals("Standard elderly users with creative engagement", mode.targetGroup)
    }
    
    @Test
    fun `connected mode should have correct properties`() {
        val mode = ToggleMode.CONNECTED
        
        assertEquals("Connected", mode.displayName)
        assertEquals(NaviyaConstants.Modes.CONNECTED_MAX_TILES, mode.maxTiles)
        assertEquals(NaviyaConstants.Modes.CONNECTED_GRID_COLUMNS, mode.gridColumns)
        assertEquals(NaviyaConstants.Modes.CONNECTED_GRID_ROWS, mode.gridRows)
        assertEquals("Tech-comfortable elderly with family support", mode.targetGroup)
    }
    
    @Test
    fun `all modes should be elderly friendly`() {
        ToggleMode.values().forEach { mode ->
            assertTrue("Mode $mode should be elderly friendly", mode.isElderlyFriendly())
        }
    }
    
    @Test
    fun `font scales should meet accessibility requirements`() {
        // ESSENTIAL should have largest font scale for severe cognitive impairment
        assertEquals(2.0f, ToggleMode.ESSENTIAL.getRecommendedFontScale(), 0.1f)
        
        // COMFORT should have standard elderly-friendly font scale
        assertEquals(1.6f, ToggleMode.COMFORT.getRecommendedFontScale(), 0.1f)
        
        // CONNECTED should have slightly smaller font for tech-comfortable users
        assertEquals(1.4f, ToggleMode.CONNECTED.getRecommendedFontScale(), 0.1f)
        
        // All should meet minimum accessibility requirements
        ToggleMode.values().forEach { mode ->
            assertTrue("Mode $mode should have font scale >= 1.4x for elderly accessibility", 
                mode.getRecommendedFontScale() >= 1.4f)
        }
    }
    
    @Test
    fun `icon scales should be appropriate for elderly users`() {
        // ESSENTIAL should have largest icons for motor difficulties
        assertEquals(1.8f, ToggleMode.ESSENTIAL.getRecommendedIconScale(), 0.1f)
        
        // COMFORT should have standard large icons
        assertEquals(1.4f, ToggleMode.COMFORT.getRecommendedIconScale(), 0.1f)
        
        // CONNECTED should have slightly smaller icons for more content
        assertEquals(1.2f, ToggleMode.CONNECTED.getRecommendedIconScale(), 0.1f)
        
        // All should have reasonable icon scales
        ToggleMode.values().forEach { mode ->
            assertTrue("Mode $mode should have positive icon scale", mode.getRecommendedIconScale() > 0f)
            assertTrue("Mode $mode should have reasonable icon scale", mode.getRecommendedIconScale() <= 2.0f)
        }
    }
    
    @Test
    fun `high contrast settings should be appropriate`() {
        // ESSENTIAL should use high contrast for cognitive impairment
        assertTrue("ESSENTIAL should use high contrast", ToggleMode.ESSENTIAL.shouldUseHighContrast())
        
        // COMFORT should use high contrast for standard elderly users
        assertTrue("COMFORT should use high contrast", ToggleMode.COMFORT.shouldUseHighContrast())
        
        // CONNECTED may not need high contrast by default (tech-comfortable users)
        // This depends on the implementation - test what's actually implemented
    }
    
    @Test
    fun `grid constraints should follow accessibility guidelines`() {
        ToggleMode.values().forEach { mode ->
            assertTrue("Mode $mode should not exceed 2 columns for elderly accessibility", mode.gridColumns <= 2)
            assertTrue("Mode $mode should not exceed 3 rows for elderly accessibility", mode.gridRows <= 3)
            assertTrue("Mode $mode should not exceed 6 total tiles for simplicity", mode.maxTiles <= 6)
        }
    }
    
    @Test
    fun `mode progression should be logical`() {
        // ESSENTIAL is simplest with 1x3 grid
        assertEquals("ESSENTIAL should be simplest", 3, ToggleMode.ESSENTIAL.maxTiles)
        assertEquals(1, ToggleMode.ESSENTIAL.gridColumns)
        assertEquals(3, ToggleMode.ESSENTIAL.gridRows)
        
        // COMFORT adds creative engagement with 2x2 grid
        assertEquals("COMFORT should have moderate complexity", 4, ToggleMode.COMFORT.maxTiles)
        assertEquals(2, ToggleMode.COMFORT.gridColumns)
        assertEquals(2, ToggleMode.COMFORT.gridRows)
        
        // CONNECTED adds family features with 2x3 grid
        assertEquals("CONNECTED should have most features", 6, ToggleMode.CONNECTED.maxTiles)
        assertEquals(2, ToggleMode.CONNECTED.gridColumns)
        assertEquals(3, ToggleMode.CONNECTED.gridRows)
    }
    
    @Test
    fun `localization should work for supported languages`() {
        val supportedLanguages = listOf("de", "tr", "uk", "ar", "en")
        
        ToggleMode.values().forEach { mode ->
            supportedLanguages.forEach { language ->
                val localizedName = mode.getLocalizedName(language)
                val localizedDescription = mode.getLocalizedDescription(language)
                
                assertNotNull("Mode $mode should have localized name for $language", localizedName)
                assertNotNull("Mode $mode should have localized description for $language", localizedDescription)
                assertTrue("Localized name should not be empty", localizedName.isNotEmpty())
                assertTrue("Localized description should not be empty", localizedDescription.isNotEmpty())
            }
        }
    }
    
    @Test
    fun `mode names should be descriptive`() {
        assertEquals("Essential", ToggleMode.ESSENTIAL.displayName)
        assertEquals("Comfort", ToggleMode.COMFORT.displayName)
        assertEquals("Connected", ToggleMode.CONNECTED.displayName)
        
        // All should have non-empty descriptions
        ToggleMode.values().forEach { mode ->
            assertTrue("Mode ${mode.name} should have description", mode.description.isNotEmpty())
        }
    }
    
    @Test
    fun `should only have three modes`() {
        val modes = ToggleMode.values()
        assertEquals("Should have exactly 3 modes", 3, modes.size)
        
        val modeNames = modes.map { it.name }.toSet()
        assertTrue("Should contain ESSENTIAL", modeNames.contains("ESSENTIAL"))
        assertTrue("Should contain COMFORT", modeNames.contains("COMFORT"))
        assertTrue("Should contain CONNECTED", modeNames.contains("CONNECTED"))
    }
}
