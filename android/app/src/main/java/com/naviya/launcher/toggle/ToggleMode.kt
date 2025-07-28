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
     * Comfort Mode: For daily elderly users
     * 2x3 grid, 6 large tiles, essential apps only
     */
    COMFORT(
        displayName = "Comfort",
        description = "Simple layout for daily use",
        targetGroup = "Elderly users, daily activities",
        maxTiles = 6,
        gridColumns = 2,
        gridRows = 3
    ),
    
    /**
     * Family Mode: For family-connected users
     * 3x3 grid, 9 medium tiles, family and communication apps
     */
    FAMILY(
        displayName = "Family",
        description = "Stay connected with family",
        targetGroup = "Family-oriented users, social activities",
        maxTiles = 9,
        gridColumns = 3,
        gridRows = 3
    ),
    
    /**
     * Focus Mode: For users with cognitive challenges
     * 2x2 grid, 4 large tiles, minimal distractions
     */
    FOCUS(
        displayName = "Focus",
        description = "Minimal distractions, essential only",
        targetGroup = "Users with cognitive challenges, dementia",
        maxTiles = 4,
        gridColumns = 2,
        gridRows = 2
    ),
    
    /**
     * Minimal Mode: For users with severe limitations
     * 1x3 grid, 3 very large tiles, absolute essentials
     */
    MINIMAL(
        displayName = "Minimal",
        description = "Only the most essential functions",
        targetGroup = "Users with severe limitations, emergency use",
        maxTiles = 3,
        gridColumns = 1,
        gridRows = 3
    ),
    
    /**
     * Welcome Mode: For new users and onboarding
     * 2x3 grid, 6 tiles with tutorial and setup
     */
    WELCOME(
        displayName = "Welcome",
        description = "Getting started with Naviya",
        targetGroup = "New users, onboarding, setup",
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
                COMFORT -> "Komfort"
                FAMILY -> "Familie"
                FOCUS -> "Fokus"
                MINIMAL -> "Minimal"
                WELCOME -> "Willkommen"
            }
            "tr" -> when (this) {
                COMFORT -> "Konfor"
                FAMILY -> "Aile"
                FOCUS -> "Odak"
                MINIMAL -> "Minimal"
                WELCOME -> "Hoş Geldiniz"
            }
            "uk" -> when (this) {
                COMFORT -> "Комфорт"
                FAMILY -> "Сім'я"
                FOCUS -> "Фокус"
                MINIMAL -> "Мінімальний"
                WELCOME -> "Ласкаво просимо"
            }
            "ar" -> when (this) {
                COMFORT -> "راحة"
                FAMILY -> "عائلة"
                FOCUS -> "تركيز"
                MINIMAL -> "الحد الأدنى"
                WELCOME -> "مرحبا"
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
                COMFORT -> "Einfaches Layout für den täglichen Gebrauch"
                FAMILY -> "Bleiben Sie mit der Familie in Verbindung"
                FOCUS -> "Minimale Ablenkungen, nur das Wesentliche"
                MINIMAL -> "Nur die wichtigsten Funktionen"
                WELCOME -> "Erste Schritte mit Naviya"
            }
            "tr" -> when (this) {
                COMFORT -> "Günlük kullanım için basit düzen"
                FAMILY -> "Ailenizle bağlantıda kalın"
                FOCUS -> "Minimum dikkat dağınıklığı, sadece gerekli"
                MINIMAL -> "Sadece en temel işlevler"
                WELCOME -> "Naviya ile başlangıç"
            }
            "uk" -> when (this) {
                COMFORT -> "Простий макет для щоденного використання"
                FAMILY -> "Залишайтеся на зв'язку з родиною"
                FOCUS -> "Мінімум відволікань, тільки необхідне"
                MINIMAL -> "Тільки найважливіші функції"
                WELCOME -> "Початок роботи з Naviya"
            }
            "ar" -> when (this) {
                COMFORT -> "تخطيط بسيط للاستخدام اليومي"
                FAMILY -> "ابق على تواصل مع العائلة"
                FOCUS -> "الحد الأدنى من الإلهاء، الأساسيات فقط"
                MINIMAL -> "الوظائف الأساسية فقط"
                WELCOME -> "البدء مع نافيا"
            }
            else -> description // English default
        }
    }
    
    /**
     * Check if this mode is suitable for elderly users
     */
    fun isElderlyFriendly(): Boolean {
        return when (this) {
            COMFORT, FOCUS, MINIMAL -> true
            FAMILY -> true // Family mode is also elderly-friendly
            WELCOME -> false // Welcome mode is temporary
        }
    }
    
    /**
     * Get recommended font scale for this mode
     */
    fun getRecommendedFontScale(): Float {
        return when (this) {
            COMFORT -> 1.6f
            FAMILY -> 1.4f
            FOCUS -> 1.8f
            MINIMAL -> 2.0f
            WELCOME -> 1.6f
        }
    }
    
    /**
     * Get recommended icon scale for this mode
     */
    fun getRecommendedIconScale(): Float {
        return when (this) {
            COMFORT -> 1.4f
            FAMILY -> 1.2f
            FOCUS -> 1.6f
            MINIMAL -> 1.8f
            WELCOME -> 1.4f
        }
    }
    
    /**
     * Check if high contrast should be enabled by default
     */
    fun shouldUseHighContrast(): Boolean {
        return when (this) {
            COMFORT, FOCUS, MINIMAL, WELCOME -> true
            FAMILY -> false // Normal contrast for family mode
        }
    }
    
    /**
     * Get the next logical mode for progression
     */
    fun getNextMode(): ToggleMode? {
        return when (this) {
            WELCOME -> COMFORT // Welcome -> Comfort
            MINIMAL -> FOCUS // Minimal -> Focus
            FOCUS -> COMFORT // Focus -> Comfort
            COMFORT -> FAMILY // Comfort -> Family
            FAMILY -> null // Family is the most advanced
        }
    }
    
    /**
     * Get the previous mode for regression
     */
    fun getPreviousMode(): ToggleMode? {
        return when (this) {
            FAMILY -> COMFORT // Family -> Comfort
            COMFORT -> FOCUS // Comfort -> Focus
            FOCUS -> MINIMAL // Focus -> Minimal
            MINIMAL -> null // Minimal is the simplest
            WELCOME -> null // Welcome has no previous
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
        fun getDefaultMode(): ToggleMode = WELCOME
        
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
                isNewUser -> WELCOME
                hasCognitiveImpairment -> if (isElderly) MINIMAL else FOCUS
                isElderly && !hasFamilySupport -> COMFORT
                isElderly && hasFamilySupport -> FAMILY
                else -> COMFORT
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
