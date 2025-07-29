package com.naviya.launcher.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.naviya.launcher.healthcare.ui.ProfessionalRegistrationScreen
import com.naviya.launcher.healthcare.ui.ProfessionalInstallationScreen
import com.naviya.launcher.healthcare.ui.ClinicalAssessmentScreen

/**
 * Navigation setup for Healthcare Professional workflows
 * Provides demo navigation between registration, installation, and assessment screens
 */

sealed class HealthcareScreen(val route: String) {
    object Home : HealthcareScreen("healthcare_home")
    object Registration : HealthcareScreen("professional_registration")
    object Installation : HealthcareScreen("professional_installation/{userId}/{professionalId}") {
        fun createRoute(userId: String, professionalId: String) = 
            "professional_installation/$userId/$professionalId"
    }
    object Assessment : HealthcareScreen("clinical_assessment/{userId}/{professionalId}") {
        fun createRoute(userId: String, professionalId: String) = 
            "clinical_assessment/$userId/$professionalId"
    }
}

@Composable
fun HealthcareNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = HealthcareScreen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Healthcare Demo Home Screen
        composable(HealthcareScreen.Home.route) {
            HealthcareDemoHomeScreen(
                onNavigateToRegistration = {
                    navController.navigate(HealthcareScreen.Registration.route)
                },
                onNavigateToInstallation = { userId, professionalId ->
                    navController.navigate(
                        HealthcareScreen.Installation.createRoute(userId, professionalId)
                    )
                },
                onNavigateToAssessment = { userId, professionalId ->
                    navController.navigate(
                        HealthcareScreen.Assessment.createRoute(userId, professionalId)
                    )
                }
            )
        }

        // Professional Registration Screen
        composable(HealthcareScreen.Registration.route) {
            ProfessionalRegistrationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistrationComplete = { registrationId ->
                    // Navigate to installation with demo data
                    navController.navigate(
                        HealthcareScreen.Installation.createRoute(
                            userId = "demo-user-123",
                            professionalId = "demo-prof-456"
                        )
                    )
                }
            )
        }

        // Professional Installation Screen
        composable(HealthcareScreen.Installation.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "demo-user-123"
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: "demo-prof-456"
            
            ProfessionalInstallationScreen(
                userId = userId,
                professionalId = professionalId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onInstallationComplete = { installationId ->
                    // Navigate to clinical assessment
                    navController.navigate(
                        HealthcareScreen.Assessment.createRoute(userId, professionalId)
                    )
                }
            )
        }

        // Clinical Assessment Screen
        composable(HealthcareScreen.Assessment.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "demo-user-123"
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: "demo-prof-456"
            
            ClinicalAssessmentScreen(
                userId = userId,
                professionalId = professionalId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAssessmentComplete = { assessmentId ->
                    // Return to home with success message
                    navController.navigate(HealthcareScreen.Home.route) {
                        popUpTo(HealthcareScreen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun HealthcareDemoHomeScreen(
    onNavigateToRegistration: () -> Unit,
    onNavigateToInstallation: (String, String) -> Unit,
    onNavigateToAssessment: (String, String) -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(24.dp)
    ) {
        // Header
        androidx.compose.material3.Text(
            text = "🏥 Healthcare Professional Demo",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        )

        androidx.compose.material3.Text(
            text = "Naviya Elder Protection System\nHealthcare Professional Workflows",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp
            ),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(32.dp))

        // Demo Buttons
        DemoWorkflowCard(
            title = "👨‍⚕️ Professional Registration",
            description = "Register a new healthcare professional with credentials, specializations, and institution affiliation.",
            buttonText = "Start Registration Demo",
            onClick = onNavigateToRegistration
        )

        DemoWorkflowCard(
            title = "🏠 Professional Installation",
            description = "Perform professional installation with consent management, clinical context, and safety protocols.",
            buttonText = "Start Installation Demo",
            onClick = { 
                onNavigateToInstallation("demo-user-123", "demo-prof-456") 
            }
        )

        DemoWorkflowCard(
            title = "📋 Clinical Assessment",
            description = "Comprehensive clinical assessment including cognitive, functional, social, and risk factor evaluation.",
            buttonText = "Start Assessment Demo",
            onClick = { 
                onNavigateToAssessment("demo-user-123", "demo-prof-456") 
            }
        )

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))

        // Footer
        androidx.compose.material3.Card(
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "🛡️ Elder Protection Features",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                )

                androidx.compose.material3.Text(
                    text = "• Abuse detection and elder rights advocate integration\n• Offline-first architecture with secure data storage\n• Accessibility-focused design for elderly users\n• GDPR compliance and medical device certification ready",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun DemoWorkflowCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )

            androidx.compose.material3.Text(
                text = description,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )

            androidx.compose.material3.Button(
                onClick = onClick,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 24.dp,
                    vertical = 16.dp
                )
            ) {
                androidx.compose.material3.Text(
                    text = buttonText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
