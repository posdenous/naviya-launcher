package com.naviya.launcher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for Naviya Launcher
 * 
 * This class is annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application. It serves as the entry point for the dependency
 * injection container and manages the application lifecycle.
 * 
 * Key responsibilities:
 * - Initialize Hilt dependency injection
 * - Provide application-wide context
 * - Manage global application state
 * - Handle application lifecycle events
 */
@HiltAndroidApp
class NaviyaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any global application components here
        // Hilt will automatically handle dependency injection setup
    }
}
