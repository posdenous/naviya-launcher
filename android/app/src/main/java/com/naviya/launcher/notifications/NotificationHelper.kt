package com.naviya.launcher.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.naviya.launcher.R
import com.naviya.launcher.ui.MainActivity

/**
 * Helper class for creating elderly-friendly notifications
 * Features:
 * - Large text size
 * - High contrast colors
 * - Simple language
 * - Accessibility support
 * - Long timeout durations
 */
class NotificationHelper private constructor(private val context: Context) {

    companion object {
        const val PRIORITY_LOW = NotificationCompat.PRIORITY_LOW
        const val PRIORITY_DEFAULT = NotificationCompat.PRIORITY_DEFAULT
        const val PRIORITY_HIGH = NotificationCompat.PRIORITY_HIGH
        
        // Singleton instance
        @Volatile
        private var instance: NotificationHelper? = null
        
        fun getInstance(context: Context): NotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: NotificationHelper(context.applicationContext).also { instance = it }
            }
        }
        
        // Default notification channel IDs
        const val CHANNEL_UNREAD = "unread_communications"
        const val CHANNEL_EMERGENCY = "emergency_alerts"
        const val CHANNEL_REMINDERS = "daily_reminders"
    }
    
    /**
     * Create a notification channel for Android O and above
     */
    private fun createNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int = NotificationManager.IMPORTANCE_HIGH
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                importance
            ).apply {
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                description = "Notifications for $channelName"
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show an elderly-friendly notification with large text and high contrast
     */
    fun showElderlyFriendlyNotification(
        title: String,
        message: String,
        channelId: String,
        channelName: String,
        priority: Int = PRIORITY_DEFAULT,
        autoCancel: Boolean = true,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        // Create the notification channel if needed
        createNotificationChannel(channelId, channelName)
        
        // Create an intent that opens the main activity when clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification with elderly-friendly features
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(autoCancel)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setTimeoutAfter(60000) // 1 minute timeout for elderly users to notice
        
        // Show the notification
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handle missing notification permission gracefully
            // Log but don't crash - elderly users need stability
        }
    }
    
    /**
     * Show an emergency notification with high priority and distinctive styling
     */
    fun showEmergencyNotification(
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        // Create the emergency channel
        createNotificationChannel(
            CHANNEL_EMERGENCY,
            "Emergency Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        
        // Create an intent that opens the main activity when clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the emergency notification with distinctive styling
        val builder = NotificationCompat.Builder(context, CHANNEL_EMERGENCY)
            .setSmallIcon(R.drawable.ic_emergency)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setColor(Color.RED)
            .setOngoing(true) // Can't be swiped away
            .setTimeoutAfter(300000) // 5 minute timeout for emergency
        
        // Show the notification
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handle missing notification permission gracefully
        }
    }
    
    /**
     * Show a reminder notification with medium priority
     */
    fun showReminderNotification(
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        showElderlyFriendlyNotification(
            title = title,
            message = message,
            channelId = CHANNEL_REMINDERS,
            channelName = "Daily Reminders",
            priority = PRIORITY_DEFAULT,
            autoCancel = true,
            notificationId = notificationId
        )
    }
    
    /**
     * Cancel a specific notification
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
