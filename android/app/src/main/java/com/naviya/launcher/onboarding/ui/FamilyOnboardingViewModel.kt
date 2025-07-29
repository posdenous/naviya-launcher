package com.naviya.launcher.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.naviya.launcher.onboarding.*

/**
 * ViewModel for family onboarding flow
 * Manages state and coordinates with FamilyOnboardingFlow
 */
@HiltViewModel
class FamilyOnboardingViewModel @Inject constructor(
    private val familyOnboardingFlow: FamilyOnboardingFlow
) : ViewModel() {
    
    // Expose flow states from FamilyOnboardingFlow
    val currentStep: StateFlow<OnboardingStep> = familyOnboardingFlow.currentStep
    val isLoading: StateFlow<Boolean> = familyOnboardingFlow.isLoading
    val errorMessage: StateFlow<String?> = familyOnboardingFlow.errorMessage
    val setupProgress: StateFlow<OnboardingProgress> = familyOnboardingFlow.setupProgress
    
    /**
     * Start family-assisted setup
     */
    fun startFamilyAssistedSetup(
        elderlyUserName: String,
        familyMemberName: String,
        relationship: String
    ) {
        viewModelScope.launch {
            familyOnboardingFlow.startFamilyAssistedSetup(
                elderlyUserName = elderlyUserName,
                familyMemberName = familyMemberName,
                relationship = relationship
            )
        }
    }
    
    /**
     * Configure basic preferences for elderly user
     */
    fun configureBasicPreferences(preferences: BasicPreferences) {
        viewModelScope.launch {
            familyOnboardingFlow.configureBasicPreferences(preferences)
        }
    }
    
    /**
     * Setup emergency contacts (required step)
     */
    fun setupEmergencyContacts(contacts: List<EmergencyContactInfo>) {
        viewModelScope.launch {
            familyOnboardingFlow.setupEmergencyContacts(contacts)
        }
    }
    
    /**
     * Optional caregiver pairing
     */
    fun optionalCaregiverPairing(caregiverInfo: CaregiverInfo?, userConsent: Boolean) {
        viewModelScope.launch {
            familyOnboardingFlow.optionalCaregiverPairing(caregiverInfo, userConsent)
        }
    }
    
    /**
     * Skip professional installation
     */
    fun skipProfessionalInstallation(reason: String = "family_assisted_setup") {
        viewModelScope.launch {
            familyOnboardingFlow.skipProfessionalInstallation(reason)
        }
    }
    
    /**
     * Complete onboarding process
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            familyOnboardingFlow.completeOnboarding()
        }
    }
    
    /**
     * Proceed to next step (for simple navigation)
     */
    fun proceedToNextStep() {
        viewModelScope.launch {
            when (currentStep.value) {
                OnboardingStep.FAMILY_INTRODUCTION -> {
                    // Move to basic preferences with elderly-friendly defaults
                    val elderlyDefaults = BasicPreferences(
                        fontScale = 1.6f,
                        iconScale = 1.4f,
                        highContrastEnabled = true,
                        hapticFeedbackEnabled = true,
                        slowAnimationsEnabled = true,
                        emergencyButtonAlwaysVisible = true
                    )
                    configureBasicPreferences(elderlyDefaults)
                }
                OnboardingStep.BASIC_PREFERENCES -> {
                    // Auto-proceed to emergency contacts
                    // This step is handled automatically after preferences are set
                }
                OnboardingStep.EMERGENCY_CONTACTS -> {
                    // Auto-proceed to optional caregiver
                    // This step is handled automatically after contacts are set
                }
                OnboardingStep.OPTIONAL_CAREGIVER -> {
                    // Auto-proceed to skip professional
                    skipProfessionalInstallation()
                }
                OnboardingStep.SKIP_PROFESSIONAL -> {
                    // Auto-proceed to completion
                    completeOnboarding()
                }
                else -> {
                    // No automatic progression for other steps
                }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        // This would be implemented in FamilyOnboardingFlow
        // For now, we can create a simple state management
    }
    
    /**
     * Reset onboarding flow
     */
    fun resetOnboarding() {
        viewModelScope.launch {
            familyOnboardingFlow.resetOnboarding()
        }
    }
    
    /**
     * Get current progress for UI display
     */
    fun getCurrentProgress(): OnboardingProgress {
        return familyOnboardingFlow.getOnboardingProgress()
    }
}
