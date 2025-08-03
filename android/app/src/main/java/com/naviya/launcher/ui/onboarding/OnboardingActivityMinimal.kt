package com.naviya.launcher.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.naviya.launcher.MainActivitySimplified
import com.naviya.launcher.ui.theme.NaviyaLauncherTheme

/**
 * Minimal OnboardingActivity for testing integration
 * This bypasses complex dependencies and provides a simple onboarding flow
 */
class OnboardingActivityMinimal : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent back navigation during onboarding
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Show message that onboarding must be completed
                Toast.makeText(
                    this@OnboardingActivityMinimal,
                    "Please complete the onboarding setup to continue.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        
        setContent {
            NaviyaLauncherTheme {
                MinimalOnboardingScreen(
                    onOnboardingComplete = { handleOnboardingComplete() }
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
                    this@OnboardingActivityMinimal,
                    "Setup complete! Welcome to your new launcher.",
                    Toast.LENGTH_LONG
                ).show()
                
                // Navigate to main launcher
                val intent = Intent(this@OnboardingActivityMinimal, MainActivitySimplified::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@OnboardingActivityMinimal,
                    "Failed to complete onboarding: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
fun MinimalOnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 3
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Progress indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / totalSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                )
                
                Text(
                    text = "Step ${currentStep + 1} of $totalSteps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Content area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                when (currentStep) {
                    0 -> WelcomeStep()
                    1 -> SetupStep()
                    2 -> CompletionStep()
                }
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    Button(
                        onClick = { currentStep-- },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }
                
                Button(
                    onClick = {
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(if (currentStep < totalSteps - 1) "Next" else "Complete Setup")
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "🎉",
            fontSize = 64.sp
        )
        
        Text(
            text = "Welcome to Naviya Launcher",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "A simple, elderly-friendly launcher designed for easy use and safety.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SetupStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "⚙️",
            fontSize = 64.sp
        )
        
        Text(
            text = "Quick Setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "We're configuring your launcher with elderly-friendly settings including large text, simple navigation, and emergency features.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompletionStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "✅",
            fontSize = 64.sp
        )
        
        Text(
            text = "All Set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your launcher is ready to use. You'll have access to essential apps, emergency features, and a simplified interface designed for your needs.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
