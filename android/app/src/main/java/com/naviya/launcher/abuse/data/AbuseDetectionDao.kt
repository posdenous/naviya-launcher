package com.naviya.launcher.abuse.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.naviya.launcher.abuse.*

/**
 * Data Access Object for rule-based abuse detection system
 * Handles all database operations for abuse risk assessments and alerts
 */
@Dao
interface AbuseDetectionDao {
    
    // Risk Assessment Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskAssessment(assessment: AbuseRiskAssessment): Long
    
    @Update
    suspend fun updateRiskAssessment(assessment: AbuseRiskAssessment)
    
    @Query("SELECT * FROM abuse_risk_assessments WHERE assessmentId = :assessmentId")
    suspend fun getRiskAssessment(assessmentId: String): AbuseRiskAssessment?
    
    @Query("SELECT * FROM abuse_risk_assessments WHERE caregiverId = :caregiverId AND userId = :userId ORDER BY assessmentTimestamp DESC LIMIT 1")
    suspend fun getLatestRiskAssessment(caregiverId: String, userId: String): AbuseRiskAssessment?
    
    @Query("SELECT * FROM abuse_risk_assessments WHERE caregiverId = :caregiverId AND userId = :userId AND assessmentTimestamp > :sinceTime ORDER BY assessmentTimestamp DESC")
    suspend fun getRecentRiskAssessments(
        caregiverId: String, 
        userId: String, 
        sinceTime: Long
    ): List<AbuseRiskAssessment>
    
    @Query("SELECT * FROM abuse_risk_assessments WHERE userId = :userId ORDER BY assessmentTimestamp DESC LIMIT :limit")
    suspend fun getUserRiskAssessments(userId: String, limit: Int = 20): List<AbuseRiskAssessment>
    
    @Query("SELECT * FROM abuse_risk_assessments WHERE caregiverId = :caregiverId ORDER BY assessmentTimestamp DESC LIMIT :limit")
    suspend fun getCaregiverRiskAssessments(caregiverId: String, limit: Int = 20): List<AbuseRiskAssessment>
    
    @Query("SELECT * FROM abuse_risk_assessments WHERE riskLevel IN (:riskLevels) ORDER BY assessmentTimestamp DESC")
    suspend fun getAssessmentsByRiskLevel(riskLevels: List<AbuseRiskLevel>): List<AbuseRiskAssessment>
    
    @Query("SELECT * FROM abuse_risk_assessments WHERE userId = :userId")
    fun getRiskAssessmentsFlow(userId: String): Flow<List<AbuseRiskAssessment>>
    
    @Query("DELETE FROM abuse_risk_assessments WHERE assessmentTimestamp < :cutoffTime")
    suspend fun cleanupOldAssessments(cutoffTime: Long)
    
    // Abuse Alert Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbuseAlert(alert: AbuseAlert): Long
    
    @Update
    suspend fun updateAbuseAlert(alert: AbuseAlert)
    
    @Query("SELECT * FROM abuse_alerts WHERE alertId = :alertId")
    suspend fun getAbuseAlert(alertId: String): AbuseAlert?
    
    @Query("SELECT * FROM abuse_alerts WHERE userId = :userId AND alertStatus = 'ACTIVE' ORDER BY alertTimestamp DESC")
    suspend fun getActiveAlerts(userId: String): List<AbuseAlert>
    
    @Query("SELECT * FROM abuse_alerts WHERE caregiverId = :caregiverId ORDER BY alertTimestamp DESC LIMIT :limit")
    suspend fun getCaregiverAlerts(caregiverId: String, limit: Int = 10): List<AbuseAlert>
    
    @Query("SELECT * FROM abuse_alerts WHERE riskLevel IN (:riskLevels) AND alertStatus = 'ACTIVE' ORDER BY alertTimestamp DESC")
    suspend fun getAlertsByRiskLevel(riskLevels: List<AbuseRiskLevel>): List<AbuseAlert>
    
    @Query("SELECT * FROM abuse_alerts WHERE requiresImmedateAction = 1 AND alertStatus = 'ACTIVE' ORDER BY alertTimestamp DESC")
    suspend fun getUrgentAlerts(): List<AbuseAlert>
    
    @Query("SELECT * FROM abuse_alerts WHERE userId = :userId ORDER BY alertTimestamp DESC LIMIT :limit")
    suspend fun getRecentAlerts(userId: String, limit: Int = 50): List<AbuseAlert>
    
    @Query("SELECT * FROM abuse_alerts WHERE userId = :userId AND alertStatus = 'ACTIVE'")
    fun getActiveAlertsFlow(userId: String): Flow<List<AbuseAlert>>
    
    @Query("UPDATE abuse_alerts SET alertStatus = :status, acknowledgedBy = :acknowledgedBy, acknowledgedAt = :timestamp WHERE alertId = :alertId")
    suspend fun acknowledgeAlert(alertId: String, status: AlertStatus, acknowledgedBy: String, timestamp: Long)
    
    @Query("UPDATE abuse_alerts SET alertStatus = 'RESOLVED', resolvedBy = :resolvedBy, resolvedAt = :timestamp, resolutionNotes = :notes WHERE alertId = :alertId")
    suspend fun resolveAlert(alertId: String, resolvedBy: String, timestamp: Long, notes: String?)
    
    @Query("DELETE FROM abuse_alerts WHERE alertStatus IN ('RESOLVED', 'FALSE_POSITIVE') AND resolvedAt < :cutoffTime")
    suspend fun cleanupResolvedAlerts(cutoffTime: Long)
    
    // Abuse Detection Rules Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectionRule(rule: AbuseDetectionRule): Long
    
    @Update
    suspend fun updateDetectionRule(rule: AbuseDetectionRule)
    
    @Query("SELECT * FROM abuse_detection_rules WHERE isActive = 1 ORDER BY ruleName")
    suspend fun getActiveDetectionRules(): List<AbuseDetectionRule>
    
    @Query("SELECT * FROM abuse_detection_rules WHERE ruleId = :ruleId")
    suspend fun getDetectionRule(ruleId: String): AbuseDetectionRule?
    
    @Query("SELECT * FROM abuse_detection_rules WHERE ruleType = :ruleType AND isActive = 1")
    suspend fun getRulesByType(ruleType: AbuseRiskFactorType): List<AbuseDetectionRule>
    
    @Query("UPDATE abuse_detection_rules SET isActive = 0 WHERE ruleId = :ruleId")
    suspend fun deactivateRule(ruleId: String)
    
    @Query("DELETE FROM abuse_detection_rules WHERE ruleId = :ruleId")
    suspend fun deleteDetectionRule(ruleId: String)
    
    // Abuse Detection Statistics Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectionStats(stats: AbuseDetectionStats): Long
    
    @Query("SELECT * FROM abuse_detection_stats WHERE userId = :userId AND periodType = :periodType ORDER BY periodStart DESC LIMIT 1")
    suspend fun getLatestUserStats(userId: String, periodType: String): AbuseDetectionStats?
    
    @Query("SELECT * FROM abuse_detection_stats WHERE caregiverId = :caregiverId AND periodType = :periodType ORDER BY periodStart DESC LIMIT 1")
    suspend fun getLatestCaregiverStats(caregiverId: String, periodType: String): AbuseDetectionStats?
    
    @Query("SELECT * FROM abuse_detection_stats WHERE userId IS NULL AND caregiverId IS NULL AND periodType = :periodType ORDER BY periodStart DESC LIMIT 1")
    suspend fun getLatestSystemStats(periodType: String): AbuseDetectionStats?
    
    @Query("DELETE FROM abuse_detection_stats WHERE recordedAt < :cutoffTime")
    suspend fun cleanupOldStats(cutoffTime: Long)
    
    // Analytics and Reporting Queries
    
    /**
     * Get abuse risk trend for a caregiver
     */
    @Query("""
        SELECT 
            assessmentTimestamp,
            riskScore,
            riskLevel
        FROM abuse_risk_assessments 
        WHERE caregiverId = :caregiverId AND userId = :userId 
            AND assessmentTimestamp > :sinceTime
        ORDER BY assessmentTimestamp ASC
    """)
    suspend fun getCaregiverRiskTrend(
        caregiverId: String, 
        userId: String, 
        sinceTime: Long
    ): List<RiskTrendPoint>
    
    /**
     * Get most common risk factors for a caregiver
     */
    @Query("""
        SELECT 
            COUNT(*) as frequency,
            riskFactors
        FROM abuse_risk_assessments 
        WHERE caregiverId = :caregiverId AND assessmentTimestamp > :sinceTime
        GROUP BY riskFactors
        ORDER BY frequency DESC
        LIMIT 10
    """)
    suspend fun getCommonRiskFactors(caregiverId: String, sinceTime: Long): List<RiskFactorFrequency>
    
    /**
     * Get system-wide abuse detection summary
     */
    @Query("""
        SELECT 
            COUNT(DISTINCT caregiverId) as totalCaregiversAnalyzed,
            COUNT(*) as totalAssessments,
            COUNT(CASE WHEN riskLevel = 'HIGH' OR riskLevel = 'CRITICAL' THEN 1 END) as highRiskAssessments,
            AVG(riskScore) as averageRiskScore,
            MAX(assessmentTimestamp) as lastAnalysis
        FROM abuse_risk_assessments 
        WHERE assessmentTimestamp > :sinceTime
    """)
    suspend fun getSystemSummary(sinceTime: Long): SystemAbuseDetectionSummary?
    
    /**
     * Get alert response statistics
     */
    @Query("""
        SELECT 
            COUNT(*) as totalAlerts,
            COUNT(CASE WHEN alertStatus = 'RESOLVED' THEN 1 END) as resolvedAlerts,
            COUNT(CASE WHEN alertStatus = 'FALSE_POSITIVE' THEN 1 END) as falsePositives,
            AVG(CASE WHEN resolvedAt IS NOT NULL THEN resolvedAt - alertTimestamp END) as averageResponseTime,
            COUNT(CASE WHEN elderRightsNotified = 1 THEN 1 END) as elderRightsNotifications
        FROM abuse_alerts 
        WHERE alertTimestamp > :sinceTime
    """)
    suspend fun getAlertResponseStats(sinceTime: Long): AlertResponseStats?
    
    /**
     * Get caregivers requiring immediate attention
     */
    @Query("""
        SELECT DISTINCT 
            ara.caregiverId,
            ara.userId,
            ara.riskLevel,
            ara.assessmentTimestamp,
            COUNT(aa.alertId) as activeAlerts
        FROM abuse_risk_assessments ara
        LEFT JOIN abuse_alerts aa ON ara.caregiverId = aa.caregiverId AND aa.alertStatus = 'ACTIVE'
        WHERE ara.riskLevel IN ('HIGH', 'CRITICAL')
            AND ara.assessmentTimestamp > :recentTime
        GROUP BY ara.caregiverId, ara.userId
        ORDER BY ara.riskLevel DESC, ara.assessmentTimestamp DESC
    """)
    suspend fun getCaregiversRequiringAttention(recentTime: Long): List<CaregiverAttentionSummary>
    
    /**
     * Get risk assessment history for a user
     */
    @Query("""
        SELECT 
            ara.*,
            COUNT(aa.alertId) as alertsGenerated
        FROM abuse_risk_assessments ara
        LEFT JOIN abuse_alerts aa ON ara.assessmentId = aa.alertId
        WHERE ara.userId = :userId
        ORDER BY ara.assessmentTimestamp DESC
        LIMIT :limit
    """)
    suspend fun getUserRiskHistory(userId: String, limit: Int = 20): List<RiskAssessmentWithAlerts>
    
    /**
     * Check if caregiver has recent high-risk assessments
     */
    @Query("""
        SELECT COUNT(*) > 0
        FROM abuse_risk_assessments 
        WHERE caregiverId = :caregiverId 
            AND riskLevel IN ('HIGH', 'CRITICAL')
            AND assessmentTimestamp > :recentTime
    """)
    suspend fun hasRecentHighRiskAssessments(caregiverId: String, recentTime: Long): Boolean
    
    /**
     * Get abuse pattern summary for reporting
     */
    @Query("""
        SELECT 
            riskLevel,
            COUNT(*) as assessmentCount,
            COUNT(DISTINCT caregiverId) as uniqueCaregivers,
            COUNT(DISTINCT userId) as uniqueUsers,
            AVG(riskScore) as averageScore
        FROM abuse_risk_assessments 
        WHERE assessmentTimestamp > :sinceTime
        GROUP BY riskLevel
        ORDER BY 
            CASE riskLevel 
                WHEN 'CRITICAL' THEN 5
                WHEN 'HIGH' THEN 4
                WHEN 'MEDIUM' THEN 3
                WHEN 'LOW' THEN 2
                ELSE 1
            END DESC
    """)
    suspend fun getAbusePatternSummary(sinceTime: Long): List<AbusePatternSummary>
    
    /**
     * Update alert with elder rights notification
     */
    @Query("UPDATE abuse_alerts SET elderRightsNotified = 1, elderRightsNotificationTime = :timestamp WHERE alertId = :alertId")
    suspend fun markElderRightsNotified(alertId: String, timestamp: Long)
    
    /**
     * Get unresolved high-priority alerts
     */
    @Query("""
        SELECT * FROM abuse_alerts 
        WHERE alertStatus IN ('ACTIVE', 'ACKNOWLEDGED')
            AND riskLevel IN ('HIGH', 'CRITICAL')
            AND alertTimestamp > :sinceTime
        ORDER BY 
            CASE riskLevel 
                WHEN 'CRITICAL' THEN 1
                WHEN 'HIGH' THEN 2
                ELSE 3
            END,
            alertTimestamp DESC
    """)
    suspend fun getUnresolvedHighPriorityAlerts(sinceTime: Long): List<AbuseAlert>
    
    /**
     * Cleanup all abuse detection data for a user
     */
    @Transaction
    suspend fun cleanupUserAbuseData(userId: String) {
        // Archive assessments instead of deleting for legal evidence
        @Query("UPDATE abuse_risk_assessments SET userId = 'archived_' || :userId WHERE userId = :userId")
        suspend fun archiveRiskAssessments(userId: String)
        
        // Archive alerts instead of deleting
        @Query("UPDATE abuse_alerts SET userId = 'archived_' || :userId WHERE userId = :userId")
        suspend fun archiveAlerts(userId: String)
        
        archiveRiskAssessments(userId)
        archiveAlerts(userId)
    }
    
    /**
     * Get detection system health metrics
     */
    @Query("""
        SELECT 
            COUNT(*) as totalAssessments,
            COUNT(DISTINCT caregiverId) as activeCaregivers,
            COUNT(CASE WHEN assessmentTimestamp > :last24Hours THEN 1 END) as assessmentsLast24h,
            COUNT(CASE WHEN riskLevel != 'MINIMAL' THEN 1 END) as concerningAssessments,
            MAX(assessmentTimestamp) as lastActivity
        FROM abuse_risk_assessments
        WHERE assessmentTimestamp > :sinceTime
    """)
    suspend fun getSystemHealthMetrics(sinceTime: Long, last24Hours: Long): SystemHealthMetrics?
}

/**
 * Data classes for query results
 */
data class RiskTrendPoint(
    val assessmentTimestamp: Long,
    val riskScore: Int,
    val riskLevel: AbuseRiskLevel
)

data class RiskFactorFrequency(
    val frequency: Int,
    val riskFactors: List<AbuseRiskFactor>
)

data class SystemAbuseDetectionSummary(
    val totalCaregiversAnalyzed: Int,
    val totalAssessments: Int,
    val highRiskAssessments: Int,
    val averageRiskScore: Double,
    val lastAnalysis: Long
)

data class AlertResponseStats(
    val totalAlerts: Int,
    val resolvedAlerts: Int,
    val falsePositives: Int,
    val averageResponseTime: Long?,
    val elderRightsNotifications: Int
)

data class CaregiverAttentionSummary(
    val caregiverId: String,
    val userId: String,
    val riskLevel: AbuseRiskLevel,
    val assessmentTimestamp: Long,
    val activeAlerts: Int
)

data class RiskAssessmentWithAlerts(
    val assessment: AbuseRiskAssessment,
    val alertsGenerated: Int
)

data class AbusePatternSummary(
    val riskLevel: AbuseRiskLevel,
    val assessmentCount: Int,
    val uniqueCaregivers: Int,
    val uniqueUsers: Int,
    val averageScore: Double
)

data class SystemHealthMetrics(
    val totalAssessments: Int,
    val activeCaregivers: Int,
    val assessmentsLast24h: Int,
    val concerningAssessments: Int,
    val lastActivity: Long
)
