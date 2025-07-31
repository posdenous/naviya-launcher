package com.naviya.launcher.healthcare.compliance

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Medical Compliance Data Models for Naviya Elder Protection System
 * Ensures HIPAA, GDPR, and clinical governance compliance
 */

// ==================== COMPLIANCE RESULT MODELS ====================

data class MedicalComplianceResult(
    val isCompliant: Boolean,
    val violations: List<ComplianceViolation>,
    val recommendedActions: List<String>,
    val nextReviewDate: Long,
    val complianceScore: Double // 0-100 score
)

data class ComplianceViolation(
    val type: ComplianceViolationType,
    val severity: ComplianceSeverity,
    val description: String,
    val remediation: String,
    val detectedAt: Long = System.currentTimeMillis(),
    val regulatoryReference: String? = null
)

data class ComplianceReport(
    val professionalId: String,
    val reportTimestamp: Long,
    val overallCompliance: Boolean,
    val totalViolations: Int,
    val criticalViolations: Int,
    val highViolations: Int,
    val mediumViolations: Int,
    val lowViolations: Int,
    val violations: List<ComplianceViolation>,
    val registrationCompliance: MedicalComplianceResult?,
    val installationCompliance: List<MedicalComplianceResult>,
    val oversightCompliance: List<MedicalComplianceResult>,
    val recommendedActions: List<String>,
    val complianceScore: Double,
    val nextReviewDate: Long
)

data class ComplianceAlert(
    val alertId: String,
    val professionalId: String,
    val alertType: ComplianceAlertType,
    val severity: ComplianceSeverity,
    val message: String,
    val dueDate: Long,
    val actionRequired: String,
    val acknowledged: Boolean = false,
    val resolvedAt: Long? = null
)

// ==================== HIPAA COMPLIANCE MODELS ====================

@Entity(tableName = "hipaa_compliance_logs")
@TypeConverters(MedicalComplianceConverters::class)
data class HipaaComplianceLog(
    @PrimaryKey
    val logId: String,
    val professionalId: String,
    val userId: String,
    val dataAccessType: HipaaDataAccessType,
    val accessTimestamp: Long,
    val accessPurpose: String,
    val minimumNecessaryJustification: String,
    val patientConsentReference: String,
    val dataElementsAccessed: List<String>,
    val accessDuration: Long,
    val accessMethod: HipaaAccessMethod,
    val auditTrailReference: String,
    val complianceVerified: Boolean = false,
    val verificationTimestamp: Long? = null
)

@Entity(tableName = "patient_consent_records")
@TypeConverters(MedicalComplianceConverters::class)
data class PatientConsentRecord(
    @PrimaryKey
    val consentId: String,
    val userId: String,
    val professionalId: String,
    val consentType: HipaaConsentType,
    val consentScope: List<String>, // What data can be accessed
    val consentPurpose: List<String>, // Why data can be accessed
    val consentMethod: ConsentVerificationMethod,
    val consentTimestamp: Long,
    val consentExpiry: Long?,
    val witnessPresent: Boolean,
    val witnessDetails: String?,
    val guardianConsent: Boolean = false,
    val guardianDetails: String?,
    val revokedAt: Long? = null,
    val revokedReason: String? = null,
    val digitalSignature: String?,
    val complianceNotes: String?
)

// ==================== CLINICAL GOVERNANCE MODELS ====================

@Entity(tableName = "clinical_governance_audits")
@TypeConverters(MedicalComplianceConverters::class)
data class ClinicalGovernanceAudit(
    @PrimaryKey
    val auditId: String,
    val professionalId: String,
    val auditType: ClinicalGovernanceAuditType,
    val auditTimestamp: Long,
    val auditScope: List<String>,
    val findings: List<GovernanceFinding>,
    val complianceScore: Double,
    val criticalIssues: List<String>,
    val recommendedActions: List<String>,
    val followUpRequired: Boolean,
    val followUpDate: Long?,
    val auditedBy: String,
    val auditStandard: ClinicalGovernanceStandard,
    val certificationBody: String?,
    val auditStatus: AuditStatus
)

data class GovernanceFinding(
    val findingId: String,
    val category: GovernanceFindingCategory,
    val severity: ComplianceSeverity,
    val description: String,
    val evidence: String,
    val recommendation: String,
    val standardReference: String,
    val timelineForResolution: Long
)

// ==================== ELDER PROTECTION COMPLIANCE MODELS ====================

@Entity(tableName = "elder_protection_assessments")
@TypeConverters(MedicalComplianceConverters::class)
data class ElderProtectionAssessment(
    @PrimaryKey
    val assessmentId: String,
    val userId: String,
    val assessorId: String,
    val assessmentTimestamp: Long,
    val vulnerabilityFactors: List<VulnerabilityFactor>,
    val abuseRiskLevel: AbuseRiskLevel,
    val protectionMeasures: List<ProtectionMeasure>,
    val mandatoryReportingRequired: Boolean,
    val reportingAgency: String?,
    val reportingTimestamp: Long?,
    val safeguardingPlan: SafeguardingPlan,
    val reviewDate: Long,
    val assessmentValid: Boolean = true,
    val validationNotes: String?
)

data class VulnerabilityFactor(
    val factorType: VulnerabilityFactorType,
    val severity: VulnerabilitySeverity,
    val description: String,
    val mitigationStrategy: String
)

data class ProtectionMeasure(
    val measureType: ProtectionMeasureType,
    val implementation: String,
    val responsibleParty: String,
    val timeline: Long,
    val monitoringRequired: Boolean
)

data class SafeguardingPlan(
    val planId: String,
    val objectives: List<String>,
    val interventions: List<String>,
    val riskMitigation: List<String>,
    val monitoringSchedule: String,
    val reviewFrequency: MonitoringFrequency,
    val emergencyContacts: List<String>,
    val escalationProcedure: String
)

// ==================== REGULATORY COMPLIANCE MODELS ====================

@Entity(tableName = "regulatory_compliance_checks")
@TypeConverters(MedicalComplianceConverters::class)
data class RegulatoryComplianceCheck(
    @PrimaryKey
    val checkId: String,
    val professionalId: String,
    val regulatoryFramework: RegulatoryFramework,
    val checkTimestamp: Long,
    val complianceRequirements: List<ComplianceRequirement>,
    val complianceStatus: ComplianceStatus,
    val violations: List<RegulatoryViolation>,
    val correctionPlan: CorrectionPlan?,
    val nextCheckDate: Long,
    val certifyingBody: String,
    val certificateNumber: String?,
    val certificateExpiry: Long?
)

data class ComplianceRequirement(
    val requirementId: String,
    val category: ComplianceCategory,
    val description: String,
    val mandatory: Boolean,
    val evidenceRequired: List<String>,
    val verificationMethod: VerificationMethod,
    val complianceDeadline: Long?
)

data class RegulatoryViolation(
    val violationId: String,
    val violationType: RegulatoryViolationType,
    val severity: ComplianceSeverity,
    val description: String,
    val regulatoryReference: String,
    val penaltyRisk: PenaltyRisk,
    val correctionRequired: String,
    val correctionDeadline: Long
)

data class CorrectionPlan(
    val planId: String,
    val violations: List<String>,
    val correctionSteps: List<CorrectionStep>,
    val timeline: Long,
    val responsibleParty: String,
    val verificationRequired: Boolean,
    val completionDeadline: Long
)

data class CorrectionStep(
    val stepId: String,
    val description: String,
    val deadline: Long,
    val completed: Boolean = false,
    val completionTimestamp: Long? = null,
    val evidence: String?
)

// ==================== ENUMS ====================

enum class ComplianceViolationType {
    // Training and Certification
    INSUFFICIENT_TRAINING,
    MISSING_BACKGROUND_CHECK,
    INVALID_CERTIFICATION,
    MISSING_LIABILITY_INSURANCE,
    MISSING_ETHICS_TRAINING,
    MISSING_SPECIALISATION,
    
    // Consent and Legal
    INVALID_PATIENT_CONSENT,
    MISSING_GUARDIAN_CONSENT,
    EXPIRED_CONSENT,
    INADEQUATE_CONSENT_DOCUMENTATION,
    
    // Clinical Practice
    MISSING_CLINICAL_ASSESSMENT,
    MISSING_RISK_ASSESSMENT,
    INADEQUATE_OVERSIGHT_LEVEL,
    INSUFFICIENT_MONITORING_FREQUENCY,
    MISSING_CLINICAL_PROTOCOLS,
    MISSING_ESCALATION_PROCEDURES,
    
    // Elder Protection
    MISSING_ADVOCATE_NOTIFICATION,
    MISSING_CAREGIVER_TRAINING,
    INADEQUATE_SAFEGUARDING_MEASURES,
    MANDATORY_REPORTING_FAILURE,
    
    // HIPAA and Privacy
    HIPAA_MINIMUM_NECESSARY_VIOLATION,
    UNAUTHORISED_DATA_ACCESS,
    INADEQUATE_AUDIT_TRAIL,
    PRIVACY_BREACH,
    
    // Regulatory
    REGULATORY_NON_COMPLIANCE,
    MISSING_DOCUMENTATION,
    OVERDUE_REVIEW
}

enum class ComplianceSeverity {
    CRITICAL, // Immediate action required, system access may be suspended
    HIGH,     // Action required within 7 days
    MEDIUM,   // Action required within 30 days
    LOW       // Action required within 90 days
}

enum class ComplianceAlertType {
    CERTIFICATION_EXPIRING,
    REVIEW_OVERDUE,
    TRAINING_REQUIRED,
    VIOLATION_DETECTED,
    AUDIT_SCHEDULED,
    CORRECTIVE_ACTION_DUE
}

enum class HipaaDataAccessType {
    PATIENT_DEMOGRAPHICS,
    MEDICAL_HISTORY,
    ASSESSMENT_RESULTS,
    TREATMENT_PLANS,
    MONITORING_DATA,
    EMERGENCY_CONTACTS,
    INSURANCE_INFORMATION,
    FULL_RECORD_ACCESS
}

enum class HipaaAccessMethod {
    DIRECT_SYSTEM_ACCESS,
    API_ACCESS,
    REPORT_GENERATION,
    EMERGENCY_ACCESS,
    DELEGATED_ACCESS
}

enum class HipaaConsentType {
    TREATMENT_CONSENT,
    MONITORING_CONSENT,
    DATA_SHARING_CONSENT,
    RESEARCH_CONSENT,
    EMERGENCY_CONSENT
}

enum class ConsentVerificationMethod {
    WRITTEN_SIGNATURE,
    DIGITAL_SIGNATURE,
    VERBAL_WITNESSED,
    ELECTRONIC_CONSENT,
    GUARDIAN_CONSENT
}

enum class ClinicalGovernanceAuditType {
    INITIAL_ASSESSMENT,
    ROUTINE_REVIEW,
    INCIDENT_INVESTIGATION,
    COMPLIANCE_AUDIT,
    QUALITY_ASSURANCE,
    CERTIFICATION_RENEWAL
}

enum class ClinicalGovernanceStandard {
    NHS_CLINICAL_GOVERNANCE,
    CQC_STANDARDS,
    NICE_GUIDELINES,
    GMC_GOOD_MEDICAL_PRACTICE,
    NMC_CODE_OF_CONDUCT,
    HIPAA_COMPLIANCE,
    GDPR_COMPLIANCE
}

enum class AuditStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    UNDER_REVIEW,
    APPROVED,
    REQUIRES_FOLLOW_UP
}

enum class GovernanceFindingCategory {
    PATIENT_SAFETY,
    CLINICAL_EFFECTIVENESS,
    PATIENT_EXPERIENCE,
    STAFF_COMPETENCE,
    INFORMATION_GOVERNANCE,
    RISK_MANAGEMENT,
    SAFEGUARDING
}

enum class VulnerabilityFactorType {
    COGNITIVE_IMPAIRMENT,
    PHYSICAL_DISABILITY,
    SOCIAL_ISOLATION,
    FINANCIAL_VULNERABILITY,
    COMMUNICATION_BARRIERS,
    DEPENDENCY_ON_CAREGIVER,
    HISTORY_OF_ABUSE,
    MENTAL_HEALTH_ISSUES
}

enum class VulnerabilitySeverity {
    MINIMAL,
    MODERATE,
    SIGNIFICANT,
    SEVERE
}

enum class ProtectionMeasureType {
    ENHANCED_MONITORING,
    CAREGIVER_TRAINING,
    ADVOCATE_INVOLVEMENT,
    EMERGENCY_PROCEDURES,
    ACCESS_RESTRICTIONS,
    REPORTING_MECHANISMS,
    SUPPORT_SERVICES,
    LEGAL_SAFEGUARDS
}

enum class RegulatoryFramework {
    UK_CARE_QUALITY_COMMISSION,
    UK_GENERAL_MEDICAL_COUNCIL,
    UK_NURSING_MIDWIFERY_COUNCIL,
    US_HIPAA,
    EU_GDPR,
    UK_MENTAL_CAPACITY_ACT,
    UK_CARE_ACT,
    UK_SAFEGUARDING_ADULTS
}

enum class ComplianceStatus {
    COMPLIANT,
    NON_COMPLIANT,
    PARTIALLY_COMPLIANT,
    UNDER_REVIEW,
    REMEDIATION_IN_PROGRESS
}

enum class ComplianceCategory {
    PROFESSIONAL_REGISTRATION,
    CLINICAL_COMPETENCE,
    PATIENT_SAFETY,
    DATA_PROTECTION,
    SAFEGUARDING,
    QUALITY_ASSURANCE,
    RISK_MANAGEMENT,
    INFORMATION_GOVERNANCE
}

enum class VerificationMethod {
    DOCUMENT_REVIEW,
    SYSTEM_AUDIT,
    WITNESS_VERIFICATION,
    THIRD_PARTY_CERTIFICATION,
    PERFORMANCE_ASSESSMENT,
    COMPLIANCE_TESTING
}

enum class RegulatoryViolationType {
    LICENSING_VIOLATION,
    SCOPE_OF_PRACTICE_VIOLATION,
    DOCUMENTATION_VIOLATION,
    PATIENT_SAFETY_VIOLATION,
    PRIVACY_VIOLATION,
    SAFEGUARDING_VIOLATION,
    QUALITY_STANDARD_VIOLATION
}

enum class PenaltyRisk {
    NO_PENALTY,
    WARNING_ONLY,
    FINANCIAL_PENALTY,
    LICENSE_SUSPENSION,
    LICENSE_REVOCATION,
    CRIMINAL_PROSECUTION
}

// ==================== TYPE CONVERTERS ====================

class MedicalComplianceConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(strings: List<String>): String = gson.toJson(strings)
    
    @TypeConverter
    fun toStringList(stringsString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringsString, listType)
    }
    
    @TypeConverter
    fun fromGovernanceFindingList(findings: List<GovernanceFinding>): String = gson.toJson(findings)
    
    @TypeConverter
    fun toGovernanceFindingList(findingsString: String): List<GovernanceFinding> {
        val listType = object : TypeToken<List<GovernanceFinding>>() {}.type
        return gson.fromJson(findingsString, listType)
    }
    
    @TypeConverter
    fun fromVulnerabilityFactorList(factors: List<VulnerabilityFactor>): String = gson.toJson(factors)
    
    @TypeConverter
    fun toVulnerabilityFactorList(factorsString: String): List<VulnerabilityFactor> {
        val listType = object : TypeToken<List<VulnerabilityFactor>>() {}.type
        return gson.fromJson(factorsString, listType)
    }
    
    @TypeConverter
    fun fromProtectionMeasureList(measures: List<ProtectionMeasure>): String = gson.toJson(measures)
    
    @TypeConverter
    fun toProtectionMeasureList(measuresString: String): List<ProtectionMeasure> {
        val listType = object : TypeToken<List<ProtectionMeasure>>() {}.type
        return gson.fromJson(measuresString, listType)
    }
    
    @TypeConverter
    fun fromSafeguardingPlan(plan: SafeguardingPlan): String = gson.toJson(plan)
    
    @TypeConverter
    fun toSafeguardingPlan(planString: String): SafeguardingPlan =
        gson.fromJson(planString, SafeguardingPlan::class.java)
    
    @TypeConverter
    fun fromComplianceRequirementList(requirements: List<ComplianceRequirement>): String = gson.toJson(requirements)
    
    @TypeConverter
    fun toComplianceRequirementList(requirementsString: String): List<ComplianceRequirement> {
        val listType = object : TypeToken<List<ComplianceRequirement>>() {}.type
        return gson.fromJson(requirementsString, listType)
    }
    
    @TypeConverter
    fun fromRegulatoryViolationList(violations: List<RegulatoryViolation>): String = gson.toJson(violations)
    
    @TypeConverter
    fun toRegulatoryViolationList(violationsString: String): List<RegulatoryViolation> {
        val listType = object : TypeToken<List<RegulatoryViolation>>() {}.type
        return gson.fromJson(violationsString, listType)
    }
    
    @TypeConverter
    fun fromCorrectionPlan(plan: CorrectionPlan?): String? = gson.toJson(plan)
    
    @TypeConverter
    fun toCorrectionPlan(planString: String?): CorrectionPlan? =
        planString?.let { gson.fromJson(it, CorrectionPlan::class.java) }
}
