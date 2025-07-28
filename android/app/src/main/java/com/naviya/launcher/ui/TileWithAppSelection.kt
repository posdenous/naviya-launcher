package com.naviya.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naviya.launcher.layout.AndroidTileTypeManager
import com.naviya.launcher.layout.TileSlot
import com.naviya.launcher.layout.TileType

/**
 * Example of how the app selection feature integrates with tiles
 * Shows the user-friendly flow for adding apps to empty tiles
 */
@Composable
fun TileWithAppSelection(
    slot: TileSlot,
    assignedApp: String? = null,
    onAppAssigned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tileTypeManager = remember { AndroidTileTypeManager(context) }
    var showAppSelection by remember { mutableStateOf(false) }
    
    // Main tile UI
    Card(
        modifier = modifier
            .size(120.dp)
            .clickable {
                if (assignedApp == null) {
                    showAppSelection = true
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (assignedApp != null) {
                getTileTypeColor(slot.tileType).copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        border = if (assignedApp == null) {
            androidx.compose.foundation.BorderStroke(
                2.dp, 
                getTileTypeColor(slot.tileType).copy(alpha = 0.5f)
            )
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (assignedApp != null) {
                // Show assigned app
                AssignedAppContent(
                    packageName = assignedApp,
                    tileTypeManager = tileTypeManager
                )
            } else {
                // Show empty tile with guidance
                EmptyTileContent(slot.tileType)
            }
        }
    }
    
    // App selection dialog
    if (showAppSelection) {
        AppSelectionDialog(
            slot = slot,
            onAppSelected = { packageName ->
                onAppAssigned(packageName)
                showAppSelection = false
            },
            onDismiss = { showAppSelection = false },
            tileTypeManager = tileTypeManager
        )
    }
}

@Composable
private fun EmptyTileContent(tileType: TileType) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        // Add icon with type-specific color
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add app",
            modifier = Modifier.size(32.dp),
            tint = getTileTypeColor(tileType)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Type hint for user
        Text(
            text = when (tileType) {
                TileType.COMMUNICATION -> "📞\nCall & Text"
                TileType.MEDITATION_FOCUS -> "🧘\nMeditation"
                TileType.EDUCATION_KIDS -> "🎓\nEducation"
                TileType.PRODUCTIVITY_WORK -> "💼\nWork"
                TileType.CAMERA_PHOTO -> "📷\nPhotos"
                TileType.ACCESSIBILITY_TOOLS -> "♿\nAccessibility"
                TileType.PARENTAL_CONTROL -> "👨‍👩‍👧‍👦\nFamily"
                TileType.EMERGENCY_SAFETY -> "🚨\nEmergency"
                TileType.SYSTEM_ESSENTIAL -> "⚙️\nSystem"
                TileType.FLEXIBLE -> "📱\nAny App"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = getTileTypeColor(tileType),
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun AssignedAppContent(
    packageName: String,
    tileTypeManager: AndroidTileTypeManager
) {
    val context = LocalContext.current
    val appName = remember { tileTypeManager.getAppDisplayName(packageName) }
    val appIcon = remember {
        try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        // App icon or placeholder
        if (appIcon != null) {
            androidx.compose.foundation.Image(
                bitmap = androidx.core.graphics.drawable.toBitmap(appIcon).asImageBitmap(),
                contentDescription = appName,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appName.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // App name (truncated)
        Text(
            text = if (appName.length > 10) {
                appName.take(8) + "..."
            } else appName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )
    }
}

@Composable
private fun getTileTypeColor(tileType: TileType): Color {
    return when (tileType) {
        TileType.COMMUNICATION -> Color(0xFF2196F3) // Blue
        TileType.MEDITATION_FOCUS -> Color(0xFF4CAF50) // Green
        TileType.EDUCATION_KIDS -> Color(0xFFFF9800) // Orange
        TileType.PRODUCTIVITY_WORK -> Color(0xFF9C27B0) // Purple
        TileType.CAMERA_PHOTO -> Color(0xFFE91E63) // Pink
        TileType.ACCESSIBILITY_TOOLS -> Color(0xFF00BCD4) // Cyan
        TileType.PARENTAL_CONTROL -> Color(0xFFFF5722) // Deep Orange
        TileType.EMERGENCY_SAFETY -> Color(0xFFF44336) // Red
        TileType.SYSTEM_ESSENTIAL -> Color(0xFF607D8B) // Blue Grey
        TileType.FLEXIBLE -> Color(0xFF9E9E9E) // Grey
    }
}

/**
 * Demo of how the tile grid would look with app selection
 */
@Composable
fun AppSelectionDemo() {
    val context = LocalContext.current
    val comfortModeSlots = remember {
        listOf(
            TileSlot(0, TileType.COMMUNICATION, isRequired = true, displayPriority = 10),
            TileSlot(1, TileType.COMMUNICATION, displayPriority = 9),
            TileSlot(2, TileType.EMERGENCY_SAFETY, isRequired = true, displayPriority = 10),
            TileSlot(3, TileType.CAMERA_PHOTO, displayPriority = 7),
            TileSlot(4, TileType.SYSTEM_ESSENTIAL, displayPriority = 6),
            TileSlot(5, TileType.FLEXIBLE, displayPriority = 5)
        )
    }
    
    var appAssignments by remember { mutableStateOf(mapOf<Int, String>()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tap empty tiles to add apps",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 2x3 grid for Comfort Mode
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) { col ->
                        val index = row * 3 + col
                        if (index < comfortModeSlots.size) {
                            val slot = comfortModeSlots[index]
                            TileWithAppSelection(
                                slot = slot,
                                assignedApp = appAssignments[slot.position],
                                onAppAssigned = { packageName ->
                                    appAssignments = appAssignments + (slot.position to packageName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
