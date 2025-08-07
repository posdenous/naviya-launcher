package com.naviya.launcher.unread

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event handler for UnreadTile that responds to system events
 * Follows the requirements in unread-tile.md:
 * - Responds to launcher_home_opened event
 * - Responds to app_resume event
 * - Updates the unread tile with current data
 */
@Singleton
class UnreadTileEventHandler @Inject constructor(
    private val unreadTileService: UnreadTileService
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    /**
     * Handle launcher_home_opened event
     * Updates the unread tile when the launcher home is opened
     */
    fun onLauncherHomeOpened() {
        scope.launch {
            unreadTileService.onLauncherHomeOpened()
        }
    }
    
    /**
     * Handle app_resume event
     * Updates the unread tile when the app resumes
     */
    fun onAppResume() {
        scope.launch {
            unreadTileService.onAppResume()
        }
    }
    
    /**
     * Initialize the event handler
     * Performs initial update of the unread tile
     */
    fun initialize() {
        scope.launch {
            unreadTileService.updateTileData()
        }
    }
}
