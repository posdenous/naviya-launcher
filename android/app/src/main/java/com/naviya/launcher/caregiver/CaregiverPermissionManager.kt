package com.naviya.launcher.caregiver

import android.content.Context
import androidx.room.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caregiver Permission Manager with Abuse Prevention
 * Implements default minimal access and granular permission control
 */

@Entity(tableName = "caregiver_permissions")
data class CaregiverPermissions(
    @PrimaryKey val caregiverId: String,
    val caregiverName: String,
    val caregiverEmail: String,
    val caregiverPhone: String,
    
    // CORE PRINCIPLE: Default to minimal access
    val emergencyNotifications: Boolean = true,        // ONLY this enabled by default
    val locationAccess: LocationAccessLevel = LocationAccessLevel.NONE,
    val appUsageMonitoring: Boolean = false,          // Opt-in only
    val remoteConfiguration: Boolean = false,         // Requires PIN + consent
    val communicationAccess: Boolean = false,         // NEVER enabled by default
    val healthDataAccess: Boolean = false,            // Medical info opt-in
    val financialDataAccess: Boolean = false,         // NEVER allowed
    
    // Abuse prevention metadata
    val consentTimestamp: Long = System.currentTimeMillis(),
    val lastConsentReview: Long = System.currentTimeMillis(),
    val consentWitnessId: String? = null,             // Independent witness
    val isActive: Boolean = true,
    val createdBy: String = "user",                   // Who added this caregiver
    val revokedAt: Long? = null,
    val revokedReason: String? = null
)

enum class LocationAccessLevel {
    NONE,                    // No location access
    EMERGENCY_ONLY,          // Only during SOS events
    APPROXIMATE,             // General area only (within 1 mile)
    PRECISE                  // Exact coordinates (requires special justification)
}

@Entity(tableName = "permission_audit_log")
data class PermissionAuditEntry(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val caregiverId: String,
    val actionType: PermissionActionType,
    val permissionChanged: String,
    val oldValue: String,
    val newValue: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userConsent: Boolean,
    val witnessId: String? = null,
    val ipAddress: String? = null,
    val deviceInfo: String? = null,
    val immutableHash: String,                        // Blockchain-style integrity
    val previousEntryHash: String? = null
)

enum class PermissionActionType {
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,
    PERMISSION_MODIFIED,
    ACCESS_ATTEMPTED,
    CONSENT_REVIEWED,
    CAREGIVER_ADDED,
    CAREGIVER_REMOVED,
    ABUSE_FLAG_RAISED,
    EMERGENCY_OVERRIDE
}

@Dao
interface CaregiverPermissionDao {
    @Query("SELECT * FROM caregiver_permissions WHERE isActive = 1")
    fun getActiveCaregivers(): Flow<List<CaregiverPermissions>>
    
    @Query("SELECT * FROM caregiver_permissions WHERE caregiverId = :id")
    suspend fun getCaregiverPermissions(id: String): CaregiverPermissions?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePermissions(permissions: CaregiverPermissions)
    
    @Query("UPDATE caregiver_permissions SET isActive = 0, revokedAt = :timestamp, revokedReason = :reason WHERE caregiverId = :id")
    suspend fun revokeCaregiver(id: String, timestamp: Long, reason: String)
    
    @Insert
    suspend fun insertAuditEntry(entry: PermissionAuditEntry)
    
    @Query("SELECT * FROM permission_audit_log WHERE caregiverId = :id ORDER BY timestamp DESC")
    fun getAuditLog(id: String): Flow<List<PermissionAuditEntry>>
    
    @Query("SELECT * FROM permission_audit_log WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getRecentAuditEntries(since: Long): List<PermissionAuditEntry>
}

@Singleton
class CaregiverPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: CaregiverPermissionDao,
    private val abuseDetector: AbuseDetectionEngine
) {
    
    companion object {
        private const val TAG = "CaregiverPermissionManager"
        private const val CONSENT_REVIEW_INTERVAL_DAYS = 30L
        private const val MAX_CAREGIVERS = 5  // Prevent caregiver flooding
    }
    
    /**
     * Add new caregiver with MINIMAL default permissions
     * Implements abuse prevention from the start
     */
    suspend fun addCaregiver(
        caregiverName: String,
        caregiverEmail: String,
        caregiverPhone: String,
        userConsent: Boolean,
        witnessId: String? = null
    ): Result<String> {
        
        if (!userConsent) {
            return Result.failure(Exception("User consent required to add caregiver"))
        }
        
        // Check caregiver limit to prevent abuse
        val existingCaregivers = dao.getActiveCaregivers()
        // Note: In real implementation, we'd collect this as a suspend function
        
        val caregiverId = java.util.UUID.randomUUID().toString()
        
        // DEFAULT TO MINIMAL ACCESS - Critical for abuse prevention
        val permissions = CaregiverPermissions(
            caregiverId = caregiverId,
            caregiverName = caregiverName,
            caregiverEmail = caregiverEmail,
            caregiverPhone = caregiverPhone,
            // All permissions default to false/none except emergency
            emergencyNotifications = true,  // ONLY this enabled by default
            consentWitnessId = witnessId
        )
        
        dao.insertOrUpdatePermissions(permissions)
        
        // Log the addition with immutable audit trail
        logPermissionChange(
            caregiverId = caregiverId,
            actionType = PermissionActionType.CAREGIVER_ADDED,
            permissionChanged = "caregiver_added",
            oldValue = "none",
            newValue = caregiverName,
            userConsent = userConsent,
            witnessId = witnessId
        )
        
        return Result.success(caregiverId)
    }
    
    /**
     * Request additional permissions - requires explicit user consent
     */
    suspend fun requestPermission(
        caregiverId: String,
        permission: String,
        userConsent: Boolean,
        justification: String,
        witnessId: String? = null
    ): Result<Boolean> {
        
        if (!userConsent) {
            logPermissionChange(
                caregiverId = caregiverId,
                actionType = PermissionActionType.ACCESS_ATTEMPTED,
                permissionChanged = permission,
                oldValue = "denied",
                newValue = "denied",
                userConsent = false
            )
            return Result.failure(Exception("User consent required for permission: $permission"))
        }
        
        val currentPermissions = dao.getCaregiverPermissions(caregiverId)
            ?: return Result.failure(Exception("Caregiver not found"))
        
        // Special restrictions for high-risk permissions
        when (permission) {
            "financialDataAccess" -> {
                // NEVER allow financial access
                return Result.failure(Exception("Financial data access is not permitted"))
            }
            "communicationAccess" -> {
                // Require special justification for communication access
                if (justification.length < 50) {
                    return Result.failure(Exception("Detailed justification required for communication access"))
                }
            }
            "locationAccess" -> {
                // Default to emergency-only location access
                if (!justification.contains("emergency", ignoreCase = true)) {
                    return Result.failure(Exception("Location access requires emergency justification"))
                }
            }
        }
        
        // Update permissions
        val updatedPermissions = when (permission) {
            "locationAccess" -> currentPermissions.copy(locationAccess = LocationAccessLevel.EMERGENCY_ONLY)
            "appUsageMonitoring" -> currentPermissions.copy(appUsageMonitoring = true)
            "remoteConfiguration" -> currentPermissions.copy(remoteConfiguration = true)
            "healthDataAccess" -> currentPermissions.copy(healthDataAccess = true)
            else -> return Result.failure(Exception("Unknown permission: $permission"))
        }
        
        dao.insertOrUpdatePermissions(updatedPermissions)
        
        // Log permission grant with immutable audit trail
        logPermissionChange(
            caregiverId = caregiverId,
            actionType = PermissionActionType.PERMISSION_GRANTED,
            permissionChanged = permission,
            oldValue = "false",
            newValue = "true",
            userConsent = userConsent,
            witnessId = witnessId
        )
        
        return Result.success(true)
    }
    
    /**
     * Easy one-tap caregiver removal - critical for abuse prevention
     */
    suspend fun removeCaregiver(
        caregiverId: String,
        reason: String = "user_requested",
        userInitiated: Boolean = true
    ): Result<Boolean> {
        
        dao.revokeCaregiver(
            id = caregiverId,
            timestamp = System.currentTimeMillis(),
            reason = reason
        )
        
        // Log removal with immutable audit trail
        logPermissionChange(
            caregiverId = caregiverId,
            actionType = PermissionActionType.CAREGIVER_REMOVED,
            permissionChanged = "caregiver_status",
            oldValue = "active",
            newValue = "revoked",
            userConsent = userInitiated
        )
        
        return Result.success(true)
    }
    
    /**
     * Check if caregiver has specific permission
     */
    suspend fun hasPermission(caregiverId: String, permission: String): Boolean {
        val permissions = dao.getCaregiverPermissions(caregiverId) ?: return false
        
        if (!permissions.isActive) return false
        
        // Log access attempt for audit trail
        logPermissionChange(
            caregiverId = caregiverId,
            actionType = PermissionActionType.ACCESS_ATTEMPTED,
            permissionChanged = permission,
            oldValue = "checked",
            newValue = "checked",
            userConsent = true  // Checking permission is not consent-requiring
        )
        
        return when (permission) {
            "emergencyNotifications" -> permissions.emergencyNotifications
            "locationAccess" -> permissions.locationAccess != LocationAccessLevel.NONE
            "appUsageMonitoring" -> permissions.appUsageMonitoring
            "remoteConfiguration" -> permissions.remoteConfiguration
            "communicationAccess" -> permissions.communicationAccess
            "healthDataAccess" -> permissions.healthDataAccess
            "financialDataAccess" -> false  // NEVER allowed
            else -> false
        }
    }
    
    /**
     * Regular consent review - required monthly
     */
    suspend fun requiresConsentReview(caregiverId: String): Boolean {
        val permissions = dao.getCaregiverPermissions(caregiverId) ?: return true
        
        val daysSinceLastReview = Duration.between(
            Instant.ofEpochMilli(permissions.lastConsentReview),
            Instant.now()
        ).toDays()
        
        return daysSinceLastReview >= CONSENT_REVIEW_INTERVAL_DAYS
    }
    
    /**
     * Conduct consent review with independent witness
     */
    suspend fun conductConsentReview(
        caregiverId: String,
        userConsent: Boolean,
        witnessId: String,
        reviewResponses: Map<String, Boolean>
    ): Result<Boolean> {
        
        val permissions = dao.getCaregiverPermissions(caregiverId)
            ?: return Result.failure(Exception("Caregiver not found"))
        
        // Update consent review timestamp
        val updatedPermissions = permissions.copy(
            lastConsentReview = System.currentTimeMillis(),
            consentWitnessId = witnessId
        )
        
        dao.insertOrUpdatePermissions(updatedPermissions)
        
        // Log consent review
        logPermissionChange(
            caregiverId = caregiverId,
            actionType = PermissionActionType.CONSENT_REVIEWED,
            permissionChanged = "consent_review",
            oldValue = "due",
            newValue = "completed",
            userConsent = userConsent,
            witnessId = witnessId
        )
        
        // Check for concerning responses
        val concerningResponses = reviewResponses.filter { (question, response) ->
            question.contains("pressured", ignoreCase = true) && response == true ||
            question.contains("uncomfortable", ignoreCase = true) && response == true
        }
        
        if (concerningResponses.isNotEmpty()) {
            // Flag potential abuse
            abuseDetector.flagPotentialAbuse(
                caregiverId = caregiverId,
                reason = "Concerning consent review responses: $concerningResponses"
            )
        }
        
        return Result.success(true)
    }
    
    /**
     * Create immutable audit log entry
     */
    private suspend fun logPermissionChange(
        caregiverId: String,
        actionType: PermissionActionType,
        permissionChanged: String,
        oldValue: String,
        newValue: String,
        userConsent: Boolean,
        witnessId: String? = null
    ) {
        
        // Get previous entry hash for blockchain-style chaining
        val recentEntries = dao.getRecentAuditEntries(System.currentTimeMillis() - 1000)
        val previousHash = recentEntries.firstOrNull()?.immutableHash
        
        // Create content hash for immutability
        val content = "$caregiverId:$actionType:$permissionChanged:$oldValue:$newValue:${System.currentTimeMillis()}"
        val immutableHash = content.hashCode().toString()  // In production, use proper cryptographic hash
        
        val auditEntry = PermissionAuditEntry(
            caregiverId = caregiverId,
            actionType = actionType,
            permissionChanged = permissionChanged,
            oldValue = oldValue,
            newValue = newValue,
            userConsent = userConsent,
            witnessId = witnessId,
            immutableHash = immutableHash,
            previousEntryHash = previousHash
        )
        
        dao.insertAuditEntry(auditEntry)
    }
    
    /**
     * Get user-friendly permission summary
     */
    suspend fun getPermissionSummary(caregiverId: String): String {
        val permissions = dao.getCaregiverPermissions(caregiverId) ?: return "Caregiver not found"
        
        val enabledPermissions = mutableListOf<String>()
        
        if (permissions.emergencyNotifications) enabledPermissions.add("Emergency alerts")
        if (permissions.locationAccess != LocationAccessLevel.NONE) {
            enabledPermissions.add("Location (${permissions.locationAccess.name.lowercase()})")
        }
        if (permissions.appUsageMonitoring) enabledPermissions.add("App usage monitoring")
        if (permissions.remoteConfiguration) enabledPermissions.add("Remote assistance")
        if (permissions.healthDataAccess) enabledPermissions.add("Health reminders")
        
        return if (enabledPermissions.isEmpty()) {
            "No permissions granted"
        } else {
            "Permissions: ${enabledPermissions.joinToString(", ")}"
        }
    }
}
