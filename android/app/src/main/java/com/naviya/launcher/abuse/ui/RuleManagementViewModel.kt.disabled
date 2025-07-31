package com.naviya.launcher.abuse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naviya.launcher.abuse.data.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for rule management screen
 * Handles detection rule configuration and system settings
 */
@HiltViewModel
class RuleManagementViewModel @Inject constructor(
    private val abuseDao: AbuseDetectionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RuleManagementUiState())
    val uiState: StateFlow<RuleManagementUiState> = _uiState.asStateFlow()

    fun loadRules(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load all detection rules
                val allRules = abuseDao.getAllDetectionRules()
                
                // Group rules by category
                val ruleCategories = groupRulesByCategory(allRules)
                
                // Load system status
                val systemStatus = loadSystemStatus()
                
                // Load current detection sensitivity
                val detectionSensitivity = loadDetectionSensitivity(userId)
                
                _uiState.value = RuleManagementUiState(
                    isLoading = false,
                    ruleCategories = ruleCategories,
                    detectionSensitivity = detectionSensitivity,
                    systemStatus = systemStatus,
                    lastSystemUpdate = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load rules: ${e.message}"
                )
            }
        }
    }

    fun refreshRules(userId: String) {
        loadRules(userId)
    }

    fun updateDetectionSensitivity(sensitivity: DetectionSensitivity) {
        viewModelScope.launch {
            try {
                // Update sensitivity settings
                val sensitivityConfig = when (sensitivity) {
                    DetectionSensitivity.HIGH -> createHighSensitivityConfig()
                    DetectionSensitivity.MEDIUM -> createMediumSensitivityConfig()
                    DetectionSensitivity.LOW -> createLowSensitivityConfig()
                }
                
                // Apply sensitivity configuration to all rules
                applySensitivityConfiguration(sensitivityConfig)
                
                _uiState.value = _uiState.value.copy(
                    detectionSensitivity = sensitivity,
                    lastSystemUpdate = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update sensitivity: ${e.message}"
                )
            }
        }
    }

    fun toggleRule(ruleId: String, enabled: Boolean) {
        viewModelScope.launch {
            try {
                abuseDao.updateRuleEnabled(ruleId, enabled)
                
                // Reload rules to reflect changes
                val currentState = _uiState.value
                val updatedCategories = currentState.ruleCategories.map { category ->
                    category.copy(
                        rules = category.rules.map { rule ->
                            if (rule.ruleId == ruleId) {
                                rule.copy(isEnabled = enabled)
                            } else {
                                rule
                            }
                        },
                        enabledRules = category.rules.count { rule ->
                            if (rule.ruleId == ruleId) enabled else rule.isEnabled
                        }
                    )
                }
                
                _uiState.value = currentState.copy(
                    ruleCategories = updatedCategories,
                    lastSystemUpdate = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle rule: ${e.message}"
                )
            }
        }
    }

    fun updateRuleConfiguration(ruleId: String, configuration: Map<String, String>) {
        viewModelScope.launch {
            try {
                abuseDao.updateRuleConfiguration(ruleId, configuration)
                
                // Reload rules to reflect changes
                val currentState = _uiState.value
                val updatedCategories = currentState.ruleCategories.map { category ->
                    category.copy(
                        rules = category.rules.map { rule ->
                            if (rule.ruleId == ruleId) {
                                rule.copy(
                                    configuration = configuration,
                                    lastModifiedTimestamp = System.currentTimeMillis()
                                )
                            } else {
                                rule
                            }
                        }
                    )
                }
                
                _uiState.value = currentState.copy(
                    ruleCategories = updatedCategories,
                    lastSystemUpdate = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update rule configuration: ${e.message}"
                )
            }
        }
    }

    private fun groupRulesByCategory(rules: List<AbuseDetectionRule>): List<RuleCategory> {
        val categories = mutableMapOf<AbuseRuleType, MutableList<AbuseDetectionRule>>()
        
        rules.forEach { rule ->
            categories.getOrPut(rule.ruleType) { mutableListOf() }.add(rule)
        }
        
        return categories.map { (ruleType, ruleList) ->
            val enabledCount = ruleList.count { it.isEnabled }
            
            RuleCategory(
                name = when (ruleType) {
                    AbuseRuleType.CONTACT_MANIPULATION -> "Contact Protection"
                    AbuseRuleType.PERMISSION_ESCALATION -> "Permission Control"
                    AbuseRuleType.TEMPORAL_PATTERNS -> "Timing Analysis"
                    AbuseRuleType.EMERGENCY_SYSTEM_ABUSE -> "Emergency System Protection"
                    AbuseRuleType.ESCALATING_BEHAVIOR -> "Behavior Escalation"
                    AbuseRuleType.TRIGGER_EVENTS -> "Event Response"
                },
                description = when (ruleType) {
                    AbuseRuleType.CONTACT_MANIPULATION -> "Protects against social isolation by monitoring contact removal attempts"
                    AbuseRuleType.PERMISSION_ESCALATION -> "Detects attempts to gain unauthorized access or control"
                    AbuseRuleType.TEMPORAL_PATTERNS -> "Identifies suspicious timing patterns in caregiver behavior"
                    AbuseRuleType.EMERGENCY_SYSTEM_ABUSE -> "Monitors for tampering with emergency and safety systems"
                    AbuseRuleType.ESCALATING_BEHAVIOR -> "Tracks increasing risk patterns over time"
                    AbuseRuleType.TRIGGER_EVENTS -> "Responds to panic mode and emergency situations"
                },
                icon = when (ruleType) {
                    AbuseRuleType.CONTACT_MANIPULATION -> Icons.Default.Contacts
                    AbuseRuleType.PERMISSION_ESCALATION -> Icons.Default.Security
                    AbuseRuleType.TEMPORAL_PATTERNS -> Icons.Default.Schedule
                    AbuseRuleType.EMERGENCY_SYSTEM_ABUSE -> Icons.Default.Emergency
                    AbuseRuleType.ESCALATING_BEHAVIOR -> Icons.Default.TrendingUp
                    AbuseRuleType.TRIGGER_EVENTS -> Icons.Default.NotificationImportant
                },
                rules = ruleList,
                enabledRules = enabledCount,
                totalRules = ruleList.size
            )
        }.sortedBy { it.name }
    }

    private suspend fun loadSystemStatus(): SystemStatus {
        return try {
            val health = abuseDao.getDetectionSystemHealth()
            when {
                health?.isHealthy == true -> SystemStatus.ACTIVE
                health?.lastAssessmentTime != null -> SystemStatus.WARNING
                else -> SystemStatus.ERROR
            }
        } catch (e: Exception) {
            SystemStatus.ERROR
        }
    }

    private suspend fun loadDetectionSensitivity(userId: String): DetectionSensitivity {
        return try {
            // This would typically be stored in user preferences
            // For now, default to MEDIUM
            DetectionSensitivity.MEDIUM
        } catch (e: Exception) {
            DetectionSensitivity.MEDIUM
        }
    }

    private fun createHighSensitivityConfig(): Map<String, Map<String, String>> {
        return mapOf(
            "contact_manipulation" to mapOf(
                "threshold" to "2", // Lower threshold = more sensitive
                "severity_multiplier" to "1.5"
            ),
            "permission_escalation" to mapOf(
                "threshold" to "1",
                "severity_multiplier" to "1.5"
            ),
            "temporal_patterns" to mapOf(
                "night_time_threshold" to "3",
                "weekend_concentration_threshold" to "0.5"
            ),
            "emergency_system_abuse" to mapOf(
                "threshold" to "1",
                "severity_multiplier" to "2.0"
            ),
            "escalating_behavior" to mapOf(
                "escalation_threshold" to "1.2",
                "time_window_days" to "3"
            )
        )
    }

    private fun createMediumSensitivityConfig(): Map<String, Map<String, String>> {
        return mapOf(
            "contact_manipulation" to mapOf(
                "threshold" to "3",
                "severity_multiplier" to "1.0"
            ),
            "permission_escalation" to mapOf(
                "threshold" to "2",
                "severity_multiplier" to "1.0"
            ),
            "temporal_patterns" to mapOf(
                "night_time_threshold" to "5",
                "weekend_concentration_threshold" to "0.6"
            ),
            "emergency_system_abuse" to mapOf(
                "threshold" to "1",
                "severity_multiplier" to "1.5"
            ),
            "escalating_behavior" to mapOf(
                "escalation_threshold" to "1.5",
                "time_window_days" to "5"
            )
        )
    }

    private fun createLowSensitivityConfig(): Map<String, Map<String, String>> {
        return mapOf(
            "contact_manipulation" to mapOf(
                "threshold" to "5", // Higher threshold = less sensitive
                "severity_multiplier" to "0.8"
            ),
            "permission_escalation" to mapOf(
                "threshold" to "3",
                "severity_multiplier" to "0.8"
            ),
            "temporal_patterns" to mapOf(
                "night_time_threshold" to "8",
                "weekend_concentration_threshold" to "0.7"
            ),
            "emergency_system_abuse" to mapOf(
                "threshold" to "2",
                "severity_multiplier" to "1.0"
            ),
            "escalating_behavior" to mapOf(
                "escalation_threshold" to "2.0",
                "time_window_days" to "7"
            )
        )
    }

    private suspend fun applySensitivityConfiguration(
        sensitivityConfig: Map<String, Map<String, String>>
    ) {
        sensitivityConfig.forEach { (ruleType, config) ->
            val rules = abuseDao.getRulesByType(
                AbuseRuleType.valueOf(ruleType.uppercase().replace("_", "_"))
            )
            
            rules.forEach { rule ->
                abuseDao.updateRuleConfiguration(rule.ruleId, config)
            }
        }
    }
}

/**
 * UI state for rule management
 */
data class RuleManagementUiState(
    val isLoading: Boolean = false,
    val ruleCategories: List<RuleCategory> = emptyList(),
    val detectionSensitivity: DetectionSensitivity = DetectionSensitivity.MEDIUM,
    val systemStatus: SystemStatus = SystemStatus.ACTIVE,
    val lastSystemUpdate: Long? = null,
    val error: String? = null
)

/**
 * Extension functions for DAO to support rule management
 */
suspend fun AbuseDetectionDao.getAllDetectionRules(): List<AbuseDetectionRule> {
    // This would get all rules regardless of enabled status
    return getEnabledRules() // Placeholder - would need actual implementation
}

suspend fun AbuseDetectionDao.updateRuleEnabled(ruleId: String, enabled: Boolean) {
    // This would update the isEnabled field for a specific rule
    // Placeholder implementation - would need actual DAO method
}

/**
 * Extension of AbuseRuleType enum to support all rule categories
 */
enum class AbuseRuleType {
    CONTACT_MANIPULATION,
    PERMISSION_ESCALATION,
    TEMPORAL_PATTERNS,
    EMERGENCY_SYSTEM_ABUSE,
    ESCALATING_BEHAVIOR,
    TRIGGER_EVENTS
}
