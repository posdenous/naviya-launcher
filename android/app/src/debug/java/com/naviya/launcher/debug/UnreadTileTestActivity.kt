package com.naviya.launcher.debug

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.naviya.launcher.R
import com.naviya.launcher.tiles.UnreadTile
import com.naviya.launcher.ui.components.UnreadTileView
import kotlinx.coroutines.launch

/**
 * Debug activity for testing the Unread Tile functionality.
 * Shows total count of missed calls + unread SMS in a large tile.
 * Works offline using local call log and SMS inbox access.
 * 
 * This activity allows testing:
 * - Permission handling
 * - Unread counts display
 * - Caregiver availability toggle
 * - Manual refresh
 * - UI appearance in different states
 */
class UnreadTileTestActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS
        )
    }
    
    private lateinit var unreadTile: UnreadTile
    private lateinit var unreadTileView: UnreadTileView
    private lateinit var statusText: TextView
    private lateinit var caregiverSwitch: Switch
    private lateinit var refreshButton: Button
    private lateinit var requestPermissionsButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unread_tile_test)
        
        // Initialize views
        unreadTileView = findViewById(R.id.unread_tile_view)
        statusText = findViewById(R.id.status_text)
        caregiverSwitch = findViewById(R.id.caregiver_switch)
        refreshButton = findViewById(R.id.refresh_button)
        requestPermissionsButton = findViewById(R.id.request_permissions_button)
        
        // Initialize unread tile
        unreadTile = UnreadTile(this)
        
        // Set up observers
        setupObservers()
        
        // Set up click listeners
        setupClickListeners()
        
        // Check permissions
        if (hasRequiredPermissions()) {
            updateStatus("Permissions granted, tile active")
            requestPermissionsButton.isEnabled = false
            refreshTile()
        } else {
            updateStatus("Missing permissions, tile inactive")
            requestPermissionsButton.isEnabled = true
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Simulate app resume event if permissions are granted
        if (hasRequiredPermissions()) {
            unreadTile.onAppResume(caregiverSwitch.isChecked)
        }
    }
    
    /**
     * Set up observers for tile state and reminders
     */
    private fun setupObservers() {
        // Observe tile state changes
        unreadTile.tileState.observe(this) { tileState ->
            unreadTileView.updateWithState(tileState)
            updateStatus("Tile updated: ${tileState.badgeCount} unread items")
        }
        
        // Observe reminder notifications
        unreadTile.reminder.observe(this) { reminderText ->
            reminderText?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Set up click listeners for UI controls
     */
    private fun setupClickListeners() {
        // Refresh button
        refreshButton.setOnClickListener {
            refreshTile()
        }
        
        // Caregiver switch
        caregiverSwitch.setOnCheckedChangeListener { _, isChecked ->
            refreshTile()
        }
        
        // Request permissions button
        requestPermissionsButton.setOnClickListener {
            requestRequiredPermissions()
        }
    }
    
    /**
     * Refresh the tile with current caregiver status
     */
    private fun refreshTile() {
        if (hasRequiredPermissions()) {
            lifecycleScope.launch {
                val caregiverOnline = caregiverSwitch.isChecked
                unreadTile.refresh(caregiverOnline)
            }
        } else {
            updateStatus("Cannot refresh: missing permissions")
        }
    }
    
    /**
     * Update the status text
     */
    private fun updateStatus(message: String) {
        statusText.text = message
    }
    
    /**
     * Check if the app has all required permissions
     */
    private fun hasRequiredPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Request required permissions
     */
    private fun requestRequiredPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PERMISSIONS_REQUEST_CODE
        )
    }
    
    /**
     * Handle permission request results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                updateStatus("Permissions granted, tile active")
                requestPermissionsButton.isEnabled = false
                refreshTile()
            } else {
                updateStatus("Permissions denied, tile inactive")
                requestPermissionsButton.isEnabled = true
            }
        }
    }
}
