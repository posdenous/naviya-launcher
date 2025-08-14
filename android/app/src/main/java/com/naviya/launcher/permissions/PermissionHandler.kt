package com.naviya.launcher.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.naviya.launcher.R
import com.naviya.launcher.unread.UnreadTileService

/**
 * Handler for managing permissions required by the app
 * Designed with elderly users in mind - clear explanations and simple UI
 */
class PermissionHandler(private val activity: FragmentActivity) {

    companion object {
        // Permission request codes
        private const val UNREAD_TILE_PERMISSION_REQUEST = 1001
        
        // Permission groups
        val UNREAD_TILE_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS
        )
    }
    
    // Permission request launcher
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    
    // Callback for permission results
    private var permissionCallback: ((Boolean) -> Unit)? = null
    
    /**
     * Initialize the permission handler
     */
    fun initialize() {
        // Register for permission results
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Check if all permissions are granted
            val allGranted = permissions.entries.all { it.value }
            
            // Notify callback
            permissionCallback?.invoke(allGranted)
            permissionCallback = null
            
            // If permissions were denied, show rationale dialog
            if (!allGranted) {
                showPermissionRationaleDialog()
            }
        }
    }
    
    /**
     * Check if all unread tile permissions are granted
     */
    fun hasUnreadTilePermissions(): Boolean {
        return UNREAD_TILE_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Request unread tile permissions
     * @param callback Callback to be invoked when permissions are granted or denied
     */
    fun requestUnreadTilePermissions(callback: (Boolean) -> Unit) {
        // Store callback
        permissionCallback = callback
        
        // Check if permissions are already granted
        if (hasUnreadTilePermissions()) {
            callback(true)
            return
        }
        
        // Show explanation dialog first for elderly users
        showElderlyFriendlyPermissionExplanation {
            // Launch permission request
            permissionLauncher.launch(UNREAD_TILE_PERMISSIONS)
        }
    }
    
    /**
     * Show an elderly-friendly explanation of why permissions are needed
     * Uses simple language and large text
     */
    private fun showElderlyFriendlyPermissionExplanation(onProceed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Needed")
            .setMessage(
                "To show you missed calls and unread messages, " +
                "the app needs permission to check your call history and messages.\n\n" +
                "This helps you see what you might have missed."
            )
            .setPositiveButton("Allow") { _, _ -> onProceed() }
            .setNegativeButton("Not Now") { dialog, _ -> dialog.dismiss() }
            .setIcon(R.drawable.ic_envelope)
            .create()
            .apply {
                // Make text larger for elderly users
                setOnShowListener {
                    val textView = findViewById<android.widget.TextView>(android.R.id.message)
                    textView?.textSize = 18f
                }
            }
            .show()
    }
    
    /**
     * Show a dialog explaining why permissions are important and how to enable them
     * in settings if they were denied
     */
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Important Permissions")
            .setMessage(
                "Without these permissions, the app cannot show your missed calls and messages.\n\n" +
                "Would you like to open settings to enable these permissions?"
            )
            .setPositiveButton("Open Settings") { _, _ ->
                // Open app settings
                openAppSettings()
            }
            .setNegativeButton("Not Now") { dialog, _ -> dialog.dismiss() }
            .setIcon(R.drawable.ic_settings)
            .create()
            .apply {
                // Make text larger for elderly users
                setOnShowListener {
                    val textView = findViewById<android.widget.TextView>(android.R.id.message)
                    textView?.textSize = 18f
                }
            }
            .show()
    }
    
    /**
     * Open the app settings page
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
    
    /**
     * Check if all required permissions for the app are granted
     */
    fun hasAllRequiredPermissions(): Boolean {
        return hasUnreadTilePermissions()
    }
    
    /**
     * Request all required permissions for the app
     */
    fun requestAllRequiredPermissions(callback: (Boolean) -> Unit) {
        requestUnreadTilePermissions(callback)
    }
}
