package com.naviya.launcher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.naviya.launcher.R

/**
 * Accessible UI Components for Naviya Healthcare Professional Interface
 * Follows elderly-first accessibility principles:
 * - 1.6x font scaling minimum
 * - 48dp minimum touch targets
 * - High contrast colors
 * - TTS-compatible descriptions
 * - Haptic feedback for interactions
 */

enum class ButtonStyle {
    FILLED,
    OUTLINED,
    TEXT
}

/**
 * Accessible button with elderly-friendly design
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    style: ButtonStyle = ButtonStyle.FILLED,
    contentDescription: String? = null
) {
    val haptic = LocalHapticFeedback.current
    
    val buttonModifier = modifier
        .heightIn(min = 48.dp) // Minimum touch target
        .semantics {
            this.contentDescription = contentDescription ?: text
        }

    when (style) {
        ButtonStyle.FILLED -> {
            Button(
                onClick = {
                    if (!isLoading) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                },
                enabled = enabled && !isLoading,
                modifier = buttonModifier,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                ButtonContent(text = text, isLoading = isLoading)
            }
        }
        ButtonStyle.OUTLINED -> {
            OutlinedButton(
                onClick = {
                    if (!isLoading) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                },
                enabled = enabled && !isLoading,
                modifier = buttonModifier,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                ButtonContent(text = text, isLoading = isLoading)
            }
        }
        ButtonStyle.TEXT -> {
            TextButton(
                onClick = {
                    if (!isLoading) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                },
                enabled = enabled && !isLoading,
                modifier = buttonModifier,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                ButtonContent(text = text, isLoading = isLoading)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 18.sp, // 1.6x scale for elderly users
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * Accessible icon button with haptic feedback
 */
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val haptic = LocalHapticFeedback.current
    
    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .size(48.dp) // Minimum touch target
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        // Icon implementation would depend on your icon system
        // This is a placeholder for the actual icon
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall,
            color = tint
        )
    }
}

/**
 * Accessible outlined text field with elderly-friendly design
 */
@Composable
fun AccessibleOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isRequired: Boolean = false,
    errorMessage: String? = null,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        )
                    )
                    if (isRequired) {
                        Text(
                            text = "*",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            placeholder = placeholder?.let { placeholderText ->
                {
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            isError = errorMessage != null,
            maxLines = maxLines,
            readOnly = readOnly,
            trailingIcon = trailingIcon,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp) // Minimum touch target height
                .semantics {
                    contentDescription = buildString {
                        append(label)
                        if (isRequired) append(", required")
                        if (errorMessage != null) append(", error: $errorMessage")
                        if (placeholder != null) append(", placeholder: $placeholder")
                    }
                },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        // Error message
        errorMessage?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

/**
 * Loading indicator with accessible message
 */
@Composable
fun LoadingIndicator(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Loading: $message"
                    }
            )
        }
    }
}

/**
 * Error message with dismiss action
 */
@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Error: $message"
                    }
            )

            AccessibleIconButton(
                onClick = onDismiss,
                contentDescription = stringResource(R.string.dismiss_error),
                icon = "✕",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * Success message with completion action
 */
@Composable
fun SuccessMessage(
    message: String,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "Success: $message"
                        }
                )
            }

            AccessibleButton(
                onClick = onComplete,
                text = stringResource(R.string.continue_button),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Risk level display with color coding
 */
@Composable
fun RiskLevelDisplay(
    label: String,
    level: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.2f)
            )
        ) {
            Text(
                text = level,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * Risk factor checklist section
 */
@Composable
fun RiskFactorChecklistSection(
    title: String,
    factors: List<String>,
    selectedFactors: List<String>,
    onFactorToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        factors.forEach { factor ->
            val isSelected = selectedFactors.contains(factor)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "$factor, ${if (isSelected) "selected" else "not selected"}"
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                onClick = { onFactorToggle(factor, !isSelected) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onFactorToggle(factor, it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(
                        text = factor,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Community resources section with add/remove functionality
 */
@Composable
fun CommunityResourcesSection(
    resources: List<String>,
    onResourceAdd: (String) -> Unit,
    onResourceRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newResource by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.healthcare_community_resources),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccessibleOutlinedTextField(
                value = newResource,
                onValueChange = { newResource = it },
                label = stringResource(R.string.healthcare_add_resource),
                placeholder = stringResource(R.string.healthcare_resource_hint),
                modifier = Modifier.weight(1f)
            )

            AccessibleButton(
                onClick = {
                    if (newResource.isNotBlank()) {
                        onResourceAdd(newResource.trim())
                        newResource = ""
                    }
                },
                text = stringResource(R.string.add),
                enabled = newResource.isNotBlank()
            )
        }

        resources.forEach { resource ->
            ResourceChip(
                resource = resource,
                onRemove = { onResourceRemove(resource) }
            )
        }
    }
}

@Composable
private fun ResourceChip(
    resource: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                text = resource,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )

            AccessibleIconButton(
                onClick = onRemove,
                contentDescription = stringResource(R.string.healthcare_remove_resource, resource),
                icon = "✕",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * Protective factors section
 */
@Composable
fun ProtectiveFactorsSection(
    factors: List<String>,
    onFactorAdd: (String) -> Unit,
    onFactorRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newFactor by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.healthcare_protective_factors),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccessibleOutlinedTextField(
                value = newFactor,
                onValueChange = { newFactor = it },
                label = stringResource(R.string.healthcare_add_protective_factor),
                placeholder = stringResource(R.string.healthcare_protective_factor_hint),
                modifier = Modifier.weight(1f)
            )

            AccessibleButton(
                onClick = {
                    if (newFactor.isNotBlank()) {
                        onFactorAdd(newFactor.trim())
                        newFactor = ""
                    }
                },
                text = stringResource(R.string.add),
                enabled = newFactor.isNotBlank()
            )
        }

        factors.forEach { factor ->
            ProtectiveFactorChip(
                factor = factor,
                onRemove = { onFactorRemove(factor) }
            )
        }
    }
}

@Composable
private fun ProtectiveFactorChip(
    factor: String,
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
                text = factor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            AccessibleIconButton(
                onClick = onRemove,
                contentDescription = stringResource(R.string.healthcare_remove_protective_factor, factor),
                icon = "✕",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// Helper functions for risk level colors
@Composable
fun getRiskLevelColor(riskLevel: OverallRiskLevel): Color {
    return when (riskLevel) {
        OverallRiskLevel.LOW -> MaterialTheme.colorScheme.primary
        OverallRiskLevel.MODERATE -> MaterialTheme.colorScheme.secondary
        OverallRiskLevel.HIGH -> MaterialTheme.colorScheme.error
        OverallRiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
    }
}

@Composable
fun getAbuseRiskLevelColor(riskLevel: AbuseRiskLevel): Color {
    return when (riskLevel) {
        AbuseRiskLevel.MINIMAL, AbuseRiskLevel.LOW -> MaterialTheme.colorScheme.primary
        AbuseRiskLevel.MODERATE -> MaterialTheme.colorScheme.secondary
        AbuseRiskLevel.HIGH, AbuseRiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
    }
}
