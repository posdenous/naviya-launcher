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
 * Healthcare Professional Registration Screen
 * Accessible, elderly-friendly interface for healthcare professional registration
 * Follows Windsurf accessibility rules: 1.6x font scale, 48dp touch targets, high contrast
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalRegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationComplete: (String) -> Unit,
    viewModel: ProfessionalRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    NaviyaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Healthcare Professional Registration Form"
                },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            ProfessionalRegistrationHeader(
                onNavigateBack = onNavigateBack
            )

            // Registration Form
            ProfessionalRegistrationForm(
                uiState = uiState,
                onFieldChange = viewModel::updateField,
                onProfessionalTypeChange = viewModel::updateProfessionalType,
                onSpecializationAdd = viewModel::addSpecialization,
                onSpecializationRemove = viewModel::removeSpecialization
            )

            // Institution Section
            InstitutionAffiliationSection(
                uiState = uiState,
                onInstitutionChange = viewModel::updateInstitution
            )

            // Credentials Section
            CredentialsSection(
                uiState = uiState,
                onCredentialChange = viewModel::updateCredentials
            )

            // Action Buttons
            RegistrationActionButtons(
                uiState = uiState,
                onRegister = viewModel::registerProfessional,
                onCancel = onNavigateBack
            )

            // Status Messages
            if (uiState.isLoading) {
                LoadingIndicator(
                    message = stringResource(R.string.healthcare_registering_professional)
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
                        uiState.registrationId?.let { id ->
                            onRegistrationComplete(id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfessionalRegistrationHeader(
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
                text = stringResource(R.string.healthcare_professional_registration),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp, // 1.6x scale for elderly users
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
        }

        Text(
            text = stringResource(R.string.healthcare_registration_description),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp // Larger text for elderly users
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProfessionalRegistrationForm(
    uiState: ProfessionalRegistrationUiState,
    onFieldChange: (String, String) -> Unit,
    onProfessionalTypeChange: (ProfessionalType) -> Unit,
    onSpecializationAdd: (String) -> Unit,
    onSpecializationRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.healthcare_personal_information),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Professional ID
            AccessibleOutlinedTextField(
                value = uiState.professionalId,
                onValueChange = { onFieldChange("professionalId", it) },
                label = stringResource(R.string.healthcare_professional_id),
                placeholder = stringResource(R.string.healthcare_professional_id_hint),
                isRequired = true,
                errorMessage = uiState.fieldErrors["professionalId"]
            )

            // First Name
            AccessibleOutlinedTextField(
                value = uiState.firstName,
                onValueChange = { onFieldChange("firstName", it) },
                label = stringResource(R.string.healthcare_first_name),
                placeholder = stringResource(R.string.healthcare_first_name_hint),
                isRequired = true,
                errorMessage = uiState.fieldErrors["firstName"]
            )

            // Last Name
            AccessibleOutlinedTextField(
                value = uiState.lastName,
                onValueChange = { onFieldChange("lastName", it) },
                label = stringResource(R.string.healthcare_last_name),
                placeholder = stringResource(R.string.healthcare_last_name_hint),
                isRequired = true,
                errorMessage = uiState.fieldErrors["lastName"]
            )

            // Professional Type Dropdown
            ProfessionalTypeDropdown(
                selectedType = uiState.professionalType,
                onTypeSelected = onProfessionalTypeChange,
                errorMessage = uiState.fieldErrors["professionalType"]
            )

            // Specializations
            SpecializationsSection(
                specializations = uiState.specializations,
                onAdd = onSpecializationAdd,
                onRemove = onSpecializationRemove
            )

            // Contact Information
            ContactInformationSection(
                uiState = uiState,
                onFieldChange = onFieldChange
            )

            // Experience
            ExperienceSection(
                uiState = uiState,
                onFieldChange = onFieldChange
            )
        }
    }
}

@Composable
private fun ProfessionalTypeDropdown(
    selectedType: ProfessionalType?,
    onTypeSelected: (ProfessionalType) -> Unit,
    errorMessage: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val professionalTypes = ProfessionalType.values()

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            AccessibleOutlinedTextField(
                value = selectedType?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
                onValueChange = { },
                label = stringResource(R.string.healthcare_professional_type),
                placeholder = stringResource(R.string.healthcare_select_professional_type),
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor(),
                isRequired = true,
                errorMessage = errorMessage
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                professionalTypes.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp
                                )
                            )
                        },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp) // Minimum touch target
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecializationsSection(
    specializations: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var newSpecialization by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.healthcare_specializations),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccessibleOutlinedTextField(
                value = newSpecialization,
                onValueChange = { newSpecialization = it },
                label = stringResource(R.string.healthcare_add_specialization),
                placeholder = stringResource(R.string.healthcare_specialization_hint),
                modifier = Modifier.weight(1f)
            )

            AccessibleButton(
                onClick = {
                    if (newSpecialization.isNotBlank()) {
                        onAdd(newSpecialization.trim())
                        newSpecialization = ""
                    }
                },
                text = stringResource(R.string.add),
                enabled = newSpecialization.isNotBlank()
            )
        }

        // Display current specializations
        specializations.forEach { specialization ->
            SpecializationChip(
                specialization = specialization,
                onRemove = { onRemove(specialization) }
            )
        }
    }
}

@Composable
private fun SpecializationChip(
    specialization: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = specialization,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            AccessibleIconButton(
                onClick = onRemove,
                contentDescription = stringResource(R.string.healthcare_remove_specialization, specialization),
                icon = "close",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ContactInformationSection(
    uiState: ProfessionalRegistrationUiState,
    onFieldChange: (String, String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.healthcare_contact_information),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AccessibleOutlinedTextField(
            value = uiState.primaryPhone,
            onValueChange = { onFieldChange("primaryPhone", it) },
            label = stringResource(R.string.healthcare_primary_phone),
            placeholder = stringResource(R.string.healthcare_phone_hint),
            isRequired = true,
            errorMessage = uiState.fieldErrors["primaryPhone"]
        )

        AccessibleOutlinedTextField(
            value = uiState.email,
            onValueChange = { onFieldChange("email", it) },
            label = stringResource(R.string.healthcare_email),
            placeholder = stringResource(R.string.healthcare_email_hint),
            isRequired = true,
            errorMessage = uiState.fieldErrors["email"]
        )

        AccessibleOutlinedTextField(
            value = uiState.officeAddress,
            onValueChange = { onFieldChange("officeAddress", it) },
            label = stringResource(R.string.healthcare_office_address),
            placeholder = stringResource(R.string.healthcare_address_hint),
            isRequired = true,
            errorMessage = uiState.fieldErrors["officeAddress"]
        )

        AccessibleOutlinedTextField(
            value = uiState.emergencyContact,
            onValueChange = { onFieldChange("emergencyContact", it) },
            label = stringResource(R.string.healthcare_emergency_contact),
            placeholder = stringResource(R.string.healthcare_emergency_contact_hint),
            isRequired = false
        )
    }
}

@Composable
private fun ExperienceSection(
    uiState: ProfessionalRegistrationUiState,
    onFieldChange: (String, String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.healthcare_experience),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AccessibleOutlinedTextField(
                value = uiState.yearsOfExperience,
                onValueChange = { onFieldChange("yearsOfExperience", it) },
                label = stringResource(R.string.healthcare_years_experience),
                placeholder = "0",
                isRequired = true,
                errorMessage = uiState.fieldErrors["yearsOfExperience"],
                modifier = Modifier.weight(1f)
            )

            AccessibleOutlinedTextField(
                value = uiState.elderCareExperience,
                onValueChange = { onFieldChange("elderCareExperience", it) },
                label = stringResource(R.string.healthcare_elder_care_experience),
                placeholder = "0",
                isRequired = true,
                errorMessage = uiState.fieldErrors["elderCareExperience"],
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InstitutionAffiliationSection(
    uiState: ProfessionalRegistrationUiState,
    onInstitutionChange: (String, String) -> Unit
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
                text = stringResource(R.string.healthcare_institution_affiliation),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.healthcare_institution_optional),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AccessibleOutlinedTextField(
                value = uiState.institutionName,
                onValueChange = { onInstitutionChange("institutionName", it) },
                label = stringResource(R.string.healthcare_institution_name),
                placeholder = stringResource(R.string.healthcare_institution_name_hint),
                isRequired = false
            )

            AccessibleOutlinedTextField(
                value = uiState.institutionAddress,
                onValueChange = { onInstitutionChange("institutionAddress", it) },
                label = stringResource(R.string.healthcare_institution_address),
                placeholder = stringResource(R.string.healthcare_institution_address_hint),
                isRequired = false
            )
        }
    }
}

@Composable
private fun CredentialsSection(
    uiState: ProfessionalRegistrationUiState,
    onCredentialChange: (String, String) -> Unit
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
                text = stringResource(R.string.healthcare_credentials),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AccessibleOutlinedTextField(
                value = uiState.licenseNumbers,
                onValueChange = { onCredentialChange("licenseNumbers", it) },
                label = stringResource(R.string.healthcare_license_numbers),
                placeholder = stringResource(R.string.healthcare_license_numbers_hint),
                isRequired = true,
                errorMessage = uiState.fieldErrors["licenseNumbers"]
            )

            AccessibleOutlinedTextField(
                value = uiState.boardCertifications,
                onValueChange = { onCredentialChange("boardCertifications", it) },
                label = stringResource(R.string.healthcare_board_certifications),
                placeholder = stringResource(R.string.healthcare_board_certifications_hint),
                isRequired = false
            )

            AccessibleOutlinedTextField(
                value = uiState.malpracticeInsurance,
                onValueChange = { onCredentialChange("malpracticeInsurance", it) },
                label = stringResource(R.string.healthcare_malpractice_insurance),
                placeholder = stringResource(R.string.healthcare_malpractice_insurance_hint),
                isRequired = true,
                errorMessage = uiState.fieldErrors["malpracticeInsurance"]
            )
        }
    }
}

@Composable
private fun RegistrationActionButtons(
    uiState: ProfessionalRegistrationUiState,
    onRegister: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AccessibleButton(
            onClick = onCancel,
            text = stringResource(R.string.cancel),
            style = ButtonStyle.OUTLINED,
            modifier = Modifier.weight(1f)
        )

        AccessibleButton(
            onClick = onRegister,
            text = stringResource(R.string.healthcare_register_professional),
            enabled = !uiState.isLoading && uiState.isFormValid,
            isLoading = uiState.isLoading,
            modifier = Modifier.weight(1f)
        )
    }
}

// UI State Data Class
data class ProfessionalRegistrationUiState(
    val professionalId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val professionalType: ProfessionalType? = null,
    val specializations: List<String> = emptyList(),
    val primaryPhone: String = "",
    val email: String = "",
    val officeAddress: String = "",
    val emergencyContact: String = "",
    val yearsOfExperience: String = "",
    val elderCareExperience: String = "",
    val institutionName: String = "",
    val institutionAddress: String = "",
    val licenseNumbers: String = "",
    val boardCertifications: String = "",
    val malpracticeInsurance: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val registrationId: String? = null,
    val isFormValid: Boolean = false
)
