package com.naviya.launcher.unread

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.naviya.launcher.caregiver.CaregiverRepository
import com.naviya.launcher.data.LauncherRepository
import com.naviya.launcher.notifications.NotificationHelper
import kotlinx.coroutines.launch

/**
 * Initializer for the UnreadTileService
 * Ensures the service starts on app launch and relevant system events
 */
class UnreadTileServiceInitializer(private val context: Context) {

    private val launcherRepository = LauncherRepository.getInstance(context)
    private val caregiverRepository = CaregiverRepository.getInstance(context)
    private val notificationHelper = NotificationHelper.getInstance(context)
    
    private val unreadTileService = UnreadTileService(
        context,
        launcherRepository,
        caregiverRepository,
        notificationHelper
    )
    
    // Broadcast receiver for system events
    private val systemEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                    // Schedule updates after device boot or app update
                    unreadTileService.schedulePeriodicUpdates()
                }
                
                Intent.ACTION_SCREEN_ON -> {
                    // Update when screen turns on for immediate feedback
                    ProcessLifecycleOwner.get().lifecycleScope.launch {
                        unreadTileService.updateUnreadTileNow()
                    }
                }
                
                "com.naviya.launcher.action.LAUNCHER_HOME_OPENED" -> {
                    // Update when launcher home is opened
                    ProcessLifecycleOwner.get().lifecycleScope.launch {
                        unreadTileService.updateUnreadTileNow()
                    }
                }
                
                "com.naviya.launcher.action.APP_RESUME" -> {
                    // Update when app resumes
                    ProcessLifecycleOwner.get().lifecycleScope.launch {
                        unreadTileService.updateUnreadTileNow()
                    }
                }
                
                "android.provider.Telephony.SMS_RECEIVED",
                "android.intent.action.PHONE_STATE" -> {
                    // Update when new SMS is received or call state changes
                    ProcessLifecycleOwner.get().lifecycleScope.launch {
                        unreadTileService.updateUnreadTileNow()
                    }
                }
            }
        }
    }
    
    /**
     * Initialize the service and register for system events
     */
    fun initialize() {
        // Schedule periodic updates
        unreadTileService.schedulePeriodicUpdates()
        
        // Register for system events
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BOOT_COMPLETED)
            addAction(Intent.ACTION_MY_PACKAGE_REPLACED)
            addAction(Intent.ACTION_LOCKED_BOOT_COMPLETED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction("com.naviya.launcher.action.LAUNCHER_HOME_OPENED")
            addAction("com.naviya.launcher.action.APP_RESUME")
            addAction("android.provider.Telephony.SMS_RECEIVED")
            addAction("android.intent.action.PHONE_STATE")
        }
        
        context.registerReceiver(systemEventReceiver, intentFilter)
        
        // Perform initial update
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            unreadTileService.updateUnreadTileNow()
        }
    }
    
    /**
     * Clean up resources when the service is no longer needed
     */
    fun cleanup() {
        try {
            context.unregisterReceiver(systemEventReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered, ignore
        }
    }
    
    /**
     * Manually trigger an update of the unread tile
     */
    fun triggerUpdate() {
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            unreadTileService.updateUnreadTileNow()
        }
    }
    
    companion object {
        // Singleton instance
        @Volatile
        private var instance: UnreadTileServiceInitializer? = null
        
        fun getInstance(context: Context): UnreadTileServiceInitializer {
            return instance ?: synchronized(this) {
                instance ?: UnreadTileServiceInitializer(context.applicationContext).also { instance = it }
            }
        }
    }
}
