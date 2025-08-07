package com.naviya.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// import dagger.hilt.android.AndroidEntryPoint - temporarily disabled for build stability
import com.naviya.launcher.emergency.EmergencyService
import com.naviya.launcher.emergency.ui.SOSButton
import com.naviya.launcher.layout.LayoutManager
import com.naviya.launcher.layout.ui.LauncherGridView
import com.naviya.launcher.toggle.ToggleMode
import com.naviya.launcher.ui.theme.NaviyaLauncherTheme
import com.naviya.launcher.unread.UnreadTileService
import com.naviya.launcher.unread.UnreadTile
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import android.content.Context
import com.naviya.launcher.ui.onboarding.OnboardingActivity

/**
 * Main launcher activity that integrates Emergency SOS System and Launcher Layout Engine
 * into a cohesive, elderly-friendly interface following Windsurf accessibility rules.
 * 
 * Features:
 * - Emergency SOS button with accessibility compliance
 * - Dynamic app grid based on current toggle mode
 * - Mode switching interface with elderly-friendly design
 * - Unread notifications tile
 * - Offline mode indicators
 * - Multilingual support (DE/EN/TR/AR/UA)
 * - 48dp+ touch targets, 1.6x+ font scaling
 * - High contrast colors and large text
 */
// @AndroidEntryPoint - temporarily disabled for build stability
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val PREFS_NAME = "naviya_launcher_prefs"
    }
    
    // Temporarily creating instances directly instead of using Hilt
    // @Inject lateinit var emergencyService: EmergencyService
    // @Inject lateinit var layoutManager: LayoutManager
    // @Inject lateinit var unreadTileService: UnreadTileService
    
    // Direct instantiation for build stability
    private val emergencyService = EmergencyService()
    private val layoutManager = LayoutManager()
    private val unreadTileService = UnreadTileService()
    
    private val viewModel: MainLauncherViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if onboarding is needed before showing main UI
        checkOnboardingStatus()
    }
    
    /**
     * Check if user needs onboarding and route accordingly
     */
    private fun checkOnboardingStatus() {
        // Check if onboarding is completed
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isOnboardingCompleted = prefs.getBoolean(PREF_ONBOARDING_COMPLETED, false)
        
        if (!isOnboardingCompleted) {
            // Launch onboarding activity
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity so user can't go back
            return
        } else {
            showMainLauncher()
        }
    }
    

    /**
     * Show the main launcher UI after onboarding is completed
     */
    private fun showMainLauncher() {
        // Initialize emergency service
        emergencyService.initialize()
        
        setContent {
            NaviyaLauncherTheme {
                MainLauncherScreen(
                    viewModel = viewModel,
                    unreadTileService = unreadTileService,
                    onEmergencyActivated = { handleEmergencyActivation() },
                    onModeChanged = { mode -> handleModeChange(mode) },
                    onAppLaunched = { packageName -> handleAppLaunch(packageName) },
                    onUnreadTileClicked = { handleUnreadTileClick() }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Handle app_resume event per user rules
        viewModel.onAppResume()
    }
    
    override fun onStart() {
        super.onStart()
        // Handle launcher_home_opened event per user rules
        viewModel.onLauncherHomeOpened()
    }
    
    private fun handleEmergencyActivation() {
        lifecycleScope.launch {
            try {
                emergencyService.activateSOS(
                    context = this@MainActivity,
                    triggeredBy = "main_launcher_button"
                )
                
                // Show confirmation to user
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.emergency_activated),
                    Toast.LENGTH_LONG
                ).show()
                
            } catch (e: Exception) {
                // Handle emergency activation error
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.emergency_activation_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun handleModeChange(mode: ToggleMode) {
        lifecycleScope.launch {
            try {
                layoutManager.switchToMode(mode)
                viewModel.updateCurrentMode(mode)
                
                // Show mode change confirmation
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.mode_switched_to, mode.getDisplayName(this@MainActivity)),
                    Toast.LENGTH_SHORT
                ).show()
                
            } catch (e: Exception) {
                // Handle mode change error
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.mode_switch_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun handleAppLaunch(packageName: String) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
                
                // Log app launch for analytics
                viewModel.logAppLaunch(packageName)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.app_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.app_launch_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun handleUnreadTileClick() {
        // Open phone app or messaging app to handle unread notifications
        try {
            val phoneIntent = Intent(Intent.ACTION_DIAL)
            startActivity(phoneIntent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open phone app",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun MainLauncherScreen(
    viewModel: MainLauncherViewModel,
    unreadTileService: UnreadTileService,
    onEmergencyActivated: () -> Unit,
    onModeChanged: (ToggleMode) -> Unit,
    onAppLaunched: (String) -> Unit,
    onUnreadTileClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)) // High contrast dark background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        // Status Bar with Mode and Connectivity
        StatusBar(
            currentMode = uiState.currentMode,
            isOnline = uiState.isOnline,
            unreadCount = uiState.unreadCount,
            onModeChanged = onModeChanged
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Unread Tile (following user rules)
        UnreadTile(
            unreadTileService = unreadTileService,
            onTileClicked = onUnreadTileClicked,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main App Grid
        LauncherGridView(
            layoutConfiguration = uiState.currentLayout,
            appTiles = uiState.appTiles,
            onTileClicked = onAppLaunched,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Emergency SOS Button (Always visible at bottom)
        SOSButton(
            onSOSActivated = onEmergencyActivated,
            isEnabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp) // Large touch target for elderly users
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatusBar(
    currentMode: ToggleMode,
    isOnline: Boolean,
    unreadCount: Int,
    onModeChanged: (ToggleMode) -> Unit
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF2D2D2D),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        // Current Mode Indicator
        Column {
            Text(
                text = stringResource(R.string.current_mode),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = currentMode.getDisplayName(context),
                color = Color(0xFF4CAF50), // Green accent
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Connectivity and Unread Indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Connectivity Status
            ConnectivityIndicator(isOnline = isOnline)
            
            // Unread Count
            if (unreadCount > 0) {
                UnreadIndicator(count = unreadCount)
            }
            
            // Mode Switch Button
            ModeSwitchButton(
                currentMode = currentMode,
                onModeChanged = onModeChanged
            )
        }
    }
}

@Composable
fun ConnectivityIndicator(isOnline: Boolean) {
    val color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800)
    val text = if (isOnline) 
        stringResource(R.string.online) 
    else 
        stringResource(R.string.offline)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(6.dp))
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun UnreadIndicator(count: Int) {
    Box(
        modifier = Modifier
            .background(
                Color(0xFFFF5722), // Orange for attention
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModeSwitchButton(
    currentMode: ToggleMode,
    onModeChanged: (ToggleMode) -> Unit
) {
    var showModeDialog by remember { mutableStateOf(false) }
    
    Button(
        onClick = { showModeDialog = true },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3) // Blue accent
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(48.dp) // Minimum touch target
    ) {
        Text(
            text = stringResource(R.string.switch_mode),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
    
    if (showModeDialog) {
        ModeSelectionDialog(
            currentMode = currentMode,
            onModeSelected = { mode ->
                onModeChanged(mode)
                showModeDialog = false
            },
            onDismiss = { showModeDialog = false }
        )
    }
}

@Composable
fun ModeSelectionDialog(
    currentMode: ToggleMode,
    onModeSelected: (ToggleMode) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_mode),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                ToggleMode.values().forEach { mode ->
                    val isSelected = mode == currentMode
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) 
                                Color(0xFF2196F3) 
                            else 
                                Color(0xFF2D2D2D)
                        ),
                        onClick = { onModeSelected(mode) }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = mode.getDisplayName(context),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mode.getDescription(context),
                                color = Color(0xFFCCCCCC),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = Color(0xFF2196F3),
                    fontSize = 16.sp
                )
            }
        }
    )
}
