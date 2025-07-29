package com.naviya.launcher.elderrights.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for elder rights advocate notification system
 * Handles all database operations for notifications, escalations, and follow-ups
 */
@Dao
interface ElderRightsDao {

    // ==================== ELDER RIGHTS NOTIFICATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElderRightsNotification(notification: ElderRightsNotification)

    @Update
    suspend fun updateElderRightsNotification(notification: ElderRightsNotification)

    @Query("SELECT * FROM elder_rights_notifications WHERE notificationId = :notificationId")
    suspend fun getElderRightsNotification(notificationId: String): ElderRightsNotification?

    @Query("SELECT * FROM elder_rights_notifications WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getNotificationsForUser(userId: String): List<ElderRightsNotification>

    @Query("SELECT * FROM elder_rights_notifications WHERE userId = :userId AND status = :status ORDER BY timestamp DESC")
    suspend fun getNotificationsByStatus(userId: String, status: NotificationStatus): List<ElderRightsNotification>

    @Query("SELECT * FROM elder_rights_notifications WHERE userId = :userId AND priority = :priority ORDER BY timestamp DESC")
    suspend fun getNotificationsByPriority(userId: String, priority: NotificationPriority): List<ElderRightsNotification>

    @Query("SELECT * FROM elder_rights_notifications WHERE userId = :userId AND timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getRecentNotifications(userId: String, startTime: Long): List<ElderRightsNotification>

    @Query("SELECT * FROM elder_rights_notifications WHERE status IN ('PENDING', 'SENT') AND followUpRequired = 1")
    suspend fun getNotificationsRequiringFollowUp(): List<ElderRightsNotification>

    @Query("SELECT * FROM elder_rights_notifications WHERE priority = 'IMMEDIATE' AND status != 'RESOLVED' ORDER BY timestamp DESC")
    suspend fun getCriticalActiveNotifications(): List<ElderRightsNotification>

    @Query("UPDATE elder_rights_notifications SET status = :status WHERE notificationId = :notificationId")
    suspend fun updateNotificationStatus(notificationId: String, status: NotificationStatus)

    @Query("UPDATE elder_rights_notifications SET resolutionDetails = :details, resolutionTimestamp = :timestamp, status = 'RESOLVED' WHERE notificationId = :notificationId")
    suspend fun resolveNotification(notificationId: String, details: String, timestamp: Long)

    @Query("DELETE FROM elder_rights_notifications WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldNotifications(cutoffTime: Long): Int

    // ==================== ESCALATION PROCEDURES ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEscalationProcedure(procedure: EscalationProcedure)

    @Update
    suspend fun updateEscalationProcedure(procedure: EscalationProcedure)

    @Query("SELECT * FROM escalation_procedures WHERE procedureId = :procedureId")
    suspend fun getEscalationProcedure(procedureId: String): EscalationProcedure?

    @Query("SELECT * FROM escalation_procedures WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveEscalationProcedures(userId: String): List<EscalationProcedure>

    @Query("UPDATE escalation_procedures SET isActive = :isActive WHERE procedureId = :procedureId")
    suspend fun updateEscalationProcedureStatus(procedureId: String, isActive: Boolean)

    @Delete
    suspend fun deleteEscalationProcedure(procedure: EscalationProcedure)

    // ==================== NOTIFICATION FOLLOW-UPS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationFollowUp(followUp: NotificationFollowUp)

    @Update
    suspend fun updateNotificationFollowUp(followUp: NotificationFollowUp)

    @Query("SELECT * FROM notification_followups WHERE followUpId = :followUpId")
    suspend fun getNotificationFollowUp(followUpId: String): NotificationFollowUp?

    @Query("SELECT * FROM notification_followups WHERE notificationId = :notificationId ORDER BY scheduledTimestamp DESC")
    suspend fun getFollowUpsForNotification(notificationId: String): List<NotificationFollowUp>

    @Query("SELECT * FROM notification_followups WHERE userId = :userId AND status = :status ORDER BY scheduledTimestamp ASC")
    suspend fun getFollowUpsByStatus(userId: String, status: FollowUpStatus): List<NotificationFollowUp>

    @Query("SELECT * FROM notification_followups WHERE scheduledTimestamp <= :currentTime AND status = 'SCHEDULED' ORDER BY scheduledTimestamp ASC")
    suspend fun getDueFollowUps(currentTime: Long): List<NotificationFollowUp>

    @Query("UPDATE notification_followups SET status = :status, lastAttemptTimestamp = :timestamp, attempts = attempts + 1 WHERE followUpId = :followUpId")
    suspend fun updateFollowUpAttempt(followUpId: String, status: FollowUpStatus, timestamp: Long)

    @Query("UPDATE notification_followups SET status = 'COMPLETED', completedTimestamp = :timestamp WHERE followUpId = :followUpId")
    suspend fun completeFollowUp(followUpId: String, timestamp: Long)

    @Delete
    suspend fun deleteNotificationFollowUp(followUp: NotificationFollowUp)

    // ==================== ADVOCATE RESPONSES ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvocateResponse(response: AdvocateResponse)

    @Update
    suspend fun updateAdvocateResponse(response: AdvocateResponse)

    @Query("SELECT * FROM advocate_responses WHERE responseId = :responseId")
    suspend fun getAdvocateResponse(responseId: String): AdvocateResponse?

    @Query("SELECT * FROM advocate_responses WHERE notificationId = :notificationId ORDER BY responseTimestamp DESC")
    suspend fun getResponsesForNotification(notificationId: String): List<AdvocateResponse>

    @Query("SELECT * FROM advocate_responses WHERE advocateId = :advocateId ORDER BY responseTimestamp DESC")
    suspend fun getResponsesByAdvocate(advocateId: String): List<AdvocateResponse>

    @Query("SELECT * FROM advocate_responses WHERE responseTimestamp >= :startTime ORDER BY responseTimestamp DESC")
    suspend fun getRecentResponses(startTime: Long): List<AdvocateResponse>

    @Query("SELECT * FROM advocate_responses WHERE caseStatus = :status ORDER BY responseTimestamp DESC")
    suspend fun getResponsesByCaseStatus(status: CaseStatus): List<AdvocateResponse>

    @Delete
    suspend fun deleteAdvocateResponse(response: AdvocateResponse)

    // ==================== EMERGENCY CONTACTS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContact(contact: EmergencyContact)

    @Update
    suspend fun updateEmergencyContact(contact: EmergencyContact)

    @Query("SELECT * FROM emergency_contacts WHERE contactId = :contactId")
    suspend fun getEmergencyContact(contactId: String): EmergencyContact?

    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND isActive = 1 ORDER BY priority ASC")
    suspend fun getActiveEmergencyContacts(userId: String): List<EmergencyContact>

    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND contactType = :type AND isActive = 1 ORDER BY priority ASC")
    suspend fun getEmergencyContactsByType(userId: String, type: EmergencyContactType): List<EmergencyContact>

    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND contactType = 'ELDER_RIGHTS_ADVOCATE' AND isActive = 1 ORDER BY priority ASC LIMIT 1")
    suspend fun getPrimaryElderRightsAdvocate(userId: String): EmergencyContact?

    @Query("UPDATE emergency_contacts SET lastContactedTimestamp = :timestamp WHERE contactId = :contactId")
    suspend fun updateLastContactedTimestamp(contactId: String, timestamp: Long)

    @Query("UPDATE emergency_contacts SET isActive = :isActive WHERE contactId = :contactId")
    suspend fun updateEmergencyContactStatus(contactId: String, isActive: Boolean)

    @Delete
    suspend fun deleteEmergencyContact(contact: EmergencyContact)

    // ==================== NOTIFICATION STATISTICS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationStatistics(statistics: NotificationStatistics)

    @Query("SELECT * FROM notification_statistics WHERE userId = :userId ORDER BY generatedTimestamp DESC LIMIT 1")
    suspend fun getLatestStatistics(userId: String): NotificationStatistics?

    @Query("SELECT * FROM notification_statistics WHERE userId = :userId AND generatedTimestamp >= :startTime ORDER BY generatedTimestamp DESC")
    suspend fun getStatisticsInTimeRange(userId: String, startTime: Long): List<NotificationStatistics>

    @Delete
    suspend fun deleteNotificationStatistics(statistics: NotificationStatistics)

    // ==================== ANALYTICS AND REPORTING ====================

    @Query("""
        SELECT COUNT(*) as total,
               SUM(CASE WHEN status = 'SENT' OR status = 'DELIVERED' OR status = 'RESPONDED' OR status = 'RESOLVED' THEN 1 ELSE 0 END) as successful,
               AVG(CASE WHEN resolutionTimestamp IS NOT NULL THEN resolutionTimestamp - timestamp ELSE NULL END) as avgResolutionTime
        FROM elder_rights_notifications 
        WHERE userId = :userId AND timestamp >= :startTime
    """)
    suspend fun getNotificationSummary(userId: String, startTime: Long): NotificationSummaryResult

    @Query("""
        SELECT priority, COUNT(*) as count
        FROM elder_rights_notifications 
        WHERE userId = :userId AND timestamp >= :startTime
        GROUP BY priority
        ORDER BY priority
    """)
    suspend fun getNotificationsByPriorityCount(userId: String, startTime: Long): List<PriorityCountResult>

    @Query("""
        SELECT advocateId, COUNT(*) as responseCount, AVG(responseTimestamp - (
            SELECT timestamp FROM elder_rights_notifications WHERE notificationId = advocate_responses.notificationId
        )) as avgResponseTime
        FROM advocate_responses ar
        WHERE EXISTS (SELECT 1 FROM elder_rights_notifications ern WHERE ern.notificationId = ar.notificationId AND ern.userId = :userId)
        AND responseTimestamp >= :startTime
        GROUP BY advocateId
        ORDER BY responseCount DESC
    """)
    suspend fun getAdvocatePerformanceMetrics(userId: String, startTime: Long): List<AdvocatePerformanceResult>

    @Query("""
        SELECT followUpType, 
               COUNT(*) as total,
               SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
               AVG(CASE WHEN completedTimestamp IS NOT NULL THEN completedTimestamp - scheduledTimestamp ELSE NULL END) as avgCompletionTime
        FROM notification_followups 
        WHERE userId = :userId AND scheduledTimestamp >= :startTime
        GROUP BY followUpType
    """)
    suspend fun getFollowUpEffectiveness(userId: String, startTime: Long): List<FollowUpEffectivenessResult>

    @Query("""
        SELECT DATE(timestamp/1000, 'unixepoch') as date, 
               COUNT(*) as notificationCount,
               SUM(CASE WHEN priority = 'IMMEDIATE' THEN 1 ELSE 0 END) as criticalCount
        FROM elder_rights_notifications 
        WHERE userId = :userId AND timestamp >= :startTime
        GROUP BY DATE(timestamp/1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getDailyNotificationTrends(userId: String, startTime: Long): List<DailyTrendResult>

    @Query("""
        SELECT caseStatus, COUNT(*) as count
        FROM advocate_responses 
        WHERE EXISTS (SELECT 1 FROM elder_rights_notifications ern WHERE ern.notificationId = advocate_responses.notificationId AND ern.userId = :userId)
        AND responseTimestamp >= :startTime
        GROUP BY caseStatus
        ORDER BY count DESC
    """)
    suspend fun getCaseStatusDistribution(userId: String, startTime: Long): List<CaseStatusResult>

    // ==================== SYSTEM HEALTH AND MONITORING ====================

    @Query("""
        SELECT 
            COUNT(*) as totalNotifications,
            SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failedNotifications,
            MAX(timestamp) as lastNotificationTime,
            COUNT(CASE WHEN priority = 'IMMEDIATE' AND status != 'RESOLVED' THEN 1 END) as activeCriticalAlerts
        FROM elder_rights_notifications 
        WHERE timestamp >= :startTime
    """)
    suspend fun getSystemHealthMetrics(startTime: Long): SystemHealthResult

    @Query("SELECT COUNT(*) FROM notification_followups WHERE status = 'SCHEDULED' AND scheduledTimestamp <= :currentTime")
    suspend fun getOverdueFollowUpCount(currentTime: Long): Int

    @Query("SELECT COUNT(*) FROM elder_rights_notifications WHERE status = 'PENDING' AND timestamp < :staleThreshold")
    suspend fun getStaleNotificationCount(staleThreshold: Long): Int

    // ==================== FLOW-BASED QUERIES FOR REAL-TIME UPDATES ====================

    @Query("SELECT * FROM elder_rights_notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeNotificationsForUser(userId: String): Flow<List<ElderRightsNotification>>

    @Query("SELECT * FROM notification_followups WHERE userId = :userId AND status = 'SCHEDULED' ORDER BY scheduledTimestamp ASC")
    fun observePendingFollowUps(userId: String): Flow<List<NotificationFollowUp>>

    @Query("SELECT * FROM elder_rights_notifications WHERE priority = 'IMMEDIATE' AND status != 'RESOLVED' ORDER BY timestamp DESC")
    fun observeCriticalNotifications(): Flow<List<ElderRightsNotification>>
}

// ==================== RESULT DATA CLASSES ====================

data class NotificationSummaryResult(
    val total: Int,
    val successful: Int,
    val avgResolutionTime: Long?
)

data class PriorityCountResult(
    val priority: NotificationPriority,
    val count: Int
)

data class AdvocatePerformanceResult(
    val advocateId: String,
    val responseCount: Int,
    val avgResponseTime: Long
)

data class FollowUpEffectivenessResult(
    val followUpType: FollowUpType,
    val total: Int,
    val completed: Int,
    val avgCompletionTime: Long?
)

data class DailyTrendResult(
    val date: String,
    val notificationCount: Int,
    val criticalCount: Int
)

data class CaseStatusResult(
    val caseStatus: CaseStatus,
    val count: Int
)

data class SystemHealthResult(
    val totalNotifications: Int,
    val failedNotifications: Int,
    val lastNotificationTime: Long?,
    val activeCriticalAlerts: Int
)
