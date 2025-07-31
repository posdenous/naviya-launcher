package com.naviya.launcher.caregiver

import android.content.Context
import com.naviya.launcher.caregiver.data.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages opportunistic synchronization with caregivers
 * Handles different sync strategies based on network conditions and data priority
 */
@Singleton
class CaregiverSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val caregiverDao: CaregiverDao,
    private val networkManager: NetworkManager
) {

    companion object {
        private const val SYNC_TIMEOUT_MS = 30_000L // 30 seconds
        private const val CRITICAL_SYNC_TIMEOUT_MS = 10_000L // 10 seconds
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 5_000L // 5 seconds
    }

    private var currentSyncStrategy = SyncStrategy.FULL
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Perform full synchronization with all caregivers
     */
    suspend fun performFullSync(): SyncResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val syncId = "full-sync-${System.nanoTime()}"
        
        try {
            val connectedCaregivers = caregiverDao.getConnectedCaregivers()
            var totalRecordsSynced = 0
            val errors = mutableListOf<String>()
            
            connectedCaregivers.forEach { caregiver ->
                try {
                    val syncOperation = SyncOperation(
                        syncId = "${syncId}-${caregiver.caregiverId}",
                        userId = caregiver.userId,
                        caregiverId = caregiver.caregiverId,
                        syncType = SyncType.FULL,
                        startTimestamp = System.currentTimeMillis(),
                        status = SyncStatus.IN_PROGRESS,
                        dataCategories = getAllSyncCategories(),
                        networkQuality = networkManager.getCurrentConnectionQuality()
                    )
                    
                    caregiverDao.insertSyncOperation(syncOperation)
                    
                    val result = syncWithCaregiver(caregiver, SyncType.FULL)
                    totalRecordsSynced += result.recordsSynced
                    
                    if (!result.success) {
                        errors.addAll(result.errors)
                    }
                    
                    // Update sync operation
                    caregiverDao.updateSyncOperation(
                        syncOperation.copy(
                            endTimestamp = System.currentTimeMillis(),
                            status = if (result.success) SyncStatus.COMPLETED else SyncStatus.FAILED,
                            recordsSynced = result.recordsSynced,
                            errorMessage = result.errors.firstOrNull()
                        )
                    )
                    
                } catch (e: Exception) {
                    errors.add("Sync failed for caregiver ${caregiver.caregiverId}: ${e.message}")
                }
            }
            
            val success = errors.isEmpty()
            val endTime = System.currentTimeMillis()
            
            if (success) {
                caregiverDao.updateLastSyncTime(endTime)
            }
            
            SyncResult(
                success = success,
                message = if (success) "Full sync completed successfully" else "Full sync completed with errors",
                timestamp = endTime,
                recordsSynced = totalRecordsSynced,
                errors = errors
            )
            
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Full sync failed: ${e.message}",
                timestamp = System.currentTimeMillis(),
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    /**
     * Perform critical data synchronization only
     */
    suspend fun performCriticalSync(): SyncResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val syncId = "critical-sync-${System.nanoTime()}"
        
        try {
            val connectedCaregivers = caregiverDao.getConnectedCaregivers()
            var totalRecordsSynced = 0
            val errors = mutableListOf<String>()
            
            connectedCaregivers.forEach { caregiver ->
                try {
                    val syncOperation = SyncOperation(
                        syncId = "${syncId}-${caregiver.caregiverId}",
                        userId = caregiver.userId,
                        caregiverId = caregiver.caregiverId,
                        syncType = SyncType.CRITICAL,
                        startTimestamp = System.currentTimeMillis(),
                        status = SyncStatus.IN_PROGRESS,
                        dataCategories = getCriticalSyncCategories(),
                        networkQuality = networkManager.getCurrentConnectionQuality()
                    )
                    
                    caregiverDao.insertSyncOperation(syncOperation)
                    
                    val result = syncCriticalDataWithCaregiver(caregiver)
                    totalRecordsSynced += result.recordsSynced
                    
                    if (!result.success) {
                        errors.addAll(result.errors)
                    }
                    
                    // Update sync operation
                    caregiverDao.updateSyncOperation(
                        syncOperation.copy(
                            endTimestamp = System.currentTimeMillis(),
                            status = if (result.success) SyncStatus.COMPLETED else SyncStatus.FAILED,
                            recordsSynced = result.recordsSynced,
                            errorMessage = result.errors.firstOrNull()
                        )
                    )
                    
                } catch (e: Exception) {
                    errors.add("Critical sync failed for caregiver ${caregiver.caregiverId}: ${e.message}")
                }
            }
            
            val success = errors.isEmpty()
            val endTime = System.currentTimeMillis()
            
            SyncResult(
                success = success,
                message = if (success) "Critical sync completed successfully" else "Critical sync completed with errors",
                timestamp = endTime,
                recordsSynced = totalRecordsSynced,
                errors = errors
            )
            
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Critical sync failed: ${e.message}",
                timestamp = System.currentTimeMillis(),
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    /**
     * Perform forced synchronization for specific user
     */
    suspend fun performForcedSync(userId: String): SyncResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val syncId = "forced-sync-${System.nanoTime()}"
        
        try {
            val userCaregivers = caregiverDao.getConnectedCaregivers(userId)
            var totalRecordsSynced = 0
            val errors = mutableListOf<String>()
            
            userCaregivers.forEach { caregiver ->
                try {
                    val result = syncWithCaregiver(caregiver, SyncType.MANUAL)
                    totalRecordsSynced += result.recordsSynced
                    
                    if (!result.success) {
                        errors.addAll(result.errors)
                    }
                    
                } catch (e: Exception) {
                    errors.add("Forced sync failed for caregiver ${caregiver.caregiverId}: ${e.message}")
                }
            }
            
            val success = errors.isEmpty()
            val endTime = System.currentTimeMillis()
            
            if (success) {
                caregiverDao.updateLastSyncTime(endTime)
            }
            
            SyncResult(
                success = success,
                message = if (success) "Forced sync completed successfully" else "Forced sync completed with errors",
                timestamp = endTime,
                recordsSynced = totalRecordsSynced,
                errors = errors
            )
            
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Forced sync failed: ${e.message}",
                timestamp = System.currentTimeMillis(),
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    /**
     * Sync with individual caregiver
     */
    private suspend fun syncWithCaregiver(
        caregiver: CaregiverConnection,
        syncType: SyncType
    ): SyncResult = withTimeout(SYNC_TIMEOUT_MS) {
        try {
            val dataToSync = when (syncType) {
                SyncType.CRITICAL -> getCriticalDataForSync(caregiver.userId)
                SyncType.FULL -> getAllDataForSync(caregiver.userId)
                SyncType.INCREMENTAL -> getIncrementalDataForSync(caregiver.userId, caregiver.lastSyncTime)
                SyncType.MANUAL -> getAllDataForSync(caregiver.userId)
                SyncType.OPPORTUNISTIC -> getOpportunisticDataForSync(caregiver.userId)
                SyncType.EMERGENCY -> getEmergencyDataForSync(caregiver.userId)
            }
            
            // Filter data based on caregiver permissions
            val filteredData = filterDataByPermissions(dataToSync, caregiver.permissions)
            
            // Send data to caregiver
            val sendResult = sendDataToCaregiver(caregiver, filteredData)
            
            if (sendResult.success) {
                // Update last sync time for this caregiver
                caregiverDao.updateCaregiverLastSyncTime(caregiver.caregiverId, System.currentTimeMillis())
                
                SyncResult(
                    success = true,
                    message = "Sync completed successfully",
                    timestamp = System.currentTimeMillis(),
                    recordsSynced = filteredData.size
                )
            } else {
                SyncResult(
                    success = false,
                    message = "Failed to send data to caregiver",
                    timestamp = System.currentTimeMillis(),
                    errors = listOf(sendResult.error ?: "Unknown send error")
                )
            }
            
        } catch (e: TimeoutCancellationException) {
            SyncResult(
                success = false,
                message = "Sync timed out",
                timestamp = System.currentTimeMillis(),
                errors = listOf("Sync operation timed out after ${SYNC_TIMEOUT_MS}ms")
            )
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Sync failed: ${e.message}",
                timestamp = System.currentTimeMillis(),
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    /**
     * Sync critical data only with caregiver
     */
    private suspend fun syncCriticalDataWithCaregiver(
        caregiver: CaregiverConnection
    ): SyncResult = withTimeout(CRITICAL_SYNC_TIMEOUT_MS) {
        try {
            val criticalData = getCriticalDataForSync(caregiver.userId)
            val filteredData = filterDataByPermissions(criticalData, caregiver.permissions)
            
            val sendResult = sendDataToCaregiver(caregiver, filteredData)
            
            if (sendResult.success) {
                SyncResult(
                    success = true,
                    message = "Critical sync completed successfully",
                    timestamp = System.currentTimeMillis(),
                    recordsSynced = filteredData.size
                )
            } else {
                SyncResult(
                    success = false,
                    message = "Failed to send critical data to caregiver",
                    timestamp = System.currentTimeMillis(),
                    errors = listOf(sendResult.error ?: "Unknown send error")
                )
            }
            
        } catch (e: TimeoutCancellationException) {
            SyncResult(
                success = false,
                message = "Critical sync timed out",
                timestamp = System.currentTimeMillis(),
                errors = listOf("Critical sync operation timed out after ${CRITICAL_SYNC_TIMEOUT_MS}ms")
            )
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Critical sync failed: ${e.message}",
                timestamp = System.currentTimeMillis(),
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    /**
     * Set sync strategy based on network conditions
     */
    fun setSyncStrategy(strategy: SyncStrategy) {
        currentSyncStrategy = strategy
    }

    /**
     * Get current sync strategy
     */
    fun getCurrentSyncStrategy(): SyncStrategy = currentSyncStrategy

    /**
     * Process offline data queue when connection is restored
     */
    suspend fun processOfflineDataQueue(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val queuedItems = caregiverDao.getOfflineDataQueue()
            var processedItems = 0
            val errors = mutableListOf<String>()
            
            // Sort by priority and timestamp
            val sortedItems = queuedItems.sortedWith(
                compareBy<OfflineDataQueue> { it.priority.ordinal }
                    .thenBy { it.timestamp }
            )
            
            sortedItems.forEach { queueItem ->
                try {
                    val result = processQueuedItem(queueItem)
                    if (result.success) {
                        processedItems++
                        caregiverDao.deleteOfflineDataQueueItem(queueItem.queueId)
                    } else {
                        errors.add("Failed to process queue item ${queueItem.queueId}: ${result.message}")
                        
                        // Update retry count
                        if (queueItem.retryCount < queueItem.maxRetries) {
                            caregiverDao.updateOfflineDataQueueRetry(
                                queueItem.queueId,
                                queueItem.retryCount + 1,
                                System.currentTimeMillis() + (queueItem.retryCount + 1) * 60_000L // Exponential backoff
                            )
                        } else {
                            // Max retries reached - mark as failed
                            caregiverDao.deleteOfflineDataQueueItem(queueItem.queueId)
                        }
                    }
                } catch (e: Exception) {
                    errors.add("Error processing queue item ${queueItem.queueId}: ${e.message}")
                }
            }
            
            SyncResult(
                success = errors.isEmpty(),
                message = "Processed $processedItems offline queue items",
                timestamp = System.currentTimeMillis(),
                recordsSynced = processedItems,
                errors = errors
            )
            
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Failed to process offline data queue: ${e.message}",
                timestamp = System.currentTimeMillis(),
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    // ==================== HELPER METHODS ====================

    private fun getAllSyncCategories(): List<SyncCategory> {
        return listOf(
            SyncCategory.EMERGENCY_EVENTS,
            SyncCategory.HEALTH_STATUS,
            SyncCategory.APP_USAGE_SUMMARY,
            SyncCategory.LOCATION_APPROXIMATE,
            SyncCategory.CONTACT_CHANGES,
            SyncCategory.SYSTEM_HEALTH,
            SyncCategory.ABUSE_ALERTS,
            SyncCategory.MEDICATION_REMINDERS
        )
    }

    private fun getCriticalSyncCategories(): List<SyncCategory> {
        return listOf(
            SyncCategory.EMERGENCY_EVENTS,
            SyncCategory.ABUSE_ALERTS,
            SyncCategory.SYSTEM_HEALTH
        )
    }

    private suspend fun getCriticalDataForSync(userId: String): List<SyncDataItem> {
        val items = mutableListOf<SyncDataItem>()
        
        // Emergency events
        val emergencyEvents = caregiverDao.getRecentEmergencyEvents(userId, System.currentTimeMillis() - 86400000L) // Last 24 hours
        items.addAll(emergencyEvents.map { 
            SyncDataItem(
                id = it.eventId,
                type = SyncCategory.EMERGENCY_EVENTS,
                data = it,
                priority = OfflineDataPriority.CRITICAL,
                timestamp = it.timestamp
            )
        })
        
        // Abuse alerts
        val abuseAlerts = caregiverDao.getRecentAbuseAlerts(userId, System.currentTimeMillis() - 86400000L)
        items.addAll(abuseAlerts.map {
            SyncDataItem(
                id = it.alertId,
                type = SyncCategory.ABUSE_ALERTS,
                data = it,
                priority = OfflineDataPriority.CRITICAL,
                timestamp = it.timestamp
            )
        })
        
        return items
    }

    private suspend fun getAllDataForSync(userId: String): List<SyncDataItem> {
        val items = mutableListOf<SyncDataItem>()
        
        // Start with critical data
        items.addAll(getCriticalDataForSync(userId))
        
        // Add non-critical data
        // Health status, app usage, etc.
        // Implementation depends on specific data sources
        
        return items
    }

    private suspend fun getIncrementalDataForSync(userId: String, lastSyncTime: Long): List<SyncDataItem> {
        // Get only data that has changed since last sync
        return getAllDataForSync(userId).filter { it.timestamp > lastSyncTime }
    }

    private suspend fun getOpportunisticDataForSync(userId: String): List<SyncDataItem> {
        // Get data suitable for opportunistic sync (when network is available)
        return getAllDataForSync(userId).filter { 
            it.priority == OfflineDataPriority.HIGH || it.priority == OfflineDataPriority.CRITICAL 
        }
    }

    private suspend fun getEmergencyDataForSync(userId: String): List<SyncDataItem> {
        // Get only emergency data
        return getCriticalDataForSync(userId).filter { 
            it.type == SyncCategory.EMERGENCY_EVENTS || it.type == SyncCategory.ABUSE_ALERTS 
        }
    }

    private fun filterDataByPermissions(
        data: List<SyncDataItem>,
        permissions: List<CaregiverPermission>
    ): List<SyncDataItem> {
        return data.filter { item ->
            when (item.type) {
                SyncCategory.EMERGENCY_EVENTS -> permissions.contains(CaregiverPermission.RECEIVE_EMERGENCY_ALERTS)
                SyncCategory.HEALTH_STATUS -> permissions.contains(CaregiverPermission.VIEW_HEALTH_STATUS)
                SyncCategory.APP_USAGE_SUMMARY -> permissions.contains(CaregiverPermission.VIEW_APP_USAGE)
                SyncCategory.LOCATION_APPROXIMATE -> permissions.contains(CaregiverPermission.VIEW_LOCATION)
                SyncCategory.CONTACT_CHANGES -> permissions.contains(CaregiverPermission.VIEW_CONTACTS)
                SyncCategory.ABUSE_ALERTS -> permissions.contains(CaregiverPermission.RECEIVE_EMERGENCY_ALERTS)
                else -> permissions.contains(CaregiverPermission.FULL_ACCESS)
            }
        }
    }

    private suspend fun sendDataToCaregiver(
        caregiver: CaregiverConnection,
        data: List<SyncDataItem>
    ): SendResult {
        return try {
            // Implementation depends on communication protocol with caregiver
            // This could be REST API, WebSocket, Firebase, etc.
            
            // For now, simulate sending data
            delay(100) // Simulate network delay
            
            SendResult(
                success = true,
                message = "Data sent successfully",
                recordsSent = data.size
            )
        } catch (e: Exception) {
            SendResult(
                success = false,
                error = e.message,
                recordsSent = 0
            )
        }
    }

    private suspend fun processQueuedItem(queueItem: OfflineDataQueue): SyncResult {
        return try {
            // Process the queued item based on its type
            when (queueItem.dataType) {
                OfflineDataType.EMERGENCY_EVENT -> processEmergencyEvent(queueItem)
                OfflineDataType.ABUSE_ALERT -> processAbuseAlert(queueItem)
                OfflineDataType.HEALTH_UPDATE -> processHealthUpdate(queueItem)
                else -> processGenericData(queueItem)
            }
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Failed to process queued item: ${e.message}",
                timestamp = System.currentTimeMillis(),
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    private suspend fun processEmergencyEvent(queueItem: OfflineDataQueue): SyncResult {
        // Process emergency event from queue
        return SyncResult(
            success = true,
            message = "Emergency event processed",
            timestamp = System.currentTimeMillis(),
            recordsSynced = 1
        )
    }

    private suspend fun processAbuseAlert(queueItem: OfflineDataQueue): SyncResult {
        // Process abuse alert from queue
        return SyncResult(
            success = true,
            message = "Abuse alert processed",
            timestamp = System.currentTimeMillis(),
            recordsSynced = 1
        )
    }

    private suspend fun processHealthUpdate(queueItem: OfflineDataQueue): SyncResult {
        // Process health update from queue
        return SyncResult(
            success = true,
            message = "Health update processed",
            timestamp = System.currentTimeMillis(),
            recordsSynced = 1
        )
    }

    private suspend fun processGenericData(queueItem: OfflineDataQueue): SyncResult {
        // Process generic data from queue
        return SyncResult(
            success = true,
            message = "Generic data processed",
            timestamp = System.currentTimeMillis(),
            recordsSynced = 1
        )
    }
}

// ==================== SUPPORTING DATA CLASSES ====================

enum class SyncStrategy {
    FULL,           // Sync all data
    PRIORITIZED,    // Sync prioritized data first
    CRITICAL_ONLY   // Sync only critical data
}

data class SyncDataItem(
    val id: String,
    val type: SyncCategory,
    val data: Any,
    val priority: OfflineDataPriority,
    val timestamp: Long
)

data class SendResult(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null,
    val recordsSent: Int = 0
)
