package com.naviya.launcher.compliance.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * GDPR Compliance Data Models for Naviya Elder Protection System
 * Comprehensive data structures for GDPR compliance, consent management, and data subject rights
 */

@Entity(tableName = "consent_records")
@TypeConverters(GDPRComplianceConverters::class)
data class ConsentRecord(
    @PrimaryKey
    val consentId: String,
    val userId: String,
    val consentType: ConsentType,
    val lawfulBasis: LawfulBasis,
    val purposes: List<ProcessingPurpose>,
    val dataCategories: List<PersonalDataCategory>,
    val consentMethod: ConsentMethod,
    val consentTimestamp: Long,
    val isActive: Boolean = true,
    val retentionPeriod: Long,
    val renewalRequired: Boolean = true,
    val nextRenewalDate: Long,
    val consentVersion: String,
    val consentText: String? = null,
    val witnessId: String? = null,
    val legalGuardianConsent: Boolean = false,
    val legalGuardianId: String? = null,
    val withdrawalTimestamp: Long? = null,
    val withdrawalReason: String? = null,
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "consent_withdrawals")
data class ConsentWithdrawal(
    @PrimaryKey
    val withdrawalId: String,
    val userId: String,
    val consentId: String,
    val withdrawalTimestamp: Long,
    val withdrawalReason: String? = null,
    val withdrawalMethod: ConsentMethod = ConsentMethod.EXPLICIT_OPT_OUT,
    val dataRetentionAction: DataRetentionAction,
    val processingCeased: Boolean = false,
    val dataSubjectNotified: Boolean = false,
    val thirdPartiesNotified: List<String> = emptyList(),
    val effectiveDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "data_subject_requests")
@TypeConverters(GDPRComplianceConverters::class)
data class DataSubjectRequest(
    @PrimaryKey
    val requestId: String,
    val userId: String,
    val requesterId: String, // May be different from userId (legal guardian, etc.)
    val requestType: DataSubjectRequestType,
    val requestTimestamp: Long,
    val status: DataSubjectRequestStatus,
    val responseDeadline: Long,
    val identityVerified: Boolean = false,
    val identityVerificationMethod: String? = null,
    val requestValid: Boolean = false,
    val validationReason: String? = null,
    val responseGenerated: Boolean = false,
    val responseTimestamp: Long? = null,
    val responseMethod: ResponseMethod? = null,
    val requestDetails: String? = null,
    val internalNotes: String? = null,
    val assignedProcessor: String? = null,
    val escalationRequired: Boolean = false,
    val escalationReason: String? = null
)

@Entity(tableName = "data_export_results")
@TypeConverters(GDPRComplianceConverters::class)
data class DataExportResult(
    @PrimaryKey
    val exportId: String,
    val userId: String,
    val requestId: String,
    val exportFormat: DataExportFormat,
    val exportTimestamp: Long,
    val dataCategories: List<PersonalDataCategory>,
    val exportSize: Long, // Size in bytes
    val exportPath: String? = null,
    val expirationTimestamp: Long,
    val downloadCount: Int = 0,
    val lastDownloadTimestamp: Long? = null,
    val isEncrypted: Boolean = true,
    val encryptionMethod: String = "AES-256",
    val checksumHash: String? = null,
    val errorMessage: String? = null,
    val deliveryMethod: DataDeliveryMethod = DataDeliveryMethod.SECURE_DOWNLOAD
)

@Entity(tableName = "data_erasure_results")
@TypeConverters(GDPRComplianceConverters::class)
data class DataErasureResult(
    @PrimaryKey
    val erasureId: String,
    val userId: String,
    val requestId: String,
    val erasureReason: ErasureReason,
    val erasureTimestamp: Long,
    val erasureCompleted: Boolean = false,
    val dataErased: List<PersonalDataCategory>,
    val dataRetained: List<PersonalDataCategory>,
    val legalBasisForRetention: Map<PersonalDataCategory, String>,
    val thirdPartiesNotified: List<String>,
    val verificationRequired: Boolean = true,
    val verificationCompleted: Boolean = false,
    val verificationTimestamp: Long? = null,
    val errorMessage: String? = null,
    val backupDataErased: Boolean = false,
    val logDataErased: Boolean = false
)

@Entity(tableName = "data_breach_reports")
@TypeConverters(GDPRComplianceConverters::class)
data class DataBreachReport(
    @PrimaryKey
    val breachId: String,
    val breachTimestamp: Long,
    val discoveryTimestamp: Long = breachTimestamp,
    val reportTimestamp: Long,
    val breachType: DataBreachType,
    val affectedDataCategories: List<PersonalDataCategory>,
    val affectedUserCount: Int,
    val riskLevel: BreachRiskLevel,
    val breachDescription: String,
    val breachCause: String? = null,
    val technicalMeasures: List<String>,
    val organizationalMeasures: List<String>,
    val authorityNotified: Boolean = false,
    val authorityNotificationTimestamp: Long? = null,
    val dataSubjectsNotified: Boolean = false,
    val dataSubjectNotificationTimestamp: Long? = null,
    val notificationDeadline: Long,
    val remedialActions: List<String>,
    val preventiveMeasures: List<String>,
    val containmentActions: List<String> = emptyList(),
    val recoveryActions: List<String> = emptyList(),
    val lessonsLearned: String? = null,
    val isResolved: Boolean = false,
    val resolutionTimestamp: Long? = null
)

@Entity(tableName = "privacy_by_design_assessments")
@TypeConverters(GDPRComplianceConverters::class)
data class PrivacyByDesignAssessment(
    @PrimaryKey
    val assessmentId: String,
    val assessmentTimestamp: Long,
    val assessorId: String? = null,
    val systemVersion: String? = null,
    val principles: Map<PrivacyPrinciple, PrivacyComplianceLevel>,
    val overallCompliance: PrivacyComplianceLevel,
    val complianceScore: Double = 0.0,
    val recommendations: List<String>,
    val criticalIssues: List<String> = emptyList(),
    val improvementActions: List<String> = emptyList(),
    val nextAssessmentDate: Long,
    val assessmentNotes: String? = null,
    val stakeholdersInvolved: List<String> = emptyList(),
    val documentationReviewed: List<String> = emptyList(),
    val technicalReviewCompleted: Boolean = false,
    val legalReviewCompleted: Boolean = false
)

@Entity(tableName = "compliance_reports")
@TypeConverters(GDPRComplianceConverters::class)
data class ComplianceReport(
    @PrimaryKey
    val reportId: String,
    val reportType: ComplianceReportType,
    val generationTimestamp: Long,
    val reportPeriodStart: Long,
    val reportPeriodEnd: Long,
    val generatedBy: String? = null,
    val consentMetrics: ConsentMetrics,
    val dataSubjectRequestMetrics: DataSubjectRequestMetrics,
    val dataBreachMetrics: DataBreachMetrics,
    val privacyAssessmentResults: List<PrivacyByDesignAssessment>,
    val complianceScore: Double,
    val recommendations: List<String>,
    val actionItems: List<String> = emptyList(),
    val nextReviewDate: Long,
    val executiveSummary: String? = null,
    val detailedFindings: String? = null,
    val riskAssessment: String? = null,
    val isPublished: Boolean = false,
    val publishedTimestamp: Long? = null
)

@Entity(tableName = "data_processing_activities")
@TypeConverters(GDPRComplianceConverters::class)
data class DataProcessingActivity(
    @PrimaryKey
    val activityId: String,
    val activityName: String,
    val description: String,
    val controller: String,
    val processor: String? = null,
    val purposes: List<ProcessingPurpose>,
    val lawfulBasis: LawfulBasis,
    val dataCategories: List<PersonalDataCategory>,
    val dataSubjectCategories: List<DataSubjectCategory>,
    val recipients: List<String> = emptyList(),
    val thirdCountryTransfers: List<ThirdCountryTransfer> = emptyList(),
    val retentionPeriod: Long,
    val technicalMeasures: List<String>,
    val organizationalMeasures: List<String>,
    val riskLevel: ProcessingRiskLevel,
    val dpiaRequired: Boolean = false,
    val dpiaCompleted: Boolean = false,
    val dpiaId: String? = null,
    val createdTimestamp: Long,
    val lastReviewTimestamp: Long,
    val nextReviewDate: Long,
    val isActive: Boolean = true
)

@Entity(tableName = "data_protection_impact_assessments")
@TypeConverters(GDPRComplianceConverters::class)
data class DataProtectionImpactAssessment(
    @PrimaryKey
    val dpiaId: String,
    val activityId: String,
    val assessmentTimestamp: Long,
    val assessorId: String,
    val riskIdentification: List<PrivacyRisk>,
    val riskMitigation: Map<String, List<String>>, // Risk ID to mitigation measures
    val residualRisk: ProcessingRiskLevel,
    val consultationRequired: Boolean = false,
    val consultationCompleted: Boolean = false,
    val supervisoryAuthorityConsulted: Boolean = false,
    val stakeholdersConsulted: List<String> = emptyList(),
    val recommendations: List<String>,
    val approvalStatus: DPIAApprovalStatus,
    val approvedBy: String? = null,
    val approvalTimestamp: Long? = null,
    val reviewDate: Long,
    val isValid: Boolean = true,
    val notes: String? = null
)

@Entity(tableName = "consent_audit_logs")
@TypeConverters(GDPRComplianceConverters::class)
data class ConsentAuditLog(
    @PrimaryKey
    val logId: String,
    val userId: String,
    val eventType: ConsentEventType,
    val consentId: String? = null,
    val timestamp: Long,
    val eventDetails: String,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val sessionId: String? = null,
    val witnessId: String? = null,
    val legalBasis: LawfulBasis? = null,
    val dataCategories: List<PersonalDataCategory> = emptyList(),
    val processingPurposes: List<ProcessingPurpose> = emptyList(),
    val previousConsentState: String? = null,
    val newConsentState: String? = null,
    val automaticProcessing: Boolean = false,
    val systemGenerated: Boolean = false
)

// ==================== SUPPORTING DATA CLASSES ====================

data class ConsentMetrics(
    val totalConsents: Int,
    val activeConsents: Int,
    val withdrawnConsents: Int,
    val expiredConsents: Int,
    val renewalRate: Double,
    val averageConsentDuration: Long,
    val consentsByPurpose: Map<ProcessingPurpose, Int>,
    val consentsByMethod: Map<ConsentMethod, Int>
)

data class DataSubjectRequestMetrics(
    val totalRequests: Int,
    val accessRequests: Int,
    val erasureRequests: Int,
    val portabilityRequests: Int,
    val rectificationRequests: Int,
    val restrictionRequests: Int,
    val objectionRequests: Int,
    val averageResponseTime: Long,
    val onTimeResponses: Int,
    val overdueResponses: Int,
    val requestsByMonth: Map<String, Int>
)

data class DataBreachMetrics(
    val totalBreaches: Int,
    val highRiskBreaches: Int,
    val mediumRiskBreaches: Int,
    val lowRiskBreaches: Int,
    val averageResponseTime: Long,
    val affectedUsers: Int,
    val breachesByType: Map<DataBreachType, Int>,
    val breachesByCategory: Map<PersonalDataCategory, Int>,
    val authorityNotificationRate: Double,
    val dataSubjectNotificationRate: Double
)

data class ErasureAssessment(
    val canErase: Boolean,
    val reason: String,
    val categoriesToErase: List<PersonalDataCategory>,
    val categoriesToRetain: List<PersonalDataCategory>,
    val retentionReasons: Map<PersonalDataCategory, String>
)

data class DataBreachDetails(
    val discoveryTimestamp: Long,
    val breachType: DataBreachType,
    val affectedDataCategories: List<PersonalDataCategory>,
    val description: String,
    val technicalMeasures: List<String>,
    val organizationalMeasures: List<String>,
    val remedialActions: List<String>,
    val preventiveMeasures: List<String>
)

data class ThirdCountryTransfer(
    val country: String,
    val adequacyDecision: Boolean,
    val safeguards: List<String>,
    val legalBasis: String
)

data class PrivacyRisk(
    val riskId: String,
    val riskDescription: String,
    val likelihood: RiskLikelihood,
    val impact: RiskImpact,
    val riskLevel: ProcessingRiskLevel,
    val affectedDataSubjects: List<DataSubjectCategory>,
    val affectedDataCategories: List<PersonalDataCategory>
)

// ==================== ENUMS ====================

enum class ConsentType {
    EXPLICIT_CONSENT,       // Article 7 - explicit consent
    LEGITIMATE_INTEREST,    // Article 6(1)(f) - legitimate interest
    VITAL_INTERESTS,        // Article 6(1)(d) - vital interests
    PUBLIC_TASK,           // Article 6(1)(e) - public task
    LEGAL_OBLIGATION,      // Article 6(1)(c) - legal obligation
    CONTRACT_PERFORMANCE   // Article 6(1)(b) - contract performance
}

enum class LawfulBasis {
    CONSENT,              // Article 6(1)(a)
    CONTRACT,             // Article 6(1)(b)
    LEGAL_OBLIGATION,     // Article 6(1)(c)
    VITAL_INTERESTS,      // Article 6(1)(d)
    PUBLIC_TASK,          // Article 6(1)(e)
    LEGITIMATE_INTERESTS  // Article 6(1)(f)
}

enum class ProcessingPurpose {
    EMERGENCY_RESPONSE,           // Emergency and safety services
    ABUSE_DETECTION,             // Elder abuse detection and prevention
    CAREGIVER_COMMUNICATION,     // Communication with caregivers
    HEALTH_MONITORING,           // Basic health status monitoring
    MEDICATION_REMINDERS,        // Medication compliance
    CONTACT_MANAGEMENT,          // Emergency contact management
    SYSTEM_FUNCTIONALITY,        // Core app functionality
    ANALYTICS_IMPROVEMENT,       // Service improvement analytics
    LEGAL_COMPLIANCE,           // Legal and regulatory compliance
    RESEARCH_DEVELOPMENT        // Research and development (anonymized)
}

enum class PersonalDataCategory {
    CONTACT_INFORMATION,    // Name, phone, email, address
    HEALTH_DATA,           // Health status, medical information
    LOCATION_DATA,         // GPS coordinates, approximate location
    USAGE_DATA,            // App usage patterns, interaction data
    EMERGENCY_CONTACTS,    // Emergency contact information
    CAREGIVER_DATA,        // Caregiver relationship and permissions
    DEVICE_DATA,           // Device identifiers, technical data
    COMMUNICATION_DATA,    // Messages, call logs, notifications
    BIOMETRIC_DATA,        // Voice patterns, behavioral biometrics
    FINANCIAL_DATA         // Payment information (if applicable)
}

enum class DataSubjectCategory {
    ELDERLY_USERS,         // Primary elderly users
    CAREGIVERS,           // Family caregivers
    EMERGENCY_CONTACTS,   // Emergency contact persons
    HEALTHCARE_PROVIDERS, // Medical professionals
    LEGAL_GUARDIANS,      // Legal guardians
    ELDER_RIGHTS_ADVOCATES // Elder rights advocates
}

enum class ConsentMethod {
    EXPLICIT_OPT_IN,       // Clear affirmative action
    IMPLICIT_OPT_IN,       // Implied consent (limited use)
    EXPLICIT_OPT_OUT,      // Clear withdrawal action
    AUTOMATIC_RENEWAL,     // Automatic consent renewal
    WITNESSED_CONSENT,     // Consent with witness present
    GUARDIAN_CONSENT       // Legal guardian consent
}

enum class DataSubjectRequestType {
    ACCESS,               // Article 15 - Right of access
    RECTIFICATION,        // Article 16 - Right to rectification
    ERASURE,             // Article 17 - Right to erasure
    RESTRICTION,         // Article 18 - Right to restriction
    PORTABILITY,         // Article 20 - Right to data portability
    OBJECTION,           // Article 21 - Right to object
    AUTOMATED_DECISION   // Article 22 - Automated decision-making
}

enum class DataSubjectRequestStatus {
    RECEIVED,            // Request received
    IDENTITY_VERIFICATION, // Verifying requester identity
    UNDER_REVIEW,        // Request being reviewed
    IN_PROGRESS,         // Request being processed
    COMPLETED,           // Request completed
    REJECTED,            // Request rejected
    PARTIALLY_FULFILLED, // Request partially fulfilled
    EXTENDED,            // Response time extended
    ESCALATED           // Request escalated
}

enum class DataExportFormat {
    JSON,
    XML,
    CSV,
    PDF;
    
    val extension: String
        get() = when (this) {
            JSON -> "json"
            XML -> "xml"
            CSV -> "csv"
            PDF -> "pdf"
        }
}

enum class DataDeliveryMethod {
    SECURE_DOWNLOAD,     // Encrypted download link
    ENCRYPTED_EMAIL,     // Encrypted email attachment
    POSTAL_MAIL,         // Physical mail
    SECURE_PORTAL,       // Secure web portal
    API_ENDPOINT        // Secure API endpoint
}

enum class ErasureReason {
    CONSENT_WITHDRAWN,           // Consent withdrawn
    PURPOSE_FULFILLED,           // Processing purpose fulfilled
    UNLAWFUL_PROCESSING,        // Processing was unlawful
    LEGAL_OBLIGATION,           // Legal obligation to erase
    CHILD_CONSENT_WITHDRAWN,    // Child's consent withdrawn
    OBJECTION_UPHELD           // Objection to processing upheld
}

enum class DataRetentionAction {
    DELETE,              // Permanently delete data
    ANONYMIZE,           // Anonymize personal data
    PSEUDONYMIZE,        // Pseudonymize personal data
    ARCHIVE,             // Archive with restricted access
    RETAIN_LEGAL        // Retain for legal obligations
}

enum class DataBreachType {
    CONFIDENTIALITY_BREACH,  // Unauthorized access/disclosure
    INTEGRITY_BREACH,        // Unauthorized alteration
    AVAILABILITY_BREACH,     // Loss of access/availability
    COMBINED_BREACH         // Multiple types combined
}

enum class BreachRiskLevel {
    LOW,                // Unlikely to result in risk
    MEDIUM,             // Likely to result in limited risk
    HIGH,               // Likely to result in high risk
    CRITICAL           // Likely to result in severe risk
}

enum class PrivacyPrinciple {
    PROACTIVE_NOT_REACTIVE,        // Anticipate and prevent privacy invasions
    PRIVACY_AS_DEFAULT,            // Maximum privacy protection by default
    PRIVACY_EMBEDDED_INTO_DESIGN,  // Privacy is a core component
    FULL_FUNCTIONALITY,            // All legitimate interests accommodated
    END_TO_END_SECURITY,           // Secure data lifecycle
    VISIBILITY_AND_TRANSPARENCY,   // Ensure all stakeholders can verify
    RESPECT_FOR_USER_PRIVACY      // Keep user interests paramount
}

enum class PrivacyComplianceLevel {
    COMPLIANT,          // Fully compliant
    MOSTLY_COMPLIANT,   // Minor issues
    PARTIALLY_COMPLIANT, // Some significant issues
    NON_COMPLIANT       // Major compliance issues
}

enum class ComplianceReportType {
    MONTHLY,            // Monthly compliance report
    QUARTERLY,          // Quarterly compliance report
    ANNUAL,             // Annual compliance report
    INCIDENT,           // Incident-specific report
    AUDIT,              // Audit compliance report
    REGULATORY         // Regulatory submission report
}

enum class ProcessingRiskLevel {
    LOW,               // Low risk to data subjects
    MEDIUM,            // Medium risk to data subjects
    HIGH,              // High risk to data subjects
    VERY_HIGH         // Very high risk to data subjects
}

enum class DPIAApprovalStatus {
    PENDING,           // DPIA pending approval
    APPROVED,          // DPIA approved
    REJECTED,          // DPIA rejected
    CONDITIONAL,       // DPIA conditionally approved
    UNDER_REVIEW      // DPIA under review
}

enum class ConsentEventType {
    CONSENT_GIVEN,     // User provided consent
    CONSENT_WITHDRAWN, // User withdrew consent
    CONSENT_RENEWED,   // Consent was renewed
    CONSENT_EXPIRED,   // Consent expired
    CONSENT_MODIFIED,  // Consent terms modified
    CONSENT_VERIFIED  // Consent verification completed
}

enum class ResponseMethod {
    EMAIL,             // Response via email
    POSTAL_MAIL,       // Response via postal mail
    SECURE_PORTAL,     // Response via secure portal
    IN_PERSON,         // Response in person
    PHONE_CALL        // Response via phone call
}

enum class RiskLikelihood {
    VERY_LOW,          // Very unlikely to occur
    LOW,               // Unlikely to occur
    MEDIUM,            // Possible to occur
    HIGH,              // Likely to occur
    VERY_HIGH         // Very likely to occur
}

enum class RiskImpact {
    NEGLIGIBLE,        // Negligible impact
    MINOR,             // Minor impact
    MODERATE,          // Moderate impact
    MAJOR,             // Major impact
    SEVERE            // Severe impact
}

// ==================== TYPE CONVERTERS ====================

class GDPRComplianceConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromProcessingPurposeList(purposes: List<ProcessingPurpose>): String {
        return gson.toJson(purposes)
    }

    @TypeConverter
    fun toProcessingPurposeList(purposesString: String): List<ProcessingPurpose> {
        val listType = object : TypeToken<List<ProcessingPurpose>>() {}.type
        return gson.fromJson(purposesString, listType)
    }

    @TypeConverter
    fun fromPersonalDataCategoryList(categories: List<PersonalDataCategory>): String {
        return gson.toJson(categories)
    }

    @TypeConverter
    fun toPersonalDataCategoryList(categoriesString: String): List<PersonalDataCategory> {
        val listType = object : TypeToken<List<PersonalDataCategory>>() {}.type
        return gson.fromJson(categoriesString, listType)
    }

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
    fun fromPersonalDataCategoryStringMap(map: Map<PersonalDataCategory, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toPersonalDataCategoryStringMap(mapString: String): Map<PersonalDataCategory, String> {
        val mapType = object : TypeToken<Map<PersonalDataCategory, String>>() {}.type
        return gson.fromJson(mapString, mapType)
    }

    @TypeConverter
    fun fromPrivacyPrincipleComplianceMap(map: Map<PrivacyPrinciple, PrivacyComplianceLevel>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toPrivacyPrincipleComplianceMap(mapString: String): Map<PrivacyPrinciple, PrivacyComplianceLevel> {
        val mapType = object : TypeToken<Map<PrivacyPrinciple, PrivacyComplianceLevel>>() {}.type
        return gson.fromJson(mapString, mapType)
    }

    @TypeConverter
    fun fromConsentMetrics(metrics: ConsentMetrics): String {
        return gson.toJson(metrics)
    }

    @TypeConverter
    fun toConsentMetrics(metricsString: String): ConsentMetrics {
        return gson.fromJson(metricsString, ConsentMetrics::class.java)
    }

    @TypeConverter
    fun fromDataSubjectRequestMetrics(metrics: DataSubjectRequestMetrics): String {
        return gson.toJson(metrics)
    }

    @TypeConverter
    fun toDataSubjectRequestMetrics(metricsString: String): DataSubjectRequestMetrics {
        return gson.fromJson(metricsString, DataSubjectRequestMetrics::class.java)
    }

    @TypeConverter
    fun fromDataBreachMetrics(metrics: DataBreachMetrics): String {
        return gson.toJson(metrics)
    }

    @TypeConverter
    fun toDataBreachMetrics(metricsString: String): DataBreachMetrics {
        return gson.fromJson(metricsString, DataBreachMetrics::class.java)
    }

    @TypeConverter
    fun fromPrivacyByDesignAssessmentList(assessments: List<PrivacyByDesignAssessment>): String {
        return gson.toJson(assessments)
    }

    @TypeConverter
    fun toPrivacyByDesignAssessmentList(assessmentsString: String): List<PrivacyByDesignAssessment> {
        val listType = object : TypeToken<List<PrivacyByDesignAssessment>>() {}.type
        return gson.fromJson(assessmentsString, listType)
    }

    @TypeConverter
    fun fromDataSubjectCategoryList(categories: List<DataSubjectCategory>): String {
        return gson.toJson(categories)
    }

    @TypeConverter
    fun toDataSubjectCategoryList(categoriesString: String): List<DataSubjectCategory> {
        val listType = object : TypeToken<List<DataSubjectCategory>>() {}.type
        return gson.fromJson(categoriesString, listType)
    }

    @TypeConverter
    fun fromThirdCountryTransferList(transfers: List<ThirdCountryTransfer>): String {
        return gson.toJson(transfers)
    }

    @TypeConverter
    fun toThirdCountryTransferList(transfersString: String): List<ThirdCountryTransfer> {
        val listType = object : TypeToken<List<ThirdCountryTransfer>>() {}.type
        return gson.fromJson(transfersString, listType)
    }

    @TypeConverter
    fun fromPrivacyRiskList(risks: List<PrivacyRisk>): String {
        return gson.toJson(risks)
    }

    @TypeConverter
    fun toPrivacyRiskList(risksString: String): List<PrivacyRisk> {
        val listType = object : TypeToken<List<PrivacyRisk>>() {}.type
        return gson.fromJson(risksString, listType)
    }

    @TypeConverter
    fun fromStringListMap(map: Map<String, List<String>>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toStringListMap(mapString: String): Map<String, List<String>> {
        val mapType = object : TypeToken<Map<String, List<String>>>() {}.type
        return gson.fromJson(mapString, mapType)
    }
}
