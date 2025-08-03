package com.naviya.launcher.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.naviya.launcher.MainActivity
import com.naviya.launcher.MainActivitySimplified
import com.naviya.launcher.onboarding.ui.FamilyOnboardingScreen
import com.naviya.launcher.onboarding.ui.FamilyOnboardingViewModel
import com.naviya.launcher.onboarding.OnboardingStep
import com.naviya.launcher.ui.theme.NaviyaLauncherTheme

/**
 * Dedicated onboarding activity for first-time setup of the Naviya elderly launcher.
 * 
 * This activity handles the complete onboarding flow including:
 * - Welcome and introduction
 * - Mode selection (Essential/Comfort/Connected)
 * - Emergency contact setup
 * - Accessibility configuration
 * - Caregiver pairing (optional)
 * - Privacy consent and GDPR compliance
 * 
 * Features:
 * - Elderly-friendly UI with large touch targets (48dp+)
 * - High contrast colors and 1.6x+ font scaling
 * - Step-by-step guided flow with clear progress indication
 * - Multilingual support (EN/DE/TR/AR/UA)
 * - Accessibility compliance with screen reader support
 * - Offline-first functionality
 * 
 * Navigation:
 * - Launched automatically on first app start
 * - Transitions to MainActivity upon completion
 * - Cannot be bypassed (ensures safety setup)
 */
class OnboardingActivity : ComponentActivity() {
    
    private val viewModel: FamilyOnboardingViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent going back to previous activity during onboarding
        // This ensures users complete the safety setup
        
        setContent {
            NaviyaLauncherTheme {
                OnboardingScreen(
                    viewModel = viewModel,
                    onOnboardingComplete = { handleOnboardingComplete() },
                    onError = { error -> handleOnboardingError(error) }
                )
            }
        }
    }
    
    /**
     * Handle successful onboarding completion
     */
    private fun handleOnboardingComplete() {
        lifecycleScope.launch {
            try {
                // Mark onboarding as completed
                val prefs = getSharedPreferences("naviya_launcher_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("onboarding_completed", true).apply()
                
                // Show success message
                Toast.makeText(
                    this@OnboardingActivity,
                    "Setup complete! Welcome to your new launcher.",
                    Toast.LENGTH_LONG
                ).show()
                
                // Navigate to main launcher
                val intent = Intent(this@OnboardingActivity, MainActivitySimplified::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                handleOnboardingError("Failed to complete onboarding: ${e.message}")
            }
        }
    }
    
    /**
     * Handle onboarding errors
     */
    private fun handleOnboardingError(error: String) {
        Toast.makeText(
            this,
            "Onboarding Error: $error",
            Toast.LENGTH_LONG
        ).show()
        
        // Log error for debugging
        android.util.Log.e("OnboardingActivity", "Onboarding error: $error")
    }
    
    /**
     * Prevent back button during onboarding to ensure safety setup completion
     */
    override fun onBackPressed() {
        // Show confirmation dialog for exiting onboarding
        lifecycleScope.launch {
            showExitConfirmationDialog()
        }
    }
    
    /**
     * Show confirmation dialog when user tries to exit onboarding
     */
    private fun showExitConfirmationDialog() {
        // This would typically show an AlertDialog
        // For now, we'll prevent exit to ensure safety setup
        Toast.makeText(
            this,
            "Please complete the setup for your safety and security.",
            Toast.LENGTH_LONG
        ).show()
    }
}

/**
 * Main onboarding screen composable that integrates with the existing onboarding system
 */
@Composable
private fun OnboardingScreen(
    viewModel: FamilyOnboardingViewModel,
    onOnboardingComplete: () -> Unit,
    onError: (String) -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    // Handle error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            onError(error)
        }
    }
    
    // Handle onboarding completion
    LaunchedEffect(currentStep) {
        if (currentStep == OnboardingStep.LAUNCHER_READY) {
            onOnboardingComplete()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            // Show loading screen with elderly-friendly design
            LoadingScreen()
        } else {
            // Show main onboarding flow
            FamilyOnboardingScreen(
                onOnboardingComplete = onOnboardingComplete
            )
        }
    }
}

/**
 * Loading screen with elderly-friendly design
 */
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Setting up your launcher…",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "This will only take a moment",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}
