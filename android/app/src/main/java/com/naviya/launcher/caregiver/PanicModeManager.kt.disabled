package com.naviya.launcher.caregiver

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.room.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Panic Mode Manager - Critical Abuse Prevention Feature
 * Provides secret ways for elderly users to disable caregiver monitoring
 * and contact independent help if they feel unsafe or controlled
 */

@Entity(tableName = "panic_mode_events")
data class PanicModeEvent(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val activationMethod: PanicActivationMethod,
    val timestamp: Long = System.currentTimeMillis(),
    val wasSuccessful: Boolean,
    val helpContacted: Boolean = false,
    val evidenceCollected: Boolean = false,
    val userSafe: Boolean? = null,  // Follow-up status
    val notes: String? = null
)

enum class PanicActivationMethod {
    TRIPLE_TAP_EMERGENCY,      // Triple-tap emergency button
    VOICE_COMMAND,             // "Privacy mode" or "Help me"
    SHAKE_GESTURE,             // Shake phone 5 times rapidly
    SECRET_TEXT,               // Text "HELP" to secret number
    LONG_PRESS_VOLUME,         // Hold volume down for 10 seconds
    PATTERN_UNLOCK             // Special unlock pattern
}

enum class PanicModeLevel {
    SAFE_MODE,                 // Disable monitoring, keep basic functions
    EMERGENCY_MODE,            // Contact elder advocate immediately
    STEALTH_MODE,              // Collect evidence, appear normal to caregiver
    FULL_ESCAPE               // Maximum protection, contact authorities
}

@Dao
interface PanicModeDao {
    @Insert
    suspend fun insertPanicEvent(event: PanicModeEvent)
    
    @Query("SELECT * FROM panic_mode_events ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentPanicEvents(): List<PanicModeEvent>
    
    @Query("SELECT COUNT(*) FROM panic_mode_events WHERE timestamp > :since")
    suspend fun getPanicEventCount(since: Long): Int
}

@Singleton
class PanicModeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: PanicModeDao,
    private val permissionManager: CaregiverPermissionManager,
    private val elderAdvocateService: ElderAdvocateService
) : SensorEventListener {
    
    private val _panicModeActive = MutableStateFlow(false)
    val panicModeActive: StateFlow<Boolean> = _panicModeActive
    
    private val _safeModeActive = MutableStateFlow(false)
    val safeModeActive: StateFlow<Boolean> = _safeModeActive
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private var shakeCount = 0
    private var lastShakeTime = 0L
    private var emergencyTapCount = 0
    private var lastEmergencyTap = 0L
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val TAG = "PanicModeManager"
        private const val SHAKE_THRESHOLD = 12.0f
        private const val SHAKE_RESET_TIME = 3000L  // 3 seconds
        private const val REQUIRED_SHAKES = 5
        private const val TAP_RESET_TIME = 2000L    // 2 seconds
        private const val REQUIRED_TAPS = 3
        
        // Elder Rights Hotline (National)
        private const val ELDER_RIGHTS_HOTLINE = "1-800-677-1116"
        private const val CRISIS_TEXT_LINE = "233733"  // Text HOME to 233733
    }
    
    init {
        // Start monitoring for panic gestures
        startPanicGestureMonitoring()
    }
    
    /**
     * Start monitoring for various panic activation methods
     */
    private fun startPanicGestureMonitoring() {
        // Register accelerometer for shake detection
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }
    
    /**
     * Handle emergency button triple-tap
     */
    fun handleEmergencyButtonTap() {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastEmergencyTap > TAP_RESET_TIME) {
            emergencyTapCount = 1
        } else {
            emergencyTapCount++
        }
        
        lastEmergencyTap = currentTime
        
        if (emergencyTapCount >= REQUIRED_TAPS) {
            activatePanicMode(PanicActivationMethod.TRIPLE_TAP_EMERGENCY, PanicModeLevel.SAFE_MODE)
            emergencyTapCount = 0
        }
    }
    
    /**
     * Handle voice command activation
     */
    fun handleVoiceCommand(command: String) {
        val normalizedCommand = command.lowercase().trim()
        
        when {
            normalizedCommand.contains("privacy mode") -> {
                activatePanicMode(PanicActivationMethod.VOICE_COMMAND, PanicModeLevel.SAFE_MODE)
            }
            normalizedCommand.contains("help me") -> {
                activatePanicMode(PanicActivationMethod.VOICE_COMMAND, PanicModeLevel.EMERGENCY_MODE)
            }
            normalizedCommand.contains("not safe") -> {
                activatePanicMode(PanicActivationMethod.VOICE_COMMAND, PanicModeLevel.FULL_ESCAPE)
            }
        }
    }
    
    /**
     * Handle secret text message
     */
    fun handleSecretText(message: String, sender: String) {
        val normalizedMessage = message.uppercase().trim()
        
        when (normalizedMessage) {
            "HELP" -> activatePanicMode(PanicActivationMethod.SECRET_TEXT, PanicModeLevel.EMERGENCY_MODE)
            "SAFE" -> deactivatePanicMode()
            "ABUSE" -> activatePanicMode(PanicActivationMethod.SECRET_TEXT, PanicModeLevel.FULL_ESCAPE)
            "STEALTH" -> activatePanicMode(PanicActivationMethod.SECRET_TEXT, PanicModeLevel.STEALTH_MODE)
        }
    }
    
    /**
     * Activate panic mode with specified level
     */
    private fun activatePanicMode(method: PanicActivationMethod, level: PanicModeLevel) {
        scope.launch {
            try {
                _panicModeActive.value = true
                
                // Provide immediate feedback to user
                providePanicFeedback(level)
                
                // Execute panic mode actions based on level
                when (level) {
                    PanicModeLevel.SAFE_MODE -> executeSafeMode()
                    PanicModeLevel.EMERGENCY_MODE -> executeEmergencyMode()
                    PanicModeLevel.STEALTH_MODE -> executeStealthMode()
                    PanicModeLevel.FULL_ESCAPE -> executeFullEscape()
                }
                
                // Log panic event
                val event = PanicModeEvent(
                    activationMethod = method,
                    wasSuccessful = true,
                    helpContacted = (level != PanicModeLevel.SAFE_MODE),
                    evidenceCollected = (level == PanicModeLevel.STEALTH_MODE)
                )
                dao.insertPanicEvent(event)
                
            } catch (e: Exception) {
                // Even if panic mode fails, log the attempt
                val event = PanicModeEvent(
                    activationMethod = method,
                    wasSuccessful = false,
                    notes = "Error: ${e.message}"
                )
                dao.insertPanicEvent(event)
            }
        }
    }
    
    /**
     * Safe Mode: Disable monitoring, keep basic functions
     */
    private suspend fun executeSafeMode() {
        _safeModeActive.value = true
        
        // Temporarily disable all caregiver monitoring
        disableAllCaregiverMonitoring(duration = Duration.ofHours(4))
        
        // Show discrete notification to user
        showDiscreteNotification(
            title = "Privacy Mode Active",
            message = "Monitoring disabled for 4 hours. Tap here to extend or disable.",
            isSecret = true
        )
        
        // Keep emergency functions available
        // Keep basic phone/messaging functions
        // Hide panic mode activation from caregiver logs
    }
    
    /**
     * Emergency Mode: Contact elder advocate immediately
     */
    private suspend fun executeEmergencyMode() {
        // Contact elder rights advocate
        elderAdvocateService.sendEmergencyAlert(
            reason = "User activated panic mode - may need immediate assistance",
            location = getCurrentApproximateLocation(),
            urgency = EmergencyUrgency.HIGH
        )
        
        // Provide multiple contact options
        showEmergencyContactDialog()
        
        // Disable caregiver monitoring
        disableAllCaregiverMonitoring(duration = Duration.ofDays(1))
        
        // Send discrete text to crisis line
        sendDiscreteCrisisText()
    }
    
    /**
     * Stealth Mode: Collect evidence, appear normal
     */
    private suspend fun executeStealthMode() {
        // Start collecting evidence of potential abuse
        startEvidenceCollection()
        
        // Continue appearing normal to caregiver
        // But log all caregiver actions with extra detail
        
        // Schedule check-in with elder advocate
        elderAdvocateService.scheduleDiscreteCheckIn(
            delayHours = 24,
            reason = "User requested stealth monitoring"
        )
    }
    
    /**
     * Full Escape: Maximum protection, contact authorities
     */
    private suspend fun executeFullEscape() {
        // Immediately contact multiple authorities
        elderAdvocateService.sendEmergencyAlert(
            reason = "URGENT: User reports feeling unsafe - potential elder abuse",
            location = getCurrentApproximateLocation(),
            urgency = EmergencyUrgency.CRITICAL
        )
        
        // Contact local Adult Protective Services
        contactAdultProtectiveServices()
        
        // Completely disable caregiver access
        removeAllCaregivers(reason = "User safety concern")
        
        // Provide safe communication channels
        enableSafeCommunicationMode()
        
        // Start evidence collection
        startEvidenceCollection()
    }
    
    /**
     * Provide immediate feedback to user that panic mode activated
     */
    private fun providePanicFeedback(level: PanicModeLevel) {
        // Discrete vibration pattern
        val pattern = when (level) {
            PanicModeLevel.SAFE_MODE -> longArrayOf(0, 100, 100, 100)      // Short-short
            PanicModeLevel.EMERGENCY_MODE -> longArrayOf(0, 200, 100, 200) // Long-short-long
            PanicModeLevel.STEALTH_MODE -> longArrayOf(0, 50)              // Very brief
            PanicModeLevel.FULL_ESCAPE -> longArrayOf(0, 300, 100, 300, 100, 300) // SOS pattern
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Disable all caregiver monitoring temporarily
     */
    private suspend fun disableAllCaregiverMonitoring(duration: Duration) {
        // This would integrate with the permission manager
        // to temporarily suspend all caregiver access
        
        // Schedule automatic re-enablement (user can extend)
        scope.launch {
            delay(duration.toMillis())
            if (_safeModeActive.value) {
                showReenableMonitoringDialog()
            }
        }
    }
    
    /**
     * Show emergency contact dialog with multiple options
     */
    private fun showEmergencyContactDialog() {
        // This would show a dialog with:
        // - Call Elder Rights Hotline: 1-800-677-1116
        // - Text Crisis Line: 233733
        // - Call Local Adult Protective Services
        // - Contact Trusted Friend/Family (not current caregiver)
        // - "I'm Safe Now" button
    }
    
    /**
     * Send discrete text to crisis line
     */
    private suspend fun sendDiscreteCrisisText() {
        // Send automated text to crisis line
        // "This is an automated message. An elderly person may need assistance. 
        //  They activated emergency mode on their device. Please follow up."
    }
    
    /**
     * Start collecting evidence of potential abuse
     */
    private suspend fun startEvidenceCollection() {
        // Start logging:
        // - All caregiver actions with timestamps
        // - Location data when caregiver accesses it
        // - Communication attempts
        // - App usage restrictions
        // Store evidence in secure, tamper-proof format
    }
    
    /**
     * Remove all caregivers for user safety
     */
    private suspend fun removeAllCaregivers(reason: String) {
        // Get all active caregivers and revoke their access
        // Log the action as user-initiated safety measure
        // Cannot be undone by caregivers
    }
    
    /**
     * Deactivate panic mode
     */
    fun deactivatePanicMode() {
        scope.launch {
            _panicModeActive.value = false
            _safeModeActive.value = false
            
            // Log deactivation
            val event = PanicModeEvent(
                activationMethod = PanicActivationMethod.SECRET_TEXT,
                wasSuccessful = true,
                userSafe = true,
                notes = "User deactivated panic mode"
            )
            dao.insertPanicEvent(event)
        }
    }
    
    // Sensor event handling for shake detection
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val acceleration = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            
            if (acceleration > SHAKE_THRESHOLD) {
                val currentTime = System.currentTimeMillis()
                
                if (currentTime - lastShakeTime > SHAKE_RESET_TIME) {
                    shakeCount = 1
                } else {
                    shakeCount++
                }
                
                lastShakeTime = currentTime
                
                if (shakeCount >= REQUIRED_SHAKES) {
                    activatePanicMode(PanicActivationMethod.SHAKE_GESTURE, PanicModeLevel.EMERGENCY_MODE)
                    shakeCount = 0
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for shake detection
    }
    
    /**
     * Check if user has used panic mode recently (potential ongoing abuse)
     */
    suspend fun hasRecentPanicActivity(): Boolean {
        val recentCount = dao.getPanicEventCount(System.currentTimeMillis() - Duration.ofDays(7).toMillis())
        return recentCount > 0
    }
    
    private fun showDiscreteNotification(title: String, message: String, isSecret: Boolean) {
        // Implementation would show notification that doesn't appear in caregiver monitoring
    }
    
    private fun showReenableMonitoringDialog() {
        // Ask user if they want to re-enable monitoring or extend safe mode
    }
    
    private suspend fun getCurrentApproximateLocation(): String {
        // Get general location (city/state) without exact coordinates
        return "Location services disabled for privacy"
    }
    
    private suspend fun contactAdultProtectiveServices() {
        // Contact local APS with automated report
    }
    
    private fun enableSafeCommunicationMode() {
        // Enable communication channels that caregiver cannot monitor
    }
}
