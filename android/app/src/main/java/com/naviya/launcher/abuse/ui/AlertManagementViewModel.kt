package com.naviya.launcher.abuse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naviya.launcher.abuse.data.*
import com.naviya.launcher.emergency.EmergencyService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for alert management screen
 * Handles alert filtering, actions, and elder rights notifications
 */
@HiltViewModel
class AlertManagementViewModel @Inject constructor(
    private val abuseDao: AbuseDetectionDao,
    private val emergencyService: EmergencyService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertManagementUiState())
    val uiState: StateFlow<AlertManagementUiState> = _uiState.asStateFlow()

    fun loadAlerts(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val allAlerts = abuseDao.getAllAlerts(userId)
                val filteredAlerts = filterAlerts(allAlerts, _uiState.value.currentFilter)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allAlerts = allAlerts,
                    filteredAlerts = filteredAlerts
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load alerts: ${e.message}"
                )
            }
        }
    }

    fun refreshAlerts(userId: String) {
        loadAlerts(userId)
    }

    fun updateFilter(filter: AlertFilter) {
        val currentState = _uiState.value
        val filteredAlerts = filterAlerts(currentState.allAlerts, filter)
        
        _uiState.value = currentState.copy(
            currentFilter = filter,
            filteredAlerts = filteredAlerts
        )
    }

    fun handleAlertAction(
        alert: AbuseAlert,
        action: AlertAction,
        onContactElderRights: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (action) {
                    AlertAction.CONTACT_ELDER_RIGHTS -> {
                        handleContactElderRights(alert, onContactElderRights)
                    }
                    AlertAction.MARK_RESOLVED -> {
                        handleMarkResolved(alert)
                    }
                    AlertAction.VIEW_DETAILS -> {
                        handleViewDetails(alert)
                    }
                }
                
                // Refresh alerts after action
                loadAlerts(alert.userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to handle alert action: ${e.message}"
                )
            }
        }
    }

    private suspend fun handleContactElderRights(
        alert: AbuseAlert,
        onContactElderRights: (String) -> Unit
    ) {
        // Notify emergency service to contact elder rights advocate
        emergencyService.notifyElderRightsAdvocate(
            userId = alert.userId,
            alertId = alert.alertId,
            message = "Immediate assistance requested for abuse alert: ${alert.message}",
            priority = "IMMEDIATE"
        )
        
        // Update alert status to indicate elder rights contacted
        abuseDao.updateAlertStatus(alert.alertId, AbuseAlertStatus.ESCALATED)
        
        // Add notification record
        val notification = AbuseAlertNotification(
            alertId = alert.alertId,
            notificationType = "ELDER_RIGHTS_ADVOCATE",
            recipient = "elder_rights_advocate",
            message = "Alert escalated to elder rights advocate",
            sentTimestamp = System.currentTimeMillis(),
            deliveryStatus = "SENT"
        )
        abuseDao.insertAlertNotification(notification)
        
        // Trigger UI callback for additional actions (like opening dialer)
        onContactElderRights(alert.alertId)
    }

    private suspend fun handleMarkResolved(alert: AbuseAlert) {
        val resolutionDetails = "Alert marked as resolved by user"
        val resolutionTimestamp = System.currentTimeMillis()
        
        abuseDao.updateAlertResolution(alert.alertId, resolutionDetails, resolutionTimestamp)
        abuseDao.updateAlertStatus(alert.alertId, AbuseAlertStatus.RESOLVED)
    }

    private suspend fun handleViewDetails(alert: AbuseAlert) {
        // Mark alert as viewed if it's currently active
        if (alert.status == AbuseAlertStatus.ACTIVE) {
            abuseDao.updateAlertStatus(alert.alertId, AbuseAlertStatus.VIEWED)
        }
    }

    private fun filterAlerts(alerts: List<AbuseAlert>, filter: AlertFilter): List<AbuseAlert> {
        return when (filter) {
            AlertFilter.ALL -> alerts
            AlertFilter.ACTIVE -> alerts.filter { 
                it.status == AbuseAlertStatus.ACTIVE || it.status == AbuseAlertStatus.VIEWED 
            }
            AlertFilter.CRITICAL -> alerts.filter { 
                it.riskLevel >= AbuseRiskLevel.HIGH 
            }
            AlertFilter.RESOLVED -> alerts.filter { 
                it.status == AbuseAlertStatus.RESOLVED 
            }
        }.sortedWith(compareByDescending<AbuseAlert> { alert ->
            // Sort by priority: immediate action first, then by risk level, then by timestamp
            when {
                alert.requiresImmedateAction -> 1000 + alert.riskLevel.ordinal
                else -> alert.riskLevel.ordinal
            }
        }.thenByDescending { it.createdTimestamp })
    }
}

/**
 * UI state for alert management
 */
data class AlertManagementUiState(
    val isLoading: Boolean = false,
    val allAlerts: List<AbuseAlert> = emptyList(),
    val filteredAlerts: List<AbuseAlert> = emptyList(),
    val currentFilter: AlertFilter = AlertFilter.ALL,
    val error: String? = null
)

/**
 * Extension function for DAO to get all alerts
 */
suspend fun AbuseDetectionDao.getAllAlerts(userId: String): List<AbuseAlert> {
    // This would need to be implemented in the actual DAO
    // For now, we'll use the existing getActiveAlerts method
    return getActiveAlerts(userId)
}

/**
 * Data class for alert notifications
 */
data class AbuseAlertNotification(
    val notificationId: String = "notification-${System.nanoTime()}",
    val alertId: String,
    val notificationType: String,
    val recipient: String,
    val message: String,
    val sentTimestamp: Long,
    val deliveryStatus: String
)

/**
 * Extension function for DAO to insert alert notifications
 */
suspend fun AbuseDetectionDao.insertAlertNotification(notification: AbuseAlertNotification) {
    // This would need to be implemented in the actual DAO
    // For now, this is a placeholder
}
