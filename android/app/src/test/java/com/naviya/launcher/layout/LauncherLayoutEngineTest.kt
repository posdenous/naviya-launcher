package com.naviya.launcher.layout

import android.content.Context
import com.naviya.launcher.core.NaviyaConstants
import com.naviya.launcher.layout.data.LayoutConfiguration
import com.naviya.launcher.testing.NaviyaTestConfig
import com.naviya.launcher.toggle.ToggleMode
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for LauncherLayoutEngine - 3-mode system
 * Tests layout generation, Windsurf rules compliance, and accessibility features
 * Uses standardized NaviyaTestConfig for consistent testing patterns
 */
class LauncherLayoutEngineTest : NaviyaTestConfig() {
    
    private lateinit var layoutEngine: LauncherLayoutEngine
    private lateinit var mockContext: Context
    
    // Test screen dimensions
    private val testScreenWidth = 1080
    private val testScreenHeight = 1920
    private val testDensity = 3.0f
    
    override fun setup() {
        super.setup()
        mockContext = createMockContext()
        
        // Mock display metrics
        val mockResources = mockk<android.content.res.Resources>(relaxed = true)
        val mockDisplayMetrics = mockk<android.util.DisplayMetrics>(relaxed = true)
        
        every { mockContext.resources } returns mockResources
        every { mockResources.displayMetrics } returns mockDisplayMetrics
        every { mockDisplayMetrics.density } returns testDensity
        
        layoutEngine = LauncherLayoutEngine(mockContext)
    }
    
    @Test
    fun `generateLayoutForMode should create valid comfort layout`() = runTest {
        // Given
        val availableApps = listOf("Phone", "Messages", "Camera", "Settings", "Emergency", "Family")
        
        // When
        val layout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.COMFORT,
            screenWidth = testScreenWidth,
            screenHeight = testScreenHeight,
            availableApps = availableApps
        )
        
        // Then
        assertEquals("Layout should be for COMFORT mode", ToggleMode.COMFORT, layout.mode)
        assertEquals("Comfort mode should have 2 columns", 2, layout.gridColumns)
        assertEquals("Comfort mode should have 3 rows", 3, layout.gridRows)
        assertTrue("Comfort mode should have 6 or fewer tiles", layout.tiles.size <= 6)
        assertTrue("Font scale should be at least 1.6x", layout.fontScale >= 1.6f)
        assertTrue("Should have emergency access", layout.hasEmergencyAccess)
        assertTrue("Should be accessibility optimized", layout.isAccessibilityOptimized)
    }
    
    @Test
    fun `generateLayoutForMode should create valid family layout`() = runTest {
        // Given
        val availableApps = listOf("Phone", "Messages", "Camera", "Gallery", "Video Call", "Family Chat", "Emergency", "Settings", "Weather")
        
        // When
        val layout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.FAMILY,
            screenWidth = testScreenWidth,
            screenHeight = testScreenHeight,
            availableApps = availableApps
        )
        
        // Then
        assertEquals("Layout should be for FAMILY mode", ToggleMode.FAMILY, layout.mode)
        assertEquals("Family mode should have 3 columns", 3, layout.gridColumns)
        assertEquals("Family mode should have 3 rows", 3, layout.gridRows)
        assertTrue("Family mode should have 9 or fewer tiles", layout.tiles.size <= 9)
        assertTrue("Font scale should be appropriate", layout.fontScale >= 1.4f)
    }
    
    @Test
    fun `generateLayoutForMode should create valid focus layout`() = runTest {
        // Given
        val availableApps = listOf("Phone", "Emergency", "Messages", "Settings")
        
        // When
        val layout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.FOCUS,
            screenWidth = testScreenWidth,
            screenHeight = testScreenHeight,
            availableApps = availableApps
        )
        
        // Then
        assertEquals("Layout should be for FOCUS mode", ToggleMode.FOCUS, layout.mode)
        assertEquals("Focus mode should have 2 columns", 2, layout.gridColumns)
        assertEquals("Focus mode should have 2 rows", 2, layout.gridRows)
        assertTrue("Focus mode should have 4 or fewer tiles", layout.tiles.size <= 4)
        assertTrue("Font scale should be larger for focus", layout.fontScale >= 1.8f)
        assertTrue("Should use high contrast", layout.tiles.all { it.hasHighContrast })
    }
    
    @Test
    fun `generateLayoutForMode should create valid minimal layout`() = runTest {
        // Given
        val availableApps = listOf("Phone", "Emergency", "Messages")
        
        // When
        val layout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.MINIMAL,
            screenWidth = testScreenWidth,
            screenHeight = testScreenHeight,
            availableApps = availableApps
        )
        
        // Then
        assertEquals("Layout should be for MINIMAL mode", ToggleMode.MINIMAL, layout.mode)
        assertEquals("Minimal mode should have 1 column", 1, layout.gridColumns)
        assertEquals("Minimal mode should have 3 rows", 3, layout.gridRows)
        assertTrue("Minimal mode should have 3 or fewer tiles", layout.tiles.size <= 3)
        assertTrue("Font scale should be maximum for minimal", layout.fontScale >= 2.0f)
        assertTrue("Should use high contrast", layout.tiles.all { it.hasHighContrast })
    }
    
    @Test
    fun `generateLayoutForMode should create valid welcome layout`() = runTest {
        // Given
        val availableApps = listOf("Setup", "Tutorial", "Phone", "Emergency", "Help", "Settings")
        
        // When
        val layout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.WELCOME,
            screenWidth = testScreenWidth,
            screenHeight = testScreenHeight,
            availableApps = availableApps
        )
        
        // Then
        assertEquals("Layout should be for WELCOME mode", ToggleMode.WELCOME, layout.mode)
        assertEquals("Welcome mode should have 2 columns", 2, layout.gridColumns)
        assertEquals("Welcome mode should have 3 rows", 3, layout.gridRows)
        assertTrue("Welcome mode should have 6 or fewer tiles", layout.tiles.size <= 6)
        assertTrue("Font scale should be elderly-friendly", layout.fontScale >= 1.6f)
    }
    
    @Test
    fun `all layouts should meet minimum tile size requirements`() = runTest {
        val modes = ToggleMode.values()
        val minTileSizePx = (64 * testDensity).toInt() // 64dp minimum
        
        for (mode in modes) {
            val availableApps = listOf("Phone", "Emergency", "Messages", "Settings", "Camera", "Gallery")
            
            val layout = layoutEngine.generateLayoutForMode(
                mode = mode,
                screenWidth = testScreenWidth,
                screenHeight = testScreenHeight,
                availableApps = availableApps
            )
            
            layout.tiles.forEach { tile ->
                assertTrue(
                    "Tile ${tile.id} in $mode should meet minimum width requirement",
                    tile.size.width >= minTileSizePx
                )
                assertTrue(
                    "Tile ${tile.id} in $mode should meet minimum height requirement", 
                    tile.size.height >= minTileSizePx
                )
            }
        }
    }
    
    @Test
    fun `all layouts should respect cognitive load limits`() = runTest {
        val modes = ToggleMode.values()
        
        for (mode in modes) {
            val availableApps = listOf("Phone", "Emergency", "Messages", "Settings", "Camera", "Gallery", "Weather", "Calculator", "Clock")
            
            val layout = layoutEngine.generateLayoutForMode(
                mode = mode,
                screenWidth = testScreenWidth,
                screenHeight = testScreenHeight,
                availableApps = availableApps
            )
            
            assertTrue(
                "Layout for $mode should not exceed maximum tiles (${mode.maxTiles})",
                layout.tiles.size <= mode.maxTiles
            )
            
            assertTrue(
                "Layout for $mode should not exceed 9 tiles total",
                layout.tiles.size <= 9
            )
        }
    }
    
    @Test
    fun `all tiles should have accessibility properties`() = runTest {
        val layout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.COMFORT,
            screenWidth = testScreenWidth,
            screenHeight = testScreenHeight,
            availableApps = listOf("Phone", "Emergency", "Messages")
        )
        
        layout.tiles.forEach { tile ->
            assertTrue("Tile ${tile.id} should be accessible", tile.isAccessible)
            assertTrue("Tile ${tile.id} should have large text", tile.hasLargeText)
            assertNotNull("Tile ${tile.id} should have an app name", tile.appName)
            assertTrue("Tile ${tile.id} should have valid position", 
                tile.position.row >= 0 && tile.position.column >= 0)
            assertTrue("Tile ${tile.id} should have positive priority", tile.priority > 0)
        }
    }
    
    @Test
    fun `isAccessibilityCompliant should validate layouts correctly`() = runTest {
        // Test compliant layout
        val compliantLayout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.COMFORT,
            screenWidth = testScreenWidth,
            screenHeight = testScreenHeight,
            availableApps = listOf("Phone", "Emergency")
        )
        
        assertTrue("Comfort layout should be accessibility compliant", 
            layoutEngine.isAccessibilityCompliant(compliantLayout))
        
        // Test non-compliant layout (would need to create manually)
        val nonCompliantLayout = compliantLayout.copy(
            fontScale = 1.0f, // Below 1.6x minimum
            tiles = (1..15).map { // Too many tiles
                compliantLayout.tiles.first().copy(id = "tile_$it")
            }
        )
        
        assertFalse("Layout with too small font and too many tiles should not be compliant",
            layoutEngine.isAccessibilityCompliant(nonCompliantLayout))
    }
    
    @Test
    fun `updateTilePosition should work correctly`() = runTest {
        // Given
        val layout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.COMFORT,
            screenWidth = testScreenWidth,
            screenHeight = testScreenHeight,
            availableApps = listOf("Phone", "Emergency")
        )
        
        val firstTile = layout.tiles.first()
        val newPosition = com.naviya.launcher.layout.data.TilePosition(1, 1)
        
        // When
        val success = layoutEngine.updateTilePosition(firstTile.id, newPosition)
        
        // Then
        assertTrue("Tile position update should succeed", success)
        
        val updatedLayout = layoutEngine.getCurrentLayout()
        assertNotNull("Updated layout should be available", updatedLayout)
        
        val updatedTile = updatedLayout!!.tiles.find { it.id == firstTile.id }
        assertNotNull("Updated tile should be found", updatedTile)
        assertEquals("Tile position should be updated", newPosition, updatedTile!!.position)
    }
    
    @Test
    fun `layout generation should handle empty app list gracefully`() = runTest {
        // When
        val layout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.MINIMAL,
            screenWidth = testScreenWidth,
            screenHeight = testScreenHeight,
            availableApps = emptyList()
        )
        
        // Then
        assertNotNull("Layout should be generated even with empty app list", layout)
        assertTrue("Layout should have some default tiles", layout.tiles.isNotEmpty())
    }
    
    @Test
    fun `layout generation should handle small screen sizes`() = runTest {
        // Given small screen
        val smallWidth = 480
        val smallHeight = 800
        
        // When
        val layout = layoutEngine.generateLayoutForMode(
            mode = ToggleMode.COMFORT,
            screenWidth = smallWidth,
            screenHeight = smallHeight,
            availableApps = listOf("Phone", "Emergency")
        )
        
        // Then
        assertNotNull("Layout should be generated for small screens", layout)
        assertTrue("Tiles should still meet minimum size requirements",
            layout.tiles.all { it.size.width >= (64 * testDensity).toInt() })
    }
}
