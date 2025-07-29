package com.naviya.launcher.healthcare

import com.naviya.launcher.healthcare.data.*
import com.naviya.launcher.elderrights.ElderRightsAdvocateNotificationService
import com.naviya.launcher.elderrights.data.ElderRightsDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.*

/**
 * Unit tests for HealthcareProfessionalIntegrationManager
 * Tests professional registration, installation, clinical oversight, and assessment workflows
 */
@ExperimentalCoroutinesApi
class HealthcareProfessionalIntegrationManagerTest {

    @Mock
    private lateinit var healthcareProfessionalDao: HealthcareProfessionalDao

    @Mock
    private lateinit var elderRightsDao: ElderRightsDao

    @Mock
    private lateinit var elderRightsAdvocateService: ElderRightsAdvocateNotificationService

    private lateinit var healthcareProfessionalManager: HealthcareProfessionalIntegrationManager

    private val testUserId = "test-user-123"
    private val testProfessionalId = "prof-456"
    private val testRegistrationId = "reg-789"
    private val testInstallationId = "install-101"
    private val testOversightId = "oversight-202"
    private val testAssessmentId = "assess-303"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        healthcareProfessionalManager = HealthcareProfessionalIntegrationManager(
            healthcareProfessionalDao,
            elderRightsDao,
            elderRightsAdvocateService
        )
    }

    // ==================== PROFESSIONAL REGISTRATION TESTS ====================

    @Test
    fun `registerHealthcareProfessional with valid credentials should succeed`() = runTest {
        // Given
        val professionalDetails = createTestProfessionalDetails()
        val credentials = createTestCredentials()
        val institution = createTestInstitution()

        val expectedRegistration = HealthcareProfessionalRegistration(
            registrationId = testRegistrationId,
            professionalId = testProfessionalId,
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

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(null)

        // When
        val result = healthcareProfessionalManager.registerHealthcareProfessional(
            professionalDetails, credentials, institution
        )

        // Then
        assertTrue(result.success)
        assertEquals(testRegistrationId, result.registrationId)
        assertTrue(result.verificationRequired)
        verify(healthcareProfessionalDao).insertProfessionalRegistration(any())
    }

    @Test
    fun `registerHealthcareProfessional with existing professional should fail`() = runTest {
        // Given
        val professionalDetails = createTestProfessionalDetails()
        val credentials = createTestCredentials()
        val existingRegistration = createTestRegistration()

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(existingRegistration)

        // When
        val result = healthcareProfessionalManager.registerHealthcareProfessional(
            professionalDetails, credentials, null
        )

        // Then
        assertFalse(result.success)
        assertEquals("Professional already registered", result.message)
        verify(healthcareProfessionalDao, never()).insertProfessionalRegistration(any())
    }

    @Test
    fun `validateProfessionalCredentials with valid credentials should succeed`() = runTest {
        // Given
        val credentials = createTestCredentials()

        // When
        val result = healthcareProfessionalManager.validateProfessionalCredentials(credentials)

        // Then
        assertTrue(result.success)
        assertEquals("Credentials validated successfully", result.message)
    }

    @Test
    fun `validateProfessionalCredentials with expired credentials should fail`() = runTest {
        // Given
        val expiredCredentials = createTestCredentials().copy(
            credentialExpirationDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L),
            isValid = false
        )

        // When
        val result = healthcareProfessionalManager.validateProfessionalCredentials(expiredCredentials)

        // Then
        assertFalse(result.success)
        assertTrue(result.message.contains("expired"))
    }

    // ==================== PROFESSIONAL INSTALLATION TESTS ====================

    @Test
    fun `performProfessionalInstallation with authorized professional should succeed`() = runTest {
        // Given
        val authorizedRegistration = createTestRegistration().copy(
            installationAuthorized = true,
            status = ProfessionalRegistrationStatus.ACTIVE
        )
        val installationRequest = createTestInstallationRequest()

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(authorizedRegistration)
        whenever(healthcareProfessionalDao.getProfessionalInstallationsByUserId(testUserId))
            .thenReturn(emptyList())

        // When
        val result = healthcareProfessionalManager.performProfessionalInstallation(installationRequest)

        // Then
        assertTrue(result.success)
        assertEquals(testInstallationId, result.installationId)
        assertFalse(result.authorizationRequired)
        verify(healthcareProfessionalDao).insertProfessionalInstallation(any())
        verify(elderRightsAdvocateService).notifyElderRightsAdvocate(any(), any(), any())
    }

    @Test
    fun `performProfessionalInstallation with unauthorized professional should fail`() = runTest {
        // Given
        val unauthorizedRegistration = createTestRegistration().copy(
            installationAuthorized = false,
            status = ProfessionalRegistrationStatus.TRAINING_REQUIRED
        )
        val installationRequest = createTestInstallationRequest()

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(unauthorizedRegistration)

        // When
        val result = healthcareProfessionalManager.performProfessionalInstallation(installationRequest)

        // Then
        assertFalse(result.success)
        assertTrue(result.authorizationRequired)
        assertEquals("Professional not authorized for installation", result.message)
        verify(healthcareProfessionalDao, never()).insertProfessionalInstallation(any())
    }

    @Test
    fun `performProfessionalInstallation with existing installation should update`() = runTest {
        // Given
        val authorizedRegistration = createTestRegistration().copy(installationAuthorized = true)
        val existingInstallation = createTestInstallation()
        val installationRequest = createTestInstallationRequest()

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(authorizedRegistration)
        whenever(healthcareProfessionalDao.getProfessionalInstallationsByUserId(testUserId))
            .thenReturn(listOf(existingInstallation))

        // When
        val result = healthcareProfessionalManager.performProfessionalInstallation(installationRequest)

        // Then
        assertTrue(result.success)
        verify(healthcareProfessionalDao).updateProfessionalInstallation(any())
    }

    // ==================== CLINICAL OVERSIGHT TESTS ====================

    @Test
    fun `establishClinicalOversight with valid physician should succeed`() = runTest {
        // Given
        val validPhysician = createTestRegistration().copy(
            personalDetails = createTestProfessionalDetails().copy(
                professionalType = ProfessionalType.PHYSICIAN
            )
        )
        val oversightRequest = createTestOversightRequest()

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(validPhysician)
        whenever(healthcareProfessionalDao.getClinicalOversightByUserId(testUserId))
            .thenReturn(null)

        // When
        val result = healthcareProfessionalManager.establishClinicalOversight(testUserId, oversightRequest)

        // Then
        assertTrue(result.success)
        assertEquals(testOversightId, result.oversightId)
        assertEquals(ClinicalOversightLevel.STANDARD, result.oversightLevel)
        verify(healthcareProfessionalDao).insertClinicalOversight(any())
    }

    @Test
    fun `establishClinicalOversight with non-physician should fail`() = runTest {
        // Given
        val nonPhysician = createTestRegistration().copy(
            personalDetails = createTestProfessionalDetails().copy(
                professionalType = ProfessionalType.REGISTERED_NURSE
            )
        )
        val oversightRequest = createTestOversightRequest()

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(nonPhysician)

        // When
        val result = healthcareProfessionalManager.establishClinicalOversight(testUserId, oversightRequest)

        // Then
        assertFalse(result.success)
        assertTrue(result.message.contains("physician"))
        verify(healthcareProfessionalDao, never()).insertClinicalOversight(any())
    }

    @Test
    fun `establishClinicalOversight with existing oversight should update`() = runTest {
        // Given
        val validPhysician = createTestRegistration()
        val existingOversight = createTestOversight()
        val oversightRequest = createTestOversightRequest()

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(validPhysician)
        whenever(healthcareProfessionalDao.getClinicalOversightByUserId(testUserId))
            .thenReturn(existingOversight)

        // When
        val result = healthcareProfessionalManager.establishClinicalOversight(testUserId, oversightRequest)

        // Then
        assertTrue(result.success)
        verify(healthcareProfessionalDao).updateClinicalOversight(any())
    }

    // ==================== CLINICAL ASSESSMENT TESTS ====================

    @Test
    fun `performClinicalAssessment with valid data should succeed`() = runTest {
        // Given
        val validPhysician = createTestRegistration()
        val assessmentRequest = createTestAssessmentRequest()

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(validPhysician)

        // When
        val result = healthcareProfessionalManager.performClinicalAssessment(testUserId, assessmentRequest)

        // Then
        assertTrue(result.success)
        assertEquals(testAssessmentId, result.assessmentId)
        assertEquals(AbuseRiskLevel.MODERATE, result.abuseRiskLevel)
        assertTrue(result.followUpRequired)
        verify(healthcareProfessionalDao).insertClinicalAssessment(any())
    }

    @Test
    fun `performClinicalAssessment with high abuse risk should notify advocate`() = runTest {
        // Given
        val validPhysician = createTestRegistration()
        val highRiskAssessment = createTestAssessmentRequest().copy(
            riskFactorAssessment = createTestRiskFactorAssessment().copy(
                overallRiskLevel = OverallRiskLevel.HIGH,
                abuseRiskFactors = listOf("Financial exploitation", "Physical abuse indicators")
            )
        )

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(validPhysician)

        // When
        val result = healthcareProfessionalManager.performClinicalAssessment(testUserId, highRiskAssessment)

        // Then
        assertTrue(result.success)
        assertEquals(AbuseRiskLevel.HIGH, result.abuseRiskLevel)
        assertTrue(result.elderRightsAdvocateRecommended)
        verify(elderRightsAdvocateService).notifyElderRightsAdvocate(any(), any(), any())
    }

    @Test
    fun `performClinicalAssessment with critical risk should escalate immediately`() = runTest {
        // Given
        val validPhysician = createTestRegistration()
        val criticalRiskAssessment = createTestAssessmentRequest().copy(
            riskFactorAssessment = createTestRiskFactorAssessment().copy(
                overallRiskLevel = OverallRiskLevel.CRITICAL,
                abuseRiskFactors = listOf("Immediate physical danger", "Severe neglect")
            )
        )

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(validPhysician)

        // When
        val result = healthcareProfessionalManager.performClinicalAssessment(testUserId, criticalRiskAssessment)

        // Then
        assertTrue(result.success)
        assertEquals(AbuseRiskLevel.CRITICAL, result.abuseRiskLevel)
        assertTrue(result.elderRightsAdvocateRecommended)
        verify(elderRightsAdvocateService).escalateToEmergencyServices(any(), any())
    }

    // ==================== INTEGRATION WORKFLOW TESTS ====================

    @Test
    fun `complete professional installation workflow should succeed`() = runTest {
        // Given
        val professionalDetails = createTestProfessionalDetails()
        val credentials = createTestCredentials()
        val installationRequest = createTestInstallationRequest()
        val oversightRequest = createTestOversightRequest()
        val assessmentRequest = createTestAssessmentRequest()

        val authorizedRegistration = createTestRegistration().copy(
            installationAuthorized = true,
            status = ProfessionalRegistrationStatus.ACTIVE
        )

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(null, authorizedRegistration, authorizedRegistration, authorizedRegistration)
        whenever(healthcareProfessionalDao.getProfessionalInstallationsByUserId(testUserId))
            .thenReturn(emptyList())
        whenever(healthcareProfessionalDao.getClinicalOversightByUserId(testUserId))
            .thenReturn(null)

        // When - Step 1: Register Professional
        val registrationResult = healthcareProfessionalManager.registerHealthcareProfessional(
            professionalDetails, credentials, null
        )

        // Then
        assertTrue(registrationResult.success)

        // When - Step 2: Perform Installation
        val installationResult = healthcareProfessionalManager.performProfessionalInstallation(installationRequest)

        // Then
        assertTrue(installationResult.success)

        // When - Step 3: Establish Clinical Oversight
        val oversightResult = healthcareProfessionalManager.establishClinicalOversight(testUserId, oversightRequest)

        // Then
        assertTrue(oversightResult.success)

        // When - Step 4: Perform Clinical Assessment
        val assessmentResult = healthcareProfessionalManager.performClinicalAssessment(testUserId, assessmentRequest)

        // Then
        assertTrue(assessmentResult.success)

        // Verify all components were created
        verify(healthcareProfessionalDao).insertProfessionalRegistration(any())
        verify(healthcareProfessionalDao).insertProfessionalInstallation(any())
        verify(healthcareProfessionalDao).insertClinicalOversight(any())
        verify(healthcareProfessionalDao).insertClinicalAssessment(any())
        verify(elderRightsAdvocateService, atLeastOnce()).notifyElderRightsAdvocate(any(), any(), any())
    }

    @Test
    fun `generateInstallationReport should include all components`() = runTest {
        // Given
        val installation = createTestInstallation()
        val registration = createTestRegistration()
        val assessment = createTestAssessment()
        val oversight = createTestOversight()

        whenever(healthcareProfessionalDao.getProfessionalInstallationById(testInstallationId))
            .thenReturn(installation)
        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(testProfessionalId))
            .thenReturn(registration)
        whenever(healthcareProfessionalDao.getLatestValidClinicalAssessment(testUserId))
            .thenReturn(assessment)
        whenever(healthcareProfessionalDao.getClinicalOversightByUserId(testUserId))
            .thenReturn(oversight)

        // When
        val report = healthcareProfessionalManager.generateInstallationReport(testInstallationId)

        // Then
        assertNotNull(report)
        assertEquals(testInstallationId, report?.installationId)
        assertEquals(installation, report?.installation)
        assertEquals(registration, report?.professional)
        assertEquals(assessment, report?.clinicalAssessment)
        assertEquals(oversight, report?.clinicalOversight)
        assertTrue(report?.qualityMetrics?.isNotEmpty() == true)
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    fun `operations should handle database exceptions gracefully`() = runTest {
        // Given
        val professionalDetails = createTestProfessionalDetails()
        val credentials = createTestCredentials()

        whenever(healthcareProfessionalDao.getProfessionalRegistrationByProfessionalId(any()))
            .thenThrow(RuntimeException("Database error"))

        // When
        val result = healthcareProfessionalManager.registerHealthcareProfessional(
            professionalDetails, credentials, null
        )

        // Then
        assertFalse(result.success)
        assertTrue(result.message.contains("error"))
        assertNotNull(result.error)
    }

    // ==================== HELPER METHODS ====================

    private fun createTestProfessionalDetails() = HealthcareProfessionalDetails(
        professionalId = testProfessionalId,
        firstName = "Dr. Jane",
        lastName = "Smith",
        professionalType = ProfessionalType.PHYSICIAN,
        specializations = listOf("Geriatrics", "Internal Medicine"),
        licenseNumbers = listOf("MD123456"),
        contactInformation = ProfessionalContactInfo(
            primaryPhone = "+1-555-0123",
            email = "dr.smith@hospital.com",
            officeAddress = "123 Medical Center Dr",
            emergencyContact = "+1-555-0124"
        ),
        yearsOfExperience = 15,
        elderCareExperience = 8
    )

    private fun createTestCredentials() = ProfessionalCredentials(
        professionalType = ProfessionalType.PHYSICIAN,
        licenseNumbers = listOf("MD123456"),
        boardCertifications = listOf("Internal Medicine", "Geriatrics"),
        specializations = listOf("Elder Care"),
        institutionalAffiliations = listOf("General Hospital"),
        malpracticeInsurance = InsuranceRecord(
            insuranceProvider = "Medical Malpractice Inc",
            policyNumber = "MP123456",
            coverageAmount = 1000000.0,
            effectiveDate = System.currentTimeMillis(),
            expirationDate = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L),
            isActive = true
        ),
        backgroundCheckStatus = BackgroundCheckStatus.CLEARED,
        continuingEducationStatus = ContinuingEducationStatus.CURRENT,
        credentialVerificationDate = System.currentTimeMillis(),
        credentialExpirationDate = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L),
        isValid = true
    )

    private fun createTestInstitution() = HealthcareInstitution(
        institutionId = "inst-123",
        institutionName = "General Hospital",
        institutionType = InstitutionType.HOSPITAL,
        address = "456 Hospital Ave",
        contactInformation = "+1-555-0200",
        accreditation = listOf("Joint Commission"),
        specializations = listOf("Geriatrics", "Emergency Medicine"),
        elderCareServices = listOf("Geriatric Assessment", "Elder Abuse Prevention"),
        isActive = true
    )

    private fun createTestRegistration() = HealthcareProfessionalRegistration(
        registrationId = testRegistrationId,
        professionalId = testProfessionalId,
        personalDetails = createTestProfessionalDetails(),
        credentials = createTestCredentials(),
        institutionAffiliation = createTestInstitution(),
        registrationTimestamp = System.currentTimeMillis(),
        status = ProfessionalRegistrationStatus.ACTIVE,
        certificationStatus = CertificationStatus.CERTIFIED,
        clinicalOversightLevel = ClinicalOversightLevel.STANDARD,
        lastReviewDate = System.currentTimeMillis(),
        nextRecertificationDate = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L),
        installationAuthorized = true,
        trainingCompleted = true,
        backgroundCheckCompleted = true,
        ethicsTrainingCompleted = true
    )

    private fun createTestInstallationRequest() = ProfessionalInstallationRequest(
        userId = testUserId,
        professionalId = testProfessionalId,
        institutionId = "inst-123",
        installationType = InstallationType.INITIAL_INSTALLATION,
        clinicalContext = ClinicalContext.HOME_CARE,
        patientConsent = ConsentRecord(
            consentId = "consent-123",
            consentType = ConsentType.TREATMENT_CONSENT,
            consentTimestamp = System.currentTimeMillis(),
            consentMethod = ConsentMethod.WRITTEN_SIGNATURE,
            consentText = "I consent to professional installation",
            isValid = true
        ),
        familyConsent = null,
        initialClinicalNotes = "Initial installation for elder protection"
    )

    private fun createTestInstallation() = ProfessionalInstallation(
        installationId = testInstallationId,
        userId = testUserId,
        professionalId = testProfessionalId,
        institutionId = "inst-123",
        installationTimestamp = System.currentTimeMillis(),
        installationType = InstallationType.INITIAL_INSTALLATION,
        clinicalContext = ClinicalContext.HOME_CARE,
        patientConsent = ConsentRecord(
            consentId = "consent-123",
            consentType = ConsentType.TREATMENT_CONSENT,
            consentTimestamp = System.currentTimeMillis(),
            consentMethod = ConsentMethod.WRITTEN_SIGNATURE,
            consentText = "I consent to professional installation",
            isValid = true
        ),
        familyConsent = null,
        installationStatus = InstallationStatus.COMPLETED,
        clinicalNotes = "Installation completed successfully",
        nextReviewDate = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L),
        elderRightsAdvocateInformed = true,
        clinicalAssessmentCompleted = true,
        systemConfigurationCompleted = true
    )

    private fun createTestOversightRequest() = ClinicalOversightRequest(
        primaryPhysicianId = testProfessionalId,
        specialistIds = emptyList(),
        institutionId = "inst-123",
        requestedOversightLevel = ClinicalOversightLevel.STANDARD
    )

    private fun createTestOversight() = ClinicalOversight(
        oversightId = testOversightId,
        userId = testUserId,
        primaryPhysicianId = testProfessionalId,
        specialistIds = emptyList(),
        institutionId = "inst-123",
        oversightLevel = ClinicalOversightLevel.STANDARD,
        establishmentTimestamp = System.currentTimeMillis(),
        clinicalProtocols = listOf("Elder Abuse Detection", "Safety Monitoring"),
        monitoringFrequency = MonitoringFrequency.WEEKLY,
        alertThresholds = mapOf("abuse_risk" to 0.7),
        escalationProcedures = listOf("Notify Elder Rights Advocate", "Contact Emergency Services"),
        qualityMetrics = listOf("Response Time", "Assessment Accuracy"),
        patientSafetyMeasures = listOf("Regular Check-ins", "Emergency Contacts"),
        clinicalGovernance = "Hospital Quality Committee",
        lastReviewDate = System.currentTimeMillis(),
        nextReviewDate = System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000L)
    )

    private fun createTestAssessmentRequest() = ClinicalAssessmentRequest(
        assessingPhysicianId = testProfessionalId,
        assessmentType = ClinicalAssessmentType.INITIAL_ASSESSMENT,
        cognitiveAssessment = createTestCognitiveAssessment(),
        functionalAssessment = createTestFunctionalAssessment(),
        socialAssessment = createTestSocialAssessment(),
        riskFactorAssessment = createTestRiskFactorAssessment(),
        caregiverAssessment = createTestCaregiverAssessment(),
        familyDynamicsAssessment = createTestFamilyDynamicsAssessment(),
        clinicalNotes = "Initial assessment completed"
    )

    private fun createTestAssessment() = ClinicalAssessment(
        assessmentId = testAssessmentId,
        userId = testUserId,
        assessingPhysicianId = testProfessionalId,
        assessmentTimestamp = System.currentTimeMillis(),
        assessmentType = ClinicalAssessmentType.INITIAL_ASSESSMENT,
        cognitiveAssessment = createTestCognitiveAssessment(),
        functionalAssessment = createTestFunctionalAssessment(),
        socialAssessment = createTestSocialAssessment(),
        riskFactorAssessment = createTestRiskFactorAssessment(),
        abuseRiskLevel = AbuseRiskLevel.MODERATE,
        protectionRecommendations = listOf("Regular monitoring", "Family education"),
        monitoringRecommendations = listOf("Weekly check-ins", "Monthly assessments"),
        caregiverAssessment = createTestCaregiverAssessment(),
        familyDynamicsAssessment = createTestFamilyDynamicsAssessment(),
        elderRightsAdvocateRecommended = false,
        followUpRequired = true,
        nextAssessmentDate = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L),
        clinicalNotes = "Patient shows moderate risk factors"
    )

    private fun createTestCognitiveAssessment() = CognitiveAssessment(
        assessmentTool = "MMSE",
        score = 26.0,
        interpretation = "Mild cognitive impairment",
        cognitiveImpairmentLevel = CognitiveImpairmentLevel.MILD,
        decisionMakingCapacity = DecisionMakingCapacity.PARTIAL_CAPACITY
    )

    private fun createTestFunctionalAssessment() = FunctionalAssessment(
        activitiesOfDailyLiving = mapOf(
            "bathing" to FunctionalLevel.REQUIRES_ASSISTANCE,
            "dressing" to FunctionalLevel.INDEPENDENT
        ),
        instrumentalActivitiesOfDailyLiving = mapOf(
            "medication_management" to FunctionalLevel.REQUIRES_ASSISTANCE,
            "financial_management" to FunctionalLevel.DEPENDENT
        ),
        mobilityStatus = MobilityStatus.USES_ASSISTIVE_DEVICE,
        fallRisk = FallRiskLevel.MODERATE
    )

    private fun createTestSocialAssessment() = SocialAssessment(
        socialSupport = SocialSupportLevel.MODERATE,
        socialIsolation = SocialIsolationLevel.MILD,
        familyDynamics = "Supportive family with some tension",
        caregiverRelationships = "Good relationship with primary caregiver",
        communityResources = listOf("Senior Center", "Meals on Wheels")
    )

    private fun createTestRiskFactorAssessment() = RiskFactorAssessment(
        abuseRiskFactors = listOf("Financial vulnerability", "Social isolation"),
        neglectRiskFactors = listOf("Medication management issues"),
        exploitationRiskFactors = listOf("Cognitive impairment"),
        overallRiskLevel = OverallRiskLevel.MODERATE,
        protectiveFactors = listOf("Family support", "Community involvement")
    )

    private fun createTestCaregiverAssessment() = CaregiverAssessment(
        caregiverType = CaregiverType.FAMILY_MEMBER,
        caregiverCapacity = CaregiverCapacity.GOOD,
        caregiverStress = CaregiverStressLevel.MODERATE,
        caregiverKnowledge = CaregiverKnowledgeLevel.ADEQUATE,
        caregiverSupport = CaregiverSupportLevel.GOOD
    )

    private fun createTestFamilyDynamicsAssessment() = FamilyDynamicsAssessment(
        familyStructure = "Adult children involved in care",
        familyRelationships = "Generally supportive with occasional conflicts",
        communicationPatterns = "Open communication with some barriers",
        conflictResolution = "Family meetings help resolve issues",
        familySupport = FamilySupportLevel.MODERATE
    )
}
