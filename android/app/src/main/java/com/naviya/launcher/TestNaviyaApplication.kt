package com.naviya.launcher

import android.app.Application

/**
 * Minimal Test Application Class
 * Simple application class without Hilt/Dagger dependencies for emulator testing
 */
class TestNaviyaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any basic components needed for testing
        // No dependency injection or complex setup required
    }
}
