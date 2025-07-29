package com.naviya.launcher.ui.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naviya.launcher.R

/**
 * Elder Rights Advocacy Widget
 * Provides easily accessible elder rights resources and advocacy contacts
 * Protected from caregiver removal to ensure elderly users always have access to help
 */
@Composable
fun ElderRightsAdvocacyWidget(
    modifier: Modifier = Modifier,
    isEmergencyMode: Boolean = false
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEmergencyMode) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_elder_rights),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isEmergencyMode) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.elder_rights_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Emergency Notice (if in emergency mode)
            if (isEmergencyMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFCDD2)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_warning),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.elder_rights_emergency_notice),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Emergency Call Button
                Button(
                    onClick = { makeEmergencyCall(context) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF1744)
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_emergency_call),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.elder_rights_emergency_call),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Helpline Button
                OutlinedButton(
                    onClick = { callElderRightsHelpline(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_phone),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.elder_rights_helpline),
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Resources List
            Text(
                text = stringResource(R.string.elder_rights_resources_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(getElderRightsResources()) { resource ->
                    ElderRightsResourceCard(
                        resource = resource,
                        onClick = { openResource(context, resource) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_shield),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.elder_rights_protection_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.elder_rights_protection_description),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Individual resource card component
 */
@Composable
private fun ElderRightsResourceCard(
    resource: ElderRightsResource,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(resource.icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = resource.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = resource.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Data class for elder rights resources
 */
data class ElderRightsResource(
    val id: String,
    val title: String,
    val description: String,
    val type: ResourceType,
    val contact: String,
    val icon: Int,
    val isEmergency: Boolean = false
)

enum class ResourceType {
    PHONE, WEBSITE, EMAIL, CHAT, LEGAL_AID
}

/**
 * Get list of elder rights resources
 */
@Composable
private fun getElderRightsResources(): List<ElderRightsResource> {
    return listOf(
        ElderRightsResource(
            id = "age_uk",
            title = stringResource(R.string.resource_age_uk_title),
            description = stringResource(R.string.resource_age_uk_description),
            type = ResourceType.PHONE,
            contact = "0800 678 1602",
            icon = R.drawable.ic_age_uk
        ),
        ElderRightsResource(
            id = "elder_abuse_helpline",
            title = stringResource(R.string.resource_elder_abuse_title),
            description = stringResource(R.string.resource_elder_abuse_description),
            type = ResourceType.PHONE,
            contact = "0808 808 8141",
            icon = R.drawable.ic_helpline
        ),
        ElderRightsResource(
            id = "citizens_advice",
            title = stringResource(R.string.resource_citizens_advice_title),
            description = stringResource(R.string.resource_citizens_advice_description),
            type = ResourceType.WEBSITE,
            contact = "https://www.citizensadvice.org.uk",
            icon = R.drawable.ic_citizens_advice
        ),
        ElderRightsResource(
            id = "legal_aid",
            title = stringResource(R.string.resource_legal_aid_title),
            description = stringResource(R.string.resource_legal_aid_description),
            type = ResourceType.LEGAL_AID,
            contact = "0345 345 4 345",
            icon = R.drawable.ic_legal_aid
        ),
        ElderRightsResource(
            id = "safeguarding",
            title = stringResource(R.string.resource_safeguarding_title),
            description = stringResource(R.string.resource_safeguarding_description),
            type = ResourceType.PHONE,
            contact = "0300 123 4567",
            icon = R.drawable.ic_safeguarding
        )
    )
}

/**
 * Make emergency call (999/112)
 */
private fun makeEmergencyCall(context: Context) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:999")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to dialer if direct calling not permitted
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:999")
        }
        context.startActivity(dialIntent)
    }
}

/**
 * Call elder rights helpline
 */
private fun callElderRightsHelpline(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:08088088141") // Elder Abuse Helpline
    }
    context.startActivity(intent)
}

/**
 * Open resource based on type
 */
private fun openResource(context: Context, resource: ElderRightsResource) {
    when (resource.type) {
        ResourceType.PHONE -> {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${resource.contact}")
            }
            context.startActivity(intent)
        }
        ResourceType.WEBSITE -> {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(resource.contact)
            }
            context.startActivity(intent)
        }
        ResourceType.EMAIL -> {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${resource.contact}")
            }
            context.startActivity(intent)
        }
        ResourceType.CHAT -> {
            // Open chat application or web chat
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(resource.contact)
            }
            context.startActivity(intent)
        }
        ResourceType.LEGAL_AID -> {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${resource.contact}")
            }
            context.startActivity(intent)
        }
    }
}

/**
 * Elder Rights Advocacy Manager
 * Handles advocacy notifications and resource management
 */
class ElderRightsAdvocacyManager(private val context: Context) {
    
    fun notifyAdvocate(
        userId: String,
        eventType: String,
        urgency: AdvocacyUrgency,
        details: Map<String, Any>
    ) {
        when (urgency) {
            AdvocacyUrgency.IMMEDIATE -> {
                // Send immediate notification to advocate
                sendImmediateNotification(userId, eventType, details)
            }
            AdvocacyUrgency.URGENT -> {
                // Send urgent notification within 1 hour
                scheduleUrgentNotification(userId, eventType, details)
            }
            AdvocacyUrgency.ROUTINE -> {
                // Add to routine monitoring report
                addToRoutineReport(userId, eventType, details)
            }
        }
    }
    
    private fun sendImmediateNotification(userId: String, eventType: String, details: Map<String, Any>) {
        // Implementation would send immediate notification to elder rights advocate
        // Could use SMS, email, push notification, or direct API call
    }
    
    private fun scheduleUrgentNotification(userId: String, eventType: String, details: Map<String, Any>) {
        // Implementation would schedule notification for urgent but not immediate issues
    }
    
    private fun addToRoutineReport(userId: String, eventType: String, details: Map<String, Any>) {
        // Implementation would add to routine monitoring report
    }
    
    fun isAdvocateContactProtected(): Boolean {
        // Elder rights advocate contact is always protected from caregiver removal
        return true
    }
}

enum class AdvocacyUrgency {
    IMMEDIATE,  // Emergency situations requiring immediate intervention
    URGENT,     // Serious concerns requiring response within hours
    ROUTINE     // Regular monitoring and support needs
}
