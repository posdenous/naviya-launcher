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
 * ViewModel for Clinical Assessment Screen
 * Manages comprehensive clinical assessment workflow with real-time validation
 */
@HiltViewModel
class ClinicalAssessmentViewModel @Inject constructor(
    private val healthcareIntegrationService: HealthcareIntegrationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClinicalAssessmentUiState())
    val uiState: StateFlow<ClinicalAssessmentUiState> = _uiState.asStateFlow()

    /**
     * Initialize assessment with user and professional data
     */
    fun initializeAssessment(userId: String, professionalId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingMessage = "Loading assessment data..."
            )

            try {
                val professionalData = loadProfessionalData(professionalId)
                val userData = loadUserData(userId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    userId = userId,
                    professionalId = professionalId,
                    professionalName = "${professionalData.firstName} ${professionalData.lastName}",
                    patientName = userData.name
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to initialize assessment: ${e.message}"
                )
            }
        }
    }

    /**
     * Update cognitive assessment
     */
    fun updateCognitiveAssessment(field: String, value: Any) {
        val currentState = _uiState.value
        val currentAssessment = currentState.cognitiveAssessment
        
        val updatedAssessment = when (field) {
            "assessmentTool" -> currentAssessment.copy(assessmentTool = value as String)
            "score" -> currentAssessment.copy(score = value as String)
            "interpretation" -> currentAssessment.copy(interpretation = value as String)
            "cognitiveImpairmentLevel" -> currentAssessment.copy(cognitiveImpairmentLevel = value as CognitiveImpairmentLevel?)
            "decisionMakingCapacity" -> currentAssessment.copy(decisionMakingCapacity = value as DecisionMakingCapacity?)
            else -> currentAssessment
        }

        _uiState.value = currentState.copy(
            cognitiveAssessment = updatedAssessment,
            hasChanges = true
        )
        
        updateAssessmentCompleteness()
    }

    /**
     * Update functional assessment
     */
    fun updateFunctionalAssessment(field: String, value: Any) {
        val currentState = _uiState.value
        val currentAssessment = currentState.functionalAssessment
        
        val updatedAssessment = when {
            field.startsWith("adl_") -> {
                val activity = field.removePrefix("adl_")
                currentAssessment.copy(
                    adlLevels = currentAssessment.adlLevels + (activity to (value as FunctionalLevel))
                )
            }
            field.startsWith("iadl_") -> {
                val activity = field.removePrefix("iadl_")
                currentAssessment.copy(
                    iadlLevels = currentAssessment.iadlLevels + (activity to (value as FunctionalLevel))
                )
            }
            field == "mobilityStatus" -> currentAssessment.copy(mobilityStatus = value as MobilityStatus?)
            field == "fallRisk" -> currentAssessment.copy(fallRisk = value as FallRiskLevel?)
            else -> currentAssessment
        }

        _uiState.value = currentState.copy(
            functionalAssessment = updatedAssessment,
            hasChanges = true
        )
        
        updateAssessmentCompleteness()
    }

    /**
     * Update social assessment
     */
    fun updateSocialAssessment(field: String, value: Any) {
        val currentState = _uiState.value
        val currentAssessment = currentState.socialAssessment
        
        val updatedAssessment = when (field) {
            "socialSupport" -> currentAssessment.copy(socialSupport = value as SocialSupportLevel?)
            "socialIsolation" -> currentAssessment.copy(socialIsolation = value as SocialIsolationLevel?)
            "familyDynamics" -> currentAssessment.copy(familyDynamics = value as String)
            "caregiverRelationships" -> currentAssessment.copy(caregiverRelationships = value as String)
            "addCommunityResource" -> {
                val resource = value as String
                if (resource.isNotBlank() && !currentAssessment.communityResources.contains(resource)) {
                    currentAssessment.copy(
                        communityResources = currentAssessment.communityResources + resource
                    )
                } else currentAssessment
            }
            "removeCommunityResource" -> {
                val resource = value as String
                currentAssessment.copy(
                    communityResources = currentAssessment.communityResources - resource
                )
            }
            else -> currentAssessment
        }

        _uiState.value = currentState.copy(
            socialAssessment = updatedAssessment,
            hasChanges = true
        )
        
        updateAssessmentCompleteness()
    }

    /**
     * Update risk factor assessment
     */
    fun updateRiskFactorAssessment(field: String, value: Any) {
        val currentState = _uiState.value
        val currentAssessment = currentState.riskFactorAssessment
        
        val updatedAssessment = when (field) {
            "toggleAbuseRiskFactor" -> {
                val (factor, selected) = value as Pair<String, Boolean>
                currentAssessment.copy(
                    abuseRiskFactors = if (selected) {
                        currentAssessment.abuseRiskFactors + factor
                    } else {
                        currentAssessment.abuseRiskFactors - factor
                    }
                )
            }
            "toggleNeglectRiskFactor" -> {
                val (factor, selected) = value as Pair<String, Boolean>
                currentAssessment.copy(
                    neglectRiskFactors = if (selected) {
                        currentAssessment.neglectRiskFactors + factor
                    } else {
                        currentAssessment.neglectRiskFactors - factor
                    }
                )
            }
            "toggleExploitationRiskFactor" -> {
                val (factor, selected) = value as Pair<String, Boolean>
                currentAssessment.copy(
                    exploitationRiskFactors = if (selected) {
                        currentAssessment.exploitationRiskFactors + factor
                    } else {
                        currentAssessment.exploitationRiskFactors - factor
                    }
                )
            }
            "overallRiskLevel" -> currentAssessment.copy(overallRiskLevel = value as OverallRiskLevel?)
            "addProtectiveFactor" -> {
                val factor = value as String
                if (factor.isNotBlank() && !currentAssessment.protectiveFactors.contains(factor)) {
                    currentAssessment.copy(
                        protectiveFactors = currentAssessment.protectiveFactors + factor
                    )
                } else currentAssessment
            }
            "removeProtectiveFactor" -> {
                val factor = value as String
                currentAssessment.copy(
                    protectiveFactors = currentAssessment.protectiveFactors - factor
                )
            }
            else -> currentAssessment
        }

        _uiState.value = currentState.copy(
            riskFactorAssessment = updatedAssessment,
            hasChanges = true
        )
        
        updateRiskSummary()
        updateAssessmentCompleteness()
    }

    /**
     * Update caregiver assessment
     */
    fun updateCaregiverAssessment(field: String, value: Any) {
        val currentState = _uiState.value
        val currentAssessment = currentState.caregiverAssessment
        
        val updatedAssessment = when (field) {
            "caregiverType" -> currentAssessment.copy(caregiverType = value as CaregiverType?)
            "caregiverCapacity" -> currentAssessment.copy(caregiverCapacity = value as CaregiverCapacity?)
            "caregiverStress" -> currentAssessment.copy(caregiverStress = value as CaregiverStressLevel?)
            "caregiverKnowledge" -> currentAssessment.copy(caregiverKnowledge = value as CaregiverKnowledgeLevel?)
            "caregiverSupport" -> currentAssessment.copy(caregiverSupport = value as CaregiverSupportLevel?)
            else -> currentAssessment
        }

        _uiState.value = currentState.copy(
            caregiverAssessment = updatedAssessment,
            hasChanges = true
        )
        
        updateAssessmentCompleteness()
    }

    /**
     * Update clinical notes
     */
    fun updateClinicalNotes(notes: String) {
        _uiState.value = _uiState.value.copy(
            clinicalNotes = notes,
            hasChanges = true
        )
        
        updateAssessmentCompleteness()
    }

    /**
     * Save assessment as draft
     */
    fun saveAssessment() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                isLoading = true,
                loadingMessage = "Saving assessment draft..."
            )

            try {
                // Save draft logic would go here
                kotlinx.coroutines.delay(1000) // Simulate save

                _uiState.value = currentState.copy(
                    isLoading = false,
                    hasChanges = false,
                    successMessage = "Assessment draft saved successfully"
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Failed to save assessment: ${e.message}"
                )
            }
        }
    }

    /**
     * Complete assessment and submit
     */
    fun completeAssessment() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                isLoading = true,
                loadingMessage = "Completing clinical assessment..."
            )

            try {
                val assessmentRequest = createAssessmentRequest(currentState)
                val result = healthcareIntegrationService.performClinicalAssessment(
                    currentState.userId,
                    assessmentRequest
                )

                if (result.success) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        successMessage = buildAssessmentSuccessMessage(result),
                        assessmentId = result.assessmentId
                    )
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Assessment completion failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Assessment completion failed: ${e.message}"
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

    // Private helper methods

    private fun updateAssessmentCompleteness() {
        val currentState = _uiState.value
        val isComplete = isAssessmentComplete(currentState)
        
        _uiState.value = currentState.copy(
            isAssessmentComplete = isComplete
        )
    }

    private fun updateRiskSummary() {
        val currentState = _uiState.value
        val riskAssessment = currentState.riskFactorAssessment
        
        // Calculate abuse risk level based on risk factors
        val abuseRiskLevel = calculateAbuseRiskLevel(riskAssessment)
        
        // Generate recommendations based on assessment
        val recommendations = generateRecommendations(currentState)
        
        _uiState.value = currentState.copy(
            abuseRiskLevel = abuseRiskLevel,
            overallRiskLevel = riskAssessment.overallRiskLevel,
            recommendations = recommendations,
            showRiskSummary = true
        )
    }

    private fun isAssessmentComplete(state: ClinicalAssessmentUiState): Boolean {
        val cognitive = state.cognitiveAssessment
        val functional = state.functionalAssessment
        val social = state.socialAssessment
        val risk = state.riskFactorAssessment
        val caregiver = state.caregiverAssessment

        return cognitive.assessmentTool.isNotBlank() &&
                cognitive.score.isNotBlank() &&
                cognitive.interpretation.isNotBlank() &&
                cognitive.cognitiveImpairmentLevel != null &&
                cognitive.decisionMakingCapacity != null &&
                functional.mobilityStatus != null &&
                functional.fallRisk != null &&
                social.socialSupport != null &&
                social.socialIsolation != null &&
                risk.overallRiskLevel != null &&
                caregiver.caregiverType != null &&
                caregiver.caregiverCapacity != null &&
                state.clinicalNotes.isNotBlank()
    }

    private fun calculateAbuseRiskLevel(riskAssessment: RiskFactorAssessmentUiState): AbuseRiskLevel {
        val totalRiskFactors = riskAssessment.abuseRiskFactors.size + 
                              riskAssessment.neglectRiskFactors.size + 
                              riskAssessment.exploitationRiskFactors.size
        
        return when {
            totalRiskFactors >= 6 -> AbuseRiskLevel.CRITICAL
            totalRiskFactors >= 4 -> AbuseRiskLevel.HIGH
            totalRiskFactors >= 2 -> AbuseRiskLevel.MODERATE
            totalRiskFactors >= 1 -> AbuseRiskLevel.LOW
            else -> AbuseRiskLevel.MINIMAL
        }
    }

    private fun generateRecommendations(state: ClinicalAssessmentUiState): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Cognitive-based recommendations
        when (state.cognitiveAssessment.cognitiveImpairmentLevel) {
            CognitiveImpairmentLevel.SEVERE -> {
                recommendations.add("Consider 24/7 supervision or care facility placement")
                recommendations.add("Implement medication management system")
            }
            CognitiveImpairmentLevel.MODERATE -> {
                recommendations.add("Regular cognitive assessment follow-ups")
                recommendations.add("Caregiver training on cognitive support strategies")
            }
            CognitiveImpairmentLevel.MILD -> {
                recommendations.add("Cognitive stimulation activities")
                recommendations.add("Monitor for progression")
            }
            else -> {}
        }

        // Risk-based recommendations
        when (state.abuseRiskLevel) {
            AbuseRiskLevel.CRITICAL, AbuseRiskLevel.HIGH -> {
                recommendations.add("Immediate elder rights advocate notification")
                recommendations.add("Consider protective services referral")
                recommendations.add("Increase monitoring frequency")
            }
            AbuseRiskLevel.MODERATE -> {
                recommendations.add("Enhanced caregiver support and training")
                recommendations.add("Regular welfare checks")
            }
            else -> {}
        }

        // Functional-based recommendations
        if (state.functionalAssessment.fallRisk == FallRiskLevel.HIGH) {
            recommendations.add("Home safety assessment and modifications")
            recommendations.add("Physical therapy evaluation")
        }

        return recommendations
    }

    private suspend fun loadProfessionalData(professionalId: String): ProfessionalData {
        // Simulate loading professional data
        return ProfessionalData(
            professionalId = professionalId,
            firstName = "Dr. Jane",
            lastName = "Smith"
        )
    }

    private suspend fun loadUserData(userId: String): UserData {
        // Simulate loading user data
        return UserData(
            userId = userId,
            name = "John Doe"
        )
    }

    private fun createAssessmentRequest(state: ClinicalAssessmentUiState): HealthcareIntegrationService.ClinicalAssessmentRequest {
        return HealthcareIntegrationService.ClinicalAssessmentRequest(
            assessingPhysicianId = state.professionalId,
            assessmentType = ClinicalAssessmentType.INITIAL_ASSESSMENT,
            cognitiveAssessment = createCognitiveAssessment(state.cognitiveAssessment),
            functionalAssessment = createFunctionalAssessment(state.functionalAssessment),
            socialAssessment = createSocialAssessment(state.socialAssessment),
            riskFactorAssessment = createRiskFactorAssessment(state.riskFactorAssessment),
            caregiverAssessment = createCaregiverAssessment(state.caregiverAssessment),
            familyDynamicsAssessment = createFamilyDynamicsAssessment(state.socialAssessment),
            clinicalNotes = state.clinicalNotes
        )
    }

    private fun createCognitiveAssessment(uiAssessment: CognitiveAssessmentUiState): CognitiveAssessment {
        return CognitiveAssessment(
            assessmentTool = uiAssessment.assessmentTool,
            score = uiAssessment.score.toDoubleOrNull() ?: 0.0,
            interpretation = uiAssessment.interpretation,
            cognitiveImpairmentLevel = uiAssessment.cognitiveImpairmentLevel ?: CognitiveImpairmentLevel.NONE,
            decisionMakingCapacity = uiAssessment.decisionMakingCapacity ?: DecisionMakingCapacity.FULL_CAPACITY
        )
    }

    private fun createFunctionalAssessment(uiAssessment: FunctionalAssessmentUiState): FunctionalAssessment {
        return FunctionalAssessment(
            activitiesOfDailyLiving = uiAssessment.adlLevels,
            instrumentalActivitiesOfDailyLiving = uiAssessment.iadlLevels,
            mobilityStatus = uiAssessment.mobilityStatus ?: MobilityStatus.INDEPENDENT,
            fallRisk = uiAssessment.fallRisk ?: FallRiskLevel.LOW
        )
    }

    private fun createSocialAssessment(uiAssessment: SocialAssessmentUiState): SocialAssessment {
        return SocialAssessment(
            socialSupport = uiAssessment.socialSupport ?: SocialSupportLevel.ADEQUATE,
            socialIsolation = uiAssessment.socialIsolation ?: SocialIsolationLevel.NONE,
            familyDynamics = uiAssessment.familyDynamics,
            caregiverRelationships = uiAssessment.caregiverRelationships,
            communityResources = uiAssessment.communityResources
        )
    }

    private fun createRiskFactorAssessment(uiAssessment: RiskFactorAssessmentUiState): RiskFactorAssessment {
        return RiskFactorAssessment(
            abuseRiskFactors = uiAssessment.abuseRiskFactors,
            neglectRiskFactors = uiAssessment.neglectRiskFactors,
            exploitationRiskFactors = uiAssessment.exploitationRiskFactors,
            overallRiskLevel = uiAssessment.overallRiskLevel ?: OverallRiskLevel.LOW,
            protectiveFactors = uiAssessment.protectiveFactors
        )
    }

    private fun createCaregiverAssessment(uiAssessment: CaregiverAssessmentUiState): CaregiverAssessment {
        return CaregiverAssessment(
            caregiverType = uiAssessment.caregiverType ?: CaregiverType.FAMILY_MEMBER,
            caregiverCapacity = uiAssessment.caregiverCapacity ?: CaregiverCapacity.ADEQUATE,
            caregiverStress = uiAssessment.caregiverStress ?: CaregiverStressLevel.LOW,
            caregiverKnowledge = uiAssessment.caregiverKnowledge ?: CaregiverKnowledgeLevel.ADEQUATE,
            caregiverSupport = uiAssessment.caregiverSupport ?: CaregiverSupportLevel.ADEQUATE
        )
    }

    private fun createFamilyDynamicsAssessment(socialAssessment: SocialAssessmentUiState): FamilyDynamicsAssessment {
        return FamilyDynamicsAssessment(
            familyStructure = "Assessment based on social evaluation",
            familyRelationships = socialAssessment.familyDynamics,
            communicationPatterns = "Evaluated during assessment",
            conflictResolution = "To be determined through ongoing monitoring",
            familySupport = FamilySupportLevel.MODERATE // Default, could be enhanced
        )
    }

    private fun buildAssessmentSuccessMessage(result: HealthcareIntegrationService.ClinicalAssessmentResult): String {
        val message = StringBuilder("Clinical assessment completed successfully!")
        
        message.append("\n\nAssessment ID: ${result.assessmentId}")
        message.append("\nOverall Risk Level: ${result.abuseRiskLevel}")
        
        if (result.elderRightsAdvocateRecommended) {
            message.append("\n\n⚠️ Elder rights advocate has been notified due to elevated risk factors.")
        }
        
        if (result.followUpRequired) {
            message.append("\n\nFollow-up assessment recommended.")
        }

        return message.toString()
    }

    // Data classes for loading
    private data class ProfessionalData(
        val professionalId: String,
        val firstName: String,
        val lastName: String
    )

    private data class UserData(
        val userId: String,
        val name: String
    )
}

// UI State Data Classes
data class ClinicalAssessmentUiState(
    val isInitialized: Boolean = false,
    val userId: String = "",
    val professionalId: String = "",
    val professionalName: String = "",
    val patientName: String = "",
    
    // Assessment sections
    val cognitiveAssessment: CognitiveAssessmentUiState = CognitiveAssessmentUiState(),
    val functionalAssessment: FunctionalAssessmentUiState = FunctionalAssessmentUiState(),
    val socialAssessment: SocialAssessmentUiState = SocialAssessmentUiState(),
    val riskFactorAssessment: RiskFactorAssessmentUiState = RiskFactorAssessmentUiState(),
    val caregiverAssessment: CaregiverAssessmentUiState = CaregiverAssessmentUiState(),
    
    // Clinical notes
    val clinicalNotes: String = "",
    
    // Risk summary
    val showRiskSummary: Boolean = false,
    val overallRiskLevel: OverallRiskLevel? = null,
    val abuseRiskLevel: AbuseRiskLevel? = null,
    val recommendations: List<String> = emptyList(),
    
    // State management
    val hasChanges: Boolean = false,
    val isAssessmentComplete: Boolean = false,
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val assessmentId: String? = null
)

data class CognitiveAssessmentUiState(
    val assessmentTool: String = "",
    val score: String = "",
    val interpretation: String = "",
    val cognitiveImpairmentLevel: CognitiveImpairmentLevel? = null,
    val decisionMakingCapacity: DecisionMakingCapacity? = null
)

data class FunctionalAssessmentUiState(
    val adlLevels: Map<String, FunctionalLevel> = emptyMap(),
    val iadlLevels: Map<String, FunctionalLevel> = emptyMap(),
    val mobilityStatus: MobilityStatus? = null,
    val fallRisk: FallRiskLevel? = null
)

data class SocialAssessmentUiState(
    val socialSupport: SocialSupportLevel? = null,
    val socialIsolation: SocialIsolationLevel? = null,
    val familyDynamics: String = "",
    val caregiverRelationships: String = "",
    val communityResources: List<String> = emptyList()
)

data class RiskFactorAssessmentUiState(
    val abuseRiskFactors: List<String> = emptyList(),
    val neglectRiskFactors: List<String> = emptyList(),
    val exploitationRiskFactors: List<String> = emptyList(),
    val overallRiskLevel: OverallRiskLevel? = null,
    val protectiveFactors: List<String> = emptyList()
)

data class CaregiverAssessmentUiState(
    val caregiverType: CaregiverType? = null,
    val caregiverCapacity: CaregiverCapacity? = null,
    val caregiverStress: CaregiverStressLevel? = null,
    val caregiverKnowledge: CaregiverKnowledgeLevel? = null,
    val caregiverSupport: CaregiverSupportLevel? = null
)
