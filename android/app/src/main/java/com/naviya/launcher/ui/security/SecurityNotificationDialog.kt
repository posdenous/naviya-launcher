package com.naviya.launcher.ui.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.naviya.launcher.R
import com.naviya.launcher.security.ModeSwitchValidation

/**
 * Security notification dialog for elderly-friendly security alerts
 * Displays clear, accessible messages about security events and blocked actions
 */
@Composable
fun SecurityNotificationDialog(
    validationResult: ModeSwitchValidation,
    reason: String,
    onDismiss: () -> Unit,
    onContactElderRights: () -> Unit = {},
    onEmergencyEscape: () -> Unit = {},
    showEmergencyOptions: Boolean = false
) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = getSecurityCardColor(validationResult)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Security Icon
                Icon(
                    painter = painterResource(getSecurityIcon(validationResult)),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = getSecurityIconColor(validationResult)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = getSecurityTitle(validationResult),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Description
                Text(
                    text = getElderlyFriendlyMessage(validationResult, reason),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Primary Action Button
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
                            text = stringResource(R.string.security_dialog_understood),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Emergency Options (if security issue is serious)
                    if (showEmergencyOptions) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Emergency Escape Button
                            OutlinedButton(
                                onClick = onEmergencyEscape,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.security_emergency_escape),
                                    fontSize = 16.sp
                                )
                            }
                            
                            // Contact Elder Rights Button
                            OutlinedButton(
                                onClick = onContactElderRights,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.security_contact_advocate),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    
                    // Help Text
                    if (isHighRiskSituation(validationResult)) {
                        Text(
                            text = stringResource(R.string.security_help_available),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Get appropriate card background color based on security validation result
 */
@Composable
private fun getSecurityCardColor(validationResult: ModeSwitchValidation): Color {
    return when (validationResult) {
        ModeSwitchValidation.SUSPICIOUS_ACTIVITY,
        ModeSwitchValidation.SYSTEM_LOCKED -> Color(0xFFFFEBEE) // Light red
        
        ModeSwitchValidation.AUTHENTICATION_REQUIRED,
        ModeSwitchValidation.INVALID_CAREGIVER_TOKEN -> Color(0xFFFFF3E0) // Light orange
        
        ModeSwitchValidation.ELDERLY_PROTECTION,
        ModeSwitchValidation.EMERGENCY_ESCAPE_ACTIVE -> Color(0xFFE8F5E8) // Light green
        
        ModeSwitchValidation.RATE_LIMITED -> Color(0xFFF3E5F5) // Light purple
        
        else -> MaterialTheme.colorScheme.surface
    }
}

/**
 * Get appropriate icon based on security validation result
 */
private fun getSecurityIcon(validationResult: ModeSwitchValidation): Int {
    return when (validationResult) {
        ModeSwitchValidation.SUSPICIOUS_ACTIVITY,
        ModeSwitchValidation.SYSTEM_LOCKED -> R.drawable.ic_security_warning
        
        ModeSwitchValidation.AUTHENTICATION_REQUIRED,
        ModeSwitchValidation.INVALID_CAREGIVER_TOKEN -> R.drawable.ic_lock
        
        ModeSwitchValidation.ELDERLY_PROTECTION -> R.drawable.ic_shield_person
        
        ModeSwitchValidation.EMERGENCY_ESCAPE_ACTIVE -> R.drawable.ic_emergency_exit
        
        ModeSwitchValidation.RATE_LIMITED -> R.drawable.ic_timer
        
        else -> R.drawable.ic_info
    }
}

/**
 * Get appropriate icon color based on security validation result
 */
@Composable
private fun getSecurityIconColor(validationResult: ModeSwitchValidation): Color {
    return when (validationResult) {
        ModeSwitchValidation.SUSPICIOUS_ACTIVITY,
        ModeSwitchValidation.SYSTEM_LOCKED -> Color(0xFFD32F2F) // Red
        
        ModeSwitchValidation.AUTHENTICATION_REQUIRED,
        ModeSwitchValidation.INVALID_CAREGIVER_TOKEN -> Color(0xFFFF9800) // Orange
        
        ModeSwitchValidation.ELDERLY_PROTECTION,
        ModeSwitchValidation.EMERGENCY_ESCAPE_ACTIVE -> Color(0xFF4CAF50) // Green
        
        ModeSwitchValidation.RATE_LIMITED -> Color(0xFF9C27B0) // Purple
        
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * Get user-friendly title for security dialog
 */
@Composable
private fun getSecurityTitle(validationResult: ModeSwitchValidation): String {
    return when (validationResult) {
        ModeSwitchValidation.SUSPICIOUS_ACTIVITY -> stringResource(R.string.security_title_suspicious)
        ModeSwitchValidation.SYSTEM_LOCKED -> stringResource(R.string.security_title_locked)
        ModeSwitchValidation.AUTHENTICATION_REQUIRED -> stringResource(R.string.security_title_auth_required)
        ModeSwitchValidation.INVALID_CAREGIVER_TOKEN -> stringResource(R.string.security_title_invalid_caregiver)
        ModeSwitchValidation.ELDERLY_PROTECTION -> stringResource(R.string.security_title_protection)
        ModeSwitchValidation.EMERGENCY_ESCAPE_ACTIVE -> stringResource(R.string.security_title_emergency_active)
        ModeSwitchValidation.RATE_LIMITED -> stringResource(R.string.security_title_rate_limited)
        else -> stringResource(R.string.security_title_general)
    }
}

/**
 * Convert technical security messages to elderly-friendly language
 */
@Composable
private fun getElderlyFriendlyMessage(validationResult: ModeSwitchValidation, reason: String): String {
    return when (validationResult) {
        ModeSwitchValidation.SUSPICIOUS_ACTIVITY -> 
            stringResource(R.string.security_message_suspicious)
        
        ModeSwitchValidation.SYSTEM_LOCKED -> 
            stringResource(R.string.security_message_locked)
        
        ModeSwitchValidation.AUTHENTICATION_REQUIRED -> 
            stringResource(R.string.security_message_auth_required)
        
        ModeSwitchValidation.INVALID_CAREGIVER_TOKEN -> 
            stringResource(R.string.security_message_invalid_caregiver)
        
        ModeSwitchValidation.ELDERLY_PROTECTION -> 
            stringResource(R.string.security_message_protection)
        
        ModeSwitchValidation.EMERGENCY_ESCAPE_ACTIVE -> 
            stringResource(R.string.security_message_emergency_active)
        
        ModeSwitchValidation.RATE_LIMITED -> 
            stringResource(R.string.security_message_rate_limited)
        
        else -> reason
    }
}

/**
 * Determine if this is a high-risk situation requiring emergency options
 */
private fun isHighRiskSituation(validationResult: ModeSwitchValidation): Boolean {
    return validationResult in listOf(
        ModeSwitchValidation.SUSPICIOUS_ACTIVITY,
        ModeSwitchValidation.SYSTEM_LOCKED,
        ModeSwitchValidation.INVALID_CAREGIVER_TOKEN
    )
}

/**
 * Helper function to contact elder rights advocate
 */
fun contactElderRightsAdvocate(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:+441234567890") // Elder Rights Hotline
    }
    context.startActivity(intent)
}

/**
 * Helper function to open elder rights website
 */
fun openElderRightsWebsite(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://www.elderrights.org.uk/help")
    }
    context.startActivity(intent)
}
