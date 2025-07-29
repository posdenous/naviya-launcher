package com.naviya.launcher.compliance.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Medical Device Compliance Data Models for Naviya Elder Protection System
 * Comprehensive data structures for medical device regulatory compliance
 * Supports FDA, CE marking, ISO standards, and international regulations
 */

@Entity(tableName = "device_classifications")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class DeviceClassificationResult(
    @PrimaryKey
    val classificationId: String,
    val deviceName: String,
    val intendedUse: String,
    val deviceClass: MedicalDeviceClass,
    val riskLevel: DeviceRiskLevel,
    val regulatoryPathway: RegulatoryPathway,
    val ceMarkingRequired: Boolean,
    val fdaApprovalRequired: Boolean,
    val clinicalDataRequired: Boolean,
    val qualitySystemRequired: Boolean,
    val postMarketSurveillanceRequired: Boolean,
    val adverseEventReportingRequired: Boolean,
    val classificationRationale: String,
    val applicableStandards: List<String>,
    val regulatoryRequirements: List<String>,
    val classificationTimestamp: Long,
    val reviewDate: Long,
    val isValid: Boolean = true,
    val classificationAuthority: RegulatoryAuthority? = null,
    val predicate510kDevices: List<String> = emptyList(),
    val specialControls: List<String> = emptyList(),
    val exemptions: List<String> = emptyList()
)

@Entity(tableName = "risk_management_results")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class RiskManagementResult(
    @PrimaryKey
    val assessmentId: String,
    val deviceId: String,
    val assessmentTimestamp: Long,
    val riskAnalysisCompleted: Boolean,
    val riskEvaluationCompleted: Boolean,
    val riskControlMeasures: List<String>,
    val residualRiskAcceptable: Boolean,
    val riskManagementFile: String,
    val hazards: List<String>,
    val riskAcceptabilityCriteria: String,
    val postProductionInformation: String,
    val overallRiskLevel: DeviceRiskLevel,
    val riskBenefitAnalysis: String,
    val nextReviewDate: Long,
    val isCompliant: Boolean,
    val iso14971Version: String = "2019",
    val riskManagementPlan: String? = null,
    val hazardAnalysisMethod: String = "FMEA",
    val riskEstimationMethod: String = "Risk Matrix",
    val riskControlStrategy: String? = null,
    val residualRiskEvaluation: String? = null,
    val productionAndPostProductionInfo: String? = null
)

@Entity(tableName = "clinical_evaluation_results")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class ClinicalEvaluationResult(
    @PrimaryKey
    val evaluationId: String,
    val deviceId: String,
    val evaluationTimestamp: Long,
    val clinicalDataSources: List<String>,
    val literatureReview: String,
    val clinicalInvestigationRequired: Boolean,
    val equivalentDevices: List<String>,
    val clinicalEvidence: String,
    val safetyProfile: String,
    val clinicalPerformance: String,
    val benefitRiskProfile: String,
    val clinicalEvaluationReport: String,
    val postMarketClinicalFollowUp: String,
    val evaluatorQualifications: String,
    val nextReviewDate: Long,
    val isCompliant: Boolean,
    val mdrArticle61Compliant: Boolean = true,
    val clinicalDataAdequacy: ClinicalDataAdequacy = ClinicalDataAdequacy.ADEQUATE,
    val clinicalEvidenceLevel: ClinicalEvidenceLevel = ClinicalEvidenceLevel.MODERATE,
    val stateOfArtConsideration: String? = null,
    val targetPopulationAnalysis: String? = null,
    val clinicalRiskAnalysis: String? = null
)

@Entity(tableName = "technical_documentation")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class TechnicalDocumentationPackage(
    @PrimaryKey
    val packageId: String,
    val deviceId: String,
    val generationTimestamp: Long,
    val deviceDescription: String,
    val intendedPurpose: String,
    val riskManagementFile: String,
    val designAndManufacturingInfo: String,
    val softwareDocumentation: String,
    val clinicalEvaluationReport: String,
    val labelingAndInstructions: String,
    val declarationOfConformity: String,
    val qualityManagementCertificate: String,
    val postMarketSurveillancePlan: String,
    val adverseEventReportingProcedures: String,
    val packageVersion: String,
    val authorizedRepresentative: String,
    val notifiedBody: String,
    val isComplete: Boolean,
    val reviewDate: Long,
    val technicalFileLocation: String? = null,
    val documentControlNumber: String? = null,
    val approvalStatus: DocumentApprovalStatus = DocumentApprovalStatus.DRAFT,
    val approvedBy: String? = null,
    val approvalDate: Long? = null
)

@Entity(tableName = "post_market_surveillance")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class PostMarketSurveillanceSystem(
    @PrimaryKey
    val systemId: String,
    val deviceId: String,
    val setupTimestamp: Long,
    val surveillancePlan: String,
    val dataCollectionMethods: List<String>,
    val adverseEventReporting: String,
    val periodicSafetyUpdateReports: String,
    val trendAnalysis: String,
    val correctiveActions: String,
    val fieldSafetyNotices: String,
    val vigilanceReporting: String,
    val customerFeedbackSystem: String,
    val performanceMonitoring: String,
    val riskBenefitReassessment: String,
    val isActive: Boolean,
    val nextReviewDate: Long,
    val surveillanceScope: SurveillanceScope = SurveillanceScope.COMPREHENSIVE,
    val dataAnalysisFrequency: DataAnalysisFrequency = DataAnalysisFrequency.MONTHLY,
    val reportingThresholds: Map<String, Double> = emptyMap(),
    val stakeholderNotification: List<String> = emptyList()
)

@Entity(tableName = "adverse_event_reports")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class AdverseEventReport(
    @PrimaryKey
    val reportId: String,
    val deviceId: String,
    val eventTimestamp: Long,
    val reportTimestamp: Long,
    val eventType: AdverseEventType,
    val severity: AdverseEventSeverity,
    val causality: CausalityAssessment,
    val patientInformation: String,
    val deviceInformation: String,
    val eventDescription: String,
    val immediateActions: String,
    val followUpRequired: Boolean,
    val regulatoryReportingRequired: Boolean,
    val reportingDeadline: Long,
    val investigationStatus: AdverseEventInvestigationStatus,
    val correctiveActionsRequired: Boolean,
    val trendAnalysisImpact: String,
    val reportedToAuthorities: Boolean = false,
    val reportedToNotifiedBody: Boolean = false,
    val isResolved: Boolean = false,
    val resolutionDate: Long? = null,
    val investigationReport: String? = null,
    val rootCauseAnalysis: String? = null,
    val preventiveActions: String? = null,
    val regulatoryResponse: String? = null
)

@Entity(tableName = "regulatory_submissions")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class RegulatorySubmissionPackage(
    @PrimaryKey
    val submissionId: String,
    val deviceId: String,
    val submissionType: RegulatorySubmissionType,
    val targetAuthority: RegulatoryAuthority,
    val generationTimestamp: Long,
    val deviceClassification: String,
    val technicalDocumentation: String,
    val clinicalData: String,
    val riskManagementData: String,
    val qualitySystemCertificate: String,
    val labelingAndInstructions: String,
    val postMarketSurveillancePlan: String,
    val declarationOfConformity: String,
    val authorizedRepresentativeInfo: String,
    val manufacturerInformation: String,
    val submissionFees: Double,
    val expectedReviewTime: Long,
    val submissionStatus: RegulatorySubmissionStatus,
    val isComplete: Boolean,
    val submissionDate: Long? = null,
    val approvalDate: Long? = null,
    val approvalNumber: String? = null,
    val conditions: List<String> = emptyList(),
    val reviewComments: String? = null,
    val appealDeadline: Long? = null
)

@Entity(tableName = "medical_device_compliance_status")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class MedicalDeviceComplianceStatus(
    @PrimaryKey
    val statusId: String,
    val deviceId: String,
    val assessmentTimestamp: Long,
    val deviceClassificationCompliant: Boolean,
    val riskManagementCompliant: Boolean,
    val clinicalEvaluationCompliant: Boolean,
    val technicalDocumentationCompliant: Boolean,
    val qualitySystemCompliant: Boolean,
    val postMarketSurveillanceCompliant: Boolean,
    val regulatoryApprovalsStatus: Map<RegulatoryAuthority, String>,
    val overallComplianceLevel: ComplianceLevel,
    val criticalIssues: List<String>,
    val recommendedActions: List<String>,
    val nextAssessmentDate: Long,
    val complianceScore: Double,
    val isMarketReady: Boolean,
    val marketApprovalStatus: Map<String, MarketApprovalStatus> = emptyMap(),
    val complianceGaps: List<String> = emptyList(),
    val mitigationPlan: String? = null,
    val stakeholderNotifications: List<String> = emptyList()
)

@Entity(tableName = "quality_management_records")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class QualityManagementRecord(
    @PrimaryKey
    val recordId: String,
    val deviceId: String,
    val recordType: QualityRecordType,
    val recordTimestamp: Long,
    val iso13485Compliant: Boolean,
    val qualityObjectives: List<String>,
    val processControls: List<String>,
    val documentControl: String,
    val managementReview: String,
    val internalAudits: String,
    val correctiveActions: String,
    val preventiveActions: String,
    val supplierManagement: String,
    val designControls: String,
    val productionControls: String,
    val serviceControls: String,
    val nextReviewDate: Long,
    val isValid: Boolean = true,
    val certificationBody: String? = null,
    val certificateNumber: String? = null,
    val certificateExpiry: Long? = null,
    val surveillanceAudits: List<String> = emptyList()
)

@Entity(tableName = "usability_engineering_records")
@TypeConverters(MedicalDeviceComplianceConverters::class)
data class UsabilityEngineeringRecord(
    @PrimaryKey
    val recordId: String,
    val deviceId: String,
    val recordTimestamp: Long,
    val iec62366Compliant: Boolean,
    val usabilitySpecification: String,
    val userInterfaceSpecification: String,
    val useScenarios: List<String>,
    val userGroups: List<String>,
    val usabilityValidation: String,
    val summativeEvaluation: String,
    val usabilityRisks: List<String>,
    val riskMitigations: List<String>,
    val usabilityFile: String,
    val nextReviewDate: Long,
    val isCompliant: Boolean,
    val formativeEvaluations: List<String> = emptyList(),
    val userFeedback: String? = null,
    val accessibilityCompliance: String? = null,
    val elderlyUserConsiderations: String? = null
)

// ==================== SUPPORTING DATA CLASSES ====================

data class AdverseEventDetails(
    val eventTimestamp: Long,
    val eventType: AdverseEventType,
    val patientInformation: String,
    val deviceInformation: String,
    val eventDescription: String,
    val immediateActions: String
)

data class ClinicalStudyData(
    val studyId: String,
    val studyType: ClinicalStudyType,
    val studyPopulation: String,
    val primaryEndpoints: List<String>,
    val secondaryEndpoints: List<String>,
    val studyResults: String,
    val statisticalAnalysis: String,
    val conclusions: String
)

data class RegulatoryCorrespondence(
    val correspondenceId: String,
    val authority: RegulatoryAuthority,
    val correspondenceType: CorrespondenceType,
    val timestamp: Long,
    val subject: String,
    val content: String,
    val responseRequired: Boolean,
    val responseDeadline: Long? = null,
    val status: CorrespondenceStatus
)

// ==================== ENUMS ====================

enum class MedicalDeviceClass {
    CLASS_I,        // Low risk devices
    CLASS_IIA,      // Low-medium risk devices
    CLASS_IIB,      // Medium-high risk devices
    CLASS_III       // High risk devices
}

enum class DeviceRiskLevel {
    LOW,           // Minimal risk to patient/user
    MEDIUM,        // Moderate risk to patient/user
    HIGH,          // Significant risk to patient/user
    CRITICAL       // Life-threatening risk
}

enum class RegulatoryPathway {
    FDA_510K,              // FDA 510(k) Premarket Notification
    FDA_PMA,               // FDA Premarket Approval
    FDA_DE_NOVO,           // FDA De Novo Classification
    CE_MARKING_MDR,        // CE Marking under MDR
    CE_MARKING_MDD,        // CE Marking under MDD (legacy)
    HEALTH_CANADA_MDL,     // Health Canada Medical Device License
    TGA_CONFORMITY,        // TGA Conformity Assessment
    PMDA_APPROVAL,         // PMDA (Japan) Approval
    NMPA_APPROVAL          // NMPA (China) Approval
}

enum class RegulatoryAuthority {
    FDA,                   // US Food and Drug Administration
    EMA,                   // European Medicines Agency
    HEALTH_CANADA,         // Health Canada
    TGA,                   // Therapeutic Goods Administration (Australia)
    PMDA,                  // Pharmaceuticals and Medical Devices Agency (Japan)
    NMPA,                  // National Medical Products Administration (China)
    ANVISA,                // Brazilian Health Regulatory Agency
    COFEPRIS,              // Mexican Federal Commission for Sanitary Risks
    NOTIFIED_BODY          // European Notified Body
}

enum class RegulatorySubmissionType {
    INITIAL_SUBMISSION,    // Initial regulatory submission
    AMENDMENT,             // Amendment to existing submission
    SUPPLEMENT,            // Supplement to approved device
    RENEWAL,               // Renewal of existing approval
    VARIATION,             // Variation to existing approval
    WITHDRAWAL,            // Withdrawal of submission
    APPEAL                 // Appeal of regulatory decision
}

enum class RegulatorySubmissionStatus {
    PREPARED,              // Submission prepared but not submitted
    SUBMITTED,             // Submitted to regulatory authority
    UNDER_REVIEW,          // Under regulatory review
    ADDITIONAL_INFO_REQUESTED, // Additional information requested
    APPROVED,              // Approved by regulatory authority
    REJECTED,              // Rejected by regulatory authority
    WITHDRAWN,             // Withdrawn by applicant
    APPEALED              // Under appeal process
}

enum class AdverseEventType {
    DEVICE_MALFUNCTION,    // Device did not perform as intended
    USER_ERROR,            // Incorrect use of device
    DESIGN_DEFECT,         // Design-related issue
    MANUFACTURING_DEFECT,  // Manufacturing-related issue
    SOFTWARE_ERROR,        // Software-related issue
    LABELING_ERROR,        // Labeling or instructions issue
    INTERACTION_ISSUE,     // Device interaction issue
    ENVIRONMENTAL_FACTOR,  // Environmental factor impact
    OTHER                  // Other type of adverse event
}

enum class AdverseEventSeverity {
    MINOR,                 // Minor inconvenience or temporary discomfort
    MODERATE,              // Moderate impact on health or wellbeing
    SERIOUS,               // Serious impact requiring intervention
    CRITICAL,              // Life-threatening or permanent impairment
    FATAL                  // Death related to device use
}

enum class CausalityAssessment {
    UNRELATED,             // Event unrelated to device
    UNLIKELY,              // Event unlikely related to device
    POSSIBLE,              // Event possibly related to device
    PROBABLE,              // Event probably related to device
    DEFINITE               // Event definitely related to device
}

enum class AdverseEventInvestigationStatus {
    NOT_STARTED,           // Investigation not yet started
    INITIATED,             // Investigation initiated
    IN_PROGRESS,           // Investigation in progress
    ADDITIONAL_INFO_NEEDED, // Additional information needed
    COMPLETED,             // Investigation completed
    CLOSED                 // Investigation closed
}

enum class ComplianceLevel {
    NON_COMPLIANT,         // Does not meet requirements
    PARTIALLY_COMPLIANT,   // Meets some requirements
    MOSTLY_COMPLIANT,      // Meets most requirements
    COMPLIANT,             // Fully compliant
    EXCEEDS_REQUIREMENTS   // Exceeds compliance requirements
}

enum class MarketApprovalStatus {
    NOT_SUBMITTED,         // Not yet submitted for approval
    SUBMITTED,             // Submitted for approval
    UNDER_REVIEW,          // Under regulatory review
    APPROVED,              // Approved for market
    CONDITIONAL_APPROVAL,  // Conditionally approved
    REJECTED,              // Rejected for market
    SUSPENDED,             // Market approval suspended
    WITHDRAWN              // Market approval withdrawn
}

enum class QualityRecordType {
    MANAGEMENT_REVIEW,     // Management review records
    INTERNAL_AUDIT,        // Internal audit records
    CORRECTIVE_ACTION,     // Corrective action records
    PREVENTIVE_ACTION,     // Preventive action records
    DESIGN_CONTROL,        // Design control records
    SUPPLIER_EVALUATION,   // Supplier evaluation records
    PRODUCTION_CONTROL,    // Production control records
    SERVICE_CONTROL        // Service control records
}

enum class DocumentApprovalStatus {
    DRAFT,                 // Document in draft status
    UNDER_REVIEW,          // Document under review
    APPROVED,              // Document approved
    REJECTED,              // Document rejected
    OBSOLETE               // Document obsolete
}

enum class SurveillanceScope {
    BASIC,                 // Basic surveillance activities
    COMPREHENSIVE,         // Comprehensive surveillance
    ENHANCED,              // Enhanced surveillance for high-risk devices
    TARGETED               // Targeted surveillance for specific issues
}

enum class DataAnalysisFrequency {
    WEEKLY,                // Weekly data analysis
    MONTHLY,               // Monthly data analysis
    QUARTERLY,             // Quarterly data analysis
    ANNUALLY,              // Annual data analysis
    CONTINUOUS             // Continuous data analysis
}

enum class ClinicalDataAdequacy {
    INADEQUATE,            // Clinical data inadequate
    ADEQUATE,              // Clinical data adequate
    COMPREHENSIVE,         // Comprehensive clinical data
    EXCEPTIONAL            // Exceptional clinical data quality
}

enum class ClinicalEvidenceLevel {
    LOW,                   // Low level of clinical evidence
    MODERATE,              // Moderate level of clinical evidence
    HIGH,                  // High level of clinical evidence
    VERY_HIGH              // Very high level of clinical evidence
}

enum class ClinicalStudyType {
    OBSERVATIONAL,         // Observational study
    INTERVENTIONAL,        // Interventional study
    RANDOMIZED_CONTROLLED, // Randomized controlled trial
    COHORT,                // Cohort study
    CASE_CONTROL,          // Case-control study
    CROSS_SECTIONAL,       // Cross-sectional study
    SYSTEMATIC_REVIEW,     // Systematic review
    META_ANALYSIS          // Meta-analysis
}

enum class CorrespondenceType {
    INQUIRY,               // Inquiry to regulatory authority
    RESPONSE,              // Response from regulatory authority
    NOTIFICATION,          // Notification to regulatory authority
    REPORT,                // Report to regulatory authority
    REQUEST,               // Request to regulatory authority
    CLARIFICATION          // Clarification request/response
}

enum class CorrespondenceStatus {
    PENDING,               // Correspondence pending
    SENT,                  // Correspondence sent
    RECEIVED,              // Correspondence received
    UNDER_REVIEW,          // Under review
    RESPONDED,             // Responded to
    CLOSED                 // Correspondence closed
}

// ==================== TYPE CONVERTERS ====================

class MedicalDeviceComplianceConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return gson.toJson(strings)
    }

    @TypeConverter
    fun toStringList(stringsString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringsString, listType)
    }

    @TypeConverter
    fun fromRegulatoryAuthorityStringMap(map: Map<RegulatoryAuthority, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toRegulatoryAuthorityStringMap(mapString: String): Map<RegulatoryAuthority, String> {
        val mapType = object : TypeToken<Map<RegulatoryAuthority, String>>() {}.type
        return gson.fromJson(mapString, mapType)
    }

    @TypeConverter
    fun fromStringMarketApprovalStatusMap(map: Map<String, MarketApprovalStatus>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toStringMarketApprovalStatusMap(mapString: String): Map<String, MarketApprovalStatus> {
        val mapType = object : TypeToken<Map<String, MarketApprovalStatus>>() {}.type
        return gson.fromJson(mapString, mapType)
    }

    @TypeConverter
    fun fromStringDoubleMap(map: Map<String, Double>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toStringDoubleMap(mapString: String): Map<String, Double> {
        val mapType = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(mapString, mapType)
    }
}
