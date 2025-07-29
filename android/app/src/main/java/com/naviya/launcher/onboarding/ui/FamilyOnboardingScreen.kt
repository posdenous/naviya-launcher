package com.naviya.launcher.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naviya.launcher.R
import com.naviya.launcher.onboarding.*
import com.naviya.launcher.ui.theme.NaviyaTheme

/**
 * Family-friendly onboarding screen with elderly-optimized UI
 * Implements accessibility guidelines and simple navigation
 */
@Composable
fun FamilyOnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: FamilyOnboardingViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val setupProgress by viewModel.setupProgress.collectAsStateWithLifecycle()
    
    NaviyaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp), // Large padding for elderly users
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress indicator
                OnboardingProgressIndicator(
                    currentStep = currentStep,
                    progress = setupProgress.getProgressPercentage()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Main content based on current step
                when (currentStep) {
                    OnboardingStep.WELCOME -> WelcomeStep(
                        onStartSetup = { elderlyName, familyName, relationship ->
                            viewModel.startFamilyAssistedSetup(elderlyName, familyName, relationship)
                        }
                    )
                    OnboardingStep.FAMILY_INTRODUCTION -> FamilyIntroductionStep(
                        setupProgress = setupProgress,
                        onContinue = { viewModel.proceedToNextStep() }
                    )
                    OnboardingStep.BASIC_PREFERENCES -> BasicPreferencesStep(
                        onPreferencesSet = { preferences ->
                            viewModel.configureBasicPreferences(preferences)
                        }
                    )
                    OnboardingStep.EMERGENCY_CONTACTS -> EmergencyContactsStep(
                        onContactsSet = { contacts ->
                            viewModel.setupEmergencyContacts(contacts)
                        }
                    )
                    OnboardingStep.OPTIONAL_CAREGIVER -> OptionalCaregiverStep(
                        onCaregiverSet = { caregiver, consent ->
                            viewModel.optionalCaregiverPairing(caregiver, consent)
                        },
                        onSkip = {
                            viewModel.optionalCaregiverPairing(null, false)
                        }
                    )
                    OnboardingStep.SKIP_PROFESSIONAL -> SkipProfessionalStep(
                        onSkip = { reason ->
                            viewModel.skipProfessionalInstallation(reason)
                        }
                    )
                    OnboardingStep.COMPLETION -> CompletionStep(
                        setupProgress = setupProgress,
                        onComplete = {
                            viewModel.completeOnboarding()
                        }
                    )
                    OnboardingStep.LAUNCHER_READY -> LauncherReadyStep(
                        onFinish = onOnboardingComplete
                    )
                }
                
                // Error display
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorCard(
                        message = error,
                        onDismiss = { viewModel.clearError() }
                    )
                }
                
                // Loading overlay
                if (isLoading) {
                    LoadingOverlay()
                }
            }
        }
    }
}

/**
 * Progress indicator optimized for elderly users
 */
@Composable
private fun OnboardingProgressIndicator(
    currentStep: OnboardingStep,
    progress: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_progress_title),
            fontSize = 20.sp, // Large font for elderly
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp), // Thick progress bar for visibility
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${progress.toInt()}% ${stringResource(R.string.complete)}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Welcome step - start of family setup
 */
@Composable
private fun WelcomeStep(
    onStartSetup: (String, String, String) -> Unit
) {
    var elderlyName by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.welcome_to_naviya),
            fontSize = 28.sp, // Extra large for elderly
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.family_setup_description),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Input fields with elderly-friendly design
        ElderlyTextField(
            value = elderlyName,
            onValueChange = { elderlyName = it },
            label = stringResource(R.string.elderly_user_name),
            placeholder = stringResource(R.string.elderly_name_hint)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ElderlyTextField(
            value = familyName,
            onValueChange = { familyName = it },
            label = stringResource(R.string.family_member_name),
            placeholder = stringResource(R.string.family_name_hint)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ElderlyTextField(
            value = relationship,
            onValueChange = { relationship = it },
            label = stringResource(R.string.relationship),
            placeholder = stringResource(R.string.relationship_hint)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ElderlyButton(
            onClick = {
                if (elderlyName.isNotBlank() && familyName.isNotBlank()) {
                    onStartSetup(elderlyName, familyName, relationship.ifBlank { "family_member" })
                }
            },
            enabled = elderlyName.isNotBlank() && familyName.isNotBlank(),
            text = stringResource(R.string.start_setup)
        )
    }
}

/**
 * Family introduction step
 */
@Composable
private fun FamilyIntroductionStep(
    setupProgress: OnboardingProgress,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.hello_family_title, setupProgress.elderlyUserName),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        InfoCard(
            title = stringResource(R.string.family_setup_benefits_title),
            content = stringResource(R.string.family_setup_benefits_content)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        InfoCard(
            title = stringResource(R.string.privacy_first_title),
            content = stringResource(R.string.privacy_first_content)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ElderlyButton(
            onClick = onContinue,
            text = stringResource(R.string.continue_setup)
        )
    }
}

/**
 * Emergency contacts step - REQUIRED for safety
 */
@Composable
private fun EmergencyContactsStep(
    onContactsSet: (List<EmergencyContactInfo>) -> Unit
) {
    var contacts by remember { mutableStateOf(listOf(EmergencyContactInfo("", "", "", true))) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.emergency_contacts_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.emergency_contacts_description),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Emergency contacts list
        contacts.forEachIndexed { index, contact ->
            EmergencyContactCard(
                contact = contact,
                onContactChange = { updatedContact ->
                    contacts = contacts.toMutableList().apply {
                        set(index, updatedContact)
                    }
                },
                onRemove = if (contacts.size > 1) {
                    {
                        contacts = contacts.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                } else null,
                isPrimary = index == 0
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Add contact button
        if (contacts.size < 3) {
            OutlinedButton(
                onClick = {
                    contacts = contacts + EmergencyContactInfo("", "", "", false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_emergency_contact),
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        ElderlyButton(
            onClick = {
                val validContacts = contacts.filter { 
                    it.name.isNotBlank() && it.phone.isNotBlank() 
                }
                if (validContacts.isNotEmpty()) {
                    onContactsSet(validContacts)
                }
            },
            enabled = contacts.any { it.name.isNotBlank() && it.phone.isNotBlank() },
            text = stringResource(R.string.save_emergency_contacts)
        )
    }
}

/**
 * Optional caregiver step - emphasizes it's optional
 */
@Composable
private fun OptionalCaregiverStep(
    onCaregiverSet: (CaregiverInfo?, Boolean) -> Unit,
    onSkip: () -> Unit
) {
    var caregiverName by remember { mutableStateOf("") }
    var caregiverEmail by remember { mutableStateOf("") }
    var caregiverPhone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var userConsent by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.optional_caregiver_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        InfoCard(
            title = stringResource(R.string.caregiver_optional_title),
            content = stringResource(R.string.caregiver_optional_content),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Caregiver input fields
        ElderlyTextField(
            value = caregiverName,
            onValueChange = { caregiverName = it },
            label = stringResource(R.string.caregiver_name),
            placeholder = stringResource(R.string.caregiver_name_hint)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ElderlyTextField(
            value = caregiverEmail,
            onValueChange = { caregiverEmail = it },
            label = stringResource(R.string.caregiver_email),
            placeholder = stringResource(R.string.caregiver_email_hint),
            keyboardType = KeyboardType.Email
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ElderlyTextField(
            value = caregiverPhone,
            onValueChange = { caregiverPhone = it },
            label = stringResource(R.string.caregiver_phone),
            placeholder = stringResource(R.string.caregiver_phone_hint),
            keyboardType = KeyboardType.Phone
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ElderlyTextField(
            value = relationship,
            onValueChange = { relationship = it },
            label = stringResource(R.string.caregiver_relationship),
            placeholder = stringResource(R.string.caregiver_relationship_hint)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Consent checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = userConsent,
                onCheckedChange = { userConsent = it },
                modifier = Modifier.size(32.dp) // Large checkbox for elderly
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.caregiver_consent_text),
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.skip_caregiver),
                    fontSize = 16.sp
                )
            }
            
            ElderlyButton(
                onClick = {
                    if (caregiverName.isNotBlank() && userConsent) {
                        val caregiver = CaregiverInfo(
                            name = caregiverName,
                            email = caregiverEmail,
                            phone = caregiverPhone,
                            relationship = relationship.ifBlank { "caregiver" }
                        )
                        onCaregiverSet(caregiver, true)
                    }
                },
                enabled = caregiverName.isNotBlank() && userConsent,
                text = stringResource(R.string.add_caregiver),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Elderly-optimized text field
 */
@Composable
private fun ElderlyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 16.sp
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 16.sp
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp), // Tall for elderly users
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Elderly-optimized button
 */
@Composable
private fun ElderlyButton(
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp), // Extra tall for elderly users
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Information card component
 */
@Composable
private fun InfoCard(
    title: String,
    content: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Emergency contact input card
 */
@Composable
private fun EmergencyContactCard(
    contact: EmergencyContactInfo,
    onContactChange: (EmergencyContactInfo) -> Unit,
    onRemove: (() -> Unit)?,
    isPrimary: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPrimary) 
                        stringResource(R.string.primary_emergency_contact)
                    else 
                        stringResource(R.string.emergency_contact),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                onRemove?.let { remove ->
                    TextButton(onClick = remove) {
                        Text(stringResource(R.string.remove))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ElderlyTextField(
                value = contact.name,
                onValueChange = { onContactChange(contact.copy(name = it)) },
                label = stringResource(R.string.contact_name),
                placeholder = stringResource(R.string.contact_name_hint)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ElderlyTextField(
                value = contact.phone,
                onValueChange = { onContactChange(contact.copy(phone = it)) },
                label = stringResource(R.string.contact_phone),
                placeholder = stringResource(R.string.contact_phone_hint),
                keyboardType = KeyboardType.Phone
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ElderlyTextField(
                value = contact.relationship,
                onValueChange = { onContactChange(contact.copy(relationship = it)) },
                label = stringResource(R.string.contact_relationship),
                placeholder = stringResource(R.string.contact_relationship_hint)
            )
        }
    }
}

/**
 * Error display card
 */
@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.dismiss),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Loading overlay
 */
@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.setting_up),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FamilyOnboardingScreenPreview() {
    NaviyaTheme {
        FamilyOnboardingScreen(
            onOnboardingComplete = {}
        )
    }
}
