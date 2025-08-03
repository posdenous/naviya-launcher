package com.naviya.launcher.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Healthcare Professional Data Classes (used in converters)
import com.naviya.launcher.healthcare.data.ConsentRecord
import com.naviya.launcher.healthcare.data.HealthcareProfessionalDetails
import com.naviya.launcher.healthcare.data.ProfessionalCredentials
import com.naviya.launcher.healthcare.data.HealthcareInstitution
import com.naviya.launcher.healthcare.data.CognitiveAssessment
import com.naviya.launcher.healthcare.data.FunctionalAssessment
import com.naviya.launcher.healthcare.data.SocialAssessment
import com.naviya.launcher.healthcare.data.RiskFactorAssessment
import com.naviya.launcher.healthcare.data.CaregiverAssessment
import com.naviya.launcher.healthcare.data.FamilyDynamicsAssessment
import com.naviya.launcher.healthcare.data.ProfessionalContactInfo
import com.naviya.launcher.healthcare.data.InsuranceRecord
import com.naviya.launcher.healthcare.data.ProfessionalRegistrationResult
import com.naviya.launcher.healthcare.data.ProfessionalInstallationResult
import com.naviya.launcher.healthcare.data.ClinicalOversightResult
import com.naviya.launcher.healthcare.data.ClinicalAssessment
import com.naviya.launcher.healthcare.data.ClinicalAssessmentResult

// Healthcare Professional Enums (used in converters)
import com.naviya.launcher.healthcare.data.ProfessionalType
import com.naviya.launcher.healthcare.data.ProfessionalRegistrationStatus
import com.naviya.launcher.healthcare.data.CertificationStatus
import com.naviya.launcher.healthcare.data.ClinicalOversightLevel
import com.naviya.launcher.healthcare.data.InstallationType
import com.naviya.launcher.healthcare.data.ClinicalContext
import com.naviya.launcher.healthcare.data.InstallationStatus
import com.naviya.launcher.healthcare.data.MonitoringFrequency
import com.naviya.launcher.healthcare.data.ClinicalAssessmentType
import com.naviya.launcher.healthcare.data.AbuseRiskLevel
import com.naviya.launcher.healthcare.data.InstitutionType
import com.naviya.launcher.healthcare.data.BackgroundCheckStatus
import com.naviya.launcher.healthcare.data.ConsentMethod

// Healthcare Compliance Data Classes (used in converters)
import com.naviya.launcher.healthcare.compliance.GovernanceFinding
import com.naviya.launcher.healthcare.compliance.VulnerabilityFactor
import com.naviya.launcher.healthcare.compliance.ProtectionMeasure
import com.naviya.launcher.healthcare.compliance.SafeguardingPlan
import com.naviya.launcher.healthcare.compliance.ComplianceRequirement
import com.naviya.launcher.healthcare.compliance.RegulatoryViolation
import com.naviya.launcher.healthcare.compliance.CorrectionPlan
import com.naviya.launcher.healthcare.compliance.ComplianceAlert
import com.naviya.launcher.healthcare.compliance.HipaaComplianceLog
import com.naviya.launcher.healthcare.compliance.PatientConsentRecord
import com.naviya.launcher.healthcare.compliance.ClinicalGovernanceAudit
import com.naviya.launcher.healthcare.compliance.ElderProtectionAssessment
import com.naviya.launcher.healthcare.compliance.RegulatoryComplianceCheck
import com.naviya.launcher.healthcare.compliance.CorrectionStep
import com.naviya.launcher.healthcare.compliance.GovernanceFindingCategory
import com.naviya.launcher.healthcare.compliance.VulnerabilityFactorType
import com.naviya.launcher.healthcare.compliance.VulnerabilitySeverity
import com.naviya.launcher.healthcare.compliance.ProtectionMeasureType
import com.naviya.launcher.healthcare.compliance.RegulatoryFramework
import com.naviya.launcher.healthcare.compliance.ComplianceStatus
import com.naviya.launcher.healthcare.compliance.ComplianceCategory
import com.naviya.launcher.healthcare.compliance.VerificationMethod
import com.naviya.launcher.healthcare.compliance.RegulatoryViolationType
import com.naviya.launcher.healthcare.compliance.PenaltyRisk
import com.naviya.launcher.healthcare.compliance.ProfessionalTraining
import com.naviya.launcher.healthcare.compliance.ProfessionalCertification
import com.naviya.launcher.healthcare.compliance.ProfessionalOversight
import com.naviya.launcher.healthcare.compliance.InstitutionAffiliation

// Elder Rights Data Classes
import com.naviya.launcher.elderrights.data.ElderRightsAdvocateContact
import com.naviya.launcher.elderrights.data.ChannelResult
import com.naviya.launcher.elderrights.data.EscalationTrigger
import com.naviya.launcher.elderrights.data.EscalationStep
import com.naviya.launcher.elderrights.data.ContactResponse
import com.naviya.launcher.elderrights.data.NotificationChannel

// Caregiver Data Classes
import com.naviya.launcher.caregiver.data.CaregiverPermission
import com.naviya.launcher.caregiver.data.OfflineCapability
import com.naviya.launcher.caregiver.data.CaregiverSyncPreferences
import com.naviya.launcher.caregiver.data.EmergencyAlertPreferences
import com.naviya.launcher.caregiver.data.EmergencyChannelResult
import com.naviya.launcher.caregiver.data.SyncCategory
import com.naviya.launcher.caregiver.data.EmergencyEventType
import com.naviya.launcher.caregiver.data.EmergencyAlertPriority

// Compliance Data Classes
import com.naviya.launcher.compliance.data.ProcessingPurpose
import com.naviya.launcher.compliance.data.PersonalDataCategory
import com.naviya.launcher.compliance.data.PrivacyPrinciple
import com.naviya.launcher.compliance.data.PrivacyComplianceLevel
import com.naviya.launcher.compliance.data.ConsentMetrics
import com.naviya.launcher.compliance.data.DataSubjectRequestMetrics
import com.naviya.launcher.compliance.data.DataBreachMetrics
import com.naviya.launcher.compliance.data.PrivacyByDesignAssessment
import com.naviya.launcher.compliance.data.DataSubjectCategory
import com.naviya.launcher.compliance.data.ThirdCountryTransfer
import com.naviya.launcher.compliance.data.PrivacyRisk
import com.naviya.launcher.compliance.data.RegulatoryAuthority
import com.naviya.launcher.compliance.data.MarketApprovalStatus
import com.naviya.launcher.compliance.data.ConsentType
import com.naviya.launcher.compliance.data.LawfulBasis
import com.naviya.launcher.compliance.data.DataSubjectRequestType
import com.naviya.launcher.compliance.data.DataSubjectRequestStatus
import com.naviya.launcher.compliance.data.DataExportFormat
import com.naviya.launcher.compliance.data.DataDeliveryMethod
import com.naviya.launcher.compliance.data.ErasureReason
import com.naviya.launcher.compliance.data.DataRetentionAction
import com.naviya.launcher.compliance.data.DataBreachType
import com.naviya.launcher.compliance.data.BreachRiskLevel
import com.naviya.launcher.compliance.data.ComplianceReportType
import com.naviya.launcher.compliance.data.ProcessingRiskLevel
import com.naviya.launcher.compliance.data.ConsentEventType
import com.naviya.launcher.compliance.data.ResponseMethod
import com.naviya.launcher.compliance.data.RiskLikelihood
import com.naviya.launcher.compliance.data.RiskImpact
import com.naviya.launcher.emergency.MedicalEmergencyType
import com.naviya.launcher.onboarding.data.EmergencyContact
import com.naviya.launcher.onboarding.data.OnboardingState
import com.naviya.launcher.toggle.ToggleMode
import com.naviya.launcher.healthcare.compliance.MedicalComplianceResult
import com.naviya.launcher.healthcare.compliance.ComplianceReport

class SharedTypeConverters {
    private val gson = Gson()
    
    // String List converters (used by both healthcare professional and medical compliance)
    @TypeConverter
    fun fromStringList(strings: List<String>): String = gson.toJson(strings)
    
    @TypeConverter
    fun toStringList(stringsString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringsString, listType) ?: emptyList()
    }
    
    // Consent Record converters (used by healthcare professional)
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
    
    // Medical Compliance specific converters
    @TypeConverter

    
    @TypeConverter

        val listType = object : TypeToken<List<GovernanceFinding>>() {}.type
        return gson.fromJson(findingsString, listType) ?: emptyList()
    }
    
    @TypeConverter

    
    @TypeConverter

        val listType = object : TypeToken<List<VulnerabilityFactor>>() {}.type
        return gson.fromJson(factorsString, listType) ?: emptyList()
    }
    
    @TypeConverter

    
    @TypeConverter

        val listType = object : TypeToken<List<ProtectionMeasure>>() {}.type
        return gson.fromJson(measuresString, listType) ?: emptyList()
    }
    
    @TypeConverter

    
    @TypeConverter

        gson.fromJson(planString, SafeguardingPlan::class.java)
    
    @TypeConverter
    fun fromComplianceRequirementList(requirements: List<ComplianceRequirement>): String = gson.toJson(requirements)
    
    @TypeConverter
    fun toComplianceRequirementList(requirementsString: String): List<ComplianceRequirement> {
        val listType = object : TypeToken<List<ComplianceRequirement>>() {}.type
        return gson.fromJson(requirementsString, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromRegulatoryViolationList(violations: List<RegulatoryViolation>): String = gson.toJson(violations)
    
    @TypeConverter
    fun toRegulatoryViolationList(violationsString: String): List<RegulatoryViolation> {
        val listType = object : TypeToken<List<RegulatoryViolation>>() {}.type
        return gson.fromJson(violationsString, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromCorrectionPlan(plan: CorrectionPlan?): String? = gson.toJson(plan)
    
    @TypeConverter
    fun toCorrectionPlan(planString: String?): CorrectionPlan? =
        planString?.let { gson.fromJson(it, CorrectionPlan::class.java) }
    
    @TypeConverter
    fun fromMedicalComplianceResult(result: MedicalComplianceResult): String = gson.toJson(result)
    
    @TypeConverter
    fun toMedicalComplianceResult(resultString: String): MedicalComplianceResult =
        gson.fromJson(resultString, MedicalComplianceResult::class.java)
    
    @TypeConverter
    fun fromComplianceReport(report: ComplianceReport): String = gson.toJson(report)
    
    @TypeConverter
    fun toComplianceReport(reportString: String): ComplianceReport =
        gson.fromJson(reportString, ComplianceReport::class.java)
    
    @TypeConverter
    fun fromComplianceAlert(alert: ComplianceAlert): String = gson.toJson(alert)
    
    @TypeConverter
    fun toComplianceAlert(alertString: String): ComplianceAlert =
        gson.fromJson(alertString, ComplianceAlert::class.java)
    
    @TypeConverter
    fun fromHipaaComplianceLog(log: HipaaComplianceLog): String = gson.toJson(log)
    
    @TypeConverter
    fun toHipaaComplianceLog(logString: String): HipaaComplianceLog =
        gson.fromJson(logString, HipaaComplianceLog::class.java)
    
    @TypeConverter
    fun fromPatientConsentRecord(record: PatientConsentRecord): String = gson.toJson(record)
    
    @TypeConverter
    fun toPatientConsentRecord(recordString: String): PatientConsentRecord =
        gson.fromJson(recordString, PatientConsentRecord::class.java)
    
    @TypeConverter
    fun fromClinicalGovernanceAudit(audit: ClinicalGovernanceAudit): String = gson.toJson(audit)
    
    @TypeConverter
    fun toClinicalGovernanceAudit(auditString: String): ClinicalGovernanceAudit =
        gson.fromJson(auditString, ClinicalGovernanceAudit::class.java)
    
    @TypeConverter
    fun fromElderProtectionAssessment(assessment: ElderProtectionAssessment): String = gson.toJson(assessment)
    
    @TypeConverter
    fun toElderProtectionAssessment(assessmentString: String): ElderProtectionAssessment =
        gson.fromJson(assessmentString, ElderProtectionAssessment::class.java)
    
    @TypeConverter
    fun fromRegulatoryComplianceCheck(check: RegulatoryComplianceCheck): String = gson.toJson(check)
    
    @TypeConverter
    fun toRegulatoryComplianceCheck(checkString: String): RegulatoryComplianceCheck =
        gson.fromJson(checkString, RegulatoryComplianceCheck::class.java)
    
    @TypeConverter
    fun fromCorrectionStep(step: CorrectionStep): String = gson.toJson(step)
    
    @TypeConverter
    fun toCorrectionStep(stepString: String): CorrectionStep =
        gson.fromJson(stepString, CorrectionStep::class.java)
    
    // Healthcare Professional specific converters
    @TypeConverter
    fun fromProfessionalCredentials(credentials: ProfessionalCredentials): String = gson.toJson(credentials)
    
    @TypeConverter
    fun toProfessionalCredentials(credentialsString: String): ProfessionalCredentials =
        gson.fromJson(credentialsString, ProfessionalCredentials::class.java)
    
    @TypeConverter
    fun fromInstitutionAffiliation(affiliation: InstitutionAffiliation?): String? = gson.toJson(affiliation)
    
    @TypeConverter
    fun toInstitutionAffiliation(affiliationString: String?): InstitutionAffiliation? =
        affiliationString?.let { gson.fromJson(it, InstitutionAffiliation::class.java) }
    
    @TypeConverter
    fun fromProfessionalTraining(training: ProfessionalTraining): String = gson.toJson(training)
    
    @TypeConverter
    fun toProfessionalTraining(trainingString: String): ProfessionalTraining =
        gson.fromJson(trainingString, ProfessionalTraining::class.java)
    
    @TypeConverter
    fun fromProfessionalCertification(certification: ProfessionalCertification?): String? = gson.toJson(certification)
    
    @TypeConverter
    fun toProfessionalCertification(certificationString: String?): ProfessionalCertification? =
        certificationString?.let { gson.fromJson(it, ProfessionalCertification::class.java) }
    
    @TypeConverter
    fun fromProfessionalOversight(oversight: ProfessionalOversight): String = gson.toJson(oversight)
    
    @TypeConverter
    fun toProfessionalOversight(oversightString: String): ProfessionalOversight =
        gson.fromJson(oversightString, ProfessionalOversight::class.java)
    
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
    
    // Healthcare Professional Details converter
    @TypeConverter
    fun fromHealthcareProfessionalDetails(details: HealthcareProfessionalDetails): String = gson.toJson(details)
    
    @TypeConverter
    fun toHealthcareProfessionalDetails(detailsString: String): HealthcareProfessionalDetails =
        gson.fromJson(detailsString, HealthcareProfessionalDetails::class.java)
    
    // Healthcare Institution converter
    @TypeConverter
    fun fromHealthcareInstitution(institution: HealthcareInstitution): String = gson.toJson(institution)
    
    @TypeConverter
    fun toHealthcareInstitution(institutionString: String): HealthcareInstitution =
        gson.fromJson(institutionString, HealthcareInstitution::class.java)
    
    // Professional Contact Info converter
    @TypeConverter
    fun fromProfessionalContactInfo(contactInfo: ProfessionalContactInfo): String = gson.toJson(contactInfo)
    
    @TypeConverter
    fun toProfessionalContactInfo(contactInfoString: String): ProfessionalContactInfo =
        gson.fromJson(contactInfoString, ProfessionalContactInfo::class.java)
    
    // Insurance Record converter
    @TypeConverter
    fun fromInsuranceRecord(insuranceRecord: InsuranceRecord): String = gson.toJson(insuranceRecord)
    
    @TypeConverter
    fun toInsuranceRecord(insuranceRecordString: String): InsuranceRecord =
        gson.fromJson(insuranceRecordString, InsuranceRecord::class.java)
    
    // Professional Registration Result converter
    @TypeConverter
    fun fromProfessionalRegistrationResult(result: ProfessionalRegistrationResult): String = gson.toJson(result)
    
    @TypeConverter
    fun toProfessionalRegistrationResult(resultString: String): ProfessionalRegistrationResult =
        gson.fromJson(resultString, ProfessionalRegistrationResult::class.java)
    
    // Professional Installation Result converter
    @TypeConverter
    fun fromProfessionalInstallationResult(result: ProfessionalInstallationResult): String = gson.toJson(result)
    
    @TypeConverter
    fun toProfessionalInstallationResult(resultString: String): ProfessionalInstallationResult =
        gson.fromJson(resultString, ProfessionalInstallationResult::class.java)
    
    // Clinical Oversight Result converter
    @TypeConverter
    fun fromClinicalOversightResult(result: ClinicalOversightResult): String = gson.toJson(result)
    
    @TypeConverter
    fun toClinicalOversightResult(resultString: String): ClinicalOversightResult =
        gson.fromJson(resultString, ClinicalOversightResult::class.java)
    
    // Cognitive Assessment converter
    @TypeConverter
    fun fromCognitiveAssessment(assessment: CognitiveAssessment): String = gson.toJson(assessment)
    
    @TypeConverter
    fun toCognitiveAssessment(assessmentString: String): CognitiveAssessment =
        gson.fromJson(assessmentString, CognitiveAssessment::class.java)
    
    // Clinical Assessment converter
    @TypeConverter
    fun fromClinicalAssessment(assessment: ClinicalAssessment): String = gson.toJson(assessment)
    
    @TypeConverter
    fun toClinicalAssessment(assessmentString: String): ClinicalAssessment =
        gson.fromJson(assessmentString, ClinicalAssessment::class.java)
    
    // Enum converters for Healthcare Professional
    @TypeConverter
    fun fromProfessionalType(type: ProfessionalType): String = type.name
    
    @TypeConverter
    fun toProfessionalType(typeString: String): ProfessionalType = ProfessionalType.valueOf(typeString)
    
    @TypeConverter
    fun fromProfessionalRegistrationStatus(status: ProfessionalRegistrationStatus): String = status.name
    
    @TypeConverter
    fun toProfessionalRegistrationStatus(statusString: String): ProfessionalRegistrationStatus = ProfessionalRegistrationStatus.valueOf(statusString)
    
    @TypeConverter
    fun fromCertificationStatus(status: CertificationStatus): String = status.name
    
    @TypeConverter
    fun toCertificationStatus(statusString: String): CertificationStatus = CertificationStatus.valueOf(statusString)
    
    @TypeConverter
    fun fromClinicalOversightLevel(level: ClinicalOversightLevel): String = level.name
    
    @TypeConverter
    fun toClinicalOversightLevel(levelString: String): ClinicalOversightLevel = ClinicalOversightLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromInstallationType(type: InstallationType): String = type.name
    
    @TypeConverter
    fun toInstallationType(typeString: String): InstallationType = InstallationType.valueOf(typeString)
    
    @TypeConverter
    fun fromClinicalContext(context: ClinicalContext): String = context.name
    
    @TypeConverter
    fun toClinicalContext(contextString: String): ClinicalContext = ClinicalContext.valueOf(contextString)
    
    @TypeConverter
    fun fromInstallationStatus(status: InstallationStatus): String = status.name
    
    @TypeConverter
    fun toInstallationStatus(statusString: String): InstallationStatus = InstallationStatus.valueOf(statusString)
    
    @TypeConverter
    fun fromMonitoringFrequency(frequency: MonitoringFrequency): String = frequency.name
    
    @TypeConverter
    fun toMonitoringFrequency(frequencyString: String): MonitoringFrequency = MonitoringFrequency.valueOf(frequencyString)
    
    @TypeConverter
    fun fromClinicalAssessmentType(type: ClinicalAssessmentType): String = type.name
    
    @TypeConverter
    fun toClinicalAssessmentType(typeString: String): ClinicalAssessmentType = ClinicalAssessmentType.valueOf(typeString)
    
    @TypeConverter
    fun fromAbuseRiskLevel(level: AbuseRiskLevel): String = level.name
    
    @TypeConverter
    fun toAbuseRiskLevel(levelString: String): AbuseRiskLevel = AbuseRiskLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromInstitutionType(type: InstitutionType): String = type.name
    
    @TypeConverter
    fun toInstitutionType(typeString: String): InstitutionType = InstitutionType.valueOf(typeString)
    
    @TypeConverter
    fun fromBackgroundCheckStatus(status: BackgroundCheckStatus): String = status.name
    
    @TypeConverter
    fun toBackgroundCheckStatus(statusString: String): BackgroundCheckStatus = BackgroundCheckStatus.valueOf(statusString)
    
    @TypeConverter
    fun fromContinuingEducationStatus(status: ContinuingEducationStatus): String = status.name
    
    @TypeConverter
    fun toContinuingEducationStatus(statusString: String): ContinuingEducationStatus = ContinuingEducationStatus.valueOf(statusString)
    
    @TypeConverter
    fun fromConsentType(type: ConsentType): String = type.name
    
    @TypeConverter
    fun toConsentType(typeString: String): ConsentType = ConsentType.valueOf(typeString)
    
    @TypeConverter
    fun fromConsentMethod(method: ConsentMethod): String = method.name
    
    @TypeConverter
    fun toConsentMethod(methodString: String): ConsentMethod = ConsentMethod.valueOf(methodString)
    
    @TypeConverter
    fun fromCognitiveImpairmentLevel(level: CognitiveImpairmentLevel): String = level.name
    
    @TypeConverter
    fun toCognitiveImpairmentLevel(levelString: String): CognitiveImpairmentLevel = CognitiveImpairmentLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromDecisionMakingCapacity(capacity: DecisionMakingCapacity): String = capacity.name
    
    @TypeConverter
    fun toDecisionMakingCapacity(capacityString: String): DecisionMakingCapacity = DecisionMakingCapacity.valueOf(capacityString)
    
    @TypeConverter
    fun fromFunctionalLevel(level: FunctionalLevel): String = level.name
    
    @TypeConverter
    fun toFunctionalLevel(levelString: String): FunctionalLevel = FunctionalLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromMobilityStatus(status: MobilityStatus): String = status.name
    
    @TypeConverter
    fun toMobilityStatus(statusString: String): MobilityStatus = MobilityStatus.valueOf(statusString)
    
    @TypeConverter
    fun fromFallRiskLevel(level: FallRiskLevel): String = level.name
    
    @TypeConverter
    fun toFallRiskLevel(levelString: String): FallRiskLevel = FallRiskLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromSocialSupportLevel(level: SocialSupportLevel): String = level.name
    
    @TypeConverter
    fun toSocialSupportLevel(levelString: String): SocialSupportLevel = SocialSupportLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromSocialIsolationLevel(level: SocialIsolationLevel): String = level.name
    
    @TypeConverter
    fun toSocialIsolationLevel(levelString: String): SocialIsolationLevel = SocialIsolationLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromOverallRiskLevel(level: OverallRiskLevel): String = level.name
    
    @TypeConverter
    fun toOverallRiskLevel(levelString: String): OverallRiskLevel = OverallRiskLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromCaregiverType(type: CaregiverType): String = type.name
    
    @TypeConverter
    fun toCaregiverType(typeString: String): CaregiverType = CaregiverType.valueOf(typeString)
    
    @TypeConverter
    fun fromCaregiverCapacity(capacity: CaregiverCapacity): String = capacity.name
    
    @TypeConverter
    fun toCaregiverCapacity(capacityString: String): CaregiverCapacity = CaregiverCapacity.valueOf(capacityString)
    
    @TypeConverter
    fun fromCaregiverStressLevel(level: CaregiverStressLevel): String = level.name
    
    @TypeConverter
    fun toCaregiverStressLevel(levelString: String): CaregiverStressLevel = CaregiverStressLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromCaregiverKnowledgeLevel(level: CaregiverKnowledgeLevel): String = level.name
    
    @TypeConverter
    fun toCaregiverKnowledgeLevel(levelString: String): CaregiverKnowledgeLevel = CaregiverKnowledgeLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromCaregiverSupportLevel(level: CaregiverSupportLevel): String = level.name
    
    @TypeConverter
    fun toCaregiverSupportLevel(levelString: String): CaregiverSupportLevel = CaregiverSupportLevel.valueOf(levelString)
    
    @TypeConverter
    fun fromFamilySupportLevel(level: FamilySupportLevel): String = level.name
    
    @TypeConverter
    fun toFamilySupportLevel(levelString: String): FamilySupportLevel = FamilySupportLevel.valueOf(levelString)
    
    // Enum converters for Healthcare Compliance
    @TypeConverter
    fun fromComplianceViolationType(type: ComplianceViolationType): String = type.name
    
    @TypeConverter
    fun toComplianceViolationType(typeString: String): ComplianceViolationType = ComplianceViolationType.valueOf(typeString)
    
    @TypeConverter
    fun fromComplianceSeverity(severity: ComplianceSeverity): String = severity.name
    
    @TypeConverter
    fun toComplianceSeverity(severityString: String): ComplianceSeverity = ComplianceSeverity.valueOf(severityString)
    
    @TypeConverter
    fun fromComplianceAlertType(type: ComplianceAlertType): String = type.name
    
    @TypeConverter
    fun toComplianceAlertType(typeString: String): ComplianceAlertType = ComplianceAlertType.valueOf(typeString)
    
    @TypeConverter
    fun fromHipaaDataAccessType(type: HipaaDataAccessType): String = type.name
    
    @TypeConverter
    fun toHipaaDataAccessType(typeString: String): HipaaDataAccessType = HipaaDataAccessType.valueOf(typeString)
    
    @TypeConverter
    fun fromHipaaAccessMethod(method: HipaaAccessMethod): String = method.name
    
    @TypeConverter
    fun toHipaaAccessMethod(methodString: String): HipaaAccessMethod = HipaaAccessMethod.valueOf(methodString)
    
    @TypeConverter
    fun fromHipaaConsentType(type: HipaaConsentType): String = type.name
    
    @TypeConverter
    fun toHipaaConsentType(typeString: String): HipaaConsentType = HipaaConsentType.valueOf(typeString)
    
    @TypeConverter
    fun fromConsentVerificationMethod(method: ConsentVerificationMethod): String = method.name
    
    @TypeConverter
    fun toConsentVerificationMethod(methodString: String): ConsentVerificationMethod = ConsentVerificationMethod.valueOf(methodString)
    
    @TypeConverter
    fun fromClinicalGovernanceAuditType(type: ClinicalGovernanceAuditType): String = type.name
    
    @TypeConverter
    fun toClinicalGovernanceAuditType(typeString: String): ClinicalGovernanceAuditType = ClinicalGovernanceAuditType.valueOf(typeString)
    
    @TypeConverter
    fun fromClinicalGovernanceStandard(standard: ClinicalGovernanceStandard): String = standard.name
    
    @TypeConverter
    fun toClinicalGovernanceStandard(standardString: String): ClinicalGovernanceStandard = ClinicalGovernanceStandard.valueOf(standardString)
    
    @TypeConverter
    fun fromAuditStatus(status: AuditStatus): String = status.name
    
    @TypeConverter
    fun toAuditStatus(statusString: String): AuditStatus = AuditStatus.valueOf(statusString)
    
    @TypeConverter
    fun fromGovernanceFindingCategory(category: GovernanceFindingCategory): String = category.name
    
    @TypeConverter
    fun toGovernanceFindingCategory(categoryString: String): GovernanceFindingCategory = GovernanceFindingCategory.valueOf(categoryString)
    
    @TypeConverter
    fun fromVulnerabilityFactorType(type: VulnerabilityFactorType): String = type.name
    
    @TypeConverter
    fun toVulnerabilityFactorType(typeString: String): VulnerabilityFactorType = VulnerabilityFactorType.valueOf(typeString)
    
    @TypeConverter
    fun fromVulnerabilitySeverity(severity: VulnerabilitySeverity): String = severity.name
    
    @TypeConverter
    fun toVulnerabilitySeverity(severityString: String): VulnerabilitySeverity = VulnerabilitySeverity.valueOf(severityString)
    
    @TypeConverter
    fun fromProtectionMeasureType(type: ProtectionMeasureType): String = type.name
    
    @TypeConverter
    fun toProtectionMeasureType(typeString: String): ProtectionMeasureType = ProtectionMeasureType.valueOf(typeString)
    
    @TypeConverter
    fun fromRegulatoryFramework(framework: RegulatoryFramework): String = framework.name
    
    @TypeConverter
    fun toRegulatoryFramework(frameworkString: String): RegulatoryFramework = RegulatoryFramework.valueOf(frameworkString)
    
    @TypeConverter
    fun fromComplianceStatus(status: ComplianceStatus): String = status.name
    
    @TypeConverter
    fun toComplianceStatus(statusString: String): ComplianceStatus = ComplianceStatus.valueOf(statusString)
    
    @TypeConverter
    fun fromComplianceCategory(category: ComplianceCategory): String = category.name
    
    @TypeConverter
    fun toComplianceCategory(categoryString: String): ComplianceCategory = ComplianceCategory.valueOf(categoryString)
    
    @TypeConverter
    fun fromVerificationMethod(method: VerificationMethod): String = method.name
    
    @TypeConverter
    fun toVerificationMethod(methodString: String): VerificationMethod = VerificationMethod.valueOf(methodString)
    
    @TypeConverter
    fun fromRegulatoryViolationType(type: RegulatoryViolationType): String = type.name
    
    @TypeConverter
    fun toRegulatoryViolationType(typeString: String): RegulatoryViolationType = RegulatoryViolationType.valueOf(typeString)
    
    @TypeConverter
    fun fromPenaltyRisk(risk: PenaltyRisk): String = risk.name
    
    @TypeConverter
    fun toPenaltyRisk(riskString: String): PenaltyRisk = PenaltyRisk.valueOf(riskString)
}
