package com.naviya.launcher.caregiver

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.naviya.launcher.caregiver.data.*
import com.naviya.launcher.emergency.EmergencyService
import com.naviya.launcher.elderrights.ElderRightsAdvocateService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first caregiver connectivity service
 * Manages opportunistic sync, heartbeat monitoring, and multi-channel emergency alerts
 * Designed to work reliably even with poor or intermittent internet connectivity
 */
@Singleton
class OfflineCaregiverConnectivityService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val caregiverDao: CaregiverDao,
    private val emergencyService: EmergencyService,
    private val elderRightsService: ElderRightsAdvocateService,
    private val syncManager: CaregiverSyncManager,
    private val heartbeatManager: CaregiverHeartbeatManager,
    private val emergencyAlertManager: MultiChannelEmergencyAlertManager
) {

    companion object {
        private const val HEARTBEAT_INTERVAL = TimeUnit.MINUTES.toMillis(5) // 5 minutes
        private const val SYNC_RETRY_INTERVAL = TimeUnit.MINUTES.toMillis(15) // 15 minutes
        private const val OFFLINE_THRESHOLD = TimeUnit.MINUTES.toMillis(30) // 30 minutes
        private const val EMERGENCY_RETRY_INTERVAL = TimeUnit.SECONDS.toMillis(30) // 30 seconds
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _connectivityState = MutableStateFlow(CaregiverConnectivityState())
    val connectivityState: StateFlow<CaregiverConnectivityState> = _connectivityState.asStateFlow()

    private val _networkState = MutableStateFlow(NetworkState.UNKNOWN)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var heartbeatJob: Job? = null
    private var syncJob: Job? = null
    private var emergencyMonitoringJob: Job? = null

    /**
     * Initialize the offline-first connectivity service
     */
    fun initialize() {
        serviceScope.launch {
            setupNetworkMonitoring()
            startHeartbeatMonitoring()
            startOpportunisticSync()
            startEmergencyMonitoring()
            
            // Load initial connectivity state
            loadConnectivityState()
        }
    }

    /**
     * Setup network connectivity monitoring
     */
    private fun setupNetworkMonitoring() {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                serviceScope.launch {
                    _networkState.value = NetworkState.CONNECTED
                    onNetworkAvailable()
                }
            }

            override fun onLost(network: Network) {
                serviceScope.launch {
                    _networkState.value = NetworkState.DISCONNECTED
                    onNetworkLost()
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                serviceScope.launch {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    
                    _networkState.value = when {
                        hasInternet && isValidated -> NetworkState.CONNECTED
                        hasInternet -> NetworkState.LIMITED
                        else -> NetworkState.DISCONNECTED
                    }
                    
                    onNetworkCapabilitiesChanged(networkCapabilities)
                }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    /**
     * Start heartbeat monitoring for all connected caregivers
     */
    private fun startHeartbeatMonitoring() {
        heartbeatJob = serviceScope.launch {
            while (isActive) {
                try {
                    performHeartbeatCheck()
                    delay(HEARTBEAT_INTERVAL)
                } catch (e: Exception) {
                    // Log error but continue monitoring
                    delay(HEARTBEAT_INTERVAL)
                }
            }
        }
    }

    /**
     * Start opportunistic synchronization
     */
    private fun startOpportunisticSync() {
        syncJob = serviceScope.launch {
            // Monitor network state changes for sync opportunities
            networkState.collect { state ->
                when (state) {
                    NetworkState.CONNECTED -> {
                        performOpportunisticSync()
                    }
                    NetworkState.LIMITED -> {
                        performLimitedSync()
                    }
                    NetworkState.DISCONNECTED -> {
                        handleOfflineMode()
                    }
                    NetworkState.UNKNOWN -> {
                        // Wait for network state to be determined
                    }
                }
            }
        }
    }

    /**
     * Start emergency monitoring for critical situations
     */
    private fun startEmergencyMonitoring() {
        emergencyMonitoringJob = serviceScope.launch {
            // Monitor for emergency situations that require immediate caregiver notification
            emergencyService.emergencyEvents.collect { event ->
                when (event.type) {
                    EmergencyEventType.PANIC_MODE_ACTIVATED -> {
                        handleEmergencyAlert(event, EmergencyAlertPriority.CRITICAL)
                    }
                    EmergencyEventType.ABUSE_DETECTED -> {
                        handleEmergencyAlert(event, EmergencyAlertPriority.HIGH)
                    }
                    EmergencyEventType.CONTACT_PROTECTION_VIOLATED -> {
                        handleEmergencyAlert(event, EmergencyAlertPriority.MEDIUM)
                    }
                    EmergencyEventType.SYSTEM_HEALTH_CRITICAL -> {
                        handleEmergencyAlert(event, EmergencyAlertPriority.LOW)
                    }
                }
            }
        }
    }

    /**
     * Perform heartbeat check for all caregivers
     */
    private suspend fun performHeartbeatCheck() {
        val connectedCaregivers = caregiverDao.getConnectedCaregivers()
        val currentTime = System.currentTimeMillis()
        
        connectedCaregivers.forEach { caregiver ->
            try {
                val heartbeatResult = heartbeatManager.sendHeartbeat(caregiver.caregiverId)
                
                // Update caregiver connection status
                val updatedStatus = when {
                    heartbeatResult.success -> CaregiverConnectionStatus.ONLINE
                    currentTime - caregiver.lastHeartbeat > OFFLINE_THRESHOLD -> CaregiverConnectionStatus.OFFLINE
                    else -> CaregiverConnectionStatus.LIMITED
                }
                
                caregiverDao.updateCaregiverConnectionStatus(
                    caregiver.caregiverId,
                    updatedStatus,
                    if (heartbeatResult.success) currentTime else caregiver.lastHeartbeat
                )
                
                // Update connectivity state
                updateConnectivityState()
                
            } catch (e: Exception) {
                // Mark caregiver as having connection issues
                caregiverDao.updateCaregiverConnectionStatus(
                    caregiver.caregiverId,
                    CaregiverConnectionStatus.ERROR,
                    caregiver.lastHeartbeat
                )
            }
        }
    }

    /**
     * Perform opportunistic synchronization when network is available
     */
    private suspend fun performOpportunisticSync() {
        try {
            // Sync pending data with caregivers
            val syncResult = syncManager.performFullSync()
            
            if (syncResult.success) {
                updateLastSyncTime()
                
                // Notify caregivers of successful sync
                notifyCaregivers(
                    message = "Sync completed successfully",
                    priority = NotificationPriority.LOW
                )
            } else {
                // Schedule retry
                scheduleRetrySync()
            }
            
        } catch (e: Exception) {
            // Handle sync failure
            handleSyncFailure(e)
        }
    }

    /**
     * Perform limited sync with essential data only
     */
    private suspend fun performLimitedSync() {
        try {
            // Sync only critical data (emergency events, alerts)
            val syncResult = syncManager.performCriticalSync()
            
            if (syncResult.success) {
                updateLastSyncTime()
            }
            
        } catch (e: Exception) {
            // Even limited sync failed - full offline mode
            handleOfflineMode()
        }
    }

    /**
     * Handle offline mode operations
     */
    private suspend fun handleOfflineMode() {
        // Update connectivity state
        _connectivityState.value = _connectivityState.value.copy(
            isOnline = false,
            lastOnlineTime = System.currentTimeMillis(),
            offlineCapabilities = getOfflineCapabilities()
        )
        
        // Enable offline-only features
        enableOfflineMode()
        
        // Queue data for sync when connection returns
        queuePendingData()
    }

    /**
     * Handle emergency alerts with multi-channel approach
     */
    private suspend fun handleEmergencyAlert(
        event: EmergencyEvent,
        priority: EmergencyAlertPriority
    ) {
        try {
            // Create emergency alert
            val alert = EmergencyAlert(
                alertId = "emergency-${System.nanoTime()}",
                userId = event.userId,
                eventType = event.type,
                priority = priority,
                message = event.message,
                timestamp = System.currentTimeMillis(),
                channels = mutableListOf(),
                status = EmergencyAlertStatus.PENDING
            )
            
            // Send through multiple channels based on priority and network state
            val channelResults = when (priority) {
                EmergencyAlertPriority.CRITICAL -> {
                    sendCriticalEmergencyAlert(alert)
                }
                EmergencyAlertPriority.HIGH -> {
                    sendHighPriorityEmergencyAlert(alert)
                }
                EmergencyAlertPriority.MEDIUM -> {
                    sendMediumPriorityEmergencyAlert(alert)
                }
                EmergencyAlertPriority.LOW -> {
                    sendLowPriorityEmergencyAlert(alert)
                }
            }
            
            // Update alert with results
            alert.channels = channelResults
            alert.status = if (channelResults.any { it.success }) {
                EmergencyAlertStatus.SENT
            } else {
                EmergencyAlertStatus.FAILED
            }
            
            // Store alert
            caregiverDao.insertEmergencyAlert(alert)
            
            // If critical and failed, escalate to elder rights
            if (priority == EmergencyAlertPriority.CRITICAL && alert.status == EmergencyAlertStatus.FAILED) {
                escalateToElderRights(alert)
            }
            
        } catch (e: Exception) {
            // Emergency alert failed - escalate immediately
            escalateToElderRights(
                EmergencyAlert(
                    alertId = "emergency-escalation-${System.nanoTime()}",
                    userId = event.userId,
                    eventType = event.type,
                    priority = EmergencyAlertPriority.CRITICAL,
                    message = "Emergency alert system failure: ${e.message}",
                    timestamp = System.currentTimeMillis(),
                    channels = emptyList(),
                    status = EmergencyAlertStatus.FAILED
                )
            )
        }
    }

    /**
     * Send critical emergency alert through all available channels
     */
    private suspend fun sendCriticalEmergencyAlert(alert: EmergencyAlert): List<EmergencyChannelResult> {
        val results = mutableListOf<EmergencyChannelResult>()
        
        // 1. SMS (works with minimal connectivity)
        results.add(emergencyAlertManager.sendEmergencySMS(alert))
        
        // 2. Phone call (if network available)
        if (networkState.value != NetworkState.DISCONNECTED) {
            results.add(emergencyAlertManager.initiateEmergencyCall(alert))
        }
        
        // 3. Push notification (if connected)
        if (networkState.value == NetworkState.CONNECTED) {
            results.add(emergencyAlertManager.sendPushNotification(alert))
        }
        
        // 4. Local notification (always works)
        results.add(emergencyAlertManager.sendLocalNotification(alert))
        
        // 5. Backup SMS to all emergency contacts
        results.addAll(emergencyAlertManager.sendBackupSMS(alert))
        
        return results
    }

    /**
     * Send high priority emergency alert
     */
    private suspend fun sendHighPriorityEmergencyAlert(alert: EmergencyAlert): List<EmergencyChannelResult> {
        val results = mutableListOf<EmergencyChannelResult>()
        
        // 1. SMS
        results.add(emergencyAlertManager.sendEmergencySMS(alert))
        
        // 2. Push notification (if connected)
        if (networkState.value == NetworkState.CONNECTED) {
            results.add(emergencyAlertManager.sendPushNotification(alert))
        }
        
        // 3. Local notification
        results.add(emergencyAlertManager.sendLocalNotification(alert))
        
        return results
    }

    /**
     * Send medium priority emergency alert
     */
    private suspend fun sendMediumPriorityEmergencyAlert(alert: EmergencyAlert): List<EmergencyChannelResult> {
        val results = mutableListOf<EmergencyChannelResult>()
        
        // 1. Push notification (if connected)
        if (networkState.value == NetworkState.CONNECTED) {
            results.add(emergencyAlertManager.sendPushNotification(alert))
        } else {
            // Fallback to SMS if not connected
            results.add(emergencyAlertManager.sendEmergencySMS(alert))
        }
        
        // 2. Local notification
        results.add(emergencyAlertManager.sendLocalNotification(alert))
        
        return results
    }

    /**
     * Send low priority emergency alert
     */
    private suspend fun sendLowPriorityEmergencyAlert(alert: EmergencyAlert): List<EmergencyChannelResult> {
        val results = mutableListOf<EmergencyChannelResult>()
        
        // 1. Local notification (always works)
        results.add(emergencyAlertManager.sendLocalNotification(alert))
        
        // 2. Push notification (if connected)
        if (networkState.value == NetworkState.CONNECTED) {
            results.add(emergencyAlertManager.sendPushNotification(alert))
        }
        
        return results
    }

    /**
     * Escalate to elder rights advocate when caregiver alerts fail
     */
    private suspend fun escalateToElderRights(alert: EmergencyAlert) {
        try {
            elderRightsService.notifyElderRightsAdvocate(
                userId = alert.userId,
                alertId = alert.alertId,
                message = "Caregiver emergency alert failed: ${alert.message}",
                priority = "IMMEDIATE"
            )
        } catch (e: Exception) {
            // Last resort - log for manual intervention
            caregiverDao.insertFailedEscalation(
                FailedEscalation(
                    escalationId = "failed-${System.nanoTime()}",
                    originalAlertId = alert.alertId,
                    userId = alert.userId,
                    failureReason = e.message ?: "Unknown error",
                    timestamp = System.currentTimeMillis(),
                    requiresManualIntervention = true
                )
            )
        }
    }

    /**
     * Handle network becoming available
     */
    private suspend fun onNetworkAvailable() {
        // Update connectivity state
        _connectivityState.value = _connectivityState.value.copy(
            isOnline = true,
            lastOnlineTime = System.currentTimeMillis()
        )
        
        // Trigger immediate sync of pending data
        performOpportunisticSync()
        
        // Send queued emergency alerts
        sendQueuedEmergencyAlerts()
        
        // Notify caregivers that connection is restored
        notifyCaregivers(
            message = "Connection restored - syncing data",
            priority = NotificationPriority.LOW
        )
    }

    /**
     * Handle network being lost
     */
    private suspend fun onNetworkLost() {
        // Update connectivity state
        _connectivityState.value = _connectivityState.value.copy(
            isOnline = false,
            lastOfflineTime = System.currentTimeMillis()
        )
        
        // Switch to offline mode
        handleOfflineMode()
    }

    /**
     * Handle network capabilities changing
     */
    private suspend fun onNetworkCapabilitiesChanged(capabilities: NetworkCapabilities) {
        val connectionQuality = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionQuality.HIGH
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ConnectionQuality.MEDIUM
                    else -> ConnectionQuality.LOW
                }
            }
            else -> ConnectionQuality.LOW
        }
        
        // Update connectivity state with quality information
        _connectivityState.value = _connectivityState.value.copy(
            connectionQuality = connectionQuality
        )
        
        // Adjust sync strategy based on connection quality
        adjustSyncStrategy(connectionQuality)
    }

    /**
     * Get current caregiver connectivity status
     */
    suspend fun getCaregiverConnectivityStatus(userId: String): CaregiverConnectivityStatus {
        val connectedCaregivers = caregiverDao.getConnectedCaregivers(userId)
        val onlineCaregivers = connectedCaregivers.count { it.connectionStatus == CaregiverConnectionStatus.ONLINE }
        val totalCaregivers = connectedCaregivers.size
        
        return CaregiverConnectivityStatus(
            totalCaregivers = totalCaregivers,
            onlineCaregivers = onlineCaregivers,
            offlineCaregivers = totalCaregivers - onlineCaregivers,
            lastSyncTime = getLastSyncTime(),
            isEmergencyCapable = hasEmergencyCapability(),
            networkState = networkState.value
        )
    }

    /**
     * Force sync with caregivers
     */
    suspend fun forceSyncWithCaregivers(userId: String): SyncResult {
        return try {
            syncManager.performForcedSync(userId)
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Forced sync failed: ${e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send test emergency alert to verify connectivity
     */
    suspend fun sendTestEmergencyAlert(userId: String): EmergencyAlertResult {
        val testAlert = EmergencyAlert(
            alertId = "test-${System.nanoTime()}",
            userId = userId,
            eventType = EmergencyEventType.SYSTEM_TEST,
            priority = EmergencyAlertPriority.LOW,
            message = "Test emergency alert - please confirm receipt",
            timestamp = System.currentTimeMillis(),
            channels = mutableListOf(),
            status = EmergencyAlertStatus.PENDING
        )
        
        val results = sendLowPriorityEmergencyAlert(testAlert)
        
        return EmergencyAlertResult(
            success = results.any { it.success },
            channelsUsed = results.map { it.channel },
            message = if (results.any { it.success }) {
                "Test alert sent successfully"
            } else {
                "Test alert failed on all channels"
            }
        )
    }

    /**
     * Cleanup and shutdown the service
     */
    fun shutdown() {
        heartbeatJob?.cancel()
        syncJob?.cancel()
        emergencyMonitoringJob?.cancel()
        
        networkCallback?.let { callback ->
            connectivityManager?.unregisterNetworkCallback(callback)
        }
        
        serviceScope.cancel()
    }

    // ==================== HELPER METHODS ====================

    private suspend fun loadConnectivityState() {
        // Load initial state from database
        val lastSync = getLastSyncTime()
        val offlineCapabilities = getOfflineCapabilities()
        
        _connectivityState.value = CaregiverConnectivityState(
            isOnline = networkState.value == NetworkState.CONNECTED,
            lastSyncTime = lastSync,
            offlineCapabilities = offlineCapabilities
        )
    }

    private suspend fun updateConnectivityState() {
        val currentState = _connectivityState.value
        val connectedCaregivers = caregiverDao.getConnectedCaregivers()
        
        _connectivityState.value = currentState.copy(
            connectedCaregivers = connectedCaregivers.size,
            onlineCaregivers = connectedCaregivers.count { 
                it.connectionStatus == CaregiverConnectionStatus.ONLINE 
            }
        )
    }

    private suspend fun getLastSyncTime(): Long {
        return caregiverDao.getLastSyncTime() ?: 0L
    }

    private suspend fun updateLastSyncTime() {
        caregiverDao.updateLastSyncTime(System.currentTimeMillis())
    }

    private fun getOfflineCapabilities(): List<OfflineCapability> {
        return listOf(
            OfflineCapability.LOCAL_NOTIFICATIONS,
            OfflineCapability.SMS_EMERGENCY_ALERTS,
            OfflineCapability.LOCAL_DATA_STORAGE,
            OfflineCapability.PANIC_MODE,
            OfflineCapability.CONTACT_PROTECTION,
            OfflineCapability.ABUSE_DETECTION
        )
    }

    private suspend fun enableOfflineMode() {
        // Configure system for offline operation
        caregiverDao.updateSystemMode(SystemMode.OFFLINE)
    }

    private suspend fun queuePendingData() {
        // Queue data that needs to be synced when connection returns
        // This is handled by the SyncManager
    }

    private suspend fun scheduleRetrySync() {
        delay(SYNC_RETRY_INTERVAL)
        if (networkState.value != NetworkState.DISCONNECTED) {
            performOpportunisticSync()
        }
    }

    private suspend fun handleSyncFailure(error: Exception) {
        caregiverDao.insertSyncFailure(
            SyncFailure(
                failureId = "sync-failure-${System.nanoTime()}",
                timestamp = System.currentTimeMillis(),
                error = error.message ?: "Unknown sync error",
                retryScheduled = true
            )
        )
    }

    private suspend fun sendQueuedEmergencyAlerts() {
        val queuedAlerts = caregiverDao.getQueuedEmergencyAlerts()
        queuedAlerts.forEach { alert ->
            handleEmergencyAlert(
                EmergencyEvent(
                    eventId = alert.alertId,
                    userId = alert.userId,
                    type = alert.eventType,
                    message = alert.message,
                    timestamp = alert.timestamp
                ),
                alert.priority
            )
        }
    }

    private suspend fun notifyCaregivers(message: String, priority: NotificationPriority) {
        val connectedCaregivers = caregiverDao.getConnectedCaregivers()
        connectedCaregivers.forEach { caregiver ->
            // Send notification to caregiver
            // Implementation depends on notification system
        }
    }

    private suspend fun adjustSyncStrategy(quality: ConnectionQuality) {
        when (quality) {
            ConnectionQuality.HIGH -> {
                // Full sync with all data
                syncManager.setSyncStrategy(SyncStrategy.FULL)
            }
            ConnectionQuality.MEDIUM -> {
                // Prioritized sync with essential data first
                syncManager.setSyncStrategy(SyncStrategy.PRIORITIZED)
            }
            ConnectionQuality.LOW -> {
                // Critical data only
                syncManager.setSyncStrategy(SyncStrategy.CRITICAL_ONLY)
            }
        }
    }

    private suspend fun hasEmergencyCapability(): Boolean {
        return networkState.value != NetworkState.DISCONNECTED || 
               emergencyAlertManager.hasSMSCapability()
    }
}
