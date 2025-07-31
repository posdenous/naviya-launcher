package com.naviya.launcher.healthcare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.naviya.launcher.healthcare.HealthcareIntegrationService
import com.naviya.launcher.healthcare.data.*
import javax.inject.Inject

/**
 * ViewModel for Professional Installation Screen
 * Manages multi-step installation workflow with validation and state management
 */
@HiltViewModel
class ProfessionalInstallationViewModel @Inject constructor(
    private val healthcareIntegrationService: HealthcareIntegrationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfessionalInstallationUiState())
    val uiState: StateFlow<ProfessionalInstallationUiState> = _uiState.asStateFlow()

    /**
     * Initialize installation with user and professional data
     */
    fun initializeInstallation(userId: String, professionalId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingMessage = "Initializing installation..."
            )

            try {
                // Load professional and user data
                val professionalData = loadProfessionalData(professionalId)
                val userData = loadUserData(userId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    userId = userId,
                    professionalId = professionalId,
                    professionalName = "${professionalData.firstName} ${professionalData.lastName}",
                    patientName = userData.name,
                    currentStep = 1,
                    totalSteps = 6,
                    completedSteps = 0,
                    // Initialize authorization checks
                    professionalAuthorized = professionalData.installationAuthorized,
                    institutionVerified = professionalData.institutionVerified,
                    credentialsVerified = professionalData.credentialsValid,
                    authorizationDetails = "Professional ID: ${professionalData.professionalId}",
                    institutionDetails = professionalData.institutionName,
                    credentialsDetails = "License: ${professionalData.licenseNumbers.joinToString()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to initialize installation: ${e.message}"
                )
            }
        }
    }

    /**
     * Complete authorization verification step
     */
    fun completeAuthorizationVerification() {
        val currentState = _uiState.value
        if (currentState.allAuthorizationsVerified) {
            _uiState.value = currentState.copy(
                completedSteps = 1,
                currentStep = 2
            )
        }
    }

    /**
     * Update patient consent information
     */
    fun updatePatientConsent(field: String, value: Any) {
        val currentState = _uiState.value
        val updatedState = when (field) {
            "consentMethod" -> currentState.copy(
                consentMethod = value as ConsentMethod,
                witnessRequired = (value == ConsentMethod.WITNESSED_CONSENT || value == ConsentMethod.GUARDIAN_CONSENT)
            )
            "consentConfirmed" -> currentState.copy(consentConfirmed = value as Boolean)
            "witnessName" -> currentState.copy(witnessName = value as String)
            else -> currentState
        }

        _uiState.value = updatedState.copy(
            canProceedFromConsent = validateConsentStep(updatedState)
        )
    }

    /**
     * Complete patient consent step
     */
    fun completePatientConsent() {
        val currentState = _uiState.value
        if (currentState.canProceedFromConsent) {
            _uiState.value = currentState.copy(
                completedSteps = 2,
                currentStep = 3,
                consentText = generateConsentText(currentState)
            )
        }
    }

    /**
     * Update clinical context information
     */
    fun updateClinicalContext(field: String, value: Any) {
        val currentState = _uiState.value
        val updatedState = when (field) {
            "clinicalContext" -> currentState.copy(clinicalContext = value as ClinicalContext)
            "installationType" -> currentState.copy(installationType = value as InstallationType)
            "clinicalNotes" -> currentState.copy(clinicalNotes = value as String)
            else -> currentState
        }

        _uiState.value = updatedState.copy(
            canProceedFromClinicalContext = validateClinicalContextStep(updatedState)
        )
    }

    /**
     * Complete clinical context step
     */
    fun completeClinicalContext() {
        val currentState = _uiState.value
        if (currentState.canProceedFromClinicalContext) {
            _uiState.value = currentState.copy(
                completedSteps = 3,
                currentStep = 4
            )
        }
    }

    /**
     * Complete system configuration step
     */
    fun completeSystemConfiguration() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                isLoading = true,
                loadingMessage = "Configuring system settings..."
            )

            try {
                // Simulate system configuration
                kotlinx.coroutines.delay(2000)

                _uiState.value = currentState.copy(
                    isLoading = false,
                    completedSteps = 4,
                    currentStep = 5,
                    systemConfigured = true
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "System configuration failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Complete safety protocols step
     */
    fun completeSafetyProtocols() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            completedSteps = 5,
            currentStep = 6,
            safetyProtocolsConfigured = true
        )
    }

    /**
     * Complete installation process
     */
    fun completeInstallation() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                isLoading = true,
                loadingMessage = "Finalizing installation..."
            )

            try {
                val installationRequest = createInstallationRequest(currentState)
                val result = healthcareIntegrationService.performProfessionalInstallation(installationRequest)

                if (result.success) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        completedSteps = 6,
                        successMessage = buildInstallationSuccessMessage(result),
                        installationId = result.installationId
                    )
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Installation failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Installation failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Navigate to previous step
     */
    fun previousStep() {
        val currentState = _uiState.value
        if (currentState.canGoToPreviousStep) {
            _uiState.value = currentState.copy(
                currentStep = currentState.currentStep - 1,
                completedSteps = maxOf(0, currentState.completedSteps - 1)
            )
        }
    }

    /**
     * Navigate to next step
     */
    fun nextStep() {
        val currentState = _uiState.value
        if (currentState.canGoToNextStep && currentState.canProceedToNextStep) {
            when (currentState.currentStep) {
                1 -> completeAuthorizationVerification()
                2 -> completePatientConsent()
                3 -> completeClinicalContext()
                4 -> completeSystemConfiguration()
                5 -> completeSafetyProtocols()
                6 -> completeInstallation()
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Private helper methods

    private suspend fun loadProfessionalData(professionalId: String): ProfessionalData {
        // Simulate loading professional data
        return ProfessionalData(
            professionalId = professionalId,
            firstName = "Dr. Jane",
            lastName = "Smith",
            installationAuthorized = true,
            institutionVerified = true,
            credentialsValid = true,
            institutionName = "General Hospital",
            licenseNumbers = listOf("MD123456")
        )
    }

    private suspend fun loadUserData(userId: String): UserData {
        // Simulate loading user data
        return UserData(
            userId = userId,
            name = "John Doe"
        )
    }

    private fun validateConsentStep(state: ProfessionalInstallationUiState): Boolean {
        return state.consentMethod != null &&
                state.consentConfirmed &&
                (!state.witnessRequired || state.witnessName.isNotBlank())
    }

    private fun validateClinicalContextStep(state: ProfessionalInstallationUiState): Boolean {
        return state.clinicalContext != null &&
                state.installationType != null &&
                state.clinicalNotes.isNotBlank()
    }

    private fun generateConsentText(state: ProfessionalInstallationUiState): String {
        return """
            I, ${state.patientName}, hereby consent to the installation and configuration of the Naviya Elder Protection System by ${state.professionalName}.
            
            I understand that:
            • This system is designed to enhance my safety and well-being
            • Healthcare professionals will have access to necessary monitoring data
            • I can modify or revoke permissions at any time
            • My privacy and autonomy will be respected
            • Emergency contacts and elder rights advocates will be notified as appropriate
            
            Consent method: ${state.consentMethod?.name?.replace("_", " ")?.lowercase()}
            ${if (state.witnessRequired) "Witness: ${state.witnessName}" else ""}
            
            Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}
        """.trimIndent()
    }

    private fun createInstallationRequest(state: ProfessionalInstallationUiState): HealthcareIntegrationService.ProfessionalInstallationRequest {
        return HealthcareIntegrationService.ProfessionalInstallationRequest(
            userId = state.userId,
            professionalId = state.professionalId,
            institutionId = "inst-123", // Would be loaded from professional data
            installationType = state.installationType ?: InstallationType.INITIAL_INSTALLATION,
            clinicalContext = state.clinicalContext ?: ClinicalContext.HOME_CARE,
            patientConsent = ConsentRecord(
                consentId = "consent-${System.currentTimeMillis()}",
                consentType = ConsentType.TREATMENT_CONSENT,
                consentTimestamp = System.currentTimeMillis(),
                consentMethod = state.consentMethod ?: ConsentMethod.DIGITAL_SIGNATURE,
                consentText = state.consentText,
                isValid = true
            ),
            familyConsent = null, // Would be handled if family consent was obtained
            initialClinicalNotes = state.clinicalNotes
        )
    }

    private fun buildInstallationSuccessMessage(result: HealthcareIntegrationService.ProfessionalInstallationResult): String {
        val message = StringBuilder("Installation completed successfully!")
        
        message.append("\n\nInstallation ID: ${result.installationId}")
        
        if (result.followUpRequired) {
            message.append("\n\nFollow-up actions:")
            result.pendingSteps.forEach { step ->
                message.append("\n• $step")
            }
        }

        message.append("\n\nThe elder rights advocate has been notified of this installation.")
        
        return message.toString()
    }

    // Data classes for loading
    private data class ProfessionalData(
        val professionalId: String,
        val firstName: String,
        val lastName: String,
        val installationAuthorized: Boolean,
        val institutionVerified: Boolean,
        val credentialsValid: Boolean,
        val institutionName: String,
        val licenseNumbers: List<String>
    )

    private data class UserData(
        val userId: String,
        val name: String
    )
}

// UI State Data Class
data class ProfessionalInstallationUiState(
    val isInitialized: Boolean = false,
    val userId: String = "",
    val professionalId: String = "",
    val professionalName: String = "",
    val patientName: String = "",
    
    // Progress tracking
    val currentStep: Int = 1,
    val totalSteps: Int = 6,
    val completedSteps: Int = 0,
    
    // Step 1: Authorization Verification
    val professionalAuthorized: Boolean = false,
    val institutionVerified: Boolean = false,
    val credentialsVerified: Boolean = false,
    val authorizationDetails: String? = null,
    val institutionDetails: String? = null,
    val credentialsDetails: String? = null,
    val allAuthorizationsVerified: Boolean = false,
    
    // Step 2: Patient Consent
    val consentMethod: ConsentMethod? = null,
    val consentConfirmed: Boolean = false,
    val witnessRequired: Boolean = false,
    val witnessName: String = "",
    val consentText: String = "",
    val canProceedFromConsent: Boolean = false,
    
    // Step 3: Clinical Context
    val clinicalContext: ClinicalContext? = null,
    val installationType: InstallationType? = null,
    val clinicalNotes: String = "",
    val canProceedFromClinicalContext: Boolean = false,
    
    // Step 4: System Configuration
    val systemConfigured: Boolean = false,
    
    // Step 5: Safety Protocols
    val safetyProtocolsConfigured: Boolean = false,
    
    // Navigation
    val canGoToPreviousStep: Boolean = false,
    val canGoToNextStep: Boolean = false,
    val canProceedToNextStep: Boolean = false,
    val isLastStep: Boolean = false,
    
    // Status
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val installationId: String? = null
) {
    val allAuthorizationsVerified: Boolean
        get() = professionalAuthorized && institutionVerified && credentialsVerified
    
    val canGoToPreviousStep: Boolean
        get() = currentStep > 1
    
    val canGoToNextStep: Boolean
        get() = currentStep < totalSteps
    
    val isLastStep: Boolean
        get() = currentStep == totalSteps
    
    val canProceedToNextStep: Boolean
        get() = when (currentStep) {
            1 -> allAuthorizationsVerified
            2 -> canProceedFromConsent
            3 -> canProceedFromClinicalContext
            4 -> systemConfigured
            5 -> safetyProtocolsConfigured
            6 -> true
            else -> false
        }
}
