package com.naviya.launcher.toggle

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ToggleMode enum
 * Tests mode properties, localization, and recommendations
 */
class ToggleModeTest {
    
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
    fun `comfort mode should have correct properties`() {
        val mode = ToggleMode.COMFORT
        
        assertEquals("Comfort", mode.displayName)
        assertEquals(6, mode.maxTiles)
        assertEquals(2, mode.gridColumns)
        assertEquals(3, mode.gridRows)
        assertTrue("Comfort mode should be elderly friendly", mode.isElderlyFriendly())
        assertEquals(1.6f, mode.getRecommendedFontScale(), 0.1f)
        assertTrue("Comfort mode should use high contrast", mode.shouldUseHighContrast())
    }
    
    @Test
    fun `family mode should have correct properties`() {
        val mode = ToggleMode.FAMILY
        
        assertEquals("Family", mode.displayName)
        assertEquals(9, mode.maxTiles)
        assertEquals(3, mode.gridColumns)
        assertEquals(3, mode.gridRows)
        assertTrue("Family mode should be elderly friendly", mode.isElderlyFriendly())
        assertEquals(1.4f, mode.getRecommendedFontScale(), 0.1f)
        assertFalse("Family mode should not use high contrast by default", mode.shouldUseHighContrast())
    }
    
    @Test
    fun `focus mode should have correct properties`() {
        val mode = ToggleMode.FOCUS
        
        assertEquals("Focus", mode.displayName)
        assertEquals(4, mode.maxTiles)
        assertEquals(2, mode.gridColumns)
        assertEquals(2, mode.gridRows)
        assertTrue("Focus mode should be elderly friendly", mode.isElderlyFriendly())
        assertEquals(1.8f, mode.getRecommendedFontScale(), 0.1f)
        assertTrue("Focus mode should use high contrast", mode.shouldUseHighContrast())
    }
    
    @Test
    fun `minimal mode should have correct properties`() {
        val mode = ToggleMode.MINIMAL
        
        assertEquals("Minimal", mode.displayName)
        assertEquals(3, mode.maxTiles)
        assertEquals(1, mode.gridColumns)
        assertEquals(3, mode.gridRows)
        assertTrue("Minimal mode should be elderly friendly", mode.isElderlyFriendly())
        assertEquals(2.0f, mode.getRecommendedFontScale(), 0.1f)
        assertTrue("Minimal mode should use high contrast", mode.shouldUseHighContrast())
    }
    
    @Test
    fun `welcome mode should have correct properties`() {
        val mode = ToggleMode.WELCOME
        
        assertEquals("Welcome", mode.displayName)
        assertEquals(6, mode.maxTiles)
        assertEquals(2, mode.gridColumns)
        assertEquals(3, mode.gridRows)
        assertFalse("Welcome mode should not be elderly friendly (temporary)", mode.isElderlyFriendly())
        assertEquals(1.6f, mode.getRecommendedFontScale(), 0.1f)
        assertTrue("Welcome mode should use high contrast", mode.shouldUseHighContrast())
    }
    
    @Test
    fun `localization should work for all supported languages`() {
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
    fun `german localization should be correct`() {
        assertEquals("Komfort", ToggleMode.COMFORT.getLocalizedName("de"))
        assertEquals("Familie", ToggleMode.FAMILY.getLocalizedName("de"))
        assertEquals("Fokus", ToggleMode.FOCUS.getLocalizedName("de"))
        assertEquals("Minimal", ToggleMode.MINIMAL.getLocalizedName("de"))
        assertEquals("Willkommen", ToggleMode.WELCOME.getLocalizedName("de"))
    }
    
    @Test
    fun `arabic localization should be correct`() {
        assertEquals("راحة", ToggleMode.COMFORT.getLocalizedName("ar"))
        assertEquals("عائلة", ToggleMode.FAMILY.getLocalizedName("ar"))
        assertEquals("تركيز", ToggleMode.FOCUS.getLocalizedName("ar"))
        assertEquals("الحد الأدنى", ToggleMode.MINIMAL.getLocalizedName("ar"))
        assertEquals("مرحبا", ToggleMode.WELCOME.getLocalizedName("ar"))
    }
    
    @Test
    fun `mode progression should work correctly`() {
        assertEquals(ToggleMode.COMFORT, ToggleMode.WELCOME.getNextMode())
        assertEquals(ToggleMode.FOCUS, ToggleMode.MINIMAL.getNextMode())
        assertEquals(ToggleMode.COMFORT, ToggleMode.FOCUS.getNextMode())
        assertEquals(ToggleMode.FAMILY, ToggleMode.COMFORT.getNextMode())
        assertNull("Family mode should have no next mode", ToggleMode.FAMILY.getNextMode())
    }
    
    @Test
    fun `mode regression should work correctly`() {
        assertEquals(ToggleMode.COMFORT, ToggleMode.FAMILY.getPreviousMode())
        assertEquals(ToggleMode.FOCUS, ToggleMode.COMFORT.getPreviousMode())
        assertEquals(ToggleMode.MINIMAL, ToggleMode.FOCUS.getPreviousMode())
        assertNull("Minimal mode should have no previous mode", ToggleMode.MINIMAL.getPreviousMode())
        assertNull("Welcome mode should have no previous mode", ToggleMode.WELCOME.getPreviousMode())
    }
    
    @Test
    fun `fromString should parse mode names correctly`() {
        assertEquals(ToggleMode.COMFORT, ToggleMode.fromString("COMFORT"))
        assertEquals(ToggleMode.COMFORT, ToggleMode.fromString("comfort"))
        assertEquals(ToggleMode.FAMILY, ToggleMode.fromString("Family"))
        assertNull("Invalid mode string should return null", ToggleMode.fromString("INVALID"))
    }
    
    @Test
    fun `getDefaultMode should return welcome`() {
        assertEquals(ToggleMode.WELCOME, ToggleMode.getDefaultMode())
    }
    
    @Test
    fun `getRecommendedMode should work for different user types`() {
        // New user should get welcome
        assertEquals(ToggleMode.WELCOME, ToggleMode.getRecommendedMode(
            isElderly = true,
            hasCognitiveImpairment = false,
            isNewUser = true,
            hasFamilySupport = false
        ))
        
        // Elderly with cognitive impairment should get minimal
        assertEquals(ToggleMode.MINIMAL, ToggleMode.getRecommendedMode(
            isElderly = true,
            hasCognitiveImpairment = true,
            isNewUser = false,
            hasFamilySupport = false
        ))
        
        // Non-elderly with cognitive impairment should get focus
        assertEquals(ToggleMode.FOCUS, ToggleMode.getRecommendedMode(
            isElderly = false,
            hasCognitiveImpairment = true,
            isNewUser = false,
            hasFamilySupport = false
        ))
        
        // Elderly with family support should get family
        assertEquals(ToggleMode.FAMILY, ToggleMode.getRecommendedMode(
            isElderly = true,
            hasCognitiveImpairment = false,
            isNewUser = false,
            hasFamilySupport = true
        ))
        
        // Elderly without family support should get comfort
        assertEquals(ToggleMode.COMFORT, ToggleMode.getRecommendedMode(
            isElderly = true,
            hasCognitiveImpairment = false,
            isNewUser = false,
            hasFamilySupport = false
        ))
    }
    
    @Test
    fun `getElderlyFriendlyModes should return correct modes`() {
        val elderlyModes = ToggleMode.getElderlyFriendlyModes()
        
        assertTrue("Should include COMFORT", elderlyModes.contains(ToggleMode.COMFORT))
        assertTrue("Should include FAMILY", elderlyModes.contains(ToggleMode.FAMILY))
        assertTrue("Should include FOCUS", elderlyModes.contains(ToggleMode.FOCUS))
        assertTrue("Should include MINIMAL", elderlyModes.contains(ToggleMode.MINIMAL))
        assertFalse("Should not include WELCOME", elderlyModes.contains(ToggleMode.WELCOME))
    }
    
    @Test
    fun `font scales should meet accessibility requirements`() {
        ToggleMode.values().forEach { mode ->
            val fontScale = mode.getRecommendedFontScale()
            
            if (mode != ToggleMode.FAMILY) {
                assertTrue("Mode $mode should have font scale >= 1.6x for elderly accessibility", 
                    fontScale >= 1.6f)
            } else {
                assertTrue("Family mode should have reasonable font scale", fontScale >= 1.4f)
            }
        }
    }
    
    @Test
    fun `icon scales should be appropriate`() {
        ToggleMode.values().forEach { mode ->
            val iconScale = mode.getRecommendedIconScale()
            
            assertTrue("Mode $mode should have positive icon scale", iconScale > 0f)
            assertTrue("Mode $mode should have reasonable icon scale", iconScale <= 2.0f)
        }
    }
    
    @Test
    fun `grid constraints should follow windsurf rules`() {
        ToggleMode.values().forEach { mode ->
            assertTrue("Mode $mode should not exceed 3 columns", mode.gridColumns <= 3)
            assertTrue("Mode $mode should not exceed 3 rows", mode.gridRows <= 3)
            assertTrue("Mode $mode should not exceed 9 total tiles", mode.maxTiles <= 9)
        }
    }
}
