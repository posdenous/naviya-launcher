package com.naviya.launcher.healthcare.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Healthcare Professional Integration Data Models for Naviya Elder Protection System
 * Comprehensive data structures for professional installation, clinical oversight, and healthcare integration
 */

@Entity(tableName = "healthcare_professional_registrations")
data class HealthcareProfessionalRegistration(
    @PrimaryKey
    val registrationId: String,
    val professionalId: String,
    val personalDetails: HealthcareProfessionalDetails,
    val credentials: ProfessionalCredentials,
    val institutionAffiliation: HealthcareInstitution?,
    val registrationTimestamp: Long,
    val status: ProfessionalRegistrationStatus,
    val trainingCompleted: Boolean = false,
    val certificationStatus: CertificationStatus,
    val installationAuthorized: Boolean = false,
    val clinicalOversightLevel: ClinicalOversightLevel,
    val liabilityInsuranceVerified: Boolean = false,
    val backgroundCheckCompleted: Boolean = false,
    val ethicsTrainingCompleted: Boolean = false,
    val elderCareSpecializationVerified: Boolean = false,
    val lastReviewDate: Long,
    val nextRecertificationDate: Long,
    val trainingHoursCompleted: Int = 0,
    val requiredTrainingHours: Int = 8
)

@Entity(tableName = "professional_installations")
data class ProfessionalInstallation(
    @PrimaryKey
    val installationId: String,
    val userId: String,
    val professionalId: String,
    val institutionId: String?,
    val installationTimestamp: Long,
    val installationType: InstallationType,
    val clinicalContext: ClinicalContext,
    val patientConsent: ConsentRecord,
    val familyConsent: ConsentRecord?,
    val elderRightsAdvocateInformed: Boolean = false,
    val clinicalAssessmentCompleted: Boolean = false,
    val riskAssessmentCompleted: Boolean = false,
    val caregiverTrainingProvided: Boolean = false,
    val systemConfigurationCompleted: Boolean = false,
    val qualityAssuranceCompleted: Boolean = false,
    val installationStatus: InstallationStatus,
    val clinicalNotes: String?,
    val followUpScheduled: Boolean = false,
    val nextReviewDate: Long
)

@Entity(tableName = "clinical_oversight")
data class ClinicalOversight(
    @PrimaryKey
    val oversightId: String,
    val userId: String,
    val primaryPhysicianId: String,
    val specialistIds: List<String> = emptyList(),
    val institutionId: String?,
    val oversightLevel: ClinicalOversightLevel,
    val establishmentTimestamp: Long,
    val clinicalProtocols: List<String>,
    val monitoringFrequency: MonitoringFrequency,
    val alertThresholds: Map<String, Double>,
    val escalationProcedures: List<String>,
    val qualityMetrics: List<String>,
    val patientSafetyMeasures: List<String>,
    val clinicalGovernance: String,
    val isActive: Boolean = true,
    val lastReviewDate: Long,
    val nextReviewDate: Long
)

@Entity(tableName = "clinical_assessments")
data class ClinicalAssessment(
    @PrimaryKey
    val assessmentId: String,
    val userId: String,
    val assessingPhysicianId: String,
    val assessmentTimestamp: Long,
    val assessmentType: ClinicalAssessmentType,
    val cognitiveAssessment: CognitiveAssessment,
    val functionalAssessment: FunctionalAssessment,
    val socialAssessment: SocialAssessment,
    val riskFactorAssessment: RiskFactorAssessment,
    val abuseRiskLevel: AbuseRiskLevel,
    val protectionRecommendations: List<String>,
    val monitoringRecommendations: List<String>,
    val caregiverAssessment: CaregiverAssessment?,
    val familyDynamicsAssessment: FamilyDynamicsAssessment?,
    val elderRightsAdvocateRecommended: Boolean = false,
    val followUpRequired: Boolean = true,
    val nextAssessmentDate: Long,
    val clinicalNotes: String?,
    val assessmentValid: Boolean = true
)

// ==================== SUPPORTING DATA CLASSES ====================

data class HealthcareProfessionalDetails(
    val professionalId: String,
    val firstName: String,
    val lastName: String,
    val professionalType: ProfessionalType,
    val specializations: List<String>,
    val licenseNumbers: List<String>,
    val contactInformation: ProfessionalContactInfo,
    val yearsOfExperience: Int,
    val elderCareExperience: Int
)

data class ProfessionalCredentials(
    val professionalType: ProfessionalType,
    val licenseNumbers: List<String>,
    val boardCertifications: List<String>,
    val specializations: List<String>,
    val institutionalAffiliations: List<String>,
    val malpracticeInsurance: InsuranceRecord,
    val backgroundCheckStatus: BackgroundCheckStatus,
    val continuingEducationStatus: ContinuingEducationStatus,
    val credentialVerificationDate: Long,
    val credentialExpirationDate: Long,
    val isValid: Boolean = true
)

data class HealthcareInstitution(
    val institutionId: String,
    val institutionName: String,
    val institutionType: InstitutionType,
    val address: String,
    val contactInformation: String,
    val accreditation: List<String>,
    val specializations: List<String>,
    val elderCareServices: List<String>,
    val isActive: Boolean = true
)

data class ConsentRecord(
    val consentId: String,
    val consentType: ConsentType,
    val consentTimestamp: Long,
    val consentMethod: ConsentMethod,
    val witnessId: String? = null,
    val legalGuardianConsent: Boolean = false,
    val consentText: String,
    val isValid: Boolean = true
)

data class CognitiveAssessment(
    val assessmentTool: String,
    val score: Double,
    val interpretation: String,
    val cognitiveImpairmentLevel: CognitiveImpairmentLevel,
    val decisionMakingCapacity: DecisionMakingCapacity
)

data class FunctionalAssessment(
    val activitiesOfDailyLiving: Map<String, FunctionalLevel>,
    val instrumentalActivitiesOfDailyLiving: Map<String, FunctionalLevel>,
    val mobilityStatus: MobilityStatus,
    val fallRisk: FallRiskLevel
)

data class SocialAssessment(
    val socialSupport: SocialSupportLevel,
    val socialIsolation: SocialIsolationLevel,
    val familyDynamics: String,
    val caregiverRelationships: String,
    val communityResources: List<String>
)

data class RiskFactorAssessment(
    val abuseRiskFactors: List<String>,
    val neglectRiskFactors: List<String>,
    val exploitationRiskFactors: List<String>,
    val overallRiskLevel: OverallRiskLevel,
    val protectiveFactors: List<String>
)

data class CaregiverAssessment(
    val caregiverType: CaregiverType,
    val caregiverCapacity: CaregiverCapacity,
    val caregiverStress: CaregiverStressLevel,
    val caregiverKnowledge: CaregiverKnowledgeLevel,
    val caregiverSupport: CaregiverSupportLevel
)

data class FamilyDynamicsAssessment(
    val familyStructure: String,
    val familyRelationships: String,
    val communicationPatterns: String,
    val conflictResolution: String,
    val familySupport: FamilySupportLevel
)

data class ProfessionalContactInfo(
    val primaryPhone: String,
    val email: String,
    val officeAddress: String,
    val emergencyContact: String?
)

data class InsuranceRecord(
    val insuranceProvider: String,
    val policyNumber: String,
    val coverageAmount: Double,
    val effectiveDate: Long,
    val expirationDate: Long,
    val isActive: Boolean = true
)

// ==================== RESULT CLASSES ====================

data class ProfessionalRegistrationResult(
    val registrationId: String?,
    val success: Boolean,
    val message: String,
    val verificationRequired: Boolean = false,
    val trainingRequired: Boolean = false,
    val requiresAdditionalDocumentation: Boolean = false,
    val nextSteps: List<String> = emptyList(),
    val estimatedCompletionTime: Long? = null,
    val error: String? = null
)

data class ProfessionalInstallationResult(
    val installationId: String?,
    val success: Boolean,
    val message: String,
    val completedSteps: List<String> = emptyList(),
    val pendingSteps: List<String> = emptyList(),
    val followUpRequired: Boolean = false,
    val authorizationRequired: Boolean = false,
    val nextReviewDate: Long? = null,
    val error: String? = null
)

data class ClinicalOversightResult(
    val oversightId: String?,
    val success: Boolean,
    val message: String,
    val oversightLevel: ClinicalOversightLevel? = null,
    val monitoringFrequency: MonitoringFrequency? = null,
    val nextReviewDate: Long? = null,
    val error: String? = null
)

data class ClinicalAssessmentResult(
    val assessmentId: String?,
    val success: Boolean,
    val message: String,
    val abuseRiskLevel: AbuseRiskLevel? = null,
    val protectionRecommendations: List<String> = emptyList(),
    val monitoringRecommendations: List<String> = emptyList(),
    val elderRightsAdvocateRecommended: Boolean = false,
    val followUpRequired: Boolean = false,
    val nextAssessmentDate: Long? = null,
    val error: String? = null
)

// ==================== ENUMS ====================

enum class ProfessionalType {
    PHYSICIAN, NURSE_PRACTITIONER, PHYSICIAN_ASSISTANT, REGISTERED_NURSE,
    SOCIAL_WORKER, GERIATRICIAN, PSYCHIATRIST, PSYCHOLOGIST, CASE_MANAGER, ELDER_RIGHTS_ADVOCATE
}

enum class ProfessionalRegistrationStatus {
    PENDING_VERIFICATION, VERIFICATION_IN_PROGRESS, TRAINING_REQUIRED, TRAINING_IN_PROGRESS,
    CERTIFICATION_PENDING, ACTIVE, SUSPENDED, REVOKED, EXPIRED
}

enum class CertificationStatus {
    PENDING, IN_PROGRESS, CERTIFIED, RENEWAL_REQUIRED, EXPIRED, REVOKED
}

enum class ClinicalOversightLevel {
    BASIC, STANDARD, ENHANCED, INTENSIVE, PHYSICIAN, SPECIALIST, ADVANCED_PRACTICE
}

enum class InstallationType {
    INITIAL_INSTALLATION, UPGRADE_INSTALLATION, REPLACEMENT_INSTALLATION,
    EMERGENCY_INSTALLATION, PILOT_INSTALLATION, RESEARCH_INSTALLATION
}

enum class ClinicalContext {
    HOME_CARE, ASSISTED_LIVING, NURSING_HOME, HOSPITAL_DISCHARGE,
    OUTPATIENT_CLINIC, REHABILITATION_FACILITY, HOSPICE_CARE, INDEPENDENT_LIVING
}

enum class InstallationStatus {
    SCHEDULED, IN_PROGRESS, COMPLETED, PARTIAL, FAILED, CANCELLED, RESCHEDULED
}

enum class MonitoringFrequency {
    CONTINUOUS, HOURLY, DAILY, WEEKLY, MONTHLY, QUARTERLY, AS_NEEDED
}

enum class ClinicalAssessmentType {
    INITIAL_ASSESSMENT, FOLLOW_UP_ASSESSMENT, ANNUAL_ASSESSMENT,
    INCIDENT_ASSESSMENT, DISCHARGE_ASSESSMENT, EMERGENCY_ASSESSMENT
}

enum class AbuseRiskLevel { LOW, MODERATE, HIGH, CRITICAL }

enum class InstitutionType {
    HOSPITAL, CLINIC, NURSING_HOME, ASSISTED_LIVING, HOME_HEALTH_AGENCY,
    REHABILITATION_CENTER, HOSPICE, COMMUNITY_HEALTH_CENTER, ACADEMIC_MEDICAL_CENTER, SPECIALTY_PRACTICE
}

enum class BackgroundCheckStatus { PENDING, IN_PROGRESS, CLEARED, FLAGGED, FAILED }

enum class ContinuingEducationStatus { CURRENT, DEFICIENT, EXPIRED, PENDING_VERIFICATION }

enum class ConsentType {
    TREATMENT_CONSENT, MONITORING_CONSENT, DATA_SHARING_CONSENT, RESEARCH_CONSENT, EMERGENCY_CONSENT
}

enum class ConsentMethod {
    WRITTEN_SIGNATURE, ELECTRONIC_SIGNATURE, VERBAL_CONSENT, IMPLIED_CONSENT, GUARDIAN_CONSENT
}

enum class CognitiveImpairmentLevel { NONE, MILD, MODERATE, SEVERE, UNKNOWN }

enum class DecisionMakingCapacity {
    FULL_CAPACITY, PARTIAL_CAPACITY, LIMITED_CAPACITY, NO_CAPACITY, FLUCTUATING_CAPACITY
}

enum class FunctionalLevel { INDEPENDENT, REQUIRES_ASSISTANCE, DEPENDENT, UNABLE_TO_PERFORM }

enum class MobilityStatus { FULLY_MOBILE, USES_ASSISTIVE_DEVICE, WHEELCHAIR_BOUND, BEDBOUND, VARIABLE }

enum class FallRiskLevel { LOW, MODERATE, HIGH, VERY_HIGH }

enum class SocialSupportLevel { STRONG, MODERATE, WEAK, ABSENT }

enum class SocialIsolationLevel { NONE, MILD, MODERATE, SEVERE }

enum class OverallRiskLevel { LOW, MODERATE, HIGH, CRITICAL }

enum class CaregiverType {
    FAMILY_MEMBER, FRIEND, PAID_CAREGIVER, PROFESSIONAL_CAREGIVER, INSTITUTIONAL_STAFF
}

enum class CaregiverCapacity { EXCELLENT, GOOD, ADEQUATE, POOR, OVERWHELMED }

enum class CaregiverStressLevel { LOW, MODERATE, HIGH, SEVERE }

enum class CaregiverKnowledgeLevel { EXCELLENT, GOOD, ADEQUATE, POOR, MINIMAL }

enum class CaregiverSupportLevel { EXCELLENT, GOOD, ADEQUATE, POOR, ABSENT }

enum class FamilySupportLevel { STRONG, MODERATE, WEAK, CONFLICTED, ABSENT }

// ==================== TYPE CONVERTERS ====================

class HealthcareProfessionalConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromHealthcareProfessionalDetails(details: HealthcareProfessionalDetails): String = gson.toJson(details)

    @TypeConverter
    fun toHealthcareProfessionalDetails(detailsString: String): HealthcareProfessionalDetails =
        gson.fromJson(detailsString, HealthcareProfessionalDetails::class.java)

    @TypeConverter
    fun fromProfessionalCredentials(credentials: ProfessionalCredentials): String = gson.toJson(credentials)

    @TypeConverter
    fun toProfessionalCredentials(credentialsString: String): ProfessionalCredentials =
        gson.fromJson(credentialsString, ProfessionalCredentials::class.java)

    @TypeConverter
    fun fromHealthcareInstitution(institution: HealthcareInstitution?): String? = gson.toJson(institution)

    @TypeConverter
    fun toHealthcareInstitution(institutionString: String?): HealthcareInstitution? =
        institutionString?.let { gson.fromJson(it, HealthcareInstitution::class.java) }

    @TypeConverter
    fun fromConsentRecord(consent: ConsentRecord): String = gson.toJson(consent)

    @TypeConverter
    fun toConsentRecord(consentString: String): ConsentRecord =
        gson.fromJson(consentString, ConsentRecord::class.java)

    @TypeConverter
    fun fromConsentRecordNullable(consent: ConsentRecord?): String? = gson.toJson(consent)

    @TypeConverter
    fun toConsentRecordNullable(consentString: String?): ConsentRecord? =
        consentString?.let { gson.fromJson(it, ConsentRecord::class.java) }

    @TypeConverter
    fun fromStringList(strings: List<String>): String = gson.toJson(strings)

    @TypeConverter
    fun toStringList(stringsString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringsString, listType)
    }

    @TypeConverter
    fun fromStringDoubleMap(map: Map<String, Double>): String = gson.toJson(map)

    @TypeConverter
    fun toStringDoubleMap(mapString: String): Map<String, Double> {
        val mapType = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(mapString, mapType)
    }

    @TypeConverter
    fun fromCognitiveAssessment(assessment: CognitiveAssessment): String = gson.toJson(assessment)

    @TypeConverter
    fun toCognitiveAssessment(assessmentString: String): CognitiveAssessment =
        gson.fromJson(assessmentString, CognitiveAssessment::class.java)

    @TypeConverter
    fun fromFunctionalAssessment(assessment: FunctionalAssessment): String = gson.toJson(assessment)

    @TypeConverter
    fun toFunctionalAssessment(assessmentString: String): FunctionalAssessment =
        gson.fromJson(assessmentString, FunctionalAssessment::class.java)

    @TypeConverter
    fun fromSocialAssessment(assessment: SocialAssessment): String = gson.toJson(assessment)

    @TypeConverter
    fun toSocialAssessment(assessmentString: String): SocialAssessment =
        gson.fromJson(assessmentString, SocialAssessment::class.java)

    @TypeConverter
    fun fromRiskFactorAssessment(assessment: RiskFactorAssessment): String = gson.toJson(assessment)

    @TypeConverter
    fun toRiskFactorAssessment(assessmentString: String): RiskFactorAssessment =
        gson.fromJson(assessmentString, RiskFactorAssessment::class.java)

    @TypeConverter
    fun fromCaregiverAssessment(assessment: CaregiverAssessment?): String? = gson.toJson(assessment)

    @TypeConverter
    fun toCaregiverAssessment(assessmentString: String?): CaregiverAssessment? =
        assessmentString?.let { gson.fromJson(it, CaregiverAssessment::class.java) }

    @TypeConverter
    fun fromFamilyDynamicsAssessment(assessment: FamilyDynamicsAssessment?): String? = gson.toJson(assessment)

    @TypeConverter
    fun toFamilyDynamicsAssessment(assessmentString: String?): FamilyDynamicsAssessment? =
        assessmentString?.let { gson.fromJson(it, FamilyDynamicsAssessment::class.java) }
}
