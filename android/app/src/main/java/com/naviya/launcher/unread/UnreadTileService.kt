package com.naviya.launcher.unread

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.Telephony
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    @ApplicationContext private val context: Context
) {
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _missedCallsCount = MutableStateFlow(0)
    val missedCallsCount: StateFlow<Int> = _missedCallsCount.asStateFlow()
    
    private val _unreadSmsCount = MutableStateFlow(0)
    val unreadSmsCount: StateFlow<Int> = _unreadSmsCount.asStateFlow()
    
    private val _caregiverOnline = MutableStateFlow(true)
    val caregiverOnline: StateFlow<Boolean> = _caregiverOnline.asStateFlow()
    
    private val _tileData = MutableStateFlow(UnreadTileData())
    val tileData: StateFlow<UnreadTileData> = _tileData.asStateFlow()
    
    /**
     * Initialize unread tile service and check permissions
     */
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            updateUnreadCounts()
        }
    }
    
    /**
     * Update unread counts following user rules logic
     */
    suspend fun updateUnreadCounts() {
        withContext(Dispatchers.IO) {
            try {
                // Read missed calls and unread SMS as per user rules
                val callCount = readMissedCalls()
                val smsCount = readUnreadSms()
                val totalUnread = callCount + smsCount
                
                // Update state flows
                _missedCallsCount.value = callCount
                _unreadSmsCount.value = smsCount
                _unreadCount.value = totalUnread
                
                // Update tile following user rules logic
                updateTile(totalUnread)
                
                // Show reminder if total_unread > 0
                if (totalUnread > 0) {
                    showUnreadReminder()
                }
                
                // Add caregiver note if offline
                if (!_caregiverOnline.value) {
                    addCaregiverNote()
                }
                
            } catch (e: Exception) {
                // Handle errors gracefully - don't crash the launcher
                _unreadCount.value = 0
                _tileData.value = UnreadTileData(
                    title = "Unread",
                    icon = "envelope",
                    badge = 0,
                    hasError = true,
                    errorMessage = "Permission required to read notifications"
                )
            }
        }
    }
    
    /**
     * Read missed calls from call log
     */
    private suspend fun readMissedCalls(): Int {
        return withContext(Dispatchers.IO) {
            if (!hasCallLogPermission()) {
                return@withContext 0
            }
            
            try {
                val uri = CallLog.Calls.CONTENT_URI
                val projection = arrayOf(
                    CallLog.Calls.TYPE,
                    CallLog.Calls.IS_READ
                )
                val selection = "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.IS_READ} = ?"
                val selectionArgs = arrayOf(
                    CallLog.Calls.MISSED_TYPE.toString(),
                    "0" // Unread
                )
                
                val cursor: Cursor? = context.contentResolver.query(
                    uri, projection, selection, selectionArgs, null
                )
                
                val count = cursor?.count ?: 0
                cursor?.close()
                
                return@withContext count
                
            } catch (e: Exception) {
                return@withContext 0
            }
        }
    }
    
    /**
     * Read unread SMS from inbox
     */
    private suspend fun readUnreadSms(): Int {
        return withContext(Dispatchers.IO) {
            if (!hasSmsPermission()) {
                return@withContext 0
            }
            
            try {
                val uri = Telephony.Sms.Inbox.CONTENT_URI
                val projection = arrayOf(Telephony.Sms.Inbox.READ)
                val selection = "${Telephony.Sms.Inbox.READ} = ?"
                val selectionArgs = arrayOf("0") // Unread
                
                val cursor: Cursor? = context.contentResolver.query(
                    uri, projection, selection, selectionArgs, null
                )
                
                val count = cursor?.count ?: 0
                cursor?.close()
                
                return@withContext count
                
            } catch (e: Exception) {
                return@withContext 0
            }
        }
    }
    
    /**
     * Update tile following user rules format
     */
    private fun updateTile(totalUnread: Int) {
        _tileData.value = UnreadTileData(
            title = "Unread",
            icon = "envelope",
            badge = totalUnread,
            hasReminder = totalUnread > 0,
            reminderText = if (totalUnread > 0) "You have missed calls or messages." else null,
            caregiverNote = if (!_caregiverOnline.value) "Caregiver not available." else null
        )
    }
    
    /**
     * Show reminder when total_unread > 0
     */
    private fun showUnreadReminder() {
        // This would trigger a notification or UI reminder
        // Implementation depends on notification system
    }
    
    /**
     * Add caregiver availability note when offline
     */
    private fun addCaregiverNote() {
        val currentData = _tileData.value
        _tileData.value = currentData.copy(
            caregiverNote = "Caregiver not available."
        )
    }
    
    /**
     * Update caregiver online status
     */
    fun updateCaregiverStatus(isOnline: Boolean) {
        _caregiverOnline.value = isOnline
        
        // Update tile with caregiver note if offline
        if (!isOnline) {
            addCaregiverNote()
        } else {
            // Remove caregiver note if back online
            val currentData = _tileData.value
            _tileData.value = currentData.copy(caregiverNote = null)
        }
    }
    
    /**
     * Handle launcher_home_opened event
     */
    suspend fun onLauncherHomeOpened() {
        updateUnreadCounts()
    }
    
    /**
     * Handle app_resume event
     */
    suspend fun onAppResume() {
        updateUnreadCounts()
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
     * Get detailed unread breakdown for accessibility
     */
    fun getUnreadBreakdown(): UnreadBreakdown {
        return UnreadBreakdown(
            missedCalls = _missedCallsCount.value,
            unreadSms = _unreadSmsCount.value,
            total = _unreadCount.value
        )
    }
}

/**
 * Data class for unread tile information
 */
data class UnreadTileData(
    val title: String = "Unread",
    val icon: String = "envelope",
    val badge: Int = 0,
    val hasReminder: Boolean = false,
    val reminderText: String? = null,
    val caregiverNote: String? = null,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Data class for unread breakdown
 */
data class UnreadBreakdown(
    val missedCalls: Int,
    val unreadSms: Int,
    val total: Int
) {
    fun getAccessibilityDescription(): String {
        return when {
            total == 0 -> "No unread notifications"
            missedCalls > 0 && unreadSms > 0 -> "$missedCalls missed calls and $unreadSms unread messages"
            missedCalls > 0 -> "$missedCalls missed calls"
            unreadSms > 0 -> "$unreadSms unread messages"
            else -> "$total unread notifications"
        }
    }
}
