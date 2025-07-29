package com.naviya.launcher.onboarding.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for family onboarding flow
 * Handles all database operations for onboarding process
 */
@Dao
interface OnboardingDao {
    
    // Onboarding State Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnboardingState(onboardingState: OnboardingState): Long
    
    @Update
    suspend fun updateOnboardingState(onboardingState: OnboardingState)
    
    @Query("UPDATE onboarding_state SET updatedAt = :timestamp WHERE userId = :userId")
    suspend fun updateTimestamp(userId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM onboarding_state WHERE userId = :userId")
    suspend fun getOnboardingState(userId: String): OnboardingState?
    
    @Query("SELECT * FROM onboarding_state WHERE userId = :userId")
    fun getOnboardingStateFlow(userId: String): Flow<OnboardingState?>
    
    @Query("SELECT * FROM onboarding_state WHERE onboardingCompleted = 0 ORDER BY setupStartTime DESC")
    suspend fun getIncompleteOnboardings(): List<OnboardingState>
    
    @Query("DELETE FROM onboarding_state WHERE userId = :userId")
    suspend fun deleteOnboardingState(userId: String)
    
    // Family Setup Session Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilySetupSession(session: FamilySetupSession): Long
    
    @Update
    suspend fun updateFamilySetupSession(session: FamilySetupSession)
    
    @Query("SELECT * FROM family_setup_session WHERE sessionId = :sessionId")
    suspend fun getFamilySetupSession(sessionId: String): FamilySetupSession?
    
    @Query("SELECT * FROM family_setup_session WHERE elderlyUserId = :userId AND isActive = 1")
    suspend fun getActiveFamilySession(userId: String): FamilySetupSession?
    
    @Query("UPDATE family_setup_session SET isActive = 0, sessionCompleted = 1, completionTimestamp = :timestamp, sessionResult = :result WHERE sessionId = :sessionId")
    suspend fun completeFamilySession(sessionId: String, timestamp: Long, result: String)
    
    @Query("UPDATE family_setup_session SET isActive = 0, sessionResult = 'timeout' WHERE sessionTimeoutTime < :currentTime AND isActive = 1")
    suspend fun timeoutExpiredSessions(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM family_setup_session WHERE sessionCompleted = 1 AND completionTimestamp < :cutoffTime")
    suspend fun cleanupOldSessions(cutoffTime: Long)
    
    // Onboarding Preferences Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnboardingPreferences(preferences: OnboardingPreferences): Long
    
    @Update
    suspend fun updateOnboardingPreferences(preferences: OnboardingPreferences)
    
    @Query("SELECT * FROM onboarding_preferences WHERE userId = :userId")
    suspend fun getOnboardingPreferences(userId: String): OnboardingPreferences?
    
    @Query("SELECT * FROM onboarding_preferences WHERE userId = :userId")
    fun getOnboardingPreferencesFlow(userId: String): Flow<OnboardingPreferences?>
    
    @Query("DELETE FROM onboarding_preferences WHERE userId = :userId")
    suspend fun deleteOnboardingPreferences(userId: String)
    
    // Setup Validation Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetupValidation(validation: SetupValidation): Long
    
    @Update
    suspend fun updateSetupValidation(validation: SetupValidation)
    
    @Query("SELECT * FROM setup_validation WHERE userId = :userId ORDER BY validatedAt DESC LIMIT 1")
    suspend fun getLatestSetupValidation(userId: String): SetupValidation?
    
    @Query("SELECT * FROM setup_validation WHERE sessionId = :sessionId ORDER BY validatedAt DESC LIMIT 1")
    suspend fun getSessionValidation(sessionId: String): SetupValidation?
    
    @Query("DELETE FROM setup_validation WHERE userId = :userId")
    suspend fun deleteSetupValidation(userId: String)
    
    // Family Assistance Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyAssistance(assistance: FamilyAssistance): Long
    
    @Update
    suspend fun updateFamilyAssistance(assistance: FamilyAssistance)
    
    @Query("SELECT * FROM family_assistance WHERE elderlyUserId = :userId ORDER BY assistanceStartTime DESC")
    suspend fun getFamilyAssistanceHistory(userId: String): List<FamilyAssistance>
    
    @Query("SELECT * FROM family_assistance WHERE elderlyUserId = :userId AND assistanceEndTime IS NULL")
    suspend fun getActiveAssistance(userId: String): FamilyAssistance?
    
    @Query("UPDATE family_assistance SET assistanceEndTime = :endTime, assistanceDurationMs = :duration WHERE assistanceId = :assistanceId")
    suspend fun endAssistance(assistanceId: String, endTime: Long, duration: Long)
    
    @Query("DELETE FROM family_assistance WHERE elderlyUserId = :userId")
    suspend fun deleteFamilyAssistance(userId: String)
    
    // Onboarding Analytics Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnboardingAnalytics(analytics: OnboardingAnalytics): Long
    
    @Query("SELECT * FROM onboarding_analytics WHERE userId = :userId")
    suspend fun getOnboardingAnalytics(userId: String): OnboardingAnalytics?
    
    @Query("SELECT * FROM onboarding_analytics WHERE sessionId = :sessionId")
    suspend fun getSessionAnalytics(sessionId: String): OnboardingAnalytics?
    
    @Query("SELECT AVG(totalSetupTimeMs) FROM onboarding_analytics WHERE setupCompleted = 1")
    suspend fun getAverageSetupTime(): Long?
    
    @Query("SELECT COUNT(*) FROM onboarding_analytics WHERE setupCompleted = 1")
    suspend fun getCompletedSetupCount(): Int
    
    @Query("SELECT COUNT(*) FROM onboarding_analytics WHERE setupAbandoned = 1")
    suspend fun getAbandonedSetupCount(): Int
    
    @Query("DELETE FROM onboarding_analytics WHERE recordedAt < :cutoffTime")
    suspend fun cleanupOldAnalytics(cutoffTime: Long)
    
    // Utility Operations
    
    /**
     * Update onboarding state with key-value pairs
     */
    suspend fun updateOnboardingState(userId: String, updates: Map<String, Any>) {
        val currentState = getOnboardingState(userId) ?: return
        
        var updatedState = currentState.copy(updatedAt = System.currentTimeMillis())
        
        updates.forEach { (key, value) ->
            updatedState = when (key) {
                "currentStep" -> updatedState.copy(currentStep = value as String)
                "onboarding_completed" -> updatedState.copy(onboardingCompleted = value as Boolean)
                "completion_timestamp" -> updatedState.copy(completionTimestamp = value as Long)
                "setup_method" -> updatedState.copy(setupMethod = value as String)
                "total_setup_time_ms" -> updatedState.copy(
                    completionTimestamp = value as Long
                )
                "professional_installation_skipped" -> updatedState.copy(
                    professionalInstallationSkipped = value as Boolean
                )
                "skip_reason" -> updatedState.copy(skipReason = value as String)
                "family_setup_sufficient" -> updatedState.copy(
                    familyAssistedSetup = value as Boolean
                )
                "has_errors" -> updatedState.copy(hasErrors = value as Boolean)
                "last_error" -> updatedState.copy(lastError = value as String)
                else -> updatedState
            }
        }
        
        updateOnboardingState(updatedState)
    }
    
    /**
     * Update basic preferences from BasicPreferences object
     */
    suspend fun updateBasicPreferences(userId: String, preferences: BasicPreferences) {
        val existingPrefs = getOnboardingPreferences(userId)
        
        val updatedPrefs = if (existingPrefs != null) {
            existingPrefs.copy(
                fontScale = preferences.fontScale,
                iconScale = preferences.iconScale,
                highContrastEnabled = preferences.highContrastEnabled,
                hapticFeedbackEnabled = preferences.hapticFeedbackEnabled,
                slowAnimationsEnabled = preferences.slowAnimationsEnabled,
                emergencyButtonAlwaysVisible = preferences.emergencyButtonAlwaysVisible,
                preferredLanguage = preferences.preferredLanguage,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            OnboardingPreferences(
                userId = userId,
                fontScale = preferences.fontScale,
                iconScale = preferences.iconScale,
                highContrastEnabled = preferences.highContrastEnabled,
                hapticFeedbackEnabled = preferences.hapticFeedbackEnabled,
                slowAnimationsEnabled = preferences.slowAnimationsEnabled,
                emergencyButtonAlwaysVisible = preferences.emergencyButtonAlwaysVisible,
                preferredLanguage = preferences.preferredLanguage
            )
        }
        
        insertOnboardingPreferences(updatedPrefs)
    }
    
    /**
     * Get comprehensive onboarding status
     */
    @Query("""
        SELECT 
            os.userId,
            os.onboardingCompleted,
            os.currentStep,
            os.setupStartTime,
            os.completionTimestamp,
            op.fontScale,
            op.preferredLanguage,
            sv.allRequiredValid,
            sv.readyForCompletion
        FROM onboarding_state os
        LEFT JOIN onboarding_preferences op ON os.userId = op.userId
        LEFT JOIN setup_validation sv ON os.userId = sv.userId
        WHERE os.userId = :userId
    """)
    suspend fun getOnboardingStatus(userId: String): OnboardingStatus?
    
    /**
     * Check if user has completed onboarding
     */
    @Query("SELECT onboardingCompleted FROM onboarding_state WHERE userId = :userId")
    suspend fun isOnboardingCompleted(userId: String): Boolean?
    
    /**
     * Get onboarding progress percentage
     */
    @Query("""
        SELECT 
            CASE 
                WHEN onboardingCompleted = 1 THEN 100
                WHEN currentStep = 'LAUNCHER_READY' THEN 95
                WHEN currentStep = 'COMPLETION' THEN 85
                WHEN currentStep = 'SKIP_PROFESSIONAL' THEN 70
                WHEN currentStep = 'OPTIONAL_CAREGIVER' THEN 60
                WHEN currentStep = 'EMERGENCY_CONTACTS' THEN 45
                WHEN currentStep = 'BASIC_PREFERENCES' THEN 30
                WHEN currentStep = 'FAMILY_INTRODUCTION' THEN 15
                ELSE 0
            END as progress
        FROM onboarding_state 
        WHERE userId = :userId
    """)
    suspend fun getOnboardingProgress(userId: String): Int?
    
    /**
     * Cleanup all onboarding data for a user
     */
    @Transaction
    suspend fun cleanupUserOnboardingData(userId: String) {
        deleteOnboardingState(userId)
        deleteOnboardingPreferences(userId)
        deleteSetupValidation(userId)
        deleteFamilyAssistance(userId)
    }
}

/**
 * Onboarding status summary for UI display
 */
data class OnboardingStatus(
    val userId: String,
    val onboardingCompleted: Boolean,
    val currentStep: String,
    val setupStartTime: Long,
    val completionTimestamp: Long?,
    val fontScale: Float?,
    val preferredLanguage: String?,
    val allRequiredValid: Boolean?,
    val readyForCompletion: Boolean?
)
