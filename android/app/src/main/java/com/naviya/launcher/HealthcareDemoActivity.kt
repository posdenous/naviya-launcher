package com.naviya.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.naviya.launcher.navigation.HealthcareNavigation
import com.naviya.launcher.ui.theme.NaviyaTheme

/**
 * Demo Activity for Healthcare Professional Workflows
 * 
 * This activity demonstrates the complete healthcare professional integration system:
 * - Professional Registration with credentials and validation
 * - Professional Installation with consent management and clinical context
 * - Clinical Assessment with comprehensive risk evaluation
 * 
 * Features demonstrated:
 * ✅ Elderly-first accessibility (1.6x font scaling, 48dp touch targets)
 * ✅ Offline-first architecture with local data persistence
 * ✅ Real-time form validation with helpful error messages
 * ✅ Multi-step guided workflows with progress tracking
 * ✅ Risk assessment with automatic elder rights advocate notifications
 * ✅ Integration with abuse detection and emergency alert systems
 * ✅ GDPR-compliant consent management
 * ✅ Professional credential verification and authorization
 * 
 * To run this demo:
 * 1. Launch the app and you'll see the Healthcare Demo Home screen
 * 2. Try each workflow:
 *    - Professional Registration: Complete form with validation
 *    - Professional Installation: 6-step guided installation process
 *    - Clinical Assessment: Comprehensive patient evaluation
 * 3. Watch the console output for integration system notifications
 * 
 * Note: This is a demo version with mock services for testing purposes.
 * In production, this would connect to real healthcare systems and databases.
 */
@AndroidEntryPoint
class HealthcareDemoActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log demo startup
        println("🏥 Starting Healthcare Professional Demo")
        println("   Features: Registration, Installation, Clinical Assessment")
        println("   Accessibility: Elderly-first design with 1.6x font scaling")
        println("   Integration: Abuse detection, Elder rights advocates")
        println("   Architecture: Offline-first with Room database")
        
        setContent {
            NaviyaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HealthcareNavigation()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        println("🏥 Healthcare Professional Demo ended")
    }
}

/**
 * Alternative standalone demo function for testing without Android Activity
 * Useful for Compose preview and unit testing
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun HealthcareDemoPreview() {
    NaviyaTheme {
        HealthcareNavigation()
    }
}

/**
 * Demo Application class with Hilt setup
 */
@dagger.hilt.android.HiltAndroidApp
class HealthcareDemoApplication : android.app.Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        println("🚀 Initializing Naviya Healthcare Demo Application")
        println("   Database: Room with healthcare professional tables")
        println("   DI: Hilt with healthcare services and repositories")
        println("   UI: Jetpack Compose with accessibility compliance")
        
        // Initialize demo data if needed
        initializeDemoData()
    }
    
    private fun initializeDemoData() {
        // In a real app, this would seed the database with demo data
        println("📊 Demo data initialized")
        println("   Mock professional: Dr. Jane Smith (Geriatrician)")
        println("   Mock patient: John Doe (75 years old)")
        println("   Mock institution: General Hospital")
    }
}
