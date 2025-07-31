package com.naviya.launcher.ui.security

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.naviya.launcher.R
import kotlinx.coroutines.delay
import java.util.*

/**
 * Emergency escape widget for elderly users
 * Provides multiple accessible ways to disable monitoring:
 * - Triple-tap gesture
 * - Voice command detection
 * - Long press emergency button
 * - Shake gesture (if device supports)
 */
@Composable
fun EmergencyEscapeWidget(
    onEmergencyEscape: (method: String) -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tapCount by remember { mutableStateOf(0) }
    var isListening by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    var emergencyMethod by remember { mutableStateOf("") }
    
    val pulseAnimation = rememberInfiniteTransition()
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Emergency escape confirmation dialog
    if (showConfirmation) {
        EmergencyEscapeConfirmationDialog(
            method = emergencyMethod,
            onConfirm = {
                onEmergencyEscape(emergencyMethod)
                showConfirmation = false
                triggerEmergencyFeedback(context)
            },
            onCancel = {
                showConfirmation = false
                emergencyMethod = ""
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Emergency Status Indicator
        if (isActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_emergency_active),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.emergency_escape_active),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Triple-Tap Detection Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            tapCount++
                            if (tapCount == 3) {
                                emergencyMethod = "TRIPLE_TAP"
                                showConfirmation = true
                                tapCount = 0
                            }
                        }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = if (tapCount > 0) Color(0xFFFFE0B2) else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_tap_gesture),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.emergency_triple_tap_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.emergency_triple_tap_description),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (tapCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.emergency_tap_count, tapCount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Voice Command Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isListening) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_voice_command),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.emergency_voice_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.emergency_voice_description),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        isListening = !isListening
                        if (isListening) {
                            startVoiceListening(context) { command ->
                                if (command.contains("emergency", ignoreCase = true) || 
                                    command.contains("help", ignoreCase = true) ||
                                    command.contains("stop", ignoreCase = true)) {
                                    emergencyMethod = "VOICE_COMMAND"
                                    showConfirmation = true
                                    isListening = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) Color(0xFFFF5722) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        painter = painterResource(
                            if (isListening) R.drawable.ic_mic_on else R.drawable.ic_mic_off
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (isListening) R.string.emergency_voice_listening else R.string.emergency_voice_start
                        ),
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Emergency Button (Long Press)
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(if (isActive) pulseScale else 1f)
                .clip(CircleShape)
                .background(Color(0xFFFF1744))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            emergencyMethod = "LONG_PRESS"
                            showConfirmation = true
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_emergency),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.emergency_button_text),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.emergency_button_instruction),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    // Reset tap count after delay
    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            delay(2000) // Reset after 2 seconds
            tapCount = 0
        }
    }
}

/**
 * Emergency escape confirmation dialog
 */
@Composable
private fun EmergencyEscapeConfirmationDialog(
    method: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = stringResource(R.string.emergency_confirm_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.emergency_confirm_message),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.emergency_confirm_method, getMethodName(method)),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF1744)
                )
            ) {
                Text(
                    text = stringResource(R.string.emergency_confirm_yes),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.emergency_confirm_no))
            }
        }
    )
}

/**
 * Get user-friendly method name
 */
@Composable
private fun getMethodName(method: String): String {
    return when (method) {
        "TRIPLE_TAP" -> stringResource(R.string.emergency_method_triple_tap)
        "VOICE_COMMAND" -> stringResource(R.string.emergency_method_voice)
        "LONG_PRESS" -> stringResource(R.string.emergency_method_long_press)
        "SHAKE" -> stringResource(R.string.emergency_method_shake)
        else -> method
    }
}

/**
 * Trigger emergency feedback (vibration, sound, TTS)
 */
private fun triggerEmergencyFeedback(context: Context) {
    // Vibration feedback
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
    vibrator?.let {
        val effect = VibrationEffect.createWaveform(
            longArrayOf(0, 200, 100, 200, 100, 200),
            -1
        )
        it.vibrate(effect)
    }
    
    // TTS announcement
    val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.speak(
                "Emergency escape activated. Monitoring disabled.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "emergency_escape"
            )
        }
    }
}

/**
 * Start voice command listening (simplified implementation)
 */
private fun startVoiceListening(context: Context, onCommand: (String) -> Unit) {
    // This would integrate with Android's SpeechRecognizer
    // For now, we'll simulate with a timer
    // In real implementation, this would use SpeechRecognizer API
}

/**
 * Emergency escape manager for handling all escape methods
 */
class EmergencyEscapeManager(private val context: Context) {
    
    fun activateEmergencyEscape(method: String, onActivated: () -> Unit) {
        // Log the emergency escape
        logEmergencyEscape(method)
        
        // Disable all monitoring
        disableAllMonitoring()
        
        // Notify elder rights advocate
        notifyElderRightsAdvocate()
        
        // Provide user feedback
        triggerEmergencyFeedback(context)
        
        onActivated()
    }
    
    private fun logEmergencyEscape(method: String) {
        // Implementation would log to SecurityAuditDao
    }
    
    private fun disableAllMonitoring() {
        // Implementation would disable caregiver monitoring
    }
    
    private fun notifyElderRightsAdvocate() {
        // Implementation would send notification to elder rights advocate
    }
}
