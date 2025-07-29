package com.naviya.launcher.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.naviya.launcher.database.NaviyaDatabase
import com.naviya.launcher.layout.LauncherLayoutEngine
import com.naviya.launcher.layout.LayoutManager
import com.naviya.launcher.layout.TileTypeSystem
import com.naviya.launcher.layout.AndroidTileTypeManager
import com.naviya.launcher.unread.UnreadTileService
import com.naviya.launcher.emergency.EmergencyService
import com.naviya.launcher.emergency.EmergencyLocationService
import com.naviya.launcher.emergency.CaregiverNotificationService
import com.naviya.launcher.caregiver.CaregiverPermissionManager
import com.naviya.launcher.caregiver.CaregiverSyncManager
import com.naviya.launcher.caregiver.CaregiverHeartbeatManager
import com.naviya.launcher.caregiver.AbuseDetectionEngine
import com.naviya.launcher.caregiver.PanicModeManager
import com.naviya.launcher.caregiver.MultiChannelEmergencyAlertManager
import com.naviya.launcher.caregiver.OfflineCaregiverConnectivityService
import com.naviya.launcher.contacts.ContactProtectionManager
import com.naviya.launcher.compliance.GDPRComplianceManager
import com.naviya.launcher.compliance.MedicalDeviceComplianceManager
import com.naviya.launcher.elderrights.ElderRightsAdvocateService
import com.naviya.launcher.abuse.RuleBasedAbuseDetector
import com.naviya.launcher.onboarding.FamilyOnboardingFlow
import com.naviya.launcher.ethics.EthicalAppAccessManager
import com.naviya.launcher.security.ModeSwitchingSecurityManager
import javax.inject.Singleton

/**
 * Main Hilt module for core application dependencies
 * Provides essential services, managers, and components
 */
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideNaviyaDatabase(@ApplicationContext context: Context): NaviyaDatabase {
        return NaviyaDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideLayoutManager(
        @ApplicationContext context: Context,
        database: NaviyaDatabase
    ): LayoutManager {
        return LayoutManager(context, database)
    }

    @Provides
    @Singleton
    fun provideTileTypeSystem(): TileTypeSystem {
        return AndroidTileTypeManager()
    }

    @Provides
    @Singleton
    fun provideLauncherLayoutEngine(
        layoutManager: LayoutManager,
        tileTypeSystem: TileTypeSystem
    ): LauncherLayoutEngine {
        return LauncherLayoutEngine(layoutManager, tileTypeSystem)
    }
}
