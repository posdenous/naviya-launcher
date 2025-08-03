package com.naviya.launcher.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.naviya.launcher.emergency.data.EmergencyContact
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.emergency.data.EmergencyEvent
import com.naviya.launcher.emergency.data.EmergencyEventType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core Emergency Service for Naviya Launcher
 * Implements offline-first emergency functionality for elderly users
 * Follows Windsurf rules for emergency safety and accessibility
 */
@Singleton
class EmergencyService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emergencyDao: EmergencyDao,
    private val locationService: EmergencyLocationService,
    private val notificationService: CaregiverNotificationService
) {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val emergencyScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val TAG = "EmergencyService"
        private const val SOS_RESPONSE_TIMEOUT_MS = 500L // Windsurf rule: <500ms response
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val EMERGENCY_SMS_TEMPLATE = "EMERGENCY: I need help. Location: %s. Time: %s. This is an automated message from Naviya Launcher."
    }
    
    /**
     * Activate SOS emergency system
     * Must work offline and respond within 500ms (Windsurf rule)
     */
    suspend fun activateSOS(
        userLanguage: String = "en",
        triggeredBy: SOSTrigger = SOSTrigger.MANUAL
    ): EmergencyResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            Log.i(TAG, "SOS activated - trigger: $triggeredBy, language: $userLanguage")
            
            // Log emergency event immediately (audit requirement)
            val emergencyEvent = EmergencyEvent(
                eventType = EmergencyEventType.SOS_ACTIVATED,
                userLanguage = userLanguage,
                wasOffline = !isNetworkAvailable(),
                notes = "Triggered by: $triggeredBy"
            )
            emergencyDao.insertEmergencyEvent(emergencyEvent)
            
            // Get emergency contacts (offline-first)
            val contacts = emergencyDao.getAllActiveContactsSync()
            if (contacts.isEmpty()) {
                return EmergencyResult.NoContactsConfigured
            }
            
            // Get current location (if available)
            val location = locationService.getCurrentLocationSync()
            
            // Start emergency sequence
            val results = mutableListOf<ContactResult>()
            
            // 1. Call emergency services first (highest priority)
            val emergencyService = emergencyDao.getEmergencyService()
            emergencyService?.let { contact ->
                val result = makeEmergencyCall(contact, location, userLanguage)
                results.add(result)
            }
            
            // 2. Notify primary caregiver
            val primaryCaregiver = emergencyDao.getPrimaryCaregiver()
            primaryCaregiver?.let { contact ->
                val result = notifyCaregiver(contact, location, userLanguage)
                results.add(result)
            }
            
            // 3. Send SMS to all contacts (offline backup)
            sendEmergencySMS(contacts, location, userLanguage)
            
            val responseTime = System.currentTimeMillis() - startTime
            Log.i(TAG, "SOS sequence completed in ${responseTime}ms")
            
            // Update event with response time
            emergencyDao.insertEmergencyEvent(
                emergencyEvent.copy(
                    responseTimeMs = responseTime,
                    locationLatitude = location?.latitude,
                    locationLongitude = location?.longitude
                )
            )
            
            EmergencyResult.Success(results, responseTime)
            
        } catch (e: Exception) {
            Log.e(TAG, "SOS activation failed", e)
            val responseTime = System.currentTimeMillis() - startTime
            
            // Log failure
            emergencyDao.insertEmergencyEvent(
                EmergencyEvent(
                    eventType = EmergencyEventType.SOS_ACTIVATED,
                    userLanguage = userLanguage,
                    wasOffline = !isNetworkAvailable(),
                    responseTimeMs = responseTime,
                    wasSuccessful = false,
                    notes = "Error: ${e.message}"
                )
            )
            
            EmergencyResult.Error(e.message ?: "Unknown error", responseTime)
        }
    }
    
    /**
     * Make emergency call with accessibility support
     */
    private suspend fun makeEmergencyCall(
        contact: EmergencyContact,
        location: Location?,
        userLanguage: String
    ): ContactResult {
        return try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${contact.phoneNumber}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                
                // Add emergency context for modern Android versions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    putExtra("android.telecom.extra.IS_EMERGENCY_CALL", true)
                }
            }
            
            // Check call permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                == PackageManager.PERMISSION_GRANTED) {
                
                context.startActivity(callIntent)
                
                // Update last contacted
                emergencyDao.updateLastContacted(contact.id, System.currentTimeMillis())
                
                // Log successful call
                emergencyDao.insertEmergencyEvent(
                    EmergencyEvent(
                        eventType = EmergencyEventType.EMERGENCY_CALL_MADE,
                        contactId = contact.id,
                        userLanguage = userLanguage,
                        locationLatitude = location?.latitude,
                        locationLongitude = location?.longitude
                    )
                )
                
                ContactResult.Success(contact, "Call initiated")
            } else {
                ContactResult.Failed(contact, "Call permission not granted")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Emergency call failed for ${contact.name}", e)
            ContactResult.Failed(contact, e.message ?: "Call failed")
        }
    }
    
    /**
     * Notify caregiver through multiple channels
     */
    private suspend fun notifyCaregiver(
        contact: EmergencyContact,
        location: Location?,
        userLanguage: String
    ): ContactResult {
        return try {
            // Send notification through caregiver service
            val notificationSent = notificationService.sendEmergencyNotification(
                contact, location, userLanguage
            )
            
            if (notificationSent) {
                emergencyDao.insertEmergencyEvent(
                    EmergencyEvent(
                        eventType = EmergencyEventType.CAREGIVER_NOTIFIED,
                        contactId = contact.id,
                        userLanguage = userLanguage,
                        locationLatitude = location?.latitude,
                        locationLongitude = location?.longitude
                    )
                )
                ContactResult.Success(contact, "Caregiver notified")
            } else {
                ContactResult.Failed(contact, "Notification failed")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Caregiver notification failed for ${contact.name}", e)
            ContactResult.Failed(contact, e.message ?: "Notification failed")
        }
    }
    
    /**
     * Send emergency SMS to all contacts (offline backup)
     */
    private suspend fun sendEmergencySMS(
        contacts: List<EmergencyContact>,
        location: Location?,
        userLanguage: String
    ) {
        try {
            val smsManager = SmsManager.getDefault()
            val locationText = location?.let { 
                "Lat: ${it.latitude}, Lon: ${it.longitude}" 
            } ?: "Location unavailable"
            
            val timestamp = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", 
                java.util.Locale.getDefault()
            ).format(java.util.Date())
            
            val message = EMERGENCY_SMS_TEMPLATE.format(locationText, timestamp)
            
            contacts.forEach { contact ->
                try {
                    smsManager.sendTextMessage(
                        contact.phoneNumber,
                        null,
                        message,
                        null,
                        null
                    )
                    
                    Log.i(TAG, "Emergency SMS sent to ${contact.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "SMS failed for ${contact.name}", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Emergency SMS sending failed", e)
        }
    }
    
    /**
     * Cancel active SOS (if user is safe)
     */
    suspend fun cancelSOS(reason: String = "User cancelled"): Boolean {
        return try {
            emergencyDao.insertEmergencyEvent(
                EmergencyEvent(
                    eventType = EmergencyEventType.SOS_CANCELLED,
                    userLanguage = "en", // Default language for now
                    notes = reason
                )
            )
            
            // Notify caregivers of cancellation
            notificationService.sendCancellationNotification(reason)
            
            Log.i(TAG, "SOS cancelled: $reason")
            true
        } catch (e: Exception) {
            Log.e(TAG, "SOS cancellation failed", e)
            false
        }
    }
    
    /**
     * Get emergency contacts for UI display
     */
    fun getEmergencyContacts(): Flow<List<EmergencyContact>> {
        return emergencyDao.getAllActiveContacts()
    }
    
    /**
     * Add or update emergency contact
     */
    suspend fun saveEmergencyContact(contact: EmergencyContact) {
        emergencyDao.insertContact(contact)
        Log.i(TAG, "Emergency contact saved: ${contact.name}")
    }
    
    /**
     * Check if emergency system is properly configured
     */
    suspend fun isEmergencySystemReady(): Boolean {
        val contactCount = emergencyDao.getActiveContactCount()
        val hasEmergencyService = emergencyDao.getEmergencyService() != null
        val hasCaregiver = emergencyDao.getPrimaryCaregiver() != null
        
        return contactCount > 0 && (hasEmergencyService || hasCaregiver)
    }
    
    /**
     * Get recent emergency events for caregiver dashboard
     */
    fun getRecentEmergencyEvents(): Flow<List<EmergencyEvent>> {
        return emergencyDao.getRecentEmergencyEvents()
    }
    
    /**
     * Get current SOS system status for monitoring and testing
     * Provides real-time status information about emergency system readiness
     */
    suspend fun getSOSStatus(): SOSStatus {
        return try {
            val recentEvents = emergencyDao.getRecentEmergencyEvents().first()
            val lastActivation = recentEvents.firstOrNull { 
                it.eventType == EmergencyEventType.SOS_ACTIVATED 
            }
            val lastCancellation = recentEvents.firstOrNull {
                it.eventType == EmergencyEventType.SOS_CANCELLED
            }
            
            // Determine if SOS is currently active
            val isActive = lastActivation != null && 
                (lastCancellation == null || lastActivation.timestamp > lastCancellation.timestamp)
            
            SOSStatus(
                isActive = isActive,
                activatedAt = lastActivation?.timestamp,
                triggeredBy = SOSTrigger.MANUAL // Default trigger type
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting SOS status", e)
            SOSStatus(
                isActive = false,
                activatedAt = null,
                triggeredBy = null
            )
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        // Simple network check - implement proper network detection
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            activeNetwork?.isConnected == true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * SOS trigger types for analytics
 */
enum class SOSTrigger {
    MANUAL,           // User pressed SOS button
    VOICE_COMMAND,    // Voice activation
    GESTURE,          // Emergency gesture
    FALL_DETECTION,   // Automatic fall detection
    PANIC_BUTTON,     // Physical panic button
    CAREGIVER_REMOTE  // Triggered by caregiver
}

/**
 * Emergency activation results
 */
sealed class EmergencyResult {
    data class Success(val results: List<ContactResult>, val responseTimeMs: Long) : EmergencyResult()
    data class Error(val message: String, val responseTimeMs: Long) : EmergencyResult()
    object NoContactsConfigured : EmergencyResult()
}

/**
 * Individual contact notification results
 */
sealed class ContactResult {
    data class Success(val contact: EmergencyContact, val message: String) : ContactResult()
    data class Failed(val contact: EmergencyContact, val error: String) : ContactResult()
}

/**
 * SOS system status for monitoring and testing
 * Matches the structure expected by existing tests
 */
data class SOSStatus(
    val isActive: Boolean,
    val activatedAt: Long? = null,
    val triggeredBy: SOSTrigger? = null
)
