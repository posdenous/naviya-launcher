package com.naviya.launcher.healthcare

import com.naviya.launcher.healthcare.data.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository layer for Healthcare Professional Integration System
 * Provides clean API for accessing healthcare professional data with caching and offline support
 */
@Singleton
class HealthcareProfessionalRepository @Inject constructor(
    private val healthcareProfessionalDao: HealthcareProfessionalDao
) {

    // ==================== PROFESSIONAL REGISTRATION ====================

    suspend fun insertProfessionalRegistration(registration: HealthcareProfessionalRegistration) {
        healthcareProfessionalDao.insertProfessionalRegistration(registration)
    }

    suspend fun updateProfessionalRegistration(registration: HealthcareProfessionalRegistration) {
        healthcareProfessionalDao.updateProfessionalRegistration(registration)
    }

    suspend fun getProfessionalRegistrationById(registrationId: String): HealthcareProfessionalRegistration? {
        return healthcareProfessionalDao.getProfessionalRegistrationById(registrationId)
    }

    suspend fun getProfessionalRegistrationByProfessionalId(professionalId: String): HealthcareProfessionalRegistration? {
        return healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(professionalId)
    }

    suspend fun getProfessionalRegistrationsByStatus(status: ProfessionalRegistrationStatus): List<HealthcareProfessionalRegistration> {
        return healthcareProfessionalDao.getProfessionalRegistrationsByStatus(status)
    }

    fun getProfessionalRegistrationsByStatusFlow(status: ProfessionalRegistrationStatus): Flow<List<HealthcareProfessionalRegistration>> {
        return healthcareProfessionalDao.getProfessionalRegistrationsByStatusFlow(status)
    }

    suspend fun getAuthorizedProfessionals(): List<HealthcareProfessionalRegistration> {
        return healthcareProfessionalDao.getAuthorizedProfessionals()
    }

    fun getAuthorizedProfessionalsFlow(): Flow<List<HealthcareProfessionalRegistration>> {
        return healthcareProfessionalDao.getAuthorizedProfessionalsFlow()
    }

    suspend fun getProfessionalsRequiringRecertification(currentTime: Long): List<HealthcareProfessionalRegistration> {
        return healthcareProfessionalDao.getProfessionalsRequiringRecertification(currentTime)
    }

    suspend fun getProfessionalsRequiringTraining(): List<HealthcareProfessionalRegistration> {
        return healthcareProfessionalDao.getProfessionalsRequiringTraining()
    }

    suspend fun getAllProfessionalRegistrations(): List<HealthcareProfessionalRegistration> {
        return healthcareProfessionalDao.getAllProfessionalRegistrations()
    }

    fun getAllProfessionalRegistrationsFlow(): Flow<List<HealthcareProfessionalRegistration>> {
        return healthcareProfessionalDao.getAllProfessionalRegistrationsFlow()
    }

    suspend fun getProfessionalRegistrationCountByStatus(status: ProfessionalRegistrationStatus): Int {
        return healthcareProfessionalDao.getProfessionalRegistrationCountByStatus(status)
    }

    suspend fun getAuthorizedProfessionalCount(): Int {
        return healthcareProfessionalDao.getAuthorizedProfessionalCount()
    }

    // ==================== PROFESSIONAL INSTALLATION ====================

    suspend fun insertProfessionalInstallation(installation: ProfessionalInstallation) {
        healthcareProfessionalDao.insertProfessionalInstallation(installation)
    }

    suspend fun updateProfessionalInstallation(installation: ProfessionalInstallation) {
        healthcareProfessionalDao.updateProfessionalInstallation(installation)
    }

    suspend fun getProfessionalInstallationById(installationId: String): ProfessionalInstallation? {
        return healthcareProfessionalDao.getProfessionalInstallationById(installationId)
    }

    suspend fun getProfessionalInstallationsByUserId(userId: String): List<ProfessionalInstallation> {
        return healthcareProfessionalDao.getProfessionalInstallationsByUserId(userId)
    }

    fun getProfessionalInstallationsByUserIdFlow(userId: String): Flow<List<ProfessionalInstallation>> {
        return healthcareProfessionalDao.getProfessionalInstallationsByUserIdFlow(userId)
    }

    suspend fun getProfessionalInstallationsByProfessionalId(professionalId: String): List<ProfessionalInstallation> {
        return healthcareProfessionalDao.getProfessionalInstallationsByProfessionalId(professionalId)
    }

    fun getProfessionalInstallationsByProfessionalIdFlow(professionalId: String): Flow<List<ProfessionalInstallation>> {
        return healthcareProfessionalDao.getProfessionalInstallationsByProfessionalIdFlow(professionalId)
    }

    suspend fun getProfessionalInstallationsByStatus(status: InstallationStatus): List<ProfessionalInstallation> {
        return healthcareProfessionalDao.getProfessionalInstallationsByStatus(status)
    }

    fun getProfessionalInstallationsByStatusFlow(status: InstallationStatus): Flow<List<ProfessionalInstallation>> {
        return healthcareProfessionalDao.getProfessionalInstallationsByStatusFlow(status)
    }

    suspend fun getProfessionalInstallationsRequiringReview(currentTime: Long): List<ProfessionalInstallation> {
        return healthcareProfessionalDao.getProfessionalInstallationsRequiringReview(currentTime)
    }

    suspend fun getProfessionalInstallationsRequiringFollowUp(): List<ProfessionalInstallation> {
        return healthcareProfessionalDao.getProfessionalInstallationsRequiringFollowUp()
    }

    suspend fun getProfessionalInstallationsRequiringAdvocateNotification(): List<ProfessionalInstallation> {
        return healthcareProfessionalDao.getProfessionalInstallationsRequiringAdvocateNotification()
    }

    suspend fun getAllProfessionalInstallations(): List<ProfessionalInstallation> {
        return healthcareProfessionalDao.getAllProfessionalInstallations()
    }

    fun getAllProfessionalInstallationsFlow(): Flow<List<ProfessionalInstallation>> {
        return healthcareProfessionalDao.getAllProfessionalInstallationsFlow()
    }

    suspend fun getProfessionalInstallationCountByStatus(status: InstallationStatus): Int {
        return healthcareProfessionalDao.getProfessionalInstallationCountByStatus(status)
    }

    // ==================== CLINICAL OVERSIGHT ====================

    suspend fun insertClinicalOversight(oversight: ClinicalOversight) {
        healthcareProfessionalDao.insertClinicalOversight(oversight)
    }

    suspend fun updateClinicalOversight(oversight: ClinicalOversight) {
        healthcareProfessionalDao.updateClinicalOversight(oversight)
    }

    suspend fun getClinicalOversightById(oversightId: String): ClinicalOversight? {
        return healthcareProfessionalDao.getClinicalOversightById(oversightId)
    }

    suspend fun getClinicalOversightByUserId(userId: String): ClinicalOversight? {
        return healthcareProfessionalDao.getClinicalOversightByUserId(userId)
    }

    fun getClinicalOversightByUserIdFlow(userId: String): Flow<ClinicalOversight?> {
        return healthcareProfessionalDao.getClinicalOversightByUserIdFlow(userId)
    }

    suspend fun getClinicalOversightByPhysician(physicianId: String): List<ClinicalOversight> {
        return healthcareProfessionalDao.getClinicalOversightByPhysician(physicianId)
    }

    fun getClinicalOversightByPhysicianFlow(physicianId: String): Flow<List<ClinicalOversight>> {
        return healthcareProfessionalDao.getClinicalOversightByPhysicianFlow(physicianId)
    }

    suspend fun getActiveClinicalOversight(): List<ClinicalOversight> {
        return healthcareProfessionalDao.getActiveClinicalOversight()
    }

    fun getActiveClinicalOversightFlow(): Flow<List<ClinicalOversight>> {
        return healthcareProfessionalDao.getActiveClinicalOversightFlow()
    }

    suspend fun getClinicalOversightRequiringReview(currentTime: Long): List<ClinicalOversight> {
        return healthcareProfessionalDao.getClinicalOversightRequiringReview(currentTime)
    }

    suspend fun getAllClinicalOversight(): List<ClinicalOversight> {
        return healthcareProfessionalDao.getAllClinicalOversight()
    }

    fun getAllClinicalOversightFlow(): Flow<List<ClinicalOversight>> {
        return healthcareProfessionalDao.getAllClinicalOversightFlow()
    }

    suspend fun getActiveClinicalOversightCount(): Int {
        return healthcareProfessionalDao.getActiveClinicalOversightCount()
    }

    // ==================== CLINICAL ASSESSMENT ====================

    suspend fun insertClinicalAssessment(assessment: ClinicalAssessment) {
        healthcareProfessionalDao.insertClinicalAssessment(assessment)
    }

    suspend fun updateClinicalAssessment(assessment: ClinicalAssessment) {
        healthcareProfessionalDao.updateClinicalAssessment(assessment)
    }

    suspend fun getClinicalAssessmentById(assessmentId: String): ClinicalAssessment? {
        return healthcareProfessionalDao.getClinicalAssessmentById(assessmentId)
    }

    suspend fun getClinicalAssessmentsByUserId(userId: String): List<ClinicalAssessment> {
        return healthcareProfessionalDao.getClinicalAssessmentsByUserId(userId)
    }

    fun getClinicalAssessmentsByUserIdFlow(userId: String): Flow<List<ClinicalAssessment>> {
        return healthcareProfessionalDao.getClinicalAssessmentsByUserIdFlow(userId)
    }

    suspend fun getClinicalAssessmentsByPhysician(physicianId: String): List<ClinicalAssessment> {
        return healthcareProfessionalDao.getClinicalAssessmentsByPhysician(physicianId)
    }

    fun getClinicalAssessmentsByPhysicianFlow(physicianId: String): Flow<List<ClinicalAssessment>> {
        return healthcareProfessionalDao.getClinicalAssessmentsByPhysicianFlow(physicianId)
    }

    suspend fun getClinicalAssessmentsByRiskLevel(riskLevel: AbuseRiskLevel): List<ClinicalAssessment> {
        return healthcareProfessionalDao.getClinicalAssessmentsByRiskLevel(riskLevel)
    }

    fun getClinicalAssessmentsByRiskLevelFlow(riskLevel: AbuseRiskLevel): Flow<List<ClinicalAssessment>> {
        return healthcareProfessionalDao.getClinicalAssessmentsByRiskLevelFlow(riskLevel)
    }

    suspend fun getClinicalAssessmentsRecommendingAdvocate(): List<ClinicalAssessment> {
        return healthcareProfessionalDao.getClinicalAssessmentsRecommendingAdvocate()
    }

    fun getClinicalAssessmentsRecommendingAdvocateFlow(): Flow<List<ClinicalAssessment>> {
        return healthcareProfessionalDao.getClinicalAssessmentsRecommendingAdvocateFlow()
    }

    suspend fun getClinicalAssessmentsRequiringFollowUp(): List<ClinicalAssessment> {
        return healthcareProfessionalDao.getClinicalAssessmentsRequiringFollowUp()
    }

    suspend fun getClinicalAssessmentsDue(currentTime: Long): List<ClinicalAssessment> {
        return healthcareProfessionalDao.getClinicalAssessmentsDue(currentTime)
    }

    suspend fun getLatestValidClinicalAssessment(userId: String): ClinicalAssessment? {
        return healthcareProfessionalDao.getLatestValidClinicalAssessment(userId)
    }

    fun getLatestValidClinicalAssessmentFlow(userId: String): Flow<ClinicalAssessment?> {
        return healthcareProfessionalDao.getLatestValidClinicalAssessmentFlow(userId)
    }

    suspend fun getAllClinicalAssessments(): List<ClinicalAssessment> {
        return healthcareProfessionalDao.getAllClinicalAssessments()
    }

    fun getAllClinicalAssessmentsFlow(): Flow<List<ClinicalAssessment>> {
        return healthcareProfessionalDao.getAllClinicalAssessmentsFlow()
    }

    suspend fun getClinicalAssessmentCountByRiskLevel(riskLevel: AbuseRiskLevel): Int {
        return healthcareProfessionalDao.getClinicalAssessmentCountByRiskLevel(riskLevel)
    }

    suspend fun getClinicalAssessmentCountRecommendingAdvocate(): Int {
        return healthcareProfessionalDao.getClinicalAssessmentCountRecommendingAdvocate()
    }

    // ==================== COMPLEX QUERIES ====================

    suspend fun getProfessionalInstallationsByRegistrationStatus(status: ProfessionalRegistrationStatus): List<ProfessionalInstallation> {
        return healthcareProfessionalDao.getProfessionalInstallationsByRegistrationStatus(status)
    }

    suspend fun getClinicalAssessmentsByOversightPhysician(physicianId: String): List<ClinicalAssessment> {
        return healthcareProfessionalDao.getClinicalAssessmentsByOversightPhysician(physicianId)
    }

    suspend fun getProfessionalInstallationsByAbuseRiskLevel(riskLevel: AbuseRiskLevel): List<ProfessionalInstallation> {
        return healthcareProfessionalDao.getProfessionalInstallationsByAbuseRiskLevel(riskLevel)
    }

    suspend fun getProfessionalsByInstallationStatus(status: InstallationStatus): List<HealthcareProfessionalRegistration> {
        return healthcareProfessionalDao.getProfessionalsByInstallationStatus(status)
    }

    suspend fun getClinicalAssessmentsByInstallationProfessional(professionalId: String): List<ClinicalAssessment> {
        return healthcareProfessionalDao.getClinicalAssessmentsByInstallationProfessional(professionalId)
    }

    // ==================== STATISTICS AND REPORTING ====================

    suspend fun getProfessionalInstallationStatistics(professionalId: String): InstallationStatistics {
        return healthcareProfessionalDao.getProfessionalInstallationStatistics(professionalId)
    }

    suspend fun getPhysicianAssessmentStatistics(physicianId: String): AssessmentStatistics {
        return healthcareProfessionalDao.getPhysicianAssessmentStatistics(physicianId)
    }

    suspend fun getOverallProfessionalStatistics(): ProfessionalStatistics {
        return healthcareProfessionalDao.getOverallProfessionalStatistics()
    }

    // ==================== BATCH OPERATIONS ====================

    suspend fun insertProfessionalRegistrations(registrations: List<HealthcareProfessionalRegistration>) {
        healthcareProfessionalDao.insertProfessionalRegistrations(registrations)
    }

    suspend fun insertProfessionalInstallations(installations: List<ProfessionalInstallation>) {
        healthcareProfessionalDao.insertProfessionalInstallations(installations)
    }

    suspend fun insertClinicalOversights(oversights: List<ClinicalOversight>) {
        healthcareProfessionalDao.insertClinicalOversights(oversights)
    }

    suspend fun insertClinicalAssessments(assessments: List<ClinicalAssessment>) {
        healthcareProfessionalDao.insertClinicalAssessments(assessments)
    }

    // ==================== CLEANUP OPERATIONS ====================

    suspend fun cleanupExpiredRegistrations(cutoffTime: Long) {
        healthcareProfessionalDao.cleanupExpiredRegistrations(cutoffTime)
    }

    suspend fun cleanupFailedInstallations(cutoffTime: Long) {
        healthcareProfessionalDao.cleanupFailedInstallations(cutoffTime)
    }

    suspend fun cleanupInvalidAssessments(cutoffTime: Long) {
        healthcareProfessionalDao.cleanupInvalidAssessments(cutoffTime)
    }

    suspend fun deactivateOverdueOversight(currentTime: Long) {
        healthcareProfessionalDao.deactivateOverdueOversight(currentTime)
    }

    // ==================== BUSINESS LOGIC HELPERS ====================

    suspend fun isHealthcareProfessionalAuthorized(professionalId: String): Boolean {
        val registration = getProfessionalRegistrationByProfessionalId(professionalId)
        return registration?.let { 
            it.installationAuthorized && 
            it.status == ProfessionalRegistrationStatus.ACTIVE &&
            it.trainingCompleted &&
            it.backgroundCheckCompleted
        } ?: false
    }

    suspend fun hasActiveClinicalOversight(userId: String): Boolean {
        val oversight = getClinicalOversightByUserId(userId)
        return oversight?.isActive == true
    }

    suspend fun getHighRiskPatients(): List<ClinicalAssessment> {
        return getClinicalAssessmentsByRiskLevel(AbuseRiskLevel.HIGH) +
               getClinicalAssessmentsByRiskLevel(AbuseRiskLevel.CRITICAL)
    }

    suspend fun getProfessionalsRequiringAttention(): List<HealthcareProfessionalRegistration> {
        val currentTime = System.currentTimeMillis()
        return getProfessionalsRequiringRecertification(currentTime) +
               getProfessionalsRequiringTraining()
    }

    suspend fun getInstallationsRequiringAttention(): List<ProfessionalInstallation> {
        val currentTime = System.currentTimeMillis()
        return getProfessionalInstallationsRequiringReview(currentTime) +
               getProfessionalInstallationsRequiringFollowUp() +
               getProfessionalInstallationsRequiringAdvocateNotification()
    }

    suspend fun getAssessmentsRequiringAttention(): List<ClinicalAssessment> {
        val currentTime = System.currentTimeMillis()
        return getClinicalAssessmentsDue(currentTime) +
               getClinicalAssessmentsRequiringFollowUp() +
               getClinicalAssessmentsRecommendingAdvocate()
    }
}
