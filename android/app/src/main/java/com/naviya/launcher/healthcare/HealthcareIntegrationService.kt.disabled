package com.naviya.launcher.healthcare

import com.naviya.launcher.healthcare.data.*
import com.naviya.launcher.elderrights.ElderRightsAdvocateNotificationService
import com.naviya.launcher.abuse.RuleBasedAbuseDetector
import com.naviya.launcher.abuse.data.AbuseDetectionEvent
import com.naviya.launcher.abuse.data.AbuseRiskLevel as AbuseSystemRiskLevel
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simplified Healthcare Integration Service
 * Connects healthcare professional system with existing abuse detection and elder rights systems
 */
@Singleton
class HealthcareIntegrationService @Inject constructor(
    private val healthcareProfessionalRepository: HealthcareProfessionalRepository,
    private val elderRightsAdvocateService: ElderRightsAdvocateNotificationService,
    private val abuseDetector: RuleBasedAbuseDetector
) {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ==================== PROFESSIONAL REGISTRATION ====================

    suspend fun registerHealthcareProfessional(
        professionalDetails: HealthcareProfessionalDetails,
        credentials: ProfessionalCredentials,
        institution: HealthcareInstitution? = null
    ): ProfessionalRegistrationResult = withContext(Dispatchers.IO) {
        try {
            // Check if professional already exists
            val existingRegistration = healthcareProfessionalRepository
                .getProfessionalRegistrationByProfessionalId(professionalDetails.professionalId)

            if (existingRegistration != null) {
                return@withContext ProfessionalRegistrationResult(
                    registrationId = null,
                    success = false,
                    message = "Professional already registered",
                    verificationRequired = false,
                    trainingRequired = false
                )
            }

            // Validate credentials
            val credentialValidation = validateCredentials(credentials)
            if (!credentialValidation.success) {
                return@withContext ProfessionalRegistrationResult(
                    registrationId = null,
                    success = false,
                    message = credentialValidation.message,
                    verificationRequired = true,
                    trainingRequired = false
                )
            }

            // Create registration
            val registrationId = "reg-${System.nanoTime()}"
            val registration = HealthcareProfessionalRegistration(
                registrationId = registrationId,
                professionalId = professionalDetails.professionalId,
                personalDetails = professionalDetails,
                credentials = credentials,
                institutionAffiliation = institution,
                registrationTimestamp = System.currentTimeMillis(),
                status = ProfessionalRegistrationStatus.PENDING_VERIFICATION,
                certificationStatus = CertificationStatus.PENDING,
                clinicalOversightLevel = ClinicalOversightLevel.STANDARD,
                lastReviewDate = System.currentTimeMillis(),
                nextRecertificationDate = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L)
            )

            healthcareProfessionalRepository.insertProfessionalRegistration(registration)

            ProfessionalRegistrationResult(
                registrationId = registrationId,
                success = true,
                message = "Professional registration initiated",
                verificationRequired = true,
                trainingRequired = true,
                nextSteps = listOf(
                    "Complete background check",
                    "Complete ethics training",
                    "Complete elder care specialization training"
                )
            )

        } catch (e: Exception) {
            ProfessionalRegistrationResult(
                registrationId = null,
                success = false,
                message = "Registration failed: ${e.message}",
                error = e.toString()
            )
        }
    }

    // ==================== PROFESSIONAL INSTALLATION ====================

    suspend fun performProfessionalInstallation(
        request: ProfessionalInstallationRequest
    ): ProfessionalInstallationResult = withContext(Dispatchers.IO) {
        try {
            // Verify professional authorization
            val professionalRegistration = healthcareProfessionalRepository
                .getProfessionalRegistrationByProfessionalId(request.professionalId)
                ?: return@withContext ProfessionalInstallationResult(
                    installationId = null,
                    success = false,
                    message = "Professional not found",
                    authorizationRequired = true
                )

            if (!professionalRegistration.installationAuthorized) {
                return@withContext ProfessionalInstallationResult(
                    installationId = null,
                    success = false,
                    message = "Professional not authorized for installation",
                    authorizationRequired = true
                )
            }

            // Check for existing installations
            val existingInstallations = healthcareProfessionalRepository
                .getProfessionalInstallationsByUserId(request.userId)

            val installationId = "install-${System.nanoTime()}"
            val installation = ProfessionalInstallation(
                installationId = installationId,
                userId = request.userId,
                professionalId = request.professionalId,
                institutionId = request.institutionId,
                installationTimestamp = System.currentTimeMillis(),
                installationType = request.installationType,
                clinicalContext = request.clinicalContext,
                patientConsent = request.patientConsent,
                familyConsent = request.familyConsent,
                elderRightsAdvocateInformed = false,
                clinicalAssessmentCompleted = false,
                riskAssessmentCompleted = false,
                caregiverTrainingProvided = false,
                systemConfigurationCompleted = false,
                qualityAssuranceCompleted = false,
                installationStatus = InstallationStatus.IN_PROGRESS,
                clinicalNotes = request.initialClinicalNotes,
                followUpScheduled = false,
                nextReviewDate = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
            )

            if (existingInstallations.isEmpty()) {
                healthcareProfessionalRepository.insertProfessionalInstallation(installation)
            } else {
                // Update existing installation
                val updatedInstallation = installation.copy(
                    installationId = existingInstallations.first().installationId
                )
                healthcareProfessionalRepository.updateProfessionalInstallation(updatedInstallation)
            }

            // Notify elder rights advocate
            notifyElderRightsAdvocateOfInstallation(installation)

            ProfessionalInstallationResult(
                installationId = installationId,
                success = true,
                message = "Professional installation completed",
                completedSteps = listOf(
                    "Professional verification",
                    "Consent validation",
                    "System configuration",
                    "Elder rights advocate notification"
                ),
                pendingSteps = listOf(
                    "Clinical assessment",
                    "Risk assessment",
                    "Caregiver training",
                    "Quality assurance"
                ),
                followUpRequired = true,
                nextReviewDate = installation.nextReviewDate
            )

        } catch (e: Exception) {
            ProfessionalInstallationResult(
                installationId = null,
                success = false,
                message = "Installation failed: ${e.message}",
                error = e.toString()
            )
        }
    }

    // ==================== CLINICAL ASSESSMENT ====================

    suspend fun performClinicalAssessment(
        userId: String,
        request: ClinicalAssessmentRequest
    ): ClinicalAssessmentResult = withContext(Dispatchers.IO) {
        try {
            // Verify assessing physician
            val assessingPhysician = healthcareProfessionalRepository
                .getProfessionalRegistrationByProfessionalId(request.assessingPhysicianId)
                ?: return@withContext ClinicalAssessmentResult(
                    assessmentId = null,
                    success = false,
                    message = "Assessing physician not found"
                )

            val assessmentId = "assess-${System.nanoTime()}"
            val abuseRiskLevel = determineAbuseRiskLevel(request.riskFactorAssessment)
            
            val assessment = ClinicalAssessment(
                assessmentId = assessmentId,
                userId = userId,
                assessingPhysicianId = request.assessingPhysicianId,
                assessmentTimestamp = System.currentTimeMillis(),
                assessmentType = request.assessmentType,
                cognitiveAssessment = request.cognitiveAssessment,
                functionalAssessment = request.functionalAssessment,
                socialAssessment = request.socialAssessment,
                riskFactorAssessment = request.riskFactorAssessment,
                abuseRiskLevel = abuseRiskLevel,
                protectionRecommendations = generateProtectionRecommendations(request.riskFactorAssessment),
                monitoringRecommendations = generateMonitoringRecommendations(abuseRiskLevel),
                caregiverAssessment = request.caregiverAssessment,
                familyDynamicsAssessment = request.familyDynamicsAssessment,
                elderRightsAdvocateRecommended = abuseRiskLevel in listOf(AbuseRiskLevel.HIGH, AbuseRiskLevel.CRITICAL),
                followUpRequired = true,
                nextAssessmentDate = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L),
                clinicalNotes = request.clinicalNotes,
                assessmentValid = true
            )

            healthcareProfessionalRepository.insertClinicalAssessment(assessment)

            // Integrate with abuse detection system
            integrateWithAbuseDetection(userId, assessment)

            // Handle high-risk cases
            if (abuseRiskLevel in listOf(AbuseRiskLevel.HIGH, AbuseRiskLevel.CRITICAL)) {
                handleHighRiskAssessment(userId, assessment)
            }

            ClinicalAssessmentResult(
                assessmentId = assessmentId,
                success = true,
                message = "Clinical assessment completed",
                abuseRiskLevel = abuseRiskLevel,
                protectionRecommendations = assessment.protectionRecommendations,
                monitoringRecommendations = assessment.monitoringRecommendations,
                elderRightsAdvocateRecommended = assessment.elderRightsAdvocateRecommended,
                followUpRequired = assessment.followUpRequired,
                nextAssessmentDate = assessment.nextAssessmentDate
            )

        } catch (e: Exception) {
            ClinicalAssessmentResult(
                assessmentId = null,
                success = false,
                message = "Assessment failed: ${e.message}",
                error = e.toString()
            )
        }
    }

    // ==================== CLINICAL OVERSIGHT ====================

    suspend fun establishClinicalOversight(
        userId: String,
        request: ClinicalOversightRequest
    ): ClinicalOversightResult = withContext(Dispatchers.IO) {
        try {
            // Verify primary physician
            val primaryPhysician = healthcareProfessionalRepository
                .getProfessionalRegistrationByProfessionalId(request.primaryPhysicianId)
                ?: return@withContext ClinicalOversightResult(
                    oversightId = null,
                    success = false,
                    message = "Primary physician not found"
                )

            // Check if oversight already exists
            val existingOversight = healthcareProfessionalRepository.getClinicalOversightByUserId(userId)

            val oversightId = existingOversight?.oversightId ?: "oversight-${System.nanoTime()}"
            val oversight = ClinicalOversight(
                oversightId = oversightId,
                userId = userId,
                primaryPhysicianId = request.primaryPhysicianId,
                specialistIds = request.specialistIds,
                institutionId = request.institutionId,
                oversightLevel = request.requestedOversightLevel,
                establishmentTimestamp = System.currentTimeMillis(),
                clinicalProtocols = listOf("Elder Abuse Detection", "Safety Monitoring", "Risk Assessment"),
                monitoringFrequency = MonitoringFrequency.WEEKLY,
                alertThresholds = mapOf("abuse_risk" to 0.7, "safety_concern" to 0.8),
                escalationProcedures = listOf(
                    "Notify Elder Rights Advocate",
                    "Contact Emergency Services",
                    "Initiate Safety Plan"
                ),
                qualityMetrics = listOf("Response Time", "Assessment Accuracy", "Patient Safety"),
                patientSafetyMeasures = listOf("Regular Check-ins", "Emergency Contacts", "Safety Protocols"),
                clinicalGovernance = "Healthcare Institution Quality Committee",
                isActive = true,
                lastReviewDate = System.currentTimeMillis(),
                nextReviewDate = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L)
            )

            if (existingOversight == null) {
                healthcareProfessionalRepository.insertClinicalOversight(oversight)
            } else {
                healthcareProfessionalRepository.updateClinicalOversight(oversight)
            }

            ClinicalOversightResult(
                oversightId = oversightId,
                success = true,
                message = "Clinical oversight established",
                oversightLevel = oversight.oversightLevel,
                monitoringFrequency = oversight.monitoringFrequency,
                nextReviewDate = oversight.nextReviewDate
            )

        } catch (e: Exception) {
            ClinicalOversightResult(
                oversightId = null,
                success = false,
                message = "Oversight establishment failed: ${e.message}",
                error = e.toString()
            )
        }
    }

    // ==================== INTEGRATION HELPERS ====================

    private suspend fun integrateWithAbuseDetection(userId: String, assessment: ClinicalAssessment) {
        try {
            // Convert healthcare risk level to abuse detection system risk level
            val abuseSystemRiskLevel = when (assessment.abuseRiskLevel) {
                AbuseRiskLevel.LOW -> AbuseSystemRiskLevel.LOW
                AbuseRiskLevel.MODERATE -> AbuseSystemRiskLevel.MEDIUM
                AbuseRiskLevel.HIGH -> AbuseSystemRiskLevel.HIGH
                AbuseRiskLevel.CRITICAL -> AbuseSystemRiskLevel.CRITICAL
            }

            // Create abuse detection event
            val abuseEvent = AbuseDetectionEvent(
                eventId = "clinical-${assessment.assessmentId}",
                userId = userId,
                eventType = "clinical_assessment",
                riskLevel = abuseSystemRiskLevel,
                description = "Clinical assessment indicates ${assessment.abuseRiskLevel} abuse risk",
                riskFactors = assessment.riskFactorAssessment.abuseRiskFactors,
                timestamp = assessment.assessmentTimestamp,
                source = "healthcare_professional",
                metadata = mapOf(
                    "assessmentId" to assessment.assessmentId,
                    "assessingPhysicianId" to assessment.assessingPhysicianId,
                    "cognitiveImpairment" to assessment.cognitiveAssessment.cognitiveImpairmentLevel.name,
                    "decisionMakingCapacity" to assessment.cognitiveAssessment.decisionMakingCapacity.name
                )
            )

            // Submit to abuse detection system
            abuseDetector.processAbuseEvent(abuseEvent)

        } catch (e: Exception) {
            // Log error but don't fail the assessment
            println("Failed to integrate with abuse detection system: ${e.message}")
        }
    }

    private suspend fun handleHighRiskAssessment(userId: String, assessment: ClinicalAssessment) {
        serviceScope.launch {
            try {
                // Notify elder rights advocate immediately
                elderRightsAdvocateService.notifyElderRightsAdvocate(
                    userId = userId,
                    alertType = "high_risk_clinical_assessment",
                    details = mapOf(
                        "assessmentId" to assessment.assessmentId,
                        "riskLevel" to assessment.abuseRiskLevel.name,
                        "riskFactors" to assessment.riskFactorAssessment.abuseRiskFactors.joinToString(", "),
                        "protectionRecommendations" to assessment.protectionRecommendations.joinToString(", "),
                        "assessingPhysician" to assessment.assessingPhysicianId
                    )
                )

                // For critical cases, escalate to emergency services
                if (assessment.abuseRiskLevel == AbuseRiskLevel.CRITICAL) {
                    elderRightsAdvocateService.escalateToEmergencyServices(
                        userId = userId,
                        details = mapOf(
                            "reason" to "Critical abuse risk identified in clinical assessment",
                            "assessmentId" to assessment.assessmentId,
                            "immediateRisks" to assessment.riskFactorAssessment.abuseRiskFactors.joinToString(", ")
                        )
                    )
                }

            } catch (e: Exception) {
                println("Failed to handle high-risk assessment: ${e.message}")
            }
        }
    }

    private suspend fun notifyElderRightsAdvocateOfInstallation(installation: ProfessionalInstallation) {
        serviceScope.launch {
            try {
                elderRightsAdvocateService.notifyElderRightsAdvocate(
                    userId = installation.userId,
                    alertType = "professional_installation",
                    details = mapOf(
                        "installationId" to installation.installationId,
                        "professionalId" to installation.professionalId,
                        "installationType" to installation.installationType.name,
                        "clinicalContext" to installation.clinicalContext.name,
                        "institutionId" to (installation.institutionId ?: "independent")
                    )
                )
            } catch (e: Exception) {
                println("Failed to notify elder rights advocate of installation: ${e.message}")
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private fun validateCredentials(credentials: ProfessionalCredentials): ValidationResult {
        if (!credentials.isValid) {
            return ValidationResult(false, "Credentials are marked as invalid")
        }

        if (credentials.credentialExpirationDate < System.currentTimeMillis()) {
            return ValidationResult(false, "Credentials have expired")
        }

        if (credentials.backgroundCheckStatus != BackgroundCheckStatus.CLEARED) {
            return ValidationResult(false, "Background check not cleared")
        }

        return ValidationResult(true, "Credentials validated successfully")
    }

    private fun determineAbuseRiskLevel(riskFactorAssessment: RiskFactorAssessment): AbuseRiskLevel {
        return when (riskFactorAssessment.overallRiskLevel) {
            OverallRiskLevel.LOW -> AbuseRiskLevel.LOW
            OverallRiskLevel.MODERATE -> AbuseRiskLevel.MODERATE
            OverallRiskLevel.HIGH -> AbuseRiskLevel.HIGH
            OverallRiskLevel.CRITICAL -> AbuseRiskLevel.CRITICAL
        }
    }

    private fun generateProtectionRecommendations(riskFactorAssessment: RiskFactorAssessment): List<String> {
        val recommendations = mutableListOf<String>()

        if (riskFactorAssessment.abuseRiskFactors.contains("Financial vulnerability")) {
            recommendations.add("Implement financial safeguards and monitoring")
        }

        if (riskFactorAssessment.abuseRiskFactors.contains("Social isolation")) {
            recommendations.add("Increase social engagement and community connections")
        }

        if (riskFactorAssessment.neglectRiskFactors.contains("Medication management issues")) {
            recommendations.add("Establish medication management support")
        }

        if (riskFactorAssessment.exploitationRiskFactors.contains("Cognitive impairment")) {
            recommendations.add("Implement cognitive protection measures")
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Continue regular monitoring and assessment")
        }

        return recommendations
    }

    private fun generateMonitoringRecommendations(abuseRiskLevel: AbuseRiskLevel): List<String> {
        return when (abuseRiskLevel) {
            AbuseRiskLevel.LOW -> listOf("Monthly check-ins", "Quarterly assessments")
            AbuseRiskLevel.MODERATE -> listOf("Bi-weekly check-ins", "Monthly assessments")
            AbuseRiskLevel.HIGH -> listOf("Weekly check-ins", "Bi-weekly assessments", "Elder rights advocate involvement")
            AbuseRiskLevel.CRITICAL -> listOf("Daily check-ins", "Weekly assessments", "Immediate elder rights advocate involvement", "Emergency services on standby")
        }
    }

    // ==================== DATA CLASSES ====================

    data class ValidationResult(
        val success: Boolean,
        val message: String
    )

    // ==================== REQUEST CLASSES ====================

    data class ProfessionalInstallationRequest(
        val userId: String,
        val professionalId: String,
        val institutionId: String?,
        val installationType: InstallationType,
        val clinicalContext: ClinicalContext,
        val patientConsent: ConsentRecord,
        val familyConsent: ConsentRecord?,
        val initialClinicalNotes: String?
    )

    data class ClinicalOversightRequest(
        val primaryPhysicianId: String,
        val specialistIds: List<String> = emptyList(),
        val institutionId: String?,
        val requestedOversightLevel: ClinicalOversightLevel
    )

    data class ClinicalAssessmentRequest(
        val assessingPhysicianId: String,
        val assessmentType: ClinicalAssessmentType,
        val cognitiveAssessment: CognitiveAssessment,
        val functionalAssessment: FunctionalAssessment,
        val socialAssessment: SocialAssessment,
        val riskFactorAssessment: RiskFactorAssessment,
        val caregiverAssessment: CaregiverAssessment?,
        val familyDynamicsAssessment: FamilyDynamicsAssessment?,
        val clinicalNotes: String?
    )
}
