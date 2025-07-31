package com.naviya.launcher.healthcare.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naviya.launcher.R
import com.naviya.launcher.healthcare.data.*
import com.naviya.launcher.ui.theme.NaviyaTheme
import com.naviya.launcher.ui.components.*

/**
 * Professional Installation Screen
 * Accessible interface for healthcare professionals to perform system installation
 * Includes consent management, clinical context, and safety protocols
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalInstallationScreen(
    userId: String,
    professionalId: String,
    onNavigateBack: () -> Unit,
    onInstallationComplete: (String) -> Unit,
    viewModel: ProfessionalInstallationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(userId, professionalId) {
        viewModel.initializeInstallation(userId, professionalId)
    }

    NaviyaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Professional Installation Interface"
                },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            ProfessionalInstallationHeader(
                professionalName = uiState.professionalName,
                patientName = uiState.patientName,
                onNavigateBack = onNavigateBack
            )

            // Installation Progress
            if (uiState.isInitialized) {
                InstallationProgressSection(
                    completedSteps = uiState.completedSteps,
                    currentStep = uiState.currentStep,
                    totalSteps = uiState.totalSteps
                )

                // Current Step Content
                when (uiState.currentStep) {
                    1 -> AuthorizationVerificationStep(
                        uiState = uiState,
                        onVerificationComplete = viewModel::completeAuthorizationVerification
                    )
                    2 -> PatientConsentStep(
                        uiState = uiState,
                        onConsentChange = viewModel::updatePatientConsent,
                        onConsentComplete = viewModel::completePatientConsent
                    )
                    3 -> ClinicalContextStep(
                        uiState = uiState,
                        onContextChange = viewModel::updateClinicalContext,
                        onContextComplete = viewModel::completeClinicalContext
                    )
                    4 -> SystemConfigurationStep(
                        uiState = uiState,
                        onConfigurationComplete = viewModel::completeSystemConfiguration
                    )
                    5 -> SafetyProtocolsStep(
                        uiState = uiState,
                        onProtocolsComplete = viewModel::completeSafetyProtocols
                    )
                    6 -> FinalReviewStep(
                        uiState = uiState,
                        onInstallationComplete = viewModel::completeInstallation
                    )
                }

                // Navigation Buttons
                InstallationNavigationButtons(
                    uiState = uiState,
                    onPrevious = viewModel::previousStep,
                    onNext = viewModel::nextStep,
                    onCancel = onNavigateBack
                )
            }

            // Status Messages
            if (uiState.isLoading) {
                LoadingIndicator(
                    message = uiState.loadingMessage ?: stringResource(R.string.healthcare_processing_installation)
                )
            }

            uiState.errorMessage?.let { error ->
                ErrorMessage(
                    message = error,
                    onDismiss = viewModel::clearError
                )
            }

            uiState.successMessage?.let { success ->
                SuccessMessage(
                    message = success,
                    onComplete = {
                        uiState.installationId?.let { id ->
                            onInstallationComplete(id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfessionalInstallationHeader(
    professionalName: String,
    patientName: String,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccessibleIconButton(
                onClick = onNavigateBack,
                contentDescription = stringResource(R.string.navigate_back),
                icon = "arrow_back"
            )

            Text(
                text = stringResource(R.string.healthcare_professional_installation),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.healthcare_installation_details),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(R.string.healthcare_professional_name, professionalName),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(R.string.healthcare_patient_name, patientName),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun InstallationProgressSection(
    completedSteps: Int,
    currentStep: Int,
    totalSteps: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.healthcare_installation_progress),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Progress Bar
            LinearProgressIndicator(
                progress = completedSteps.toFloat() / totalSteps.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline
            )

            Text(
                text = stringResource(
                    R.string.healthcare_step_progress,
                    currentStep,
                    totalSteps
                ),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Step Names
            InstallationStepsList(
                completedSteps = completedSteps,
                currentStep = currentStep,
                totalSteps = totalSteps
            )
        }
    }
}

@Composable
private fun InstallationStepsList(
    completedSteps: Int,
    currentStep: Int,
    totalSteps: Int
) {
    val stepNames = listOf(
        stringResource(R.string.healthcare_authorization_verification),
        stringResource(R.string.healthcare_patient_consent),
        stringResource(R.string.healthcare_clinical_context),
        stringResource(R.string.healthcare_system_configuration),
        stringResource(R.string.healthcare_safety_protocols),
        stringResource(R.string.healthcare_final_review)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stepNames.forEachIndexed { index, stepName ->
            val stepNumber = index + 1
            val isCompleted = stepNumber <= completedSteps
            val isCurrent = stepNumber == currentStep

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .semantics {
                            contentDescription = when {
                                isCompleted -> "Step $stepNumber completed"
                                isCurrent -> "Step $stepNumber current"
                                else -> "Step $stepNumber pending"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isCompleted -> {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        isCurrent -> {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.RadioButtonChecked,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Text(
                    text = stepName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthorizationVerificationStep(
    uiState: ProfessionalInstallationUiState,
    onVerificationComplete: () -> Unit
) {
    InstallationStepCard(
        title = stringResource(R.string.healthcare_authorization_verification),
        description = stringResource(R.string.healthcare_authorization_description)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Professional Authorization Status
            AuthorizationStatusCard(
                title = stringResource(R.string.healthcare_professional_authorization),
                status = uiState.professionalAuthorized,
                details = uiState.authorizationDetails
            )

            // Institution Verification
            AuthorizationStatusCard(
                title = stringResource(R.string.healthcare_institution_verification),
                status = uiState.institutionVerified,
                details = uiState.institutionDetails
            )

            // Credentials Check
            AuthorizationStatusCard(
                title = stringResource(R.string.healthcare_credentials_verification),
                status = uiState.credentialsVerified,
                details = uiState.credentialsDetails
            )

            if (uiState.allAuthorizationsVerified) {
                AccessibleButton(
                    onClick = onVerificationComplete,
                    text = stringResource(R.string.healthcare_proceed_to_consent),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = stringResource(R.string.healthcare_authorization_pending),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AuthorizationStatusCard(
    title: String,
    status: Boolean,
    details: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (status) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (status) {
                    androidx.compose.material.icons.Icons.Default.CheckCircle
                } else {
                    androidx.compose.material.icons.Icons.Default.Error
                },
                contentDescription = if (status) "Verified" else "Not verified",
                tint = if (status) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                },
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (status) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )

                details?.let { detail ->
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = if (status) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientConsentStep(
    uiState: ProfessionalInstallationUiState,
    onConsentChange: (String, Any) -> Unit,
    onConsentComplete: () -> Unit
) {
    InstallationStepCard(
        title = stringResource(R.string.healthcare_patient_consent),
        description = stringResource(R.string.healthcare_consent_description)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Consent Method Selection
            ConsentMethodSelection(
                selectedMethod = uiState.consentMethod,
                onMethodSelected = { onConsentChange("consentMethod", it) }
            )

            // Consent Text Display
            ConsentTextDisplay(
                consentText = uiState.consentText
            )

            // Consent Confirmation
            ConsentConfirmation(
                isConfirmed = uiState.consentConfirmed,
                onConfirmationChange = { onConsentChange("consentConfirmed", it) },
                witnessRequired = uiState.witnessRequired,
                witnessName = uiState.witnessName,
                onWitnessNameChange = { onConsentChange("witnessName", it) }
            )

            if (uiState.canProceedFromConsent) {
                AccessibleButton(
                    onClick = onConsentComplete,
                    text = stringResource(R.string.healthcare_consent_confirmed),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ConsentMethodSelection(
    selectedMethod: ConsentMethod?,
    onMethodSelected: (ConsentMethod) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.healthcare_consent_method),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        ConsentMethod.values().forEach { method ->
            ConsentMethodOption(
                method = method,
                isSelected = selectedMethod == method,
                onSelected = { onMethodSelected(method) }
            )
        }
    }
}

@Composable
private fun ConsentMethodOption(
    method: ConsentMethod,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Consent method: ${method.name.replace("_", " ").lowercase()}"
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onSelected
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelected,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )

            Column {
                Text(
                    text = method.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = getConsentMethodDescription(method),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun ConsentTextDisplay(
    consentText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.healthcare_consent_agreement),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = consentText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConsentConfirmation(
    isConfirmed: Boolean,
    onConfirmationChange: (Boolean) -> Unit,
    witnessRequired: Boolean,
    witnessName: String,
    onWitnessNameChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Consent checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isConfirmed,
                onCheckedChange = onConfirmationChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = stringResource(R.string.healthcare_consent_confirmation),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        // Witness field if required
        if (witnessRequired) {
            AccessibleOutlinedTextField(
                value = witnessName,
                onValueChange = onWitnessNameChange,
                label = stringResource(R.string.healthcare_witness_name),
                placeholder = stringResource(R.string.healthcare_witness_name_hint),
                isRequired = true
            )
        }
    }
}

@Composable
private fun InstallationStepCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            content()
        }
    }
}

@Composable
private fun InstallationNavigationButtons(
    uiState: ProfessionalInstallationUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cancel button
        AccessibleButton(
            onClick = onCancel,
            text = stringResource(R.string.cancel),
            style = ButtonStyle.OUTLINED,
            modifier = Modifier.weight(1f)
        )

        // Previous button
        if (uiState.canGoToPreviousStep) {
            AccessibleButton(
                onClick = onPrevious,
                text = stringResource(R.string.previous),
                style = ButtonStyle.OUTLINED,
                modifier = Modifier.weight(1f)
            )
        }

        // Next button
        if (uiState.canGoToNextStep) {
            AccessibleButton(
                onClick = onNext,
                text = if (uiState.isLastStep) {
                    stringResource(R.string.healthcare_complete_installation)
                } else {
                    stringResource(R.string.next)
                },
                enabled = uiState.canProceedToNextStep,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Helper functions
private fun getConsentMethodDescription(method: ConsentMethod): String {
    return when (method) {
        ConsentMethod.VERBAL_CONSENT -> "Verbal agreement recorded"
        ConsentMethod.WRITTEN_SIGNATURE -> "Physical signature required"
        ConsentMethod.DIGITAL_SIGNATURE -> "Electronic signature"
        ConsentMethod.WITNESSED_CONSENT -> "Requires independent witness"
        ConsentMethod.GUARDIAN_CONSENT -> "Legal guardian consent"
    }
}

// Additional step composables would be implemented similarly...
// ClinicalContextStep, SystemConfigurationStep, SafetyProtocolsStep, FinalReviewStep
