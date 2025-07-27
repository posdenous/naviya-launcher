package com.naviya.launcher.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.naviya.launcher.R
import com.naviya.launcher.core.accessibility.AccessibilityHelper
import com.naviya.launcher.core.crash.CrashHandler
import com.naviya.launcher.core.logging.NaviyaLogger
import com.naviya.launcher.databinding.ActivityLauncherBinding
import com.naviya.launcher.ui.emergency.EmergencyActivity
import com.naviya.launcher.ui.onboarding.OnboardingActivity
import com.naviya.launcher.ui.recovery.CrashRecoveryActivity
import com.naviya.launcher.ui.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main launcher activity for elderly users
 * Implements 2x3 grid layout with accessibility features
 */
@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private val viewModel: LauncherViewModel by viewModels()
    
    @Inject
    lateinit var accessibilityHelper: AccessibilityHelper
    
    @Inject
    lateinit var crashHandler: CrashHandler
    
    @Inject
    lateinit var logger: NaviyaLogger
    
    private lateinit var tilesAdapter: LauncherTilesAdapter
    private var backPressCount = 0
    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        logger.info("LauncherActivity", "Starting launcher for elderly user")
        
        // Apply elderly-friendly window settings
        setupWindowForElderlyUsers()
        
        // Initialize view binding
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if user needs onboarding
        if (viewModel.isFirstLaunch()) {
            startOnboarding()
            return
        }
        
        // Check if crash recovery mode is needed
        if (viewModel.shouldEnterRecoveryMode()) {
            startCrashRecovery()
            return
        }
        
        // Initialize launcher UI
        initializeLauncher()
        
        // Set up observers
        setupObservers()
        
        // Apply accessibility settings
        applyAccessibilitySettings()
        
        logger.info("LauncherActivity", "Launcher initialized successfully")
    }

    override fun onResume() {
        super.onResume()
        
        // Refresh launcher state
        viewModel.refreshLauncherState()
        
        // Update notification counts
        viewModel.updateNotificationCounts()
        
        // Check for crash recovery exit conditions
        viewModel.checkRecoveryExitConditions()
        
        logger.debug("LauncherActivity", "Launcher resumed")
    }

    override fun onPause() {
        super.onPause()
        
        // Save current state
        viewModel.saveLauncherState()
        
        logger.debug("LauncherActivity", "Launcher paused")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                handleBackPress()
                true
            }
            KeyEvent.KEYCODE_HOME -> {
                // Already home, do nothing
                true
            }
            KeyEvent.KEYCODE_MENU -> {
                openSettings()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    /**
     * Setup window settings optimized for elderly users
     */
    private fun setupWindowForElderlyUsers() {
        // Keep screen on during use
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Enable hardware acceleration for smooth animations
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        
        // Hide system UI for distraction-free experience
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply system bar insets as padding
            findViewById<View>(android.R.id.content).setPadding(
                systemBars.left, systemBars.top, systemBars.right, systemBars.bottom
            )
            insets
        }
    }

    /**
     * Initialize the main launcher interface
     */
    private fun initializeLauncher() {
        // Set up 2x3 grid layout
        val gridLayoutManager = GridLayoutManager(this, 3) // 3 columns
        binding.tilesRecyclerView.layoutManager = gridLayoutManager
        
        // Initialize tiles adapter
        tilesAdapter = LauncherTilesAdapter(
            onTileClick = { tile -> handleTileClick(tile) },
            onTileLongClick = { tile -> handleTileLongClick(tile) },
            accessibilityHelper = accessibilityHelper
        )
        binding.tilesRecyclerView.adapter = tilesAdapter
        
        // Set up SOS button
        binding.sosButton.setOnClickListener { handleSosButtonClick() }
        binding.sosButton.setOnLongClickListener { 
            handleSosButtonLongClick()
            true
        }
        
        // Set up settings access (long press on home icon)
        binding.homeIcon.setOnLongClickListener {
            handleHomeIconLongPress()
            true
        }
        
        // Set up unread notifications tile
        binding.unreadTile.setOnClickListener { handleUnreadTileClick() }
        
        logger.debug("LauncherActivity", "Launcher UI initialized")
    }

    /**
     * Set up data observers
     */
    private fun setupObservers() {
        // Observe launcher state
        viewModel.launcherState.observe(this) { state ->
            updateLauncherUI(state)
        }
        
        // Observe tile configuration
        viewModel.tileConfiguration.observe(this) { tiles ->
            tilesAdapter.updateTiles(tiles)
        }
        
        // Observe notification state
        viewModel.notificationState.observe(this) { notifications ->
            updateNotificationTile(notifications)
        }
        
        // Observe crash recovery state
        viewModel.crashRecoveryState.observe(this) { recovery ->
            if (recovery.recoveryMode.isActive) {
                startCrashRecovery()
            }
        }
        
        // Observe accessibility settings
        viewModel.accessibilitySettings.observe(this) { settings ->
            applyAccessibilitySettings(settings)
        }
        
        // Observe error states
        viewModel.errorState.observe(this) { error ->
            handleError(error)
        }
        
        logger.debug("LauncherActivity", "Observers set up")
    }

    /**
     * Apply accessibility settings for elderly users
     */
    private fun applyAccessibilitySettings(settings: com.naviya.launcher.data.models.AccessibilitySettings? = null) {
        val accessibilitySettings = settings ?: viewModel.getCurrentAccessibilitySettings()
        
        // Apply font scaling
        accessibilityHelper.applyFontScale(this, accessibilitySettings.fontScale)
        
        // Apply high contrast if enabled
        if (accessibilitySettings.highContrastEnabled) {
            accessibilityHelper.applyHighContrast(binding.root)
        }
        
        // Configure haptic feedback
        accessibilityHelper.configureHapticFeedback(
            binding.root, 
            accessibilitySettings.hapticFeedbackEnabled
        )
        
        // Configure TTS if enabled
        if (accessibilitySettings.ttsEnabled) {
            accessibilityHelper.enableTTS(this)
        }
        
        logger.debug("LauncherActivity", "Accessibility settings applied")
    }

    /**
     * Handle tile click events
     */
    private fun handleTileClick(tile: com.naviya.launcher.data.models.TileConfiguration) {
        logger.info("LauncherActivity", "Tile clicked: ${tile.tileType}")
        
        when (tile.tileType) {
            com.naviya.launcher.data.models.TileType.PHONE_DIALER -> {
                launchPhoneDialer()
            }
            com.naviya.launcher.data.models.TileType.SMS_MESSAGES -> {
                launchMessagesApp()
            }
            com.naviya.launcher.data.models.TileType.SETTINGS -> {
                openSettings()
            }
            com.naviya.launcher.data.models.TileType.CAMERA -> {
                launchCamera()
            }
            com.naviya.launcher.data.models.TileType.CUSTOM_APP -> {
                launchCustomApp(tile.packageName)
            }
            else -> {
                logger.warning("LauncherActivity", "Unknown tile type: ${tile.tileType}")
            }
        }
        
        // Provide haptic feedback
        accessibilityHelper.provideHapticFeedback(binding.root)
    }

    /**
     * Handle tile long click events
     */
    private fun handleTileLongClick(tile: com.naviya.launcher.data.models.TileConfiguration) {
        logger.info("LauncherActivity", "Tile long clicked: ${tile.tileType}")
        
        // Show tile options or app info
        // Implementation depends on specific requirements
        accessibilityHelper.provideHapticFeedback(binding.root)
    }

    /**
     * Handle SOS button click
     */
    private fun handleSosButtonClick() {
        logger.info("LauncherActivity", "SOS button clicked")
        
        // Start emergency activity
        val intent = Intent(this, EmergencyActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        
        // Provide strong haptic feedback
        accessibilityHelper.provideStrongHapticFeedback(binding.root)
    }

    /**
     * Handle SOS button long click
     */
    private fun handleSosButtonLongClick() {
        logger.info("LauncherActivity", "SOS button long clicked")
        
        // Trigger immediate emergency call
        viewModel.triggerEmergencyCall()
        
        // Provide very strong haptic feedback
        accessibilityHelper.provideVeryStrongHapticFeedback(binding.root)
    }

    /**
     * Handle home icon long press for settings access
     */
    private fun handleHomeIconLongPress(): Boolean {
        logger.info("LauncherActivity", "Home icon long pressed")
        
        // Check if recovery mode should be triggered
        if (viewModel.shouldTriggerRecoveryMode()) {
            startCrashRecovery()
            return true
        }
        
        // Otherwise open settings
        openSettings()
        return true
    }

    /**
     * Handle unread notifications tile click
     */
    private fun handleUnreadTileClick() {
        logger.info("LauncherActivity", "Unread tile clicked")
        
        // Show unread notifications or launch appropriate app
        viewModel.handleUnreadNotificationsClick()
    }

    /**
     * Handle back button press with double-tap to exit
     */
    private fun handleBackPress() {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastBackPressTime < 2000) { // 2 seconds
            backPressCount++
        } else {
            backPressCount = 1
        }
        
        lastBackPressTime = currentTime
        
        if (backPressCount >= 2) {
            // Double tap detected - minimize launcher
            moveTaskToBack(true)
            backPressCount = 0
        } else {
            // Show hint for double tap
            accessibilityHelper.announceForAccessibility(
                binding.root,
                getString(R.string.double_tap_to_exit)
            )
        }
        
        logger.debug("LauncherActivity", "Back press handled: count=$backPressCount")
    }

    /**
     * Launch phone dialer
     */
    private fun launchPhoneDialer() {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            startActivity(intent)
            logger.info("LauncherActivity", "Phone dialer launched")
        } catch (e: Exception) {
            logger.error("LauncherActivity", "Failed to launch phone dialer", e)
            handleError("Failed to open phone")
        }
    }

    /**
     * Launch messages app
     */
    private fun launchMessagesApp() {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_MESSAGING)
            startActivity(intent)
            logger.info("LauncherActivity", "Messages app launched")
        } catch (e: Exception) {
            logger.error("LauncherActivity", "Failed to launch messages app", e)
            handleError("Failed to open messages")
        }
    }

    /**
     * Launch camera app
     */
    private fun launchCamera() {
        try {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivity(intent)
            logger.info("LauncherActivity", "Camera launched")
        } catch (e: Exception) {
            logger.error("LauncherActivity", "Failed to launch camera", e)
            handleError("Failed to open camera")
        }
    }

    /**
     * Launch custom app by package name
     */
    private fun launchCustomApp(packageName: String?) {
        if (packageName.isNullOrEmpty()) {
            logger.warning("LauncherActivity", "No package name provided for custom app")
            return
        }
        
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
                logger.info("LauncherActivity", "Custom app launched: $packageName")
            } else {
                logger.warning("LauncherActivity", "No launch intent found for: $packageName")
                handleError("App not found")
            }
        } catch (e: Exception) {
            logger.error("LauncherActivity", "Failed to launch custom app: $packageName", e)
            handleError("Failed to open app")
        }
    }

    /**
     * Open settings activity
     */
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        logger.info("LauncherActivity", "Settings opened")
    }

    /**
     * Start onboarding flow
     */
    private fun startOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        logger.info("LauncherActivity", "Onboarding started")
    }

    /**
     * Start crash recovery mode
     */
    private fun startCrashRecovery() {
        val intent = Intent(this, CrashRecoveryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        logger.info("LauncherActivity", "Crash recovery started")
    }

    /**
     * Update launcher UI based on state
     */
    private fun updateLauncherUI(state: com.naviya.launcher.data.models.LauncherState) {
        // Update tiles
        tilesAdapter.updateTiles(state.tileLayout)
        
        // Update accessibility settings
        applyAccessibilitySettings(state.accessibilitySettings)
        
        // Update offline mode indicator
        binding.offlineIndicator.visibility = if (state.offlineMode.offlineModeEnabled) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        logger.debug("LauncherActivity", "Launcher UI updated")
    }

    /**
     * Update notification tile display
     */
    private fun updateNotificationTile(notifications: com.naviya.launcher.data.models.NotificationState) {
        val unreadCount = notifications.unreadSummary.totalUnreadCount
        
        binding.unreadTile.updateBadgeCount(unreadCount)
        binding.unreadTile.updateAnimationState(notifications.tileDisplay.animationState)
        
        logger.debug("LauncherActivity", "Notification tile updated: $unreadCount unread")
    }

    /**
     * Handle error states
     */
    private fun handleError(error: String) {
        // Show error message with accessibility support
        accessibilityHelper.announceForAccessibility(binding.root, error)
        
        // Log error
        logger.error("LauncherActivity", "Error: $error")
        
        // Provide haptic feedback for error
        accessibilityHelper.provideErrorHapticFeedback(binding.root)
    }
}
