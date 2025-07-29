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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main dashboard for monitoring and managing abuse detection system
 * Designed with elderly-first principles: large text, high contrast, simple navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbuseDetectionDashboard(
    userId: String,
    onNavigateToAlerts: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToReports: () -> Unit,
    viewModel: AbuseDetectionDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadDashboardData(userId)
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
            DashboardHeader(
                systemHealth = uiState.systemHealth,
                onRefresh = { viewModel.refreshData(userId) }
            )

            // Quick Stats Overview
            QuickStatsOverview(
                stats = uiState.quickStats,
                onNavigateToAlerts = onNavigateToAlerts
            )

            // Active Alerts Section
            ActiveAlertsSection(
                alerts = uiState.activeAlerts,
                onAlertClick = { alert -> viewModel.handleAlertClick(alert) },
                onViewAllAlerts = onNavigateToAlerts
            )

            // Recent Activity
            RecentActivitySection(
                recentActivity = uiState.recentActivity,
                onViewReports = onNavigateToReports
            )

            // Management Actions
            ManagementActionsSection(
                onNavigateToRules = onNavigateToRules,
                onNavigateToReports = onNavigateToReports,
                onSystemSettings = { viewModel.openSystemSettings() }
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    systemHealth: SystemHealthStatus,
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
            Column {
                Text(
                    text = stringResource(R.string.abuse_detection_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SystemHealthIndicator(systemHealth)
                    Text(
                        text = when (systemHealth) {
                            SystemHealthStatus.HEALTHY -> stringResource(R.string.system_healthy)
                            SystemHealthStatus.WARNING -> stringResource(R.string.system_warning)
                            SystemHealthStatus.ERROR -> stringResource(R.string.system_error)
                        },
                        fontSize = 18.sp,
                        color = when (systemHealth) {
                            SystemHealthStatus.HEALTHY -> Color(0xFF4CAF50)
                            SystemHealthStatus.WARNING -> Color(0xFFFF9800)
                            SystemHealthStatus.ERROR -> Color(0xFFF44336)
                        }
                    )
                }
            }

            ElderlyFriendlyButton(
                onClick = onRefresh,
                modifier = Modifier.size(64.dp)
            ) {
                AccessibleIcon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh_dashboard),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    size = 32.dp
                )
            }
        }
    }
}

@Composable
private fun SystemHealthIndicator(health: SystemHealthStatus) {
    val (color, icon) = when (health) {
        SystemHealthStatus.HEALTHY -> Color(0xFF4CAF50) to Icons.Default.CheckCircle
        SystemHealthStatus.WARNING -> Color(0xFFFF9800) to Icons.Default.Warning
        SystemHealthStatus.ERROR -> Color(0xFFF44336) to Icons.Default.Error
    }

    AccessibleIcon(
        imageVector = icon,
        contentDescription = stringResource(R.string.system_health_indicator),
        tint = color,
        size = 24.dp
    )
}

@Composable
private fun QuickStatsOverview(
    stats: DashboardQuickStats,
    onNavigateToAlerts: () -> Unit
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.protection_overview),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = stringResource(R.string.active_alerts),
                    value = stats.activeAlerts.toString(),
                    icon = Icons.Default.Warning,
                    color = if (stats.activeAlerts > 0) Color(0xFFFF9800) else Color(0xFF4CAF50),
                    onClick = if (stats.activeAlerts > 0) onNavigateToAlerts else null
                )

                StatCard(
                    title = stringResource(R.string.caregivers_monitored),
                    value = stats.caregiversMonitored.toString(),
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.primary
                )

                StatCard(
                    title = stringResource(R.string.protection_level),
                    value = when (stats.overallProtectionLevel) {
                        ProtectionLevel.HIGH -> stringResource(R.string.high)
                        ProtectionLevel.MEDIUM -> stringResource(R.string.medium)
                        ProtectionLevel.LOW -> stringResource(R.string.low)
                    },
                    icon = Icons.Default.Shield,
                    color = when (stats.overallProtectionLevel) {
                        ProtectionLevel.HIGH -> Color(0xFF4CAF50)
                        ProtectionLevel.MEDIUM -> Color(0xFFFF9800)
                        ProtectionLevel.LOW -> Color(0xFFF44336)
                    }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(16.dp)
            .semantics { contentDescription = "$title: $value" }
    } else {
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(16.dp)
            .semantics { contentDescription = "$title: $value" }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick ?: {}
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessibleIcon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                size = 32.dp
            )
            
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActiveAlertsSection(
    alerts: List<AbuseAlert>,
    onAlertClick: (AbuseAlert) -> Unit,
    onViewAllAlerts: () -> Unit
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.active_alerts),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (alerts.isNotEmpty()) {
                    TextButton(onClick = onViewAllAlerts) {
                        Text(
                            text = stringResource(R.string.view_all),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (alerts.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_active_alerts),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                alerts.take(3).forEach { alert ->
                    AlertSummaryCard(
                        alert = alert,
                        onClick = { onAlertClick(alert) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertSummaryCard(
    alert: AbuseAlert,
    onClick: () -> Unit
) {
    val riskColor = when (alert.riskLevel) {
        AbuseRiskLevel.CRITICAL -> Color(0xFFF44336)
        AbuseRiskLevel.HIGH -> Color(0xFFFF5722)
        AbuseRiskLevel.MEDIUM -> Color(0xFFFF9800)
        AbuseRiskLevel.LOW -> Color(0xFFFFC107)
        AbuseRiskLevel.MINIMAL -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccessibleIcon(
                        imageVector = when (alert.riskLevel) {
                            AbuseRiskLevel.CRITICAL -> Icons.Default.Error
                            AbuseRiskLevel.HIGH -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = riskColor,
                        size = 20.dp
                    )
                    
                    Text(
                        text = alert.riskLevel.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }
                
                Text(
                    text = alert.message,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                
                Text(
                    text = formatTimestamp(alert.createdTimestamp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (alert.requiresImmedateAction) {
                AccessibleIcon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = stringResource(R.string.requires_immediate_action),
                    tint = Color(0xFFF44336),
                    size = 24.dp
                )
            }
        }
    }
}

@Composable
private fun RecentActivitySection(
    recentActivity: List<ActivitySummary>,
    onViewReports: () -> Unit
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_activity),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onViewReports) {
                    Text(
                        text = stringResource(R.string.view_reports),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (recentActivity.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_recent_activity),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentActivity) { activity ->
                        ActivitySummaryItem(activity)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitySummaryItem(activity: ActivitySummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AccessibleIcon(
            imageVector = when (activity.type) {
                ActivityType.RISK_ASSESSMENT -> Icons.Default.Assessment
                ActivityType.ALERT_GENERATED -> Icons.Default.Warning
                ActivityType.CONTACT_PROTECTION -> Icons.Default.Shield
                ActivityType.PERMISSION_DENIED -> Icons.Default.Block
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            size = 20.dp
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = formatTimestamp(activity.timestamp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ManagementActionsSection(
    onNavigateToRules: () -> Unit,
    onNavigateToReports: () -> Unit,
    onSystemSettings: () -> Unit
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.management_actions),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ManagementActionButton(
                    title = stringResource(R.string.detection_rules),
                    icon = Icons.Default.Rule,
                    onClick = onNavigateToRules
                )

                ManagementActionButton(
                    title = stringResource(R.string.view_reports),
                    icon = Icons.Default.Assessment,
                    onClick = onNavigateToReports
                )

                ManagementActionButton(
                    title = stringResource(R.string.system_settings),
                    icon = Icons.Default.Settings,
                    onClick = onSystemSettings
                )
            }
        }
    }
}

@Composable
private fun ManagementActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElderlyFriendlyButton(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessibleIcon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                size = 32.dp
            )
            
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// Data classes for UI state
data class DashboardQuickStats(
    val activeAlerts: Int = 0,
    val caregiversMonitored: Int = 0,
    val overallProtectionLevel: ProtectionLevel = ProtectionLevel.HIGH
)

data class ActivitySummary(
    val type: ActivityType,
    val description: String,
    val timestamp: Long
)

enum class SystemHealthStatus {
    HEALTHY, WARNING, ERROR
}

enum class ProtectionLevel {
    HIGH, MEDIUM, LOW
}

enum class ActivityType {
    RISK_ASSESSMENT, ALERT_GENERATED, CONTACT_PROTECTION, PERMISSION_DENIED
}
