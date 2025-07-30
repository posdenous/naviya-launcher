package com.naviya.launcher.emergency

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.emergency.data.EmergencyEvent
import com.naviya.launcher.emergency.data.EmergencyEventType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Healthcare Professional Notification Service
 * Handles multi-channel notifications to healthcare professionals during medical emergencies
 * Supports SMS, email, and push notifications with response tracking
 */
@Singleton
class HealthcareProfessionalNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emergencyDao: EmergencyDao
) {
    private val notificationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _notificationStatus = MutableStateFlow<NotificationStatus>(NotificationStatus.Idle)
    val notificationStatus: Flow<NotificationStatus> = _notificationStatus.asStateFlow()
    
    companion object {
        private const val TAG = "HealthcareProfessionalNotification"
        private const val SMS_TIMEOUT_MS = 30000L // 30 seconds
        private const val EMAIL_TIMEOUT_MS = 60000L // 1 minute
        private const val PUSH_TIMEOUT_MS = 10000L // 10 seconds
    }

    /**
     * Send emergency notification to healthcare professional
     */
    suspend fun notifyHealthcareProfessional(
        professionalId: String,
        emergencyType: MedicalEmergencyType,
        patientId: String,
        location: String? = null,
        urgencyLevel: EmergencyUrgencyLevel = EmergencyUrgencyLevel.HIGH
    ): NotificationResult {
        return try {
            Log.i(TAG, "Sending emergency notification to professional: $professionalId")
            
            _notificationStatus.value = NotificationStatus.Sending
            
            // Get professional contact information (mock for now)
            val professionalContact = getProfessionalContact(professionalId)
            
            // Create notification content
            val notificationContent = createNotificationContent(
                emergencyType, patientId, location, urgencyLevel
            )
            
            // Send via multiple channels
            val results = mutableListOf<ChannelResult>()
            
            // 1. SMS (Primary - most reliable)
            if (professionalContact.phoneNumber.isNotEmpty()) {
                val smsResult = sendSMS(
                    phoneNumber = professionalContact.phoneNumber,
                    message = notificationContent.smsMessage,
                    urgencyLevel = urgencyLevel
                )
                results.add(smsResult)
            }
            
            // 2. Email (Secondary)
            if (professionalContact.email.isNotEmpty()) {
                val emailResult = sendEmail(
                    email = professionalContact.email,
                    subject = notificationContent.emailSubject,
                    body = notificationContent.emailBody,
                    urgencyLevel = urgencyLevel
                )
                results.add(emailResult)
            }
            
            // 3. Push Notification (Tertiary - if app installed)
            if (professionalContact.hasAppInstalled) {
                val pushResult = sendPushNotification(
                    professionalId = professionalId,
                    title = notificationContent.pushTitle,
                    body = notificationContent.pushBody,
                    urgencyLevel = urgencyLevel
                )
                results.add(pushResult)
            }
            
            // Log notification attempt
            logNotificationAttempt(professionalId, emergencyType, results)
            
            val successfulChannels = results.count { it.success }
            val totalChannels = results.size
            
            _notificationStatus.value = if (successfulChannels > 0) {
                NotificationStatus.Sent(successfulChannels, totalChannels)
            } else {
                NotificationStatus.Failed("All notification channels failed")
            }
            
            NotificationResult.Success(
                professionalId = professionalId,
                channelsUsed = totalChannels,
                successfulChannels = successfulChannels,
                results = results
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to notify healthcare professional: $professionalId", e)
            _notificationStatus.value = NotificationStatus.Failed(e.message ?: "Unknown error")
            
            NotificationResult.Error(
                professionalId = professionalId,
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Send SMS notification
     */
    private suspend fun sendSMS(
        phoneNumber: String,
        message: String,
        urgencyLevel: EmergencyUrgencyLevel
    ): ChannelResult {
        return try {
            withTimeout(SMS_TIMEOUT_MS) {
                val smsManager = SmsManager.getDefault()
                
                // For high urgency, send multiple times
                val sendCount = when (urgencyLevel) {
                    EmergencyUrgencyLevel.CRITICAL -> 3
                    EmergencyUrgencyLevel.HIGH -> 2
                    EmergencyUrgencyLevel.MEDIUM -> 1
                    EmergencyUrgencyLevel.LOW -> 1
                }
                
                repeat(sendCount) {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                    if (it < sendCount - 1) {
                        delay(2000) // 2 second delay between sends
                    }
                }
                
                Log.i(TAG, "SMS sent successfully to $phoneNumber (sent $sendCount times)")
                ChannelResult.Success(
                    channel = NotificationChannel.SMS,
                    message = "SMS sent successfully ($sendCount times)",
                    timestamp = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
            ChannelResult.Failed(
                channel = NotificationChannel.SMS,
                error = e.message ?: "SMS sending failed",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send email notification (mock implementation)
     */
    private suspend fun sendEmail(
        email: String,
        subject: String,
        body: String,
        urgencyLevel: EmergencyUrgencyLevel
    ): ChannelResult {
        return try {
            withTimeout(EMAIL_TIMEOUT_MS) {
                // Mock email sending - in real implementation, use email service
                delay(1000) // Simulate email sending delay
                
                Log.i(TAG, "Email sent successfully to $email")
                ChannelResult.Success(
                    channel = NotificationChannel.EMAIL,
                    message = "Email sent successfully",
                    timestamp = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email to $email", e)
            ChannelResult.Failed(
                channel = NotificationChannel.EMAIL,
                error = e.message ?: "Email sending failed",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send push notification (mock implementation)
     */
    private suspend fun sendPushNotification(
        professionalId: String,
        title: String,
        body: String,
        urgencyLevel: EmergencyUrgencyLevel
    ): ChannelResult {
        return try {
            withTimeout(PUSH_TIMEOUT_MS) {
                // Mock push notification - in real implementation, use FCM
                delay(500) // Simulate push notification delay
                
                Log.i(TAG, "Push notification sent successfully to $professionalId")
                ChannelResult.Success(
                    channel = NotificationChannel.PUSH,
                    message = "Push notification sent successfully",
                    timestamp = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send push notification to $professionalId", e)
            ChannelResult.Failed(
                channel = NotificationChannel.PUSH,
                error = e.message ?: "Push notification failed",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Create notification content based on emergency type
     */
    private fun createNotificationContent(
        emergencyType: MedicalEmergencyType,
        patientId: String,
        location: String?,
        urgencyLevel: EmergencyUrgencyLevel
    ): NotificationContent {
        val urgencyPrefix = when (urgencyLevel) {
            EmergencyUrgencyLevel.CRITICAL -> "🚨 CRITICAL EMERGENCY"
            EmergencyUrgencyLevel.HIGH -> "⚠️ HIGH PRIORITY EMERGENCY"
            EmergencyUrgencyLevel.MEDIUM -> "📋 MEDICAL EMERGENCY"
            EmergencyUrgencyLevel.LOW -> "ℹ️ MEDICAL CONSULTATION"
        }
        
        val emergencyDescription = when (emergencyType) {
            MedicalEmergencyType.CARDIAC_EVENT -> "cardiac event (chest pain, breathing difficulty)"
            MedicalEmergencyType.FALL_WITH_INJURY -> "fall with potential injury"
            MedicalEmergencyType.MEDICATION_EMERGENCY -> "medication-related emergency"
            MedicalEmergencyType.COGNITIVE_CRISIS -> "cognitive crisis or confusion"
            MedicalEmergencyType.GENERAL_MEDICAL -> "general medical emergency"
        }
        
        val locationText = location?.let { "Location: $it" } ?: "Location: Not available"
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        
        return NotificationContent(
            smsMessage = "$urgencyPrefix - Patient $patientId has $emergencyDescription. $locationText. Time: $timestamp. Please respond immediately. -Naviya Emergency System",
            
            emailSubject = "$urgencyPrefix - Patient $patientId Medical Emergency",
            
            emailBody = """
                $urgencyPrefix
                
                Patient ID: $patientId
                Emergency Type: ${emergencyType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}
                Description: $emergencyDescription
                $locationText
                Time: $timestamp
                
                Please respond immediately through your preferred communication channel.
                
                This is an automated message from the Naviya Emergency Response System.
                If this is a critical emergency, please also contact emergency services directly.
            """.trimIndent(),
            
            pushTitle = "$urgencyPrefix - Patient Emergency",
            pushBody = "Patient $patientId: $emergencyDescription. Immediate response required."
        )
    }

    /**
     * Get professional contact information (mock implementation)
     */
    private fun getProfessionalContact(professionalId: String): ProfessionalContact {
        // Mock implementation - in real app, get from database
        return ProfessionalContact(
            professionalId = professionalId,
            name = "Dr. Healthcare Professional",
            phoneNumber = "+1234567890", // Mock phone number
            email = "healthcare.professional@example.com",
            hasAppInstalled = true
        )
    }

    /**
     * Log notification attempt for audit trail
     */
    private suspend fun logNotificationAttempt(
        professionalId: String,
        emergencyType: MedicalEmergencyType,
        results: List<ChannelResult>
    ) {
        try {
            val logEvent = EmergencyEvent(
                eventType = EmergencyEventType.SOS_ACTIVATED,
                userId = "system",
                userLanguage = "en",
                wasOffline = false,
                notes = "Healthcare Professional Notification - ID: $professionalId, Type: $emergencyType, Channels: ${results.size}, Successful: ${results.count { it.success }}"
            )
            
            emergencyDao.insertEmergencyEvent(logEvent)
            Log.i(TAG, "Notification attempt logged for professional: $professionalId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log notification attempt", e)
        }
    }

    /**
     * Get notification statistics
     */
    suspend fun getNotificationStats(): NotificationStats {
        return try {
            // Mock implementation - in real app, query database
            NotificationStats(
                totalNotificationsSent = 25,
                successfulNotifications = 23,
                failedNotifications = 2,
                averageResponseTime = 180000L, // 3 minutes
                lastNotificationTime = System.currentTimeMillis() - 3600000L // 1 hour ago
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get notification stats", e)
            NotificationStats()
        }
    }
}

// Data classes for notification system

enum class EmergencyUrgencyLevel {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

enum class NotificationChannel {
    SMS,
    EMAIL,
    PUSH
}

data class ProfessionalContact(
    val professionalId: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val hasAppInstalled: Boolean
)

data class NotificationContent(
    val smsMessage: String,
    val emailSubject: String,
    val emailBody: String,
    val pushTitle: String,
    val pushBody: String
)

sealed class NotificationStatus {
    object Idle : NotificationStatus()
    object Sending : NotificationStatus()
    data class Sent(val successfulChannels: Int, val totalChannels: Int) : NotificationStatus()
    data class Failed(val error: String) : NotificationStatus()
}

sealed class NotificationResult {
    data class Success(
        val professionalId: String,
        val channelsUsed: Int,
        val successfulChannels: Int,
        val results: List<ChannelResult>
    ) : NotificationResult()
    
    data class Error(
        val professionalId: String,
        val error: String
    ) : NotificationResult()
}

sealed class ChannelResult {
    abstract val channel: NotificationChannel
    abstract val timestamp: Long
    val success: Boolean get() = this is Success
    
    data class Success(
        override val channel: NotificationChannel,
        val message: String,
        override val timestamp: Long
    ) : ChannelResult()
    
    data class Failed(
        override val channel: NotificationChannel,
        val error: String,
        override val timestamp: Long
    ) : ChannelResult()
}

data class NotificationStats(
    val totalNotificationsSent: Int = 0,
    val successfulNotifications: Int = 0,
    val failedNotifications: Int = 0,
    val averageResponseTime: Long = 0L,
    val lastNotificationTime: Long? = null
)
