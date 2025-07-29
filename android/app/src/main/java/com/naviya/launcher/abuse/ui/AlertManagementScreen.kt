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
 * Screen for managing abuse detection alerts
 * Elderly-friendly design with large text, clear actions, and simple navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertManagementScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onContactElderRights: (String) -> Unit,
    viewModel: AlertManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadAlerts(userId)
    }

    NaviyaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with back navigation
            AlertManagementHeader(
                onNavigateBack = onNavigateBack,
                onRefresh = { viewModel.refreshAlerts(userId) }
            )

            // Filter and sort options
            AlertFilterSection(
                currentFilter = uiState.currentFilter,
                onFilterChange = { filter -> viewModel.updateFilter(filter) }
            )

            // Alerts list
            AlertsList(
                alerts = uiState.filteredAlerts,
                onAlertAction = { alert, action -> 
                    viewModel.handleAlertAction(alert, action, onContactElderRights)
                },
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
private fun AlertManagementHeader(
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

                Text(
                    text = stringResource(R.string.alert_management),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            ElderlyFriendlyButton(
                onClick = onRefresh,
                modifier = Modifier.size(56.dp)
            ) {
                AccessibleIcon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh_alerts),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    size = 28.dp
                )
            }
        }
    }
}

@Composable
private fun AlertFilterSection(
    currentFilter: AlertFilter,
    onFilterChange: (AlertFilter) -> Unit
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.filter_alerts),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlertFilter.values().forEach { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = when (filter) {
                                    AlertFilter.ALL -> stringResource(R.string.all_alerts)
                                    AlertFilter.ACTIVE -> stringResource(R.string.active_alerts)
                                    AlertFilter.CRITICAL -> stringResource(R.string.critical_alerts)
                                    AlertFilter.RESOLVED -> stringResource(R.string.resolved_alerts)
                                },
                                fontSize = 16.sp
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Filter by ${filter.name.lowercase()}"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsList(
    alerts: List<AbuseAlert>,
    onAlertAction: (AbuseAlert, AlertAction) -> Unit,
    isLoading: Boolean
) {
    ElderlyFriendlyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.alerts_list),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                    }
                }
                alerts.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_alerts_found),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(alerts) { alert ->
                            AlertDetailCard(
                                alert = alert,
                                onAction = { action -> onAlertAction(alert, action) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertDetailCard(
    alert: AbuseAlert,
    onAction: (AlertAction) -> Unit
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
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alert header
            AlertHeader(alert = alert, riskColor = riskColor)
            
            // Alert message and details
            AlertDetails(alert = alert)
            
            // Recommended actions
            RecommendedActions(
                actions = alert.recommendedActions,
                riskLevel = alert.riskLevel
            )
            
            // Action buttons
            AlertActionButtons(
                alert = alert,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun AlertHeader(
    alert: AbuseAlert,
    riskColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccessibleIcon(
                imageVector = when (alert.riskLevel) {
                    AbuseRiskLevel.CRITICAL -> Icons.Default.Error
                    AbuseRiskLevel.HIGH -> Icons.Default.Warning
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                tint = riskColor,
                size = 32.dp
            )
            
            Column {
                Text(
                    text = "${alert.riskLevel.name} RISK",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
                
                Text(
                    text = alert.alertType.name.replace("_", " "),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            if (alert.requiresImmedateAction) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AccessibleIcon(
                        imageVector = Icons.Default.PriorityHigh,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        size = 20.dp
                    )
                    Text(
                        text = stringResource(R.string.immediate_action_required),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                }
            }
            
            Text(
                text = formatTimestamp(alert.createdTimestamp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AlertDetails(alert: AbuseAlert) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = alert.message,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
        )
        
        if (alert.status == AbuseAlertStatus.RESOLVED && alert.resolutionDetails != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.resolution_details),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    
                    Text(
                        text = alert.resolutionDetails,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (alert.resolutionTimestamp != null) {
                        Text(
                            text = "Resolved: ${formatTimestamp(alert.resolutionTimestamp)}",
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
private fun RecommendedActions(
    actions: List<String>,
    riskLevel: AbuseRiskLevel
) {
    if (actions.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.recommended_actions),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            actions.forEach { action ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    AccessibleIcon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = when (riskLevel) {
                            AbuseRiskLevel.CRITICAL -> Color(0xFFF44336)
                            AbuseRiskLevel.HIGH -> Color(0xFFFF5722)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        size = 20.dp
                    )
                    
                    Text(
                        text = action,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertActionButtons(
    alert: AbuseAlert,
    onAction: (AlertAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (alert.status) {
            AbuseAlertStatus.ACTIVE -> {
                if (alert.requiresImmedateAction) {
                    ElderlyFriendlyButton(
                        onClick = { onAction(AlertAction.CONTACT_ELDER_RIGHTS) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AccessibleIcon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color.White,
                                size = 20.dp
                            )
                            Text(
                                text = stringResource(R.string.contact_elder_rights),
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                
                ElderlyFriendlyButton(
                    onClick = { onAction(AlertAction.MARK_RESOLVED) },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AccessibleIcon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            size = 20.dp
                        )
                        Text(
                            text = stringResource(R.string.mark_resolved),
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            AbuseAlertStatus.VIEWED -> {
                ElderlyFriendlyButton(
                    onClick = { onAction(AlertAction.MARK_RESOLVED) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.mark_resolved),
                        fontSize = 16.sp
                    )
                }
            }
            
            AbuseAlertStatus.RESOLVED -> {
                Text(
                    text = stringResource(R.string.alert_resolved),
                    fontSize = 16.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// Enums and data classes for alert management
enum class AlertFilter {
    ALL, ACTIVE, CRITICAL, RESOLVED
}

enum class AlertAction {
    CONTACT_ELDER_RIGHTS, MARK_RESOLVED, VIEW_DETAILS
}
