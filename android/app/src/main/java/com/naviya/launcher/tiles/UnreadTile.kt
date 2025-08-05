package com.naviya.launcher.tiles

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.naviya.launcher.safety.ElderProtectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Implementation of the Unread Tile feature for the Naviya Launcher.
 * Shows total count of missed calls + unread SMS in a large tile.
 * Works offline using local call log and SMS inbox access.
 * 
 * This implementation follows the rule specification:
 * - Triggered on launcher home opened and app resume
 * - Checks missed calls and unread SMS counts
 * - Updates tile with combined count
 * - Shows reminder for unread communications
 * - Indicates caregiver availability status
 */
class UnreadTile(private val context: Context) {

    companion object {
        private const val TAG = "UnreadTile"
    }
    
    // Elder protection manager for safety features
    private val elderProtectionManager = ElderProtectionManager.getInstance(context)

    private val tileManager = UnreadTileManager.getInstance(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // LiveData for the tile state
    private val _tileState = MutableLiveData<TileState>()
    val tileState: LiveData<TileState> = _tileState
    
    // LiveData for reminder notifications
    private val _reminder = MutableLiveData<String?>()
    val reminder: LiveData<String?> = _reminder
    
    /**
     * Called when the launcher home is opened
     */
    fun onLauncherHomeOpened(caregiverOnline: Boolean = true) {
        Log.d(TAG, "Launcher home opened, updating unread tile")
        refresh(caregiverOnline)
    }
    
    /**
     * Called when the app resumes
     */
    fun onAppResume(caregiverOnline: Boolean) {
        Log.d(TAG, "App resumed, updating unread tile")
        refresh(caregiverOnline)
    }
    
    /**
     * Refresh the tile with current data
     * 
     * @param caregiverOnline Whether the caregiver is currently online
     */
    fun refresh(caregiverOnline: Boolean) {
        coroutineScope.launch {
            try {
                // Check if panic mode is active - if so, don't show any data
                if (elderProtectionManager.isPanicModeActive()) {
                    Log.d(TAG, "Panic mode active - not showing unread counts")
                    _tileState.postValue(TileState(
                        title = "Unread",
                        icon = "envelope",
                        badgeCount = 0,
                        note = "Privacy mode active"
                    ))
                    return@launch
                }
                
                val unreadTileManager = UnreadTileManager.getInstance(context)
                
                // Get unread counts
                val callCount = withContext(Dispatchers.IO) { unreadTileManager.readMissedCalls() }
                val smsCount = withContext(Dispatchers.IO) { unreadTileManager.readUnreadSms() }
                val totalUnread = callCount + smsCount
                
                Log.d(TAG, "Refreshed unread counts: calls=$callCount, SMS=$smsCount, total=$totalUnread")
                
                // Record this as a communication monitoring action for safety auditing
                if (caregiverOnline) {
                    elderProtectionManager.recordCaregiverAction(
                        ElderProtectionManager.CaregiverActionType.COMMUNICATION_MONITORING,
                        "Viewed unread communications: $totalUnread items"
                    )
                }
                
                // Update tile state
                val tileState = TileState(
                    title = "Unread",
                    icon = "envelope",
                    badgeCount = totalUnread,
                    note = if (!caregiverOnline) "Caregiver not available." else null
                )
                
                _tileState.postValue(tileState)
                
                // Show reminder if there are unread items
                if (totalUnread > 0) {
                    _reminder.postValue("You have missed calls or messages.")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing unread tile", e)
                _tileState.postValue(TileState(title = "Unread", icon = "envelope", badgeCount = 0))
            }
        }
    }
    
    /**
     * Clear the current reminder
     */
    fun clearReminder() {
        _reminder.postValue(null)
    }
    
    /**
     * Toggle panic mode (privacy protection)
     * Used to quickly disable all monitoring when needed
     * 
     * @param activate Whether to activate or deactivate panic mode
     * @param reason The reason for the change
     */
    fun togglePanicMode(activate: Boolean, reason: String) {
        if (activate) {
            elderProtectionManager.activatePanicMode("Unread Tile: $reason")
            _tileState.postValue(TileState(
                title = "Unread",
                icon = "envelope",
                badgeCount = 0,
                note = "Privacy mode active"
            ))
        } else {
            elderProtectionManager.deactivatePanicMode("User via Unread Tile")
            // Refresh with default state after deactivating
            refresh(true)
        }
    }
    
    /**
     * Data class representing the state of the tile
     */
    data class TileState(
        val title: String,
        val icon: String,
        val badgeCount: Int,
        val note: String? = null
    )
}
