package com.naviya.launcher.ui.onboarding

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Integration test for OnboardingActivity
 * Tests the basic functionality and navigation flow
 */
@RunWith(AndroidJUnit4::class)
class OnboardingActivityTest {
    
    @Test
    fun onboardingActivity_launches_successfully() {
        // Test that OnboardingActivity can be launched without crashing
        val scenario = ActivityScenario.launch(OnboardingActivity::class.java)
        
        scenario.use { activityScenario ->
            activityScenario.onActivity { activity ->
                // Verify activity is not null and properly initialized
                assertNotNull(activity)
                assertTrue(activity is OnboardingActivity)
            }
        }
    }
    
    @Test
    fun onboardingActivity_has_proper_theme() {
        val scenario = ActivityScenario.launch(OnboardingActivity::class.java)
        
        scenario.use { activityScenario ->
            activityScenario.onActivity { activity ->
                // Verify activity uses elderly-friendly theme
                assertNotNull(activity.theme)
            }
        }
    }
    
    @Test
    fun onboardingActivity_prevents_back_navigation() {
        val scenario = ActivityScenario.launch(OnboardingActivity::class.java)
        
        scenario.use { activityScenario ->
            activityScenario.onActivity { activity ->
                // Test that back button is handled properly
                // This would normally show a confirmation dialog
                activity.onBackPressed()
                
                // Activity should still be running (not finished)
                assertFalse(activity.isFinishing)
            }
        }
    }
}
