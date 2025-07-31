package com.naviya.launcher

import org.junit.Test
import org.junit.Assert.*
import com.naviya.launcher.toggle.ToggleMode
import com.naviya.launcher.core.NaviyaConstants
import com.naviya.launcher.layout.SemanticTileLayout
import com.naviya.launcher.layout.TileType
import com.naviya.launcher.emergency.MedicalEmergencyType

/**
 * Core Functionality Validation Test
 * Validates that the remaining enabled modules work correctly after systematic disabling
 */
class CoreFunctionalityValidationTest {

    @Test
    fun `test 3-mode system configuration is correct`() {
        // Test ESSENTIAL mode
        assertEquals("Essential mode should have 3 tiles", 3, ToggleMode.ESSENTIAL.maxTiles)
        assertEquals("Essential mode should be 1x3 grid", 1, ToggleMode.ESSENTIAL.gridColumns)
        assertEquals("Essential mode should be 1x3 grid", 3, ToggleMode.ESSENTIAL.gridRows)
        
        // Test COMFORT mode
        assertEquals("Comfort mode should have 4 tiles", 4, ToggleMode.COMFORT.maxTiles)
        assertEquals("Comfort mode should be 2x2 grid", 2, ToggleMode.COMFORT.gridColumns)
        assertEquals("Comfort mode should be 2x2 grid", 2, ToggleMode.COMFORT.gridRows)
        
        // Test CONNECTED mode
        assertEquals("Connected mode should have 6 tiles", 6, ToggleMode.CONNECTED.maxTiles)
        assertEquals("Connected mode should be 2x3 grid", 2, ToggleMode.CONNECTED.gridColumns)
        assertEquals("Connected mode should be 2x3 grid", 3, ToggleMode.CONNECTED.gridRows)
    }

    @Test
    fun `test mode localization works`() {
        // Test English (default)
        assertEquals("Essential", ToggleMode.ESSENTIAL.getLocalizedName("en"))
        assertEquals("Comfort", ToggleMode.COMFORT.getLocalizedName("en"))
        assertEquals("Connected", ToggleMode.CONNECTED.getLocalizedName("en"))
        
        // Test German
        assertEquals("Wesentlich", ToggleMode.ESSENTIAL.getLocalizedName("de"))
        assertEquals("Komfort", ToggleMode.COMFORT.getLocalizedName("de"))
        assertEquals("Verbunden", ToggleMode.CONNECTED.getLocalizedName("de"))
        
        // Test Turkish
        assertEquals("Temel", ToggleMode.ESSENTIAL.getLocalizedName("tr"))
        assertEquals("Konfor", ToggleMode.COMFORT.getLocalizedName("tr"))
        assertEquals("Bağlı", ToggleMode.CONNECTED.getLocalizedName("tr"))
    }

    @Test
    fun `test elderly-friendly settings are correct`() {
        // All modes should be elderly-friendly
        assertTrue("Essential mode should be elderly-friendly", ToggleMode.ESSENTIAL.isElderlyFriendly())
        assertTrue("Comfort mode should be elderly-friendly", ToggleMode.COMFORT.isElderlyFriendly())
        assertTrue("Connected mode should be elderly-friendly", ToggleMode.CONNECTED.isElderlyFriendly())
        
        // Font scales should be appropriate for elderly users
        assertEquals("Essential should have largest font", 2.0f, ToggleMode.ESSENTIAL.getRecommendedFontScale(), 0.01f)
        assertEquals("Comfort should have standard elderly font", 1.6f, ToggleMode.COMFORT.getRecommendedFontScale(), 0.01f)
        assertEquals("Connected should have slightly smaller font", 1.4f, ToggleMode.CONNECTED.getRecommendedFontScale(), 0.01f)
        
        // High contrast settings
        assertTrue("Essential should use high contrast", ToggleMode.ESSENTIAL.shouldUseHighContrast())
        assertTrue("Comfort should use high contrast", ToggleMode.COMFORT.shouldUseHighContrast())
        assertFalse("Connected should not force high contrast", ToggleMode.CONNECTED.shouldUseHighContrast())
    }

    @Test
    fun `test mode progression logic`() {
        // Forward progression
        assertEquals("Essential should progress to Comfort", ToggleMode.COMFORT, ToggleMode.ESSENTIAL.getNextMode())
        assertEquals("Comfort should progress to Connected", ToggleMode.CONNECTED, ToggleMode.COMFORT.getNextMode())
        assertNull("Connected should have no next mode", ToggleMode.CONNECTED.getNextMode())
        
        // Backward regression
        assertNull("Essential should have no previous mode", ToggleMode.ESSENTIAL.getPreviousMode())
        assertEquals("Comfort should regress to Essential", ToggleMode.ESSENTIAL, ToggleMode.COMFORT.getPreviousMode())
        assertEquals("Connected should regress to Comfort", ToggleMode.COMFORT, ToggleMode.CONNECTED.getPreviousMode())
    }

    @Test
    fun `test constants are properly defined`() {
        // Mode constants should match enum values
        assertEquals(3, NaviyaConstants.Modes.ESSENTIAL_MAX_TILES)
        assertEquals(1, NaviyaConstants.Modes.ESSENTIAL_GRID_COLUMNS)
        assertEquals(3, NaviyaConstants.Modes.ESSENTIAL_GRID_ROWS)
        
        assertEquals(4, NaviyaConstants.Modes.COMFORT_MAX_TILES)
        assertEquals(2, NaviyaConstants.Modes.COMFORT_GRID_COLUMNS)
        assertEquals(2, NaviyaConstants.Modes.COMFORT_GRID_ROWS)
        
        assertEquals(6, NaviyaConstants.Modes.CONNECTED_MAX_TILES)
        assertEquals(2, NaviyaConstants.Modes.CONNECTED_GRID_COLUMNS)
        assertEquals(3, NaviyaConstants.Modes.CONNECTED_GRID_ROWS)
        
        // Elderly-friendly UI constants
        assertEquals(48, NaviyaConstants.UI.MIN_TOUCH_TARGET_DP)
        assertEquals(64, NaviyaConstants.UI.RECOMMENDED_TOUCH_TARGET_DP)
        assertEquals(1.6f, NaviyaConstants.UI.MIN_FONT_SCALE, 0.01f)
        assertEquals(2.0f, NaviyaConstants.UI.RECOMMENDED_FONT_SCALE, 0.01f)
        
        // Emergency constants
        assertEquals(3000L, NaviyaConstants.Emergency.SOS_BUTTON_HOLD_DURATION_MS)
        assertEquals(3, NaviyaConstants.Emergency.EMERGENCY_CONTACT_MAX_COUNT)
    }

    @Test
    fun `test semantic tile layouts are correct`() {
        // Test ESSENTIAL layout
        val essentialLayout = SemanticTileLayout.getLayoutForMode(ToggleMode.ESSENTIAL)
        assertEquals("Essential should have 3 slots", 3, essentialLayout.slots.size)
        assertEquals("First slot should be PHONE", TileType.PHONE, essentialLayout.slots[0].tileType)
        assertEquals("Second slot should be MESSAGES", TileType.MESSAGES, essentialLayout.slots[1].tileType)
        assertEquals("Third slot should be CONTACTS", TileType.CONTACTS, essentialLayout.slots[2].tileType)
        
        // Test COMFORT layout
        val comfortLayout = SemanticTileLayout.getLayoutForMode(ToggleMode.COMFORT)
        assertEquals("Comfort should have 4 slots", 4, comfortLayout.slots.size)
        assertEquals("First slot should be PHONE", TileType.PHONE, comfortLayout.slots[0].tileType)
        assertEquals("Second slot should be MESSAGES", TileType.MESSAGES, comfortLayout.slots[1].tileType)
        assertEquals("Third slot should be CAMERA", TileType.CAMERA, comfortLayout.slots[2].tileType)
        assertEquals("Fourth slot should be GALLERY", TileType.GALLERY, comfortLayout.slots[3].tileType)
        
        // Test CONNECTED layout
        val connectedLayout = SemanticTileLayout.getLayoutForMode(ToggleMode.CONNECTED)
        assertEquals("Connected should have 6 slots", 6, connectedLayout.slots.size)
        assertEquals("Fifth slot should be WEATHER", TileType.WEATHER, connectedLayout.slots[4].tileType)
        assertEquals("Sixth slot should be FAMILY_COMMUNICATION", TileType.FAMILY_COMMUNICATION, connectedLayout.slots[5].tileType)
    }

    @Test
    fun `test tile types have correct properties`() {
        // Test PHONE tile type
        assertEquals("Phone", TileType.PHONE.displayName)
        assertTrue("Phone should have call permission", TileType.PHONE.requiredPermissions.contains("android.permission.CALL_PHONE"))
        
        // Test MESSAGES tile type
        assertEquals("Messages", TileType.MESSAGES.displayName)
        assertTrue("Messages should have SMS permission", TileType.MESSAGES.requiredPermissions.contains("android.permission.SEND_SMS"))
        
        // Test CAMERA tile type
        assertEquals("Camera", TileType.CAMERA.displayName)
        assertTrue("Camera should have camera permission", TileType.CAMERA.requiredPermissions.contains("android.permission.CAMERA"))
    }

    @Test
    fun `test medical emergency types are defined`() {
        // Test critical emergencies
        assertEquals("Cardiac Event", MedicalEmergencyType.CARDIAC_EVENT.displayName)
        assertEquals(1, MedicalEmergencyType.CARDIAC_EVENT.priority)
        
        assertEquals("Fall with Injury", MedicalEmergencyType.FALL_WITH_INJURY.displayName)
        assertEquals(2, MedicalEmergencyType.FALL_WITH_INJURY.priority)
        
        assertEquals("Medication Emergency", MedicalEmergencyType.MEDICATION_EMERGENCY.displayName)
        assertEquals(1, MedicalEmergencyType.MEDICATION_EMERGENCY.priority)
        
        // Test all emergency types have required properties
        for (emergencyType in MedicalEmergencyType.values()) {
            assertNotNull("Emergency type should have display name", emergencyType.displayName)
            assertNotNull("Emergency type should have icon resource", emergencyType.iconResource)
            assertNotNull("Emergency type should have description", emergencyType.description)
            assertTrue("Priority should be 1 or 2", emergencyType.priority in 1..2)
        }
    }

    @Test
    fun `test mode recommendation logic`() {
        // Test recommendations based on user characteristics
        assertEquals("Cognitive impairment should get ESSENTIAL", 
            ToggleMode.ESSENTIAL, 
            ToggleMode.getRecommendedMode(isElderly = true, hasCognitiveImpairment = true, isNewUser = true, hasFamilySupport = false))
        
        assertEquals("Elderly without family support should get COMFORT", 
            ToggleMode.COMFORT, 
            ToggleMode.getRecommendedMode(isElderly = true, hasCognitiveImpairment = false, isNewUser = false, hasFamilySupport = false))
        
        assertEquals("Elderly with family support should get CONNECTED", 
            ToggleMode.CONNECTED, 
            ToggleMode.getRecommendedMode(isElderly = true, hasCognitiveImpairment = false, isNewUser = false, hasFamilySupport = true))
        
        assertEquals("Default should be COMFORT", 
            ToggleMode.COMFORT, 
            ToggleMode.getRecommendedMode(isElderly = false, hasCognitiveImpairment = false, isNewUser = false, hasFamilySupport = false))
    }

    @Test
    fun `test elderly-friendly modes list`() {
        val elderlyModes = ToggleMode.getElderlyFriendlyModes()
        assertEquals("Should have 3 elderly-friendly modes", 3, elderlyModes.size)
        assertTrue("Should contain ESSENTIAL", elderlyModes.contains(ToggleMode.ESSENTIAL))
        assertTrue("Should contain COMFORT", elderlyModes.contains(ToggleMode.COMFORT))
        assertTrue("Should contain CONNECTED", elderlyModes.contains(ToggleMode.CONNECTED))
    }

    @Test
    fun `test string parsing works`() {
        assertEquals("Should parse ESSENTIAL", ToggleMode.ESSENTIAL, ToggleMode.fromString("ESSENTIAL"))
        assertEquals("Should parse COMFORT", ToggleMode.COMFORT, ToggleMode.fromString("COMFORT"))
        assertEquals("Should parse CONNECTED", ToggleMode.CONNECTED, ToggleMode.fromString("CONNECTED"))
        assertEquals("Should parse case-insensitive", ToggleMode.ESSENTIAL, ToggleMode.fromString("essential"))
        assertNull("Should return null for invalid string", ToggleMode.fromString("INVALID"))
    }

    @Test
    fun `test default mode is appropriate`() {
        assertEquals("Default mode should be COMFORT", ToggleMode.COMFORT, ToggleMode.getDefaultMode())
    }
}
