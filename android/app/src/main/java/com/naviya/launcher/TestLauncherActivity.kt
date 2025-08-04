package com.naviya.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naviya.launcher.toggle.ToggleMode
import com.naviya.launcher.layout.TileType
import com.naviya.launcher.layout.SemanticTileLayout
import com.naviya.launcher.emergency.MedicalEmergencyType
import com.naviya.launcher.ui.theme.NaviyaLauncherTheme
import com.naviya.launcher.core.NaviyaConstants

/**
 * Test Launcher Activity - Minimal implementation for emulator testing
 * Demonstrates core 3-mode system and emergency features without complex dependencies
 */
class TestLauncherActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ElderlyFriendlyTheme {
                TestLauncherScreen()
            }
        }
    }
}

// Elderly and disabled-friendly color scheme
@Composable
fun ElderlyFriendlyTheme(content: @Composable () -> Unit) {
    val elderlyColors = lightColorScheme(
        // High contrast colors for better visibility
        primary = Color(0xFF1565C0),           // Strong blue
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE3F2FD),  // Light blue background
        onPrimaryContainer = Color(0xFF0D47A1),
        
        // Background colors with high contrast
        background = Color.White,               // Pure white background
        onBackground = Color(0xFF212121),       // Dark gray text
        surface = Color(0xFFF5F5F5),           // Light gray surface
        onSurface = Color(0xFF212121),
        
        // Error colors for emergency
        error = Color(0xFFD32F2F),             // Strong red for emergency
        onError = Color.White,
        errorContainer = Color(0xFFFFEBEE),
        onErrorContainer = Color(0xFFB71C1C),
        
        // Secondary colors
        secondary = Color(0xFF388E3C),          // Strong green for positive actions
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8F5E8),
        onSecondaryContainer = Color(0xFF1B5E20)
    )
    
    MaterialTheme(
        colorScheme = elderlyColors,
        content = content
    )
}

@Composable
fun TestLauncherScreen() {
    var currentMode by remember { mutableStateOf(ToggleMode.COMFORT) }
    var showEmergencyScreen by remember { mutableStateOf(false) }
    var showModeSelector by remember { mutableStateOf(false) }
    
    if (showEmergencyScreen) {
        TestEmergencyScreen(
            onBack = { showEmergencyScreen = false }
        )
    } else if (showModeSelector) {
        ModeSelectorScreen(
            currentMode = currentMode,
            onModeSelected = { mode ->
                currentMode = mode
                showModeSelector = false
            },
            onBack = { showModeSelector = false }
        )
    } else {
        LauncherMainScreen(
            mode = currentMode,
            onEmergencyClick = { showEmergencyScreen = true },
            onModeClick = { showModeSelector = true }
        )
    }
}

@Composable
fun LauncherMainScreen(
    mode: ToggleMode,
    onEmergencyClick: () -> Unit,
    onModeClick: () -> Unit
) {
    val layout = SemanticTileLayout.getLayoutForMode(mode)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with mode info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Naviya Launcher - Test Mode",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current Mode: ${mode.getLocalizedName("en")}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = mode.getLocalizedDescription("en"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onModeClick,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Change Mode")
                }
            }
        }
        
        // Emergency SOS Button - Extra large for elderly accessibility
        Button(
            onClick = onEmergencyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)  // Increased height for better accessibility
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(NaviyaConstants.UI.RECOMMENDED_ICON_SIZE_DP.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "EMERGENCY SOS",
                fontSize = 24.sp,  // Increased font size
                fontWeight = FontWeight.Bold
            )
        }
        
        // App Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(mode.gridColumns),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(layout.slots) { slot ->
                AppTileCard(
                    tileType = slot.tileType,
                    onClick = { 
                        // In a real app, this would launch the actual app
                    }
                )
            }
        }
    }
}

@Composable
fun AppTileCard(
    tileType: TileType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),  // Increased padding for larger icons
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getTileIcon(tileType),
                contentDescription = null,
                modifier = Modifier.size(NaviyaConstants.UI.LARGE_ICON_SIZE_DP.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tileType.displayName,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ModeSelectorScreen(
    currentMode: ToggleMode,
    onModeSelected: (ToggleMode) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Select Launcher Mode",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ToggleMode.values().forEach { mode ->
            ModeCard(
                mode = mode,
                isSelected = mode == currentMode,
                onClick = { onModeSelected(mode) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ModeCard(
    mode: ToggleMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mode.getLocalizedName("en"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${mode.gridColumns}×${mode.gridRows}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mode.getLocalizedDescription("en"),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Target: ${mode.targetGroup}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TestEmergencyScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Emergency SOS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Select Emergency Type:",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(MedicalEmergencyType.values().toList()) { emergencyType ->
                EmergencyTypeCard(
                    emergencyType = emergencyType,
                    onClick = {
                        // In a real app, this would trigger emergency response
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
fun EmergencyTypeCard(
    emergencyType: MedicalEmergencyType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (emergencyType.priority == 1) {
                Color.Red.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),  // Increased padding for larger icons
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(NaviyaConstants.UI.RECOMMENDED_ICON_SIZE_DP.dp),
                tint = if (emergencyType.priority == 1) Color.Red else MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = emergencyType.displayName,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Elderly-friendly icon mapping with basic, guaranteed available icons
fun getTileIcon(tileType: TileType): ImageVector {
    return when (tileType) {
        TileType.PHONE -> Icons.Default.Phone           // ✓ Phone icon is universally recognized
        TileType.MESSAGES -> Icons.Default.Email        // ✓ Email icon (closest available to messages)
        TileType.CONTACTS -> Icons.Default.Person       // ✓ Person icon for contacts
        TileType.CAMERA -> Icons.Default.Add            // ✓ Add icon (basic fallback)
        TileType.GALLERY -> Icons.Default.Settings      // ✓ Settings icon for gallery
        TileType.WEATHER -> Icons.Default.Star          // ✓ Star icon for weather
        TileType.FAMILY_COMMUNICATION -> Icons.Default.Favorite // ✓ Heart icon for family
        else -> Icons.Default.Home                       // ✓ Home icon for fallback
    }
}
