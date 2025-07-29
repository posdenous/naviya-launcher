package com.naviya.launcher.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.naviya.launcher.database.NaviyaDatabase
import com.naviya.launcher.layout.LauncherLayoutEngine
import javax.inject.Singleton

/**
 * Core Hilt module providing essential dependencies for the Naviya launcher
 * This module provides the fundamental services and DAOs required by the application
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideNaviyaDatabase(@ApplicationContext context: Context): NaviyaDatabase {
        return NaviyaDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideLauncherLayoutEngine(
        @ApplicationContext context: Context
    ): LauncherLayoutEngine {
        return LauncherLayoutEngine(context)
    }
}
