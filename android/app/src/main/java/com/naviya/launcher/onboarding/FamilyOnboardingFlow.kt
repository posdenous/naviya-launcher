package com.naviya.launcher.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Family-friendly onboarding flow that prioritizes simplicity and user autonomy
 * Implements elderly-first philosophy with optional caregiver features
 */
@Singleton
class FamilyOnboardingFlow @Inject constructor(
    @ApplicationContext private val context: Context,
    private val onboardingDao: OnboardingDao,
    private val caregiverManager: CaregiverPermissionManager,
    private val emergencyService: EmergencyService
) {
    
    companion object {
        private const val TAG = "FamilyOnboardingFlow"
        private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.naviya.launcher"
        private const val FAMILY_SETUP_TIMEOUT_MS = 300000L // 5 minutes
    }
    
    private val _currentStep = MutableStateFlow(OnboardingStep.WELCOME)
    val currentStep: StateFlow<OnboardingStep> = _currentStep.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _setupProgress = MutableStateFlow(OnboardingProgress())
    val setupProgress: StateFlow<OnboardingProgress> = _setupProgress.asStateFlow()
    
    /**
     * Start family-assisted setup flow
     * Designed for adult children helping elderly parents
     */
    suspend fun startFamilyAssistedSetup(
        elderlyUserName: String,
        familyMemberName: String,
        relationship: String = "family_member"
    ): OnboardingResult {
        return try {
            _isLoading.value = true
            _currentStep.value = OnboardingStep.FAMILY_INTRODUCTION
            
            // Create user profile with family context
            val userProfile = createElderlyUserProfile(
                elderlyUserName = elderlyUserName,
                assistedBy = familyMemberName,
                relationship = relationship
            )
            
            // Save initial setup state
            onboardingDao.insertOnboardingState(
                OnboardingState(
                    userId = userProfile.userId,
                    currentStep = OnboardingStep.FAMILY_INTRODUCTION.name,
                    isAssistedSetup = true,
                    assistantName = familyMemberName,
                    assistantRelationship = relationship,
                    setupStartTime = System.currentTimeMillis()
                )
            )
            
            _setupProgress.value = _setupProgress.value.copy(
                currentStep = OnboardingStep.FAMILY_INTRODUCTION,
                elderlyUserName = elderlyUserName,
                familyAssistantName = familyMemberName,
                isAssistedSetup = true
            )
            
            OnboardingResult.Success("Family setup started successfully")
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to start family setup: ${e.message}"
            OnboardingResult.Error(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Configure basic accessibility and launcher preferences
     * Focuses on elderly-friendly defaults
     */
    suspend fun configureBasicPreferences(
        preferences: BasicPreferences
    ): OnboardingResult {
        return try {
            _isLoading.value = true
            _currentStep.value = OnboardingStep.BASIC_PREFERENCES
            
            // Apply elderly-friendly defaults
            val elderlyPreferences = preferences.copy(
                fontScale = maxOf(preferences.fontScale, 1.6f), // Minimum 1.6x for elderly
                iconScale = maxOf(preferences.iconScale, 1.4f), // Large icons
                highContrastEnabled = true, // Always enable for elderly
                hapticFeedbackEnabled = true, // Helpful for elderly users
                slowAnimationsEnabled = true, // Reduce motion for elderly
                emergencyButtonAlwaysVisible = true // Critical safety feature
            )
            
            // Save preferences
            onboardingDao.updateBasicPreferences(
                userId = _setupProgress.value.userId,
                preferences = elderlyPreferences
            )
            
            _setupProgress.value = _setupProgress.value.copy(
                basicPreferences = elderlyPreferences,
                stepsCompleted = _setupProgress.value.stepsCompleted + 1
            )
            
            OnboardingResult.Success("Basic preferences configured")
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to configure preferences: ${e.message}"
            OnboardingResult.Error(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Setup emergency contacts - REQUIRED step for safety
     */
    suspend fun setupEmergencyContacts(
        emergencyContacts: List<EmergencyContactInfo>
    ): OnboardingResult {
        return try {
            _isLoading.value = true
            _currentStep.value = OnboardingStep.EMERGENCY_CONTACTS
            
            if (emergencyContacts.isEmpty()) {
                return OnboardingResult.Error("At least one emergency contact is required")
            }
            
            // Validate and save emergency contacts
            val validatedContacts = emergencyContacts.map { contact ->
                validateEmergencyContact(contact)
            }
            
            // Save to emergency service
            validatedContacts.forEach { contact ->
                emergencyService.addEmergencyContact(contact)
            }
            
            _setupProgress.value = _setupProgress.value.copy(
                emergencyContacts = validatedContacts,
                stepsCompleted = _setupProgress.value.stepsCompleted + 1
            )
            
            OnboardingResult.Success("Emergency contacts configured")
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to setup emergency contacts: ${e.message}"
            OnboardingResult.Error(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Optional caregiver pairing - NOT required for basic functionality
     * User maintains full control and can skip this step
     */
    suspend fun optionalCaregiverPairing(
        caregiverInfo: CaregiverInfo?,
        userConsent: Boolean
    ): OnboardingResult {
        return try {
            _isLoading.value = true
            _currentStep.value = OnboardingStep.OPTIONAL_CAREGIVER
            
            if (caregiverInfo == null || !userConsent) {
                // User chose to skip caregiver pairing - this is perfectly fine
                _setupProgress.value = _setupProgress.value.copy(
                    caregiverPaired = false,
                    caregiverSkipped = true,
                    stepsCompleted = _setupProgress.value.stepsCompleted + 1
                )
                return OnboardingResult.Success("Caregiver pairing skipped - app works fully without caregiver")
            }
            
            // User chose to add caregiver with minimal permissions
            val caregiverResult = caregiverManager.addCaregiver(
                caregiverName = caregiverInfo.name,
                caregiverEmail = caregiverInfo.email,
                caregiverPhone = caregiverInfo.phone,
                userConsent = true,
                witnessId = _setupProgress.value.familyAssistantName // Family member as witness
            )
            
            if (caregiverResult.isSuccess) {
                _setupProgress.value = _setupProgress.value.copy(
                    caregiverPaired = true,
                    caregiverInfo = caregiverInfo,
                    stepsCompleted = _setupProgress.value.stepsCompleted + 1
                )
                OnboardingResult.Success("Caregiver paired with minimal permissions (emergency alerts only)")
            } else {
                OnboardingResult.Error("Failed to pair caregiver: ${caregiverResult.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Caregiver pairing failed: ${e.message}"
            OnboardingResult.Error(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Skip professional installation - family setup is sufficient
     */
    suspend fun skipProfessionalInstallation(
        reason: String = "family_assisted_setup"
    ): OnboardingResult {
        return try {
            _isLoading.value = true
            _currentStep.value = OnboardingStep.SKIP_PROFESSIONAL
            
            // Record that professional installation was skipped
            onboardingDao.updateOnboardingState(
                userId = _setupProgress.value.userId,
                updates = mapOf(
                    "professional_installation_skipped" to true,
                    "skip_reason" to reason,
                    "family_setup_sufficient" to true
                )
            )
            
            _setupProgress.value = _setupProgress.value.copy(
                professionalInstallationSkipped = true,
                skipReason = reason,
                stepsCompleted = _setupProgress.value.stepsCompleted + 1
            )
            
            OnboardingResult.Success("Professional installation skipped - family setup is sufficient")
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to skip professional installation: ${e.message}"
            OnboardingResult.Error(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Complete onboarding and activate launcher
     */
    suspend fun completeOnboarding(): OnboardingResult {
        return try {
            _isLoading.value = true
            _currentStep.value = OnboardingStep.COMPLETION
            
            val progress = _setupProgress.value
            
            // Validate required steps are completed
            if (!progress.hasEmergencyContacts()) {
                return OnboardingResult.Error("Emergency contacts are required before completing setup")
            }
            
            // Mark onboarding as complete
            onboardingDao.updateOnboardingState(
                userId = progress.userId,
                updates = mapOf(
                    "onboarding_completed" to true,
                    "completion_timestamp" to System.currentTimeMillis(),
                    "setup_method" to "family_assisted",
                    "total_setup_time_ms" to (System.currentTimeMillis() - progress.setupStartTime)
                )
            )
            
            // Activate launcher with elderly-friendly defaults
            activateElderlyLauncher(progress)
            
            _setupProgress.value = progress.copy(
                onboardingCompleted = true,
                completionTimestamp = System.currentTimeMillis()
            )
            
            _currentStep.value = OnboardingStep.LAUNCHER_READY
            
            OnboardingResult.Success("Onboarding completed successfully - launcher is ready to use")
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to complete onboarding: ${e.message}"
            OnboardingResult.Error(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Download from Play Store (helper for family members)
     */
    fun downloadFromPlayStore(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(PLAY_STORE_URL)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    /**
     * Get onboarding progress for UI display
     */
    fun getOnboardingProgress(): OnboardingProgress {
        return _setupProgress.value
    }
    
    /**
     * Reset onboarding flow (for testing or restart)
     */
    suspend fun resetOnboarding() {
        _currentStep.value = OnboardingStep.WELCOME
        _setupProgress.value = OnboardingProgress()
        _errorMessage.value = null
        _isLoading.value = false
    }
    
    // Private helper methods
    
    private fun createElderlyUserProfile(
        elderlyUserName: String,
        assistedBy: String,
        relationship: String
    ): UserProfile {
        return UserProfile(
            userId = java.util.UUID.randomUUID().toString(),
            userName = elderlyUserName,
            userType = UserType.ELDERLY,
            assistedSetup = true,
            assistantName = assistedBy,
            assistantRelationship = relationship,
            createdAt = System.currentTimeMillis()
        )
    }
    
    private fun validateEmergencyContact(contact: EmergencyContactInfo): EmergencyContact {
        // Validate phone number format
        if (!isValidPhoneNumber(contact.phone)) {
            throw IllegalArgumentException("Invalid phone number: ${contact.phone}")
        }
        
        return EmergencyContact(
            id = java.util.UUID.randomUUID().toString(),
            name = contact.name,
            phoneNumber = contact.phone,
            relationship = contact.relationship,
            isPrimary = contact.isPrimary,
            isActive = true
        )
    }
    
    private fun isValidPhoneNumber(phone: String): Boolean {
        // Basic phone number validation
        val phoneRegex = Regex("^[+]?[1-9]\\d{1,14}$")
        return phone.replace(Regex("[\\s()-]"), "").matches(phoneRegex)
    }
    
    private suspend fun activateElderlyLauncher(progress: OnboardingProgress) {
        // Set launcher as default with elderly-friendly mode
        val layoutManager = LayoutManager(context, /* dependencies */)
        layoutManager.switchToMode(
            mode = ToggleMode.COMFORT, // Start with COMFORT mode for elderly users
            screenWidth = context.resources.displayMetrics.widthPixels,
            screenHeight = context.resources.displayMetrics.heightPixels,
            userId = progress.userId
        )
        
        // Apply accessibility preferences
        progress.basicPreferences?.let { prefs ->
            applyAccessibilitySettings(prefs)
        }
    }
    
    private fun applyAccessibilitySettings(preferences: BasicPreferences) {
        // Apply elderly-friendly accessibility settings
        // This would integrate with Android accessibility services
    }
}

/**
 * Onboarding steps for family-assisted setup
 */
enum class OnboardingStep {
    WELCOME,
    FAMILY_INTRODUCTION,
    BASIC_PREFERENCES,
    EMERGENCY_CONTACTS,
    OPTIONAL_CAREGIVER,
    SKIP_PROFESSIONAL,
    COMPLETION,
    LAUNCHER_READY
}

/**
 * Result of onboarding operations
 */
sealed class OnboardingResult {
    data class Success(val message: String) : OnboardingResult()
    data class Error(val message: String) : OnboardingResult()
}

/**
 * Basic preferences for elderly users
 */
data class BasicPreferences(
    val fontScale: Float = 1.6f,
    val iconScale: Float = 1.4f,
    val highContrastEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val slowAnimationsEnabled: Boolean = true,
    val emergencyButtonAlwaysVisible: Boolean = true,
    val preferredLanguage: String = "en"
)

/**
 * Emergency contact information for setup
 */
data class EmergencyContactInfo(
    val name: String,
    val phone: String,
    val relationship: String,
    val isPrimary: Boolean = false
)

/**
 * Caregiver information for optional pairing
 */
data class CaregiverInfo(
    val name: String,
    val email: String,
    val phone: String,
    val relationship: String
)

/**
 * User profile for elderly users
 */
data class UserProfile(
    val userId: String,
    val userName: String,
    val userType: UserType,
    val assistedSetup: Boolean,
    val assistantName: String,
    val assistantRelationship: String,
    val createdAt: Long
)

enum class UserType {
    ELDERLY,
    CAREGIVER,
    FAMILY_MEMBER
}

/**
 * Onboarding progress tracking
 */
data class OnboardingProgress(
    val userId: String = "",
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val elderlyUserName: String = "",
    val familyAssistantName: String = "",
    val isAssistedSetup: Boolean = false,
    val basicPreferences: BasicPreferences? = null,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val caregiverPaired: Boolean = false,
    val caregiverSkipped: Boolean = false,
    val caregiverInfo: CaregiverInfo? = null,
    val professionalInstallationSkipped: Boolean = false,
    val skipReason: String = "",
    val onboardingCompleted: Boolean = false,
    val setupStartTime: Long = System.currentTimeMillis(),
    val completionTimestamp: Long? = null,
    val stepsCompleted: Int = 0
) {
    fun hasEmergencyContacts(): Boolean = emergencyContacts.isNotEmpty()
    
    fun getProgressPercentage(): Float {
        val totalSteps = 6 // Total required steps
        return (stepsCompleted.toFloat() / totalSteps) * 100f
    }
    
    fun getTimeSpentMinutes(): Long {
        val endTime = completionTimestamp ?: System.currentTimeMillis()
        return (endTime - setupStartTime) / 60000L
    }
}
