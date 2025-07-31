package com.naviya.launcher.monitoring

import android.content.Context
import android.util.Log
import com.naviya.launcher.config.EmergencyProductionConfig
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.emergency.data.EmergencyEvent
import com.naviya.launcher.emergency.data.EmergencyEventType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Emergency Performance Monitor
 * Tracks system performance, response times, and quality metrics
 * Provides real-time monitoring and analytics for production deployment
 */
@Singleton
class EmergencyPerformanceMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emergencyDao: EmergencyDao,
    private val config: EmergencyProductionConfig
) {
    private val monitoringScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _performanceMetrics = MutableStateFlow<PerformanceMetrics>(PerformanceMetrics())
    val performanceMetrics: Flow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    private val _systemHealth = MutableStateFlow<SystemHealth>(SystemHealth())
    val systemHealth: Flow<SystemHealth> = _systemHealth.asStateFlow()
    
    private val _alertStatus = MutableStateFlow<AlertStatus>(AlertStatus())
    val alertStatus: Flow<AlertStatus> = _alertStatus.asStateFlow()
    
    companion object {
        private const val TAG = "EmergencyPerformanceMonitor"
        private const val MONITORING_INTERVAL_MS = 60000L // 1 minute
        private const val HEALTH_CHECK_INTERVAL_MS = 300000L // 5 minutes
        private const val METRICS_RETENTION_HOURS = 168 // 7 days
    }

    init {
        startPerformanceMonitoring()
        startSystemHealthChecks()
    }

    /**
     * Start continuous performance monitoring
     */
    private fun startPerformanceMonitoring() {
        monitoringScope.launch {
            while (true) {
                try {
                    updatePerformanceMetrics()
                    checkPerformanceAlerts()
                    delay(MONITORING_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in performance monitoring", e)
                    delay(MONITORING_INTERVAL_MS)
                }
            }
        }
    }

    /**
     * Start system health checks
     */
    private fun startSystemHealthChecks() {
        monitoringScope.launch {
            while (true) {
                try {
                    updateSystemHealth()
                    checkSystemAlerts()
                    delay(HEALTH_CHECK_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in system health checks", e)
                    delay(HEALTH_CHECK_INTERVAL_MS)
                }
            }
        }
    }

    /**
     * Record emergency activation performance
     */
    suspend fun recordEmergencyActivation(
        activationTimeMs: Long,
        emergencyType: String,
        responseTimeMs: Long,
        success: Boolean,
        professionalResponseTimeMs: Long? = null
    ) {
        try {
            val performanceEvent = EmergencyPerformanceEvent(
                timestamp = System.currentTimeMillis(),
                eventType = "EMERGENCY_ACTIVATION",
                activationTimeMs = activationTimeMs,
                emergencyType = emergencyType,
                responseTimeMs = responseTimeMs,
                success = success,
                professionalResponseTimeMs = professionalResponseTimeMs
            )
            
            // Store performance event
            storePerformanceEvent(performanceEvent)
            
            // Check against performance thresholds
            checkPerformanceThresholds(performanceEvent)
            
            Log.i(TAG, "Emergency activation recorded: $emergencyType, ${activationTimeMs}ms, success: $success")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record emergency activation performance", e)
        }
    }

    /**
     * Record notification delivery performance
     */
    suspend fun recordNotificationDelivery(
        notificationType: String,
        deliveryTimeMs: Long,
        success: Boolean,
        recipientType: String
    ) {
        try {
            val performanceEvent = EmergencyPerformanceEvent(
                timestamp = System.currentTimeMillis(),
                eventType = "NOTIFICATION_DELIVERY",
                notificationType = notificationType,
                deliveryTimeMs = deliveryTimeMs,
                success = success,
                recipientType = recipientType
            )
            
            storePerformanceEvent(performanceEvent)
            
            Log.i(TAG, "Notification delivery recorded: $notificationType, ${deliveryTimeMs}ms, success: $success")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record notification delivery performance", e)
        }
    }

    /**
     * Record healthcare professional response
     */
    suspend fun recordProfessionalResponse(
        professionalId: String,
        emergencyType: String,
        responseTimeMs: Long,
        acknowledgmentTimeMs: Long,
        contactTimeMs: Long,
        outcome: String
    ) {
        try {
            val performanceEvent = EmergencyPerformanceEvent(
                timestamp = System.currentTimeMillis(),
                eventType = "PROFESSIONAL_RESPONSE",
                professionalId = professionalId,
                emergencyType = emergencyType,
                responseTimeMs = responseTimeMs,
                acknowledgmentTimeMs = acknowledgmentTimeMs,
                contactTimeMs = contactTimeMs,
                outcome = outcome,
                success = outcome == "SUCCESS"
            )
            
            storePerformanceEvent(performanceEvent)
            
            Log.i(TAG, "Professional response recorded: $professionalId, ${responseTimeMs}ms, outcome: $outcome")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record professional response performance", e)
        }
    }

    /**
     * Update performance metrics
     */
    private suspend fun updatePerformanceMetrics() {
        try {
            val last24Hours = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val performanceEvents = getPerformanceEvents(last24Hours)
            
            val emergencyActivations = performanceEvents.filter { it.eventType == "EMERGENCY_ACTIVATION" }
            val notificationDeliveries = performanceEvents.filter { it.eventType == "NOTIFICATION_DELIVERY" }
            val professionalResponses = performanceEvents.filter { it.eventType == "PROFESSIONAL_RESPONSE" }
            
            val metrics = PerformanceMetrics(
                // Emergency Activation Metrics
                totalEmergencyActivations = emergencyActivations.size,
                successfulActivations = emergencyActivations.count { it.success },
                averageActivationTimeMs = emergencyActivations.mapNotNull { it.activationTimeMs }.average().takeIf { !it.isNaN() }?.roundToInt() ?: 0,
                averageResponseTimeMs = emergencyActivations.mapNotNull { it.responseTimeMs }.average().takeIf { !it.isNaN() }?.roundToInt() ?: 0,
                
                // Notification Delivery Metrics
                totalNotifications = notificationDeliveries.size,
                successfulNotifications = notificationDeliveries.count { it.success },
                averageNotificationDeliveryTimeMs = notificationDeliveries.mapNotNull { it.deliveryTimeMs }.average().takeIf { !it.isNaN() }?.roundToInt() ?: 0,
                
                // Professional Response Metrics
                totalProfessionalResponses = professionalResponses.size,
                successfulProfessionalResponses = professionalResponses.count { it.success },
                averageProfessionalResponseTimeMs = professionalResponses.mapNotNull { it.responseTimeMs }.average().takeIf { !it.isNaN() }?.roundToInt() ?: 0,
                averageAcknowledgmentTimeMs = professionalResponses.mapNotNull { it.acknowledgmentTimeMs }.average().takeIf { !it.isNaN() }?.roundToInt() ?: 0,
                
                // Quality Metrics
                systemAvailabilityPercentage = calculateSystemAvailability(),
                errorRate = calculateErrorRate(performanceEvents),
                complianceScore = calculateComplianceScore(),
                
                // Performance by Emergency Type
                performanceByType = calculatePerformanceByType(emergencyActivations),
                
                lastUpdated = System.currentTimeMillis()
            )
            
            _performanceMetrics.value = metrics
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update performance metrics", e)
        }
    }

    /**
     * Update system health status
     */
    private suspend fun updateSystemHealth() {
        try {
            val health = SystemHealth(
                emergencySystemOperational = checkEmergencySystemHealth(),
                notificationSystemOperational = checkNotificationSystemHealth(),
                healthcareProfessionalsAvailable = checkHealthcareProfessionalAvailability(),
                complianceSystemOperational = checkComplianceSystemHealth(),
                databaseHealthy = checkDatabaseHealth(),
                apiEndpointsHealthy = checkApiEndpointsHealth(),
                
                // Resource Usage
                memoryUsagePercentage = getMemoryUsage(),
                cpuUsagePercentage = getCpuUsage(),
                diskUsagePercentage = getDiskUsage(),
                
                // Network Health
                networkLatencyMs = getNetworkLatency(),
                networkThroughputMbps = getNetworkThroughput(),
                
                lastHealthCheck = System.currentTimeMillis()
            )
            
            _systemHealth.value = health
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update system health", e)
        }
    }

    /**
     * Check performance alerts
     */
    private suspend fun checkPerformanceAlerts() {
        try {
            val metrics = _performanceMetrics.value
            val alerts = mutableListOf<Alert>()
            
            // Check activation time threshold
            if (metrics.averageActivationTimeMs > config.maxEmergencyActivationTimeMs) {
                alerts.add(Alert(
                    type = AlertType.PERFORMANCE,
                    severity = AlertSeverity.HIGH,
                    message = "Emergency activation time exceeds threshold: ${metrics.averageActivationTimeMs}ms > ${config.maxEmergencyActivationTimeMs}ms",
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            // Check notification delivery time
            if (metrics.averageNotificationDeliveryTimeMs > config.maxNotificationDeliveryTimeMs) {
                alerts.add(Alert(
                    type = AlertType.PERFORMANCE,
                    severity = AlertSeverity.HIGH,
                    message = "Notification delivery time exceeds threshold: ${metrics.averageNotificationDeliveryTimeMs}ms > ${config.maxNotificationDeliveryTimeMs}ms",
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            // Check system availability
            if (metrics.systemAvailabilityPercentage < config.minSystemAvailabilityPercentage) {
                alerts.add(Alert(
                    type = AlertType.AVAILABILITY,
                    severity = AlertSeverity.CRITICAL,
                    message = "System availability below threshold: ${metrics.systemAvailabilityPercentage}% < ${config.minSystemAvailabilityPercentage}%",
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            // Check error rate
            if (metrics.errorRate > 0.01) { // 1% error rate threshold
                alerts.add(Alert(
                    type = AlertType.ERROR_RATE,
                    severity = AlertSeverity.MEDIUM,
                    message = "Error rate exceeds threshold: ${(metrics.errorRate * 100).roundToInt()}% > 1%",
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            updateAlertStatus(alerts)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check performance alerts", e)
        }
    }

    /**
     * Check system alerts
     */
    private suspend fun checkSystemAlerts() {
        try {
            val health = _systemHealth.value
            val alerts = mutableListOf<Alert>()
            
            // Check system components
            if (!health.emergencySystemOperational) {
                alerts.add(Alert(
                    type = AlertType.SYSTEM_FAILURE,
                    severity = AlertSeverity.CRITICAL,
                    message = "Emergency system is not operational",
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            if (!health.notificationSystemOperational) {
                alerts.add(Alert(
                    type = AlertType.SYSTEM_FAILURE,
                    severity = AlertSeverity.HIGH,
                    message = "Notification system is not operational",
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            if (!health.healthcareProfessionalsAvailable) {
                alerts.add(Alert(
                    type = AlertType.PROFESSIONAL_AVAILABILITY,
                    severity = AlertSeverity.HIGH,
                    message = "No healthcare professionals available",
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            // Check resource usage
            if (health.memoryUsagePercentage > 90) {
                alerts.add(Alert(
                    type = AlertType.RESOURCE_USAGE,
                    severity = AlertSeverity.MEDIUM,
                    message = "High memory usage: ${health.memoryUsagePercentage}%",
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            if (health.cpuUsagePercentage > 80) {
                alerts.add(Alert(
                    type = AlertType.RESOURCE_USAGE,
                    severity = AlertSeverity.MEDIUM,
                    message = "High CPU usage: ${health.cpuUsagePercentage}%",
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            updateAlertStatus(alerts)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check system alerts", e)
        }
    }

    /**
     * Get real-time dashboard data
     */
    suspend fun getDashboardData(): EmergencyDashboardData {
        return try {
            val metrics = _performanceMetrics.value
            val health = _systemHealth.value
            val alerts = _alertStatus.value
            
            EmergencyDashboardData(
                performanceMetrics = metrics,
                systemHealth = health,
                alertStatus = alerts,
                recentEmergencies = getRecentEmergencies(),
                professionalResponseStats = getProfessionalResponseStats(),
                complianceStatus = getComplianceStatus(),
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get dashboard data", e)
            EmergencyDashboardData()
        }
    }

    /**
     * Generate performance report
     */
    suspend fun generatePerformanceReport(
        startTime: Long,
        endTime: Long
    ): PerformanceReport {
        return try {
            val events = getPerformanceEvents(startTime, endTime)
            
            PerformanceReport(
                reportPeriod = Pair(startTime, endTime),
                totalEmergencies = events.count { it.eventType == "EMERGENCY_ACTIVATION" },
                averageResponseTime = events.filter { it.eventType == "EMERGENCY_ACTIVATION" }
                    .mapNotNull { it.responseTimeMs }.average().takeIf { !it.isNaN() }?.roundToInt() ?: 0,
                successRate = events.filter { it.eventType == "EMERGENCY_ACTIVATION" }
                    .let { activations -> 
                        if (activations.isEmpty()) 100.0 
                        else (activations.count { it.success }.toDouble() / activations.size) * 100
                    },
                professionalResponseStats = generateProfessionalStats(events),
                notificationStats = generateNotificationStats(events),
                complianceMetrics = generateComplianceMetrics(events),
                recommendations = generateRecommendations(events),
                generatedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate performance report", e)
            PerformanceReport()
        }
    }

    // Helper methods for health checks and calculations
    private suspend fun checkEmergencySystemHealth(): Boolean = true // Mock implementation
    private suspend fun checkNotificationSystemHealth(): Boolean = true // Mock implementation
    private suspend fun checkHealthcareProfessionalAvailability(): Boolean = true // Mock implementation
    private suspend fun checkComplianceSystemHealth(): Boolean = true // Mock implementation
    private suspend fun checkDatabaseHealth(): Boolean = true // Mock implementation
    private suspend fun checkApiEndpointsHealth(): Boolean = true // Mock implementation
    
    private fun getMemoryUsage(): Int = 45 // Mock implementation
    private fun getCpuUsage(): Int = 25 // Mock implementation
    private fun getDiskUsage(): Int = 60 // Mock implementation
    private fun getNetworkLatency(): Int = 50 // Mock implementation
    private fun getNetworkThroughput(): Double = 100.0 // Mock implementation
    
    private fun calculateSystemAvailability(): Double = 99.9 // Mock implementation
    private fun calculateErrorRate(events: List<EmergencyPerformanceEvent>): Double = 0.005 // Mock implementation
    private fun calculateComplianceScore(): Double = 95.0 // Mock implementation
    
    private fun calculatePerformanceByType(events: List<EmergencyPerformanceEvent>): Map<String, PerformanceByType> {
        return events.groupBy { it.emergencyType ?: "UNKNOWN" }.mapValues { (_, typeEvents) ->
            PerformanceByType(
                count = typeEvents.size,
                averageResponseTime = typeEvents.mapNotNull { it.responseTimeMs }.average().takeIf { !it.isNaN() }?.roundToInt() ?: 0,
                successRate = if (typeEvents.isEmpty()) 100.0 else (typeEvents.count { it.success }.toDouble() / typeEvents.size) * 100
            )
        }
    }
    
    private suspend fun getPerformanceEvents(since: Long, until: Long = System.currentTimeMillis()): List<EmergencyPerformanceEvent> {
        // Mock implementation - in real app, query from database
        return emptyList()
    }
    
    private suspend fun getPerformanceEvents(since: Long): List<EmergencyPerformanceEvent> {
        return getPerformanceEvents(since, System.currentTimeMillis())
    }
    
    private suspend fun storePerformanceEvent(event: EmergencyPerformanceEvent) {
        // Mock implementation - in real app, store in database
        Log.d(TAG, "Storing performance event: ${event.eventType}")
    }
    
    private fun checkPerformanceThresholds(event: EmergencyPerformanceEvent) {
        // Check if event exceeds performance thresholds and trigger alerts
    }
    
    private fun updateAlertStatus(alerts: List<Alert>) {
        val currentAlerts = _alertStatus.value.activeAlerts.toMutableList()
        
        // Add new alerts
        alerts.forEach { newAlert ->
            if (!currentAlerts.any { it.message == newAlert.message && it.type == newAlert.type }) {
                currentAlerts.add(newAlert)
            }
        }
        
        // Remove resolved alerts (older than 1 hour with no new occurrences)
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        currentAlerts.removeAll { it.timestamp < oneHourAgo }
        
        _alertStatus.value = AlertStatus(
            activeAlerts = currentAlerts,
            criticalAlertCount = currentAlerts.count { it.severity == AlertSeverity.CRITICAL },
            highAlertCount = currentAlerts.count { it.severity == AlertSeverity.HIGH },
            mediumAlertCount = currentAlerts.count { it.severity == AlertSeverity.MEDIUM },
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private suspend fun getRecentEmergencies(): List<RecentEmergency> = emptyList() // Mock implementation
    private suspend fun getProfessionalResponseStats(): ProfessionalResponseStats = ProfessionalResponseStats() // Mock implementation
    private suspend fun getComplianceStatus(): ComplianceStatus = ComplianceStatus() // Mock implementation
    
    private fun generateProfessionalStats(events: List<EmergencyPerformanceEvent>): ProfessionalResponseStats = ProfessionalResponseStats() // Mock implementation
    private fun generateNotificationStats(events: List<EmergencyPerformanceEvent>): NotificationStats = NotificationStats() // Mock implementation
    private fun generateComplianceMetrics(events: List<EmergencyPerformanceEvent>): ComplianceMetrics = ComplianceMetrics() // Mock implementation
    private fun generateRecommendations(events: List<EmergencyPerformanceEvent>): List<String> = emptyList() // Mock implementation
}

// Data classes for monitoring system
data class PerformanceMetrics(
    val totalEmergencyActivations: Int = 0,
    val successfulActivations: Int = 0,
    val averageActivationTimeMs: Int = 0,
    val averageResponseTimeMs: Int = 0,
    val totalNotifications: Int = 0,
    val successfulNotifications: Int = 0,
    val averageNotificationDeliveryTimeMs: Int = 0,
    val totalProfessionalResponses: Int = 0,
    val successfulProfessionalResponses: Int = 0,
    val averageProfessionalResponseTimeMs: Int = 0,
    val averageAcknowledgmentTimeMs: Int = 0,
    val systemAvailabilityPercentage: Double = 0.0,
    val errorRate: Double = 0.0,
    val complianceScore: Double = 0.0,
    val performanceByType: Map<String, PerformanceByType> = emptyMap(),
    val lastUpdated: Long = 0
)

data class PerformanceByType(
    val count: Int,
    val averageResponseTime: Int,
    val successRate: Double
)

data class SystemHealth(
    val emergencySystemOperational: Boolean = false,
    val notificationSystemOperational: Boolean = false,
    val healthcareProfessionalsAvailable: Boolean = false,
    val complianceSystemOperational: Boolean = false,
    val databaseHealthy: Boolean = false,
    val apiEndpointsHealthy: Boolean = false,
    val memoryUsagePercentage: Int = 0,
    val cpuUsagePercentage: Int = 0,
    val diskUsagePercentage: Int = 0,
    val networkLatencyMs: Int = 0,
    val networkThroughputMbps: Double = 0.0,
    val lastHealthCheck: Long = 0
)

data class AlertStatus(
    val activeAlerts: List<Alert> = emptyList(),
    val criticalAlertCount: Int = 0,
    val highAlertCount: Int = 0,
    val mediumAlertCount: Int = 0,
    val lastUpdated: Long = 0
)

data class Alert(
    val type: AlertType,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long
)

enum class AlertType {
    PERFORMANCE, AVAILABILITY, ERROR_RATE, SYSTEM_FAILURE, 
    PROFESSIONAL_AVAILABILITY, RESOURCE_USAGE, COMPLIANCE
}

enum class AlertSeverity {
    CRITICAL, HIGH, MEDIUM, LOW
}

data class EmergencyPerformanceEvent(
    val timestamp: Long,
    val eventType: String,
    val activationTimeMs: Long? = null,
    val emergencyType: String? = null,
    val responseTimeMs: Long? = null,
    val success: Boolean = false,
    val professionalResponseTimeMs: Long? = null,
    val notificationType: String? = null,
    val deliveryTimeMs: Long? = null,
    val recipientType: String? = null,
    val professionalId: String? = null,
    val acknowledgmentTimeMs: Long? = null,
    val contactTimeMs: Long? = null,
    val outcome: String? = null
)

data class EmergencyDashboardData(
    val performanceMetrics: PerformanceMetrics = PerformanceMetrics(),
    val systemHealth: SystemHealth = SystemHealth(),
    val alertStatus: AlertStatus = AlertStatus(),
    val recentEmergencies: List<RecentEmergency> = emptyList(),
    val professionalResponseStats: ProfessionalResponseStats = ProfessionalResponseStats(),
    val complianceStatus: ComplianceStatus = ComplianceStatus(),
    val timestamp: Long = 0
)

data class RecentEmergency(
    val id: String = "",
    val type: String = "",
    val timestamp: Long = 0,
    val responseTime: Int = 0,
    val outcome: String = ""
)

data class ProfessionalResponseStats(
    val totalResponses: Int = 0,
    val averageResponseTime: Int = 0,
    val availableProfessionals: Int = 0
)

data class ComplianceStatus(
    val overallScore: Double = 0.0,
    val hipaaCompliant: Boolean = false,
    val gdprCompliant: Boolean = false,
    val clinicalGovernanceCompliant: Boolean = false
)

data class PerformanceReport(
    val reportPeriod: Pair<Long, Long> = Pair(0L, 0L),
    val totalEmergencies: Int = 0,
    val averageResponseTime: Int = 0,
    val successRate: Double = 0.0,
    val professionalResponseStats: ProfessionalResponseStats = ProfessionalResponseStats(),
    val notificationStats: NotificationStats = NotificationStats(),
    val complianceMetrics: ComplianceMetrics = ComplianceMetrics(),
    val recommendations: List<String> = emptyList(),
    val generatedAt: Long = 0
)

data class NotificationStats(
    val totalSent: Int = 0,
    val successRate: Double = 0.0,
    val averageDeliveryTime: Int = 0
)

data class ComplianceMetrics(
    val auditTrailCompleteness: Double = 0.0,
    val dataProtectionCompliance: Double = 0.0,
    val professionalValidationRate: Double = 0.0
)
