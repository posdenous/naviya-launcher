package com.naviya.launcher.emergency

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.naviya.launcher.R
import com.naviya.launcher.emergency.data.EmergencyContact
import com.naviya.launcher.emergency.data.EmergencyDao
import com.naviya.launcher.emergency.ui.SOSButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Test Activity for Emergency SOS System
 * Allows manual testing of all emergency features
 * Use this to validate functionality before production
 */
@AndroidEntryPoint
class TestEmergencyActivity : AppCompatActivity() {
    
    @Inject
    lateinit var emergencyService: EmergencyService
    
    @Inject
    lateinit var emergencyDao: EmergencyDao
    
    @Inject
    lateinit var caregiverService: CaregiverNotificationService
    
    @Inject
    lateinit var locationService: EmergencyLocationService
    
    private lateinit var sosButton: SOSButton
    private lateinit var statusText: TextView
    private lateinit var testResultsText: TextView
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            statusText.text = "✅ All permissions granted"
            runTests()
        } else {
            statusText.text = "❌ Permissions required for testing"
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_emergency)
        
        setupViews()
        requestPermissions()
    }
    
    private fun setupViews() {
        sosButton = findViewById(R.id.test_sos_button)
        statusText = findViewById(R.id.status_text)
        testResultsText = findViewById(R.id.test_results_text)
        
        // Test buttons
        findViewById<Button>(R.id.btn_test_database).setOnClickListener {
            testDatabase()
        }
        
        findViewById<Button>(R.id.btn_test_location).setOnClickListener {
            testLocation()
        }
        
        findViewById<Button>(R.id.btn_test_notifications).setOnClickListener {
            testNotifications()
        }
        
        findViewById<Button>(R.id.btn_test_sos_flow).setOnClickListener {
            testSOSFlow()
        }
        
        findViewById<Button>(R.id.btn_test_accessibility).setOnClickListener {
            testAccessibility()
        }
        
        findViewById<Button>(R.id.btn_setup_test_data).setOnClickListener {
            setupTestData()
        }
        
        findViewById<Button>(R.id.btn_clear_test_data).setOnClickListener {
            clearTestData()
        }
        
        // Configure SOS button for testing
        sosButton.setConfirmationRequired(true)
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE
        )
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            statusText.text = "✅ All permissions granted"
            runTests()
        }
    }
    
    private fun runTests() {
        lifecycleScope.launch {
            val results = StringBuilder()
            results.append("🧪 EMERGENCY SYSTEM TEST RESULTS\n\n")
            
            // Test 1: Database connectivity
            results.append("1. Database Test: ")
            try {
                val contacts = emergencyDao.getAllEmergencyContacts()
                results.append("✅ PASS\n")
            } catch (e: Exception) {
                results.append("❌ FAIL - ${e.message}\n")
            }
            
            // Test 2: Location service
            results.append("2. Location Test: ")
            try {
                val location = locationService.getCurrentLocation()
                if (location != null) {
                    results.append("✅ PASS - Location: ${location.latitude}, ${location.longitude}\n")
                } else {
                    results.append("⚠️ PARTIAL - No location available\n")
                }
            } catch (e: Exception) {
                results.append("❌ FAIL - ${e.message}\n")
            }
            
            // Test 3: Emergency service initialization
            results.append("3. Emergency Service: ")
            try {
                val status = emergencyService.getSOSStatus()
                results.append("✅ PASS - SOS Active: ${status.isActive}\n")
            } catch (e: Exception) {
                results.append("❌ FAIL - ${e.message}\n")
            }
            
            // Test 4: Caregiver notification readiness
            results.append("4. Caregiver Notifications: ")
            try {
                val isReady = caregiverService.isCaregiverNotificationReady()
                if (isReady) {
                    results.append("✅ PASS - Ready\n")
                } else {
                    results.append("⚠️ PARTIAL - No caregiver configured\n")
                }
            } catch (e: Exception) {
                results.append("❌ FAIL - ${e.message}\n")
            }
            
            // Test 5: SOS Button accessibility
            results.append("5. SOS Button Accessibility: ")
            if (sosButton.minimumWidth >= (48 * resources.displayMetrics.density).toInt() &&
                sosButton.contentDescription?.isNotEmpty() == true) {
                results.append("✅ PASS\n")
            } else {
                results.append("❌ FAIL - Accessibility requirements not met\n")
            }
            
            results.append("\n📋 Test completed at ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}")
            
            testResultsText.text = results.toString()
        }
    }
    
    private fun testDatabase() {
        lifecycleScope.launch {
            try {
                statusText.text = "Testing database operations..."
                
                // Test inserting emergency event
                val testEvent = com.naviya.launcher.emergency.data.EmergencyEvent(
                    eventType = com.naviya.launcher.emergency.data.EmergencyEventType.SOS_ACTIVATED,
                    userLanguage = "en",
                    notes = "Test event from TestEmergencyActivity"
                )
                
                emergencyDao.insertEmergencyEvent(testEvent)
                
                // Test querying events
                val events = emergencyDao.getRecentEmergencyEvents(10)
                
                statusText.text = "✅ Database test passed - ${events.size} events found"
                Toast.makeText(this@TestEmergencyActivity, "Database test successful", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                statusText.text = "❌ Database test failed: ${e.message}"
                Toast.makeText(this@TestEmergencyActivity, "Database test failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun testLocation() {
        lifecycleScope.launch {
            try {
                statusText.text = "Testing location services..."
                
                val location = locationService.getCurrentLocation()
                if (location != null) {
                    statusText.text = "✅ Location: ${location.latitude}, ${location.longitude} (±${location.accuracy}m)"
                    Toast.makeText(this@TestEmergencyActivity, "Location test successful", Toast.LENGTH_SHORT).show()
                } else {
                    statusText.text = "⚠️ Location not available (check permissions/GPS)"
                    Toast.makeText(this@TestEmergencyActivity, "Location not available", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                statusText.text = "❌ Location test failed: ${e.message}"
                Toast.makeText(this@TestEmergencyActivity, "Location test failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun testNotifications() {
        lifecycleScope.launch {
            try {
                statusText.text = "Testing caregiver notifications..."
                
                val caregiver = emergencyDao.getPrimaryCaregiver()
                if (caregiver != null) {
                    val location = locationService.getCurrentLocation()
                    val result = caregiverService.sendEmergencyNotification(
                        caregiver = caregiver,
                        location = location,
                        userLanguage = "en"
                    )
                    
                    if (result) {
                        statusText.text = "✅ Notification sent to ${caregiver.name}"
                        Toast.makeText(this@TestEmergencyActivity, "Notification test successful", Toast.LENGTH_SHORT).show()
                    } else {
                        statusText.text = "❌ Notification failed"
                        Toast.makeText(this@TestEmergencyActivity, "Notification test failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    statusText.text = "⚠️ No caregiver configured for testing"
                    Toast.makeText(this@TestEmergencyActivity, "Setup test data first", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                statusText.text = "❌ Notification test failed: ${e.message}"
                Toast.makeText(this@TestEmergencyActivity, "Notification test failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun testSOSFlow() {
        lifecycleScope.launch {
            try {
                statusText.text = "Testing complete SOS flow..."
                
                val result = emergencyService.activateSOS(
                    userLanguage = "en",
                    triggeredBy = SOSTrigger.MANUAL
                )
                
                when (result) {
                    is EmergencyResult.Success -> {
                        statusText.text = "✅ SOS flow completed successfully"
                        Toast.makeText(this@TestEmergencyActivity, "SOS test successful", Toast.LENGTH_SHORT).show()
                        
                        // Cancel after 5 seconds for testing
                        sosButton.postDelayed({
                            lifecycleScope.launch {
                                emergencyService.cancelSOS("Test cancellation")
                                statusText.text = "✅ SOS cancelled (test mode)"
                            }
                        }, 5000)
                    }
                    is EmergencyResult.Error -> {
                        statusText.text = "❌ SOS flow failed: ${result.message}"
                        Toast.makeText(this@TestEmergencyActivity, "SOS test failed", Toast.LENGTH_SHORT).show()
                    }
                    is EmergencyResult.NoContactsConfigured -> {
                        statusText.text = "⚠️ No emergency contacts configured"
                        Toast.makeText(this@TestEmergencyActivity, "Setup test data first", Toast.LENGTH_SHORT).show()
                    }
                }
                
            } catch (e: Exception) {
                statusText.text = "❌ SOS flow test failed: ${e.message}"
                Toast.makeText(this@TestEmergencyActivity, "SOS test failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun testAccessibility() {
        val results = StringBuilder()
        results.append("🔍 ACCESSIBILITY TEST RESULTS:\n\n")
        
        // Test touch target size
        val minSize = (48 * resources.displayMetrics.density).toInt()
        results.append("Touch Target Size: ")
        if (sosButton.minimumWidth >= minSize && sosButton.minimumHeight >= minSize) {
            results.append("✅ PASS (${sosButton.minimumWidth}x${sosButton.minimumHeight}px)\n")
        } else {
            results.append("❌ FAIL - Too small\n")
        }
        
        // Test content description
        results.append("Content Description: ")
        if (sosButton.contentDescription?.isNotEmpty() == true) {
            results.append("✅ PASS\n")
        } else {
            results.append("❌ FAIL - Missing\n")
        }
        
        // Test focusability
        results.append("Focusability: ")
        if (sosButton.isFocusable && sosButton.isFocusableInTouchMode) {
            results.append("✅ PASS\n")
        } else {
            results.append("❌ FAIL\n")
        }
        
        // Test text size
        results.append("Text Size: ")
        if (sosButton.textSize >= 16f) {
            results.append("✅ PASS (${sosButton.textSize}sp)\n")
        } else {
            results.append("❌ FAIL - Too small\n")
        }
        
        testResultsText.text = results.toString()
        Toast.makeText(this, "Accessibility test completed", Toast.LENGTH_SHORT).show()
    }
    
    private fun setupTestData() {
        lifecycleScope.launch {
            try {
                statusText.text = "Setting up test data..."
                
                // Create test emergency contact
                val testEmergencyContact = EmergencyContact(
                    id = "test-emergency-112",
                    name = "Test Emergency Service",
                    phoneNumber = "112",
                    isEmergencyService = true,
                    countryCode = "DE",
                    isPrimary = true
                )
                
                // Create test caregiver
                val testCaregiver = EmergencyContact(
                    id = "test-caregiver-1",
                    name = "Test Caregiver",
                    phoneNumber = "+49123456789", // Use a test number
                    isEmergencyService = false,
                    countryCode = "DE",
                    isPrimary = true
                )
                
                emergencyDao.insertEmergencyContact(testEmergencyContact)
                emergencyDao.insertEmergencyContact(testCaregiver)
                
                statusText.text = "✅ Test data created successfully"
                Toast.makeText(this@TestEmergencyActivity, "Test data setup complete", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                statusText.text = "❌ Failed to setup test data: ${e.message}"
                Toast.makeText(this@TestEmergencyActivity, "Test data setup failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun clearTestData() {
        lifecycleScope.launch {
            try {
                statusText.text = "Clearing test data..."
                
                // Clear test contacts
                emergencyDao.deleteEmergencyContact("test-emergency-112")
                emergencyDao.deleteEmergencyContact("test-caregiver-1")
                
                statusText.text = "✅ Test data cleared"
                Toast.makeText(this@TestEmergencyActivity, "Test data cleared", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                statusText.text = "❌ Failed to clear test data: ${e.message}"
                Toast.makeText(this@TestEmergencyActivity, "Clear test data failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
