package com.naviya.launcher.ui.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naviya.launcher.R
import com.naviya.launcher.emergency.MedicalEmergencyType
import com.naviya.launcher.ui.theme.ElderlyTypography

/**
 * Medical Emergency Screen for Naviya Launcher
 * Provides accessible emergency activation with medical compliance integration
 * Follows Windsurf rules for elderly accessibility and emergency response
 */
@Composable
fun MedicalEmergencyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MedicalEmergencyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.emergencyActivated) {
        if (uiState.emergencyActivated) {
            // Emergency activated - could navigate to emergency status screen
            // For now, we'll show the activation status
        }
    }

    MaterialTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            MedicalEmergencyHeader(
                onNavigateBack = onNavigateBack,
                hasActiveProfessional = uiState.hasActiveProfessional
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Emergency Type Selection
            if (!uiState.emergencyActivated) {
                EmergencyTypeSelection(
                    onEmergencySelected = { emergencyType ->
                        viewModel.activateEmergency(emergencyType)
                    },
                    isLoading = uiState.isLoading
                )
            } else {
                EmergencyActivationStatus(
                    emergencyResult = uiState.emergencyResult,
                    onDismiss = {
                        viewModel.resetEmergencyState()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Healthcare Professional Status
            HealthcareProfessionalStatus(
                hasActiveProfessional = uiState.hasActiveProfessional,
                professionalName = uiState.primaryProfessionalName,
                isCompliant = uiState.isProfessionalCompliant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicalEmergencyHeader(
    onNavigateBack: () -> Unit,
    hasActiveProfessional: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(64.dp) // Large touch target for elderly users
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back),
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.medical_emergency),
                style = ElderlyTypography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            // Professional status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = if (hasActiveProfessional) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (hasActiveProfessional) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasActiveProfessional) 
                        stringResource(R.string.healthcare_professional_available) 
                    else 
                        stringResource(R.string.no_healthcare_professional),
                    style = ElderlyTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }
        
        // Emergency call button (always accessible)
        IconButton(
            onClick = { /* Direct emergency call */ },
            modifier = Modifier
                .size(64.dp)
                .background(
                    Color(0xFFE53E3E),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = stringResource(R.string.emergency_call),
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun EmergencyTypeSelection(
    onEmergencySelected: (MedicalEmergencyType) -> Unit,
    isLoading: Boolean
) {
    Column {
        Text(
            text = stringResource(R.string.select_emergency_type),
            style = ElderlyTypography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(getEmergencyTypes()) { emergencyType ->
                EmergencyTypeCard(
                    emergencyType = emergencyType,
                    onClick = { onEmergencySelected(emergencyType.type) },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
private fun EmergencyTypeCard(
    emergencyType: EmergencyTypeInfo,
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() }
            .border(
                width = 2.dp,
                color = emergencyType.borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = emergencyType.backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = emergencyType.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = emergencyType.iconColor
            )
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(emergencyType.titleRes),
                    style = ElderlyTypography.headlineSmall.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = stringResource(emergencyType.descriptionRes),
                    style = ElderlyTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Priority indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(emergencyType.priorityLevel) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = emergencyType.priorityColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = emergencyType.priorityText,
                        style = ElderlyTypography.bodySmall,
                        color = emergencyType.priorityColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = emergencyType.iconColor
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = emergencyType.iconColor
                )
            }
        }
    }
}

@Composable
private fun EmergencyActivationStatus(
    emergencyResult: String?,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.emergency_activated),
                style = ElderlyTypography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = emergencyResult ?: stringResource(R.string.emergency_services_notified),
                style = ElderlyTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.ok),
                    style = ElderlyTypography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun HealthcareProfessionalStatus(
    hasActiveProfessional: Boolean,
    professionalName: String?,
    isCompliant: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = stringResource(R.string.healthcare_professional_status),
                    style = ElderlyTypography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (hasActiveProfessional) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCompliant) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isCompliant) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = professionalName ?: stringResource(R.string.healthcare_professional_assigned),
                        style = ElderlyTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                if (!isCompliant) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.compliance_check_in_progress),
                        style = ElderlyTypography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = stringResource(R.string.no_healthcare_professional_assigned),
                        style = ElderlyTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// Data class for emergency type information
private data class EmergencyTypeInfo(
    val type: MedicalEmergencyType,
    val titleRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color,
    val borderColor: Color,
    val priorityLevel: Int,
    val priorityColor: Color,
    val priorityText: String
)

private fun getEmergencyTypes(): List<EmergencyTypeInfo> {
    return listOf(
        EmergencyTypeInfo(
            type = MedicalEmergencyType.CARDIAC_EVENT,
            titleRes = R.string.cardiac_emergency,
            descriptionRes = R.string.cardiac_emergency_description,
            icon = Icons.Default.Favorite,
            iconColor = Color(0xFFE53E3E),
            backgroundColor = Color(0xFFE53E3E).copy(alpha = 0.1f),
            borderColor = Color(0xFFE53E3E),
            priorityLevel = 5,
            priorityColor = Color(0xFFE53E3E),
            priorityText = "CRITICAL"
        ),
        EmergencyTypeInfo(
            type = MedicalEmergencyType.FALL_WITH_INJURY,
            titleRes = R.string.fall_emergency,
            descriptionRes = R.string.fall_emergency_description,
            icon = Icons.Default.PersonOff,
            iconColor = Color(0xFFFF6B00),
            backgroundColor = Color(0xFFFF6B00).copy(alpha = 0.1f),
            borderColor = Color(0xFFFF6B00),
            priorityLevel = 4,
            priorityColor = Color(0xFFFF6B00),
            priorityText = "HIGH"
        ),
        EmergencyTypeInfo(
            type = MedicalEmergencyType.MEDICATION_EMERGENCY,
            titleRes = R.string.medication_emergency,
            descriptionRes = R.string.medication_emergency_description,
            icon = Icons.Default.LocalPharmacy,
            iconColor = Color(0xFFFF9800),
            backgroundColor = Color(0xFFFF9800).copy(alpha = 0.1f),
            borderColor = Color(0xFFFF9800),
            priorityLevel = 3,
            priorityColor = Color(0xFFFF9800),
            priorityText = "MEDIUM"
        ),
        EmergencyTypeInfo(
            type = MedicalEmergencyType.COGNITIVE_CRISIS,
            titleRes = R.string.cognitive_emergency,
            descriptionRes = R.string.cognitive_emergency_description,
            icon = Icons.Default.Psychology,
            iconColor = Color(0xFF9C27B0),
            backgroundColor = Color(0xFF9C27B0).copy(alpha = 0.1f),
            borderColor = Color(0xFF9C27B0),
            priorityLevel = 3,
            priorityColor = Color(0xFF9C27B0),
            priorityText = "MEDIUM"
        ),
        EmergencyTypeInfo(
            type = MedicalEmergencyType.GENERAL_MEDICAL,
            titleRes = R.string.general_medical_emergency,
            descriptionRes = R.string.general_medical_emergency_description,
            icon = Icons.Default.MedicalServices,
            iconColor = Color(0xFF2196F3),
            backgroundColor = Color(0xFF2196F3).copy(alpha = 0.1f),
            borderColor = Color(0xFF2196F3),
            priorityLevel = 2,
            priorityColor = Color(0xFF2196F3),
            priorityText = "STANDARD"
        )
    )
}
