package com.naviya.launcher.layout

import android.content.Context
import android.util.Log
import com.naviya.launcher.layout.data.LayoutConfiguration
import com.naviya.launcher.layout.data.TileConfiguration
import com.naviya.launcher.layout.data.TilePosition
import com.naviya.launcher.layout.data.TileSize
import com.naviya.launcher.toggle.ToggleMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Launcher Layout Engine for Naviya Launcher
 * Manages dynamic tile layouts with elderly-friendly design principles
 * Follows Windsurf rules for grid limits, accessibility, and cognitive load management
 */
@Singleton
class LauncherLayoutEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "LauncherLayoutEngine"
        
        // Windsurf rules: Grid layout constraints for elderly users
        private const val MAX_COLUMNS = 3 // Maximum 3 columns for simplicity
        private const val MAX_ROWS = 3 // Maximum 3 rows for cognitive load
        private const val MAX_TILES_PER_SCREEN = 9 // 6-9 tiles optimal for elderly users
        private const val MIN_TILE_SIZE_DP = 64 // Minimum 64dp for elderly accessibility
        private const val MIN_PADDING_DP = 16 // Minimum 16dp padding between tiles
    }
    
    private val _currentLayout = MutableStateFlow<LayoutConfiguration?>(null)
    val currentLayout: Flow<LayoutConfiguration?> = _currentLayout.asStateFlow()
    
    private val _isLayoutLoading = MutableStateFlow(false)
    val isLayoutLoading: Flow<Boolean> = _isLayoutLoading.asStateFlow()
    
    /**
     * Generate layout configuration for a specific toggle mode
     * Follows Windsurf accessibility and cognitive load rules
     */
    suspend fun generateLayoutForMode(
        mode: ToggleMode,
        screenWidth: Int,
        screenHeight: Int,
        availableApps: List<String>
    ): LayoutConfiguration {
        _isLayoutLoading.value = true
        
        try {
            Log.i(TAG, "Generating layout for mode: $mode")
            
            val layoutConfig = when (mode) {
                ToggleMode.COMFORT -> generateComfortLayout(screenWidth, screenHeight, availableApps)
                ToggleMode.FAMILY -> generateFamilyLayout(screenWidth, screenHeight, availableApps)
                ToggleMode.FOCUS -> generateFocusLayout(screenWidth, screenHeight, availableApps)
                ToggleMode.MINIMAL -> generateMinimalLayout(screenWidth, screenHeight, availableApps)
                ToggleMode.WELCOME -> generateWelcomeLayout(screenWidth, screenHeight, availableApps)
            }
            
            // Validate layout against Windsurf rules
            val validatedLayout = validateAndOptimizeLayout(layoutConfig)
            
            _currentLayout.value = validatedLayout
            
            Log.i(TAG, "Layout generated successfully for $mode: ${validatedLayout.tiles.size} tiles")
            return validatedLayout
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate layout for mode $mode", e)
            throw LayoutGenerationException("Failed to generate layout: ${e.message}", e)
        } finally {
            _isLayoutLoading.value = false
        }
    }
    
    /**
     * Generate Comfort Mode layout (2x3 grid, large tiles, essential apps)
     */
    private fun generateComfortLayout(
        screenWidth: Int,
        screenHeight: Int,
        availableApps: List<String>
    ): LayoutConfiguration {
        val tileSize = calculateOptimalTileSize(screenWidth, screenHeight, 2, 3)
        val tiles = mutableListOf<TileConfiguration>()
        
        // Essential apps for comfort mode (elderly-friendly)
        val essentialApps = listOf("Phone", "Messages", "Camera", "Settings", "Emergency", "Family")
        val appsToShow = essentialApps.take(6) // 2x3 = 6 tiles max
        
        var row = 0
        var col = 0
        
        appsToShow.forEachIndexed { index, appName ->
            tiles.add(
                TileConfiguration(
                    id = "comfort_tile_$index",
                    appName = appName,
                    position = TilePosition(row, col),
                    size = tileSize,
                    priority = index + 1,
                    isAccessible = true,
                    hasLargeText = true,
                    hasHighContrast = true
                )
            )
            
            col++
            if (col >= 2) {
                col = 0
                row++
            }
        }
        
        return LayoutConfiguration(
            mode = ToggleMode.COMFORT,
            gridColumns = 2,
            gridRows = 3,
            tiles = tiles,
            backgroundColor = "#F5F5F5", // Light gray for comfort
            fontScale = 1.6f, // Windsurf rule: 1.6x font scale minimum
            iconScale = 1.4f,
            paddingDp = 20,
            hasEmergencyAccess = true,
            isAccessibilityOptimized = true
        )
    }
    
    /**
     * Generate Family Mode layout (3x3 grid, medium tiles, family apps)
     */
    private fun generateFamilyLayout(
        screenWidth: Int,
        screenHeight: Int,
        availableApps: List<String>
    ): LayoutConfiguration {
        val tileSize = calculateOptimalTileSize(screenWidth, screenHeight, 3, 3)
        val tiles = mutableListOf<TileConfiguration>()
        
        // Family-oriented apps
        val familyApps = listOf(
            "Phone", "Messages", "Camera", "Gallery", "Video Call", 
            "Family Chat", "Emergency", "Settings", "Weather"
        )
        
        var row = 0
        var col = 0
        
        familyApps.take(9).forEachIndexed { index, appName ->
            tiles.add(
                TileConfiguration(
                    id = "family_tile_$index",
                    appName = appName,
                    position = TilePosition(row, col),
                    size = tileSize,
                    priority = index + 1,
                    isAccessible = true,
                    hasLargeText = true,
                    hasHighContrast = false // Normal contrast for family mode
                )
            )
            
            col++
            if (col >= 3) {
                col = 0
                row++
            }
        }
        
        return LayoutConfiguration(
            mode = ToggleMode.FAMILY,
            gridColumns = 3,
            gridRows = 3,
            tiles = tiles,
            backgroundColor = "#E8F5E8", // Light green for family
            fontScale = 1.4f,
            iconScale = 1.2f,
            paddingDp = 16,
            hasEmergencyAccess = true,
            isAccessibilityOptimized = true
        )
    }
    
    /**
     * Generate Focus Mode layout (2x2 grid, large tiles, minimal apps)
     */
    private fun generateFocusLayout(
        screenWidth: Int,
        screenHeight: Int,
        availableApps: List<String>
    ): LayoutConfiguration {
        val tileSize = calculateOptimalTileSize(screenWidth, screenHeight, 2, 2)
        val tiles = mutableListOf<TileConfiguration>()
        
        // Minimal essential apps for focus
        val focusApps = listOf("Phone", "Emergency", "Messages", "Settings")
        
        var row = 0
        var col = 0
        
        focusApps.forEachIndexed { index, appName ->
            tiles.add(
                TileConfiguration(
                    id = "focus_tile_$index",
                    appName = appName,
                    position = TilePosition(row, col),
                    size = tileSize,
                    priority = index + 1,
                    isAccessible = true,
                    hasLargeText = true,
                    hasHighContrast = true
                )
            )
            
            col++
            if (col >= 2) {
                col = 0
                row++
            }
        }
        
        return LayoutConfiguration(
            mode = ToggleMode.FOCUS,
            gridColumns = 2,
            gridRows = 2,
            tiles = tiles,
            backgroundColor = "#E3F2FD", // Light blue for focus
            fontScale = 1.8f, // Larger text for focus mode
            iconScale = 1.6f,
            paddingDp = 24,
            hasEmergencyAccess = true,
            isAccessibilityOptimized = true
        )
    }
    
    /**
     * Generate Minimal Mode layout (1x3 grid, very large tiles, essential only)
     */
    private fun generateMinimalLayout(
        screenWidth: Int,
        screenHeight: Int,
        availableApps: List<String>
    ): LayoutConfiguration {
        val tileSize = calculateOptimalTileSize(screenWidth, screenHeight, 1, 3)
        val tiles = mutableListOf<TileConfiguration>()
        
        // Only the most essential apps
        val minimalApps = listOf("Phone", "Emergency", "Messages")
        
        minimalApps.forEachIndexed { index, appName ->
            tiles.add(
                TileConfiguration(
                    id = "minimal_tile_$index",
                    appName = appName,
                    position = TilePosition(index, 0),
                    size = tileSize,
                    priority = index + 1,
                    isAccessible = true,
                    hasLargeText = true,
                    hasHighContrast = true
                )
            )
        }
        
        return LayoutConfiguration(
            mode = ToggleMode.MINIMAL,
            gridColumns = 1,
            gridRows = 3,
            tiles = tiles,
            backgroundColor = "#FFFFFF", // Pure white for minimal
            fontScale = 2.0f, // Maximum font scale for minimal mode
            iconScale = 1.8f,
            paddingDp = 32,
            hasEmergencyAccess = true,
            isAccessibilityOptimized = true
        )
    }
    
    /**
     * Generate Welcome Mode layout (2x3 grid, onboarding-friendly)
     */
    private fun generateWelcomeLayout(
        screenWidth: Int,
        screenHeight: Int,
        availableApps: List<String>
    ): LayoutConfiguration {
        val tileSize = calculateOptimalTileSize(screenWidth, screenHeight, 2, 3)
        val tiles = mutableListOf<TileConfiguration>()
        
        // Welcome/onboarding apps
        val welcomeApps = listOf("Setup", "Tutorial", "Phone", "Emergency", "Help", "Settings")
        
        var row = 0
        var col = 0
        
        welcomeApps.forEachIndexed { index, appName ->
            tiles.add(
                TileConfiguration(
                    id = "welcome_tile_$index",
                    appName = appName,
                    position = TilePosition(row, col),
                    size = tileSize,
                    priority = index + 1,
                    isAccessible = true,
                    hasLargeText = true,
                    hasHighContrast = true
                )
            )
            
            col++
            if (col >= 2) {
                col = 0
                row++
            }
        }
        
        return LayoutConfiguration(
            mode = ToggleMode.WELCOME,
            gridColumns = 2,
            gridRows = 3,
            tiles = tiles,
            backgroundColor = "#FFF3E0", // Light orange for welcome
            fontScale = 1.6f,
            iconScale = 1.4f,
            paddingDp = 20,
            hasEmergencyAccess = true,
            isAccessibilityOptimized = true
        )
    }
    
    /**
     * Calculate optimal tile size based on screen dimensions and grid
     */
    private fun calculateOptimalTileSize(
        screenWidth: Int,
        screenHeight: Int,
        columns: Int,
        rows: Int
    ): TileSize {
        val density = context.resources.displayMetrics.density
        val minTileSizePx = (MIN_TILE_SIZE_DP * density).toInt()
        val paddingPx = (MIN_PADDING_DP * density).toInt()
        
        // Calculate available space
        val availableWidth = screenWidth - (paddingPx * (columns + 1))
        val availableHeight = screenHeight - (paddingPx * (rows + 1))
        
        // Calculate tile dimensions
        val tileWidth = maxOf(availableWidth / columns, minTileSizePx)
        val tileHeight = maxOf(availableHeight / rows, minTileSizePx)
        
        // Use square tiles (minimum of width/height for consistency)
        val tileDimension = minOf(tileWidth, tileHeight)
        
        return TileSize(tileDimension, tileDimension)
    }
    
    /**
     * Validate layout against Windsurf accessibility and cognitive load rules
     */
    private fun validateAndOptimizeLayout(layout: LayoutConfiguration): LayoutConfiguration {
        val violations = mutableListOf<String>()
        
        // Validate grid constraints
        if (layout.gridColumns > MAX_COLUMNS) {
            violations.add("Grid columns exceed maximum ($MAX_COLUMNS)")
        }
        
        if (layout.gridRows > MAX_ROWS) {
            violations.add("Grid rows exceed maximum ($MAX_ROWS)")
        }
        
        if (layout.tiles.size > MAX_TILES_PER_SCREEN) {
            violations.add("Tile count exceeds maximum ($MAX_TILES_PER_SCREEN)")
        }
        
        // Validate accessibility requirements
        if (layout.fontScale < 1.6f && layout.mode != ToggleMode.FAMILY) {
            violations.add("Font scale below minimum 1.6x for elderly users")
        }
        
        // Validate tile sizes
        val density = context.resources.displayMetrics.density
        val minTileSizePx = (MIN_TILE_SIZE_DP * density).toInt()
        
        layout.tiles.forEach { tile ->
            if (tile.size.width < minTileSizePx || tile.size.height < minTileSizePx) {
                violations.add("Tile ${tile.id} below minimum size requirement")
            }
        }
        
        if (violations.isNotEmpty()) {
            Log.w(TAG, "Layout validation violations: ${violations.joinToString(", ")}")
            // In production, we might auto-correct these violations
        }
        
        return layout
    }
    
    /**
     * Update tile position in current layout
     */
    suspend fun updateTilePosition(tileId: String, newPosition: TilePosition): Boolean {
        val currentLayout = _currentLayout.value ?: return false
        
        val updatedTiles = currentLayout.tiles.map { tile ->
            if (tile.id == tileId) {
                tile.copy(position = newPosition)
            } else {
                tile
            }
        }
        
        val updatedLayout = currentLayout.copy(tiles = updatedTiles)
        _currentLayout.value = updatedLayout
        
        Log.i(TAG, "Updated tile $tileId position to $newPosition")
        return true
    }
    
    /**
     * Get current layout configuration
     */
    fun getCurrentLayout(): LayoutConfiguration? = _currentLayout.value
    
    /**
     * Check if layout meets elderly accessibility requirements
     */
    fun isAccessibilityCompliant(layout: LayoutConfiguration): Boolean {
        // Check font scale
        if (layout.fontScale < 1.6f && layout.mode != ToggleMode.FAMILY) return false
        
        // Check tile count (cognitive load)
        if (layout.tiles.size > MAX_TILES_PER_SCREEN) return false
        
        // Check tile sizes
        val density = context.resources.displayMetrics.density
        val minTileSizePx = (MIN_TILE_SIZE_DP * density).toInt()
        
        return layout.tiles.all { tile ->
            tile.size.width >= minTileSizePx && 
            tile.size.height >= minTileSizePx &&
            tile.isAccessible
        }
    }
}

/**
 * Exception thrown when layout generation fails
 */
class LayoutGenerationException(message: String, cause: Throwable? = null) : Exception(message, cause)
