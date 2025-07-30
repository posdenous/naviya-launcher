package com.naviya.launcher.healthcare.compliance

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Medical Compliance System
 * Handles HIPAA, GDPR, and clinical governance compliance data operations
 */
@Dao
interface MedicalComplianceDao {

    // ==================== HIPAA COMPLIANCE OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHipaaComplianceLog(log: HipaaComplianceLog)

    @Update
    suspend fun updateHipaaComplianceLog(log: HipaaComplianceLog)

    @Query("SELECT * FROM hipaa_compliance_logs WHERE professionalId = :professionalId ORDER BY accessTimestamp DESC")
    suspend fun getHipaaComplianceLogsByProfessional(professionalId: String): List<HipaaComplianceLog>

    @Query("SELECT * FROM hipaa_compliance_logs WHERE userId = :userId ORDER BY accessTimestamp DESC")
    suspend fun getHipaaComplianceLogsByUser(userId: String): List<HipaaComplianceLog>

    @Query("SELECT * FROM hipaa_compliance_logs WHERE complianceVerified = 0")
    suspend fun getUnverifiedHipaaAccess(): List<HipaaComplianceLog>

    @Query("SELECT * FROM hipaa_compliance_logs WHERE accessTimestamp BETWEEN :startTime AND :endTime")
    suspend fun getHipaaAccessByTimeRange(startTime: Long, endTime: Long): List<HipaaComplianceLog>

    @Query("""
        SELECT dataAccessType, COUNT(*) as accessCount 
        FROM hipaa_compliance_logs 
        WHERE professionalId = :professionalId 
        GROUP BY dataAccessType
    """)
    suspend fun getHipaaAccessStatsByProfessional(professionalId: String): List<HipaaAccessStats>

    @Query("DELETE FROM hipaa_compliance_logs WHERE accessTimestamp < :cutoffTime")
    suspend fun cleanupOldHipaaLogs(cutoffTime: Long)

    // ==================== PATIENT CONSENT OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatientConsentRecord(consent: PatientConsentRecord)

    @Update
    suspend fun updatePatientConsentRecord(consent: PatientConsentRecord)

    @Query("SELECT * FROM patient_consent_records WHERE userId = :userId AND revokedAt IS NULL")
    suspend fun getActiveConsentsByUser(userId: String): List<PatientConsentRecord>

    @Query("SELECT * FROM patient_consent_records WHERE professionalId = :professionalId AND revokedAt IS NULL")
    suspend fun getActiveConsentsByProfessional(professionalId: String): List<PatientConsentRecord>

    @Query("SELECT * FROM patient_consent_records WHERE consentExpiry < :currentTime AND revokedAt IS NULL")
    suspend fun getExpiredConsents(currentTime: Long): List<PatientConsentRecord>

    @Query("SELECT * FROM patient_consent_records WHERE consentExpiry BETWEEN :currentTime AND :warningTime AND revokedAt IS NULL")
    suspend fun getExpiringConsents(currentTime: Long, warningTime: Long): List<PatientConsentRecord>

    @Query("UPDATE patient_consent_records SET revokedAt = :revokedAt, revokedReason = :reason WHERE consentId = :consentId")
    suspend fun revokeConsent(consentId: String, revokedAt: Long, reason: String)

    @Query("SELECT * FROM patient_consent_records WHERE userId = :userId AND professionalId = :professionalId AND consentType = :consentType AND revokedAt IS NULL")
    suspend fun getSpecificConsent(userId: String, professionalId: String, consentType: HipaaConsentType): PatientConsentRecord?

    // ==================== CLINICAL GOVERNANCE OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalGovernanceAudit(audit: ClinicalGovernanceAudit)

    @Update
    suspend fun updateClinicalGovernanceAudit(audit: ClinicalGovernanceAudit)

    @Query("SELECT * FROM clinical_governance_audits WHERE professionalId = :professionalId ORDER BY auditTimestamp DESC")
    suspend fun getClinicalGovernanceAuditsByProfessional(professionalId: String): List<ClinicalGovernanceAudit>

    @Query("SELECT * FROM clinical_governance_audits WHERE auditStatus = :status")
    suspend fun getClinicalGovernanceAuditsByStatus(status: AuditStatus): List<ClinicalGovernanceAudit>

    @Query("SELECT * FROM clinical_governance_audits WHERE followUpRequired = 1 AND followUpDate <= :currentTime")
    suspend fun getOverdueFollowUpAudits(currentTime: Long): List<ClinicalGovernanceAudit>

    @Query("SELECT * FROM clinical_governance_audits WHERE complianceScore < :threshold")
    suspend fun getLowComplianceAudits(threshold: Double): List<ClinicalGovernanceAudit>

    @Query("SELECT AVG(complianceScore) FROM clinical_governance_audits WHERE professionalId = :professionalId")
    suspend fun getAverageComplianceScore(professionalId: String): Double?

    @Query("""
        SELECT auditStandard, AVG(complianceScore) as averageScore, COUNT(*) as auditCount
        FROM clinical_governance_audits 
        WHERE professionalId = :professionalId 
        GROUP BY auditStandard
    """)
    suspend fun getComplianceScoresByStandard(professionalId: String): List<ComplianceScoreByStandard>

    // ==================== ELDER PROTECTION OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElderProtectionAssessment(assessment: ElderProtectionAssessment)

    @Update
    suspend fun updateElderProtectionAssessment(assessment: ElderProtectionAssessment)

    @Query("SELECT * FROM elder_protection_assessments WHERE userId = :userId ORDER BY assessmentTimestamp DESC")
    suspend fun getElderProtectionAssessmentsByUser(userId: String): List<ElderProtectionAssessment>

    @Query("SELECT * FROM elder_protection_assessments WHERE assessorId = :assessorId ORDER BY assessmentTimestamp DESC")
    suspend fun getElderProtectionAssessmentsByAssessor(assessorId: String): List<ElderProtectionAssessment>

    @Query("SELECT * FROM elder_protection_assessments WHERE abuseRiskLevel IN (:riskLevels)")
    suspend fun getElderProtectionAssessmentsByRiskLevel(riskLevels: List<AbuseRiskLevel>): List<ElderProtectionAssessment>

    @Query("SELECT * FROM elder_protection_assessments WHERE mandatoryReportingRequired = 1 AND reportingTimestamp IS NULL")
    suspend fun getPendingMandatoryReports(): List<ElderProtectionAssessment>

    @Query("SELECT * FROM elder_protection_assessments WHERE reviewDate <= :currentTime AND assessmentValid = 1")
    suspend fun getAssessmentsDueForReview(currentTime: Long): List<ElderProtectionAssessment>

    @Query("UPDATE elder_protection_assessments SET reportingTimestamp = :timestamp, reportingAgency = :agency WHERE assessmentId = :assessmentId")
    suspend fun recordMandatoryReporting(assessmentId: String, timestamp: Long, agency: String)

    @Query("""
        SELECT abuseRiskLevel, COUNT(*) as count 
        FROM elder_protection_assessments 
        WHERE assessorId = :assessorId AND assessmentValid = 1
        GROUP BY abuseRiskLevel
    """)
    suspend fun getRiskLevelDistribution(assessorId: String): List<RiskLevelStats>

    // ==================== REGULATORY COMPLIANCE OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegulatoryComplianceCheck(check: RegulatoryComplianceCheck)

    @Update
    suspend fun updateRegulatoryComplianceCheck(check: RegulatoryComplianceCheck)

    @Query("SELECT * FROM regulatory_compliance_checks WHERE professionalId = :professionalId ORDER BY checkTimestamp DESC")
    suspend fun getRegulatoryComplianceChecksByProfessional(professionalId: String): List<RegulatoryComplianceCheck>

    @Query("SELECT * FROM regulatory_compliance_checks WHERE regulatoryFramework = :framework")
    suspend fun getRegulatoryComplianceChecksByFramework(framework: RegulatoryFramework): List<RegulatoryComplianceCheck>

    @Query("SELECT * FROM regulatory_compliance_checks WHERE complianceStatus = :status")
    suspend fun getRegulatoryComplianceChecksByStatus(status: ComplianceStatus): List<RegulatoryComplianceCheck>

    @Query("SELECT * FROM regulatory_compliance_checks WHERE nextCheckDate <= :currentTime")
    suspend fun getOverdueComplianceChecks(currentTime: Long): List<RegulatoryComplianceCheck>

    @Query("SELECT * FROM regulatory_compliance_checks WHERE certificateExpiry BETWEEN :currentTime AND :warningTime")
    suspend fun getExpiringCertificates(currentTime: Long, warningTime: Long): List<RegulatoryComplianceCheck>

    @Query("""
        SELECT regulatoryFramework, complianceStatus, COUNT(*) as count
        FROM regulatory_compliance_checks 
        WHERE professionalId = :professionalId 
        GROUP BY regulatoryFramework, complianceStatus
    """)
    suspend fun getComplianceStatusSummary(professionalId: String): List<ComplianceStatusSummary>

    // ==================== COMPLIANCE MONITORING FLOWS ====================

    @Query("SELECT * FROM hipaa_compliance_logs WHERE complianceVerified = 0")
    fun getUnverifiedHipaaAccessFlow(): Flow<List<HipaaComplianceLog>>

    @Query("SELECT * FROM patient_consent_records WHERE consentExpiry BETWEEN :currentTime AND :warningTime AND revokedAt IS NULL")
    fun getExpiringConsentsFlow(currentTime: Long, warningTime: Long): Flow<List<PatientConsentRecord>>

    @Query("SELECT * FROM clinical_governance_audits WHERE followUpRequired = 1 AND followUpDate <= :currentTime")
    fun getOverdueFollowUpAuditsFlow(currentTime: Long): Flow<List<ClinicalGovernanceAudit>>

    @Query("SELECT * FROM elder_protection_assessments WHERE mandatoryReportingRequired = 1 AND reportingTimestamp IS NULL")
    fun getPendingMandatoryReportsFlow(): Flow<List<ElderProtectionAssessment>>

    @Query("SELECT * FROM regulatory_compliance_checks WHERE nextCheckDate <= :currentTime")
    fun getOverdueComplianceChecksFlow(currentTime: Long): Flow<List<RegulatoryComplianceCheck>>

    // ==================== BATCH OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHipaaComplianceLogs(logs: List<HipaaComplianceLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatientConsentRecords(consents: List<PatientConsentRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalGovernanceAudits(audits: List<ClinicalGovernanceAudit>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElderProtectionAssessments(assessments: List<ElderProtectionAssessment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegulatoryComplianceChecks(checks: List<RegulatoryComplianceCheck>)

    // ==================== CLEANUP OPERATIONS ====================

    @Query("DELETE FROM hipaa_compliance_logs WHERE accessTimestamp < :cutoffTime")
    suspend fun cleanupOldHipaaComplianceLogs(cutoffTime: Long)

    @Query("DELETE FROM patient_consent_records WHERE revokedAt IS NOT NULL AND revokedAt < :cutoffTime")
    suspend fun cleanupRevokedConsents(cutoffTime: Long)

    @Query("DELETE FROM clinical_governance_audits WHERE auditStatus = 'COMPLETED' AND auditTimestamp < :cutoffTime")
    suspend fun cleanupOldGovernanceAudits(cutoffTime: Long)

    @Query("DELETE FROM elder_protection_assessments WHERE assessmentValid = 0 AND assessmentTimestamp < :cutoffTime")
    suspend fun cleanupInvalidProtectionAssessments(cutoffTime: Long)

    @Query("DELETE FROM regulatory_compliance_checks WHERE complianceStatus = 'COMPLIANT' AND checkTimestamp < :cutoffTime")
    suspend fun cleanupOldComplianceChecks(cutoffTime: Long)

    // ==================== STATISTICS AND REPORTING ====================

    @Query("""
        SELECT 
            COUNT(*) as totalAccess,
            COUNT(CASE WHEN complianceVerified = 1 THEN 1 END) as verifiedAccess,
            COUNT(CASE WHEN complianceVerified = 0 THEN 1 END) as unverifiedAccess
        FROM hipaa_compliance_logs 
        WHERE professionalId = :professionalId
    """)
    suspend fun getHipaaComplianceStats(professionalId: String): HipaaComplianceStats

    @Query("""
        SELECT 
            COUNT(*) as totalConsents,
            COUNT(CASE WHEN revokedAt IS NULL THEN 1 END) as activeConsents,
            COUNT(CASE WHEN revokedAt IS NOT NULL THEN 1 END) as revokedConsents,
            COUNT(CASE WHEN consentExpiry < :currentTime THEN 1 END) as expiredConsents
        FROM patient_consent_records 
        WHERE professionalId = :professionalId
    """)
    suspend fun getConsentStats(professionalId: String, currentTime: Long): ConsentStats

    @Query("""
        SELECT 
            COUNT(*) as totalAudits,
            AVG(complianceScore) as averageScore,
            COUNT(CASE WHEN complianceScore >= 90 THEN 1 END) as excellentAudits,
            COUNT(CASE WHEN complianceScore < 70 THEN 1 END) as poorAudits
        FROM clinical_governance_audits 
        WHERE professionalId = :professionalId
    """)
    suspend fun getGovernanceAuditStats(professionalId: String): GovernanceAuditStats

    @Query("""
        SELECT 
            COUNT(*) as totalAssessments,
            COUNT(CASE WHEN abuseRiskLevel = 'HIGH' OR abuseRiskLevel = 'CRITICAL' THEN 1 END) as highRiskAssessments,
            COUNT(CASE WHEN mandatoryReportingRequired = 1 THEN 1 END) as mandatoryReports,
            COUNT(CASE WHEN mandatoryReportingRequired = 1 AND reportingTimestamp IS NOT NULL THEN 1 END) as completedReports
        FROM elder_protection_assessments 
        WHERE assessorId = :assessorId
    """)
    suspend fun getProtectionAssessmentStats(assessorId: String): ProtectionAssessmentStats
}

// ==================== STATISTICS DATA CLASSES ====================

data class HipaaAccessStats(
    val dataAccessType: HipaaDataAccessType,
    val accessCount: Int
)

data class HipaaComplianceStats(
    val totalAccess: Int,
    val verifiedAccess: Int,
    val unverifiedAccess: Int
) {
    val complianceRate: Double
        get() = if (totalAccess > 0) (verifiedAccess.toDouble() / totalAccess) * 100 else 0.0
}

data class ConsentStats(
    val totalConsents: Int,
    val activeConsents: Int,
    val revokedConsents: Int,
    val expiredConsents: Int
)

data class GovernanceAuditStats(
    val totalAudits: Int,
    val averageScore: Double,
    val excellentAudits: Int,
    val poorAudits: Int
)

data class ProtectionAssessmentStats(
    val totalAssessments: Int,
    val highRiskAssessments: Int,
    val mandatoryReports: Int,
    val completedReports: Int
) {
    val reportingComplianceRate: Double
        get() = if (mandatoryReports > 0) (completedReports.toDouble() / mandatoryReports) * 100 else 100.0
}

data class ComplianceScoreByStandard(
    val auditStandard: ClinicalGovernanceStandard,
    val averageScore: Double,
    val auditCount: Int
)

data class ComplianceStatusSummary(
    val regulatoryFramework: RegulatoryFramework,
    val complianceStatus: ComplianceStatus,
    val count: Int
)

data class RiskLevelStats(
    val abuseRiskLevel: AbuseRiskLevel,
    val count: Int
)
