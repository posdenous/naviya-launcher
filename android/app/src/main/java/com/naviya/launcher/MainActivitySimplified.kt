package com.naviya.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.naviya.launcher.ui.onboarding.OnboardingActivityMinimal
import com.naviya.launcher.ui.theme.NaviyaLauncherTheme

/**
 * Simplified MainActivity for onboarding integration testing
 * This version removes problematic dependencies to focus on core onboarding flow
 */
class MainActivitySimplified : ComponentActivity() {
    
    companion object {
        private const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val PREFS_NAME = "naviya_launcher_prefs"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if onboarding is completed
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isOnboardingCompleted = prefs.getBoolean(PREF_ONBOARDING_COMPLETED, false)
        
        if (!isOnboardingCompleted) {
            // Launch onboarding activity
            val intent = Intent(this, OnboardingActivityMinimal::class.java)
            startActivity(intent)
            finish() // Close MainActivity so user can't go back
            return
        }
        
        // Show main launcher UI
        setContent {
            NaviyaLauncherTheme {
                MainLauncherPlaceholder()
            }
        }
    }
}

@Composable
fun MainLauncherPlaceholder() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉 Welcome to Naviya Launcher!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Onboarding completed successfully.\nMain launcher UI will be implemented here.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Button(
                onClick = { 
                    // TODO: Implement settings or advanced features
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Settings")
            }
            
            Button(
                onClick = { 
                    // TODO: Implement emergency features
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Emergency")
            }
        }
    }
}
