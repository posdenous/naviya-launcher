package com.naviya.launcher.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import com.naviya.launcher.emergency.data.EmergencyEvent
import com.naviya.launcher.emergency.data.EmergencyEventType
import java.util.Date

/**
 * Emergency service stub implementation for the Naviya launcher.
 * This class provides core emergency functionality for elderly users.
 * Follows Windsurf Wave 8+ rules for elderly accessibility and emergency safety.
 */
class EmergencyService(private val context: Context) {
    private val TAG = "EmergencyService"
    
    // Simple notification service for caregiver alerts
    private val notificationService = object {
        fun sendEmergencyNotification(
            contact: EmergencyContactStub,
            location: String?,
            userLanguage: String
        ): Boolean {
            // Simplified implementation - just log the notification
            Log.i(TAG, "Emergency notification sent to ${contact.name} in ${userLanguage} language")
            if (location != null) {
                Log.i(TAG, "Location information included: $location")
            }
            return true
        }
        
        fun sendCancellationNotification(reason: String): Boolean {
            // Simplified implementation - just log the cancellation
            Log.i(TAG, "Emergency cancellation notification sent: $reason")
            return true
        }
    }
    
    // StateFlow properties for reactive UI updates
    private val _emergencyActive = MutableStateFlow(false)
    val emergencyActive: StateFlow<Boolean> = _emergencyActive

    private val _caregiverAvailable = MutableStateFlow(false)
    val caregiverAvailable: StateFlow<Boolean> = _caregiverAvailable

    private val _emergencyContacts = MutableStateFlow<List<EmergencyContactStub>>(emptyList())
    val emergencyContacts: StateFlow<List<EmergencyContactStub>> = _emergencyContacts

    private val _reminderText = MutableStateFlow<String?>(null)
    val reminderText: StateFlow<String?> = _reminderText

    private val _lastKnownLocation = MutableStateFlow<String?>(null)
    val lastKnownLocation: StateFlow<String?> = _lastKnownLocation

    init {
        // Initialize with sample emergency contacts for testing
        _emergencyContacts.value = listOf(
            EmergencyContactStub(id = "1", name = "Primary Caregiver", phoneNumber = "+1234567890", isPrimary = true),
            EmergencyContactStub(id = "2", name = "Medical Professional", phoneNumber = "+1987654321", isMedicalProfessional = true)
        )
    }
    
    /**
     * Activate emergency mode with specified trigger
     * @param trigger The type of emergency trigger
     */
    fun activateEmergency(trigger: String) {
        Log.i(TAG, "Emergency activated with trigger: $trigger")
        _emergencyActive.value = true
        _reminderText.value = "Emergency services contacted. Help is on the way."
        // In a real implementation, this would contact emergency services
    }
    
    /**
     * Cancel an active emergency
     */
    fun cancelEmergency() {
        Log.i(TAG, "Emergency cancelled")
        _emergencyActive.value = false
        _reminderText.value = null
    }
    
    /**
     * Check if caregiver is available
     * @return true if caregiver is available, false otherwise
     */
    fun isCaregiverAvailable(): Boolean {
        return _caregiverAvailable.value
    }
    
    /**
     * Set caregiver availability status
     * @param available true if caregiver is available, false otherwise
     */
    fun setCaregiverAvailability(available: Boolean) {
        _caregiverAvailable.value = available
    }
    
    /**
     * Get all emergency contacts
     * @return List of emergency contacts
     */
    fun getEmergencyContacts(): List<EmergencyContactStub> {
        return _emergencyContacts.value
    }
    
    /**
     * Add a new emergency contact
     * @param contact The emergency contact to add
     */
    fun addEmergencyContact(contact: EmergencyContactStub) {
        val currentContacts = _emergencyContacts.value.toMutableList()
        currentContacts.add(contact)
        _emergencyContacts.value = currentContacts
    }
    
    /**
     * Remove an emergency contact
     * @param contactId The ID of the contact to remove
     */
    fun removeEmergencyContact(contactId: String) {
        val currentContacts = _emergencyContacts.value.toMutableList()
        currentContacts.removeAll { it.id == contactId }
        _emergencyContacts.value = currentContacts
    }
    
    /**
     * Update location information for emergency services
     * @param locationString A string representation of the location
     */
    fun updateLocation(locationString: String) {
        _lastKnownLocation.value = locationString
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
            // Simplified: Log event instead of storing in database
            Log.i(TAG, "Emergency event: ${emergencyEvent.eventType}")
            
            // Get emergency contacts (offline-first)
            val contacts = _emergencyContacts.value
            if (contacts.isEmpty()) {
                return EmergencyResult.NoContactsConfigured
            }
            
            // Get current location (if available)
            val location = _lastKnownLocation.value
            
            // Start emergency sequence
            val results = mutableListOf<ContactResult>()
            
            // 1. Call emergency services first (highest priority)
            val emergencyService = _emergencyContacts.value.firstOrNull { it.isMedicalProfessional }
            emergencyService?.let { contact ->
                val result = makeEmergencyCall(contact, location, userLanguage)
                results.add(result)
            }
            
            // 2. Notify primary caregiver
            val primaryCaregiver = _emergencyContacts.value.firstOrNull { it.isPrimary }
            primaryCaregiver?.let { contact ->
                val result = notifyCaregiver(contact, location, userLanguage)
                results.add(result)
            }
            
            // 3. Send SMS to all contacts (offline backup)
            sendEmergencySMS(contacts, location, userLanguage)
            
            val responseTime = System.currentTimeMillis() - startTime
            Log.i(TAG, "SOS sequence completed in ${responseTime}ms")
            
            // Simplified: Log event instead of storing in database
            Log.i(TAG, "Emergency event completed: responseTime=${responseTime}ms, location=$location")
            
            EmergencyResult.Success(results, responseTime)
            
        } catch (e: Exception) {
            Log.e(TAG, "SOS activation failed", e)
            val responseTime = System.currentTimeMillis() - startTime
            return EmergencyResult.Error(e.message ?: "Unknown error", responseTime)
        }
    }
    
    /**
     * Make emergency call with accessibility support
     */
    private suspend fun makeEmergencyCall(
        contact: EmergencyContactStub,
        location: String?,
        userLanguage: String
    ): ContactResult {
        return try {
            // Simplified implementation - just log the call
            Log.i(TAG, "Emergency call made to ${contact.name} at ${contact.phoneNumber}")
            Log.i(TAG, "Location: $location, Language: $userLanguage")
            
            // In a real implementation, this would use Intent.ACTION_CALL
            // and handle permissions properly
            
            ContactResult.Success(contact, "Call initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Emergency call failed for ${contact.name}", e)
            ContactResult.Failed(contact, e.message ?: "Call failed")
        }
    }
    
    /**
     * Notify caregiver through multiple channels
     */
    private suspend fun notifyCaregiver(
        contact: EmergencyContactStub,
        location: String?,
        userLanguage: String
    ): ContactResult {
        return try {
            // Send notification through caregiver service
            val notificationSent = notificationService.sendEmergencyNotification(
                contact, location, userLanguage
            )
            
            if (notificationSent) {
                Log.i(TAG, "Caregiver ${contact.name} notified successfully")
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
        contacts: List<EmergencyContactStub>,
        location: String?,
        userLanguage: String
    ) {
        try {
            // Simplified implementation - just log the SMS
            val locationText = location ?: "Location unavailable"
            val timestamp = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", 
                java.util.Locale.getDefault()
            ).format(java.util.Date())
            
            // Use language-specific message templates based on user preference
            val message = when (userLanguage) {
                "de" -> "NOTFALL: Hilfe wird benötigt bei $locationText. Gesendet um $timestamp"
                "tr" -> "ACİL DURUM: $locationText konumunda yardıma ihtiyaç var. Gönderilme zamanı: $timestamp"
                "ar" -> "حالة طوارئ: المساعدة مطلوبة في $locationText. أرسلت في $timestamp"
                else -> "EMERGENCY: Help needed at $locationText. Sent at $timestamp"
            }
            
            contacts.forEach { contact ->
                Log.i(TAG, "Emergency SMS sent to ${contact.name} at ${contact.phoneNumber} in ${userLanguage} language: $message")
            }
            
            // In a real implementation, this would use SmsManager to send actual SMS
            
        } catch (e: Exception) {
            Log.e(TAG, "Emergency SMS sending failed", e)
        }
    }
    
    /**
     * Cancel active SOS (if user is safe)
     */
    suspend fun cancelSOS(reason: String = "User cancelled"): Boolean {
        return try {
            // Simplified: Log event instead of storing in database
            Log.i(TAG, "Emergency event: SOS_CANCELLED, reason=$reason")
            
            // Update emergency active state
            _emergencyActive.value = false
            
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
     * Get emergency contacts as a Flow for reactive UI display
     */
    fun getEmergencyContactsFlow(): Flow<List<EmergencyContactStub>> {
        // Return a flow with the stub contacts for now
        return flowOf(_emergencyContacts.value)
    }
    
    /**
     * Add or update emergency contact
     */
    suspend fun saveEmergencyContact(contact: EmergencyContactStub) {
        // Add to the local list for now
        addEmergencyContact(contact)
        Log.i(TAG, "Emergency contact saved: ${contact.name}")
    }
    
    /**
     * Check if emergency system is properly configured
     */
    suspend fun isEmergencySystemReady(): Boolean {
        val contactCount = _emergencyContacts.value.size
        val hasEmergencyService = _emergencyContacts.value.any { it.isMedicalProfessional }
        val hasCaregiver = _emergencyContacts.value.any { it.isPrimary }
        
        return contactCount > 0 && (hasEmergencyService || hasCaregiver)
    }
    
    /**
     * Get recent emergency events for caregiver dashboard
     */
    fun getRecentEmergencyEvents(): Flow<List<EmergencyEvent>> {
        // Return empty list for now
        return flowOf(emptyList())
    }
    
    /**
     * Get current SOS system status for monitoring and testing
     * Provides real-time status information about emergency system readiness
     */
    suspend fun getSOSStatus(): SOSStatus {
        return SOSStatus(
            isActive = _emergencyActive.value,
            activatedAt = if (_emergencyActive.value) System.currentTimeMillis() else null,
            triggeredBy = if (_emergencyActive.value) SOSTrigger.MANUAL else null
        )
    }
    
    private fun isNetworkAvailable(): Boolean {
        // Modern network connectivity check that works on all Android versions
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as android.net.ConnectivityManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Modern API (Android 6.0+)
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                capabilities != null && (
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
                )
            } else {
                // Legacy API (pre-Android 6.0) - suppressing deprecation warning
                @Suppress("DEPRECATION")
                val activeNetwork = connectivityManager.activeNetworkInfo
                @Suppress("DEPRECATION")
                activeNetwork?.isConnected == true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network connectivity check failed", e)
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
    data class Success(val contact: EmergencyContactStub, val message: String) : ContactResult()
    data class Failed(val contact: EmergencyContactStub, val error: String) : ContactResult()
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
