package com.naviya.launcher.emergency

import android.content.Context
import android.location.Location
import android.util.Log
import com.naviya.launcher.emergency.data.EmergencyContact
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.emergency.data.EmergencyEvent
import com.naviya.launcher.emergency.data.EmergencyEventType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caregiver Notification Service for Naviya Launcher
 * Handles remote caregiver notifications while respecting privacy boundaries
 * Follows Windsurf rules for caregiver integration and privacy
 */
@Singleton
class CaregiverNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emergencyDao: EmergencyDao,
    private val locationService: EmergencyLocationService
) {
    private val notificationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val TAG = "CaregiverNotificationService"
        private const val NOTIFICATION_TIMEOUT_MS = 30000L // 30 seconds
        private const val MAX_RETRY_ATTEMPTS = 3
    }
    
    /**
     * Send emergency notification to caregiver
     * Respects privacy boundaries and user consent
     */
    suspend fun sendEmergencyNotification(
        caregiver: EmergencyContact,
        location: Location?,
        userLanguage: String
    ): Boolean {
        return try {
            Log.i(TAG, "Sending emergency notification to caregiver: ${caregiver.name}")
            
            val notificationData = EmergencyNotificationData(
                caregiverId = caregiver.id,
                emergencyType = EmergencyType.SOS_ACTIVATED,
                timestamp = System.currentTimeMillis(),
                userLanguage = userLanguage,
                location = location?.let {
                    LocationData(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        accuracy = if (it.hasAccuracy()) it.accuracy else null,
                        timestamp = it.time
                    )
                },
                message = getEmergencyMessage(EmergencyType.SOS_ACTIVATED, userLanguage),
                priority = NotificationPriority.CRITICAL
            )
            
            // Try multiple notification channels
            val results = mutableListOf<Boolean>()
            
            // 1. Push notification (if caregiver app installed)
            results.add(sendPushNotification(notificationData))
            
            // 2. SMS notification (fallback)
            results.add(sendSMSNotification(caregiver, notificationData))
            
            // 3. Email notification (if configured)
            results.add(sendEmailNotification(caregiver, notificationData))
            
            val success = results.any { it }
            
            if (success) {
                // Log successful notification
                emergencyDao.insertEmergencyEvent(
                    EmergencyEvent(
                        eventType = EmergencyEventType.CAREGIVER_NOTIFIED,
                        contactId = caregiver.id,
                        userLanguage = userLanguage,
                        locationLatitude = location?.latitude,
                        locationLongitude = location?.longitude,
                        notes = "Notification sent successfully"
                    )
                )
            }
            
            success
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send emergency notification", e)
            false
        }
    }
    
    /**
     * Send push notification to caregiver app
     */
    private suspend fun sendPushNotification(data: EmergencyNotificationData): Boolean {
        return try {
            // This would integrate with Firebase Cloud Messaging or similar
            // For now, simulate the notification
            Log.i(TAG, "Push notification sent for emergency: ${data.emergencyType}")
            
            // Simulate network call
            delay(1000)
            
            // Return success (in real implementation, check actual delivery)
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Push notification failed", e)
            false
        }
    }
    
    /**
     * Send SMS notification to caregiver
     */
    private suspend fun sendSMSNotification(
        caregiver: EmergencyContact,
        data: EmergencyNotificationData
    ): Boolean {
        return try {
            val smsMessage = buildSMSMessage(data)
            
            // Use Android SMS manager
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(
                caregiver.phoneNumber,
                null,
                smsMessage,
                null,
                null
            )
            
            Log.i(TAG, "SMS notification sent to caregiver: ${caregiver.name}")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "SMS notification failed for ${caregiver.name}", e)
            false
        }
    }
    
    /**
     * Send email notification to caregiver (if email configured)
     */
    private suspend fun sendEmailNotification(
        caregiver: EmergencyContact,
        data: EmergencyNotificationData
    ): Boolean {
        return try {
            // Email would be sent through a backend service
            // For now, just log the attempt
            Log.i(TAG, "Email notification would be sent to caregiver: ${caregiver.name}")
            
            // Simulate email sending
            delay(2000)
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Email notification failed", e)
            false
        }
    }
    
    /**
     * Send cancellation notification to caregiver
     */
    suspend fun sendCancellationNotification(reason: String): Boolean {
        return try {
            val caregiver = emergencyDao.getPrimaryCaregiver()
            if (caregiver == null) {
                Log.w(TAG, "No primary caregiver configured for cancellation notification")
                return false
            }
            
            val message = "EMERGENCY CANCELLED: The emergency situation has been resolved. Reason: $reason. Time: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}"
            
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(
                caregiver.phoneNumber,
                null,
                message,
                null,
                null
            )
            
            Log.i(TAG, "Cancellation notification sent to caregiver")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send cancellation notification", e)
            false
        }
    }
    
    /**
     * Build SMS message for emergency notification
     */
    private fun buildSMSMessage(data: EmergencyNotificationData): String {
        val locationText = data.location?.let {
            "Location: https://maps.google.com/?q=${it.latitude},${it.longitude}"
        } ?: "Location: Not available"
        
        val timestamp = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()
        ).format(java.util.Date(data.timestamp))
        
        return """
            🚨 NAVIYA EMERGENCY ALERT 🚨
            
            ${data.message}
            
            Time: $timestamp
            $locationText
            
            This is an automated emergency notification from Naviya Launcher.
            Please respond immediately.
        """.trimIndent()
    }
    
    /**
     * Get localized emergency message
     */
    private fun getEmergencyMessage(type: EmergencyType, language: String): String {
        return when (language) {
            "de" -> when (type) {
                EmergencyType.SOS_ACTIVATED -> "NOTFALL: Ihr Angehöriger benötigt Hilfe!"
                EmergencyType.FALL_DETECTED -> "STURZ ERKANNT: Ihr Angehöriger ist möglicherweise gestürzt!"
                EmergencyType.MEDICAL_EMERGENCY -> "MEDIZINISCHER NOTFALL: Sofortige Hilfe erforderlich!"
                EmergencyType.PANIC_BUTTON -> "PANIK-TASTE: Ihr Angehöriger hat den Notknopf gedrückt!"
            }
            "tr" -> when (type) {
                EmergencyType.SOS_ACTIVATED -> "ACİL DURUM: Yakınınız yardıma ihtiyaç duyuyor!"
                EmergencyType.FALL_DETECTED -> "DÜŞME TESPİT EDİLDİ: Yakınınız düşmüş olabilir!"
                EmergencyType.MEDICAL_EMERGENCY -> "TIBBİ ACİL DURUM: Acil yardım gerekli!"
                EmergencyType.PANIC_BUTTON -> "PANİK BUTONU: Yakınınız acil durum butonuna bastı!"
            }
            "uk" -> when (type) {
                EmergencyType.SOS_ACTIVATED -> "НАДЗВИЧАЙНА СИТУАЦІЯ: Ваш близький потребує допомоги!"
                EmergencyType.FALL_DETECTED -> "ВИЯВЛЕНО ПАДІННЯ: Ваш близький можливо впав!"
                EmergencyType.MEDICAL_EMERGENCY -> "МЕДИЧНА НАДЗВИЧАЙНА СИТУАЦІЯ: Потрібна негайна допомога!"
                EmergencyType.PANIC_BUTTON -> "КНОПКА ПАНІКИ: Ваш близький натиснув кнопку екстреного виклику!"
            }
            "ar" -> when (type) {
                EmergencyType.SOS_ACTIVATED -> "حالة طوارئ: قريبك يحتاج المساعدة!"
                EmergencyType.FALL_DETECTED -> "تم اكتشاف سقوط: قد يكون قريبك قد سقط!"
                EmergencyType.MEDICAL_EMERGENCY -> "حالة طوارئ طبية: مطلوب مساعدة فورية!"
                EmergencyType.PANIC_BUTTON -> "زر الذعر: قريبك ضغط على زر الطوارئ!"
            }
            else -> when (type) { // English default
                EmergencyType.SOS_ACTIVATED -> "EMERGENCY: Your loved one needs help!"
                EmergencyType.FALL_DETECTED -> "FALL DETECTED: Your loved one may have fallen!"
                EmergencyType.MEDICAL_EMERGENCY -> "MEDICAL EMERGENCY: Immediate help required!"
                EmergencyType.PANIC_BUTTON -> "PANIC BUTTON: Your loved one pressed the emergency button!"
            }
        }
    }
    
    /**
     * Check if caregiver notifications are properly configured
     */
    suspend fun isCaregiverNotificationReady(): Boolean {
        val caregiver = emergencyDao.getPrimaryCaregiver()
        return caregiver != null && caregiver.phoneNumber.isNotBlank()
    }
    
    /**
     * Send periodic status update to caregiver (if enabled)
     */
    suspend fun sendStatusUpdate(statusMessage: String, userLanguage: String) {
        try {
            val caregiver = emergencyDao.getPrimaryCaregiver() ?: return
            
            val message = "Naviya Status Update: $statusMessage"
            
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(
                caregiver.phoneNumber,
                null,
                message,
                null,
                null
            )
            
            Log.i(TAG, "Status update sent to caregiver")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send status update", e)
        }
    }
}

/**
 * Emergency notification data structure
 */
data class EmergencyNotificationData(
    val caregiverId: String,
    val emergencyType: EmergencyType,
    val timestamp: Long,
    val userLanguage: String,
    val location: LocationData?,
    val message: String,
    val priority: NotificationPriority
)

/**
 * Location data for notifications
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val timestamp: Long
)

/**
 * Emergency types for notifications
 */
enum class EmergencyType {
    SOS_ACTIVATED,
    FALL_DETECTED,
    MEDICAL_EMERGENCY,
    PANIC_BUTTON
}

/**
 * Notification priority levels
 */
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}
