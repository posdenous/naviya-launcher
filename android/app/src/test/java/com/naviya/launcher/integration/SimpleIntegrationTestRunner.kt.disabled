package com.naviya.launcher.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naviya.launcher.emergency.EmergencyService
import com.naviya.launcher.layout.LayoutManager
import com.naviya.launcher.unread.UnreadTileService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Simple integration test runner that validates our main launcher UI integration
 * without requiring the full UI build. This allows us to test core component
 * integration quickly and identify any issues.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SimpleIntegrationTestRunner {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var simpleIntegrationTest: SimpleIntegrationTest
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }
    
    @Test
    fun runComprehensiveIntegrationTests() = runBlocking {
        println("🚀 Starting Comprehensive Integration Tests...")
        println("=" * 50)
        
        // Run all integration tests
        val results = simpleIntegrationTest.runAllTests()
        
        // Print detailed results
        println(results.getDetailedReport())
        
        // Validate results
        val passCount = results.getPassCount()
        val totalCount = results.getTotalCount()
        
        println("🎯 Integration Test Summary:")
        println("   Passed: $passCount/$totalCount tests")
        println("   Success Rate: ${(passCount * 100) / totalCount}%")
        
        if (results.getAllPassed()) {
            println("✅ ALL INTEGRATION TESTS PASSED!")
            println("   Main Launcher UI Integration is working correctly.")
            println("   All core components are properly integrated.")
        } else {
            println("❌ SOME INTEGRATION TESTS FAILED!")
            println("   Review the detailed report above for specific issues.")
            
            // Print specific failures
            listOf(
                "Emergency System" to results.emergencySystemTest,
                "Layout Engine" to results.layoutEngineTest,
                "Unread Tile" to results.unreadTileTest,
                "Mode Switching" to results.modeSwitchingTest,
                "Component Communication" to results.componentCommunicationTest
            ).forEach { (name, result) ->
                if (!result.isPassing) {
                    println("   ❌ $name: ${result.message}")
                }
            }
        }
        
        println("=" * 50)
        println("🏁 Integration Tests Complete")
        
        // Assert that all tests pass for CI/CD
        assert(results.getAllPassed()) { 
            "Integration tests failed: ${results.getPassCount()}/${results.getTotalCount()} passed" 
        }
    }
    
    @Test
    fun testEmergencySystemIntegration() = runBlocking {
        println("🆘 Testing Emergency System Integration...")
        
        val result = simpleIntegrationTest.runAllTests().emergencySystemTest
        
        println("Result: ${result.javaClass.simpleName} - ${result.message}")
        
        assert(result.isPassing) { "Emergency System integration failed: ${result.message}" }
    }
    
    @Test
    fun testLayoutEngineIntegration() = runBlocking {
        println("🎨 Testing Layout Engine Integration...")
        
        val result = simpleIntegrationTest.runAllTests().layoutEngineTest
        
        println("Result: ${result.javaClass.simpleName} - ${result.message}")
        
        assert(result.isPassing) { "Layout Engine integration failed: ${result.message}" }
    }
    
    @Test
    fun testUnreadTileIntegration() = runBlocking {
        println("📱 Testing Unread Tile Integration...")
        
        val result = simpleIntegrationTest.runAllTests().unreadTileTest
        
        println("Result: ${result.javaClass.simpleName} - ${result.message}")
        
        assert(result.isPassing) { "Unread Tile integration failed: ${result.message}" }
    }
    
    @Test
    fun testModeSwitchingIntegration() = runBlocking {
        println("🔄 Testing Mode Switching Integration...")
        
        val result = simpleIntegrationTest.runAllTests().modeSwitchingTest
        
        println("Result: ${result.javaClass.simpleName} - ${result.message}")
        
        assert(result.isPassing) { "Mode Switching integration failed: ${result.message}" }
    }
    
    @Test
    fun testComponentCommunication() = runBlocking {
        println("🔗 Testing Component Communication...")
        
        val result = simpleIntegrationTest.runAllTests().componentCommunicationTest
        
        println("Result: ${result.javaClass.simpleName} - ${result.message}")
        
        assert(result.isPassing) { "Component Communication failed: ${result.message}" }
    }
}

private operator fun String.times(count: Int): String = this.repeat(count)
