package com.naviya.launcher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naviya.launcher.unread.UnreadTileEventHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the launcher screen that handles lifecycle events
 * and coordinates with various services including the UnreadTileEventHandler.
 */
@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val unreadTileEventHandler: UnreadTileEventHandler
) : ViewModel() {

    init {
        // Initialize the unread tile handler
        unreadTileEventHandler.initialize()
    }

    /**
     * Called when the launcher home screen is opened
     * Triggers the unread tile update as specified in unread-tile.md
     */
    fun onLauncherHomeOpened() {
        unreadTileEventHandler.onLauncherHomeOpened()
    }

    /**
     * Called when the app resumes from background
     * Triggers the unread tile update as specified in unread-tile.md
     */
    fun onAppResume() {
        unreadTileEventHandler.onAppResume()
    }
}
