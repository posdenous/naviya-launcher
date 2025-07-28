package com.naviya.launcher.layout.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Layout Engine
 * Handles persistence of layout configurations and user preferences
 * Follows Windsurf rules for Room database operations
 */
@Dao
interface LayoutDao {
    
    // Layout Preferences Operations
    
    @Query("SELECT * FROM layout_preferences WHERE userId = :userId")
    suspend fun getLayoutPreferences(userId: String): LayoutPreferences?
    
    @Query("SELECT * FROM layout_preferences WHERE userId = :userId")
    fun getLayoutPreferencesFlow(userId: String): Flow<LayoutPreferences?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayoutPreferences(preferences: LayoutPreferences)
    
    @Update
    suspend fun updateLayoutPreferences(preferences: LayoutPreferences)
    
    @Delete
    suspend fun deleteLayoutPreferences(preferences: LayoutPreferences)
    
    // Saved Layouts Operations
    
    @Query("SELECT * FROM saved_layouts WHERE userId = :userId ORDER BY lastUsed DESC")
    suspend fun getSavedLayouts(userId: String): List<SavedLayout>
    
    @Query("SELECT * FROM saved_layouts WHERE userId = :userId ORDER BY lastUsed DESC")
    fun getSavedLayoutsFlow(userId: String): Flow<List<SavedLayout>>
    
    @Query("SELECT * FROM saved_layouts WHERE id = :layoutId")
    suspend fun getSavedLayout(layoutId: String): SavedLayout?
    
    @Query("SELECT * FROM saved_layouts WHERE userId = :userId AND mode = :mode AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultLayoutForMode(userId: String, mode: String): SavedLayout?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedLayout(layout: SavedLayout)
    
    @Update
    suspend fun updateSavedLayout(layout: SavedLayout)
    
    @Query("UPDATE saved_layouts SET lastUsed = :timestamp WHERE id = :layoutId")
    suspend fun updateLastUsed(layoutId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE saved_layouts SET isDefault = 0 WHERE userId = :userId AND mode = :mode")
    suspend fun clearDefaultForMode(userId: String, mode: String)
    
    @Query("DELETE FROM saved_layouts WHERE id = :layoutId")
    suspend fun deleteSavedLayout(layoutId: String)
    
    @Query("DELETE FROM saved_layouts WHERE userId = :userId")
    suspend fun deleteAllUserLayouts(userId: String)
    
    // App Tiles Operations
    
    @Query("SELECT * FROM app_tiles WHERE isHidden = 0 ORDER BY priority DESC, displayName ASC")
    suspend fun getAllAppTiles(): List<AppTile>
    
    @Query("SELECT * FROM app_tiles WHERE isHidden = 0 ORDER BY priority DESC, displayName ASC")
    fun getAllAppTilesFlow(): Flow<List<AppTile>>
    
    @Query("SELECT * FROM app_tiles WHERE packageName = :packageName")
    suspend fun getAppTile(packageName: String): AppTile?
    
    @Query("SELECT * FROM app_tiles WHERE category = :category AND isHidden = 0 ORDER BY priority DESC")
    suspend fun getAppTilesByCategory(category: String): List<AppTile>
    
    @Query("SELECT * FROM app_tiles WHERE elderlyFriendly = 1 AND isHidden = 0 ORDER BY priority DESC")
    suspend fun getElderlyFriendlyApps(): List<AppTile>
    
    @Query("SELECT * FROM app_tiles WHERE isSystemApp = 0 AND isHidden = 0 ORDER BY lastUsed DESC LIMIT :limit")
    suspend fun getRecentlyUsedApps(limit: Int): List<AppTile>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppTile(appTile: AppTile)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppTiles(appTiles: List<AppTile>)
    
    @Update
    suspend fun updateAppTile(appTile: AppTile)
    
    @Query("UPDATE app_tiles SET lastUsed = :timestamp WHERE packageName = :packageName")
    suspend fun updateAppLastUsed(packageName: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE app_tiles SET isHidden = :isHidden WHERE packageName = :packageName")
    suspend fun setAppVisibility(packageName: String, isHidden: Boolean)
    
    @Query("UPDATE app_tiles SET customLabel = :label WHERE packageName = :packageName")
    suspend fun updateAppLabel(packageName: String, label: String?)
    
    @Query("UPDATE app_tiles SET priority = :priority WHERE packageName = :packageName")
    suspend fun updateAppPriority(packageName: String, priority: Int)
    
    @Delete
    suspend fun deleteAppTile(appTile: AppTile)
    
    @Query("DELETE FROM app_tiles WHERE packageName = :packageName")
    suspend fun deleteAppTile(packageName: String)
    
    // Analytics and Statistics
    
    @Query("SELECT COUNT(*) FROM saved_layouts WHERE userId = :userId")
    suspend fun getUserLayoutCount(userId: String): Int
    
    @Query("SELECT mode, COUNT(*) as count FROM saved_layouts WHERE userId = :userId GROUP BY mode")
    suspend fun getLayoutCountByMode(userId: String): Map<String, Int>
    
    @Query("SELECT * FROM saved_layouts WHERE userId = :userId ORDER BY lastUsed DESC LIMIT 1")
    suspend fun getMostRecentLayout(userId: String): SavedLayout?
    
    @Query("SELECT AVG(lastUsed - createdAt) FROM saved_layouts WHERE userId = :userId")
    suspend fun getAverageLayoutLifetime(userId: String): Long?
    
    @Query("SELECT packageName, COUNT(*) as usage_count FROM app_tiles WHERE lastUsed > :since GROUP BY packageName ORDER BY usage_count DESC LIMIT :limit")
    suspend fun getMostUsedApps(since: Long, limit: Int): Map<String, Int>
    
    // Cleanup Operations
    
    @Query("DELETE FROM saved_layouts WHERE lastUsed < :cutoffTime AND isDefault = 0")
    suspend fun cleanupOldLayouts(cutoffTime: Long)
    
    @Query("DELETE FROM app_tiles WHERE lastUsed < :cutoffTime AND isSystemApp = 0")
    suspend fun cleanupUnusedApps(cutoffTime: Long)
    
    @Query("UPDATE app_tiles SET lastUsed = 0 WHERE lastUsed < :cutoffTime")
    suspend fun resetOldAppUsage(cutoffTime: Long)
}
