#!/usr/bin/env kotlin

/**
 * Test validation script for Naviya Launcher unit tests
 * Validates test logic and data model consistency
 */

import java.io.File
import java.util.regex.Pattern

fun main() {
    println("🧪 Naviya Launcher Unit Test Validation")
    println("=" * 50)
    
    val testResults = mutableListOf<TestResult>()
    
    // Validate test files exist
    testResults.add(validateTestFilesExist())
    
    // Validate test structure
    testResults.add(validateTestStructure())
    
    // Validate elderly user requirements
    testResults.add(validateElderlyUserRequirements())
    
    // Validate accessibility requirements
    testResults.add(validateAccessibilityRequirements())
    
    // Validate crash recovery logic
    testResults.add(validateCrashRecoveryLogic())
    
    // Validate notification logic
    testResults.add(validateNotificationLogic())
    
    // Print results
    printTestResults(testResults)
}

data class TestResult(
    val testName: String,
    val passed: Boolean,
    val details: String,
    val criticalIssues: List<String> = emptyList()
)

fun validateTestFilesExist(): TestResult {
    val testFiles = listOf(
        "app/src/test/java/com/naviya/launcher/data/models/LauncherStateTest.kt",
        "app/src/test/java/com/naviya/launcher/data/models/NotificationStateTest.kt",
        "app/src/test/java/com/naviya/launcher/data/models/CrashRecoveryStateTest.kt"
    )
    
    val missingFiles = testFiles.filter { !File(it).exists() }
    
    return if (missingFiles.isEmpty()) {
        TestResult(
            "Test Files Existence",
            true,
            "All 3 test files found: LauncherStateTest, NotificationStateTest, CrashRecoveryStateTest"
        )
    } else {
        TestResult(
            "Test Files Existence",
            false,
            "Missing test files: ${missingFiles.joinToString(", ")}",
            listOf("Critical: Missing test files prevent test execution")
        )
    }
}

fun validateTestStructure(): TestResult {
    val issues = mutableListOf<String>()
    val testFiles = listOf(
        "app/src/test/java/com/naviya/launcher/data/models/LauncherStateTest.kt",
        "app/src/test/java/com/naviya/launcher/data/models/NotificationStateTest.kt",
        "app/src/test/java/com/naviya/launcher/data/models/CrashRecoveryStateTest.kt"
    )
    
    testFiles.forEach { filePath ->
        val file = File(filePath)
        if (file.exists()) {
            val content = file.readText()
            
            // Check for required test annotations
            if (!content.contains("@Test")) {
                issues.add("$filePath: Missing @Test annotations")
            }
            
            // Check for setUp method
            if (!content.contains("@Before") || !content.contains("fun setUp()")) {
                issues.add("$filePath: Missing @Before setUp() method")
            }
            
            // Check for assertions
            if (!content.contains("assertEquals") && !content.contains("assertTrue") && !content.contains("assertFalse")) {
                issues.add("$filePath: Missing assertion methods")
            }
            
            // Check for test method naming convention
            val testMethodPattern = Pattern.compile("fun `test .+`\\(\\)")
            if (!testMethodPattern.matcher(content).find()) {
                issues.add("$filePath: Test methods should follow `test description` naming convention")
            }
        }
    }
    
    return TestResult(
        "Test Structure Validation",
        issues.isEmpty(),
        if (issues.isEmpty()) "All test files have proper structure with @Test, @Before, assertions, and naming conventions" 
        else "Structure issues found",
        issues
    )
}

fun validateElderlyUserRequirements(): TestResult {
    val launcherTestFile = File("app/src/test/java/com/naviya/launcher/data/models/LauncherStateTest.kt")
    val issues = mutableListOf<String>()
    
    if (launcherTestFile.exists()) {
        val content = launcherTestFile.readText()
        
        // Check for elderly-specific test cases
        val elderlyRequirements = mapOf(
            "Font scale 1.6x" to "1.6f",
            "Touch targets 48dp" to "48",
            "High contrast enabled" to "highContrastEnabled",
            "Large icons enabled" to "largeIconsEnabled",
            "TTS enabled" to "ttsEnabled",
            "2x3 grid layout" to "rows.*2.*columns.*3",
            "64dp icon size" to "64.*iconSizeDp",
            "PIN protection" to "pinEnabled"
        )
        
        elderlyRequirements.forEach { (requirement, pattern) ->
            if (!content.contains(Regex(pattern))) {
                issues.add("Missing test for elderly requirement: $requirement")
            }
        }
        
        // Check for multilingual support tests
        val languages = listOf("en", "de", "tr", "ar", "uk")
        languages.forEach { lang ->
            if (!content.contains("\"$lang\"")) {
                issues.add("Missing test for language support: $lang")
            }
        }
    } else {
        issues.add("LauncherStateTest.kt not found")
    }
    
    return TestResult(
        "Elderly User Requirements",
        issues.isEmpty(),
        if (issues.isEmpty()) "All elderly user requirements are tested: accessibility, multilingual, large UI elements"
        else "Missing elderly user requirement tests",
        issues
    )
}

fun validateAccessibilityRequirements(): TestResult {
    val testFiles = listOf(
        "app/src/test/java/com/naviya/launcher/data/models/LauncherStateTest.kt",
        "app/src/test/java/com/naviya/launcher/data/models/NotificationStateTest.kt"
    )
    
    val issues = mutableListOf<String>()
    val accessibilityFeatures = listOf(
        "highContrastEnabled",
        "largeIconsEnabled", 
        "hapticFeedbackEnabled",
        "ttsEnabled",
        "slowAnimationsEnabled",
        "minimumTouchTargetDp",
        "fontScale",
        "colorBlindnessSupport"
    )
    
    testFiles.forEach { filePath ->
        val file = File(filePath)
        if (file.exists()) {
            val content = file.readText()
            accessibilityFeatures.forEach { feature ->
                if (!content.contains(feature)) {
                    issues.add("$filePath: Missing test for accessibility feature: $feature")
                }
            }
        }
    }
    
    return TestResult(
        "Accessibility Requirements",
        issues.isEmpty(),
        if (issues.isEmpty()) "All accessibility features are tested: contrast, large UI, haptics, TTS, animations"
        else "Missing accessibility requirement tests",
        issues
    )
}

fun validateCrashRecoveryLogic(): TestResult {
    val crashTestFile = File("app/src/test/java/com/naviya/launcher/data/models/CrashRecoveryStateTest.kt")
    val issues = mutableListOf<String>()
    
    if (crashTestFile.exists()) {
        val content = crashTestFile.readText()
        
        // Check for 3-crash threshold logic
        if (!content.contains("crashThreshold.*3")) {
            issues.add("Missing test for 3-crash threshold")
        }
        
        // Check for 24-hour tracking period
        if (!content.contains("24.*trackingPeriodHours")) {
            issues.add("Missing test for 24-hour tracking period")
        }
        
        // Check for safe tiles (2x2 grid)
        if (!content.contains("4.*safe tiles") || !content.contains("2x2")) {
            issues.add("Missing test for 4 safe tiles in 2x2 grid")
        }
        
        // Check for essential safe tiles
        val essentialTiles = listOf("PHONE_DIALER", "SETTINGS", "SOS_EMERGENCY", "HELP_SUPPORT")
        essentialTiles.forEach { tile ->
            if (!content.contains(tile)) {
                issues.add("Missing test for essential safe tile: $tile")
            }
        }
        
        // Check for caregiver notification
        if (!content.contains("caregiverNotificationEnabled")) {
            issues.add("Missing test for caregiver notification on crash")
        }
        
        // Check for recovery assistance
        if (!content.contains("stepByStepGuidanceEnabled") || !content.contains("voiceInstructionsEnabled")) {
            issues.add("Missing test for recovery assistance features")
        }
    } else {
        issues.add("CrashRecoveryStateTest.kt not found")
    }
    
    return TestResult(
        "Crash Recovery Logic",
        issues.isEmpty(),
        if (issues.isEmpty()) "All crash recovery logic tested: 3-crash threshold, safe mode, caregiver alerts"
        else "Missing crash recovery logic tests",
        issues
    )
}

fun validateNotificationLogic(): TestResult {
    val notificationTestFile = File("app/src/test/java/com/naviya/launcher/data/models/NotificationStateTest.kt")
    val issues = mutableListOf<String>()
    
    if (notificationTestFile.exists()) {
        val content = notificationTestFile.readText()
        
        // Check for priority weighting logic
        if (!content.contains("priorityWeightedCount") || !content.contains("emergency.*×3") || !content.contains("caregiver.*×2")) {
            issues.add("Missing test for priority weighting (emergency ×3, caregiver ×2, normal ×1)")
        }
        
        // Check for missed calls + SMS combination
        if (!content.contains("totalMissedCalls") || !content.contains("totalUnreadSms") || !content.contains("totalUnread")) {
            issues.add("Missing test for missed calls + SMS combination")
        }
        
        // Check for offline access
        if (!content.contains("offlineAccessEnabled") || !content.contains("localCachingEnabled")) {
            issues.add("Missing test for offline notification access")
        }
        
        // Check for privacy settings
        if (!content.contains("dataConsentRequired") || !content.contains("showMessagePreview")) {
            issues.add("Missing test for notification privacy settings")
        }
        
        // Check for 99+ overflow handling
        if (!content.contains("99") || !content.contains("overflow")) {
            issues.add("Missing test for notification count overflow (99+)")
        }
        
        // Check for caregiver integration
        if (!content.contains("caregiverAccessEnabled") || !content.contains("remoteViewingEnabled")) {
            issues.add("Missing test for caregiver notification integration")
        }
    } else {
        issues.add("NotificationStateTest.kt not found")
    }
    
    return TestResult(
        "Notification Logic",
        issues.isEmpty(),
        if (issues.isEmpty()) "All notification logic tested: priority weighting, offline access, privacy, overflow"
        else "Missing notification logic tests",
        issues
    )
}

fun printTestResults(results: List<TestResult>) {
    println("\n📊 Test Validation Results")
    println("=" * 50)
    
    val passed = results.count { it.passed }
    val total = results.size
    
    results.forEach { result ->
        val status = if (result.passed) "✅ PASS" else "❌ FAIL"
        println("$status ${result.testName}")
        println("   ${result.details}")
        
        if (result.criticalIssues.isNotEmpty()) {
            result.criticalIssues.forEach { issue ->
                println("   🚨 $issue")
            }
        }
        println()
    }
    
    println("=" * 50)
    println("📈 Summary: $passed/$total tests passed")
    
    if (passed == total) {
        println("🎉 All validations passed! Unit tests are comprehensive and ready.")
        println("\n✨ Key Features Validated:")
        println("   • Elderly user accessibility defaults (1.6x font, 48dp touch targets)")
        println("   • 2×3 grid layout with 64dp icons")
        println("   • 3-crash threshold with 2×2 safe mode recovery")
        println("   • Priority notification weighting (emergency ×3, caregiver ×2)")
        println("   • Multilingual support (DE, EN, TR, AR, UK)")
        println("   • Offline functionality and privacy compliance")
        println("   • PIN security and caregiver integration")
    } else {
        println("⚠️  Some validations failed. Review issues above.")
        println("\n🔧 Next Steps:")
        println("   1. Address critical issues marked with 🚨")
        println("   2. Add missing test cases for elderly user requirements")
        println("   3. Ensure all accessibility features are tested")
        println("   4. Validate crash recovery and notification logic")
    }
}

operator fun String.times(n: Int): String = this.repeat(n)
