package com.naviya.launcher.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.naviya.launcher.R
import com.naviya.launcher.tiles.UnreadTile

/**
 * Custom view component for the Unread Tile in the Naviya Launcher.
 * Displays missed calls and unread SMS counts in an elderly-friendly format.
 * 
 * Features:
 * - Large, high-contrast badge showing total unread count
 * - Accessible icon with clear meaning
 * - Optional note for caregiver status
 * - Elderly-friendly touch target size (min 48dp)
 */
class UnreadTileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val titleTextView: TextView
    private val iconImageView: ImageView
    private val badgeTextView: TextView
    private val noteTextView: TextView
    private val tileBackground: View
    
    init {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.view_unread_tile, this, true)
        
        // Get references to views
        titleTextView = findViewById(R.id.tile_title)
        iconImageView = findViewById(R.id.tile_icon)
        badgeTextView = findViewById(R.id.tile_badge)
        noteTextView = findViewById(R.id.tile_note)
        tileBackground = findViewById(R.id.tile_background)
        
        // Set up default appearance
        setupDefaultAppearance()
        
        // Set click listener to open communications app
        setOnClickListener {
            openCommunicationsApp()
        }
    }
    
    /**
     * Set up the default appearance of the tile
     */
    private fun setupDefaultAppearance() {
        // Set the background shape with rounded corners
        val background = tileBackground.background as? GradientDrawable
        background?.cornerRadius = context.resources.getDimension(R.dimen.tile_corner_radius)
        
        // Hide badge and note by default
        badgeTextView.visibility = View.GONE
        noteTextView.visibility = View.GONE
    }
    
    /**
     * Update the tile with the provided state
     */
    fun updateWithState(state: UnreadTile.TileState) {
        // Update title
        titleTextView.text = state.title
        
        // Update icon
        val iconResId = getIconResourceId(state.icon)
        iconImageView.setImageResource(iconResId)
        
        // Update badge
        if (state.badgeCount > 0) {
            badgeTextView.text = state.badgeCount.toString()
            badgeTextView.visibility = View.VISIBLE
            
            // Change background color to indicate unread items
            setActiveBackground()
        } else {
            badgeTextView.visibility = View.GONE
            setInactiveBackground()
        }
        
        // Update note if present
        if (state.note != null) {
            noteTextView.text = state.note
            noteTextView.visibility = View.VISIBLE
        } else {
            noteTextView.visibility = View.GONE
        }
    }
    
    /**
     * Set the background to the active state (unread items present)
     */
    private fun setActiveBackground() {
        tileBackground.setBackgroundResource(R.drawable.bg_tile_active)
        titleTextView.setTextColor(ContextCompat.getColor(context, R.color.tile_text_active))
    }
    
    /**
     * Set the background to the inactive state (no unread items)
     */
    private fun setInactiveBackground() {
        tileBackground.setBackgroundResource(R.drawable.bg_tile_inactive)
        titleTextView.setTextColor(ContextCompat.getColor(context, R.color.tile_text_inactive))
    }
    
    /**
     * Get the appropriate icon resource based on the icon name
     */
    private fun getIconResourceId(iconName: String): Int {
        return when (iconName.lowercase()) {
            "envelope" -> R.drawable.ic_envelope
            "phone" -> R.drawable.ic_phone
            "message" -> R.drawable.ic_message
            else -> R.drawable.ic_envelope // Default
        }
    }
    
    /**
     * Open the appropriate communications app based on unread items
     */
    private fun openCommunicationsApp() {
        // Try to open the default messaging app
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_MESSAGING)
            context.startActivity(intent)
        } catch (e: Exception) {
            // If that fails, try to open the phone app
            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_CONTACTS)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }
}
