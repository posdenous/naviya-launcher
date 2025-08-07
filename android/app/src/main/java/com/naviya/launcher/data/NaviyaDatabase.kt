package com.naviya.launcher.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.naviya.launcher.data.dao.*
import com.naviya.launcher.data.models.*
import com.naviya.launcher.emergency.data.EmergencyTypeConverters

/**
 * Main Room database for Naviya Launcher
 */
@Database(
    entities = [
        com.naviya.launcher.emergency.data.EmergencyEvent::class,
        com.naviya.launcher.emergency.data.EmergencyContact::class,
        com.naviya.launcher.onboarding.data.OnboardingState::class,
        com.naviya.launcher.onboarding.data.FamilySetupSession::class,
        com.naviya.launcher.onboarding.data.OnboardingPreferences::class,
        com.naviya.launcher.onboarding.data.SetupValidation::class,
        com.naviya.launcher.onboarding.data.FamilyAssistance::class,
        com.naviya.launcher.onboarding.data.OnboardingAnalytics::class,
        com.naviya.launcher.security.ModeSwitchAudit::class,
        com.naviya.launcher.security.SecurityEvent::class,
        com.naviya.launcher.security.AuthenticationAttempt::class,
        com.naviya.launcher.security.ElderlyConsentLog::class,
        com.naviya.launcher.security.SystemLockoutLog::class,
        com.naviya.launcher.security.CaregiverTokenValidation::class,
        com.naviya.launcher.data.AnalyticsEmergencyEvent::class
    ],
    version = 1,
    exportSchema = false
)
// @TypeConverters(EmergencyTypeConverters::class)
abstract class NaviyaDatabase : RoomDatabase() {
    
    // Include all DAOs
    abstract fun emergencyDao(): com.naviya.launcher.emergency.data.EmergencyDao
    abstract fun onboardingDao(): com.naviya.launcher.onboarding.data.OnboardingDao
    abstract fun securityAuditDao(): com.naviya.launcher.data.dao.SecurityAuditDao
    
    companion object {
        @Volatile
        private var INSTANCE: NaviyaDatabase? = null
        
        fun getDatabase(context: Context): NaviyaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NaviyaDatabase::class.java,
                    "naviya_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
