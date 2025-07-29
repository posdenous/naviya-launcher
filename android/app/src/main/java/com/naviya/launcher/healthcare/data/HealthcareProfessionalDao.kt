package com.naviya.launcher.healthcare.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Healthcare Professional Integration System
 * Provides database operations for professional registration, installation, clinical oversight, and assessments
 */
@Dao
interface HealthcareProfessionalDao {

    // ==================== HEALTHCARE PROFESSIONAL REGISTRATION ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionalRegistration(registration: HealthcareProfessionalRegistration)

    @Update
    suspend fun updateProfessionalRegistration(registration: HealthcareProfessionalRegistration)

    @Delete
    suspend fun deleteProfessionalRegistration(registration: HealthcareProfessionalRegistration)

    @Query("SELECT * FROM healthcare_professional_registrations WHERE registrationId = :registrationId")
    suspend fun getProfessionalRegistrationById(registrationId: String): HealthcareProfessionalRegistration?

    @Query("SELECT * FROM healthcare_professional_registrations WHERE professionalId = :professionalId")
    suspend fun getProfessionalRegistrationByProfessionalId(professionalId: String): HealthcareProfessionalRegistration?

    @Query("SELECT * FROM healthcare_professional_registrations WHERE status = :status")
    suspend fun getProfessionalRegistrationsByStatus(status: ProfessionalRegistrationStatus): List<HealthcareProfessionalRegistration>

    @Query("SELECT * FROM healthcare_professional_registrations WHERE status = :status")
    fun getProfessionalRegistrationsByStatusFlow(status: ProfessionalRegistrationStatus): Flow<List<HealthcareProfessionalRegistration>>

    @Query("SELECT * FROM healthcare_professional_registrations WHERE installationAuthorized = 1")
    suspend fun getAuthorizedProfessionals(): List<HealthcareProfessionalRegistration>

    @Query("SELECT * FROM healthcare_professional_registrations WHERE installationAuthorized = 1")
    fun getAuthorizedProfessionalsFlow(): Flow<List<HealthcareProfessionalRegistration>>

    @Query("SELECT * FROM healthcare_professional_registrations WHERE nextRecertificationDate <= :currentTime")
    suspend fun getProfessionalsRequiringRecertification(currentTime: Long): List<HealthcareProfessionalRegistration>

    @Query("SELECT * FROM healthcare_professional_registrations WHERE trainingCompleted = 0")
    suspend fun getProfessionalsRequiringTraining(): List<HealthcareProfessionalRegistration>

    @Query("SELECT * FROM healthcare_professional_registrations WHERE backgroundCheckCompleted = 0")
    suspend fun getProfessionalsRequiringBackgroundCheck(): List<HealthcareProfessionalRegistration>

    @Query("SELECT * FROM healthcare_professional_registrations ORDER BY registrationTimestamp DESC")
    suspend fun getAllProfessionalRegistrations(): List<HealthcareProfessionalRegistration>

    @Query("SELECT * FROM healthcare_professional_registrations ORDER BY registrationTimestamp DESC")
    fun getAllProfessionalRegistrationsFlow(): Flow<List<HealthcareProfessionalRegistration>>

    @Query("SELECT COUNT(*) FROM healthcare_professional_registrations WHERE status = :status")
    suspend fun getProfessionalRegistrationCountByStatus(status: ProfessionalRegistrationStatus): Int

    @Query("SELECT COUNT(*) FROM healthcare_professional_registrations WHERE installationAuthorized = 1")
    suspend fun getAuthorizedProfessionalCount(): Int

    // ==================== PROFESSIONAL INSTALLATION ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionalInstallation(installation: ProfessionalInstallation)

    @Update
    suspend fun updateProfessionalInstallation(installation: ProfessionalInstallation)

    @Delete
    suspend fun deleteProfessionalInstallation(installation: ProfessionalInstallation)

    @Query("SELECT * FROM professional_installations WHERE installationId = :installationId")
    suspend fun getProfessionalInstallationById(installationId: String): ProfessionalInstallation?

    @Query("SELECT * FROM professional_installations WHERE userId = :userId")
    suspend fun getProfessionalInstallationsByUserId(userId: String): List<ProfessionalInstallation>

    @Query("SELECT * FROM professional_installations WHERE userId = :userId")
    fun getProfessionalInstallationsByUserIdFlow(userId: String): Flow<List<ProfessionalInstallation>>

    @Query("SELECT * FROM professional_installations WHERE professionalId = :professionalId")
    suspend fun getProfessionalInstallationsByProfessionalId(professionalId: String): List<ProfessionalInstallation>

    @Query("SELECT * FROM professional_installations WHERE professionalId = :professionalId")
    fun getProfessionalInstallationsByProfessionalIdFlow(professionalId: String): Flow<List<ProfessionalInstallation>>

    @Query("SELECT * FROM professional_installations WHERE installationStatus = :status")
    suspend fun getProfessionalInstallationsByStatus(status: InstallationStatus): List<ProfessionalInstallation>

    @Query("SELECT * FROM professional_installations WHERE installationStatus = :status")
    fun getProfessionalInstallationsByStatusFlow(status: InstallationStatus): Flow<List<ProfessionalInstallation>>

    @Query("SELECT * FROM professional_installations WHERE installationType = :type")
    suspend fun getProfessionalInstallationsByType(type: InstallationType): List<ProfessionalInstallation>

    @Query("SELECT * FROM professional_installations WHERE clinicalContext = :context")
    suspend fun getProfessionalInstallationsByContext(context: ClinicalContext): List<ProfessionalInstallation>

    @Query("SELECT * FROM professional_installations WHERE nextReviewDate <= :currentTime")
    suspend fun getProfessionalInstallationsRequiringReview(currentTime: Long): List<ProfessionalInstallation>

    @Query("SELECT * FROM professional_installations WHERE followUpScheduled = 1")
    suspend fun getProfessionalInstallationsRequiringFollowUp(): List<ProfessionalInstallation>

    @Query("SELECT * FROM professional_installations WHERE elderRightsAdvocateInformed = 0")
    suspend fun getProfessionalInstallationsRequiringAdvocateNotification(): List<ProfessionalInstallation>

    @Query("SELECT * FROM professional_installations ORDER BY installationTimestamp DESC")
    suspend fun getAllProfessionalInstallations(): List<ProfessionalInstallation>

    @Query("SELECT * FROM professional_installations ORDER BY installationTimestamp DESC")
    fun getAllProfessionalInstallationsFlow(): Flow<List<ProfessionalInstallation>>

    @Query("SELECT COUNT(*) FROM professional_installations WHERE installationStatus = :status")
    suspend fun getProfessionalInstallationCountByStatus(status: InstallationStatus): Int

    @Query("SELECT COUNT(*) FROM professional_installations WHERE professionalId = :professionalId")
    suspend fun getProfessionalInstallationCountByProfessional(professionalId: String): Int

    // ==================== CLINICAL OVERSIGHT ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalOversight(oversight: ClinicalOversight)

    @Update
    suspend fun updateClinicalOversight(oversight: ClinicalOversight)

    @Delete
    suspend fun deleteClinicalOversight(oversight: ClinicalOversight)

    @Query("SELECT * FROM clinical_oversight WHERE oversightId = :oversightId")
    suspend fun getClinicalOversightById(oversightId: String): ClinicalOversight?

    @Query("SELECT * FROM clinical_oversight WHERE userId = :userId")
    suspend fun getClinicalOversightByUserId(userId: String): ClinicalOversight?

    @Query("SELECT * FROM clinical_oversight WHERE userId = :userId")
    fun getClinicalOversightByUserIdFlow(userId: String): Flow<ClinicalOversight?>

    @Query("SELECT * FROM clinical_oversight WHERE primaryPhysicianId = :physicianId")
    suspend fun getClinicalOversightByPhysician(physicianId: String): List<ClinicalOversight>

    @Query("SELECT * FROM clinical_oversight WHERE primaryPhysicianId = :physicianId")
    fun getClinicalOversightByPhysicianFlow(physicianId: String): Flow<List<ClinicalOversight>>

    @Query("SELECT * FROM clinical_oversight WHERE oversightLevel = :level")
    suspend fun getClinicalOversightByLevel(level: ClinicalOversightLevel): List<ClinicalOversight>

    @Query("SELECT * FROM clinical_oversight WHERE institutionId = :institutionId")
    suspend fun getClinicalOversightByInstitution(institutionId: String): List<ClinicalOversight>

    @Query("SELECT * FROM clinical_oversight WHERE isActive = 1")
    suspend fun getActiveClinicalOversight(): List<ClinicalOversight>

    @Query("SELECT * FROM clinical_oversight WHERE isActive = 1")
    fun getActiveClinicalOversightFlow(): Flow<List<ClinicalOversight>>

    @Query("SELECT * FROM clinical_oversight WHERE nextReviewDate <= :currentTime")
    suspend fun getClinicalOversightRequiringReview(currentTime: Long): List<ClinicalOversight>

    @Query("SELECT * FROM clinical_oversight ORDER BY establishmentTimestamp DESC")
    suspend fun getAllClinicalOversight(): List<ClinicalOversight>

    @Query("SELECT * FROM clinical_oversight ORDER BY establishmentTimestamp DESC")
    fun getAllClinicalOversightFlow(): Flow<List<ClinicalOversight>>

    @Query("SELECT COUNT(*) FROM clinical_oversight WHERE isActive = 1")
    suspend fun getActiveClinicalOversightCount(): Int

    @Query("SELECT COUNT(*) FROM clinical_oversight WHERE primaryPhysicianId = :physicianId")
    suspend fun getClinicalOversightCountByPhysician(physicianId: String): Int

    // ==================== CLINICAL ASSESSMENT ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalAssessment(assessment: ClinicalAssessment)

    @Update
    suspend fun updateClinicalAssessment(assessment: ClinicalAssessment)

    @Delete
    suspend fun deleteClinicalAssessment(assessment: ClinicalAssessment)

    @Query("SELECT * FROM clinical_assessments WHERE assessmentId = :assessmentId")
    suspend fun getClinicalAssessmentById(assessmentId: String): ClinicalAssessment?

    @Query("SELECT * FROM clinical_assessments WHERE userId = :userId ORDER BY assessmentTimestamp DESC")
    suspend fun getClinicalAssessmentsByUserId(userId: String): List<ClinicalAssessment>

    @Query("SELECT * FROM clinical_assessments WHERE userId = :userId ORDER BY assessmentTimestamp DESC")
    fun getClinicalAssessmentsByUserIdFlow(userId: String): Flow<List<ClinicalAssessment>>

    @Query("SELECT * FROM clinical_assessments WHERE assessingPhysicianId = :physicianId ORDER BY assessmentTimestamp DESC")
    suspend fun getClinicalAssessmentsByPhysician(physicianId: String): List<ClinicalAssessment>

    @Query("SELECT * FROM clinical_assessments WHERE assessingPhysicianId = :physicianId ORDER BY assessmentTimestamp DESC")
    fun getClinicalAssessmentsByPhysicianFlow(physicianId: String): Flow<List<ClinicalAssessment>>

    @Query("SELECT * FROM clinical_assessments WHERE assessmentType = :type ORDER BY assessmentTimestamp DESC")
    suspend fun getClinicalAssessmentsByType(type: ClinicalAssessmentType): List<ClinicalAssessment>

    @Query("SELECT * FROM clinical_assessments WHERE abuseRiskLevel = :riskLevel ORDER BY assessmentTimestamp DESC")
    suspend fun getClinicalAssessmentsByRiskLevel(riskLevel: AbuseRiskLevel): List<ClinicalAssessment>

    @Query("SELECT * FROM clinical_assessments WHERE abuseRiskLevel = :riskLevel ORDER BY assessmentTimestamp DESC")
    fun getClinicalAssessmentsByRiskLevelFlow(riskLevel: AbuseRiskLevel): Flow<List<ClinicalAssessment>>

    @Query("SELECT * FROM clinical_assessments WHERE elderRightsAdvocateRecommended = 1 ORDER BY assessmentTimestamp DESC")
    suspend fun getClinicalAssessmentsRecommendingAdvocate(): List<ClinicalAssessment>

    @Query("SELECT * FROM clinical_assessments WHERE elderRightsAdvocateRecommended = 1 ORDER BY assessmentTimestamp DESC")
    fun getClinicalAssessmentsRecommendingAdvocateFlow(): Flow<List<ClinicalAssessment>>

    @Query("SELECT * FROM clinical_assessments WHERE followUpRequired = 1 ORDER BY nextAssessmentDate ASC")
    suspend fun getClinicalAssessmentsRequiringFollowUp(): List<ClinicalAssessment>

    @Query("SELECT * FROM clinical_assessments WHERE nextAssessmentDate <= :currentTime ORDER BY nextAssessmentDate ASC")
    suspend fun getClinicalAssessmentsDue(currentTime: Long): List<ClinicalAssessment>

    @Query("SELECT * FROM clinical_assessments WHERE assessmentValid = 1 ORDER BY assessmentTimestamp DESC")
    suspend fun getValidClinicalAssessments(): List<ClinicalAssessment>

    @Query("SELECT * FROM clinical_assessments WHERE userId = :userId AND assessmentValid = 1 ORDER BY assessmentTimestamp DESC LIMIT 1")
    suspend fun getLatestValidClinicalAssessment(userId: String): ClinicalAssessment?

    @Query("SELECT * FROM clinical_assessments WHERE userId = :userId AND assessmentValid = 1 ORDER BY assessmentTimestamp DESC LIMIT 1")
    fun getLatestValidClinicalAssessmentFlow(userId: String): Flow<ClinicalAssessment?>

    @Query("SELECT * FROM clinical_assessments ORDER BY assessmentTimestamp DESC")
    suspend fun getAllClinicalAssessments(): List<ClinicalAssessment>

    @Query("SELECT * FROM clinical_assessments ORDER BY assessmentTimestamp DESC")
    fun getAllClinicalAssessmentsFlow(): Flow<List<ClinicalAssessment>>

    @Query("SELECT COUNT(*) FROM clinical_assessments WHERE abuseRiskLevel = :riskLevel")
    suspend fun getClinicalAssessmentCountByRiskLevel(riskLevel: AbuseRiskLevel): Int

    @Query("SELECT COUNT(*) FROM clinical_assessments WHERE assessingPhysicianId = :physicianId")
    suspend fun getClinicalAssessmentCountByPhysician(physicianId: String): Int

    @Query("SELECT COUNT(*) FROM clinical_assessments WHERE elderRightsAdvocateRecommended = 1")
    suspend fun getClinicalAssessmentCountRecommendingAdvocate(): Int

    // ==================== COMPLEX QUERIES ====================

    @Query("""
        SELECT pi.* FROM professional_installations pi
        INNER JOIN healthcare_professional_registrations hpr ON pi.professionalId = hpr.professionalId
        WHERE hpr.status = :status
        ORDER BY pi.installationTimestamp DESC
    """)
    suspend fun getProfessionalInstallationsByRegistrationStatus(status: ProfessionalRegistrationStatus): List<ProfessionalInstallation>

    @Query("""
        SELECT ca.* FROM clinical_assessments ca
        INNER JOIN clinical_oversight co ON ca.userId = co.userId
        WHERE co.primaryPhysicianId = :physicianId
        ORDER BY ca.assessmentTimestamp DESC
    """)
    suspend fun getClinicalAssessmentsByOversightPhysician(physicianId: String): List<ClinicalAssessment>

    @Query("""
        SELECT pi.* FROM professional_installations pi
        INNER JOIN clinical_assessments ca ON pi.userId = ca.userId
        WHERE ca.abuseRiskLevel = :riskLevel
        ORDER BY pi.installationTimestamp DESC
    """)
    suspend fun getProfessionalInstallationsByAbuseRiskLevel(riskLevel: AbuseRiskLevel): List<ProfessionalInstallation>

    @Query("""
        SELECT hpr.* FROM healthcare_professional_registrations hpr
        INNER JOIN professional_installations pi ON hpr.professionalId = pi.professionalId
        WHERE pi.installationStatus = :status
        GROUP BY hpr.registrationId
        ORDER BY hpr.registrationTimestamp DESC
    """)
    suspend fun getProfessionalsByInstallationStatus(status: InstallationStatus): List<HealthcareProfessionalRegistration>

    @Query("""
        SELECT ca.* FROM clinical_assessments ca
        INNER JOIN professional_installations pi ON ca.userId = pi.userId
        WHERE pi.professionalId = :professionalId
        ORDER BY ca.assessmentTimestamp DESC
    """)
    suspend fun getClinicalAssessmentsByInstallationProfessional(professionalId: String): List<ClinicalAssessment>

    // ==================== STATISTICS AND REPORTING ====================

    @Query("""
        SELECT 
            COUNT(*) as totalInstallations,
            SUM(CASE WHEN installationStatus = 'COMPLETED' THEN 1 ELSE 0 END) as completedInstallations,
            SUM(CASE WHEN installationStatus = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgressInstallations,
            SUM(CASE WHEN installationStatus = 'FAILED' THEN 1 ELSE 0 END) as failedInstallations
        FROM professional_installations
        WHERE professionalId = :professionalId
    """)
    suspend fun getProfessionalInstallationStatistics(professionalId: String): InstallationStatistics

    @Query("""
        SELECT 
            COUNT(*) as totalAssessments,
            SUM(CASE WHEN abuseRiskLevel = 'LOW' THEN 1 ELSE 0 END) as lowRiskAssessments,
            SUM(CASE WHEN abuseRiskLevel = 'MODERATE' THEN 1 ELSE 0 END) as moderateRiskAssessments,
            SUM(CASE WHEN abuseRiskLevel = 'HIGH' THEN 1 ELSE 0 END) as highRiskAssessments,
            SUM(CASE WHEN abuseRiskLevel = 'CRITICAL' THEN 1 ELSE 0 END) as criticalRiskAssessments,
            SUM(CASE WHEN elderRightsAdvocateRecommended = 1 THEN 1 ELSE 0 END) as advocateRecommendations
        FROM clinical_assessments
        WHERE assessingPhysicianId = :physicianId
    """)
    suspend fun getPhysicianAssessmentStatistics(physicianId: String): AssessmentStatistics

    @Query("""
        SELECT 
            COUNT(*) as totalProfessionals,
            SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as activeProfessionals,
            SUM(CASE WHEN installationAuthorized = 1 THEN 1 ELSE 0 END) as authorizedProfessionals,
            SUM(CASE WHEN trainingCompleted = 1 THEN 1 ELSE 0 END) as trainedProfessionals
        FROM healthcare_professional_registrations
    """)
    suspend fun getOverallProfessionalStatistics(): ProfessionalStatistics

    // ==================== BATCH OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionalRegistrations(registrations: List<HealthcareProfessionalRegistration>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionalInstallations(installations: List<ProfessionalInstallation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalOversights(oversights: List<ClinicalOversight>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalAssessments(assessments: List<ClinicalAssessment>)

    @Query("DELETE FROM healthcare_professional_registrations WHERE registrationId IN (:registrationIds)")
    suspend fun deleteProfessionalRegistrationsByIds(registrationIds: List<String>)

    @Query("DELETE FROM professional_installations WHERE installationId IN (:installationIds)")
    suspend fun deleteProfessionalInstallationsByIds(installationIds: List<String>)

    @Query("DELETE FROM clinical_oversight WHERE oversightId IN (:oversightIds)")
    suspend fun deleteClinicalOversightsByIds(oversightIds: List<String>)

    @Query("DELETE FROM clinical_assessments WHERE assessmentId IN (:assessmentIds)")
    suspend fun deleteClinicalAssessmentsByIds(assessmentIds: List<String>)

    // ==================== CLEANUP OPERATIONS ====================

    @Query("DELETE FROM healthcare_professional_registrations WHERE status = 'EXPIRED' AND lastReviewDate < :cutoffTime")
    suspend fun cleanupExpiredRegistrations(cutoffTime: Long)

    @Query("DELETE FROM professional_installations WHERE installationStatus = 'FAILED' AND installationTimestamp < :cutoffTime")
    suspend fun cleanupFailedInstallations(cutoffTime: Long)

    @Query("DELETE FROM clinical_assessments WHERE assessmentValid = 0 AND assessmentTimestamp < :cutoffTime")
    suspend fun cleanupInvalidAssessments(cutoffTime: Long)

    @Query("UPDATE clinical_oversight SET isActive = 0 WHERE nextReviewDate < :currentTime")
    suspend fun deactivateOverdueOversight(currentTime: Long)
}

// ==================== STATISTICS DATA CLASSES ====================

data class InstallationStatistics(
    val totalInstallations: Int,
    val completedInstallations: Int,
    val inProgressInstallations: Int,
    val failedInstallations: Int
)

data class AssessmentStatistics(
    val totalAssessments: Int,
    val lowRiskAssessments: Int,
    val moderateRiskAssessments: Int,
    val highRiskAssessments: Int,
    val criticalRiskAssessments: Int,
    val advocateRecommendations: Int
)

data class ProfessionalStatistics(
    val totalProfessionals: Int,
    val activeProfessionals: Int,
    val authorizedProfessionals: Int,
    val trainedProfessionals: Int
)
