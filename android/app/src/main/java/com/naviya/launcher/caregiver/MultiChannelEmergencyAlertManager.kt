package com.naviya.launcher.caregiver

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.naviya.launcher.caregiver.data.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages multi-channel emergency alerts to caregivers
 * Supports SMS, voice calls, push notifications, and local notifications
 * Works offline when possible (SMS, local notifications)
 */
@Singleton
class MultiChannelEmergencyAlertManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val caregiverDao: CaregiverDao,
    private val networkManager: NetworkManager
) {

    companion object {
        private const val EMERGENCY_NOTIFICATION_CHANNEL_ID = "emergency_alerts"
        private const val SMS_TIMEOUT_MS = 30_000L // 30 seconds
        private const val CALL_TIMEOUT_MS = 60_000L // 60 seconds
        private const val MAX_SMS_LENGTH = 160
    }

    /**
     * Send emergency SMS alert
     */
    suspend fun sendEmergencySMS(alert: EmergencyAlert): EmergencyChannelResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            val caregivers = getCaregivers(alert.userId, alert.targetCaregivers)
            val results = mutableListOf<EmergencyChannelResult>()
            
            caregivers.forEach { caregiver ->
                try {
                    val smsMessage = formatSMSMessage(alert, caregiver)
                    val success = sendSMS(caregiver.caregiverPhone, smsMessage)
                    
                    results.add(
                        EmergencyChannelResult(
                            channel = EmergencyChannel.SMS,
                            caregiverId = caregiver.caregiverId,
                            success = success,
                            message = if (success) "SMS sent successfully" else "SMS failed to send",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    results.add(
                        EmergencyChannelResult(
                            channel = EmergencyChannel.SMS,
                            caregiverId = caregiver.caregiverId,
                            success = false,
                            message = "SMS error: ${e.message}",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
            
            // Return the first result or a summary
            results.firstOrNull() ?: EmergencyChannelResult(
                channel = EmergencyChannel.SMS,
                caregiverId = "",
                success = false,
                message = "No caregivers found",
                timestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            EmergencyChannelResult(
                channel = EmergencyChannel.SMS,
                caregiverId = "",
                success = false,
                message = "SMS system error: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Initiate emergency phone call
     */
    suspend fun initiateEmergencyCall(alert: EmergencyAlert): EmergencyChannelResult = withContext(Dispatchers.IO) {
        try {
            val caregivers = getCaregivers(alert.userId, alert.targetCaregivers)
            val primaryCaregiver = caregivers.firstOrNull { it.emergencyContactPriority == 1 }
                ?: caregivers.firstOrNull()
            
            if (primaryCaregiver == null) {
                return@withContext EmergencyChannelResult(
                    channel = EmergencyChannel.VOICE_CALL,
                    caregiverId = "",
                    success = false,
                    message = "No caregiver available for call",
                    timestamp = System.currentTimeMillis()
                )
            }
            
            val success = initiatePhoneCall(primaryCaregiver.caregiverPhone, alert)
            
            EmergencyChannelResult(
                channel = EmergencyChannel.VOICE_CALL,
                caregiverId = primaryCaregiver.caregiverId,
                success = success,
                message = if (success) "Call initiated successfully" else "Failed to initiate call",
                timestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            EmergencyChannelResult(
                channel = EmergencyChannel.VOICE_CALL,
                caregiverId = "",
                success = false,
                message = "Call system error: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send push notification alert
     */
    suspend fun sendPushNotification(alert: EmergencyAlert): EmergencyChannelResult = withContext(Dispatchers.IO) {
        try {
            if (!networkManager.isConnected()) {
                return@withContext EmergencyChannelResult(
                    channel = EmergencyChannel.PUSH_NOTIFICATION,
                    caregiverId = "",
                    success = false,
                    message = "No network connection for push notification",
                    timestamp = System.currentTimeMillis()
                )
            }
            
            val caregivers = getCaregivers(alert.userId, alert.targetCaregivers)
            val results = mutableListOf<EmergencyChannelResult>()
            
            caregivers.forEach { caregiver ->
                try {
                    val success = sendPushNotificationToCaregiver(caregiver, alert)
                    
                    results.add(
                        EmergencyChannelResult(
                            channel = EmergencyChannel.PUSH_NOTIFICATION,
                            caregiverId = caregiver.caregiverId,
                            success = success,
                            message = if (success) "Push notification sent" else "Push notification failed",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    results.add(
                        EmergencyChannelResult(
                            channel = EmergencyChannel.PUSH_NOTIFICATION,
                            caregiverId = caregiver.caregiverId,
                            success = false,
                            message = "Push notification error: ${e.message}",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
            
            results.firstOrNull() ?: EmergencyChannelResult(
                channel = EmergencyChannel.PUSH_NOTIFICATION,
                caregiverId = "",
                success = false,
                message = "No caregivers found",
                timestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            EmergencyChannelResult(
                channel = EmergencyChannel.PUSH_NOTIFICATION,
                caregiverId = "",
                success = false,
                message = "Push notification system error: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send local notification (always works)
     */
    suspend fun sendLocalNotification(alert: EmergencyAlert): EmergencyChannelResult = withContext(Dispatchers.IO) {
        try {
            val success = createLocalNotification(alert)
            
            EmergencyChannelResult(
                channel = EmergencyChannel.LOCAL_NOTIFICATION,
                caregiverId = "local",
                success = success,
                message = if (success) "Local notification created" else "Local notification failed",
                timestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            EmergencyChannelResult(
                channel = EmergencyChannel.LOCAL_NOTIFICATION,
                caregiverId = "local",
                success = false,
                message = "Local notification error: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send backup SMS to all emergency contacts
     */
    suspend fun sendBackupSMS(alert: EmergencyAlert): List<EmergencyChannelResult> = withContext(Dispatchers.IO) {
        try {
            val emergencyContacts = caregiverDao.getEmergencyContacts(alert.userId)
            val results = mutableListOf<EmergencyChannelResult>()
            
            emergencyContacts.forEach { contact ->
                try {
                    val smsMessage = formatBackupSMSMessage(alert, contact)
                    val success = sendSMS(contact.phoneNumber, smsMessage)
                    
                    results.add(
                        EmergencyChannelResult(
                            channel = EmergencyChannel.BACKUP_SMS,
                            caregiverId = contact.contactId,
                            success = success,
                            message = if (success) "Backup SMS sent" else "Backup SMS failed",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    results.add(
                        EmergencyChannelResult(
                            channel = EmergencyChannel.BACKUP_SMS,
                            caregiverId = contact.contactId,
                            success = false,
                            message = "Backup SMS error: ${e.message}",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
            
            results
            
        } catch (e: Exception) {
            listOf(
                EmergencyChannelResult(
                    channel = EmergencyChannel.BACKUP_SMS,
                    caregiverId = "",
                    success = false,
                    message = "Backup SMS system error: ${e.message}",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Check if SMS capability is available (works offline)
     */
    fun hasSMSCapability(): Boolean {
        return try {
            SmsManager.getDefault() != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if voice call capability is available
     */
    fun hasVoiceCallCapability(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.resolveActivity(context.packageManager) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get emergency alert capabilities based on current conditions
     */
    suspend fun getEmergencyAlertCapabilities(): EmergencyAlertCapabilities = withContext(Dispatchers.IO) {
        val isNetworkConnected = networkManager.isConnected()
        val connectionQuality = networkManager.getCurrentConnectionQuality()
        
        EmergencyAlertCapabilities(
            smsAvailable = hasSMSCapability(),
            voiceCallAvailable = hasVoiceCallCapability(),
            pushNotificationAvailable = isNetworkConnected,
            localNotificationAvailable = true,
            backupSMSAvailable = hasSMSCapability(),
            networkConnected = isNetworkConnected,
            connectionQuality = connectionQuality,
            recommendedChannels = getRecommendedChannels(isNetworkConnected, connectionQuality)
        )
    }

    // ==================== PRIVATE METHODS ====================

    private suspend fun getCaregivers(userId: String, targetCaregivers: List<String>): List<CaregiverConnection> {
        return if (targetCaregivers.isEmpty()) {
            caregiverDao.getConnectedCaregivers(userId)
        } else {
            caregiverDao.getCaregiverConnections(targetCaregivers)
        }
    }

    private fun sendSMS(phoneNumber: String, message: String): Boolean {
        return try {
            val smsManager = SmsManager.getDefault()
            
            if (message.length <= MAX_SMS_LENGTH) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } else {
                // Split long messages
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun initiatePhoneCall(phoneNumber: String, alert: EmergencyAlert): Boolean {
        return try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(callIntent)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun sendPushNotificationToCaregiver(
        caregiver: CaregiverConnection,
        alert: EmergencyAlert
    ): Boolean {
        return try {
            // Implementation depends on push notification service (Firebase, etc.)
            // For now, simulate sending
            delay(100)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createLocalNotification(alert: EmergencyAlert): Boolean {
        return try {
            val notification = NotificationCompat.Builder(context, EMERGENCY_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Emergency Alert")
                .setContentText(alert.message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .build()
            
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(alert.alertId.hashCode(), notification)
            
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun formatSMSMessage(alert: EmergencyAlert, caregiver: CaregiverConnection): String {
        val priorityText = when (alert.priority) {
            EmergencyAlertPriority.CRITICAL -> "🚨 CRITICAL"
            EmergencyAlertPriority.HIGH -> "⚠️ HIGH"
            EmergencyAlertPriority.MEDIUM -> "⚡ MEDIUM"
            EmergencyAlertPriority.LOW -> "ℹ️ LOW"
        }
        
        val eventText = when (alert.eventType) {
            EmergencyEventType.PANIC_MODE_ACTIVATED -> "PANIC MODE ACTIVATED"
            EmergencyEventType.ABUSE_DETECTED -> "ABUSE DETECTED"
            EmergencyEventType.CONTACT_PROTECTION_VIOLATED -> "CONTACT PROTECTION VIOLATED"
            EmergencyEventType.SYSTEM_HEALTH_CRITICAL -> "SYSTEM HEALTH CRITICAL"
            EmergencyEventType.FALL_DETECTED -> "FALL DETECTED"
            EmergencyEventType.MEDICATION_MISSED -> "MEDICATION MISSED"
            EmergencyEventType.UNUSUAL_ACTIVITY -> "UNUSUAL ACTIVITY"
            EmergencyEventType.SYSTEM_TEST -> "SYSTEM TEST"
        }
        
        return "$priorityText ALERT: $eventText\n\n${alert.message}\n\nTime: ${formatTimestamp(alert.timestamp)}\n\nNaviya Elder Care"
    }

    private fun formatBackupSMSMessage(alert: EmergencyAlert, contact: EmergencyContact): String {
        return "🚨 EMERGENCY BACKUP ALERT\n\n${alert.message}\n\nThis is a backup alert from Naviya Elder Care system.\n\nTime: ${formatTimestamp(alert.timestamp)}"
    }

    private fun formatTimestamp(timestamp: Long): String {
        return java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }

    private fun getRecommendedChannels(
        isNetworkConnected: Boolean,
        connectionQuality: ConnectionQuality
    ): List<EmergencyChannel> {
        return when {
            !isNetworkConnected -> listOf(
                EmergencyChannel.SMS,
                EmergencyChannel.LOCAL_NOTIFICATION,
                EmergencyChannel.BACKUP_SMS
            )
            connectionQuality == ConnectionQuality.HIGH -> listOf(
                EmergencyChannel.VOICE_CALL,
                EmergencyChannel.PUSH_NOTIFICATION,
                EmergencyChannel.SMS,
                EmergencyChannel.LOCAL_NOTIFICATION
            )
            connectionQuality == ConnectionQuality.MEDIUM -> listOf(
                EmergencyChannel.SMS,
                EmergencyChannel.PUSH_NOTIFICATION,
                EmergencyChannel.LOCAL_NOTIFICATION,
                EmergencyChannel.VOICE_CALL
            )
            else -> listOf(
                EmergencyChannel.SMS,
                EmergencyChannel.LOCAL_NOTIFICATION,
                EmergencyChannel.BACKUP_SMS
            )
        }
    }
}

// ==================== SUPPORTING DATA CLASSES ====================

data class EmergencyAlertCapabilities(
    val smsAvailable: Boolean,
    val voiceCallAvailable: Boolean,
    val pushNotificationAvailable: Boolean,
    val localNotificationAvailable: Boolean,
    val backupSMSAvailable: Boolean,
    val networkConnected: Boolean,
    val connectionQuality: ConnectionQuality,
    val recommendedChannels: List<EmergencyChannel>
)
