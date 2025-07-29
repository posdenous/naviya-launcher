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
 * Clinical Assessment Screen
 * Comprehensive assessment interface for healthcare professionals
 * Includes cognitive, functional, social, and risk factor evaluations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalAssessmentScreen(
    userId: String,
    professionalId: String,
    onNavigateBack: () -> Unit,
    onAssessmentComplete: (String) -> Unit,
    viewModel: ClinicalAssessmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(userId, professionalId) {
        viewModel.initializeAssessment(userId, professionalId)
    }

    NaviyaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Clinical Assessment Interface"
                },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            ClinicalAssessmentHeader(
                professionalName = uiState.professionalName,
                patientName = uiState.patientName,
                onNavigateBack = onNavigateBack
            )

            if (uiState.isInitialized) {
                // Assessment Sections
                CognitiveAssessmentSection(
                    assessment = uiState.cognitiveAssessment,
                    onAssessmentChange = viewModel::updateCognitiveAssessment
                )

                FunctionalAssessmentSection(
                    assessment = uiState.functionalAssessment,
                    onAssessmentChange = viewModel::updateFunctionalAssessment
                )

                SocialAssessmentSection(
                    assessment = uiState.socialAssessment,
                    onAssessmentChange = viewModel::updateSocialAssessment
                )

                RiskFactorAssessmentSection(
                    assessment = uiState.riskFactorAssessment,
                    onAssessmentChange = viewModel::updateRiskFactorAssessment
                )

                CaregiverAssessmentSection(
                    assessment = uiState.caregiverAssessment,
                    onAssessmentChange = viewModel::updateCaregiverAssessment
                )

                // Clinical Notes
                ClinicalNotesSection(
                    notes = uiState.clinicalNotes,
                    onNotesChange = viewModel::updateClinicalNotes
                )

                // Risk Summary
                if (uiState.showRiskSummary) {
                    RiskSummarySection(
                        overallRisk = uiState.overallRiskLevel,
                        abuseRisk = uiState.abuseRiskLevel,
                        recommendations = uiState.recommendations
                    )
                }

                // Action Buttons
                AssessmentActionButtons(
                    uiState = uiState,
                    onSaveAssessment = viewModel::saveAssessment,
                    onCompleteAssessment = viewModel::completeAssessment,
                    onCancel = onNavigateBack
                )
            }

            // Status Messages
            if (uiState.isLoading) {
                LoadingIndicator(
                    message = uiState.loadingMessage ?: stringResource(R.string.healthcare_processing_assessment)
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
                        uiState.assessmentId?.let { id ->
                            onAssessmentComplete(id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ClinicalAssessmentHeader(
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
                text = stringResource(R.string.healthcare_clinical_assessment),
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
                    text = stringResource(R.string.healthcare_assessment_details),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(R.string.healthcare_assessing_professional, professionalName),
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

                Text(
                    text = stringResource(R.string.healthcare_assessment_date, getCurrentDate()),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CognitiveAssessmentSection(
    assessment: CognitiveAssessmentUiState,
    onAssessmentChange: (String, Any) -> Unit
) {
    AssessmentSectionCard(
        title = stringResource(R.string.healthcare_cognitive_assessment),
        description = stringResource(R.string.healthcare_cognitive_assessment_description)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Assessment Tool Selection
            AssessmentToolDropdown(
                selectedTool = assessment.assessmentTool,
                onToolSelected = { onAssessmentChange("assessmentTool", it) }
            )

            // Score Input
            AccessibleOutlinedTextField(
                value = assessment.score,
                onValueChange = { onAssessmentChange("score", it) },
                label = stringResource(R.string.healthcare_assessment_score),
                placeholder = stringResource(R.string.healthcare_score_hint),
                isRequired = true
            )

            // Interpretation
            AccessibleOutlinedTextField(
                value = assessment.interpretation,
                onValueChange = { onAssessmentChange("interpretation", it) },
                label = stringResource(R.string.healthcare_interpretation),
                placeholder = stringResource(R.string.healthcare_interpretation_hint),
                isRequired = true,
                maxLines = 3
            )

            // Cognitive Impairment Level
            CognitiveImpairmentLevelSelector(
                selectedLevel = assessment.cognitiveImpairmentLevel,
                onLevelSelected = { onAssessmentChange("cognitiveImpairmentLevel", it) }
            )

            // Decision Making Capacity
            DecisionMakingCapacitySelector(
                selectedCapacity = assessment.decisionMakingCapacity,
                onCapacitySelected = { onAssessmentChange("decisionMakingCapacity", it) }
            )
        }
    }
}

@Composable
private fun FunctionalAssessmentSection(
    assessment: FunctionalAssessmentUiState,
    onAssessmentChange: (String, Any) -> Unit
) {
    AssessmentSectionCard(
        title = stringResource(R.string.healthcare_functional_assessment),
        description = stringResource(R.string.healthcare_functional_assessment_description)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Activities of Daily Living
            Text(
                text = stringResource(R.string.healthcare_activities_daily_living),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            val adlActivities = listOf(
                "bathing" to stringResource(R.string.healthcare_bathing),
                "dressing" to stringResource(R.string.healthcare_dressing),
                "eating" to stringResource(R.string.healthcare_eating),
                "toileting" to stringResource(R.string.healthcare_toileting),
                "transferring" to stringResource(R.string.healthcare_transferring),
                "continence" to stringResource(R.string.healthcare_continence)
            )

            adlActivities.forEach { (key, label) ->
                FunctionalLevelSelector(
                    label = label,
                    selectedLevel = assessment.adlLevels[key],
                    onLevelSelected = { level ->
                        onAssessmentChange("adl_$key", level)
                    }
                )
            }

            Divider()

            // Instrumental Activities of Daily Living
            Text(
                text = stringResource(R.string.healthcare_instrumental_activities),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            val iadlActivities = listOf(
                "medication_management" to stringResource(R.string.healthcare_medication_management),
                "financial_management" to stringResource(R.string.healthcare_financial_management),
                "transportation" to stringResource(R.string.healthcare_transportation),
                "shopping" to stringResource(R.string.healthcare_shopping),
                "housekeeping" to stringResource(R.string.healthcare_housekeeping),
                "meal_preparation" to stringResource(R.string.healthcare_meal_preparation)
            )

            iadlActivities.forEach { (key, label) ->
                FunctionalLevelSelector(
                    label = label,
                    selectedLevel = assessment.iadlLevels[key],
                    onLevelSelected = { level ->
                        onAssessmentChange("iadl_$key", level)
                    }
                )
            }

            Divider()

            // Mobility and Fall Risk
            MobilityStatusSelector(
                selectedStatus = assessment.mobilityStatus,
                onStatusSelected = { onAssessmentChange("mobilityStatus", it) }
            )

            FallRiskLevelSelector(
                selectedLevel = assessment.fallRisk,
                onLevelSelected = { onAssessmentChange("fallRisk", it) }
            )
        }
    }
}

@Composable
private fun SocialAssessmentSection(
    assessment: SocialAssessmentUiState,
    onAssessmentChange: (String, Any) -> Unit
) {
    AssessmentSectionCard(
        title = stringResource(R.string.healthcare_social_assessment),
        description = stringResource(R.string.healthcare_social_assessment_description)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Social Support Level
            SocialSupportLevelSelector(
                selectedLevel = assessment.socialSupport,
                onLevelSelected = { onAssessmentChange("socialSupport", it) }
            )

            // Social Isolation Level
            SocialIsolationLevelSelector(
                selectedLevel = assessment.socialIsolation,
                onLevelSelected = { onAssessmentChange("socialIsolation", it) }
            )

            // Family Dynamics
            AccessibleOutlinedTextField(
                value = assessment.familyDynamics,
                onValueChange = { onAssessmentChange("familyDynamics", it) },
                label = stringResource(R.string.healthcare_family_dynamics),
                placeholder = stringResource(R.string.healthcare_family_dynamics_hint),
                maxLines = 3
            )

            // Caregiver Relationships
            AccessibleOutlinedTextField(
                value = assessment.caregiverRelationships,
                onValueChange = { onAssessmentChange("caregiverRelationships", it) },
                label = stringResource(R.string.healthcare_caregiver_relationships),
                placeholder = stringResource(R.string.healthcare_caregiver_relationships_hint),
                maxLines = 3
            )

            // Community Resources
            CommunityResourcesSection(
                resources = assessment.communityResources,
                onResourceAdd = { resource ->
                    onAssessmentChange("addCommunityResource", resource)
                },
                onResourceRemove = { resource ->
                    onAssessmentChange("removeCommunityResource", resource)
                }
            )
        }
    }
}

@Composable
private fun RiskFactorAssessmentSection(
    assessment: RiskFactorAssessmentUiState,
    onAssessmentChange: (String, Any) -> Unit
) {
    AssessmentSectionCard(
        title = stringResource(R.string.healthcare_risk_factor_assessment),
        description = stringResource(R.string.healthcare_risk_assessment_description)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Abuse Risk Factors
            RiskFactorChecklistSection(
                title = stringResource(R.string.healthcare_abuse_risk_factors),
                factors = getAbuseRiskFactors(),
                selectedFactors = assessment.abuseRiskFactors,
                onFactorToggle = { factor, selected ->
                    onAssessmentChange("toggleAbuseRiskFactor", factor to selected)
                }
            )

            // Neglect Risk Factors
            RiskFactorChecklistSection(
                title = stringResource(R.string.healthcare_neglect_risk_factors),
                factors = getNeglectRiskFactors(),
                selectedFactors = assessment.neglectRiskFactors,
                onFactorToggle = { factor, selected ->
                    onAssessmentChange("toggleNeglectRiskFactor", factor to selected)
                }
            )

            // Exploitation Risk Factors
            RiskFactorChecklistSection(
                title = stringResource(R.string.healthcare_exploitation_risk_factors),
                factors = getExploitationRiskFactors(),
                selectedFactors = assessment.exploitationRiskFactors,
                onFactorToggle = { factor, selected ->
                    onAssessmentChange("toggleExploitationRiskFactor", factor to selected)
                }
            )

            // Overall Risk Level
            OverallRiskLevelSelector(
                selectedLevel = assessment.overallRiskLevel,
                onLevelSelected = { onAssessmentChange("overallRiskLevel", it) }
            )

            // Protective Factors
            ProtectiveFactorsSection(
                factors = assessment.protectiveFactors,
                onFactorAdd = { factor ->
                    onAssessmentChange("addProtectiveFactor", factor)
                },
                onFactorRemove = { factor ->
                    onAssessmentChange("removeProtectiveFactor", factor)
                }
            )
        }
    }
}

@Composable
private fun CaregiverAssessmentSection(
    assessment: CaregiverAssessmentUiState,
    onAssessmentChange: (String, Any) -> Unit
) {
    AssessmentSectionCard(
        title = stringResource(R.string.healthcare_caregiver_assessment),
        description = stringResource(R.string.healthcare_caregiver_assessment_description)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Caregiver Type
            CaregiverTypeSelector(
                selectedType = assessment.caregiverType,
                onTypeSelected = { onAssessmentChange("caregiverType", it) }
            )

            // Caregiver Capacity
            CaregiverCapacitySelector(
                selectedCapacity = assessment.caregiverCapacity,
                onCapacitySelected = { onAssessmentChange("caregiverCapacity", it) }
            )

            // Caregiver Stress Level
            CaregiverStressLevelSelector(
                selectedLevel = assessment.caregiverStress,
                onLevelSelected = { onAssessmentChange("caregiverStress", it) }
            )

            // Caregiver Knowledge Level
            CaregiverKnowledgeLevelSelector(
                selectedLevel = assessment.caregiverKnowledge,
                onLevelSelected = { onAssessmentChange("caregiverKnowledge", it) }
            )

            // Caregiver Support Level
            CaregiverSupportLevelSelector(
                selectedLevel = assessment.caregiverSupport,
                onLevelSelected = { onAssessmentChange("caregiverSupport", it) }
            )
        }
    }
}

@Composable
private fun ClinicalNotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    AssessmentSectionCard(
        title = stringResource(R.string.healthcare_clinical_notes),
        description = stringResource(R.string.healthcare_clinical_notes_description)
    ) {
        AccessibleOutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = stringResource(R.string.healthcare_additional_observations),
            placeholder = stringResource(R.string.healthcare_clinical_notes_hint),
            maxLines = 6,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RiskSummarySection(
    overallRisk: OverallRiskLevel?,
    abuseRisk: AbuseRiskLevel?,
    recommendations: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (overallRisk) {
                OverallRiskLevel.LOW -> MaterialTheme.colorScheme.primaryContainer
                OverallRiskLevel.MODERATE -> MaterialTheme.colorScheme.secondaryContainer
                OverallRiskLevel.HIGH, OverallRiskLevel.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.healthcare_risk_summary),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            overallRisk?.let { risk ->
                RiskLevelDisplay(
                    label = stringResource(R.string.healthcare_overall_risk),
                    level = risk.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    color = getRiskLevelColor(risk)
                )
            }

            abuseRisk?.let { risk ->
                RiskLevelDisplay(
                    label = stringResource(R.string.healthcare_abuse_risk),
                    level = risk.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    color = getAbuseRiskLevelColor(risk)
                )
            }

            if (recommendations.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.healthcare_recommendations),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                recommendations.forEach { recommendation ->
                    Text(
                        text = "• $recommendation",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AssessmentActionButtons(
    uiState: ClinicalAssessmentUiState,
    onSaveAssessment: () -> Unit,
    onCompleteAssessment: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                onClick = onSaveAssessment,
                text = stringResource(R.string.healthcare_save_draft),
                style = ButtonStyle.OUTLINED,
                enabled = !uiState.isLoading && uiState.hasChanges,
                modifier = Modifier.weight(1f)
            )
        }

        AccessibleButton(
            onClick = onCompleteAssessment,
            text = stringResource(R.string.healthcare_complete_assessment),
            enabled = !uiState.isLoading && uiState.isAssessmentComplete,
            isLoading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Helper Composables and Functions

@Composable
private fun AssessmentSectionCard(
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

private fun getCurrentDate(): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date())
}

private fun getAbuseRiskFactors(): List<String> {
    return listOf(
        "Social isolation",
        "Cognitive impairment",
        "Physical dependency",
        "Financial vulnerability",
        "History of domestic violence",
        "Caregiver stress",
        "Substance abuse in household",
        "Mental health issues in caregiver"
    )
}

private fun getNeglectRiskFactors(): List<String> {
    return listOf(
        "Inadequate supervision",
        "Poor hygiene",
        "Medication non-compliance",
        "Nutritional deficits",
        "Unsafe living conditions",
        "Lack of medical care",
        "Caregiver overwhelm",
        "Limited resources"
    )
}

private fun getExploitationRiskFactors(): List<String> {
    return listOf(
        "Financial management difficulties",
        "Recent changes in financial arrangements",
        "Unusual banking activity",
        "New relationships with financial access",
        "Cognitive decline affecting judgment",
        "Social isolation from family",
        "Dependency on others for daily needs",
        "Previous exploitation incidents"
    )
}

// Additional selector composables would be implemented here...
// These would include dropdowns and radio button groups for various assessment categories
