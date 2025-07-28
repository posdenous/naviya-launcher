package com.naviya.launcher.layout.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.naviya.launcher.R
import com.naviya.launcher.layout.data.LayoutConfiguration
import com.naviya.launcher.layout.data.TileConfiguration
import com.naviya.launcher.toggle.ToggleMode

/**
 * Custom GridView for Naviya Launcher
 * Displays app tiles in elderly-friendly layouts with accessibility support
 * Follows Windsurf rules for touch targets, contrast, and cognitive load
 */
class LauncherGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {
    
    companion object {
        private const val TAG = "LauncherGridView"
        private const val MIN_TOUCH_TARGET_DP = 48 // Windsurf rule: 48dp minimum
        private const val PREFERRED_TOUCH_TARGET_DP = 64 // Elderly-friendly size
    }
    
    private var currentLayout: LayoutConfiguration? = null
    private var onTileClickListener: ((String) -> Unit)? = null
    private var onTileLongClickListener: ((String) -> Unit)? = null
    
    init {
        setupGridView()
    }
    
    /**
     * Setup grid view with accessibility defaults
     */
    private fun setupGridView() {
        // Enable accessibility
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        
        // Set content description
        contentDescription = "Launcher grid with app tiles"
        
        // Default padding
        val defaultPadding = (16 * resources.displayMetrics.density).toInt()
        setPadding(defaultPadding)
    }
    
    /**
     * Apply layout configuration to the grid
     */
    fun applyLayout(layout: LayoutConfiguration) {
        try {
            Log.i(TAG, "Applying layout for mode: ${layout.mode}")
            
            currentLayout = layout
            
            // Clear existing views
            removeAllViews()
            
            // Configure grid
            columnCount = layout.gridColumns
            rowCount = layout.gridRows
            
            // Set background color
            setBackgroundColor(Color.parseColor(layout.backgroundColor))
            
            // Set padding
            val paddingPx = (layout.paddingDp * resources.displayMetrics.density).toInt()
            setPadding(paddingPx)
            
            // Create and add tiles
            layout.tiles.forEach { tileConfig ->
                val tileView = createTileView(tileConfig, layout)
                addView(tileView)
            }
            
            // Update content description
            contentDescription = "Launcher in ${layout.mode.getLocalizedName(getCurrentLanguage())} mode with ${layout.tiles.size} apps"
            
            Log.i(TAG, "Successfully applied layout with ${layout.tiles.size} tiles")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply layout", e)
        }
    }
    
    /**
     * Create a tile view for an app
     */
    private fun createTileView(tileConfig: TileConfiguration, layout: LayoutConfiguration): View {
        val tileView = TileView(context).apply {
            // Set tile configuration
            setTileConfiguration(tileConfig, layout)
            
            // Set click listeners
            setOnClickListener {
                onTileClickListener?.invoke(tileConfig.appName)
                announceForAccessibility("Opened ${tileConfig.appName}")
            }
            
            setOnLongClickListener {
                onTileLongClickListener?.invoke(tileConfig.appName)
                announceForAccessibility("Long pressed ${tileConfig.appName}")
                true
            }
        }
        
        // Set grid layout parameters
        val layoutParams = LayoutParams().apply {
            width = tileConfig.size.width
            height = tileConfig.size.height
            rowSpec = spec(tileConfig.position.row)
            columnSpec = spec(tileConfig.position.column)
            
            // Set margins for spacing
            val marginPx = (8 * resources.displayMetrics.density).toInt()
            setMargins(marginPx, marginPx, marginPx, marginPx)
        }
        
        tileView.layoutParams = layoutParams
        
        return tileView
    }
    
    /**
     * Set tile click listener
     */
    fun setOnTileClickListener(listener: (String) -> Unit) {
        onTileClickListener = listener
    }
    
    /**
     * Set tile long click listener
     */
    fun setOnTileLongClickListener(listener: (String) -> Unit) {
        onTileLongClickListener = listener
    }
    
    /**
     * Get current system language
     */
    private fun getCurrentLanguage(): String {
        return context.resources.configuration.locales[0].language
    }
    
    /**
     * Update tile visibility
     */
    fun updateTileVisibility(appName: String, isVisible: Boolean) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is TileView && child.getAppName() == appName) {
                child.visibility = if (isVisible) View.VISIBLE else View.GONE
                break
            }
        }
    }
    
    /**
     * Highlight a specific tile (for tutorials or guidance)
     */
    fun highlightTile(appName: String, highlight: Boolean) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is TileView && child.getAppName() == appName) {
                child.setHighlighted(highlight)
                break
            }
        }
    }
}

/**
 * Individual tile view for apps
 */
class TileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    
    private lateinit var iconView: ImageView
    private lateinit var labelView: TextView
    private var appName: String = ""
    private var isHighlighted: Boolean = false
    
    init {
        setupTileView()
    }
    
    /**
     * Setup tile view components
     */
    private fun setupTileView() {
        // Create icon view
        iconView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        addView(iconView)
        
        // Create label view
        labelView = TextView(context).apply {
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        addView(labelView)
        
        // Set accessibility properties
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = true
    }
    
    /**
     * Configure tile with layout settings
     */
    fun setTileConfiguration(tileConfig: TileConfiguration, layout: LayoutConfiguration) {
        appName = tileConfig.appName
        
        // Set label
        val displayLabel = tileConfig.customLabel ?: tileConfig.appName
        labelView.text = displayLabel
        
        // Apply font scaling (Windsurf rule: 1.6x minimum for elderly)
        val baseTextSize = 14f
        labelView.textSize = baseTextSize * layout.fontScale
        
        // Apply text styling
        if (tileConfig.hasLargeText) {
            labelView.setTypeface(labelView.typeface, android.graphics.Typeface.BOLD)
        }
        
        // Set text color based on contrast requirements
        val textColor = if (tileConfig.hasHighContrast) {
            ContextCompat.getColor(context, android.R.color.black)
        } else {
            ContextCompat.getColor(context, android.R.color.primary_text_light)
        }
        labelView.setTextColor(textColor)
        
        // Set icon (placeholder for now)
        iconView.setImageResource(getIconResource(tileConfig.appName))
        
        // Apply icon scaling
        val iconSize = (48 * layout.iconScale * resources.displayMetrics.density).toInt()
        iconView.layoutParams = LayoutParams(iconSize, iconSize)
        
        // Set background
        background = createTileBackground(tileConfig, layout)
        
        // Set content description for accessibility
        contentDescription = "${displayLabel}. ${getAppDescription(tileConfig.appName)}"
        
        // Set minimum touch target size (Windsurf rule: 48dp minimum)
        val minTouchTarget = (48 * resources.displayMetrics.density).toInt()
        minimumWidth = minTouchTarget
        minimumHeight = minTouchTarget
    }
    
    /**
     * Create background drawable for tile
     */
    private fun createTileBackground(tileConfig: TileConfiguration, layout: LayoutConfiguration): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f * resources.displayMetrics.density
            
            // Set background color based on mode and contrast
            val backgroundColor = when {
                isHighlighted -> ContextCompat.getColor(context, android.R.color.holo_blue_light)
                tileConfig.hasHighContrast -> ContextCompat.getColor(context, android.R.color.white)
                else -> ContextCompat.getColor(context, android.R.color.background_light)
            }
            setColor(backgroundColor)
            
            // Set border for better visibility
            val borderColor = if (tileConfig.hasHighContrast) {
                ContextCompat.getColor(context, android.R.color.black)
            } else {
                ContextCompat.getColor(context, android.R.color.darker_gray)
            }
            setStroke(2, borderColor)
        }
    }
    
    /**
     * Get icon resource for app
     */
    private fun getIconResource(appName: String): Int {
        return when (appName.lowercase()) {
            "phone" -> android.R.drawable.sym_action_call
            "messages" -> android.R.drawable.sym_action_email
            "camera" -> android.R.drawable.ic_menu_camera
            "settings" -> android.R.drawable.ic_menu_preferences
            "emergency" -> android.R.drawable.ic_dialog_alert
            "gallery" -> android.R.drawable.ic_menu_gallery
            else -> android.R.drawable.sym_def_app_icon
        }
    }
    
    /**
     * Get accessibility description for app
     */
    private fun getAppDescription(appName: String): String {
        return when (appName.lowercase()) {
            "phone" -> "Make phone calls"
            "messages" -> "Send and receive text messages"
            "camera" -> "Take photos and videos"
            "settings" -> "Change device settings"
            "emergency" -> "Emergency SOS button"
            "gallery" -> "View photos and videos"
            else -> "Application"
        }
    }
    
    /**
     * Set highlighted state
     */
    fun setHighlighted(highlighted: Boolean) {
        isHighlighted = highlighted
        // Recreate background to reflect highlight state
        // This would need the original tile config and layout
        invalidate()
    }
    
    /**
     * Get app name
     */
    fun getAppName(): String = appName
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Measure children
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        
        // Set our size
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        
        setMeasuredDimension(width, height)
    }
    
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t
        
        // Layout icon in top 60% of tile
        val iconHeight = (height * 0.6f).toInt()
        val iconWidth = iconView.measuredWidth
        val iconLeft = (width - iconWidth) / 2
        val iconTop = (iconHeight - iconView.measuredHeight) / 2
        iconView.layout(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconView.measuredHeight)
        
        // Layout label in bottom 40% of tile
        val labelTop = iconHeight
        val labelHeight = height - iconHeight
        val labelWidth = labelView.measuredWidth
        val labelLeft = (width - labelWidth) / 2
        labelView.layout(labelLeft, labelTop, labelLeft + labelWidth, labelTop + labelHeight)
    }
}
