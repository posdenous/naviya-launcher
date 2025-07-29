package com.naviya.launcher.elderrights

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import com.naviya.launcher.abuse.data.*
import com.naviya.launcher.contacts.data.ContactDao
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.notifications.NotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for contacting elder rights advocates and support organizations
 * Provides multiple communication channels and escalation procedures
 */
@Singleton
class ElderRightsAdvocateService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactDao: ContactDao,
    private val emergencyDao: EmergencyDao,
    private val notificationService: NotificationService,
    private val abuseDao: AbuseDetectionDao
) {

    companion object {
        // National Elder Rights Organizations
        private const val NATIONAL_ELDER_ABUSE_HOTLINE = "1-800-677-1116"
        private const val ELDER_JUSTICE_HOTLINE = "1-833-372-8311"
        private const val ADULT_PROTECTIVE_SERVICES = "1-800-490-8505"
        
        // Emergency escalation thresholds
        private const val CRITICAL_ALERT_THRESHOLD = 3 // 3+ critical alerts trigger emergency escalation
        private const val ESCALATION_TIME_WINDOW = TimeUnit.HOURS.toMillis(24) // 24 hours
    }

    /**
     * Notify elder rights advocate about abuse alert
     * Uses multiple communication channels for reliability
     */
    suspend fun notifyElderRightsAdvocate(
        userId: String,
        alertId: String,
        message: String,
        priority: String = "MEDIUM"
    ): ElderRightsNotificationResult = withContext(Dispatchers.IO) {
        try {
            // Get user's designated elder rights advocate
            val advocate = getDesignatedAdvocate(userId)
            
            // Create notification record
            val notification = ElderRightsNotification(
                notificationId = "elder-rights-${System.nanoTime()}",
                userId = userId,
                alertId = alertId,
                advocateContact = advocate,
                message = message,
                priority = NotificationPriority.valueOf(priority),
                timestamp = System.currentTimeMillis(),
                channels = mutableListOf(),
                status = NotificationStatus.PENDING
            )
            
            // Send notifications through multiple channels
            val results = mutableListOf<ChannelResult>()
            
            when (NotificationPriority.valueOf(priority)) {
                NotificationPriority.IMMEDIATE -> {
                    // Critical situation - use all channels immediately
                    results.addAll(sendImmediateNotifications(notification))
                    
                    // Also contact emergency services if escalation criteria met
                    if (shouldEscalateToEmergencyServices(userId, alertId)) {
                        results.add(contactEmergencyServices(notification))
                    }
                }
                NotificationPriority.HIGH -> {
                    // High priority - phone call + SMS + app notification
                    results.addAll(sendHighPriorityNotifications(notification))
                }
                NotificationPriority.MEDIUM -> {
                    // Medium priority - SMS + app notification + email
                    results.addAll(sendMediumPriorityNotifications(notification))
                }
                NotificationPriority.LOW -> {
                    // Low priority - app notification + email
                    results.addAll(sendLowPriorityNotifications(notification))
                }
            }
            
            // Update notification with results
            notification.channels = results
            notification.status = if (results.any { it.success }) {
                NotificationStatus.SENT
            } else {
                NotificationStatus.FAILED
            }
            
            // Store notification record
            emergencyDao.insertElderRightsNotification(notification)
            
            // Update abuse alert with notification info
            abuseDao.insertAlertNotification(
                AbuseAlertNotification(
                    alertId = alertId,
                    notificationType = "ELDER_RIGHTS_ADVOCATE",
                    recipient = advocate.name,
                    message = message,
                    sentTimestamp = System.currentTimeMillis(),
                    deliveryStatus = notification.status.name
                )
            )
            
            ElderRightsNotificationResult(
                success = notification.status == NotificationStatus.SENT,
                notificationId = notification.notificationId,
                channelsUsed = results.map { it.channel },
                message = if (notification.status == NotificationStatus.SENT) {
                    "Elder rights advocate notified successfully"
                } else {
                    "Failed to notify elder rights advocate"
                }
            )
            
        } catch (e: Exception) {
            ElderRightsNotificationResult(
                success = false,
                notificationId = null,
                channelsUsed = emptyList(),
                message = "Error notifying elder rights advocate: ${e.message}"
            )
        }
    }

    /**
     * Send immediate notifications for critical situations
     */
    private suspend fun sendImmediateNotifications(
        notification: ElderRightsNotification
    ): List<ChannelResult> {
        val results = mutableListOf<ChannelResult>()
        
        // 1. Automated phone call
        results.add(initiateEmergencyCall(notification))
        
        // 2. SMS with location and details
        results.add(sendEmergencySMS(notification))
        
        // 3. Push notification
        results.add(sendPushNotification(notification))
        
        // 4. Email with full details
        results.add(sendEmergencyEmail(notification))
        
        // 5. Contact backup advocates
        results.addAll(contactBackupAdvocates(notification))
        
        return results
    }

    /**
     * Send high priority notifications
     */
    private suspend fun sendHighPriorityNotifications(
        notification: ElderRightsNotification
    ): List<ChannelResult> {
        val results = mutableListOf<ChannelResult>()
        
        // 1. Phone call
        results.add(initiatePhoneCall(notification))
        
        // 2. SMS
        results.add(sendSMS(notification))
        
        // 3. Push notification
        results.add(sendPushNotification(notification))
        
        return results
    }

    /**
     * Send medium priority notifications
     */
    private suspend fun sendMediumPriorityNotifications(
        notification: ElderRightsNotification
    ): List<ChannelResult> {
        val results = mutableListOf<ChannelResult>()
        
        // 1. SMS
        results.add(sendSMS(notification))
        
        // 2. Push notification
        results.add(sendPushNotification(notification))
        
        // 3. Email
        results.add(sendEmail(notification))
        
        return results
    }

    /**
     * Send low priority notifications
     */
    private suspend fun sendLowPriorityNotifications(
        notification: ElderRightsNotification
    ): List<ChannelResult> {
        val results = mutableListOf<ChannelResult>()
        
        // 1. Push notification
        results.add(sendPushNotification(notification))
        
        // 2. Email
        results.add(sendEmail(notification))
        
        return results
    }

    /**
     * Initiate emergency phone call
     */
    private suspend fun initiateEmergencyCall(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val phoneNumber = notification.advocateContact.phoneNumber
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(callIntent)
            
            ChannelResult(
                channel = NotificationChannel.PHONE_CALL,
                success = true,
                message = "Emergency call initiated to ${notification.advocateContact.name}",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            // Fallback to national hotline
            initiateNationalHotlineCall(notification)
        }
    }

    /**
     * Initiate regular phone call
     */
    private suspend fun initiatePhoneCall(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val phoneNumber = notification.advocateContact.phoneNumber
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(dialIntent)
            
            ChannelResult(
                channel = NotificationChannel.PHONE_CALL,
                success = true,
                message = "Phone call initiated to ${notification.advocateContact.name}",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.PHONE_CALL,
                success = false,
                message = "Failed to initiate phone call: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send emergency SMS with location and details
     */
    private suspend fun sendEmergencySMS(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val smsManager = SmsManager.getDefault()
            val phoneNumber = notification.advocateContact.phoneNumber
            
            val emergencyMessage = buildString {
                append("🚨 ELDER ABUSE ALERT 🚨\n")
                append("User: ${notification.userId}\n")
                append("Priority: ${notification.priority}\n")
                append("Alert: ${notification.message}\n")
                append("Time: ${formatTimestamp(notification.timestamp)}\n")
                append("This is an automated alert from Naviya Elder Protection System.")
            }
            
            // Split long messages if needed
            val parts = smsManager.divideMessage(emergencyMessage)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            
            ChannelResult(
                channel = NotificationChannel.SMS,
                success = true,
                message = "Emergency SMS sent to ${notification.advocateContact.name}",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.SMS,
                success = false,
                message = "Failed to send emergency SMS: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send regular SMS
     */
    private suspend fun sendSMS(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val smsManager = SmsManager.getDefault()
            val phoneNumber = notification.advocateContact.phoneNumber
            
            val message = buildString {
                append("Naviya Alert - ${notification.priority} Priority\n")
                append("User: ${notification.userId}\n")
                append("${notification.message}\n")
                append("Please check Naviya app for details.")
            }
            
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            
            ChannelResult(
                channel = NotificationChannel.SMS,
                success = true,
                message = "SMS sent to ${notification.advocateContact.name}",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.SMS,
                success = false,
                message = "Failed to send SMS: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send push notification
     */
    private suspend fun sendPushNotification(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            notificationService.sendElderRightsNotification(
                title = "Elder Abuse Alert - ${notification.priority} Priority",
                message = notification.message,
                userId = notification.userId,
                alertId = notification.alertId
            )
            
            ChannelResult(
                channel = NotificationChannel.PUSH_NOTIFICATION,
                success = true,
                message = "Push notification sent",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.PUSH_NOTIFICATION,
                success = false,
                message = "Failed to send push notification: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send emergency email with full details
     */
    private suspend fun sendEmergencyEmail(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${notification.advocateContact.email}")
                putExtra(Intent.EXTRA_SUBJECT, "🚨 URGENT: Elder Abuse Alert - ${notification.userId}")
                putExtra(Intent.EXTRA_TEXT, buildEmergencyEmailBody(notification))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(emailIntent)
            
            ChannelResult(
                channel = NotificationChannel.EMAIL,
                success = true,
                message = "Emergency email initiated",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.EMAIL,
                success = false,
                message = "Failed to send emergency email: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send regular email
     */
    private suspend fun sendEmail(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${notification.advocateContact.email}")
                putExtra(Intent.EXTRA_SUBJECT, "Naviya Alert - ${notification.priority} Priority")
                putExtra(Intent.EXTRA_TEXT, buildEmailBody(notification))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(emailIntent)
            
            ChannelResult(
                channel = NotificationChannel.EMAIL,
                success = true,
                message = "Email initiated",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.EMAIL,
                success = false,
                message = "Failed to send email: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Contact backup advocates for critical situations
     */
    private suspend fun contactBackupAdvocates(
        notification: ElderRightsNotification
    ): List<ChannelResult> {
        val results = mutableListOf<ChannelResult>()
        
        // Contact national hotlines as backup
        results.add(contactNationalElderAbuseHotline(notification))
        results.add(contactElderJusticeHotline(notification))
        
        return results
    }

    /**
     * Contact national elder abuse hotline
     */
    private suspend fun contactNationalElderAbuseHotline(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$NATIONAL_ELDER_ABUSE_HOTLINE")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(callIntent)
            
            ChannelResult(
                channel = NotificationChannel.BACKUP_HOTLINE,
                success = true,
                message = "National Elder Abuse Hotline contacted",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.BACKUP_HOTLINE,
                success = false,
                message = "Failed to contact national hotline: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Contact elder justice hotline
     */
    private suspend fun contactElderJusticeHotline(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$ELDER_JUSTICE_HOTLINE")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(callIntent)
            
            ChannelResult(
                channel = NotificationChannel.BACKUP_HOTLINE,
                success = true,
                message = "Elder Justice Hotline contacted",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.BACKUP_HOTLINE,
                success = false,
                message = "Failed to contact elder justice hotline: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Initiate call to national hotline as fallback
     */
    private suspend fun initiateNationalHotlineCall(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$NATIONAL_ELDER_ABUSE_HOTLINE")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(callIntent)
            
            ChannelResult(
                channel = NotificationChannel.EMERGENCY_HOTLINE,
                success = true,
                message = "Emergency call to National Elder Abuse Hotline",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.EMERGENCY_HOTLINE,
                success = false,
                message = "Failed to call national hotline: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Contact emergency services for critical escalation
     */
    private suspend fun contactEmergencyServices(
        notification: ElderRightsNotification
    ): ChannelResult {
        return try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:911")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(callIntent)
            
            ChannelResult(
                channel = NotificationChannel.EMERGENCY_SERVICES,
                success = true,
                message = "Emergency services contacted (911)",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ChannelResult(
                channel = NotificationChannel.EMERGENCY_SERVICES,
                success = false,
                message = "Failed to contact emergency services: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Get designated elder rights advocate for user
     */
    private suspend fun getDesignatedAdvocate(userId: String): ElderRightsAdvocateContact {
        return try {
            // Try to get user's designated advocate
            val advocate = contactDao.getElderRightsAdvocate(userId)
            if (advocate != null) {
                ElderRightsAdvocateContact(
                    name = advocate.name,
                    phoneNumber = advocate.phoneNumber,
                    email = advocate.email ?: "support@elderrights.org",
                    organization = advocate.organization ?: "Local Elder Rights Organization"
                )
            } else {
                // Fallback to default elder rights organization
                getDefaultElderRightsAdvocate()
            }
        } catch (e: Exception) {
            getDefaultElderRightsAdvocate()
        }
    }

    /**
     * Get default elder rights advocate contact
     */
    private fun getDefaultElderRightsAdvocate(): ElderRightsAdvocateContact {
        return ElderRightsAdvocateContact(
            name = "National Elder Rights Advocate",
            phoneNumber = NATIONAL_ELDER_ABUSE_HOTLINE,
            email = "help@elderrights.org",
            organization = "National Center on Elder Abuse"
        )
    }

    /**
     * Check if situation should escalate to emergency services
     */
    private suspend fun shouldEscalateToEmergencyServices(
        userId: String,
        alertId: String
    ): Boolean {
        return try {
            val recentCriticalAlerts = abuseDao.getRecentCriticalAlerts(
                userId, 
                ESCALATION_TIME_WINDOW
            )
            
            recentCriticalAlerts.size >= CRITICAL_ALERT_THRESHOLD
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Build emergency email body with full details
     */
    private fun buildEmergencyEmailBody(notification: ElderRightsNotification): String {
        return buildString {
            appendLine("🚨 URGENT ELDER ABUSE ALERT 🚨")
            appendLine()
            appendLine("This is an automated emergency notification from the Naviya Elder Protection System.")
            appendLine()
            appendLine("ALERT DETAILS:")
            appendLine("User ID: ${notification.userId}")
            appendLine("Alert ID: ${notification.alertId}")
            appendLine("Priority: ${notification.priority}")
            appendLine("Timestamp: ${formatTimestamp(notification.timestamp)}")
            appendLine()
            appendLine("ALERT MESSAGE:")
            appendLine(notification.message)
            appendLine()
            appendLine("IMMEDIATE ACTION REQUIRED:")
            appendLine("Please contact the elderly user immediately to ensure their safety.")
            appendLine("If you cannot reach them within 30 minutes, consider contacting local emergency services.")
            appendLine()
            appendLine("CONTACT INFORMATION:")
            appendLine("National Elder Abuse Hotline: $NATIONAL_ELDER_ABUSE_HOTLINE")
            appendLine("Elder Justice Hotline: $ELDER_JUSTICE_HOTLINE")
            appendLine("Adult Protective Services: $ADULT_PROTECTIVE_SERVICES")
            appendLine()
            appendLine("This alert was generated by Naviya's AI-powered abuse detection system.")
            appendLine("For technical support, contact: support@naviya.com")
        }
    }

    /**
     * Build regular email body
     */
    private fun buildEmailBody(notification: ElderRightsNotification): String {
        return buildString {
            appendLine("Naviya Elder Protection Alert")
            appendLine()
            appendLine("Priority: ${notification.priority}")
            appendLine("Timestamp: ${formatTimestamp(notification.timestamp)}")
            appendLine()
            appendLine("Alert Details:")
            appendLine(notification.message)
            appendLine()
            appendLine("Please review this alert and take appropriate action.")
            appendLine("You can access more details through the Naviya Elder Rights portal.")
            appendLine()
            appendLine("If this is an emergency, please call ${notification.advocateContact.phoneNumber} immediately.")
        }
    }

    /**
     * Format timestamp for human readability
     */
    private fun formatTimestamp(timestamp: Long): String {
        return java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
}

/**
 * Extension function for DAO to get recent critical alerts
 */
suspend fun AbuseDetectionDao.getRecentCriticalAlerts(
    userId: String,
    timeWindow: Long
): List<AbuseAlert> {
    val cutoffTime = System.currentTimeMillis() - timeWindow
    return getActiveAlerts(userId).filter { 
        it.riskLevel >= AbuseRiskLevel.HIGH && it.createdTimestamp >= cutoffTime 
    }
}

/**
 * Extension function for ContactDao to get elder rights advocate
 */
suspend fun ContactDao.getElderRightsAdvocate(userId: String): ProtectedContact? {
    return getProtectedContacts(userId).find { 
        it.contactType == ContactType.ELDER_RIGHTS_ADVOCATE 
    }
}

/**
 * Extension function for NotificationService to send elder rights notifications
 */
suspend fun NotificationService.sendElderRightsNotification(
    title: String,
    message: String,
    userId: String,
    alertId: String
) {
    // This would be implemented in the NotificationService
    // Placeholder for now
}
