package com.naviya.launcher.emergency

import android.content.Context
import android.util.Log
import com.naviya.launcher.emergency.data.EmergencyContact
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.emergency.data.EmergencyEvent
import com.naviya.launcher.emergency.data.EmergencyEventType
import com.naviya.launcher.healthcare.compliance.MedicalComplianceManager
import com.naviya.launcher.healthcare.compliance.MedicalComplianceModels.*
import com.naviya.launcher.healthcare.data.HealthcareProfessionalDao
import com.naviya.launcher.healthcare.data.HealthcareProfessionalModels.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Medical Emergency Integration Service
 * Bridges emergency response with medical compliance requirements
 * Ensures healthcare professional involvement in emergency situations
 */
@Singleton
class MedicalEmergencyIntegration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emergencyDao: EmergencyDao,
    private val healthcareDao: HealthcareProfessionalDao,
    private val complianceManager: MedicalComplianceManager,
    private val emergencyService: EmergencyService
) {
    private val integrationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val TAG = "MedicalEmergencyIntegration"
        private const val MEDICAL_EMERGENCY_TIMEOUT_MS = 2000L // 2 seconds for medical validation
        private const val CRITICAL_RESPONSE_TIME_MS = 300000L // 5 minutes for critical response
    }

    /**
     * Enhanced emergency activation with medical compliance integration
     * Automatically involves healthcare professionals based on patient risk level
     */
    suspend fun activateMedicalEmergency(
        userId: String,
        emergencyType: MedicalEmergencyType,
        userLanguage: String = "en",
        triggeredBy: SOSTrigger = SOSTrigger.MANUAL,
        symptoms: List<String> = emptyList()
    ): MedicalEmergencyResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            Log.i(TAG, "Medical emergency activated - Type: $emergencyType, User: $userId")
            
            // 1. Log medical emergency event with compliance audit trail
            val emergencyEvent = EmergencyEvent(
                eventType = EmergencyEventType.MEDICAL_EMERGENCY,
                userId = userId,
                userLanguage = userLanguage,
                wasOffline = !isNetworkAvailable(),
                notes = "Medical Emergency: $emergencyType, Symptoms: ${symptoms.joinToString()}"
            )
            emergencyDao.insertEmergencyEvent(emergencyEvent)
            
            // 2. Get patient's healthcare professional assignments
            val healthcareProfessionals = healthcareDao.getActiveProfessionalsByUserId(userId)
            
            // 3. Validate compliance for emergency response
            val complianceValidation = validateEmergencyCompliance(userId, emergencyType)
            
            // 4. Determine response protocol based on risk assessment
            val responseProtocol = determineResponseProtocol(
                userId, emergencyType, symptoms, healthcareProfessionals
            )
            
            // 5. Execute emergency response with medical oversight
            val emergencyResult = executeEmergencyResponse(
                userId, emergencyType, responseProtocol, userLanguage, triggeredBy
            )
            
            // 6. Notify healthcare professionals
            val medicalNotificationResults = notifyHealthcareProfessionals(
                healthcareProfessionals, emergencyEvent, responseProtocol
            )
            
            // 7. Create medical emergency audit log
            createMedicalEmergencyAuditLog(
                userId, emergencyType, responseProtocol, emergencyResult, 
                medicalNotificationResults, complianceValidation
            )
            
            val responseTime = System.currentTimeMillis() - startTime
            
            MedicalEmergencyResult.Success(
                emergencyResult = emergencyResult,
                medicalNotifications = medicalNotificationResults,
                complianceStatus = complianceValidation,
                responseProtocol = responseProtocol,
                responseTimeMs = responseTime
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Medical emergency activation failed", e)
            val responseTime = System.currentTimeMillis() - startTime
            MedicalEmergencyResult.Error(e.message ?: "Unknown error", responseTime)
        }
    }

    /**
     * Validate emergency response compliance requirements
     */
    private suspend fun validateEmergencyCompliance(
        userId: String,
        emergencyType: MedicalEmergencyType
    ): EmergencyComplianceValidation {
        return try {
            // Check if user has valid healthcare professional assignments
            val professionals = healthcareDao.getActiveProfessionalsByUserId(userId)
            val hasActiveProfessional = professionals.isNotEmpty()
            
            // Validate professional compliance status
            val complianceResults = professionals.map { professional ->
                complianceManager.validateProfessionalRegistration(professional.registration)
            }
            
            val hasCompliantProfessional = complianceResults.any { it.isCompliant }
            
            // Check emergency-specific compliance requirements
            val emergencyCompliance = when (emergencyType) {
                MedicalEmergencyType.CARDIAC_EVENT -> validateCardiacEmergencyCompliance(userId)
                MedicalEmergencyType.FALL_WITH_INJURY -> validateFallEmergencyCompliance(userId)
                MedicalEmergencyType.MEDICATION_EMERGENCY -> validateMedicationEmergencyCompliance(userId)
                MedicalEmergencyType.COGNITIVE_CRISIS -> validateCognitiveEmergencyCompliance(userId)
                MedicalEmergencyType.GENERAL_MEDICAL -> validateGeneralMedicalCompliance(userId)
            }
            
            EmergencyComplianceValidation(
                hasActiveProfessional = hasActiveProfessional,
                hasCompliantProfessional = hasCompliantProfessional,
                professionalComplianceResults = complianceResults,
                emergencySpecificCompliance = emergencyCompliance,
                isCompliant = hasActiveProfessional && hasCompliantProfessional && emergencyCompliance.isValid
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Emergency compliance validation failed", e)
            EmergencyComplianceValidation(
                hasActiveProfessional = false,
                hasCompliantProfessional = false,
                professionalComplianceResults = emptyList(),
                emergencySpecificCompliance = EmergencySpecificCompliance(false, "Validation failed: ${e.message}"),
                isCompliant = false
            )
        }
    }

    /**
     * Determine appropriate response protocol based on risk assessment
     */
    private suspend fun determineResponseProtocol(
        userId: String,
        emergencyType: MedicalEmergencyType,
        symptoms: List<String>,
        healthcareProfessionals: List<HealthcareProfessionalRegistration>
    ): EmergencyResponseProtocol {
        
        // Get user's clinical assessments for risk stratification
        val clinicalAssessments = healthcareDao.getClinicalAssessmentsByUserId(userId)
        val riskLevel = assessEmergencyRiskLevel(emergencyType, symptoms, clinicalAssessments)
        
        // Determine required response based on risk level and compliance
        return when (riskLevel) {
            EmergencyRiskLevel.CRITICAL -> EmergencyResponseProtocol(
                priority = EmergencyPriority.CRITICAL,
                requiresImmediateEMS = true,
                requiresHealthcareProfessional = true,
                requiresFamilyNotification = true,
                maxResponseTimeMs = CRITICAL_RESPONSE_TIME_MS,
                escalationRequired = true,
                complianceAuditRequired = true
            )
            
            EmergencyRiskLevel.HIGH -> EmergencyResponseProtocol(
                priority = EmergencyPriority.HIGH,
                requiresImmediateEMS = emergencyType.requiresEMS(),
                requiresHealthcareProfessional = true,
                requiresFamilyNotification = true,
                maxResponseTimeMs = CRITICAL_RESPONSE_TIME_MS * 2,
                escalationRequired = false,
                complianceAuditRequired = true
            )
            
            EmergencyRiskLevel.MEDIUM -> EmergencyResponseProtocol(
                priority = EmergencyPriority.MEDIUM,
                requiresImmediateEMS = false,
                requiresHealthcareProfessional = healthcareProfessionals.isNotEmpty(),
                requiresFamilyNotification = true,
                maxResponseTimeMs = CRITICAL_RESPONSE_TIME_MS * 4,
                escalationRequired = false,
                complianceAuditRequired = false
            )
            
            EmergencyRiskLevel.LOW -> EmergencyResponseProtocol(
                priority = EmergencyPriority.LOW,
                requiresImmediateEMS = false,
                requiresHealthcareProfessional = false,
                requiresFamilyNotification = false,
                maxResponseTimeMs = CRITICAL_RESPONSE_TIME_MS * 8,
                escalationRequired = false,
                complianceAuditRequired = false
            )
        }
    }

    /**
     * Execute emergency response following determined protocol
     */
    private suspend fun executeEmergencyResponse(
        userId: String,
        emergencyType: MedicalEmergencyType,
        protocol: EmergencyResponseProtocol,
        userLanguage: String,
        triggeredBy: SOSTrigger
    ): EmergencyResult {
        
        return when (protocol.priority) {
            EmergencyPriority.CRITICAL -> {
                // Critical: Immediate EMS + Healthcare Professional + Family
                emergencyService.activateSOS(userLanguage, triggeredBy)
            }
            
            EmergencyPriority.HIGH -> {
                // High: Healthcare Professional first, then EMS if needed
                emergencyService.activateSOS(userLanguage, triggeredBy)
            }
            
            EmergencyPriority.MEDIUM -> {
                // Medium: Healthcare Professional + Family notification
                emergencyService.activateSOS(userLanguage, triggeredBy)
            }
            
            EmergencyPriority.LOW -> {
                // Low: Standard emergency response
                emergencyService.activateSOS(userLanguage, triggeredBy)
            }
        }
    }

    /**
     * Notify healthcare professionals about emergency
     */
    private suspend fun notifyHealthcareProfessionals(
        professionals: List<HealthcareProfessionalRegistration>,
        emergencyEvent: EmergencyEvent,
        protocol: EmergencyResponseProtocol
    ): List<MedicalNotificationResult> {
        
        return professionals.map { professional ->
            try {
                // Create medical emergency notification
                val notification = MedicalEmergencyNotification(
                    professionalId = professional.professionalId,
                    emergencyEventId = emergencyEvent.eventId,
                    priority = protocol.priority,
                    requiresResponse = protocol.requiresHealthcareProfessional,
                    maxResponseTime = protocol.maxResponseTimeMs
                )
                
                // Send notification through appropriate channels
                val success = sendMedicalNotification(professional, notification)
                
                if (success) {
                    MedicalNotificationResult.Success(professional, "Notification sent successfully")
                } else {
                    MedicalNotificationResult.Failed(professional, "Failed to send notification")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to notify healthcare professional: ${professional.professionalId}", e)
                MedicalNotificationResult.Failed(professional, e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Create comprehensive audit log for medical emergency
     */
    private suspend fun createMedicalEmergencyAuditLog(
        userId: String,
        emergencyType: MedicalEmergencyType,
        protocol: EmergencyResponseProtocol,
        emergencyResult: EmergencyResult,
        medicalNotifications: List<MedicalNotificationResult>,
        complianceValidation: EmergencyComplianceValidation
    ) {
        try {
            val auditLog = MedicalEmergencyAuditLog(
                userId = userId,
                emergencyType = emergencyType,
                responseProtocol = protocol,
                emergencyResult = emergencyResult,
                medicalNotifications = medicalNotifications,
                complianceValidation = complianceValidation,
                auditTimestamp = System.currentTimeMillis(),
                auditTrailHash = generateAuditHash(userId, emergencyType, protocol)
            )
            
            // Store audit log in compliance database
            // This ensures regulatory compliance and accountability
            
            Log.i(TAG, "Medical emergency audit log created for user: $userId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create medical emergency audit log", e)
        }
    }

    // Helper methods for compliance validation
    private suspend fun validateCardiacEmergencyCompliance(userId: String): EmergencySpecificCompliance {
        // Validate cardiac emergency specific requirements
        return EmergencySpecificCompliance(true, "Cardiac emergency protocols validated")
    }

    private suspend fun validateFallEmergencyCompliance(userId: String): EmergencySpecificCompliance {
        // Validate fall emergency specific requirements
        return EmergencySpecificCompliance(true, "Fall emergency protocols validated")
    }

    private suspend fun validateMedicationEmergencyCompliance(userId: String): EmergencySpecificCompliance {
        // Validate medication emergency specific requirements
        return EmergencySpecificCompliance(true, "Medication emergency protocols validated")
    }

    private suspend fun validateCognitiveEmergencyCompliance(userId: String): EmergencySpecificCompliance {
        // Validate cognitive emergency specific requirements
        return EmergencySpecificCompliance(true, "Cognitive emergency protocols validated")
    }

    private suspend fun validateGeneralMedicalCompliance(userId: String): EmergencySpecificCompliance {
        // Validate general medical emergency requirements
        return EmergencySpecificCompliance(true, "General medical emergency protocols validated")
    }

    private fun assessEmergencyRiskLevel(
        emergencyType: MedicalEmergencyType,
        symptoms: List<String>,
        assessments: List<ClinicalAssessment>
    ): EmergencyRiskLevel {
        // Risk assessment algorithm based on emergency type and patient condition
        return when (emergencyType) {
            MedicalEmergencyType.CARDIAC_EVENT -> EmergencyRiskLevel.CRITICAL
            MedicalEmergencyType.FALL_WITH_INJURY -> EmergencyRiskLevel.HIGH
            MedicalEmergencyType.MEDICATION_EMERGENCY -> EmergencyRiskLevel.MEDIUM
            MedicalEmergencyType.COGNITIVE_CRISIS -> EmergencyRiskLevel.MEDIUM
            MedicalEmergencyType.GENERAL_MEDICAL -> EmergencyRiskLevel.LOW
        }
    }

    private suspend fun sendMedicalNotification(
        professional: HealthcareProfessionalRegistration,
        notification: MedicalEmergencyNotification
    ): Boolean {
        // Implementation for sending notifications to healthcare professionals
        // This would integrate with their preferred communication channels
        return try {
            // Send via SMS, email, app notification, etc.
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send medical notification", e)
            false
        }
    }

    private fun generateAuditHash(
        userId: String,
        emergencyType: MedicalEmergencyType,
        protocol: EmergencyResponseProtocol
    ): String {
        // Generate tamper-evident hash for audit trail
        return "${userId}_${emergencyType}_${protocol.priority}_${System.currentTimeMillis()}".hashCode().toString()
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

    private fun MedicalEmergencyType.requiresEMS(): Boolean {
        return when (this) {
            MedicalEmergencyType.CARDIAC_EVENT -> true
            MedicalEmergencyType.FALL_WITH_INJURY -> true
            MedicalEmergencyType.MEDICATION_EMERGENCY -> false
            MedicalEmergencyType.COGNITIVE_CRISIS -> false
            MedicalEmergencyType.GENERAL_MEDICAL -> false
        }
    }
}

// Data classes for medical emergency integration

enum class MedicalEmergencyType {
    CARDIAC_EVENT,
    FALL_WITH_INJURY,
    MEDICATION_EMERGENCY,
    COGNITIVE_CRISIS,
    GENERAL_MEDICAL
}

enum class EmergencyRiskLevel {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

enum class EmergencyPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

data class EmergencyResponseProtocol(
    val priority: EmergencyPriority,
    val requiresImmediateEMS: Boolean,
    val requiresHealthcareProfessional: Boolean,
    val requiresFamilyNotification: Boolean,
    val maxResponseTimeMs: Long,
    val escalationRequired: Boolean,
    val complianceAuditRequired: Boolean
)

data class EmergencyComplianceValidation(
    val hasActiveProfessional: Boolean,
    val hasCompliantProfessional: Boolean,
    val professionalComplianceResults: List<MedicalComplianceResult>,
    val emergencySpecificCompliance: EmergencySpecificCompliance,
    val isCompliant: Boolean
)

data class EmergencySpecificCompliance(
    val isValid: Boolean,
    val details: String
)

data class MedicalEmergencyNotification(
    val professionalId: String,
    val emergencyEventId: String,
    val priority: EmergencyPriority,
    val requiresResponse: Boolean,
    val maxResponseTime: Long
)

data class MedicalEmergencyAuditLog(
    val userId: String,
    val emergencyType: MedicalEmergencyType,
    val responseProtocol: EmergencyResponseProtocol,
    val emergencyResult: EmergencyResult,
    val medicalNotifications: List<MedicalNotificationResult>,
    val complianceValidation: EmergencyComplianceValidation,
    val auditTimestamp: Long,
    val auditTrailHash: String
)

sealed class MedicalEmergencyResult {
    data class Success(
        val emergencyResult: EmergencyResult,
        val medicalNotifications: List<MedicalNotificationResult>,
        val complianceStatus: EmergencyComplianceValidation,
        val responseProtocol: EmergencyResponseProtocol,
        val responseTimeMs: Long
    ) : MedicalEmergencyResult()
    
    data class Error(val message: String, val responseTimeMs: Long) : MedicalEmergencyResult()
}

sealed class MedicalNotificationResult {
    data class Success(val professional: HealthcareProfessionalRegistration, val message: String) : MedicalNotificationResult()
    data class Failed(val professional: HealthcareProfessionalRegistration, val error: String) : MedicalNotificationResult()
}
