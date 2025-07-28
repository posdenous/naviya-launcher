package com.naviya.launcher

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.naviya.launcher.data.AppDatabase
import com.naviya.launcher.emergency.EmergencyService
import com.naviya.launcher.layout.LayoutManager
import com.naviya.launcher.layout.data.LayoutConfiguration
import com.naviya.launcher.layout.data.AppTile
import com.naviya.launcher.toggle.ToggleMode
import com.naviya.launcher.unread.UnreadTileService
import javax.inject.Inject

/**
 * ViewModel for the main launcher UI that manages state integration between
 * Emergency SOS System and Launcher Layout Engine.
 * 
 * Responsibilities:
 * - Manage current toggle mode and layout configuration
 * - Track app tiles and their positions
 * - Monitor connectivity and unread notifications
 * - Handle mode switching and app launch analytics
 * - Coordinate between emergency service and layout manager
 * 
 * Follows Windsurf rules for:
 * - Reactive state management with Flow
 * - Proper lifecycle handling
 * - Error handling and offline support
 * - Performance optimization for elderly devices
 */
@HiltViewModel
class MainLauncherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val layoutManager: LayoutManager,
    private val emergencyService: EmergencyService,
    private val database: AppDatabase,
    private val unreadTileService: UnreadTileService
) : ViewModel() {
    
    // Internal mutable state
    private val _currentMode = MutableStateFlow(ToggleMode.COMFORT)
    private val _isOnline = MutableStateFlow(true)
    private val _unreadCount = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    
    // Current layout configuration from layout manager
    private val currentLayoutFlow = layoutManager.getCurrentLayout()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // App tiles from layout manager
    private val appTilesFlow = layoutManager.getAppTiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Combined UI state
    val uiState: StateFlow<MainLauncherUiState> = combine(
        _currentMode,
        _isOnline,
        _unreadCount,
        _isLoading,
        _errorMessage,
        currentLayoutFlow,
        appTilesFlow
    ) { currentMode, isOnline, unreadCount, isLoading, errorMessage, layout, appTiles ->
        MainLauncherUiState(
            currentMode = currentMode,
            isOnline = isOnline,
            unreadCount = unreadCount,
            isLoading = isLoading,
            errorMessage = errorMessage,
            currentLayout = layout,
            appTiles = appTiles
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainLauncherUiState()
    )
    
    init {
        initializeViewModel()
    }
    
    private fun initializeViewModel() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Initialize layout manager and get current mode
                val currentMode = layoutManager.getCurrentMode()
                _currentMode.value = currentMode
                
                // Start monitoring connectivity
                startConnectivityMonitoring()
                
                // Start monitoring unread notifications
                startUnreadNotificationMonitoring()
                
                // Initialize emergency service state
                initializeEmergencyService()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize launcher: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun startConnectivityMonitoring() {
        viewModelScope.launch {
            // Monitor network connectivity
            // In a real implementation, this would use ConnectivityManager
            // For now, we'll simulate connectivity monitoring
            kotlinx.coroutines.delay(1000)
            _isOnline.value = true
        }
    }
    
    private fun startUnreadNotificationMonitoring() {
        viewModelScope.launch {
            // Initialize unread tile service following user rules
            try {
                unreadTileService.initialize()
                
                // Monitor unread count from service
                unreadTileService.unreadCount.collect { count ->
                    _unreadCount.value = count
                }
                
            } catch (e: Exception) {
                // Handle error silently for unread monitoring
                _unreadCount.value = 0
            }
        }
    }
    
    /**
     * Handle launcher home opened event (user rule)
     */
    fun onLauncherHomeOpened() {
        viewModelScope.launch {
            unreadTileService.onLauncherHomeOpened()
        }
    }
    
    /**
     * Handle app resume event (user rule)
     */
    fun onAppResume() {
        viewModelScope.launch {
            unreadTileService.onAppResume()
        }
    }
    
    /**
     * Update caregiver online status
     */
    fun updateCaregiverStatus(isOnline: Boolean) {
        _isOnline.value = isOnline
        unreadTileService.updateCaregiverStatus(isOnline)
    }
    
    private fun initializeEmergencyService() {
        viewModelScope.launch {
            try {
                // Ensure emergency service is properly initialized
                emergencyService.initialize()
            } catch (e: Exception) {
                _errorMessage.value = "Emergency service initialization failed: ${e.message}"
            }
        }
    }
    
    /**
     * Update the current toggle mode
     */
    fun updateCurrentMode(mode: ToggleMode) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _currentMode.value = mode
                
                // Clear any previous error
                _errorMessage.value = null
                
                // Log mode change for analytics
                logModeChange(mode)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update mode: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Log app launch for analytics
     */
    fun logAppLaunch(packageName: String) {
        viewModelScope.launch {
            try {
                // Log app launch event
                database.analyticsDao().insertAppLaunchEvent(
                    packageName = packageName,
                    timestamp = System.currentTimeMillis(),
                    mode = _currentMode.value.name
                )
            } catch (e: Exception) {
                // Log error but don't show to user
                // Analytics failures shouldn't interrupt user experience
            }
        }
    }
    
    private fun logModeChange(mode: ToggleMode) {
        viewModelScope.launch {
            try {
                // Log mode change event
                database.analyticsDao().insertModeChangeEvent(
                    fromMode = _currentMode.value.name,
                    toMode = mode.name,
                    timestamp = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                // Log error but don't show to user
            }
        }
    }
    
    /**
     * Refresh the launcher state
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Refresh layout manager
                layoutManager.refreshCurrentLayout()
                
                // Refresh unread count
                startUnreadNotificationMonitoring()
                
                // Check connectivity
                startConnectivityMonitoring()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to refresh: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Handle error dismissal
     */
    fun dismissError() {
        _errorMessage.value = null
    }
    
    /**
     * Get accessibility description for current state
     */
    fun getAccessibilityDescription(): String {
        val state = uiState.value
        return buildString {
            append("Naviya Launcher. ")
            append("Current mode: ${state.currentMode.name}. ")
            if (state.unreadCount > 0) {
                append("${state.unreadCount} unread notifications. ")
            }
            append(if (state.isOnline) "Online. " else "Offline. ")
            append("Emergency button available at bottom.")
        }
    }
}

/**
 * UI state data class for the main launcher
 */
data class MainLauncherUiState(
    val currentMode: ToggleMode = ToggleMode.COMFORT,
    val isOnline: Boolean = true,
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentLayout: LayoutConfiguration? = null,
    val appTiles: List<AppTile> = emptyList()
) {
    /**
     * Check if the launcher is in a ready state
     */
    val isReady: Boolean
        get() = !isLoading && errorMessage == null && currentLayout != null
    
    /**
     * Get the number of visible tiles
     */
    val visibleTileCount: Int
        get() = appTiles.size
    
    /**
     * Check if emergency features are available
     */
    val emergencyAvailable: Boolean
        get() = isReady // Emergency should always be available when launcher is ready
}
