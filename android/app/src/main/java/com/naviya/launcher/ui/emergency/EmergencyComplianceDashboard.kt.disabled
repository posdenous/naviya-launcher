package com.naviya.launcher.ui.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naviya.launcher.emergency.MedicalEmergencyType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Emergency Compliance Dashboard
 * Provides real-time monitoring of emergency response compliance
 * Shows healthcare professional status, response times, and audit trails
 */
@Composable
fun EmergencyComplianceDashboard(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Mock data - in real implementation, this would come from ViewModels
    val dashboardData = remember { generateMockDashboardData() }
    
    MaterialTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header
            DashboardHeader(onNavigateBack = onNavigateBack)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // System Status Overview
                item {
                    SystemStatusCard(dashboardData.systemStatus)
                }
                
                // Emergency Response Metrics
                item {
                    EmergencyMetricsCard(dashboardData.emergencyMetrics)
                }
                
                // Healthcare Professional Status
                item {
                    HealthcareProfessionalStatusCard(dashboardData.professionalStatus)
                }
                
                // Recent Emergency Events
                item {
                    RecentEmergencyEventsCard(dashboardData.recentEvents)
                }
                
                // Compliance Alerts
                item {
                    ComplianceAlertsCard(dashboardData.complianceAlerts)
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = "Emergency Compliance Dashboard",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        
        IconButton(
            onClick = { /* Refresh dashboard */ },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SystemStatusCard(systemStatus: SystemStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (systemStatus.isOperational) 
                Color(0xFF4CAF50).copy(alpha = 0.1f) 
            else 
                Color(0xFFE53E3E).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (systemStatus.isOperational) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (systemStatus.isOperational) Color(0xFF4CAF50) else Color(0xFFE53E3E),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "System Status",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (systemStatus.isOperational) "All Systems Operational" else "System Issues Detected",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (systemStatus.isOperational) Color(0xFF4CAF50) else Color(0xFFE53E3E)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusIndicator("Emergency System", systemStatus.emergencySystemReady)
                StatusIndicator("Healthcare Professionals", systemStatus.healthcareProfessionalsAvailable)
                StatusIndicator("Compliance", systemStatus.complianceSystemReady)
            }
        }
    }
}

@Composable
private fun StatusIndicator(label: String, isReady: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isReady) Color(0xFF4CAF50) else Color(0xFFE53E3E),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmergencyMetricsCard(metrics: EmergencyMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Emergency Response Metrics",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "Total Emergencies",
                    value = metrics.totalEmergencies.toString(),
                    icon = Icons.Default.Warning,
                    color = Color(0xFFE53E3E)
                )
                
                MetricItem(
                    label = "Avg Response Time",
                    value = "${metrics.averageResponseTimeSeconds}s",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF2196F3)
                )
                
                MetricItem(
                    label = "Success Rate",
                    value = "${metrics.successRate}%",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HealthcareProfessionalStatusCard(professionalStatus: List<ProfessionalStatusItem>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Healthcare Professionals",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            professionalStatus.forEach { professional ->
                ProfessionalStatusRow(professional)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ProfessionalStatusRow(professional: ProfessionalStatusItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = professional.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            
            Text(
                text = professional.specialization,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (professional.isAvailable) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (professional.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = if (professional.isAvailable) "Available" else "Busy",
                style = MaterialTheme.typography.bodySmall,
                color = if (professional.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun RecentEmergencyEventsCard(events: List<EmergencyEventItem>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Recent Emergency Events",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            events.forEach { event ->
                EmergencyEventRow(event)
                if (event != events.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun EmergencyEventRow(event: EmergencyEventItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = event.type,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            
            Text(
                text = event.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = when (event.priority) {
                "CRITICAL" -> Color(0xFFE53E3E).copy(alpha = 0.2f)
                "HIGH" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                else -> Color(0xFF2196F3).copy(alpha = 0.2f)
            },
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = event.priority,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ComplianceAlertsCard(alerts: List<ComplianceAlert>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (alerts.isNotEmpty()) Color(0xFFFF9800) else Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Compliance Alerts",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (alerts.isEmpty()) {
                Text(
                    text = "No compliance alerts. All systems are compliant.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50)
                )
            } else {
                alerts.forEach { alert ->
                    ComplianceAlertRow(alert)
                    if (alert != alerts.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

private fun ComplianceAlertRow(alert: ComplianceAlert) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (alert.severity) {
                "HIGH" -> Icons.Default.Warning
                "MEDIUM" -> Icons.Default.Warning
                else -> Icons.Default.Info
            },
            contentDescription = null,
            tint = when (alert.severity) {
                "HIGH" -> Color(0xFFE53E3E)
                "MEDIUM" -> Color(0xFFFF9800)
                else -> Color(0xFF2196F3)
            },
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = alert.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// Data classes for dashboard
private data class DashboardData(
    val systemStatus: SystemStatus,
    val emergencyMetrics: EmergencyMetrics,
    val professionalStatus: List<ProfessionalStatusItem>,
    val recentEvents: List<EmergencyEventItem>,
    val complianceAlerts: List<ComplianceAlert>
)

private data class SystemStatus(
    val isOperational: Boolean,
    val emergencySystemReady: Boolean,
    val healthcareProfessionalsAvailable: Boolean,
    val complianceSystemReady: Boolean
)

private data class EmergencyMetrics(
    val totalEmergencies: Int,
    val averageResponseTimeSeconds: Int,
    val successRate: Int
)

private data class ProfessionalStatusItem(
    val name: String,
    val specialization: String,
    val isAvailable: Boolean
)

private data class EmergencyEventItem(
    val type: String,
    val timestamp: String,
    val priority: String
)

private data class ComplianceAlert(
    val message: String,
    val severity: String,
    val timestamp: String
)

private fun generateMockDashboardData(): DashboardData {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    return DashboardData(
        systemStatus = SystemStatus(
            isOperational = true,
            emergencySystemReady = true,
            healthcareProfessionalsAvailable = true,
            complianceSystemReady = true
        ),
        emergencyMetrics = EmergencyMetrics(
            totalEmergencies = 12,
            averageResponseTimeSeconds = 45,
            successRate = 96
        ),
        professionalStatus = listOf(
            ProfessionalStatusItem("Dr. Sarah Johnson", "Emergency Medicine", true),
            ProfessionalStatusItem("Dr. Michael Chen", "Cardiology", true),
            ProfessionalStatusItem("Dr. Emily Davis", "Geriatrics", false)
        ),
        recentEvents = listOf(
            EmergencyEventItem("Cardiac Event", dateFormat.format(Date(System.currentTimeMillis() - 3600000)), "CRITICAL"),
            EmergencyEventItem("Fall with Injury", dateFormat.format(Date(System.currentTimeMillis() - 7200000)), "HIGH"),
            EmergencyEventItem("Medication Emergency", dateFormat.format(Date(System.currentTimeMillis() - 10800000)), "MEDIUM")
        ),
        complianceAlerts = listOf(
            // Empty list means no alerts - all compliant
        )
    )
}
