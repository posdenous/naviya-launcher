package com.naviya.launcher.testing

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naviya.launcher.R
import com.naviya.launcher.unread.UnreadTileEventHandler
import com.naviya.launcher.unread.UnreadTileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * In-app test runner for the Unread Tile functionality
 * Allows running tests directly from the app interface
 * Designed to be accessible for elderly users with large buttons and clear results
 */
class UnreadTileTestRunner @Inject constructor(
    private val unreadTileService: UnreadTileService,
    private val unreadTileEventHandler: UnreadTileEventHandler
) {
    private val TAG = "UnreadTileTestRunner"
    
    // Test results
    private val _testResults = mutableStateOf<List<TestResult>>(emptyList())
    val testResults: State<List<TestResult>> = _testResults
    
    // Run all tests
    fun runAllTests(scope: CoroutineScope) {
        scope.launch {
            _testResults.value = emptyList()
            
            // Run each test and collect results
            val results = mutableListOf<TestResult>()
            results.add(testReadMissedCalls())
            results.add(testReadUnreadSms())
            results.add(testTotalUnreadCount())
            results.add(testReminderShown())
            results.add(testCaregiverNote())
            
            _testResults.value = results
            
            // Log overall result
            val passedCount = results.count { it.passed }
            Log.d(TAG, "Tests completed: $passedCount/${results.size} passed")
        }
    }
    
    // Individual test methods
    private suspend fun testReadMissedCalls(): TestResult {
        return try {
            val missedCalls = unreadTileService.readMissedCalls()
            TestResult(
                name = "Read Missed Calls",
                passed = true,
                message = "Found $missedCalls missed calls"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Read Missed Calls",
                passed = false,
                message = "Error: ${e.message}"
            )
        }
    }
    
    private suspend fun testReadUnreadSms(): TestResult {
        return try {
            val unreadSms = unreadTileService.readUnreadSms()
            TestResult(
                name = "Read Unread SMS",
                passed = true,
                message = "Found $unreadSms unread messages"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Read Unread SMS",
                passed = false,
                message = "Error: ${e.message}"
            )
        }
    }
    
    private suspend fun testTotalUnreadCount(): TestResult {
        return try {
            // Trigger update
            unreadTileEventHandler.onLauncherHomeOpened()
            
            // Get the tile data
            val tileData = unreadTileService.tileData.first()
            val missedCalls = unreadTileService.readMissedCalls()
            val unreadSms = unreadTileService.readUnreadSms()
            
            // Verify total is sum of calls and SMS
            val passed = tileData.totalUnread == (missedCalls + unreadSms)
            
            TestResult(
                name = "Total Unread Count",
                passed = passed,
                message = "Total: ${tileData.totalUnread}, Expected: ${missedCalls + unreadSms}"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Total Unread Count",
                passed = false,
                message = "Error: ${e.message}"
            )
        }
    }
    
    private suspend fun testReminderShown(): TestResult {
        return try {
            // Trigger update
            unreadTileEventHandler.onLauncherHomeOpened()
            
            // Get the tile data and reminder
            val tileData = unreadTileService.tileData.first()
            val reminder = unreadTileService.reminderText.first()
            
            // Verify reminder is shown when there are unread items
            val passed = if (tileData.totalUnread > 0) {
                reminder == "You have missed calls or messages."
            } else {
                reminder?.isEmpty() ?: true
            }
            
            TestResult(
                name = "Reminder Shown",
                passed = passed,
                message = "Unread: ${tileData.totalUnread}, Reminder: '$reminder'"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Reminder Shown",
                passed = false,
                message = "Error: ${e.message}"
            )
        }
    }
    
    private suspend fun testCaregiverNote(): TestResult {
        return try {
            // Trigger update
            unreadTileEventHandler.onLauncherHomeOpened()
            
            // Get the note
            val note = unreadTileService.noteText.first()
            
            // Verify caregiver note is shown when offline
            val passed = note == "Caregiver not available."
            
            TestResult(
                name = "Caregiver Note",
                passed = passed,
                message = "Note: '$note'"
            )
        } catch (e: Exception) {
            TestResult(
                name = "Caregiver Note",
                passed = false,
                message = "Error: ${e.message}"
            )
        }
    }
    
    // Data class for test results
    data class TestResult(
        val name: String,
        val passed: Boolean,
        val message: String
    )
}

/**
 * Composable UI for the test runner
 * Provides large, elderly-friendly buttons and clear test results
 */
@Composable
fun UnreadTileTestScreen(testRunner: UnreadTileTestRunner) {
    val scope = rememberCoroutineScope()
    val testResults by testRunner.testResults
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.run_unread_tile_tests),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Button(
            onClick = { testRunner.runAllTests(scope) },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.run_tests),
                fontSize = 24.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (testResults.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.test_results),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            testResults.forEach { result ->
                TestResultCard(result)
            }
            
            val passedCount = testResults.count { it.passed }
            val failedCount = testResults.size - passedCount
            Text(
                text = stringResource(id = R.string.test_summary, passedCount, failedCount),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun TestResultCard(result: UnreadTileTestRunner.TestResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.passed) Color.Green.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (result.passed) "✅" else "❌",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.message,
                fontSize = 18.sp
            )
        }
    }
}
