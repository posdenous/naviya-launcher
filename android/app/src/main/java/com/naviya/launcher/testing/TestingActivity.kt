package com.naviya.launcher.testing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naviya.launcher.R
import com.naviya.launcher.ui.theme.NaviyaLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity for running in-app tests
 * Provides a simple, elderly-friendly interface for testing app functionality
 */
@AndroidEntryPoint
class TestingActivity : ComponentActivity() {

    @Inject
    lateinit var unreadTileTestRunner: UnreadTileTestRunner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NaviyaLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestingScreen()
                }
            }
        }
    }

    @Composable
    fun TestingScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.testing_activity_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Unread Tile Test Button
            Button(
                onClick = { showUnreadTileTests() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.run_unread_tile_tests),
                    fontSize = 24.sp
                )
            }
            
            // Add more test buttons here as needed
        }
    }
    
    private fun showUnreadTileTests() {
        setContent {
            NaviyaLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        // Back button
                        Button(
                            onClick = { recreate() },
                            modifier = Modifier
                                .padding(16.dp)
                                .height(56.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.back_to_tests),
                                fontSize = 20.sp
                            )
                        }
                        
                        // Test screen
                        UnreadTileTestScreen(unreadTileTestRunner)
                    }
                }
            }
        }
    }
}
