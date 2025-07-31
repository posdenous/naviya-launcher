package com.naviya.launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.naviya.launcher.R
import com.naviya.launcher.layout.AndroidTileTypeManager
import com.naviya.launcher.layout.TileSlot
import com.naviya.launcher.layout.TileType

/**
 * User-friendly app selection dialog for elderly users
 * Shows appropriate apps for each tile type with clear categorization
 */
@Composable
fun AppSelectionDialog(
    slot: TileSlot,
    onAppSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    tileTypeManager: AndroidTileTypeManager
) {
    val context = LocalContext.current
    val compatibleApps = remember { tileTypeManager.getAppsForTileType(slot.tileType) }
    val allApps = remember { tileTypeManager.getAllLauncherApps() }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header with tile type explanation
                AppSelectionHeader(slot.tileType)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recommended apps section
                if (compatibleApps.isNotEmpty()) {
                    RecommendedAppsSection(
                        apps = compatibleApps,
                        tileTypeManager = tileTypeManager,
                        onAppSelected = onAppSelected
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Divider
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // All apps section (if user wants something else)
                AllAppsSection(
                    allApps = allApps,
                    compatibleApps = compatibleApps,
                    tileTypeManager = tileTypeManager,
                    onAppSelected = onAppSelected
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cancel button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSelectionHeader(tileType: TileType) {
    Column {
        Text(
            text = stringResource(R.string.choose_app_for_tile),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Tile type explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = getTileTypeColor(tileType).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = tileType.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = getTileTypeColor(tileType)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = tileType.description,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun RecommendedAppsSection(
    apps: List<android.content.pm.ApplicationInfo>,
    tileTypeManager: AndroidTileTypeManager,
    onAppSelected: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "✨ " + stringResource(R.string.recommended_apps),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = stringResource(R.string.best_match),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps.take(5)) { app -> // Show top 5 recommendations
                AppSelectionItem(
                    app = app,
                    tileTypeManager = tileTypeManager,
                    onAppSelected = onAppSelected,
                    isRecommended = true
                )
            }
        }
    }
}

@Composable
private fun AllAppsSection(
    allApps: List<androidx.compose.ui.platform.LocalContext>,
    compatibleApps: List<android.content.pm.ApplicationInfo>,
    tileTypeManager: AndroidTileTypeManager,
    onAppSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val otherApps = remember {
        tileTypeManager.getAllLauncherApps()
            .mapNotNull { resolveInfo ->
                try {
                    context.packageManager.getApplicationInfo(
                        resolveInfo.activityInfo.packageName, 0
                    )
                } catch (e: Exception) { null }
            }
            .filterNot { app ->
                compatibleApps.any { compatible -> 
                    compatible.packageName == app.packageName 
                }
            }
    }
    
    Column {
        Text(
            text = stringResource(R.string.all_other_apps),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(otherApps) { app ->
                AppSelectionItem(
                    app = app,
                    tileTypeManager = tileTypeManager,
                    onAppSelected = onAppSelected,
                    isRecommended = false
                )
            }
        }
    }
}

@Composable
private fun AppSelectionItem(
    app: android.content.pm.ApplicationInfo,
    tileTypeManager: AndroidTileTypeManager,
    onAppSelected: (String) -> Unit,
    isRecommended: Boolean
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    val appName = remember { tileTypeManager.getAppDisplayName(app.packageName) }
    val appIcon = remember {
        try {
            packageManager.getApplicationIcon(app.packageName).toBitmap()
        } catch (e: Exception) {
            null
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAppSelected(app.packageName) },
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            if (appIcon != null) {
                Image(
                    bitmap = appIcon.asImageBitmap(),
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
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appName.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // App name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    fontSize = 18.sp,
                    fontWeight = if (isRecommended) FontWeight.SemiBold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (isRecommended) {
                    Text(
                        text = stringResource(R.string.perfect_for_this_tile),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Recommendation badge
            if (isRecommended) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "⭐",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
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
