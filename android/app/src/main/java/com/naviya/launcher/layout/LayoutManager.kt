package com.naviya.launcher.layout

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.naviya.launcher.layout.data.*
import com.naviya.launcher.toggle.ToggleMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Layout Manager for Naviya Launcher
 * Orchestrates layout generation, persistence, and mode switching
 * Follows Windsurf rules for elderly accessibility and cognitive load management
 */
@Singleton
class LayoutManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val layoutEngine: LauncherLayoutEngine,
    private val layoutDao: LayoutDao,
    private val gson: Gson
) {
    
    companion object {
        private const val TAG = "LayoutManager"
        private const val DEFAULT_USER_ID = "default_user"
    }
    
    private val _currentMode = MutableStateFlow(ToggleMode.getDefaultMode())
    val currentMode: Flow<ToggleMode> = _currentMode.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    private val _layoutError = MutableStateFlow<String?>(null)
    val layoutError: Flow<String?> = _layoutError.asStateFlow()
    
    /**
     * Initialize layout manager with user preferences
     */
    suspend fun initialize(userId: String = DEFAULT_USER_ID) {
        try {
            _isLoading.value = true
            _layoutError.value = null
            
            Log.i(TAG, "Initializing LayoutManager for user: $userId")
            
            // Load user preferences
            val preferences = layoutDao.getLayoutPreferences(userId)
            if (preferences != null) {
                val mode = ToggleMode.fromString(preferences.preferredMode) ?: ToggleMode.getDefaultMode()
                _currentMode.value = mode
                Log.i(TAG, "Loaded user preference for mode: $mode")
            } else {
                // Create default preferences for new user
                val defaultPreferences = createDefaultPreferences(userId)
                layoutDao.insertLayoutPreferences(defaultPreferences)
                Log.i(TAG, "Created default preferences for new user")
            }
            
            // Initialize app tiles if not exists
            initializeAppTiles()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize LayoutManager", e)
            _layoutError.value = "Failed to initialize: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Switch to a different toggle mode
     */
    suspend fun switchToMode(
        mode: ToggleMode,
        screenWidth: Int,
        screenHeight: Int,
        userId: String = DEFAULT_USER_ID
    ): LayoutConfiguration? {
        try {
            _isLoading.value = true
            _layoutError.value = null
            
            Log.i(TAG, "Switching to mode: $mode")
            
            // Check if we have a saved layout for this mode
            val savedLayout = layoutDao.getDefaultLayoutForMode(userId, mode.name)
            
            val layoutConfig = if (savedLayout != null) {
                // Use saved layout
                gson.fromJson(savedLayout.layoutJson, LayoutConfiguration::class.java)
            } else {
                // Generate new layout
                val availableApps = getAvailableAppsForMode(mode)
                val generatedLayout = layoutEngine.generateLayoutForMode(mode, screenWidth, screenHeight, availableApps)
                
                // Save the generated layout
                saveLayout(userId, generatedLayout, "Default ${mode.displayName}", isDefault = true)
                
                generatedLayout
            }
            
            // Update current mode
            _currentMode.value = mode
            
            // Update user preferences
            updateUserPreferences(userId, mode)
            
            Log.i(TAG, "Successfully switched to mode: $mode")
            return layoutConfig
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to mode: $mode", e)
            _layoutError.value = "Failed to switch mode: ${e.message}"
            return null
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Get current layout configuration
     */
    suspend fun getCurrentLayout(
        screenWidth: Int,
        screenHeight: Int,
        userId: String = DEFAULT_USER_ID
    ): LayoutConfiguration? {
        return try {
            val currentMode = _currentMode.value
            val savedLayout = layoutDao.getDefaultLayoutForMode(userId, currentMode.name)
            
            if (savedLayout != null) {
                gson.fromJson(savedLayout.layoutJson, LayoutConfiguration::class.java)
            } else {
                // Generate layout for current mode
                val availableApps = getAvailableAppsForMode(currentMode)
                layoutEngine.generateLayoutForMode(currentMode, screenWidth, screenHeight, availableApps)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current layout", e)
            null
        }
    }
    
    /**
     * Save a custom layout configuration
     */
    suspend fun saveLayout(
        userId: String,
        layout: LayoutConfiguration,
        name: String,
        isDefault: Boolean = false
    ): String {
        val layoutId = "${userId}_${layout.mode.name}_${System.currentTimeMillis()}"
        
        if (isDefault) {
            // Clear existing default for this mode
            layoutDao.clearDefaultForMode(userId, layout.mode.name)
        }
        
        val savedLayout = SavedLayout(
            id = layoutId,
            userId = userId,
            mode = layout.mode.name,
            name = name,
            layoutJson = gson.toJson(layout),
            isDefault = isDefault
        )
        
        layoutDao.insertSavedLayout(savedLayout)
        Log.i(TAG, "Saved layout: $name for mode: ${layout.mode}")
        
        return layoutId
    }
    
    /**
     * Get available apps for a specific mode
     */
    private suspend fun getAvailableAppsForMode(mode: ToggleMode): List<String> {
        return when (mode) {
            ToggleMode.COMFORT -> {
                listOf("Phone", "Messages", "Camera", "Settings", "Emergency", "Family")
            }
            ToggleMode.FAMILY -> {
                listOf("Phone", "Messages", "Camera", "Gallery", "Video Call", 
                       "Family Chat", "Emergency", "Settings", "Weather")
            }
            ToggleMode.FOCUS -> {
                listOf("Phone", "Emergency", "Messages", "Settings")
            }
            ToggleMode.MINIMAL -> {
                listOf("Phone", "Emergency", "Messages")
            }
            ToggleMode.WELCOME -> {
                listOf("Setup", "Tutorial", "Phone", "Emergency", "Help", "Settings")
            }
        }
    }
    
    /**
     * Create default preferences for a new user
     */
    private fun createDefaultPreferences(userId: String): LayoutPreferences {
        return LayoutPreferences(
            userId = userId,
            preferredMode = ToggleMode.getDefaultMode().name,
            customFontScale = 1.6f, // Windsurf rule: minimum 1.6x for elderly
            customIconScale = 1.4f,
            customBackgroundColor = null,
            hasHighContrastEnabled = true,
            hasLargeTextEnabled = true,
            hasReducedMotionEnabled = true,
            emergencyButtonPosition = null
        )
    }
    
    /**
     * Update user preferences with new mode
     */
    private suspend fun updateUserPreferences(userId: String, mode: ToggleMode) {
        val preferences = layoutDao.getLayoutPreferences(userId)
        if (preferences != null) {
            val updatedPreferences = preferences.copy(
                preferredMode = mode.name,
                lastModified = System.currentTimeMillis()
            )
            layoutDao.updateLayoutPreferences(updatedPreferences)
        }
    }
    
    /**
     * Initialize app tiles with system and essential apps
     */
    private suspend fun initializeAppTiles() {
        val existingTiles = layoutDao.getAllAppTiles()
        if (existingTiles.isEmpty()) {
            val defaultTiles = createDefaultAppTiles()
            layoutDao.insertAppTiles(defaultTiles)
            Log.i(TAG, "Initialized ${defaultTiles.size} default app tiles")
        }
    }
    
    /**
     * Create default app tiles for essential apps
     */
    private fun createDefaultAppTiles(): List<AppTile> {
        return listOf(
            AppTile(
                packageName = "com.android.dialer",
                displayName = "Phone",
                isSystemApp = true,
                isAccessible = true,
                category = "essential",
                elderlyFriendly = true,
                iconPath = null,
                customLabel = null,
                priority = 100
            ),
            AppTile(
                packageName = "com.naviya.launcher.emergency",
                displayName = "Emergency",
                isSystemApp = false,
                isAccessible = true,
                category = "essential",
                elderlyFriendly = true,
                iconPath = null,
                customLabel = null,
                priority = 99
            ),
            AppTile(
                packageName = "com.android.mms",
                displayName = "Messages",
                isSystemApp = true,
                isAccessible = true,
                category = "communication",
                elderlyFriendly = true,
                iconPath = null,
                customLabel = null,
                priority = 90
            ),
            AppTile(
                packageName = "com.android.camera2",
                displayName = "Camera",
                isSystemApp = true,
                isAccessible = true,
                category = "media",
                elderlyFriendly = true,
                iconPath = null,
                customLabel = null,
                priority = 80
            ),
            AppTile(
                packageName = "com.android.settings",
                displayName = "Settings",
                isSystemApp = true,
                isAccessible = true,
                category = "system",
                elderlyFriendly = true,
                iconPath = null,
                customLabel = null,
                priority = 70
            ),
            AppTile(
                packageName = "com.android.gallery3d",
                displayName = "Gallery",
                isSystemApp = true,
                isAccessible = true,
                category = "media",
                elderlyFriendly = true,
                iconPath = null,
                customLabel = null,
                priority = 60
            )
        )
    }
    
    /**
     * Get layout preferences flow for reactive UI updates
     */
    fun getLayoutPreferencesFlow(userId: String = DEFAULT_USER_ID): Flow<LayoutPreferences?> {
        return layoutDao.getLayoutPreferencesFlow(userId)
    }
    
    /**
     * Get saved layouts flow for reactive UI updates
     */
    fun getSavedLayoutsFlow(userId: String = DEFAULT_USER_ID): Flow<List<SavedLayout>> {
        return layoutDao.getSavedLayoutsFlow(userId)
    }
    
    /**
     * Update app usage when user interacts with an app
     */
    suspend fun updateAppUsage(packageName: String) {
        try {
            layoutDao.updateAppLastUsed(packageName)
            Log.d(TAG, "Updated usage for app: $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update app usage for $packageName", e)
        }
    }
    
    /**
     * Get recommended mode based on user behavior and preferences
     */
    suspend fun getRecommendedMode(
        userId: String = DEFAULT_USER_ID,
        isElderly: Boolean = true,
        hasCognitiveImpairment: Boolean = false,
        hasFamilySupport: Boolean = false
    ): ToggleMode {
        return try {
            val preferences = layoutDao.getLayoutPreferences(userId)
            val isNewUser = preferences == null
            
            ToggleMode.getRecommendedMode(
                isElderly = isElderly,
                hasCognitiveImpairment = hasCognitiveImpairment,
                isNewUser = isNewUser,
                hasFamilySupport = hasFamilySupport
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get recommended mode", e)
            ToggleMode.getDefaultMode()
        }
    }
    
    /**
     * Validate current layout against accessibility requirements
     */
    suspend fun validateCurrentLayout(): LayoutValidationResult {
        return try {
            val currentLayout = layoutEngine.getCurrentLayout()
            if (currentLayout != null) {
                val isCompliant = layoutEngine.isAccessibilityCompliant(currentLayout)
                val violations = mutableListOf<String>()
                val warnings = mutableListOf<String>()
                
                // Check Windsurf rules compliance
                if (currentLayout.fontScale < 1.6f && currentLayout.mode != ToggleMode.FAMILY) {
                    violations.add("Font scale below 1.6x minimum for elderly users")
                }
                
                if (currentLayout.tiles.size > currentLayout.mode.maxTiles) {
                    violations.add("Too many tiles for cognitive load management")
                }
                
                val accessibilityScore = if (isCompliant) 1.0f else 0.5f
                val cognitiveLoadScore = currentLayout.tiles.size.toFloat() / currentLayout.mode.maxTiles
                
                LayoutValidationResult(
                    isValid = violations.isEmpty(),
                    violations = violations,
                    warnings = warnings,
                    accessibilityScore = accessibilityScore,
                    cognitiveLoadScore = cognitiveLoadScore
                )
            } else {
                LayoutValidationResult(
                    isValid = false,
                    violations = listOf("No current layout available"),
                    warnings = emptyList(),
                    accessibilityScore = 0.0f,
                    cognitiveLoadScore = 1.0f
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to validate layout", e)
            LayoutValidationResult(
                isValid = false,
                violations = listOf("Validation error: ${e.message}"),
                warnings = emptyList(),
                accessibilityScore = 0.0f,
                cognitiveLoadScore = 1.0f
            )
        }
    }
}
