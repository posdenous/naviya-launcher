package com.naviya.launcher.abuse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naviya.launcher.abuse.RuleBasedAbuseDetector
import com.naviya.launcher.abuse.data.*
import com.naviya.launcher.caregiver.CaregiverPermissionManager
import com.naviya.launcher.contacts.ContactProtectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ViewModel for abuse detection dashboard
 * Manages dashboard state and coordinates with abuse detection services
 */
@HiltViewModel
class AbuseDetectionDashboardViewModel @Inject constructor(
    private val abuseDetector: RuleBasedAbuseDetector,
    private val abuseDao: AbuseDetectionDao,
    private val contactProtectionManager: ContactProtectionManager,
    private val caregiverPermissionManager: CaregiverPermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboardData(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load system health
                val systemHealth = loadSystemHealth()
                
                // Load quick stats
                val quickStats = loadQuickStats(userId)
                
                // Load active alerts
                val activeAlerts = loadActiveAlerts(userId)
                
                // Load recent activity
                val recentActivity = loadRecentActivity(userId)
                
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    systemHealth = systemHealth,
                    quickStats = quickStats,
                    activeAlerts = activeAlerts,
                    recentActivity = recentActivity
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load dashboard data: ${e.message}"
                )
            }
        }
    }

    fun refreshData(userId: String) {
        loadDashboardData(userId)
    }

    fun handleAlertClick(alert: AbuseAlert) {
        viewModelScope.launch {
            // Mark alert as viewed
            abuseDao.updateAlertStatus(alert.alertId, AbuseAlertStatus.VIEWED)
            
            // Refresh active alerts
            val userId = alert.userId
            val updatedAlerts = loadActiveAlerts(userId)
            _uiState.value = _uiState.value.copy(activeAlerts = updatedAlerts)
        }
    }

    fun openSystemSettings() {
        // Navigate to system settings - implementation depends on navigation setup
        // This would typically trigger a navigation event
    }

    private suspend fun loadSystemHealth(): SystemHealthStatus {
        return try {
            val health = abuseDao.getDetectionSystemHealth()
            when {
                health?.isHealthy == true -> SystemHealthStatus.HEALTHY
                health?.lastAssessmentTime != null && 
                (System.currentTimeMillis() - health.lastAssessmentTime) > TimeUnit.HOURS.toMillis(24) -> 
                    SystemHealthStatus.WARNING
                else -> SystemHealthStatus.ERROR
            }
        } catch (e: Exception) {
            SystemHealthStatus.ERROR
        }
    }

    private suspend fun loadQuickStats(userId: String): DashboardQuickStats {
        return try {
            val activeAlerts = abuseDao.getActiveAlerts(userId)
            val caregiversMonitored = caregiverPermissionManager.getMonitoredCaregivers(userId).size
            val protectionLevel = determineOverallProtectionLevel(userId)
            
            DashboardQuickStats(
                activeAlerts = activeAlerts.size,
                caregiversMonitored = caregiversMonitored,
                overallProtectionLevel = protectionLevel
            )
        } catch (e: Exception) {
            DashboardQuickStats()
        }
    }

    private suspend fun loadActiveAlerts(userId: String): List<AbuseAlert> {
        return try {
            abuseDao.getActiveAlerts(userId)
                .sortedByDescending { alert ->
                    // Sort by priority: immediate action first, then by risk level, then by timestamp
                    when {
                        alert.requiresImmedateAction -> 1000 + alert.riskLevel.ordinal
                        else -> alert.riskLevel.ordinal
                    }
                }
                .take(10) // Limit to most recent/important alerts
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun loadRecentActivity(userId: String): List<ActivitySummary> {
        return try {
            val activities = mutableListOf<ActivitySummary>()
            
            // Recent risk assessments
            val recentAssessments = abuseDao.getRecentRiskAssessments(
                caregiverId = null, // Get all caregivers
                userId = userId,
                timeWindow = TimeUnit.DAYS.toMillis(7)
            )
            
            recentAssessments.forEach { assessment ->
                activities.add(
                    ActivitySummary(
                        type = ActivityType.RISK_ASSESSMENT,
                        description = "Risk assessment completed for caregiver (${assessment.riskLevel.name} risk)",
                        timestamp = assessment.assessmentTimestamp
                    )
                )
            }
            
            // Recent alerts
            val recentAlerts = abuseDao.getRecentAlerts(userId, TimeUnit.DAYS.toMillis(7))
            recentAlerts.forEach { alert ->
                activities.add(
                    ActivitySummary(
                        type = ActivityType.ALERT_GENERATED,
                        description = "Alert generated: ${alert.message}",
                        timestamp = alert.createdTimestamp
                    )
                )
            }
            
            // Recent contact protection events
            val recentContactEvents = contactProtectionManager.getRecentProtectionEvents(
                userId, TimeUnit.DAYS.toMillis(7)
            )
            recentContactEvents.forEach { event ->
                activities.add(
                    ActivitySummary(
                        type = ActivityType.CONTACT_PROTECTION,
                        description = "Contact protection: ${event.description}",
                        timestamp = event.timestamp
                    )
                )
            }
            
            // Recent permission denials
            val recentPermissionDenials = caregiverPermissionManager.getRecentPermissionDenials(
                userId, TimeUnit.DAYS.toMillis(7)
            )
            recentPermissionDenials.forEach { denial ->
                activities.add(
                    ActivitySummary(
                        type = ActivityType.PERMISSION_DENIED,
                        description = "Permission denied: ${denial.permissionRequested}",
                        timestamp = denial.timestamp
                    )
                )
            }
            
            // Sort by timestamp (most recent first) and limit
            activities.sortedByDescending { it.timestamp }.take(20)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun determineOverallProtectionLevel(userId: String): ProtectionLevel {
        return try {
            val recentAlerts = abuseDao.getActiveAlerts(userId)
            val criticalAlerts = recentAlerts.count { it.riskLevel >= AbuseRiskLevel.HIGH }
            val mediumAlerts = recentAlerts.count { it.riskLevel == AbuseRiskLevel.MEDIUM }
            
            when {
                criticalAlerts > 0 -> ProtectionLevel.LOW // High alerts mean protection is needed
                mediumAlerts > 2 -> ProtectionLevel.MEDIUM
                else -> ProtectionLevel.HIGH // No alerts means good protection
            }
        } catch (e: Exception) {
            ProtectionLevel.MEDIUM
        }
    }
}

/**
 * UI state for the abuse detection dashboard
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val systemHealth: SystemHealthStatus = SystemHealthStatus.HEALTHY,
    val quickStats: DashboardQuickStats = DashboardQuickStats(),
    val activeAlerts: List<AbuseAlert> = emptyList(),
    val recentActivity: List<ActivitySummary> = emptyList(),
    val error: String? = null
)

/**
 * Extension functions for DAO to support dashboard queries
 */
suspend fun AbuseDetectionDao.getRecentAlerts(userId: String, timeWindow: Long): List<AbuseAlert> {
    val cutoffTime = System.currentTimeMillis() - timeWindow
    return getActiveAlerts(userId).filter { it.createdTimestamp >= cutoffTime }
}

/**
 * Data classes for recent activity tracking
 */
data class ContactProtectionEvent(
    val description: String,
    val timestamp: Long
)

data class PermissionDenialEvent(
    val permissionRequested: String,
    val timestamp: Long
)

/**
 * Extension functions for managers to support dashboard queries
 */
suspend fun ContactProtectionManager.getRecentProtectionEvents(
    userId: String, 
    timeWindow: Long
): List<ContactProtectionEvent> {
    // This would be implemented in the ContactProtectionManager
    // For now, return empty list - would need to add this method to the manager
    return emptyList()
}

suspend fun CaregiverPermissionManager.getMonitoredCaregivers(userId: String): List<String> {
    // This would be implemented in the CaregiverPermissionManager
    // For now, return empty list - would need to add this method to the manager
    return emptyList()
}

suspend fun CaregiverPermissionManager.getRecentPermissionDenials(
    userId: String, 
    timeWindow: Long
): List<PermissionDenialEvent> {
    // This would be implemented in the CaregiverPermissionManager
    // For now, return empty list - would need to add this method to the manager
    return emptyList()
}
