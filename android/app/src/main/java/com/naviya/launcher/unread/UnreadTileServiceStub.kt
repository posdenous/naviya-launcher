package com.naviya.launcher.unread

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Temporary stub for UnreadTileService to fix compilation
 * TODO: Re-enable full implementation when Hilt is restored
 */
class UnreadTileService {
    private val _tileData = MutableStateFlow(UnreadTileData(0, 0, false))
    val tileData: StateFlow<UnreadTileData> = _tileData
    
    fun getUnreadBreakdown(): String = "0 missed calls, 0 unread messages"
    
    suspend fun updateTileData() {
        // Stub implementation
    }
}

data class UnreadTileData(
    val totalUnread: Int,
    val missedCalls: Int,
    val caregiverOnline: Boolean
)
