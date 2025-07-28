package com.naviya.launcher.unread

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naviya.launcher.R

/**
 * UnreadTile component following user rules:
 * - Shows total count of missed calls + unread SMS in a large tile
 * - Works offline using local call log and SMS inbox access
 * - Shows reminder if total_unread > 0
 * - Adds caregiver availability note if offline
 * - Follows Windsurf elderly accessibility rules
 */
@Composable
fun UnreadTile(
    unreadTileService: UnreadTileService,
    onTileClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileData by unreadTileService.tileData.collectAsStateWithLifecycle()
    val unreadBreakdown = unreadTileService.getUnreadBreakdown()
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp) // Large tile as per user rules
            .clickable { onTileClicked() }
            .semantics {
                contentDescription = unreadBreakdown.getAccessibilityDescription()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (tileData.badge > 0) 
                Color(0xFF2D2D2D) // Highlighted when unread
            else 
                Color(0xFF1E1E1E) // Normal background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            
            // Main content
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                
                // Header with icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (tileData.hasError) Icons.Default.Warning else Icons.Default.Email,
                            contentDescription = null,
                            tint = if (tileData.badge > 0) Color(0xFFFF9800) else Color(0xFF757575),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Text(
                            text = tileData.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Badge with count
                    if (tileData.badge > 0) {
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
                                text = if (tileData.badge > 99) "99+" else tileData.badge.toString(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Content area
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    
                    // Main count display
                    if (tileData.badge > 0) {
                        Text(
                            text = "${tileData.badge} unread",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start
                        )
                        
                        // Breakdown text
                        Text(
                            text = unreadBreakdown.getAccessibilityDescription(),
                            color = Color(0xFFCCCCCC),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start
                        )
                    } else {
                        Text(
                            text = if (tileData.hasError) 
                                tileData.errorMessage ?: "Permission required"
                            else 
                                "No unread notifications",
                            color = if (tileData.hasError) Color(0xFFFF9800) else Color(0xFF757575),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                    
                    // Reminder text if total_unread > 0
                    val reminderText = tileData.reminderText
                    if (tileData.hasReminder && reminderText != null) {
                        Text(
                            text = reminderText,
                            color = Color(0xFFFF9800), // Orange for attention
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Caregiver note if offline
                    val caregiverNote = tileData.caregiverNote
                    if (caregiverNote != null) {
                        Text(
                            text = caregiverNote,
                            color = Color(0xFF757575),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact version of UnreadTile for smaller layouts
 */
@Composable
fun UnreadTileCompact(
    unreadTileService: UnreadTileService,
    onTileClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileData by unreadTileService.tileData.collectAsStateWithLifecycle()
    val unreadBreakdown = unreadTileService.getUnreadBreakdown()
    
    Card(
        modifier = modifier
            .size(80.dp) // Compact tile size
            .clickable { onTileClicked() }
            .semantics {
                contentDescription = unreadBreakdown.getAccessibilityDescription()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (tileData.badge > 0) 
                Color(0xFF2D2D2D) 
            else 
                Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                
                // Icon with badge overlay
                Box {
                    Icon(
                        imageVector = if (tileData.hasError) Icons.Default.Warning else Icons.Default.Email,
                        contentDescription = null,
                        tint = if (tileData.badge > 0) Color(0xFFFF9800) else Color(0xFF757575),
                        modifier = Modifier.size(32.dp)
                    )
                    
                    // Badge overlay
                    if (tileData.badge > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    Color(0xFFFF5722),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (tileData.badge > 9) "9+" else tileData.badge.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Title
                Text(
                    text = "Unread",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * UnreadTile preview for development
 */
@Composable
fun UnreadTilePreview(
    missedCalls: Int = 2,
    unreadSms: Int = 3,
    caregiverOnline: Boolean = true,
    hasError: Boolean = false
) {
    val previewData = UnreadTileData(
        title = "Unread",
        icon = "envelope",
        badge = missedCalls + unreadSms,
        hasReminder = (missedCalls + unreadSms) > 0,
        reminderText = if ((missedCalls + unreadSms) > 0) "You have missed calls or messages." else null,
        caregiverNote = if (!caregiverOnline) "Caregiver not available." else null,
        hasError = hasError,
        errorMessage = if (hasError) "Permission required to read notifications" else null
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (previewData.badge > 0) 
                Color(0xFF2D2D2D) 
            else 
                Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (hasError) Icons.Default.Warning else Icons.Default.Email,
                            contentDescription = null,
                            tint = if (previewData.badge > 0) Color(0xFFFF9800) else Color(0xFF757575),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = previewData.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (previewData.badge > 0) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFFFF5722),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = previewData.badge.toString(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (previewData.badge > 0) {
                        Text(
                            text = "${previewData.badge} unread",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$missedCalls missed calls and $unreadSms unread messages",
                            color = Color(0xFFCCCCCC),
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = if (hasError) "Permission required" else "No unread notifications",
                            color = if (hasError) Color(0xFFFF9800) else Color(0xFF757575),
                            fontSize = 16.sp
                        )
                    }
                    
                    if (previewData.hasReminder && previewData.reminderText != null) {
                        Text(
                            text = previewData.reminderText,
                            color = Color(0xFFFF9800),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    if (previewData.caregiverNote != null) {
                        Text(
                            text = previewData.caregiverNote,
                            color = Color(0xFF757575),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
