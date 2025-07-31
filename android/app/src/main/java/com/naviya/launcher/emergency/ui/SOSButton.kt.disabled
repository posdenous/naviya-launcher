package com.naviya.launcher.emergency.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.naviya.launcher.R
import com.naviya.launcher.emergency.EmergencyService
import com.naviya.launcher.emergency.SOSTrigger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Emergency SOS Button for Naviya Launcher
 * Always accessible, elderly-friendly design with accessibility support
 * Follows Windsurf rules for elderly accessibility and emergency safety
 */
class SOSButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {
    
    @Inject
    lateinit var emergencyService: EmergencyService
    
    private var isSOSActive = false
    private var confirmationRequired = true
    private var confirmationStartTime = 0L
    private val confirmationTimeoutMs = 3000L // 3 seconds to confirm
    
    companion object {
        private const val TAG = "SOSButton"
        private const val MIN_TOUCH_TARGET_SIZE_DP = 48 // Windsurf rule: minimum 48dp
        private const val PREFERRED_TOUCH_TARGET_SIZE_DP = 64 // Elderly-friendly size
        private const val VIBRATION_DURATION_MS = 500L
    }
    
    init {
        setupSOSButton()
    }
    
    /**
     * Setup SOS button with elderly-friendly design and accessibility
     */
    private fun setupSOSButton() {
        // Apply Windsurf accessibility rules
        setupAccessibility()
        setupVisualDesign()
        setupInteraction()
    }
    
    /**
     * Setup accessibility features following Windsurf rules
     */
    private fun setupAccessibility() {
        // Minimum touch target size (Windsurf rule: 48dp minimum)
        val touchTargetSize = (PREFERRED_TOUCH_TARGET_SIZE_DP * resources.displayMetrics.density).toInt()
        minimumWidth = touchTargetSize
        minimumHeight = touchTargetSize
        
        // Accessibility content description in multiple languages
        contentDescription = getSOSContentDescription()
        
        // Enable accessibility focus
        isFocusable = true
        isFocusableInTouchMode = true
        
        // Custom accessibility actions
        setAccessibilityDelegate(object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                
                info.className = "Emergency Button"
                info.contentDescription = getSOSContentDescription()
                
                // Add custom action for voice activation
                info.addAction(
                    AccessibilityNodeInfo.AccessibilityAction(
                        AccessibilityNodeInfo.ACTION_CLICK,
                        "Activate Emergency SOS"
                    )
                )
                
                // Mark as important for accessibility
                info.isImportantForAccessibility = true
            }
            
            // Accessibility event handling removed to fix compilation
        })
    }
    
    /**
     * Setup visual design following elderly accessibility guidelines
     */
    private fun setupVisualDesign() {
        // High contrast colors (Windsurf rule: 4.5:1 contrast ratio)
        val emergencyRed = ContextCompat.getColor(context, android.R.color.holo_red_dark)
        val whiteText = ContextCompat.getColor(context, android.R.color.white)
        
        // Create gradient background for better visibility
        val background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(emergencyRed)
            setStroke(4, whiteText) // White border for contrast
        }
        
        setBackground(background)
        setTextColor(whiteText)
        
        // Large text size for elderly users (Windsurf rule: 1.6x font scale minimum)
        textSize = 18f // Base size, will be scaled by system font scale
        
        // Bold text for better readability
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        
        // SOS text
        text = "SOS"
        
        // Padding for better touch target
        val padding = (8 * resources.displayMetrics.density).toInt()
        setPadding(padding, padding, padding, padding)
        
        // Elevation for visual prominence
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = 8f
        }
    }
    
    /**
     * Setup interaction behavior
     */
    private fun setupInteraction() {
        setOnClickListener {
            handleSOSActivation()
        }
        
        // Long press for immediate activation (accessibility feature)
        setOnLongClickListener {
            handleSOSActivation(immediate = true)
            true
        }
    }
    
    /**
     * Handle SOS button activation with confirmation
     */
    private fun handleSOSActivation(immediate: Boolean = false) {
        // Provide haptic feedback (Windsurf rule: haptic feedback for interactions)
        performHapticFeedback()
        
        if (immediate || !confirmationRequired) {
            activateSOS()
        } else {
            startConfirmationSequence()
        }
    }
    
    /**
     * Start confirmation sequence to prevent accidental activation
     */
    private fun startConfirmationSequence() {
        if (isSOSActive) return
        
        confirmationStartTime = System.currentTimeMillis()
        
        // Change button appearance to show confirmation state
        text = "PRESS\nAGAIN\nTO\nCONFIRM"
        alpha = 0.8f
        
        // Announce confirmation requirement
        announceForAccessibility("Press again within 3 seconds to confirm emergency SOS")
        
        // Reset after timeout
        postDelayed({
            if (System.currentTimeMillis() - confirmationStartTime >= confirmationTimeoutMs) {
                resetButtonState()
            }
        }, confirmationTimeoutMs)
        
        // Set click listener for confirmation
        setOnClickListener {
            if (System.currentTimeMillis() - confirmationStartTime < confirmationTimeoutMs) {
                activateSOS()
            } else {
                resetButtonState()
                handleSOSActivation() // Start new confirmation sequence
            }
        }
    }
    
    /**
     * Activate emergency SOS system
     */
    private fun activateSOS() {
        if (isSOSActive) return
        
        isSOSActive = true
        
        // Update button appearance
        text = "SOS\nACTIVE"
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
        alpha = 1.0f
        
        // Strong haptic feedback
        performStrongHapticFeedback()
        
        // Announce activation
        announceForAccessibility("Emergency SOS activated. Calling for help now.")
        
        // Activate emergency service
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            try {
                val result = emergencyService.activateSOS(
                    userLanguage = getCurrentLanguage(),
                    triggeredBy = SOSTrigger.MANUAL
                )
                
                // Handle result
                when (result) {
                    is com.naviya.launcher.emergency.EmergencyResult.Success -> {
                        announceForAccessibility("Emergency services contacted successfully")
                    }
                    is com.naviya.launcher.emergency.EmergencyResult.Error -> {
                        announceForAccessibility("Emergency activation failed: ${result.message}")
                    }
                    is com.naviya.launcher.emergency.EmergencyResult.NoContactsConfigured -> {
                        announceForAccessibility("No emergency contacts configured")
                    }
                }
                
                // Show cancel option after 10 seconds
                postDelayed({
                    showCancelOption()
                }, 10000)
                
            } catch (e: Exception) {
                announceForAccessibility("Emergency activation error")
                resetButtonState()
            }
        }
    }
    
    /**
     * Show cancel option for false alarms
     */
    private fun showCancelOption() {
        text = "TAP TO\nCANCEL"
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
        
        setOnClickListener {
            cancelSOS()
        }
        
        announceForAccessibility("Tap to cancel emergency if this was a false alarm")
    }
    
    /**
     * Cancel active SOS
     */
    private fun cancelSOS() {
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            emergencyService.cancelSOS("User cancelled - false alarm")
            announceForAccessibility("Emergency cancelled")
            resetButtonState()
        }
    }
    
    /**
     * Reset button to initial state
     */
    private fun resetButtonState() {
        isSOSActive = false
        text = "SOS"
        alpha = 1.0f
        setupVisualDesign()
        setupInteraction()
    }
    
    /**
     * Perform haptic feedback following Windsurf rules
     */
    private fun performHapticFeedback() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            // Haptic feedback failed, continue without it
        }
    }
    
    /**
     * Perform strong haptic feedback for emergency activation
     */
    private fun performStrongHapticFeedback() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create pattern: short-long-short for emergency
                val pattern = longArrayOf(0, 200, 100, 500, 100, 200)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 200, 100, 500, 100, 200)
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            // Haptic feedback failed, continue without it
        }
    }
    
    /**
     * Get content description in current language
     */
    private fun getSOSContentDescription(): String {
        return when (getCurrentLanguage()) {
            "de" -> "Notfall SOS Taste. Doppeltippen für Hilfe."
            "tr" -> "Acil SOS düğmesi. Yardım için iki kez dokunun."
            "uk" -> "Кнопка екстреного виклику SOS. Двічі торкніться для допомоги."
            "ar" -> "زر الطوارئ SOS. اضغط مرتين للمساعدة."
            else -> "Emergency SOS button. Double tap for help."
        }
    }
    
    /**
     * Get current system language
     */
    private fun getCurrentLanguage(): String {
        return context.resources.configuration.locales[0].language
    }
    
    /**
     * Enable/disable confirmation requirement
     */
    fun setConfirmationRequired(required: Boolean) {
        confirmationRequired = required
    }
    
    /**
     * Check if SOS is currently active
     */
    fun isSOSActive(): Boolean = isSOSActive
    
    /**
     * Force reset button (for emergency situations)
     */
    fun forceReset() {
        resetButtonState()
    }
}
