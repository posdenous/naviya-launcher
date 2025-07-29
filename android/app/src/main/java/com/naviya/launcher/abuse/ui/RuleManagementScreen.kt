package com.naviya.launcher.abuse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naviya.launcher.R
import com.naviya.launcher.abuse.data.*
import com.naviya.launcher.ui.theme.NaviyaTheme
import com.naviya.launcher.ui.components.ElderlyFriendlyCard
import com.naviya.launcher.ui.components.ElderlyFriendlyButton
import com.naviya.launcher.ui.components.AccessibleIcon

/**
 * Screen for managing abuse detection rules
 * Allows elderly users and caregivers to configure detection sensitivity and rules
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleManagementScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: RuleManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadRules(userId)
    }

    NaviyaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            RuleManagementHeader(
                onNavigateBack = onNavigateBack,
                onRefresh = { viewModel.refreshRules(userId) }
            )

            // Detection sensitivity settings
            DetectionSensitivitySection(
                currentSensitivity = uiState.detectionSensitivity,
                onSensitivityChange = { sensitivity -> 
                    viewModel.updateDetectionSensitivity(sensitivity)
                }
            )

            // Rule categories
            RuleCategoriesSection(
                ruleCategories = uiState.ruleCategories,
                onRuleToggle = { ruleId, enabled -> 
                    viewModel.toggleRule(ruleId, enabled)
                },
                onRuleConfigChange = { ruleId, config -> 
                    viewModel.updateRuleConfiguration(ruleId, config)
                }
            )

            // System status
            SystemStatusSection(
                systemStatus = uiState.systemStatus,
                lastUpdate = uiState.lastSystemUpdate
            )
        }
    }
}

@Composable
private fun RuleManagementHeader(
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElderlyFriendlyButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(56.dp)
                ) {
                    AccessibleIcon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        size = 28.dp
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.detection_rules),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.configure_protection_settings),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            ElderlyFriendlyButton(
                onClick = onRefresh,
                modifier = Modifier.size(56.dp)
            ) {
                AccessibleIcon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh_rules),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    size = 28.dp
                )
            }
        }
    }
}

@Composable
private fun DetectionSensitivitySection(
    currentSensitivity: DetectionSensitivity,
    onSensitivityChange: (DetectionSensitivity) -> Unit
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.protection_sensitivity),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.sensitivity_description),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 22.sp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetectionSensitivity.values().forEach { sensitivity ->
                    SensitivityOption(
                        sensitivity = sensitivity,
                        isSelected = currentSensitivity == sensitivity,
                        onSelect = { onSensitivityChange(sensitivity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SensitivityOption(
    sensitivity: DetectionSensitivity,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val (title, description, color) = when (sensitivity) {
        DetectionSensitivity.HIGH -> Triple(
            stringResource(R.string.high_sensitivity),
            stringResource(R.string.high_sensitivity_description),
            Color(0xFFF44336)
        )
        DetectionSensitivity.MEDIUM -> Triple(
            stringResource(R.string.medium_sensitivity),
            stringResource(R.string.medium_sensitivity_description),
            Color(0xFFFF9800)
        )
        DetectionSensitivity.LOW -> Triple(
            stringResource(R.string.low_sensitivity),
            stringResource(R.string.low_sensitivity_description),
            Color(0xFF4CAF50)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = color
                )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }

            AccessibleIcon(
                imageVector = when (sensitivity) {
                    DetectionSensitivity.HIGH -> Icons.Default.Security
                    DetectionSensitivity.MEDIUM -> Icons.Default.Shield
                    DetectionSensitivity.LOW -> Icons.Default.VerifiedUser
                },
                contentDescription = null,
                tint = color,
                size = 32.dp
            )
        }
    }
}

@Composable
private fun RuleCategoriesSection(
    ruleCategories: List<RuleCategory>,
    onRuleToggle: (String, Boolean) -> Unit,
    onRuleConfigChange: (String, Map<String, String>) -> Unit
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.protection_rules),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ruleCategories) { category ->
                    RuleCategoryCard(
                        category = category,
                        onRuleToggle = onRuleToggle,
                        onRuleConfigChange = onRuleConfigChange
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleCategoryCard(
    category: RuleCategory,
    onRuleToggle: (String, Boolean) -> Unit,
    onRuleConfigChange: (String, Map<String, String>) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category header
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccessibleIcon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 28.dp
                    )
                    
                    Column {
                        Text(
                            text = category.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${category.enabledRules}/${category.totalRules} rules active",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    AccessibleIcon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurface,
                        size = 24.dp
                    )
                }
            }

            // Category description
            Text(
                text = category.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )

            // Expanded rules list
            if (isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    category.rules.forEach { rule ->
                        RuleConfigItem(
                            rule = rule,
                            onToggle = { enabled -> onRuleToggle(rule.ruleId, enabled) },
                            onConfigChange = { config -> onRuleConfigChange(rule.ruleId, config) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleConfigItem(
    rule: AbuseDetectionRule,
    onToggle: (Boolean) -> Unit,
    onConfigChange: (Map<String, String>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isEnabled) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = rule.ruleName.replace("_", " ").uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = rule.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }

                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }

            // Rule configuration (if enabled)
            if (rule.isEnabled && rule.configuration.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Configuration:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    rule.configuration.forEach { (key, value) ->
                        Text(
                            text = "• ${key.replace("_", " ")}: $value",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemStatusSection(
    systemStatus: SystemStatus,
    lastUpdate: Long?
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.system_status),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (statusColor, statusIcon, statusText) = when (systemStatus) {
                        SystemStatus.ACTIVE -> Triple(
                            Color(0xFF4CAF50),
                            Icons.Default.CheckCircle,
                            stringResource(R.string.protection_active)
                        )
                        SystemStatus.WARNING -> Triple(
                            Color(0xFFFF9800),
                            Icons.Default.Warning,
                            stringResource(R.string.protection_warning)
                        )
                        SystemStatus.ERROR -> Triple(
                            Color(0xFFF44336),
                            Icons.Default.Error,
                            stringResource(R.string.protection_error)
                        )
                    }

                    AccessibleIcon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        size = 32.dp
                    )

                    Column {
                        Text(
                            text = statusText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        
                        if (lastUpdate != null) {
                            Text(
                                text = "Last updated: ${formatTimestamp(lastUpdate)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        else -> "${diff / 86400_000} days ago"
    }
}

// Data classes and enums for rule management
enum class DetectionSensitivity {
    HIGH, MEDIUM, LOW
}

enum class SystemStatus {
    ACTIVE, WARNING, ERROR
}

data class RuleCategory(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val rules: List<AbuseDetectionRule>,
    val enabledRules: Int,
    val totalRules: Int
)
