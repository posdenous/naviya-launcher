package com.naviya.launcher.healthcare.compliance

import android.content.Context
import android.util.Log
import androidx.annotation.RequiresApi
import com.naviya.launcher.healthcare.data.*
import com.naviya.launcher.database.NaviyaDatabase
import com.naviya.launcher.security.SecurityAuditLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Medical Compliance Manager for Naviya Elder Protection System
 * Ensures HIPAA, GDPR, and clinical governance compliance for healthcare professional integration
 */
@Singleton
class MedicalComplianceManager @Inject constructor(
    private val context: Context,
    private val database: NaviyaDatabase,
    private val securityAuditLogger: SecurityAuditLogger
) {
    
    companion object {
        private const val TAG = "MedicalCompliance"
        
        // Compliance requirements
        private const val MIN_TRAINING_HOURS = 8
        private const val BACKGROUND_CHECK_VALIDITY_MONTHS = 24
        private const val CERTIFICATION_VALIDITY_MONTHS = 12
        private const val CLINICAL_REVIEW_FREQUENCY_MONTHS = 6
        
        // HIPAA compliance requirements
        private const val HIPAA_MINIMUM_NECESSARY_STANDARD = true
        private const val HIPAA_AUDIT_LOG_RETENTION_YEARS = 6
        
        // Elder protection standards
        private const val ELDER_ABUSE_MANDATORY_REPORTING = true
        private const val VULNERABLE_ADULT_PROTECTION_REQUIRED = true
    }
    
    /**
     * Validates healthcare professional registration for medical compliance
     */
    suspend fun validateProfessionalRegistration(
        registration: HealthcareProfessionalRegistration
    ): MedicalComplianceResult {
        Log.i(TAG, "Validating professional registration: ${registration.registrationId}")
        
        val violations = mutableListOf<ComplianceViolation>()
        
        // Check training requirements
        if (!registration.trainingCompleted || registration.trainingHoursCompleted < MIN_TRAINING_HOURS) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.INSUFFICIENT_TRAINING,
                    severity = ComplianceSeverity.HIGH,
                    description = "Healthcare professional requires ${MIN_TRAINING_HOURS} hours of elder care training",
                    remediation = "Complete mandatory elder care training programme"
                )
            )
        }
        
        // Check background verification
        if (!registration.backgroundCheckCompleted) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_BACKGROUND_CHECK,
                    severity = ComplianceSeverity.CRITICAL,
                    description = "Background check required for vulnerable adult access",
                    remediation = "Complete enhanced DBS check or equivalent background verification"
                )
            )
        }
        
        // Check professional certification
        if (registration.certificationStatus != CertificationStatus.CURRENT) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.INVALID_CERTIFICATION,
                    severity = ComplianceSeverity.HIGH,
                    description = "Professional certification must be current and valid",
                    remediation = "Renew professional certification with relevant medical board"
                )
            )
        }
        
        // Check liability insurance
        if (!registration.liabilityInsuranceVerified) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_LIABILITY_INSURANCE,
                    severity = ComplianceSeverity.HIGH,
                    description = "Professional liability insurance required for clinical oversight",
                    remediation = "Provide proof of current professional liability insurance"
                )
            )
        }
        
        // Check ethics training
        if (!registration.ethicsTrainingCompleted) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_ETHICS_TRAINING,
                    severity = ComplianceSeverity.MEDIUM,
                    description = "Ethics training required for elder care specialisation",
                    remediation = "Complete elder care ethics and safeguarding training"
                )
            )
        }
        
        // Check elder care specialisation
        if (!registration.elderCareSpecializationVerified) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_SPECIALISATION,
                    severity = ComplianceSeverity.MEDIUM,
                    description = "Elder care specialisation verification required",
                    remediation = "Provide evidence of elder care clinical experience or specialisation"
                )
            )
        }
        
        // Log compliance check
        securityAuditLogger.logComplianceCheck(
            professionalId = registration.professionalId,
            checkType = "REGISTRATION_VALIDATION",
            violationCount = violations.size,
            passed = violations.isEmpty()
        )
        
        return MedicalComplianceResult(
            isCompliant = violations.isEmpty(),
            violations = violations,
            recommendedActions = violations.map { it.remediation },
            nextReviewDate = calculateNextReviewDate(registration),
            complianceScore = calculateComplianceScore(violations)
        )
    }
    
    /**
     * Validates clinical installation for HIPAA and patient safety compliance
     */
    suspend fun validateClinicalInstallation(
        installation: ProfessionalInstallation
    ): MedicalComplianceResult {
        Log.i(TAG, "Validating clinical installation: ${installation.installationId}")
        
        val violations = mutableListOf<ComplianceViolation>()
        
        // Check patient consent (HIPAA requirement)
        if (!isValidConsent(installation.patientConsent)) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.INVALID_PATIENT_CONSENT,
                    severity = ComplianceSeverity.CRITICAL,
                    description = "Valid patient consent required for HIPAA compliance",
                    remediation = "Obtain proper informed consent from patient or legal guardian"
                )
            )
        }
        
        // Check family consent if required
        if (installation.familyConsent == null && isMinorOrIncapacitated(installation.userId)) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_GUARDIAN_CONSENT,
                    severity = ComplianceSeverity.CRITICAL,
                    description = "Guardian consent required for vulnerable adult",
                    remediation = "Obtain consent from legal guardian or next of kin"
                )
            )
        }
        
        // Check clinical assessment completion
        if (!installation.clinicalAssessmentCompleted) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_CLINICAL_ASSESSMENT,
                    severity = ComplianceSeverity.HIGH,
                    description = "Clinical assessment required before system installation",
                    remediation = "Complete comprehensive clinical assessment including cognitive, functional, and social evaluation"
                )
            )
        }
        
        // Check risk assessment
        if (!installation.riskAssessmentCompleted) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_RISK_ASSESSMENT,
                    severity = ComplianceSeverity.HIGH,
                    description = "Risk assessment required for vulnerable adult protection",
                    remediation = "Complete abuse risk assessment and safety planning"
                )
            )
        }
        
        // Check elder rights advocate notification
        if (!installation.elderRightsAdvocateInformed) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_ADVOCATE_NOTIFICATION,
                    severity = ComplianceSeverity.MEDIUM,
                    description = "Elder rights advocate should be informed of monitoring system installation",
                    remediation = "Notify independent elder rights advocate of system installation"
                )
            )
        }
        
        // Check caregiver training
        if (!installation.caregiverTrainingProvided) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_CAREGIVER_TRAINING,
                    severity = ComplianceSeverity.MEDIUM,
                    description = "Caregiver training required for proper system use",
                    remediation = "Provide comprehensive caregiver training on system use and elder rights"
                )
            )
        }
        
        return MedicalComplianceResult(
            isCompliant = violations.isEmpty(),
            violations = violations,
            recommendedActions = violations.map { it.remediation },
            nextReviewDate = installation.nextReviewDate,
            complianceScore = calculateComplianceScore(violations)
        )
    }
    
    /**
     * Validates clinical oversight for ongoing compliance
     */
    suspend fun validateClinicalOversight(
        oversight: ClinicalOversight
    ): MedicalComplianceResult {
        Log.i(TAG, "Validating clinical oversight: ${oversight.oversightId}")
        
        val violations = mutableListOf<ComplianceViolation>()
        
        // Check oversight level appropriateness
        if (oversight.oversightLevel == ClinicalOversightLevel.MINIMAL && 
            hasHighRiskFactors(oversight.userId)) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.INADEQUATE_OVERSIGHT_LEVEL,
                    severity = ComplianceSeverity.HIGH,
                    description = "Higher oversight level required for high-risk patient",
                    remediation = "Increase oversight level to STANDARD or INTENSIVE based on risk assessment"
                )
            )
        }
        
        // Check monitoring frequency
        if (oversight.monitoringFrequency == MonitoringFrequency.MONTHLY && 
            hasUrgentMedicalNeeds(oversight.userId)) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.INSUFFICIENT_MONITORING_FREQUENCY,
                    severity = ComplianceSeverity.MEDIUM,
                    description = "More frequent monitoring required for urgent medical needs",
                    remediation = "Increase monitoring frequency to weekly or daily as clinically indicated"
                )
            )
        }
        
        // Check clinical protocols
        if (oversight.clinicalProtocols.isEmpty()) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_CLINICAL_PROTOCOLS,
                    severity = ComplianceSeverity.MEDIUM,
                    description = "Clinical protocols must be defined for patient safety",
                    remediation = "Establish clear clinical protocols for monitoring and intervention"
                )
            )
        }
        
        // Check escalation procedures
        if (oversight.escalationProcedures.isEmpty()) {
            violations.add(
                ComplianceViolation(
                    type = ComplianceViolationType.MISSING_ESCALATION_PROCEDURES,
                    severity = ComplianceSeverity.HIGH,
                    description = "Emergency escalation procedures must be defined",
                    remediation = "Define clear escalation procedures for emergency situations"
                )
            )
        }
        
        return MedicalComplianceResult(
            isCompliant = violations.isEmpty(),
            violations = violations,
            recommendedActions = violations.map { it.remediation },
            nextReviewDate = oversight.nextReviewDate,
            complianceScore = calculateComplianceScore(violations)
        )
    }
    
    /**
     * Generates comprehensive compliance report for healthcare professional
     */
    suspend fun generateComplianceReport(professionalId: String): ComplianceReport {
        Log.i(TAG, "Generating compliance report for professional: $professionalId")
        
        val healthcareDao = database.healthcareProfessionalDao()
        
        val registration = healthcareDao.getProfessionalRegistrationByProfessionalId(professionalId)
        val installations = healthcareDao.getProfessionalInstallationsByProfessionalId(professionalId)
        val oversights = healthcareDao.getClinicalOversightsByProfessionalId(professionalId)
        val assessments = healthcareDao.getClinicalAssessmentsByPhysicianId(professionalId)
        
        val registrationCompliance = registration?.let { validateProfessionalRegistration(it) }
        val installationCompliance = installations.map { validateClinicalInstallation(it) }
        val oversightCompliance = oversights.map { validateClinicalOversight(it) }
        
        val allViolations = mutableListOf<ComplianceViolation>()
        registrationCompliance?.violations?.let { allViolations.addAll(it) }
        installationCompliance.forEach { allViolations.addAll(it.violations) }
        oversightCompliance.forEach { allViolations.addAll(it.violations) }
        
        return ComplianceReport(
            professionalId = professionalId,
            reportTimestamp = System.currentTimeMillis(),
            overallCompliance = allViolations.isEmpty(),
            totalViolations = allViolations.size,
            criticalViolations = allViolations.count { it.severity == ComplianceSeverity.CRITICAL },
            highViolations = allViolations.count { it.severity == ComplianceSeverity.HIGH },
            mediumViolations = allViolations.count { it.severity == ComplianceSeverity.MEDIUM },
            lowViolations = allViolations.count { it.severity == ComplianceSeverity.LOW },
            violations = allViolations,
            registrationCompliance = registrationCompliance,
            installationCompliance = installationCompliance,
            oversightCompliance = oversightCompliance,
            recommendedActions = allViolations.map { it.remediation }.distinct(),
            complianceScore = calculateOverallComplianceScore(allViolations),
            nextReviewDate = calculateNextComplianceReview(registration, installations, oversights)
        )
    }
    
    /**
     * Monitors ongoing compliance for all healthcare professionals
     */
    fun monitorOngoingCompliance(): Flow<List<ComplianceAlert>> {
        val healthcareDao = database.healthcareProfessionalDao()
        
        return combine(
            healthcareDao.getAllProfessionalRegistrationsFlow(),
            healthcareDao.getAllProfessionalInstallationsFlow(),
            healthcareDao.getAllClinicalOversightFlow()
        ) { registrations, installations, oversights ->
            
            val alerts = mutableListOf<ComplianceAlert>()
            
            // Check for expiring certifications
            val currentTime = System.currentTimeMillis()
            registrations.forEach { registration ->
                if (registration.nextRecertificationDate - currentTime < 30 * 24 * 60 * 60 * 1000L) { // 30 days
                    alerts.add(
                        ComplianceAlert(
                            alertId = "CERT_EXPIRY_${registration.professionalId}",
                            professionalId = registration.professionalId,
                            alertType = ComplianceAlertType.CERTIFICATION_EXPIRING,
                            severity = ComplianceSeverity.HIGH,
                            message = "Professional certification expires within 30 days",
                            dueDate = registration.nextRecertificationDate,
                            actionRequired = "Renew professional certification"
                        )
                    )
                }
            }
            
            // Check for overdue reviews
            oversights.forEach { oversight ->
                if (oversight.nextReviewDate < currentTime) {
                    alerts.add(
                        ComplianceAlert(
                            alertId = "REVIEW_OVERDUE_${oversight.oversightId}",
                            professionalId = oversight.primaryPhysicianId,
                            alertType = ComplianceAlertType.REVIEW_OVERDUE,
                            severity = ComplianceSeverity.MEDIUM,
                            message = "Clinical review is overdue",
                            dueDate = oversight.nextReviewDate,
                            actionRequired = "Schedule and complete clinical review"
                        )
                    )
                }
            }
            
            alerts
        }
    }
    
    // Helper methods
    
    private fun isValidConsent(consent: ConsentRecord): Boolean {
        return consent.consentGiven && 
               consent.consentMethod != ConsentMethod.VERBAL_ONLY &&
               consent.witnessPresent &&
               consent.consentTimestamp > 0
    }
    
    private suspend fun isMinorOrIncapacitated(userId: String): Boolean {
        // Check if user is minor or has diminished capacity requiring guardian consent
        val assessments = database.healthcareProfessionalDao().getClinicalAssessmentsByUserId(userId)
        return assessments.any { 
            it.cognitiveAssessment.decisionMakingCapacity == DecisionMakingCapacity.IMPAIRED ||
            it.cognitiveAssessment.decisionMakingCapacity == DecisionMakingCapacity.SEVERELY_IMPAIRED
        }
    }
    
    private suspend fun hasHighRiskFactors(userId: String): Boolean {
        val assessments = database.healthcareProfessionalDao().getClinicalAssessmentsByUserId(userId)
        return assessments.any { 
            it.abuseRiskLevel == AbuseRiskLevel.HIGH || 
            it.abuseRiskLevel == AbuseRiskLevel.CRITICAL 
        }
    }
    
    private suspend fun hasUrgentMedicalNeeds(userId: String): Boolean {
        val assessments = database.healthcareProfessionalDao().getClinicalAssessmentsByUserId(userId)
        return assessments.any { 
            it.cognitiveAssessment.cognitiveImpairmentLevel == CognitiveImpairmentLevel.SEVERE ||
            it.functionalAssessment.independenceLevel < 3 // Significant functional impairment
        }
    }
    
    private fun calculateComplianceScore(violations: List<ComplianceViolation>): Double {
        if (violations.isEmpty()) return 100.0
        
        val totalPenalty = violations.sumOf { violation ->
            when (violation.severity) {
                ComplianceSeverity.CRITICAL -> 25.0
                ComplianceSeverity.HIGH -> 15.0
                ComplianceSeverity.MEDIUM -> 10.0
                ComplianceSeverity.LOW -> 5.0
            }
        }
        
        return maxOf(0.0, 100.0 - totalPenalty)
    }
    
    private fun calculateOverallComplianceScore(violations: List<ComplianceViolation>): Double {
        return calculateComplianceScore(violations)
    }
    
    private fun calculateNextReviewDate(registration: HealthcareProfessionalRegistration): Long {
        return registration.nextRecertificationDate.coerceAtMost(
            System.currentTimeMillis() + (CLINICAL_REVIEW_FREQUENCY_MONTHS * 30 * 24 * 60 * 60 * 1000L)
        )
    }
    
    private fun calculateNextComplianceReview(
        registration: HealthcareProfessionalRegistration?,
        installations: List<ProfessionalInstallation>,
        oversights: List<ClinicalOversight>
    ): Long {
        val dates = mutableListOf<Long>()
        
        registration?.let { dates.add(it.nextRecertificationDate) }
        installations.forEach { dates.add(it.nextReviewDate) }
        oversights.forEach { dates.add(it.nextReviewDate) }
        
        return dates.minOrNull() ?: (System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L))
    }
}
