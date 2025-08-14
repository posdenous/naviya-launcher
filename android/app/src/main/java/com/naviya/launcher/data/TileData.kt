package com.naviya.launcher.data

import androidx.annotation.DrawableRes

/**
 * Data class representing a launcher tile
 * Designed for elderly users with accessibility features
 */
data class TileData(
    /**
     * Unique identifier for the tile
     */
    val id: String,
    
    /**
     * Display title for the tile
     * Should be short and clear for elderly users
     */
    val title: String,
    
    /**
     * Resource ID for the tile icon
     * Icons should be simple and recognizable
     */
    @DrawableRes
    val iconResId: Int,
    
    /**
     * Badge count to display on the tile
     * Used for unread messages, missed calls, etc.
     */
    val badgeCount: Int = 0,
    
    /**
     * Whether the tile should be visually highlighted
     * Used for important tiles like emergency or unread communications
     */
    val isHighlighted: Boolean = false,
    
    /**
     * Description for accessibility services (TalkBack)
     * Should be descriptive and helpful for visually impaired users
     */
    val accessibilityDescription: String = "",
    
    /**
     * Additional notes to display on the tile
     * Used for status information like "Caregiver not available"
     */
    var notes: String? = null,
    
    /**
     * Whether the tile is enabled
     * Disabled tiles are shown but not interactive
     */
    val isEnabled: Boolean = true,
    
    /**
     * Color tint for the tile (optional)
     * Used for color-coding tiles by category
     */
    val colorTint: Int? = null,
    
    /**
     * Priority of the tile for sorting
     * Higher priority tiles appear first in their mode
     */
    val priority: Int = 0,
    
    /**
     * Whether the tile should provide haptic feedback when tapped
     * Useful for important tiles like emergency
     */
    val hasHapticFeedback: Boolean = false,
    
    /**
     * Whether the tile should have a large touch target
     * Useful for users with motor control difficulties
     */
    val hasLargeTouchTarget: Boolean = true,
    
    /**
     * Action to perform when the tile is tapped
     * Can be a deep link, activity intent, or custom action
     */
    val action: String? = null
) {
    /**
     * Returns whether this tile has any unread items
     */
    fun hasUnread(): Boolean = badgeCount > 0
    
    /**
     * Returns whether this tile is an emergency tile
     */
    fun isEmergencyTile(): Boolean = id == "emergency"
    
    /**
     * Returns whether this tile is an unread communications tile
     */
    fun isUnreadTile(): Boolean = id == "unread"
    
    /**
     * Returns the accessibility content description for this tile
     * Includes badge count information if present
     */
    fun getAccessibilityDescription(): String {
        val baseDescription = accessibilityDescription.ifEmpty { title }
        return when {
            badgeCount > 0 -> "$baseDescription. You have $badgeCount unread items."
            notes != null -> "$baseDescription. Note: $notes"
            else -> baseDescription
        }
    }
    
    /**
     * Returns a copy of this tile with updated badge count
     */
    fun withBadgeCount(count: Int): TileData {
        return copy(badgeCount = count, isHighlighted = count > 0)
    }
    
    /**
     * Returns a copy of this tile with updated notes
     */
    fun withNotes(newNotes: String?): TileData {
        return copy(notes = newNotes)
    }
}
