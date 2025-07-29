package com.naviya.launcher.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naviya.launcher.R
import com.naviya.launcher.onboarding.*

/**
 * Basic preferences step with elderly-optimized defaults
 */
@Composable
fun BasicPreferencesStep(
    onPreferencesSet: (BasicPreferences) -> Unit
) {
    var fontScale by remember { mutableStateOf(1.6f) }
    var iconScale by remember { mutableStateOf(1.4f) }
    var highContrast by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }
    var slowAnimations by remember { mutableStateOf(true) }
    var preferredLanguage by remember { mutableStateOf("en") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.basic_preferences_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.basic_preferences_description),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Font size preference
        PreferenceCard(
            title = stringResource(R.string.font_size),
            description = stringResource(R.string.font_size_description),
            icon = Icons.Default.Info
        ) {
            Column {
                Text(
                    text = stringResource(R.string.current_size, "${(fontScale * 100).toInt()}%"),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = fontScale,
                    onValueChange = { fontScale = it },
                    valueRange = 1.6f..2.5f, // Minimum 1.6x for elderly
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Icon size preference
        PreferenceCard(
            title = stringResource(R.string.icon_size),
            description = stringResource(R.string.icon_size_description),
            icon = Icons.Default.Info
        ) {
            Column {
                Text(
                    text = stringResource(R.string.current_size, "${(iconScale * 100).toInt()}%"),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = iconScale,
                    onValueChange = { iconScale = it },
                    valueRange = 1.4f..2.0f, // Minimum 1.4x for elderly
                    steps = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // High contrast toggle
        PreferenceCard(
            title = stringResource(R.string.high_contrast),
            description = stringResource(R.string.high_contrast_description),
            icon = Icons.Default.Security
        ) {
            Switch(
                checked = highContrast,
                onCheckedChange = { highContrast = it },
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Haptic feedback toggle
        PreferenceCard(
            title = stringResource(R.string.haptic_feedback),
            description = stringResource(R.string.haptic_feedback_description),
            icon = Icons.Default.Info
        ) {
            Switch(
                checked = hapticFeedback,
                onCheckedChange = { hapticFeedback = it },
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Slow animations toggle
        PreferenceCard(
            title = stringResource(R.string.slow_animations),
            description = stringResource(R.string.slow_animations_description),
            icon = Icons.Default.Info
        ) {
            Switch(
                checked = slowAnimations,
                onCheckedChange = { slowAnimations = it },
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ElderlyButton(
            onClick = {
                val preferences = BasicPreferences(
                    fontScale = fontScale,
                    iconScale = iconScale,
                    highContrastEnabled = highContrast,
                    hapticFeedbackEnabled = hapticFeedback,
                    slowAnimationsEnabled = slowAnimations,
                    emergencyButtonAlwaysVisible = true, // Always enabled for safety
                    preferredLanguage = preferredLanguage
                )
                onPreferencesSet(preferences)
            },
            text = stringResource(R.string.save_preferences)
        )
    }
}

/**
 * Skip professional installation step
 */
@Composable
fun SkipProfessionalStep(
    onSkip: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.professional_installation_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        InfoCard(
            title = stringResource(R.string.family_setup_sufficient_title),
            content = stringResource(R.string.family_setup_sufficient_content),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        InfoCard(
            title = stringResource(R.string.professional_when_needed_title),
            content = stringResource(R.string.professional_when_needed_content)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ElderlyButton(
            onClick = { onSkip("family_assisted_setup_sufficient") },
            text = stringResource(R.string.continue_with_family_setup)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { onSkip("user_prefers_professional") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.schedule_professional_later),
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Completion step showing setup summary
 */
@Composable
fun CompletionStep(
    setupProgress: OnboardingProgress,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.setup_almost_complete),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Setup summary
        SetupSummaryCard(setupProgress = setupProgress)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        InfoCard(
            title = stringResource(R.string.ready_to_use_title),
            content = stringResource(R.string.ready_to_use_content),
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ElderlyButton(
            onClick = onComplete,
            text = stringResource(R.string.complete_setup)
        )
    }
}

/**
 * Final step - launcher is ready
 */
@Composable
fun LauncherReadyStep(
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.welcome_to_naviya_launcher),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.launcher_ready_description),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        InfoCard(
            title = stringResource(R.string.getting_started_title),
            content = stringResource(R.string.getting_started_content)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        InfoCard(
            title = stringResource(R.string.emergency_button_reminder_title),
            content = stringResource(R.string.emergency_button_reminder_content),
            backgroundColor = MaterialTheme.colorScheme.errorContainer
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ElderlyButton(
            onClick = onFinish,
            text = stringResource(R.string.start_using_launcher)
        )
    }
}

/**
 * Preference card component
 */
@Composable
private fun PreferenceCard(
    title: String,
    description: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

/**
 * Setup summary card
 */
@Composable
private fun SetupSummaryCard(
    setupProgress: OnboardingProgress
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.setup_summary),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SummaryItem(
                label = stringResource(R.string.user_name),
                value = setupProgress.elderlyUserName
            )
            
            SummaryItem(
                label = stringResource(R.string.family_assistant),
                value = setupProgress.familyAssistantName
            )
            
            SummaryItem(
                label = stringResource(R.string.emergency_contacts),
                value = stringResource(
                    R.string.contacts_count,
                    setupProgress.emergencyContacts.size
                )
            )
            
            SummaryItem(
                label = stringResource(R.string.caregiver_status),
                value = if (setupProgress.caregiverPaired) {
                    stringResource(R.string.caregiver_paired)
                } else {
                    stringResource(R.string.no_caregiver_independent)
                }
            )
            
            SummaryItem(
                label = stringResource(R.string.setup_time),
                value = stringResource(
                    R.string.minutes_elapsed,
                    setupProgress.getTimeSpentMinutes()
                )
            )
        }
    }
}

/**
 * Summary item component
 */
@Composable
private fun SummaryItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
