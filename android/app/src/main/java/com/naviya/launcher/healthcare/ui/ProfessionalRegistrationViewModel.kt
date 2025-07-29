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
 * ViewModel for Healthcare Professional Registration
 * Manages form state, validation, and registration process
 * Follows MVVM pattern with reactive state management
 */
@HiltViewModel
class ProfessionalRegistrationViewModel @Inject constructor(
    private val healthcareIntegrationService: HealthcareIntegrationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfessionalRegistrationUiState())
    val uiState: StateFlow<ProfessionalRegistrationUiState> = _uiState.asStateFlow()

    /**
     * Update a form field and validate
     */
    fun updateField(fieldName: String, value: String) {
        val currentState = _uiState.value
        val updatedState = when (fieldName) {
            "professionalId" -> currentState.copy(professionalId = value)
            "firstName" -> currentState.copy(firstName = value)
            "lastName" -> currentState.copy(lastName = value)
            "primaryPhone" -> currentState.copy(primaryPhone = value)
            "email" -> currentState.copy(email = value)
            "officeAddress" -> currentState.copy(officeAddress = value)
            "emergencyContact" -> currentState.copy(emergencyContact = value)
            "yearsOfExperience" -> currentState.copy(yearsOfExperience = value)
            "elderCareExperience" -> currentState.copy(elderCareExperience = value)
            "licenseNumbers" -> currentState.copy(licenseNumbers = value)
            "boardCertifications" -> currentState.copy(boardCertifications = value)
            "malpracticeInsurance" -> currentState.copy(malpracticeInsurance = value)
            else -> currentState
        }

        _uiState.value = updatedState.copy(
            fieldErrors = validateField(fieldName, value, updatedState),
            isFormValid = isFormValid(updatedState)
        )
    }

    /**
     * Update professional type
     */
    fun updateProfessionalType(type: ProfessionalType) {
        val currentState = _uiState.value
        val updatedState = currentState.copy(professionalType = type)
        
        _uiState.value = updatedState.copy(
            fieldErrors = currentState.fieldErrors - "professionalType",
            isFormValid = isFormValid(updatedState)
        )
    }

    /**
     * Add specialization
     */
    fun addSpecialization(specialization: String) {
        val currentState = _uiState.value
        if (specialization.isNotBlank() && !currentState.specializations.contains(specialization)) {
            _uiState.value = currentState.copy(
                specializations = currentState.specializations + specialization
            )
        }
    }

    /**
     * Remove specialization
     */
    fun removeSpecialization(specialization: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            specializations = currentState.specializations - specialization
        )
    }

    /**
     * Update institution information
     */
    fun updateInstitution(fieldName: String, value: String) {
        val currentState = _uiState.value
        val updatedState = when (fieldName) {
            "institutionName" -> currentState.copy(institutionName = value)
            "institutionAddress" -> currentState.copy(institutionAddress = value)
            else -> currentState
        }
        _uiState.value = updatedState
    }

    /**
     * Update credentials information
     */
    fun updateCredentials(fieldName: String, value: String) {
        updateField(fieldName, value)
    }

    /**
     * Register healthcare professional
     */
    fun registerProfessional() {
        val currentState = _uiState.value
        
        if (!currentState.isFormValid) {
            _uiState.value = currentState.copy(
                errorMessage = "Please complete all required fields correctly"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val professionalDetails = createProfessionalDetails(currentState)
                val credentials = createCredentials(currentState)
                val institution = createInstitution(currentState)

                val result = healthcareIntegrationService.registerHealthcareProfessional(
                    professionalDetails = professionalDetails,
                    credentials = credentials,
                    institution = institution
                )

                if (result.success) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        successMessage = buildSuccessMessage(result),
                        registrationId = result.registrationId
                    )
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Registration failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Registration failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Validate individual field
     */
    private fun validateField(
        fieldName: String, 
        value: String, 
        state: ProfessionalRegistrationUiState
    ): Map<String, String> {
        val errors = state.fieldErrors.toMutableMap()

        when (fieldName) {
            "professionalId" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "Professional ID is required"
                } else if (value.length < 3) {
                    errors[fieldName] = "Professional ID must be at least 3 characters"
                } else {
                    errors.remove(fieldName)
                }
            }
            "firstName" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "First name is required"
                } else if (value.length < 2) {
                    errors[fieldName] = "First name must be at least 2 characters"
                } else {
                    errors.remove(fieldName)
                }
            }
            "lastName" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "Last name is required"
                } else if (value.length < 2) {
                    errors[fieldName] = "Last name must be at least 2 characters"
                } else {
                    errors.remove(fieldName)
                }
            }
            "primaryPhone" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "Primary phone is required"
                } else if (!isValidPhoneNumber(value)) {
                    errors[fieldName] = "Please enter a valid phone number"
                } else {
                    errors.remove(fieldName)
                }
            }
            "email" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "Email is required"
                } else if (!isValidEmail(value)) {
                    errors[fieldName] = "Please enter a valid email address"
                } else {
                    errors.remove(fieldName)
                }
            }
            "officeAddress" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "Office address is required"
                } else if (value.length < 10) {
                    errors[fieldName] = "Please enter a complete address"
                } else {
                    errors.remove(fieldName)
                }
            }
            "yearsOfExperience" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "Years of experience is required"
                } else {
                    val years = value.toIntOrNull()
                    if (years == null || years < 0) {
                        errors[fieldName] = "Please enter a valid number of years"
                    } else if (years > 60) {
                        errors[fieldName] = "Years of experience seems too high"
                    } else {
                        errors.remove(fieldName)
                    }
                }
            }
            "elderCareExperience" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "Elder care experience is required"
                } else {
                    val years = value.toIntOrNull()
                    val totalYears = state.yearsOfExperience.toIntOrNull() ?: 0
                    if (years == null || years < 0) {
                        errors[fieldName] = "Please enter a valid number of years"
                    } else if (years > totalYears) {
                        errors[fieldName] = "Elder care experience cannot exceed total experience"
                    } else {
                        errors.remove(fieldName)
                    }
                }
            }
            "licenseNumbers" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "License numbers are required"
                } else if (value.length < 5) {
                    errors[fieldName] = "Please enter valid license numbers"
                } else {
                    errors.remove(fieldName)
                }
            }
            "malpracticeInsurance" -> {
                if (value.isBlank()) {
                    errors[fieldName] = "Malpractice insurance information is required"
                } else if (value.length < 5) {
                    errors[fieldName] = "Please enter valid insurance information"
                } else {
                    errors.remove(fieldName)
                }
            }
        }

        return errors
    }

    /**
     * Check if entire form is valid
     */
    private fun isFormValid(state: ProfessionalRegistrationUiState): Boolean {
        val requiredFields = listOf(
            "professionalId", "firstName", "lastName", "primaryPhone", 
            "email", "officeAddress", "yearsOfExperience", "elderCareExperience",
            "licenseNumbers", "malpracticeInsurance"
        )

        // Check required fields are not empty
        val hasRequiredFields = state.professionalId.isNotBlank() &&
                state.firstName.isNotBlank() &&
                state.lastName.isNotBlank() &&
                state.primaryPhone.isNotBlank() &&
                state.email.isNotBlank() &&
                state.officeAddress.isNotBlank() &&
                state.yearsOfExperience.isNotBlank() &&
                state.elderCareExperience.isNotBlank() &&
                state.licenseNumbers.isNotBlank() &&
                state.malpracticeInsurance.isNotBlank()

        // Check professional type is selected
        val hasProfessionalType = state.professionalType != null

        // Check no validation errors
        val hasNoErrors = requiredFields.none { state.fieldErrors.containsKey(it) } &&
                !state.fieldErrors.containsKey("professionalType")

        return hasRequiredFields && hasProfessionalType && hasNoErrors
    }

    /**
     * Create professional details from UI state
     */
    private fun createProfessionalDetails(state: ProfessionalRegistrationUiState): HealthcareProfessionalDetails {
        return HealthcareProfessionalDetails(
            professionalId = state.professionalId,
            firstName = state.firstName,
            lastName = state.lastName,
            professionalType = state.professionalType!!,
            specializations = state.specializations,
            licenseNumbers = state.licenseNumbers.split(",").map { it.trim() },
            contactInformation = ProfessionalContactInfo(
                primaryPhone = state.primaryPhone,
                email = state.email,
                officeAddress = state.officeAddress,
                emergencyContact = state.emergencyContact.takeIf { it.isNotBlank() }
            ),
            yearsOfExperience = state.yearsOfExperience.toInt(),
            elderCareExperience = state.elderCareExperience.toInt()
        )
    }

    /**
     * Create credentials from UI state
     */
    private fun createCredentials(state: ProfessionalRegistrationUiState): ProfessionalCredentials {
        return ProfessionalCredentials(
            professionalType = state.professionalType!!,
            licenseNumbers = state.licenseNumbers.split(",").map { it.trim() },
            boardCertifications = if (state.boardCertifications.isNotBlank()) {
                state.boardCertifications.split(",").map { it.trim() }
            } else emptyList(),
            specializations = state.specializations,
            institutionalAffiliations = if (state.institutionName.isNotBlank()) {
                listOf(state.institutionName)
            } else emptyList(),
            malpracticeInsurance = createMalpracticeInsurance(state.malpracticeInsurance),
            backgroundCheckStatus = BackgroundCheckStatus.PENDING,
            continuingEducationStatus = ContinuingEducationStatus.PENDING,
            credentialVerificationDate = System.currentTimeMillis(),
            credentialExpirationDate = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L), // 1 year
            isValid = false // Will be validated during registration process
        )
    }

    /**
     * Create institution from UI state
     */
    private fun createInstitution(state: ProfessionalRegistrationUiState): HealthcareInstitution? {
        if (state.institutionName.isBlank()) return null

        return HealthcareInstitution(
            institutionId = "inst-${System.currentTimeMillis()}", // Temporary ID
            institutionName = state.institutionName,
            institutionType = InstitutionType.HOSPITAL, // Default, can be enhanced
            address = state.institutionAddress,
            contactInformation = state.primaryPhone, // Use professional's phone as default
            accreditation = emptyList(), // To be filled during verification
            specializations = state.specializations,
            elderCareServices = emptyList(), // To be filled during verification
            isActive = true
        )
    }

    /**
     * Create malpractice insurance record
     */
    private fun createMalpracticeInsurance(insuranceInfo: String): InsuranceRecord {
        // Parse insurance information (simplified for now)
        return InsuranceRecord(
            insuranceProvider = "Provider from: $insuranceInfo",
            policyNumber = "Policy from: $insuranceInfo",
            coverageAmount = 1000000.0, // Default coverage
            effectiveDate = System.currentTimeMillis(),
            expirationDate = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L),
            isActive = true
        )
    }

    /**
     * Build success message from registration result
     */
    private fun buildSuccessMessage(result: HealthcareIntegrationService.ProfessionalRegistrationResult): String {
        val message = StringBuilder("Registration submitted successfully!")
        
        if (result.verificationRequired) {
            message.append("\n\nNext steps:")
            result.nextSteps.forEach { step ->
                message.append("\n• $step")
            }
        }

        if (result.trainingRequired) {
            message.append("\n\nTraining will be required before installation authorization.")
        }

        return message.toString()
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validate phone number format
     */
    private fun isValidPhoneNumber(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches() && phone.length >= 10
    }
}
