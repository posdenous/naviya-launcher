package com.naviya.launcher.unread

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import android.provider.Telephony
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing unread tile logic following user rules:
 * - Show total count of missed calls + unread SMS in a large tile
 * - Works offline using local call log and SMS inbox access
 * - Updates on launcher_home_opened and app_resume events
 * - Shows reminder if total_unread > 0
 * - Adds caregiver availability note if offline
 */
@Singleton
class UnreadTileService @Inject constructor(
    private val context: Context
) {
    private val _tileData = MutableStateFlow(UnreadTileData(0, 0, false))
    val tileData: StateFlow<UnreadTileData> = _tileData
    
    private val _reminderText = MutableStateFlow<String?>(null)
    val reminderText: StateFlow<String?> = _reminderText
    
    private val _noteText = MutableStateFlow<String?>(null)
    val noteText: StateFlow<String?> = _noteText
    
    /**
     * Returns a human-readable breakdown of unread communications
     * Designed for elderly users with clear, simple language
     */
    fun getUnreadBreakdown(): String {
        val calls = _tileData.value.missedCalls
        val messages = _tileData.value.totalUnread - calls
        return "$calls missed calls, $messages unread messages"
    }
    
    /**
     * Updates the tile data by reading missed calls and unread SMS
     * Works offline using local device data
     */
    suspend fun updateTileData() {
        withContext(Dispatchers.IO) {
            try {
                // Read missed calls and unread SMS as per user rules
                val callCount = readMissedCalls()
                val smsCount = readUnreadSms()
                val totalUnread = callCount + smsCount
                val caregiverOnline = checkCaregiverStatus()
                
                _tileData.value = UnreadTileData(totalUnread, callCount, caregiverOnline)
                
                // Update tile with new information
                updateTile("Unread", "envelope", totalUnread)
                
                // Show reminder if there are unread items
                if (totalUnread > 0) {
                    showReminder("You have missed calls or messages.")
                } else {
                    _reminderText.value = null
                }
                
                // Add note if caregiver is offline
                if (!caregiverOnline) {
                    addNoteToTile("Caregiver not available.")
                } else {
                    _noteText.value = null
                }
            } catch (e: Exception) {
                // Handle errors gracefully - don't crash the launcher
                android.util.Log.e("UnreadTile", "Error updating tile data", e)
            }
        }
    }
    
    /**
     * Reads missed calls from call log
     * Requires READ_CALL_LOG permission
     */
    fun readMissedCalls(): Int {
        if (!hasCallLogPermission()) {
            return 0
        }
        
        var cursor: Cursor? = null
        var count = 0
        
        try {
            val uri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(CallLog.Calls._ID)
            val selection = "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.NEW} = ?"
            val selectionArgs = arrayOf(
                CallLog.Calls.MISSED_TYPE.toString(),
                "1" // 1 means new/unread
            )
            
            cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )
            
            count = cursor?.count ?: 0
        } catch (e: Exception) {
            android.util.Log.e("UnreadTile", "Error reading missed calls", e)
        } finally {
            cursor?.close()
        }
        
        return count
    }
    
    /**
     * Reads unread SMS from inbox
     * Requires READ_SMS permission
     */
    fun readUnreadSms(): Int {
        if (!hasSmsPermission()) {
            return 0
        }
        
        var cursor: Cursor? = null
        var count = 0
        
        try {
            val uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(Telephony.Sms._ID)
            val selection = "${Telephony.Sms.READ} = ?"
            val selectionArgs = arrayOf("0") // 0 means unread
            
            cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )
            
            count = cursor?.count ?: 0
        } catch (e: Exception) {
            android.util.Log.e("UnreadTile", "Error reading unread SMS", e)
        } finally {
            cursor?.close()
        }
        
        return count
    }
    
    /**
     * Updates the tile with new information
     */
    fun updateTile(title: String, icon: String, badge: Int) {
        // This is handled through the StateFlow updates
        android.util.Log.i("UnreadTile", "Updating tile: $title with badge $badge")
    }
    
    /**
     * Shows a reminder to the user
     */
    fun showReminder(message: String) {
        _reminderText.value = message
        android.util.Log.i("UnreadTile", "Showing reminder: $message")
    }
    
    /**
     * Adds a note to the tile
     */
    fun addNoteToTile(note: String) {
        _noteText.value = note
        android.util.Log.i("UnreadTile", "Adding note: $note")
    }
    
    /**
     * Check if we have call log permission
     */
    private fun hasCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if we have SMS permission
     */
    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if we have all required permissions
     */
    fun hasRequiredPermissions(): Boolean {
        return hasCallLogPermission() && hasSmsPermission()
    }
    
    /**
     * Get required permissions for requesting
     */
    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS
        )
    }
    
    /**
     * Handle launcher_home_opened event
     */
    suspend fun onLauncherHomeOpened() {
        updateTileData()
    }
    
    /**
     * Handle app_resume event
     */
    suspend fun onAppResume() {
        updateTileData()
    }
    
    /**
     * Check caregiver status
     * In a real implementation, this would check if the caregiver is online
     */
    private fun checkCaregiverStatus(): Boolean {
        // This is a stub implementation
        // In a real implementation, this would check if the caregiver is online
        // through a network call or local cache
        return false // Assume caregiver is offline for demonstration
    }
}

/**
 * Data class representing the state of the unread tile
 * Designed for elderly users with focus on missed communications
 */
data class UnreadTileData(
    val totalUnread: Int,
    val missedCalls: Int,
    val caregiverOnline: Boolean
)
