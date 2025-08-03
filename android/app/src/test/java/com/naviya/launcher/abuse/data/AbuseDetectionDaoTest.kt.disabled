package com.naviya.launcher.abuse.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naviya.launcher.database.NaviyaDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

/**
 * Comprehensive unit tests for AbuseDetectionDao
 * Tests all database operations for abuse detection system
 */
@RunWith(AndroidJUnit4::class)
class AbuseDetectionDaoTest {

    private lateinit var database: NaviyaDatabase
    private lateinit var abuseDao: AbuseDetectionDao

    private val testUserId = "test-user-123"
    private val testCaregiverId = "test-caregiver-456"
    private val currentTime = System.currentTimeMillis()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NaviyaDatabase::class.java
        ).allowMainThreadQueries().build()
        
        abuseDao = database.abuseDetectionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== RISK ASSESSMENT TESTS ====================

    @Test
    fun `test insert and retrieve risk assessment`() = runTest {
        // Given: A risk assessment
        val assessment = createTestRiskAssessment()

        // When: Inserting the assessment
        abuseDao.insertRiskAssessment(assessment)

        // Then: Should retrieve the assessment
        val retrieved = abuseDao.getRiskAssessment(assessment.assessmentId)
        assertNotNull("Should retrieve assessment", retrieved)
        assertEquals("Should match caregiver ID", testCaregiverId, retrieved?.caregiverId)
        assertEquals("Should match user ID", testUserId, retrieved?.userId)
        assertEquals("Should match risk score", 75, retrieved?.riskScore)
        assertEquals("Should match risk level", AbuseRiskLevel.HIGH, retrieved?.riskLevel)
    }

    @Test
    fun `test get recent risk assessments with time window`() = runTest {
        // Given: Multiple assessments across different time periods
        val recentAssessment = createTestRiskAssessment(
            assessmentId = "recent-1",
            timestamp = currentTime - TimeUnit.HOURS.toMillis(2)
        )
        val oldAssessment = createTestRiskAssessment(
            assessmentId = "old-1",
            timestamp = currentTime - TimeUnit.DAYS.toMillis(8) // Outside 7-day window
        )

        abuseDao.insertRiskAssessment(recentAssessment)
        abuseDao.insertRiskAssessment(oldAssessment)

        // When: Getting recent assessments (7 days)
        val recentAssessments = abuseDao.getRecentRiskAssessments(
            testCaregiverId,
            testUserId,
            TimeUnit.DAYS.toMillis(7)
        )

        // Then: Should only return recent assessments
        assertEquals("Should return only recent assessment", 1, recentAssessments.size)
        assertEquals("Should return the recent assessment", "recent-1", recentAssessments[0].assessmentId)
    }

    @Test
    fun `test get risk assessments by level`() = runTest {
        // Given: Assessments with different risk levels
        val highRiskAssessment = createTestRiskAssessment(
            assessmentId = "high-risk",
            riskScore = 85,
            riskLevel = AbuseRiskLevel.HIGH
        )
        val lowRiskAssessment = createTestRiskAssessment(
            assessmentId = "low-risk",
            riskScore = 20,
            riskLevel = AbuseRiskLevel.LOW
        )

        abuseDao.insertRiskAssessment(highRiskAssessment)
        abuseDao.insertRiskAssessment(lowRiskAssessment)

        // When: Getting high-risk assessments
        val highRiskAssessments = abuseDao.getRiskAssessmentsByLevel(
            testCaregiverId,
            testUserId,
            AbuseRiskLevel.HIGH
        )

        // Then: Should return only high-risk assessments
        assertEquals("Should return only high-risk assessment", 1, highRiskAssessments.size)
        assertEquals("Should return the high-risk assessment", "high-risk", highRiskAssessments[0].assessmentId)
        assertEquals("Should have high risk level", AbuseRiskLevel.HIGH, highRiskAssessments[0].riskLevel)
    }

    @Test
    fun `test get escalating risk pattern`() = runTest {
        // Given: Assessments showing escalating risk pattern
        val assessments = listOf(
            createTestRiskAssessment("escalate-1", 30, AbuseRiskLevel.LOW, currentTime - TimeUnit.DAYS.toMillis(3)),
            createTestRiskAssessment("escalate-2", 55, AbuseRiskLevel.MEDIUM, currentTime - TimeUnit.DAYS.toMillis(2)),
            createTestRiskAssessment("escalate-3", 80, AbuseRiskLevel.HIGH, currentTime - TimeUnit.DAYS.toMillis(1))
        )

        assessments.forEach { abuseDao.insertRiskAssessment(it) }

        // When: Getting escalating pattern
        val escalatingAssessments = abuseDao.getEscalatingRiskPattern(
            testCaregiverId,
            testUserId,
            TimeUnit.DAYS.toMillis(7)
        )

        // Then: Should return assessments in chronological order
        assertEquals("Should return all escalating assessments", 3, escalatingAssessments.size)
        assertTrue("Should be in chronological order", 
                  escalatingAssessments[0].riskScore < escalatingAssessments[1].riskScore)
        assertTrue("Should show escalation", 
                  escalatingAssessments[1].riskScore < escalatingAssessments[2].riskScore)
    }

    // ==================== ABUSE ALERT TESTS ====================

    @Test
    fun `test insert and retrieve abuse alert`() = runTest {
        // Given: An abuse alert
        val alert = createTestAbuseAlert()

        // When: Inserting the alert
        abuseDao.insertAbuseAlert(alert)

        // Then: Should retrieve the alert
        val retrieved = abuseDao.getAbuseAlert(alert.alertId)
        assertNotNull("Should retrieve alert", retrieved)
        assertEquals("Should match caregiver ID", testCaregiverId, retrieved?.caregiverId)
        assertEquals("Should match user ID", testUserId, retrieved?.userId)
        assertEquals("Should match risk level", AbuseRiskLevel.HIGH, retrieved?.riskLevel)
        assertTrue("Should require immediate action", retrieved?.requiresImmedateAction == true)
    }

    @Test
    fun `test get active alerts`() = runTest {
        // Given: Active and resolved alerts
        val activeAlert = createTestAbuseAlert(
            alertId = "active-alert",
            status = AbuseAlertStatus.ACTIVE
        )
        val resolvedAlert = createTestAbuseAlert(
            alertId = "resolved-alert",
            status = AbuseAlertStatus.RESOLVED
        )

        abuseDao.insertAbuseAlert(activeAlert)
        abuseDao.insertAbuseAlert(resolvedAlert)

        // When: Getting active alerts
        val activeAlerts = abuseDao.getActiveAlerts(testUserId)

        // Then: Should return only active alerts
        assertEquals("Should return only active alert", 1, activeAlerts.size)
        assertEquals("Should return the active alert", "active-alert", activeAlerts[0].alertId)
        assertEquals("Should have active status", AbuseAlertStatus.ACTIVE, activeAlerts[0].status)
    }

    @Test
    fun `test get alerts requiring immediate action`() = runTest {
        // Given: Alerts with different urgency levels
        val immediateAlert = createTestAbuseAlert(
            alertId = "immediate-alert",
            requiresImmedateAction = true
        )
        val routineAlert = createTestAbuseAlert(
            alertId = "routine-alert",
            requiresImmedateAction = false
        )

        abuseDao.insertAbuseAlert(immediateAlert)
        abuseDao.insertAbuseAlert(routineAlert)

        // When: Getting alerts requiring immediate action
        val immediateAlerts = abuseDao.getAlertsRequiringImmediateAction(testUserId)

        // Then: Should return only immediate alerts
        assertEquals("Should return only immediate alert", 1, immediateAlerts.size)
        assertEquals("Should return the immediate alert", "immediate-alert", immediateAlerts[0].alertId)
        assertTrue("Should require immediate action", immediateAlerts[0].requiresImmedateAction)
    }

    @Test
    fun `test update alert status`() = runTest {
        // Given: An active alert
        val alert = createTestAbuseAlert(status = AbuseAlertStatus.ACTIVE)
        abuseDao.insertAbuseAlert(alert)

        // When: Updating alert status to resolved
        abuseDao.updateAlertStatus(alert.alertId, AbuseAlertStatus.RESOLVED)

        // Then: Should update the status
        val updated = abuseDao.getAbuseAlert(alert.alertId)
        assertEquals("Should have resolved status", AbuseAlertStatus.RESOLVED, updated?.status)
    }

    @Test
    fun `test update alert resolution`() = runTest {
        // Given: An unresolved alert
        val alert = createTestAbuseAlert()
        abuseDao.insertAbuseAlert(alert)

        // When: Adding resolution details
        val resolutionDetails = "Alert resolved by elder rights advocate intervention"
        val resolutionTime = currentTime + TimeUnit.HOURS.toMillis(2)
        
        abuseDao.updateAlertResolution(alert.alertId, resolutionDetails, resolutionTime)

        // Then: Should update resolution details
        val updated = abuseDao.getAbuseAlert(alert.alertId)
        assertEquals("Should have resolution details", resolutionDetails, updated?.resolutionDetails)
        assertEquals("Should have resolution timestamp", resolutionTime, updated?.resolutionTimestamp)
    }

    // ==================== DETECTION RULE TESTS ====================

    @Test
    fun `test insert and retrieve detection rule`() = runTest {
        // Given: A detection rule
        val rule = createTestDetectionRule()

        // When: Inserting the rule
        abuseDao.insertDetectionRule(rule)

        // Then: Should retrieve the rule
        val retrieved = abuseDao.getDetectionRule(rule.ruleId)
        assertNotNull("Should retrieve rule", retrieved)
        assertEquals("Should match rule name", "contact_manipulation", retrieved?.ruleName)
        assertEquals("Should match rule type", AbuseRuleType.CONTACT_MANIPULATION, retrieved?.ruleType)
        assertTrue("Should be enabled", retrieved?.isEnabled == true)
    }

    @Test
    fun `test get enabled rules`() = runTest {
        // Given: Enabled and disabled rules
        val enabledRule = createTestDetectionRule(
            ruleId = "enabled-rule",
            isEnabled = true
        )
        val disabledRule = createTestDetectionRule(
            ruleId = "disabled-rule",
            isEnabled = false
        )

        abuseDao.insertDetectionRule(enabledRule)
        abuseDao.insertDetectionRule(disabledRule)

        // When: Getting enabled rules
        val enabledRules = abuseDao.getEnabledRules()

        // Then: Should return only enabled rules
        assertEquals("Should return only enabled rule", 1, enabledRules.size)
        assertEquals("Should return the enabled rule", "enabled-rule", enabledRules[0].ruleId)
        assertTrue("Should be enabled", enabledRules[0].isEnabled)
    }

    @Test
    fun `test get rules by type`() = runTest {
        // Given: Rules of different types
        val contactRule = createTestDetectionRule(
            ruleId = "contact-rule",
            ruleType = AbuseRuleType.CONTACT_MANIPULATION
        )
        val permissionRule = createTestDetectionRule(
            ruleId = "permission-rule",
            ruleType = AbuseRuleType.PERMISSION_ESCALATION
        )

        abuseDao.insertDetectionRule(contactRule)
        abuseDao.insertDetectionRule(permissionRule)

        // When: Getting contact manipulation rules
        val contactRules = abuseDao.getRulesByType(AbuseRuleType.CONTACT_MANIPULATION)

        // Then: Should return only contact rules
        assertEquals("Should return only contact rule", 1, contactRules.size)
        assertEquals("Should return the contact rule", "contact-rule", contactRules[0].ruleId)
        assertEquals("Should have contact type", AbuseRuleType.CONTACT_MANIPULATION, contactRules[0].ruleType)
    }

    @Test
    fun `test update rule configuration`() = runTest {
        // Given: A detection rule
        val rule = createTestDetectionRule()
        abuseDao.insertDetectionRule(rule)

        // When: Updating rule configuration
        val newConfig = mapOf(
            "threshold" to "5",
            "severity_multiplier" to "1.5"
        )
        abuseDao.updateRuleConfiguration(rule.ruleId, newConfig)

        // Then: Should update the configuration
        val updated = abuseDao.getDetectionRule(rule.ruleId)
        assertEquals("Should have updated threshold", "5", updated?.configuration?.get("threshold"))
        assertEquals("Should have updated multiplier", "1.5", updated?.configuration?.get("severity_multiplier"))
    }

    // ==================== STATISTICS AND ANALYTICS TESTS ====================

    @Test
    fun `test get abuse statistics summary`() = runTest {
        // Given: Multiple assessments and alerts
        val assessments = listOf(
            createTestRiskAssessment("assess-1", 85, AbuseRiskLevel.HIGH),
            createTestRiskAssessment("assess-2", 55, AbuseRiskLevel.MEDIUM),
            createTestRiskAssessment("assess-3", 25, AbuseRiskLevel.LOW)
        )
        val alerts = listOf(
            createTestAbuseAlert("alert-1", AbuseRiskLevel.HIGH),
            createTestAbuseAlert("alert-2", AbuseRiskLevel.MEDIUM)
        )

        assessments.forEach { abuseDao.insertRiskAssessment(it) }
        alerts.forEach { abuseDao.insertAbuseAlert(it) }

        // When: Getting statistics summary
        val stats = abuseDao.getAbuseStatisticsSummary(testUserId, TimeUnit.DAYS.toMillis(7))

        // Then: Should return correct statistics
        assertNotNull("Should return statistics", stats)
        assertEquals("Should count total assessments", 3, stats?.totalAssessments)
        assertEquals("Should count total alerts", 2, stats?.totalAlerts)
        assertEquals("Should count high risk assessments", 1, stats?.highRiskAssessments)
        assertEquals("Should calculate average risk score", 55, stats?.averageRiskScore) // (85+55+25)/3 = 55
    }

    @Test
    fun `test get caregiver risk trend`() = runTest {
        // Given: Assessments over time showing trend
        val trendAssessments = listOf(
            createTestRiskAssessment("trend-1", 30, AbuseRiskLevel.LOW, currentTime - TimeUnit.DAYS.toMillis(6)),
            createTestRiskAssessment("trend-2", 45, AbuseRiskLevel.MEDIUM, currentTime - TimeUnit.DAYS.toMillis(4)),
            createTestRiskAssessment("trend-3", 60, AbuseRiskLevel.MEDIUM, currentTime - TimeUnit.DAYS.toMillis(2)),
            createTestRiskAssessment("trend-4", 80, AbuseRiskLevel.HIGH, currentTime)
        )

        trendAssessments.forEach { abuseDao.insertRiskAssessment(it) }

        // When: Getting risk trend
        val trend = abuseDao.getCaregiverRiskTrend(
            testCaregiverId,
            testUserId,
            TimeUnit.DAYS.toMillis(7)
        )

        // Then: Should return trend data
        assertEquals("Should return all trend points", 4, trend.size)
        assertTrue("Should show increasing trend", trend[0].riskScore < trend[3].riskScore)
        assertEquals("Should be in chronological order", "trend-1", trend[0].assessmentId)
        assertEquals("Should end with latest", "trend-4", trend[3].assessmentId)
    }

    @Test
    fun `test get most frequent risk factors`() = runTest {
        // Given: Assessments with various risk factors
        val assessmentWithContactRisk = createTestRiskAssessment(
            assessmentId = "contact-risk",
            riskFactors = listOf(
                createRiskFactor(AbuseRiskFactorType.CONTACT_MANIPULATION, 30),
                createRiskFactor(AbuseRiskFactorType.BURST_ACTIVITY, 20)
            )
        )
        val assessmentWithPermissionRisk = createTestRiskAssessment(
            assessmentId = "permission-risk",
            riskFactors = listOf(
                createRiskFactor(AbuseRiskFactorType.PERMISSION_ESCALATION, 25),
                createRiskFactor(AbuseRiskFactorType.CONTACT_MANIPULATION, 35) // Contact manipulation appears again
            )
        )

        abuseDao.insertRiskAssessment(assessmentWithContactRisk)
        abuseDao.insertRiskAssessment(assessmentWithPermissionRisk)

        // When: Getting most frequent risk factors
        val frequentFactors = abuseDao.getMostFrequentRiskFactors(
            testCaregiverId,
            testUserId,
            TimeUnit.DAYS.toMillis(7)
        )

        // Then: Should return factors ordered by frequency
        assertTrue("Should include contact manipulation", 
                  frequentFactors.any { it.factorType == AbuseRiskFactorType.CONTACT_MANIPULATION })
        
        // Contact manipulation should appear twice, making it most frequent
        val contactManipulation = frequentFactors.find { 
            it.factorType == AbuseRiskFactorType.CONTACT_MANIPULATION 
        }
        assertNotNull("Should find contact manipulation factor", contactManipulation)
    }

    // ==================== SYSTEM HEALTH TESTS ====================

    @Test
    fun `test get detection system health`() = runTest {
        // Given: Various system activities
        val recentAssessment = createTestRiskAssessment(
            timestamp = currentTime - TimeUnit.MINUTES.toMillis(30)
        )
        val recentAlert = createTestAbuseAlert(
            timestamp = currentTime - TimeUnit.MINUTES.toMillis(15)
        )

        abuseDao.insertRiskAssessment(recentAssessment)
        abuseDao.insertAbuseAlert(recentAlert)

        // When: Getting system health
        val health = abuseDao.getDetectionSystemHealth()

        // Then: Should return health metrics
        assertNotNull("Should return health metrics", health)
        assertTrue("Should have recent activity", health?.lastAssessmentTime != null)
        assertTrue("Should have recent alerts", health?.lastAlertTime != null)
        assertTrue("Should be healthy", health?.isHealthy == true)
    }

    @Test
    fun `test cleanup old data`() = runTest {
        // Given: Old and recent data
        val oldAssessment = createTestRiskAssessment(
            assessmentId = "old-assessment",
            timestamp = currentTime - TimeUnit.DAYS.toMillis(400) // Very old
        )
        val recentAssessment = createTestRiskAssessment(
            assessmentId = "recent-assessment",
            timestamp = currentTime - TimeUnit.DAYS.toMillis(30) // Recent
        )

        abuseDao.insertRiskAssessment(oldAssessment)
        abuseDao.insertRiskAssessment(recentAssessment)

        // When: Cleaning up data older than 365 days
        val deletedCount = abuseDao.cleanupOldData(TimeUnit.DAYS.toMillis(365))

        // Then: Should delete old data but keep recent
        assertTrue("Should delete some old data", deletedCount > 0)
        
        val remainingAssessments = abuseDao.getRecentRiskAssessments(
            testCaregiverId,
            testUserId,
            TimeUnit.DAYS.toMillis(365)
        )
        assertEquals("Should keep recent assessment", 1, remainingAssessments.size)
        assertEquals("Should keep the recent one", "recent-assessment", remainingAssessments[0].assessmentId)
    }

    // ==================== HELPER METHODS ====================

    private fun createTestRiskAssessment(
        assessmentId: String = "test-assessment-${System.nanoTime()}",
        riskScore: Int = 75,
        riskLevel: AbuseRiskLevel = AbuseRiskLevel.HIGH,
        timestamp: Long = currentTime,
        riskFactors: List<AbuseRiskFactor> = listOf(
            createRiskFactor(AbuseRiskFactorType.CONTACT_MANIPULATION, 50),
            createRiskFactor(AbuseRiskFactorType.PERMISSION_ESCALATION, 25)
        )
    ): AbuseRiskAssessment {
        return AbuseRiskAssessment(
            assessmentId = assessmentId,
            caregiverId = testCaregiverId,
            userId = testUserId,
            riskScore = riskScore,
            riskLevel = riskLevel,
            riskFactors = riskFactors,
            assessmentTimestamp = timestamp,
            behaviorData = createTestBehaviorData()
        )
    }

    private fun createTestAbuseAlert(
        alertId: String = "test-alert-${System.nanoTime()}",
        riskLevel: AbuseRiskLevel = AbuseRiskLevel.HIGH,
        status: AbuseAlertStatus = AbuseAlertStatus.ACTIVE,
        requiresImmedateAction: Boolean = true,
        timestamp: Long = currentTime
    ): AbuseAlert {
        return AbuseAlert(
            alertId = alertId,
            caregiverId = testCaregiverId,
            userId = testUserId,
            riskLevel = riskLevel,
            alertType = AbuseAlertType.PATTERN_DETECTED,
            message = "Test abuse pattern detected",
            recommendedActions = listOf("Contact elder rights advocate", "Review permissions"),
            requiresImmedateAction = requiresImmedateAction,
            status = status,
            createdTimestamp = timestamp,
            notificationsSent = emptyList(),
            resolutionDetails = null,
            resolutionTimestamp = null
        )
    }

    private fun createTestDetectionRule(
        ruleId: String = "test-rule-${System.nanoTime()}",
        ruleName: String = "contact_manipulation",
        ruleType: AbuseRuleType = AbuseRuleType.CONTACT_MANIPULATION,
        isEnabled: Boolean = true
    ): AbuseDetectionRule {
        return AbuseDetectionRule(
            ruleId = ruleId,
            ruleName = ruleName,
            ruleType = ruleType,
            description = "Test rule for detecting contact manipulation patterns",
            isEnabled = isEnabled,
            configuration = mapOf(
                "threshold" to "3",
                "severity_multiplier" to "1.0"
            ),
            createdTimestamp = currentTime,
            lastModifiedTimestamp = currentTime
        )
    }

    private fun createRiskFactor(
        factorType: AbuseRiskFactorType,
        score: Int
    ): AbuseRiskFactor {
        return AbuseRiskFactor(
            factorType = factorType,
            score = score,
            severity = when {
                score >= 80 -> AbuseSeverity.CRITICAL
                score >= 60 -> AbuseSeverity.HIGH
                score >= 40 -> AbuseSeverity.MEDIUM
                score >= 20 -> AbuseSeverity.LOW
                else -> AbuseSeverity.MINIMAL
            },
            evidence = mapOf(
                "pattern" to "test_pattern",
                "count" to score.toString()
            ),
            description = "Test risk factor for $factorType"
        )
    }

    private fun createTestBehaviorData(): CaregiverBehaviorData {
        return CaregiverBehaviorData(
            caregiverId = testCaregiverId,
            userId = testUserId,
            analysisTimeWindow = TimeUnit.DAYS.toMillis(7),
            contactModificationAttempts = emptyList(),
            permissionHistory = emptyList(),
            emergencyInteractions = emptyList(),
            previousAssessments = emptyList()
        )
    }
}
