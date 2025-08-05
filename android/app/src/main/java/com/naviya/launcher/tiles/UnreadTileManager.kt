package com.naviya.launcher.tiles

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages the Unread Tile functionality for the Naviya Launcher.
 * Shows total count of missed calls + unread SMS in a large tile.
 * Works offline using local call log and SMS inbox access.
 */
class UnreadTileManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UnreadTileManager"
        
        // Singleton instance with lazy initialization
        @Volatile
        private var INSTANCE: UnreadTileManager? = null
        
        fun getInstance(context: Context): UnreadTileManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UnreadTileManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Reads the number of missed calls from the call log.
     * Works offline using local device data.
     * 
     * @return The count of missed calls
     */
    suspend fun readMissedCalls(): Int = withContext(Dispatchers.IO) {
        var cursor: Cursor? = null
        var missedCallCount = 0
        
        try {
            // Query for missed calls only (type = 3)
            val projection = arrayOf(CallLog.Calls._ID)
            val selection = "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.NEW} = ?"
            val selectionArgs = arrayOf(CallLog.Calls.MISSED_TYPE.toString(), "1")
            
            cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )
            
            missedCallCount = cursor?.count ?: 0
            Log.d(TAG, "Found $missedCallCount missed calls")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for reading call log", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading missed calls", e)
        } finally {
            cursor?.close()
        }
        
        return@withContext missedCallCount
    }
    
    /**
     * Reads the number of unread SMS messages from the inbox.
     * Works offline using local device data.
     * 
     * @return The count of unread SMS messages
     */
    suspend fun readUnreadSms(): Int = withContext(Dispatchers.IO) {
        var cursor: Cursor? = null
        var unreadSmsCount = 0
        
        try {
            // Query for unread SMS messages only
            val projection = arrayOf(Telephony.Sms._ID)
            val selection = "${Telephony.Sms.READ} = ?"
            val selectionArgs = arrayOf("0")  // 0 = unread
            
            cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )
            
            unreadSmsCount = cursor?.count ?: 0
            Log.d(TAG, "Found $unreadSmsCount unread SMS messages")
            
        } catch (SecurityException) {
            Log.e(TAG, "Permission denied for reading SMS", SecurityException())
        } catch (e: Exception) {
            Log.e(TAG, "Error reading unread SMS", e)
        } finally {
            cursor?.close()
        }
        
        return@withContext unreadSmsCount
    }
    
    /**
     * Updates the unread tile with the current count of missed calls and unread SMS.
     * Also shows a reminder if there are any unread communications.
     * 
     * @param updateTileCallback Callback to update the tile UI
     * @param showReminderCallback Callback to show a reminder notification
     * @param isCaregiverOnline Whether the caregiver is currently online
     */
    suspend fun updateUnreadTile(
        updateTileCallback: (String, String, Int) -> Unit,
        showReminderCallback: (String) -> Unit,
        isCaregiverOnline: Boolean = true
    ) {
        val missedCallCount = readMissedCalls()
        val unreadSmsCount = readUnreadSms()
        val totalUnread = missedCallCount + unreadSmsCount
        
        // Update the tile with the total unread count
        updateTileCallback("Unread", "envelope", totalUnread)
        
        // Show a reminder if there are unread communications
        if (totalUnread > 0) {
            showReminderCallback("You have missed calls or messages.")
        }
        
        // Add a note if caregiver is offline
        if (!isCaregiverOnline) {
            updateTileCallback("Unread", "envelope", totalUnread, "Caregiver not available.")
        }
    }
    
    /**
     * Overloaded method to add a note to the tile
     */
    private fun updateTileCallback(
        title: String, 
        icon: String, 
        badge: Int,
        note: String? = null
    ) {
        // This would be implemented by the caller
    }
}
