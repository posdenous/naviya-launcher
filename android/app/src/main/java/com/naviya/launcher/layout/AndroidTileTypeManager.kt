package com.naviya.launcher.layout

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

/**
 * Android-compatible implementation of the Tile Type System
 * Uses real Android APIs to classify and validate app placements
 */
class AndroidTileTypeManager(private val context: Context) {
    
    private val packageManager = context.packageManager
    
    /**
     * Get all launcher apps installed on the device
     */
    fun getAllLauncherApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0)
    }
    
    /**
     * Classify an app into our semantic tile types based on real Android data
     */
    fun classifyApp(packageName: String): Set<TileType> {
        val types = mutableSetOf<TileType>()
        
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            
            // Check by package name patterns (most reliable)
            types.addAll(getTypesByPackageName(packageName))
            
            // Check by Android app category
            types.addAll(getTypesByCategory(appInfo))
            
            // Check by permissions
            types.addAll(getTypesByPermissions(packageInfo.requestedPermissions))
            
            // If no specific type found, mark as flexible
            if (types.isEmpty()) {
                types.add(TileType.FLEXIBLE)
            }
            
        } catch (e: Exception) {
            // App not found or error - mark as flexible
            types.add(TileType.FLEXIBLE)
        }
        
        return types
    }
    
    /**
     * Most reliable method - classify by known package names
     */
    private fun getTypesByPackageName(packageName: String): Set<TileType> {
        val types = mutableSetOf<TileType>()
        
        when {
            // Communication apps
            packageName in setOf(
                "com.whatsapp",
                "com.android.dialer",
                "com.android.mms",
                "com.skype.raider",
                "com.google.android.apps.tachyon", // Google Meet
                "com.facebook.orca", // Messenger
                "com.viber.voip",
                "com.telegram.messenger",
                "us.zoom.videomeetings",
                "com.discord"
            ) -> types.add(TileType.COMMUNICATION)
            
            // Meditation & Focus apps
            packageName in setOf(
                "com.headspace.android",
                "com.calm.android",
                "com.forestapp",
                "com.insight.timer",
                "com.noisli.noisli",
                "com.dayoneapp.dayone",
                "com.waking.up",
                "com.ten.percent.happier"
            ) -> types.add(TileType.MEDITATION_FOCUS)
            
            // Education & Kids apps
            packageName in setOf(
                "org.khanacademy.android",
                "com.roblox.client",
                "com.duolingo",
                "com.scratchjr.android",
                "com.google.android.apps.kids.familylink",
                "com.pbs.pbskids"
            ) -> types.add(TileType.EDUCATION_KIDS)
            
            // Productivity apps
            packageName in setOf(
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
            ) -> types.add(TileType.PRODUCTIVITY_WORK)
            
            // Camera & Photo apps
            packageName in setOf(
                "com.android.camera",
                "com.google.android.apps.photos",
                "com.android.gallery3d",
                "com.instagram.android",
                "com.snapchat.android",
                "com.adobe.photoshop.express"
            ) -> types.add(TileType.CAMERA_PHOTO)
            
            // Accessibility apps
            packageName in setOf(
                "com.google.android.marvin.talkback",
                "com.google.android.accessibility.soundamplifier",
                "com.google.android.apps.accessibility.voiceaccess",
                "com.google.android.accessibility.switchaccess"
            ) -> types.add(TileType.ACCESSIBILITY_TOOLS)
            
            // System essential apps
            packageName in setOf(
                "com.android.settings",
                "com.android.contacts",
                "com.android.calculator2",
                "com.android.deskclock",
                "com.google.android.apps.maps"
            ) -> types.add(TileType.SYSTEM_ESSENTIAL)
            
            // Emergency apps (including our own)
            packageName in setOf(
                "com.naviya.launcher", // Our app
                "com.android.emergency",
                "com.redcross.firstaid",
                "com.zello.android"
            ) -> types.add(TileType.EMERGENCY_SAFETY)
        }
        
        return types
    }
    
    /**
     * Classify by Android's built-in app categories
     */
    private fun getTypesByCategory(appInfo: ApplicationInfo): Set<TileType> {
        val types = mutableSetOf<TileType>()
        
        when (appInfo.category) {
            ApplicationInfo.CATEGORY_SOCIAL -> types.add(TileType.COMMUNICATION)
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> types.add(TileType.PRODUCTIVITY_WORK)
            ApplicationInfo.CATEGORY_GAME -> {
                // Could be educational games for kids
                types.add(TileType.FLEXIBLE)
            }
            ApplicationInfo.CATEGORY_AUDIO -> types.add(TileType.MEDITATION_FOCUS)
            ApplicationInfo.CATEGORY_IMAGE -> types.add(TileType.CAMERA_PHOTO)
            ApplicationInfo.CATEGORY_VIDEO -> types.add(TileType.CAMERA_PHOTO)
        }
        
        return types
    }
    
    /**
     * Classify by app permissions
     */
    private fun getTypesByPermissions(permissions: Array<String>?): Set<TileType> {
        val types = mutableSetOf<TileType>()
        
        permissions?.let { perms ->
            when {
                perms.contains("android.permission.CALL_PHONE") && 
                perms.contains("android.permission.SEND_SMS") -> {
                    types.add(TileType.COMMUNICATION)
                }
                
                perms.contains("android.permission.CAMERA") -> {
                    types.add(TileType.CAMERA_PHOTO)
                }
                
                perms.contains("android.permission.ACCESS_FINE_LOCATION") &&
                perms.contains("android.permission.CALL_PHONE") -> {
                    types.add(TileType.EMERGENCY_SAFETY)
                }
                
                perms.contains("android.permission.BIND_ACCESSIBILITY_SERVICE") -> {
                    types.add(TileType.ACCESSIBILITY_TOOLS)
                }
            }
        }
        
        return types
    }
    
    /**
     * Get apps compatible with a specific tile type
     */
    fun getAppsForTileType(tileType: TileType): List<ApplicationInfo> {
        val allApps = getAllLauncherApps()
        
        return allApps.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val appTypes = classifyApp(packageName)
            
            if (appTypes.contains(tileType) || tileType == TileType.FLEXIBLE) {
                try {
                    packageManager.getApplicationInfo(packageName, 0)
                } catch (e: Exception) {
                    null
                }
            } else null
        }
    }
    
    /**
     * Validate if an app can be placed in a specific tile slot
     */
    fun canPlaceAppInSlot(packageName: String, slot: TileSlot): Boolean {
        val appTypes = classifyApp(packageName)
        return appTypes.contains(slot.tileType) || slot.tileType == TileType.FLEXIBLE
    }
    
    /**
     * Get user-friendly app name
     */
    fun getAppDisplayName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    /**
     * Auto-populate a tile layout with appropriate apps
     */
    fun autoPopulateLayout(layout: SemanticTileLayout): Map<Int, String> {
        val assignments = mutableMapOf<Int, String>()
        
        layout.slots.sortedByDescending { it.displayPriority }.forEach { slot ->
            val compatibleApps = getAppsForTileType(slot.tileType)
            
            // Find the best app for this slot that hasn't been assigned yet
            val availableApp = compatibleApps.firstOrNull { app ->
                !assignments.containsValue(app.packageName)
            }
            
            availableApp?.let {
                assignments[slot.position] = it.packageName
            }
        }
        
        return assignments
    }
}
