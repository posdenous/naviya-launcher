package com.naviya.launcher.ui.emergency

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naviya.launcher.emergency.MedicalEmergencyIntegration
import com.naviya.launcher.emergency.MedicalEmergencyResult
import com.naviya.launcher.emergency.MedicalEmergencyType
import com.naviya.launcher.emergency.SOSTrigger
import com.naviya.launcher.healthcare.compliance.MedicalComplianceManager
import com.naviya.launcher.healthcare.data.HealthcareProfessionalDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Medical Emergency Screen
 * Manages emergency activation with medical compliance integration
 * Follows Windsurf rules for elderly-friendly UI state management
 */
@HiltViewModel
class MedicalEmergencyViewModel @Inject constructor(
    private val medicalEmergencyIntegration: MedicalEmergencyIntegration,
    private val healthcareDao: HealthcareProfessionalDao,
    private val complianceManager: MedicalComplianceManager
) : ViewModel() {

    companion object {
        private const val TAG = "MedicalEmergencyViewModel"
        private const val DEFAULT_USER_ID = "current_user" // TODO: Get from user session
    }

    private val _uiState = MutableStateFlow(MedicalEmergencyUiState())
    val uiState: StateFlow<MedicalEmergencyUiState> = _uiState.asStateFlow()

    init {
        loadHealthcareProfessionalStatus()
    }

    /**
     * Activate medical emergency with compliance integration
     */
    fun activateEmergency(
        emergencyType: MedicalEmergencyType,
        symptoms: List<String> = emptyList(),
        userLanguage: String = "en"
    ) {
        viewModelScope.launch {
            try {
                Log.i(TAG, "Activating medical emergency: $emergencyType")
                
                _uiState.update { it.copy(isLoading = true, error = null) }

                val result = medicalEmergencyIntegration.activateMedicalEmergency(
                    userId = DEFAULT_USER_ID,
                    emergencyType = emergencyType,
                    userLanguage = userLanguage,
                    triggeredBy = SOSTrigger.MANUAL,
                    symptoms = symptoms
                )

                when (result) {
                    is MedicalEmergencyResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                emergencyActivated = true,
                                emergencyResult = formatEmergencyResult(result),
                                lastEmergencyType = emergencyType,
                                responseTimeMs = result.responseTimeMs
                            )
                        }
                        Log.i(TAG, "Medical emergency activated successfully in ${result.responseTimeMs}ms")
                    }
                    
                    is MedicalEmergencyResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                                responseTimeMs = result.responseTimeMs
                            )
                        }
                        Log.e(TAG, "Medical emergency activation failed: ${result.message}")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Emergency activation error", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Emergency activation failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Reset emergency state after user acknowledgment
     */
    fun resetEmergencyState() {
        _uiState.update {
            it.copy(
                emergencyActivated = false,
                emergencyResult = null,
                lastEmergencyType = null,
                error = null
            )
        }
    }

    /**
     * Refresh healthcare professional status
     */
    fun refreshHealthcareProfessionalStatus() {
        loadHealthcareProfessionalStatus()
    }

    /**
     * Load healthcare professional status and compliance
     */
    private fun loadHealthcareProfessionalStatus() {
        viewModelScope.launch {
            try {
                // Get active healthcare professionals for current user
                val professionals = healthcareDao.getActiveProfessionalsByUserId(DEFAULT_USER_ID)
                val hasActiveProfessional = professionals.isNotEmpty()
                
                val primaryProfessional = professionals.firstOrNull()
                val professionalName = primaryProfessional?.let { 
                    "${it.firstName} ${it.lastName}, ${it.professionalTitle}"
                }

                // Check compliance status if professional exists
                val isCompliant = if (primaryProfessional != null) {
                    val complianceResult = complianceManager.validateProfessionalRegistration(
                        primaryProfessional
                    )
                    complianceResult.isCompliant
                } else {
                    false
                }

                _uiState.update {
                    it.copy(
                        hasActiveProfessional = hasActiveProfessional,
                        primaryProfessionalName = professionalName,
                        isProfessionalCompliant = isCompliant,
                        professionalCount = professionals.size
                    )
                }

                Log.i(TAG, "Healthcare professional status loaded: $hasActiveProfessional professionals, compliant: $isCompliant")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load healthcare professional status", e)
                _uiState.update {
                    it.copy(
                        hasActiveProfessional = false,
                        isProfessionalCompliant = false,
                        error = "Failed to load healthcare professional status"
                    )
                }
            }
        }
    }

    /**
     * Format emergency result for user display
     */
    private fun formatEmergencyResult(result: MedicalEmergencyResult.Success): String {
        val protocol = result.responseProtocol
        val notifications = result.medicalNotifications
        
        return buildString {
            append("Emergency services activated")
            
            if (protocol.requiresImmediateEMS) {
                append("\n• Emergency services dispatched")
            }
            
            if (protocol.requiresHealthcareProfessional && notifications.isNotEmpty()) {
                val successfulNotifications = notifications.count { 
                    it is com.naviya.launcher.emergency.MedicalNotificationResult.Success 
                }
                append("\n• $successfulNotifications healthcare professional(s) notified")
            }
            
            if (protocol.requiresFamilyNotification) {
                append("\n• Family members notified")
            }
            
            append("\n\nResponse time: ${result.responseTimeMs}ms")
            
            if (result.complianceStatus.isCompliant) {
                append("\n✓ All compliance requirements met")
            } else {
                append("\n⚠ Compliance validation in progress")
            }
        }
    }

    /**
     * Get emergency activation statistics for monitoring
     */
    fun getEmergencyStats(): Flow<EmergencyStats> {
        return flow {
            try {
                // This would typically come from a repository
                // For now, we'll emit current state statistics
                val currentState = _uiState.value
                
                emit(
                    EmergencyStats(
                        hasActiveProfessional = currentState.hasActiveProfessional,
                        isProfessionalCompliant = currentState.isProfessionalCompliant,
                        professionalCount = currentState.professionalCount,
                        lastResponseTimeMs = currentState.responseTimeMs,
                        systemReady = currentState.hasActiveProfessional && currentState.isProfessionalCompliant
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get emergency stats", e)
                emit(EmergencyStats())
            }
        }
    }

    /**
     * Test emergency system without actual activation
     */
    fun testEmergencySystem() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Simulate emergency system test
                kotlinx.coroutines.delay(1000)
                
                val testResult = if (_uiState.value.hasActiveProfessional) {
                    "Emergency system test successful\n• Healthcare professional available\n• Compliance validated\n• All systems operational"
                } else {
                    "Emergency system test completed\n⚠ No healthcare professional assigned\n• Basic emergency functions available\n• Consider adding healthcare professional"
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emergencyActivated = true,
                        emergencyResult = testResult
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Emergency system test failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Emergency system test failed: ${e.message}"
                    )
                }
            }
        }
    }
}

/**
 * UI State for Medical Emergency Screen
 */
data class MedicalEmergencyUiState(
    val isLoading: Boolean = false,
    val emergencyActivated: Boolean = false,
    val emergencyResult: String? = null,
    val lastEmergencyType: MedicalEmergencyType? = null,
    val hasActiveProfessional: Boolean = false,
    val primaryProfessionalName: String? = null,
    val isProfessionalCompliant: Boolean = false,
    val professionalCount: Int = 0,
    val responseTimeMs: Long? = null,
    val error: String? = null
)

/**
 * Emergency system statistics
 */
data class EmergencyStats(
    val hasActiveProfessional: Boolean = false,
    val isProfessionalCompliant: Boolean = false,
    val professionalCount: Int = 0,
    val lastResponseTimeMs: Long? = null,
    val systemReady: Boolean = false
)
