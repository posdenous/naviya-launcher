package com.naviya.launcher.unread

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naviya.launcher.R

/**
 * A large tile showing total count of missed calls + unread SMS
 * Designed for elderly users with high visibility and large touch targets
 * Works offline using local call log and SMS inbox access
 * 
 * Features:
 * - Large icon and text for easy visibility
 * - Shows total unread count prominently
 * - Displays caregiver availability status
 * - Shows reminder when unread count > 0
 * - Adds note when caregiver is offline
 */
@Composable
fun UnreadTile(
    unreadTileService: UnreadTileService,
    onClick: () -> Unit
) {
    val tileData by unreadTileService.tileData.collectAsState()
    val reminderText by unreadTileService.reminderText.collectAsState()
    val noteText by unreadTileService.noteText.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)  // Large touch target for elderly users
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (tileData.totalUnread > 0) Color(0xFFE57373) else Color(0xFF4CAF50)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Envelope icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_envelope),
                    contentDescription = "Unread messages",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)  // Large icon for visibility
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Unread count
                Text(
                    text = tileData.totalUnread.toString(),
                    color = Color.White,
                    fontSize = 36.sp,  // Very large font for elderly users
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show breakdown text
            Text(
                text = unreadTileService.getUnreadBreakdown(),
                color = Color.White,
                fontSize = 18.sp,  // Larger text for readability
                textAlign = TextAlign.Center
            )
            
            // Show reminder if available
            reminderText?.let {
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Show caregiver note if available
            noteText?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0x33000000), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = it,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
