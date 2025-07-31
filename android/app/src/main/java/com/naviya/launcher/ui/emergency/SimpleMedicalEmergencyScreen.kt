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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.naviya.launcher.R
import com.naviya.launcher.emergency.MedicalEmergencyType

/**
 * Simplified Medical Emergency Screen
 * Provides accessible emergency activation without complex dependencies
 * Follows Windsurf rules for elderly accessibility and emergency response
 */
@Composable
fun SimpleMedicalEmergencyScreen(
    onNavigateBack: () -> Unit,
    onEmergencyActivated: (MedicalEmergencyType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedEmergency by remember { mutableStateOf<MedicalEmergencyType?>(null) }
    var isActivating by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            SimpleMedicalEmergencyHeader(onNavigateBack = onNavigateBack)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (showConfirmation && selectedEmergency != null) {
                // Confirmation Screen
                EmergencyConfirmationScreen(
                    emergencyType = selectedEmergency!!,
                    onConfirm = {
                        isActivating = true
                        onEmergencyActivated(selectedEmergency!!)
                        // Simulate activation
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(2000)
                            isActivating = false
                            showConfirmation = false
                            selectedEmergency = null
                        }
                    },
                    onCancel = {
                        showConfirmation = false
                        selectedEmergency = null
                    },
                    isActivating = isActivating
                )
            } else {
                // Emergency Type Selection
                SimpleEmergencyTypeSelection(
                    onEmergencySelected = { emergencyType ->
                        selectedEmergency = emergencyType
                        showConfirmation = true
                    }
                )
            }
        }
    }
}

@Composable
private fun SimpleMedicalEmergencyHeader(
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Text(
            text = "Medical Emergency",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        // Emergency call button
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
                contentDescription = "Emergency Call",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun SimpleEmergencyTypeSelection(
    onEmergencySelected: (MedicalEmergencyType) -> Unit
) {
    Column {
        Text(
            text = "What type of emergency?",
            style = MaterialTheme.typography.headlineMedium.copy(
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
            items(getSimpleEmergencyTypes()) { emergencyType ->
                SimpleEmergencyTypeCard(
                    emergencyType = emergencyType,
                    onClick = { onEmergencySelected(emergencyType.type) }
                )
            }
        }
    }
}

@Composable
private fun SimpleEmergencyTypeCard(
    emergencyType: SimpleEmergencyTypeInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    text = emergencyType.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = emergencyType.description,
                    style = MaterialTheme.typography.bodyLarge,
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
                        style = MaterialTheme.typography.bodySmall,
                        color = emergencyType.priorityColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = emergencyType.iconColor
            )
        }
    }
}

@Composable
private fun EmergencyConfirmationScreen(
    emergencyType: MedicalEmergencyType,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isActivating: Boolean
) {
    val emergencyInfo = getSimpleEmergencyTypes().find { it.type == emergencyType }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isActivating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = Color(0xFFE53E3E)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Activating Emergency Response...",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Emergency services are being contacted",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            } else {
                emergencyInfo?.let { info ->
                    Icon(
                        imageVector = info.icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = info.iconColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Confirm Emergency",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "You selected: ${info.title}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = info.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onCancel,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53E3E)
                            )
                        ) {
                            Text(
                                text = "ACTIVATE EMERGENCY",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data class for simplified emergency type information
private data class SimpleEmergencyTypeInfo(
    val type: MedicalEmergencyType,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color,
    val borderColor: Color,
    val priorityLevel: Int,
    val priorityColor: Color,
    val priorityText: String
)

private fun getSimpleEmergencyTypes(): List<SimpleEmergencyTypeInfo> {
    return listOf(
        SimpleEmergencyTypeInfo(
            type = MedicalEmergencyType.CARDIAC_EVENT,
            title = "Heart Problem",
            description = "Chest pain, difficulty breathing, heart attack symptoms",
            icon = Icons.Default.Favorite,
            iconColor = Color(0xFFE53E3E),
            backgroundColor = Color(0xFFE53E3E).copy(alpha = 0.1f),
            borderColor = Color(0xFFE53E3E),
            priorityLevel = 1,
            priorityColor = Color(0xFFE53E3E),
            priorityText = "Critical"
        ),
        SimpleEmergencyTypeInfo(
            type = MedicalEmergencyType.FALL_WITH_INJURY,
            title = "Fall or Injury",
            description = "I have fallen and may be injured",
            icon = Icons.Default.Person,
            iconColor = Color(0xFFFF6B00),
            backgroundColor = Color(0xFFFF6B00).copy(alpha = 0.1f),
            borderColor = Color(0xFFFF6B00),
            priorityLevel = 2,
            priorityColor = Color(0xFFFF6B00),
            priorityText = "High"
        ),
        SimpleEmergencyTypeInfo(
            type = MedicalEmergencyType.MEDICATION_EMERGENCY,
            title = "Medication Problem",
            description = "Wrong medication, overdose, or medication reaction",
            icon = Icons.Default.Add,
            iconColor = Color(0xFFFF9800),
            backgroundColor = Color(0xFFFF9800).copy(alpha = 0.1f),
            borderColor = Color(0xFFFF9800),
            priorityLevel = 3,
            priorityColor = Color(0xFFFF9800),
            priorityText = "Medium"
        ),
        SimpleEmergencyTypeInfo(
            type = MedicalEmergencyType.COGNITIVE_CRISIS,
            title = "Confusion or Memory",
            description = "Severe confusion, disorientation, or memory problems",
            icon = Icons.Default.Person,
            iconColor = Color(0xFF9C27B0),
            backgroundColor = Color(0xFF9C27B0).copy(alpha = 0.1f),
            borderColor = Color(0xFF9C27B0),
            priorityLevel = 3,
            priorityColor = Color(0xFF9C27B0),
            priorityText = "Medium"
        ),
        SimpleEmergencyTypeInfo(
            type = MedicalEmergencyType.GENERAL_MEDICAL,
            title = "Other Medical",
            description = "General medical emergency or feeling unwell",
            icon = Icons.Default.Add,
            iconColor = Color(0xFF2196F3),
            backgroundColor = Color(0xFF2196F3).copy(alpha = 0.1f),
            borderColor = Color(0xFF2196F3),
            priorityLevel = 4,
            priorityColor = Color(0xFF2196F3),
            priorityText = "Standard"
        )
    )
}
