package com.naviya.launcher.emergency

import android.content.Context
import android.util.Log
import com.naviya.launcher.emergency.data.EmergencyContact
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.emergency.data.EmergencyEvent
import com.naviya.launcher.emergency.data.EmergencyEventType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simplified Medical Emergency Integration
 * Bridges emergency response with basic medical compliance
 * Works with existing emergency system without complex healthcare dependencies
 */
@Singleton
class SimpleMedicalEmergencyIntegration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emergencyDao: EmergencyDao,
    private val emergencyService: EmergencyService
) {
    companion object {
        private const val TAG = "SimpleMedicalEmergencyIntegration"
        private const val MEDICAL_EMERGENCY_TIMEOUT_MS = 2000L
    }

    /**
     * Activate medical emergency with enhanced logging
     */
    suspend fun activateMedicalEmergency(
        userId: String,
        emergencyType: MedicalEmergencyType,
        userLanguage: String = "en",
        triggeredBy: SOSTrigger = SOSTrigger.MANUAL,
        symptoms: List<String> = emptyList()
    ): SimpleMedicalEmergencyResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            Log.i(TAG, "Medical emergency activated - Type: $emergencyType, User: $userId")
            
            // 1. Log medical emergency event
            val emergencyEvent = EmergencyEvent(
                eventType = EmergencyEventType.SOS_ACTIVATED,
                userId = userId,
                userLanguage = userLanguage,
                wasOffline = !isNetworkAvailable(),
                notes = "Medical Emergency: $emergencyType, Symptoms: ${symptoms.joinToString()}"
            )
            emergencyDao.insertEmergencyEvent(emergencyEvent)
            
            // 2. Get emergency contacts
            val contacts = emergencyDao.getAllActiveContactsSync()
            
            // 3. Determine response protocol
            val protocol = determineSimpleResponseProtocol(emergencyType, symptoms)
            
            // 4. Activate standard emergency response
            val emergencyResult = emergencyService.activateSOS(userLanguage, triggeredBy)
            
            // 5. Log additional medical context
            logMedicalContext(userId, emergencyType, symptoms, protocol)
            
            val responseTime = System.currentTimeMillis() - startTime
            
            SimpleMedicalEmergencyResult.Success(
                emergencyResult = emergencyResult,
                responseProtocol = protocol,
                responseTimeMs = responseTime,
                contactsNotified = contacts.size
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Medical emergency activation failed", e)
            val responseTime = System.currentTimeMillis() - startTime
            SimpleMedicalEmergencyResult.Error(e.message ?: "Unknown error", responseTime)
        }
    }

    /**
     * Check if medical emergency system is ready
     */
    suspend fun isMedicalEmergencySystemReady(): Boolean {
        return try {
            val hasContacts = emergencyDao.getActiveContactCount() > 0
            val emergencyServiceReady = emergencyService.isEmergencySystemReady()
            
            hasContacts && emergencyServiceReady
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check medical emergency system readiness", e)
            false
        }
    }

    /**
     * Get medical emergency statistics
     */
    suspend fun getMedicalEmergencyStats(): MedicalEmergencyStats {
        return try {
            val recentEvents = emergencyDao.getRecentEmergencyEvents().first()
            val medicalEvents = recentEvents.filter { 
                it.notes?.contains("Medical Emergency") == true 
            }
            
            MedicalEmergencyStats(
                totalMedicalEmergencies = medicalEvents.size,
                lastMedicalEmergency = medicalEvents.firstOrNull()?.timestamp,
                systemReady = isMedicalEmergencySystemReady(),
                activeContacts = emergencyDao.getActiveContactCount()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get medical emergency stats", e)
            MedicalEmergencyStats()
        }
    }

    private fun determineSimpleResponseProtocol(
        emergencyType: MedicalEmergencyType,
        symptoms: List<String>
    ): SimpleResponseProtocol {
        return when (emergencyType) {
            MedicalEmergencyType.CARDIAC_EVENT -> SimpleResponseProtocol(
                priority = "CRITICAL",
                requiresImmediateEMS = true,
                description = "Cardiac emergency - immediate medical attention required"
            )
            
            MedicalEmergencyType.FALL_WITH_INJURY -> SimpleResponseProtocol(
                priority = "HIGH",
                requiresImmediateEMS = true,
                description = "Fall with potential injury - medical assessment needed"
            )
            
            MedicalEmergencyType.MEDICATION_EMERGENCY -> SimpleResponseProtocol(
                priority = "MEDIUM",
                requiresImmediateEMS = false,
                description = "Medication-related emergency - professional consultation needed"
            )
            
            MedicalEmergencyType.COGNITIVE_CRISIS -> SimpleResponseProtocol(
                priority = "MEDIUM",
                requiresImmediateEMS = false,
                description = "Cognitive crisis - specialized support needed"
            )
            
            MedicalEmergencyType.GENERAL_MEDICAL -> SimpleResponseProtocol(
                priority = "LOW",
                requiresImmediateEMS = false,
                description = "General medical concern - standard emergency response"
            )
        }
    }

    private suspend fun logMedicalContext(
        userId: String,
        emergencyType: MedicalEmergencyType,
        symptoms: List<String>,
        protocol: SimpleResponseProtocol
    ) {
        try {
            val contextEvent = EmergencyEvent(
                eventType = EmergencyEventType.SOS_ACTIVATED,
                userId = userId,
                userLanguage = "en",
                wasOffline = !isNetworkAvailable(),
                notes = "Medical Context - Type: $emergencyType, Priority: ${protocol.priority}, Symptoms: ${symptoms.joinToString()}"
            )
            emergencyDao.insertEmergencyEvent(contextEvent)
            
            Log.i(TAG, "Medical context logged for user: $userId, type: $emergencyType")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log medical context", e)
        }
    }

    private fun isNetworkAvailable(): Boolean {
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

// Simplified data classes

enum class MedicalEmergencyType {
    CARDIAC_EVENT,
    FALL_WITH_INJURY,
    MEDICATION_EMERGENCY,
    COGNITIVE_CRISIS,
    GENERAL_MEDICAL
}

data class SimpleResponseProtocol(
    val priority: String,
    val requiresImmediateEMS: Boolean,
    val description: String
)

data class MedicalEmergencyStats(
    val totalMedicalEmergencies: Int = 0,
    val lastMedicalEmergency: Long? = null,
    val systemReady: Boolean = false,
    val activeContacts: Int = 0
)

sealed class SimpleMedicalEmergencyResult {
    data class Success(
        val emergencyResult: EmergencyResult,
        val responseProtocol: SimpleResponseProtocol,
        val responseTimeMs: Long,
        val contactsNotified: Int
    ) : SimpleMedicalEmergencyResult()
    
    data class Error(val message: String, val responseTimeMs: Long) : SimpleMedicalEmergencyResult()
}
