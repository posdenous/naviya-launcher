package com.naviya.launcher.debug

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.naviya.launcher.R
import com.naviya.launcher.emergency.data.EmergencyDataRetentionIntegrationTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Debug activity for running emergency data retention tests manually.
 * This activity is only included in debug builds and provides a simple UI
 * for executing and viewing the results of the EmergencyDataRetentionIntegrationTest.
 *
 * It can be launched via ADB with:
 * adb shell am start -n "com.naviya.launcher/.debug.EmergencyTestActivity" --ez "run_retention_test" true
 */
class EmergencyTestActivity : AppCompatActivity() {
    
    private lateinit var logTextView: TextView
    private lateinit var scrollView: ScrollView
    private val tag = "EmergencyDataRetentionTest"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_test)
        
        logTextView = findViewById(R.id.log_text_view)
        scrollView = findViewById(R.id.scroll_view)
        
        // Set up run test button
        findViewById<Button>(R.id.btn_run_test).setOnClickListener {
            runEmergencyDataRetentionTest()
        }
        
        // Set up clear logs button
        findViewById<Button>(R.id.btn_clear_logs).setOnClickListener {
            logTextView.text = ""
        }
        
        // Check if we should automatically run the test (from ADB command)
        if (intent.getBooleanExtra("run_retention_test", false)) {
            log("Auto-running test from intent...")
            runEmergencyDataRetentionTest()
        }
    }
    
    private fun runEmergencyDataRetentionTest() {
        log("Starting Emergency Data Retention Test...")
        
        // Redirect standard output to capture logs
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        System.setOut(printStream)
        
        // Run test in background thread to avoid blocking UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val test = EmergencyDataRetentionIntegrationTest()
                test.runTest(applicationContext)
                
                // Get captured output and display in UI
                System.setOut(originalOut)
                val output = outputStream.toString()
                
                runOnUiThread {
                    log(output)
                    if (output.contains("✅ TEST COMPLETED SUCCESSFULLY")) {
                        log("✅ Test completed successfully!")
                    } else if (output.contains("❌ TEST FAILED")) {
                        log("❌ Test failed! See logs for details.")
                    }
                    scrollView.post {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
            } catch (e: Exception) {
                System.setOut(originalOut)
                val errorOutput = "❌ Exception running test: ${e.message}\n${getStackTraceString(e)}"
                Log.e(tag, errorOutput)
                
                runOnUiThread {
                    log(errorOutput)
                    scrollView.post {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
            }
        }
    }
    
    private fun log(message: String) {
        Log.d(tag, message)
        runOnUiThread {
            logTextView.append("$message\n")
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }
    
    private fun getStackTraceString(e: Exception): String {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        e.printStackTrace(printStream)
        return outputStream.toString()
    }
}
