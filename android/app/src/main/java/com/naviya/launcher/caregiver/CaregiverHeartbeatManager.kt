package com.naviya.launcher.caregiver

import android.content.Context
import com.naviya.launcher.caregiver.data.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages heartbeat monitoring for caregiver connections
 * Ensures caregivers are reachable and responsive for emergency situations
 */
@Singleton
class CaregiverHeartbeatManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val caregiverDao: CaregiverDao,
    private val networkManager: NetworkManager
) {

    companion object {
        private const val HEARTBEAT_TIMEOUT_MS = 10_000L // 10 seconds
        private const val MAX_HEARTBEAT_RETRIES = 3
        private const val RETRY_DELAY_MS = 2_000L // 2 seconds
    }

    /**
     * Send heartbeat to specific caregiver
     */
    suspend fun sendHeartbeat(caregiverId: String): HeartbeatResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val heartbeatId = "heartbeat-${System.nanoTime()}"
        
        try {
            val caregiver = caregiverDao.getCaregiverConnection(caregiverId)
                ?: return@withContext HeartbeatResult(
                    success = false,
                    responseTime = 0,
                    error = "Caregiver not found"
                )
            
            val result = performHeartbeatWithRetry(caregiver, heartbeatId, startTime)
            
            // Store heartbeat record
            val heartbeat = CaregiverHeartbeat(
                heartbeatId = heartbeatId,
                userId = caregiver.userId,
                caregiverId = caregiverId,
                timestamp = startTime,
                success = result.success,
                responseTime = result.responseTime,
                networkType = networkManager.getCurrentNetworkType(),
                connectionQuality = networkManager.getCurrentConnectionQuality(),
                errorMessage = result.error
            )
            
            caregiverDao.insertCaregiverHeartbeat(heartbeat)
            
            result
            
        } catch (e: Exception) {
            HeartbeatResult(
                success = false,
                responseTime = System.currentTimeMillis() - startTime,
                error = "Heartbeat failed: ${e.message}"
            )
        }
    }

    /**
     * Send heartbeat to all connected caregivers
     */
    suspend fun sendHeartbeatToAll(userId: String): List<HeartbeatResult> = withContext(Dispatchers.IO) {
        val caregivers = caregiverDao.getConnectedCaregivers(userId)
        
        caregivers.map { caregiver ->
            async { sendHeartbeat(caregiver.caregiverId) }
        }.awaitAll()
    }

    /**
     * Get heartbeat status for caregiver
     */
    suspend fun getHeartbeatStatus(caregiverId: String): HeartbeatStatus = withContext(Dispatchers.IO) {
        val recentHeartbeats = caregiverDao.getRecentHeartbeats(caregiverId, 5)
        
        if (recentHeartbeats.isEmpty()) {
            return@withContext HeartbeatStatus(
                caregiverId = caregiverId,
                isResponsive = false,
                lastHeartbeat = 0,
                averageResponseTime = 0,
                successRate = 0.0,
                status = HeartbeatHealthStatus.UNKNOWN
            )
        }
        
        val successfulHeartbeats = recentHeartbeats.count { it.success }
        val successRate = successfulHeartbeats.toDouble() / recentHeartbeats.size
        val averageResponseTime = recentHeartbeats
            .filter { it.success }
            .map { it.responseTime }
            .average()
            .toLong()
        
        val lastHeartbeat = recentHeartbeats.maxByOrNull { it.timestamp }?.timestamp ?: 0
        val timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeat
        
        val status = when {
            successRate >= 0.8 && averageResponseTime < 5000 -> HeartbeatHealthStatus.EXCELLENT
            successRate >= 0.6 && averageResponseTime < 10000 -> HeartbeatHealthStatus.GOOD
            successRate >= 0.4 -> HeartbeatHealthStatus.POOR
            timeSinceLastHeartbeat > 1800000 -> HeartbeatHealthStatus.OFFLINE // 30 minutes
            else -> HeartbeatHealthStatus.CRITICAL
        }
        
        HeartbeatStatus(
            caregiverId = caregiverId,
            isResponsive = successRate >= 0.5,
            lastHeartbeat = lastHeartbeat,
            averageResponseTime = averageResponseTime,
            successRate = successRate,
            status = status
        )
    }

    /**
     * Get overall heartbeat health for user's caregivers
     */
    suspend fun getOverallHeartbeatHealth(userId: String): OverallHeartbeatHealth = withContext(Dispatchers.IO) {
        val caregivers = caregiverDao.getConnectedCaregivers(userId)
        val heartbeatStatuses = caregivers.map { getHeartbeatStatus(it.caregiverId) }
        
        val responsiveCaregivers = heartbeatStatuses.count { it.isResponsive }
        val totalCaregivers = heartbeatStatuses.size
        
        val overallStatus = when {
            totalCaregivers == 0 -> HeartbeatHealthStatus.UNKNOWN
            responsiveCaregivers == 0 -> HeartbeatHealthStatus.CRITICAL
            responsiveCaregivers.toDouble() / totalCaregivers >= 0.8 -> HeartbeatHealthStatus.EXCELLENT
            responsiveCaregivers.toDouble() / totalCaregivers >= 0.5 -> HeartbeatHealthStatus.GOOD
            else -> HeartbeatHealthStatus.POOR
        }
        
        OverallHeartbeatHealth(
            totalCaregivers = totalCaregivers,
            responsiveCaregivers = responsiveCaregivers,
            overallStatus = overallStatus,
            caregiverStatuses = heartbeatStatuses,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Test emergency reachability of all caregivers
     */
    suspend fun testEmergencyReachability(userId: String): EmergencyReachabilityTest = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val caregivers = caregiverDao.getConnectedCaregivers(userId)
        
        val results = caregivers.map { caregiver ->
            async {
                val heartbeatResult = sendHeartbeat(caregiver.caregiverId)
                EmergencyReachabilityResult(
                    caregiverId = caregiver.caregiverId,
                    caregiverName = caregiver.caregiverName,
                    isReachable = heartbeatResult.success,
                    responseTime = heartbeatResult.responseTime,
                    connectionType = caregiver.connectionType,
                    emergencyPriority = caregiver.emergencyContactPriority
                )
            }
        }.awaitAll()
        
        val reachableCaregivers = results.count { it.isReachable }
        val emergencyContacts = results.filter { it.emergencyPriority > 0 }
        val reachableEmergencyContacts = emergencyContacts.count { it.isReachable }
        
        val overallReachability = when {
            caregivers.isEmpty() -> EmergencyReachabilityStatus.NO_CAREGIVERS
            reachableCaregivers == 0 -> EmergencyReachabilityStatus.NONE_REACHABLE
            reachableEmergencyContacts == 0 && emergencyContacts.isNotEmpty() -> EmergencyReachabilityStatus.NO_EMERGENCY_CONTACTS
            reachableCaregivers.toDouble() / caregivers.size >= 0.8 -> EmergencyReachabilityStatus.EXCELLENT
            reachableCaregivers.toDouble() / caregivers.size >= 0.5 -> EmergencyReachabilityStatus.GOOD
            else -> EmergencyReachabilityStatus.LIMITED
        }
        
        EmergencyReachabilityTest(
            userId = userId,
            testTimestamp = startTime,
            totalCaregivers = caregivers.size,
            reachableCaregivers = reachableCaregivers,
            emergencyContacts = emergencyContacts.size,
            reachableEmergencyContacts = reachableEmergencyContacts,
            overallStatus = overallReachability,
            results = results,
            testDuration = System.currentTimeMillis() - startTime
        )
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * Perform heartbeat with retry logic
     */
    private suspend fun performHeartbeatWithRetry(
        caregiver: CaregiverConnection,
        heartbeatId: String,
        startTime: Long
    ): HeartbeatResult {
        var lastError: String? = null
        
        repeat(MAX_HEARTBEAT_RETRIES) { attempt ->
            try {
                val result = performSingleHeartbeat(caregiver, heartbeatId)
                if (result.success) {
                    return result.copy(responseTime = System.currentTimeMillis() - startTime)
                }
                lastError = result.error
                
                if (attempt < MAX_HEARTBEAT_RETRIES - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1)) // Exponential backoff
                }
            } catch (e: Exception) {
                lastError = e.message
                if (attempt < MAX_HEARTBEAT_RETRIES - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1))
                }
            }
        }
        
        return HeartbeatResult(
            success = false,
            responseTime = System.currentTimeMillis() - startTime,
            error = "Heartbeat failed after $MAX_HEARTBEAT_RETRIES attempts: $lastError"
        )
    }

    /**
     * Perform single heartbeat attempt
     */
    private suspend fun performSingleHeartbeat(
        caregiver: CaregiverConnection,
        heartbeatId: String
    ): HeartbeatResult = withTimeout(HEARTBEAT_TIMEOUT_MS) {
        try {
            // Implementation depends on communication protocol
            // This could be HTTP ping, WebSocket ping, Firebase message, etc.
            
            // For now, simulate heartbeat
            delay(100) // Simulate network delay
            
            // Simulate success/failure based on connection quality
            val success = when (caregiver.connectionQuality) {
                ConnectionQuality.HIGH -> true
                ConnectionQuality.MEDIUM -> Math.random() > 0.1 // 90% success
                ConnectionQuality.LOW -> Math.random() > 0.3 // 70% success
                ConnectionQuality.UNKNOWN -> Math.random() > 0.5 // 50% success
            }
            
            if (success) {
                HeartbeatResult(
                    success = true,
                    responseTime = 100,
                    message = "Heartbeat successful"
                )
            } else {
                HeartbeatResult(
                    success = false,
                    responseTime = 0,
                    error = "Simulated heartbeat failure"
                )
            }
            
        } catch (e: TimeoutCancellationException) {
            HeartbeatResult(
                success = false,
                responseTime = HEARTBEAT_TIMEOUT_MS,
                error = "Heartbeat timed out"
            )
        } catch (e: Exception) {
            HeartbeatResult(
                success = false,
                responseTime = 0,
                error = "Heartbeat error: ${e.message}"
            )
        }
    }
}

// ==================== SUPPORTING DATA CLASSES ====================

data class HeartbeatResult(
    val success: Boolean,
    val responseTime: Long,
    val message: String? = null,
    val error: String? = null
)

data class HeartbeatStatus(
    val caregiverId: String,
    val isResponsive: Boolean,
    val lastHeartbeat: Long,
    val averageResponseTime: Long,
    val successRate: Double,
    val status: HeartbeatHealthStatus
)

data class OverallHeartbeatHealth(
    val totalCaregivers: Int,
    val responsiveCaregivers: Int,
    val overallStatus: HeartbeatHealthStatus,
    val caregiverStatuses: List<HeartbeatStatus>,
    val lastUpdated: Long
)

data class EmergencyReachabilityTest(
    val userId: String,
    val testTimestamp: Long,
    val totalCaregivers: Int,
    val reachableCaregivers: Int,
    val emergencyContacts: Int,
    val reachableEmergencyContacts: Int,
    val overallStatus: EmergencyReachabilityStatus,
    val results: List<EmergencyReachabilityResult>,
    val testDuration: Long
)

data class EmergencyReachabilityResult(
    val caregiverId: String,
    val caregiverName: String,
    val isReachable: Boolean,
    val responseTime: Long,
    val connectionType: CaregiverConnectionType,
    val emergencyPriority: Int
)

enum class HeartbeatHealthStatus {
    EXCELLENT,  // >80% success rate, fast response
    GOOD,       // >60% success rate, reasonable response
    POOR,       // >40% success rate, slow response
    CRITICAL,   // <40% success rate or very slow
    OFFLINE,    // No recent heartbeats
    UNKNOWN     // No heartbeat data available
}

enum class EmergencyReachabilityStatus {
    EXCELLENT,              // >80% of caregivers reachable
    GOOD,                   // >50% of caregivers reachable
    LIMITED,                // Some caregivers reachable
    NO_EMERGENCY_CONTACTS,  // No emergency contacts reachable
    NONE_REACHABLE,         // No caregivers reachable
    NO_CAREGIVERS          // No caregivers configured
}
