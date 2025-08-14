package com.naviya.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naviya.launcher.R
import com.naviya.launcher.data.TileData
import com.naviya.launcher.permissions.PermissionHandler
import com.naviya.launcher.unread.UnreadTileServiceInitializer

/**
 * UnreadTile composable for displaying missed calls and unread SMS
 * Designed with elderly users in mind - large text, high contrast, and clear visuals
 */
@Composable
fun UnreadTile(
    tileData: TileData,
    onTileClick: (TileData) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val unreadCount = tileData.badgeCount ?: 0
    val hasUnread = unreadCount > 0
    
    // Animation for highlighting when there are unread items
    val scale by animateFloatAsState(
        targetValue = if (hasUnread) 1.05f else 1f,
        animationSpec = tween(durationMillis = 300)
    )
    
    // Determine background color based on unread status
    val backgroundColor = if (hasUnread) {
        MaterialTheme.colors.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colors.surface
    }
    
    // Determine border color based on unread status
    val borderColor = if (hasUnread) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    }
    
    // Create accessibility description
    val accessibilityDescription = tileData.getAccessibilityDescription()
    
    Surface(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (hasUnread) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(backgroundColor)
            .clickable(
                onClick = { 
                    // Check permissions before handling click
                    val permissionHandler = PermissionHandler(context as androidx.fragment.app.FragmentActivity)
                    if (permissionHandler.hasUnreadTilePermissions()) {
                        onTileClick(tileData)
                    } else {
                        permissionHandler.initialize()
                        permissionHandler.requestUnreadTilePermissions { granted ->
                            if (granted) {
                                // Trigger an update after permissions are granted
                                UnreadTileServiceInitializer.getInstance(context).triggerUpdate()
                                onTileClick(tileData)
                            }
                        }
                    }
                }
            )
            .semantics { contentDescription = accessibilityDescription },
        elevation = if (hasUnread) 4.dp else 1.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Icon(
                painter = painterResource(id = R.drawable.ic_envelope),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colors.primary
            )
            
            // Title
            Text(
                text = tileData.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )
            
            // Badge count
            AnimatedVisibility(
                visible = hasUnread,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Caregiver note if available
            tileData.note?.let { note ->
                Text(
                    text = note,
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
