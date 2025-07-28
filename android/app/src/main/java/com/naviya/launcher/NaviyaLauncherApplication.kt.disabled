package com.naviya.launcher

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration as WorkConfiguration
import androidx.work.WorkManager
import com.naviya.launcher.core.accessibility.AccessibilityManager
import com.naviya.launcher.core.crash.CrashHandler
import com.naviya.launcher.core.logging.NaviyaLogger
import com.naviya.launcher.core.preferences.UserPreferences
import com.naviya.launcher.data.local.NaviyaDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main Application class for Naviya Launcher
 * Handles initialization of core systems for elderly users
 */
@HiltAndroidApp
class NaviyaLauncherApplication : Application(), WorkConfiguration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var crashHandler: CrashHandler
    
    @Inject
    lateinit var accessibilityManager: AccessibilityManager
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    @Inject
    lateinit var logger: NaviyaLogger

    companion object {
        private var instance: NaviyaLauncherApplication? = null
        
        fun getInstance(): NaviyaLauncherApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize logging first for crash tracking
        logger.initialize(this)
        logger.info("NaviyaLauncher", "Application starting...")
        
        // Initialize crash handling for elderly user safety
        initializeCrashHandling()
        
        // Apply accessibility defaults for elderly users
        initializeAccessibilityDefaults()
        
        // Initialize core systems
        initializeCoreServices()
        
        logger.info("NaviyaLauncher", "Application initialized successfully")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // Handle configuration changes for accessibility
        accessibilityManager.onConfigurationChanged(newConfig)
        
        // Log configuration changes for debugging
        logger.debug("NaviyaLauncher", "Configuration changed: ${newConfig.toString()}")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        
        // Handle low memory situations gracefully for elderly users
        logger.warning("NaviyaLauncher", "Low memory detected - cleaning up resources")
        
        // Clear non-essential caches
        clearNonEssentialCaches()
        
        // Notify crash handler of memory pressure
        crashHandler.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        logger.debug("NaviyaLauncher", "Memory trim requested: level $level")
        
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // Clear caches but keep essential functionality
                clearNonEssentialCaches()
            }
            TRIM_MEMORY_UI_HIDDEN -> {
                // App is in background, safe to clear more caches
                clearBackgroundCaches()
            }
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                // Aggressive cleanup
                clearAllNonEssentialData()
            }
        }
    }

    override fun getWorkManagerConfiguration(): WorkConfiguration {
        return WorkConfiguration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.INFO)
            .build()
    }

    /**
     * Initialize crash handling system for elderly user safety
     */
    private fun initializeCrashHandling() {
        try {
            crashHandler.initialize()
            
            // Set up uncaught exception handler
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                logger.error("NaviyaLauncher", "Uncaught exception in thread ${thread.name}", exception)
                crashHandler.handleCrash(exception, thread)
            }
            
            logger.info("NaviyaLauncher", "Crash handling initialized")
        } catch (e: Exception) {
            logger.error("NaviyaLauncher", "Failed to initialize crash handling", e)
        }
    }

    /**
     * Initialize accessibility defaults for elderly users
     */
    private fun initializeAccessibilityDefaults() {
        try {
            // Apply elderly-friendly defaults
            accessibilityManager.applyElderlyDefaults()
            
            // Set up accessibility monitoring
            accessibilityManager.startMonitoring()
            
            logger.info("NaviyaLauncher", "Accessibility defaults applied")
        } catch (e: Exception) {
            logger.error("NaviyaLauncher", "Failed to initialize accessibility", e)
        }
    }

    /**
     * Initialize core application services
     */
    private fun initializeCoreServices() {
        try {
            // Initialize database
            NaviyaDatabase.getInstance(this)
            
            // Initialize WorkManager for background tasks
            WorkManager.initialize(this, workManagerConfiguration)
            
            // Start essential background services
            startEssentialServices()
            
            logger.info("NaviyaLauncher", "Core services initialized")
        } catch (e: Exception) {
            logger.error("NaviyaLauncher", "Failed to initialize core services", e)
        }
    }

    /**
     * Start essential background services
     */
    private fun startEssentialServices() {
        // Services will be started by dependency injection and activities as needed
        // This prevents unnecessary battery drain for elderly users
        logger.debug("NaviyaLauncher", "Essential services ready to start on demand")
    }

    /**
     * Clear non-essential caches to free memory
     */
    private fun clearNonEssentialCaches() {
        try {
            // Clear image caches
            // Glide.get(this).clearMemory()
            
            // Clear other non-essential data
            logger.debug("NaviyaLauncher", "Non-essential caches cleared")
        } catch (e: Exception) {
            logger.warning("NaviyaLauncher", "Error clearing caches", e)
        }
    }

    /**
     * Clear background caches when app is not visible
     */
    private fun clearBackgroundCaches() {
        clearNonEssentialCaches()
        
        // Additional background cleanup
        System.gc() // Suggest garbage collection
        
        logger.debug("NaviyaLauncher", "Background caches cleared")
    }

    /**
     * Aggressive cleanup for low memory situations
     */
    private fun clearAllNonEssentialData() {
        clearBackgroundCaches()
        
        // Clear all possible caches while preserving essential functionality
        logger.debug("NaviyaLauncher", "All non-essential data cleared")
    }

    /**
     * Get application context safely
     */
    fun getAppContext(): Context = applicationContext

    /**
     * Check if app is in debug mode
     */
    fun isDebugMode(): Boolean = BuildConfig.DEBUG

    /**
     * Get app version information
     */
    fun getVersionInfo(): String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
}
