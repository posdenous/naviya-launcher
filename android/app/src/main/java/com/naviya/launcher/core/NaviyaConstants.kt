package com.naviya.launcher.core

/**
 * Centralized constants for the Naviya 3-Mode Launcher System
 * Improves maintainability and prevents magic numbers throughout codebase
 */
object NaviyaConstants {
    
    // 3-Mode System Configuration
    object Modes {
        const val ESSENTIAL_MAX_TILES = 3
        const val ESSENTIAL_GRID_COLUMNS = 1
        const val ESSENTIAL_GRID_ROWS = 3
        
        const val COMFORT_MAX_TILES = 4
        const val COMFORT_GRID_COLUMNS = 2
        const val COMFORT_GRID_ROWS = 2
        
        const val CONNECTED_MAX_TILES = 6
        const val CONNECTED_GRID_COLUMNS = 2
        const val CONNECTED_GRID_ROWS = 3
    }
    
    // Elderly-Friendly UI Constants
    object UI {
        const val MIN_TOUCH_TARGET_DP = 48
        const val RECOMMENDED_TOUCH_TARGET_DP = 64
        const val MIN_FONT_SCALE = 1.6f
        const val RECOMMENDED_FONT_SCALE = 2.0f
        const val MIN_ICON_SIZE_DP = 64
        const val ANIMATION_DURATION_MS = 300L
        const val SLOW_ANIMATION_DURATION_MS = 500L
        const val HAPTIC_FEEDBACK_DURATION_MS = 50L
    }
    
    // Security & Privacy Constants
    object Security {
        const val PIN_LENGTH = 4
        const val MAX_PIN_ATTEMPTS = 3
        const val EMERGENCY_BYPASS_TIMEOUT_MS = 30000L // 30 seconds
        const val AUDIT_LOG_RETENTION_DAYS = 90
        const val SESSION_TIMEOUT_MS = 300000L // 5 minutes
        const val TRIPLE_TAP_TIMEOUT_MS = 1000L
    }
    
    // Database Constants
    object Database {
        const val DATABASE_NAME = "naviya_database"
        const val DATABASE_VERSION = 3
        const val QUERY_TIMEOUT_MS = 5000L
        const val MAX_CACHE_SIZE = 50
    }
    
    // Accessibility Constants
    object Accessibility {
        const val HIGH_CONTRAST_THRESHOLD = 4.5f
        const val LARGE_TEXT_SCALE = 1.8f
        const val EXTRA_LARGE_TEXT_SCALE = 2.2f
        const val MIN_COLOR_CONTRAST_RATIO = 3.0f
        const val RECOMMENDED_COLOR_CONTRAST_RATIO = 4.5f
    }
    
    // Emergency & Safety Constants
    object Emergency {
        const val EMERGENCY_CALL_NUMBER = "911" // Configurable per region
        const val SOS_BUTTON_HOLD_DURATION_MS = 3000L
        const val EMERGENCY_CONTACT_MAX_COUNT = 3
        const val LOCATION_UPDATE_INTERVAL_MS = 60000L // 1 minute
    }
    
    // Caregiver Integration Constants
    object Caregiver {
        const val MAX_CAREGIVERS = 2
        const val PERMISSION_REQUEST_TIMEOUT_MS = 86400000L // 24 hours
        const val NOTIFICATION_RETRY_ATTEMPTS = 3
        const val AUDIT_SYNC_INTERVAL_MS = 3600000L // 1 hour
    }
    
    // Performance Constants
    object Performance {
        const val IMAGE_CACHE_SIZE_MB = 50
        const val NETWORK_TIMEOUT_MS = 10000L
        const val BACKGROUND_TASK_TIMEOUT_MS = 30000L
        const val UI_THREAD_TIMEOUT_MS = 5000L
    }
    
    // Internationalization Constants
    object I18n {
        const val DEFAULT_LOCALE = "en"
        const val SUPPORTED_LOCALES = "en,es,fr,de,it,pt,zh,ja,ko,ar,hi"
        const val RTL_LANGUAGES = "ar,he,fa,ur"
    }
    
    // Testing Constants
    object Testing {
        const val TEST_USER_ID = "test_user_123"
        const val TEST_CAREGIVER_ID = "test_caregiver_456"
        const val MOCK_DELAY_MS = 100L
        const val TEST_TIMEOUT_MS = 5000L
    }
}
