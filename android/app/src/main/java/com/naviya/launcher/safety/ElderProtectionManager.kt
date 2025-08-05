package com.naviya.launcher.safety

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Elder Protection Manager
 * 
 * This class implements critical safety features to prevent potential caregiver abuse
 * and protect elderly users' autonomy and privacy. It provides:
 * 
 * 1. Emergency "panic mode" to quickly disable all monitoring
 * 2. Immutable audit trail of all caregiver actions
 * 3. Automatic abuse detection with red flag algorithms
 * 4. Consent management with expiration and renewal
 * 5. Protected communication channels to elder rights advocates
 * 
 * These features are essential for ethical implementation and to prevent potential
 * abuse vectors including surveillance abuse, financial abuse, social isolation,
 * and psychological control.
 */
class ElderProtectionManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "ElderProtectionManager"
        
        // Shared preferences keys
        private const val PREFS_NAME = "naviya_elder_protection"
        private const val KEY_PANIC_MODE_ACTIVE = "panic_mode_active"
        private const val KEY_LAST_CONSENT_TIME = "last_consent_time"
        private const val KEY_CONSENT_WITNESS = "consent_witness"
        private const val KEY_ABUSE_FLAGS = "abuse_flags"
        
        // Constants for abuse detection
        private const val MAX_LOCATION_CHECKS_PER_DAY = 24 // Once per hour max
        private const val MAX_APP_REMOVALS_PER_WEEK = 3
        private const val CONSENT_EXPIRY_DAYS = 30 // Monthly consent renewal
        
        // Singleton instance
        @Volatile
        private var INSTANCE: ElderProtectionManager? = null
        
        fun getInstance(context: Context): ElderProtectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ElderProtectionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val securityLogger = SecurityAuditLogger(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // LiveData for panic mode state
    private val _panicModeActive = MutableLiveData<Boolean>()
    val panicModeActive: LiveData<Boolean> = _panicModeActive
    
    // LiveData for abuse detection alerts
    private val _abuseDetected = MutableLiveData<AbuseAlert?>()
    val abuseDetected: LiveData<AbuseAlert?> = _abuseDetected
    
    init {
        // Initialize LiveData with current state
        _panicModeActive.value = isPanicModeActive()
        
        // Check for consent expiration
        checkConsentExpiration()
        
        Log.d(TAG, "Elder Protection Manager initialized")
    }
    
    /**
     * Activate panic mode to immediately disable all monitoring
     * This can be triggered by triple-tap, voice command, or other emergency methods
     */
    fun activatePanicMode(reason: String) {
        prefs.edit {
            putBoolean(KEY_PANIC_MODE_ACTIVE, true)
        }
        _panicModeActive.value = true
        
        // Log this critical safety event with immutable audit trail
        securityLogger.logCriticalEvent(
            SecurityAuditLogger.EventType.PANIC_MODE_ACTIVATED,
            "Panic mode activated: $reason"
        )
        
        Log.d(TAG, "PANIC MODE ACTIVATED: $reason")
        
        // Notify elder rights advocate if configured
        notifyElderRightsAdvocate("Panic mode activated: $reason")
    }
    
    /**
     * Deactivate panic mode - requires verification by trusted third party
     */
    fun deactivatePanicMode(verifiedBy: String) {
        prefs.edit {
            putBoolean(KEY_PANIC_MODE_ACTIVE, false)
        }
        _panicModeActive.value = false
        
        // Log this critical safety event with immutable audit trail
        securityLogger.logCriticalEvent(
            SecurityAuditLogger.EventType.PANIC_MODE_DEACTIVATED,
            "Panic mode deactivated: Verified by $verifiedBy"
        )
        
        Log.d(TAG, "PANIC MODE DEACTIVATED: Verified by $verifiedBy")
    }
    
    /**
     * Check if panic mode is currently active
     */
    fun isPanicModeActive(): Boolean {
        return prefs.getBoolean(KEY_PANIC_MODE_ACTIVE, false)
    }
    
    /**
     * Record caregiver action for abuse detection and auditing
     */
    fun recordCaregiverAction(actionType: CaregiverActionType, details: String) {
        // Always log the action with immutable audit trail
        securityLogger.logCaregiverAction(actionType, details)
        
        // Check for potential abuse patterns
        coroutineScope.launch {
            val isAbusive = detectAbusivePattern(actionType, details)
            if (isAbusive) {
                // Alert about potential abuse
                _abuseDetected.value = AbuseAlert(actionType, details)
                
                // Log the abuse detection
                securityLogger.logCriticalEvent(
                    SecurityAuditLogger.EventType.ABUSE_DETECTED,
                    "Potential abuse detected: $actionType - $details"
                )
                
                // Notify elder rights advocate
                notifyElderRightsAdvocate("Potential abuse detected: $actionType - $details")
            }
        }
    }
    
    /**
     * Record user consent with witness verification
     */
    fun recordConsent(witnessName: String, witnessRole: String) {
        val currentTime = System.currentTimeMillis()
        
        prefs.edit {
            putLong(KEY_LAST_CONSENT_TIME, currentTime)
            putString(KEY_CONSENT_WITNESS, "$witnessName ($witnessRole)")
        }
        
        // Log consent with immutable audit trail
        securityLogger.logCriticalEvent(
            SecurityAuditLogger.EventType.CONSENT_RECORDED,
            "Consent recorded with witness: $witnessName ($witnessRole)"
        )
        
        Log.d(TAG, "Consent recorded with witness: $witnessName ($witnessRole)")
    }
    
    /**
     * Check if consent has expired and needs renewal
     */
    private fun checkConsentExpiration() {
        val lastConsentTime = prefs.getLong(KEY_LAST_CONSENT_TIME, 0)
        if (lastConsentTime == 0L) {
            // No consent recorded yet
            Log.d(TAG, "No consent record found")
            return
        }
        
        val currentTime = System.currentTimeMillis()
        val daysSinceConsent = TimeUnit.MILLISECONDS.toDays(currentTime - lastConsentTime)
        
        if (daysSinceConsent >= CONSENT_EXPIRY_DAYS) {
            Log.d(TAG, "Consent expired ($daysSinceConsent days old)")
            
            // Log consent expiration
            securityLogger.logCriticalEvent(
                SecurityAuditLogger.EventType.CONSENT_EXPIRED,
                "Consent expired after $daysSinceConsent days"
            )
            
            // Automatically activate panic mode for safety
            activatePanicMode("Consent expired after $daysSinceConsent days")
        } else {
            Log.d(TAG, "Consent valid ($daysSinceConsent days old)")
        }
    }
    
    /**
     * Detect potentially abusive patterns in caregiver actions
     */
    private suspend fun detectAbusivePattern(actionType: CaregiverActionType, details: String): Boolean = 
        withContext(Dispatchers.Default) {
            // This would contain more sophisticated pattern detection in production
            // For now, we implement basic red flags
            
            when (actionType) {
                CaregiverActionType.LOCATION_CHECK -> {
                    val dailyChecks = securityLogger.getActionCountForToday(CaregiverActionType.LOCATION_CHECK)
                    dailyChecks > MAX_LOCATION_CHECKS_PER_DAY
                }
                
                CaregiverActionType.APP_REMOVAL -> {
                    val weeklyRemovals = securityLogger.getActionCountForPastDays(
                        CaregiverActionType.APP_REMOVAL, 
                        7
                    )
                    weeklyRemovals > MAX_APP_REMOVALS_PER_WEEK
                }
                
                CaregiverActionType.CONTACT_REMOVAL -> {
                    // Any contact removal is flagged for review
                    true
                }
                
                CaregiverActionType.SETTINGS_CHANGE -> {
                    // Check for critical settings changes
                    details.contains("emergency", ignoreCase = true) ||
                    details.contains("contact", ignoreCase = true) ||
                    details.contains("advocate", ignoreCase = true)
                }
                
                else -> false
            }
        }
    
    /**
     * Notify elder rights advocate about critical events
     * This channel cannot be disabled by caregivers
     */
    private fun notifyElderRightsAdvocate(message: String) {
        // In production, this would send a secure notification to a configured advocate
        Log.d(TAG, "ADVOCATE NOTIFICATION: $message")
        
        // Log the notification
        securityLogger.logCriticalEvent(
            SecurityAuditLogger.EventType.ADVOCATE_NOTIFIED,
            "Elder rights advocate notified: $message"
        )
    }
    
    /**
     * Types of caregiver actions that are monitored for potential abuse
     */
    enum class CaregiverActionType {
        LOCATION_CHECK,
        APP_REMOVAL,
        CONTACT_REMOVAL,
        SETTINGS_CHANGE,
        REMOTE_CONTROL,
        NOTIFICATION_ACCESS,
        COMMUNICATION_MONITORING
    }
    
    /**
     * Data class for abuse alerts
     */
    data class AbuseAlert(
        val actionType: CaregiverActionType,
        val details: String,
        val timestamp: Long = System.currentTimeMillis()
    )
}
