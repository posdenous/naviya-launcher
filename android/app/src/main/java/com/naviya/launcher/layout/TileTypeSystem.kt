package com.naviya.launcher.layout

import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo

/**
 * Tile Type System for Naviya Launcher
 * Defines semantic tile slots that only accept apps of matching types
 * Ensures appropriate apps are placed in contextually correct positions
 */

/**
 * Semantic tile types that define what kind of app can be placed in a slot
 */
enum class TileType(
    val displayName: String,
    val description: String,
    val allowedCategories: Set<String>,
    val allowedPackagePatterns: Set<String>,
    val requiredPermissions: Set<String> = emptySet()
) {
    COMMUNICATION(
        displayName = "Communication",
        description = "Phone, messaging, and video calling apps",
        allowedCategories = setOf(
            "android.intent.category.APP_MESSAGING",
            "android.intent.category.APP_EMAIL",
            "com.android.contacts"
        ),
        allowedPackagePatterns = setOf(
            "com.android.dialer",
            "com.android.mms",
            "com.whatsapp",
            "com.skype.raider",
            "com.google.android.apps.tachyon", // Google Meet
            "com.facebook.orca", // Messenger
            "com.viber.voip",
            "com.telegram.messenger",
            "us.zoom.videomeetings"
        ),
        requiredPermissions = setOf(
            "android.permission.CALL_PHONE",
            "android.permission.SEND_SMS"
        )
    ),

    CAMERA_PHOTO(
        displayName = "Camera & Photos",
        description = "Camera, gallery, and photo editing apps",
        allowedCategories = setOf(
            "android.intent.category.APP_GALLERY"
        ),
        allowedPackagePatterns = setOf(
            "com.android.camera",
            "com.google.android.apps.photos",
            "com.android.gallery3d",
            "com.instagram.android",
            "com.snapchat.android",
            "com.adobe.photoshop.express",
            "com.canva.editor"
        ),
        requiredPermissions = setOf(
            "android.permission.CAMERA"
        )
    ),

    MEDITATION_FOCUS(
        displayName = "Meditation & Focus",
        description = "Mindfulness, meditation, and focus apps",
        allowedCategories = setOf(
            "android.intent.category.APP_FITNESS"
        ),
        allowedPackagePatterns = setOf(
            "com.headspace.android",
            "com.calm.android",
            "com.forestapp",
            "com.insight.timer",
            "com.noisli.noisli",
            "com.dayoneapp.dayone",
            "com.bamboohr.breathe",
            "com.waking.up",
            "com.ten.percent.happier"
        )
    ),

    EDUCATION_KIDS(
        displayName = "Education & Kids",
        description = "Educational apps and kid-safe games",
        allowedCategories = setOf(
            "android.intent.category.APP_EDUCATION",
            "android.intent.category.GAME_EDUCATIONAL"
        ),
        allowedPackagePatterns = setOf(
            "org.khanacademy.android",
            "com.roblox.client",
            "com.duolingo",
            "com.scratchjr.android",
            "com.toca.tocaboca",
            "com.sago.sago",
            "com.duckduckmoose",
            "com.pbs.pbskids"
        )
    ),

    PRODUCTIVITY_WORK(
        displayName = "Productivity & Work",
        description = "Email, calendar, notes, and business apps",
        allowedCategories = setOf(
            "android.intent.category.APP_EMAIL",
            "android.intent.category.APP_CALENDAR"
        ),
        allowedPackagePatterns = setOf(
            "com.google.android.gm", // Gmail
            "com.google.android.calendar",
            "com.todoist",
            "com.notion.id",
            "com.google.android.apps.docs", // Google Drive
            "com.microsoft.office.outlook",
            "com.slack",
            "com.dropbox.android",
            "com.evernote",
            "com.trello"
        )
    ),

    ACCESSIBILITY_TOOLS(
        displayName = "Accessibility Tools",
        description = "Assistive technology and accessibility apps",
        allowedCategories = setOf(
            "android.intent.category.ACCESSIBILITY"
        ),
        allowedPackagePatterns = setOf(
            "com.google.android.marvin.talkback",
            "com.google.android.accessibility.soundamplifier",
            "com.google.android.apps.accessibility.voiceaccess",
            "com.google.android.accessibility.switchaccess",
            "com.android.server.accessibility",
            "com.samsung.accessibility",
            "com.microsoft.seeing.ai"
        )
    ),

    ELDER_ADVOCACY(
        displayName = "Elder Rights & Advocacy",
        description = "Elder rights resources and advocacy support",
        allowedCategories = setOf(
            "android.intent.category.APP_ADVOCACY",
            "android.intent.category.APP_SUPPORT"
        ),
        allowedPackagePatterns = setOf(
            "org.elderrights.advocacy",
            "com.ageuk.support",
            "org.aarp.mobile",
            "com.eldercare.helpline",
            "org.alzheimers.support",
            "com.seniorplanet.community"
        )
    ),

    EMERGENCY_SAFETY(
        displayName = "Emergency & Safety",
        description = "Emergency services and safety apps",
        allowedCategories = setOf(
            "android.intent.category.APP_EMERGENCY"
        ),
        allowedPackagePatterns = setOf(
            "com.naviya.launcher.emergency", // Our own SOS
            "com.android.emergency",
            "com.redcross.firstaid",
            "com.zello.android",
            "com.life360.android.safetymapd",
            "com.bsafe.droid"
        ),
        requiredPermissions = setOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.CALL_PHONE"
        )
    ),

    SYSTEM_ESSENTIAL(
        displayName = "System Essential",
        description = "Core system apps like Settings, Phone, Contacts",
        allowedCategories = setOf(
            "android.intent.category.APP_CONTACTS",
            "android.intent.category.PREFERENCE"
        ),
        allowedPackagePatterns = setOf(
            "com.android.settings",
            "com.android.contacts",
            "com.android.dialer",
            "com.android.calculator2",
            "com.android.deskclock",
            "com.google.android.apps.maps"
        )
    ),

    FLEXIBLE(
        displayName = "Flexible",
        description = "Can accept any app - no restrictions",
        allowedCategories = setOf("*"),
        allowedPackagePatterns = setOf("*")
    )
}

/**
 * Tile slot definition with semantic type constraints
 */
data class TileSlot(
    val position: Int,
    val tileType: TileType,
    val isRequired: Boolean = false,
    val fallbackApps: List<String> = emptyList(),
    val displayPriority: Int = 0
) {
    /**
     * Check if an app package is compatible with this tile slot
     */
    fun isCompatible(packageName: String, packageManager: PackageManager): Boolean {
        if (tileType == TileType.FLEXIBLE) return true
        
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            
            // Check package patterns
            val matchesPackage = tileType.allowedPackagePatterns.any { pattern ->
                if (pattern == "*") return@any true
                packageName.contains(pattern, ignoreCase = true)
            }
            
            if (matchesPackage) return true
            
            // Check app categories (would need more complex implementation)
            // This is a simplified version
            false
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Mode-specific tile layout with semantic constraints
 */
data class SemanticTileLayout(
    val mode: ToggleMode,
    val slots: List<TileSlot>
) {
    companion object {
        /**
         * Get the semantic tile layout for a specific mode
         */
        fun getLayoutForMode(mode: ToggleMode): SemanticTileLayout {
            return when (mode) {
                ToggleMode.COMFORT -> SemanticTileLayout(
                    mode = ToggleMode.COMFORT,
                    slots = listOf(
                        TileSlot(0, TileType.COMMUNICATION, isRequired = true, displayPriority = 10),
                        TileSlot(1, TileType.COMMUNICATION, displayPriority = 9),
                        TileSlot(2, TileType.EMERGENCY_SAFETY, isRequired = true, displayPriority = 10),
                        TileSlot(3, TileType.CAMERA_PHOTO, displayPriority = 7),
                        TileSlot(4, TileType.SYSTEM_ESSENTIAL, displayPriority = 6),
                        TileSlot(5, TileType.FLEXIBLE, displayPriority = 5)
                    )
                )
                
                ToggleMode.FAMILY -> SemanticTileLayout(
                    mode = ToggleMode.FAMILY,
                    slots = listOf(
                        TileSlot(0, TileType.EDUCATION_KIDS, displayPriority = 10),
                        TileSlot(1, TileType.EDUCATION_KIDS, displayPriority = 9),
                        TileSlot(2, TileType.CAMERA_PHOTO, displayPriority = 8),
                        TileSlot(3, TileType.COMMUNICATION, displayPriority = 7),
                        TileSlot(4, TileType.EMERGENCY_SAFETY, isRequired = true, displayPriority = 10),
                        TileSlot(5, TileType.PARENTAL_CONTROL, isRequired = true, displayPriority = 9),
                        TileSlot(6, TileType.PARENTAL_CONTROL, displayPriority = 8),
                        TileSlot(7, TileType.FLEXIBLE, displayPriority = 5)
                    )
                )
                
                ToggleMode.FOCUS -> SemanticTileLayout(
                    mode = ToggleMode.FOCUS,
                    slots = listOf(
                        TileSlot(0, TileType.MEDITATION_FOCUS, isRequired = true, displayPriority = 10),
                        TileSlot(1, TileType.MEDITATION_FOCUS, displayPriority = 9),
                        TileSlot(2, TileType.MEDITATION_FOCUS, displayPriority = 8),
                        TileSlot(3, TileType.PRODUCTIVITY_WORK, displayPriority = 6),
                        TileSlot(4, TileType.SYSTEM_ESSENTIAL, displayPriority = 5),
                        TileSlot(5, TileType.FLEXIBLE, displayPriority = 4)
                    )
                )
                
                ToggleMode.PRODUCTIVITY -> SemanticTileLayout(
                    mode = ToggleMode.PRODUCTIVITY,
                    slots = listOf(
                        TileSlot(0, TileType.PRODUCTIVITY_WORK, isRequired = true, displayPriority = 10),
                        TileSlot(1, TileType.PRODUCTIVITY_WORK, displayPriority = 9),
                        TileSlot(2, TileType.PRODUCTIVITY_WORK, displayPriority = 8),
                        TileSlot(3, TileType.COMMUNICATION, displayPriority = 7),
                        TileSlot(4, TileType.PRODUCTIVITY_WORK, displayPriority = 6),
                        TileSlot(5, TileType.SYSTEM_ESSENTIAL, displayPriority = 5),
                        TileSlot(6, TileType.SYSTEM_ESSENTIAL, displayPriority = 4),
                        TileSlot(7, TileType.SYSTEM_ESSENTIAL, displayPriority = 3),
                        TileSlot(8, TileType.FLEXIBLE, displayPriority = 2),
                        TileSlot(9, TileType.FLEXIBLE, displayPriority = 1),
                        TileSlot(10, TileType.COMMUNICATION, displayPriority = 6),
                        TileSlot(11, TileType.SYSTEM_ESSENTIAL, displayPriority = 4)
                    )
                )
                
                ToggleMode.ACCESSIBILITY -> SemanticTileLayout(
                    mode = ToggleMode.ACCESSIBILITY,
                    slots = listOf(
                        TileSlot(0, TileType.ACCESSIBILITY_TOOLS, isRequired = true, displayPriority = 10),
                        TileSlot(1, TileType.ACCESSIBILITY_TOOLS, displayPriority = 9),
                        TileSlot(2, TileType.EMERGENCY_SAFETY, isRequired = true, displayPriority = 10),
                        TileSlot(3, TileType.ACCESSIBILITY_TOOLS, displayPriority = 8),
                        TileSlot(4, TileType.ACCESSIBILITY_TOOLS, displayPriority = 7),
                        TileSlot(5, TileType.SYSTEM_ESSENTIAL, displayPriority = 6)
                    )
                )
            }
        }
    }
}

/**
 * App compatibility checker and auto-placement system
 */
class TileTypeManager(private val packageManager: PackageManager) {
    
    /**
     * Get all installed apps that are compatible with a specific tile type
     */
    fun getCompatibleApps(tileType: TileType): List<ApplicationInfo> {
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return installedApps.filter { appInfo ->
            isAppCompatible(appInfo.packageName, tileType)
        }
    }
    
    /**
     * Check if an app is compatible with a tile type
     */
    fun isAppCompatible(packageName: String, tileType: TileType): Boolean {
        if (tileType == TileType.FLEXIBLE) return true
        
        // Check package patterns
        val matchesPackage = tileType.allowedPackagePatterns.any { pattern ->
            if (pattern == "*") return@any true
            packageName.contains(pattern, ignoreCase = true) || 
            pattern.contains(packageName, ignoreCase = true)
        }
        
        if (matchesPackage) return true
        
        // Additional checks for permissions, categories, etc. would go here
        return false
    }
    
    /**
     * Auto-suggest apps for empty tile slots based on type
     */
    fun suggestAppsForSlot(slot: TileSlot): List<ApplicationInfo> {
        val compatibleApps = getCompatibleApps(slot.tileType)
        
        // Sort by popularity/usage (would need usage stats permission)
        return compatibleApps.take(3) // Return top 3 suggestions
    }
    
    /**
     * Validate that a proposed app placement is appropriate
     */
    fun validatePlacement(packageName: String, slot: TileSlot): PlacementResult {
        return when {
            isAppCompatible(packageName, slot.tileType) -> PlacementResult.ALLOWED
            slot.tileType == TileType.FLEXIBLE -> PlacementResult.ALLOWED
            else -> PlacementResult.INCOMPATIBLE_TYPE
        }
    }
}

enum class PlacementResult {
    ALLOWED,
    INCOMPATIBLE_TYPE,
    MISSING_PERMISSIONS,
    SLOT_REQUIRED_DIFFERENT_TYPE
}
