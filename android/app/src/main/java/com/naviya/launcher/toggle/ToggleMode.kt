package com.naviya.launcher.toggle

/**
 * Toggle modes for Naviya Launcher
 * Each mode targets specific user groups and use cases
 * Follows Windsurf rules for elderly accessibility and cognitive load management
 */
enum class ToggleMode(
    val displayName: String,
    val description: String,
    val targetGroup: String,
    val maxTiles: Int,
    val gridColumns: Int,
    val gridRows: Int
) {
    /**
     * Essential Mode: For users with severe cognitive impairment
     * 1x3 grid, 3 very large tiles, absolute essentials only
     * Phone, Messages (configurable), Contacts
     */
    ESSENTIAL(
        displayName = "Essential",
        description = "Only the most essential functions",
        targetGroup = "Severe cognitive impairment, dementia patients",
        maxTiles = 3,
        gridColumns = 1,
        gridRows = 3
    ),
    
    /**
     * Comfort Mode: For standard elderly users
     * 2x2 grid, 4 large tiles, balanced simplicity with creative engagement
     * Phone, Messages, Camera, Gallery
     */
    COMFORT(
        displayName = "Comfort",
        description = "Simple layout with creative engagement",
        targetGroup = "Standard elderly users, daily activities",
        maxTiles = 4,
        gridColumns = 2,
        gridRows = 2
    ),
    
    /**
     * Connected Mode: For tech-comfortable elderly users with family support
     * 2x3 grid, 6 tiles, enhanced communication features
     * Phone, Messages, Camera, Gallery, Weather, Family
     */
    CONNECTED(
        displayName = "Connected",
        description = "Enhanced communication and family features",
        targetGroup = "Tech-comfortable elderly with family support",
        maxTiles = 6,
        gridColumns = 2,
        gridRows = 3
    );
    
    /**
     * Get localized display name based on language
     */
    fun getLocalizedName(language: String): String {
        return when (language) {
            "de" -> when (this) {
                ESSENTIAL -> "Wesentlich"
                COMFORT -> "Komfort"
                CONNECTED -> "Verbunden"
            }
            "tr" -> when (this) {
                ESSENTIAL -> "Temel"
                COMFORT -> "Konfor"
                CONNECTED -> "Bağlı"
            }
            "uk" -> when (this) {
                ESSENTIAL -> "Основний"
                COMFORT -> "Комфорт"
                CONNECTED -> "Підключений"
            }
            "ar" -> when (this) {
                ESSENTIAL -> "أساسي"
                COMFORT -> "راحة"
                CONNECTED -> "متصل"
            }
            else -> displayName // English default
        }
    }
    
    /**
     * Get localized description based on language
     */
    fun getLocalizedDescription(language: String): String {
        return when (language) {
            "de" -> when (this) {
                ESSENTIAL -> "Nur die wichtigsten Funktionen"
                COMFORT -> "Einfaches Layout mit kreativer Beteiligung"
                CONNECTED -> "Erweiterte Kommunikation und Familienfunktionen"
            }
            "tr" -> when (this) {
                ESSENTIAL -> "Sadece en temel işlevler"
                COMFORT -> "Yaratıcı katılımla basit düzen"
                CONNECTED -> "Gelişmiş iletişim ve aile özellikleri"
            }
            "uk" -> when (this) {
                ESSENTIAL -> "Тільки найважливіші функції"
                COMFORT -> "Простий макет з творчою участю"
                CONNECTED -> "Розширена комунікація та сімейні функції"
            }
            "ar" -> when (this) {
                ESSENTIAL -> "الوظائف الأساسية فقط"
                COMFORT -> "تخطيط بسيط مع المشاركة الإبداعية"
                CONNECTED -> "التواصل المحسن وميزات العائلة"
            }
            else -> description // English default
        }
    }
    
    /**
     * Check if this mode is suitable for elderly users
     */
    fun isElderlyFriendly(): Boolean {
        return when (this) {
            ESSENTIAL, COMFORT, CONNECTED -> true // All 3 modes are elderly-friendly
        }
    }
    
    /**
     * Get recommended font scale for this mode
     */
    fun getRecommendedFontScale(): Float {
        return when (this) {
            ESSENTIAL -> 2.0f    // Largest font for severe cognitive impairment
            COMFORT -> 1.6f      // Standard elderly-friendly font
            CONNECTED -> 1.4f    // Slightly smaller for tech-comfortable users
        }
    }
    
    /**
     * Get recommended icon scale for this mode
     */
    fun getRecommendedIconScale(): Float {
        return when (this) {
            ESSENTIAL -> 1.8f    // Largest icons for motor difficulties
            COMFORT -> 1.4f      // Standard large icons
            CONNECTED -> 1.2f    // Slightly smaller for more content
        }
    }
    
    /**
     * Check if high contrast should be enabled by default
     */
    fun shouldUseHighContrast(): Boolean {
        return when (this) {
            ESSENTIAL -> true    // Maximum contrast for cognitive impairment
            COMFORT -> true      // High contrast for standard elderly users
            CONNECTED -> false   // Normal contrast for tech-comfortable users
        }
    }
    
    /**
     * Get the next logical mode for progression
     */
    fun getNextMode(): ToggleMode? {
        return when (this) {
            ESSENTIAL -> COMFORT   // Essential -> Comfort (user improving)
            COMFORT -> CONNECTED   // Comfort -> Connected (user gaining confidence)
            CONNECTED -> null      // Connected is the most advanced
        }
    }
    
    /**
     * Get the previous mode for regression
     */
    fun getPreviousMode(): ToggleMode? {
        return when (this) {
            CONNECTED -> COMFORT   // Connected -> Comfort (user needs simplification)
            COMFORT -> ESSENTIAL   // Comfort -> Essential (user needs maximum simplification)
            ESSENTIAL -> null      // Essential is the simplest
        }
    }
    
    companion object {
        /**
         * Get mode from string representation
         */
        fun fromString(modeString: String): ToggleMode? {
            return values().find { it.name.equals(modeString, ignoreCase = true) }
        }
        
        /**
         * Get default mode for new users
         */
        fun getDefaultMode(): ToggleMode = COMFORT
        
        /**
         * Get recommended mode based on user characteristics
         */
        fun getRecommendedMode(
            isElderly: Boolean,
            hasCognitiveImpairment: Boolean,
            isNewUser: Boolean,
            hasFamilySupport: Boolean
        ): ToggleMode {
            return when {
                hasCognitiveImpairment -> ESSENTIAL    // Severe impairment needs maximum simplification
                isElderly && !hasFamilySupport -> COMFORT    // Standard elderly without family support
                isElderly && hasFamilySupport -> CONNECTED   // Elderly with family support can handle more features
                else -> COMFORT    // Default to standard mode
            }
        }
        
        /**
         * Get all elderly-friendly modes
         */
        fun getElderlyFriendlyModes(): List<ToggleMode> {
            return values().filter { it.isElderlyFriendly() }
        }
    }
}
