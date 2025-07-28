package com.naviya.launcher.layout.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.naviya.launcher.toggle.ToggleMode

/**
 * Data models for Launcher Layout Engine
 * Follows Windsurf rules for elderly accessibility and cognitive load management
 */

/**
 * Complete layout configuration for a toggle mode
 */
data class LayoutConfiguration(
    val mode: ToggleMode,
    val gridColumns: Int,
    val gridRows: Int,
    val tiles: List<TileConfiguration>,
    val backgroundColor: String,
    val fontScale: Float,
    val iconScale: Float,
    val paddingDp: Int,
    val hasEmergencyAccess: Boolean,
    val isAccessibilityOptimized: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Individual tile configuration within a layout
 */
data class TileConfiguration(
    val id: String,
    val appName: String,
    val position: TilePosition,
    val size: TileSize,
    val priority: Int,
    val isAccessible: Boolean,
    val hasLargeText: Boolean,
    val hasHighContrast: Boolean,
    val iconResource: String? = null,
    val customLabel: String? = null,
    val isVisible: Boolean = true
)

/**
 * Position of a tile in the grid (row, column)
 */
data class TilePosition(
    val row: Int,
    val column: Int
) {
    override fun toString(): String = "($row, $column)"
}

/**
 * Size of a tile in pixels
 */
data class TileSize(
    val width: Int,
    val height: Int
) {
    override fun toString(): String = "${width}x${height}px"
}

/**
 * Persistent layout preferences stored in Room database
 */
@Entity(tableName = "layout_preferences")
data class LayoutPreferences(
    @PrimaryKey val userId: String,
    val preferredMode: String, // ToggleMode as string
    val customFontScale: Float,
    val customIconScale: Float,
    val customBackgroundColor: String?,
    val hasHighContrastEnabled: Boolean,
    val hasLargeTextEnabled: Boolean,
    val hasReducedMotionEnabled: Boolean,
    val emergencyButtonPosition: String?, // TilePosition as JSON
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * Saved layout configuration in database
 */
@Entity(tableName = "saved_layouts")
data class SavedLayout(
    @PrimaryKey val id: String,
    val userId: String,
    val mode: String, // ToggleMode as string
    val name: String,
    val layoutJson: String, // LayoutConfiguration as JSON
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis()
)

/**
 * App tile metadata for layout engine
 */
@Entity(tableName = "app_tiles")
data class AppTile(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val isSystemApp: Boolean,
    val isAccessible: Boolean,
    val category: String, // "essential", "communication", "entertainment", etc.
    val elderlyFriendly: Boolean,
    val iconPath: String?,
    val customLabel: String?,
    val priority: Int = 0,
    val isHidden: Boolean = false,
    val lastUsed: Long = 0L
)

/**
 * Layout validation result
 */
data class LayoutValidationResult(
    val isValid: Boolean,
    val violations: List<String>,
    val warnings: List<String>,
    val accessibilityScore: Float, // 0.0 to 1.0
    val cognitiveLoadScore: Float // 0.0 to 1.0 (lower is better)
)

/**
 * Layout generation parameters
 */
data class LayoutGenerationParams(
    val mode: ToggleMode,
    val screenWidth: Int,
    val screenHeight: Int,
    val availableApps: List<String>,
    val userPreferences: LayoutPreferences?,
    val forceAccessibilityMode: Boolean = false,
    val maxTiles: Int? = null
)

/**
 * Layout metrics for analytics and optimization
 */
data class LayoutMetrics(
    val mode: ToggleMode,
    val tileCount: Int,
    val gridDensity: Float, // tiles per screen area
    val averageTileSize: TileSize,
    val accessibilityCompliance: Boolean,
    val cognitiveLoadScore: Float,
    val generationTimeMs: Long
)
