package com.naviya.launcher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.naviya.launcher.unread.UnreadTile
import com.naviya.launcher.unread.UnreadTileService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main launcher activity that displays the home screen
 * and handles lifecycle events for the unread tile
 */
@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()
    
    @Inject
    lateinit var unreadTileService: UnreadTileService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Trigger launcher_home_opened event as specified in unread-tile.md
        viewModel.onLauncherHomeOpened()
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Main launcher layout would go here
                    // For now, just showing the UnreadTile
                    UnreadTile(
                        unreadTileService = unreadTileService,
                        onClick = { /* Handle tile click */ }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Trigger app_resume event as specified in unread-tile.md
        viewModel.onAppResume()
    }
}
