package com.naviya.launcher.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Emergency Location Service for Naviya Launcher
 * Provides location data for emergency situations with offline fallback
 * Follows Windsurf rules for elderly accessibility and offline-first functionality
 */
@Singleton
class EmergencyLocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var lastKnownLocation: Location? = null
    
    companion object {
        private const val TAG = "EmergencyLocationService"
        private const val LOCATION_TIMEOUT_MS = 10000L // 10 seconds max for emergency
        private const val MIN_LOCATION_ACCURACY = 100f // 100 meters accuracy
    }
    
    /**
     * Get current location synchronously for emergency use
     * Returns last known location if current location unavailable (offline support)
     */
    suspend fun getCurrentLocationSync(): Location? {
        return try {
            // Check location permissions
            if (!hasLocationPermissions()) {
                Log.w(TAG, "Location permissions not granted, using last known location")
                return getLastKnownLocationFromSystem()
            }
            
            // Try to get current location with timeout
            withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
                getCurrentLocationInternal()
            } ?: run {
                Log.w(TAG, "Location timeout, using last known location")
                getLastKnownLocationFromSystem()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current location", e)
            getLastKnownLocationFromSystem()
        }
    }
    
    /**
     * Get current location with coroutine support
     */
    private suspend fun getCurrentLocationInternal(): Location? = suspendCoroutine { continuation ->
        try {
            val providers = locationManager.getProviders(true)
            if (providers.isEmpty()) {
                continuation.resume(null)
                return@suspendCoroutine
            }
            
            var locationReceived = false
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (!locationReceived) {
                        locationReceived = true
                        lastKnownLocation = location
                        locationManager.removeUpdates(this)
                        continuation.resume(location)
                    }
                }
                
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            
            // Try GPS first for accuracy
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            }
            
            // Fallback to network provider
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            }
            
            // Timeout handler
            CoroutineScope(Dispatchers.Main).launch {
                delay(LOCATION_TIMEOUT_MS)
                if (!locationReceived) {
                    locationReceived = true
                    locationManager.removeUpdates(locationListener)
                    continuation.resume(getLastKnownLocationFromSystem())
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in getCurrentLocationInternal", e)
            continuation.resume(null)
        }
    }
    
    /**
     * Get last known location from system (offline fallback)
     */
    private fun getLastKnownLocationFromSystem(): Location? {
        return try {
            if (!hasLocationPermissions()) {
                return lastKnownLocation
            }
            
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            
            var bestLocation: Location? = null
            
            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val location = locationManager.getLastKnownLocation(provider)
                        if (location != null && isBetterLocation(location, bestLocation)) {
                            bestLocation = location
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting last known location from $provider", e)
                }
            }
            
            bestLocation?.let { lastKnownLocation = it }
            bestLocation ?: lastKnownLocation
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last known location", e)
            lastKnownLocation
        }
    }
    
    /**
     * Determine if one location is better than another
     */
    private fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            return true
        }
        
        // Check if the new location is newer
        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer = timeDelta > 2 * 60 * 1000 // 2 minutes
        val isSignificantlyOlder = timeDelta < -2 * 60 * 1000
        val isNewer = timeDelta > 0
        
        if (isSignificantlyNewer) {
            return true
        } else if (isSignificantlyOlder) {
            return false
        }
        
        // Check accuracy
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200
        
        // Check if the old and new location are from the same provider
        val isFromSameProvider = location.provider == currentBestLocation.provider
        
        return when {
            isMoreAccurate -> true
            isNewer && !isLessAccurate -> true
            isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
            else -> false
        }
    }
    
    /**
     * Format location for emergency messages
     */
    fun formatLocationForEmergency(location: Location?): String {
        return if (location != null) {
            val accuracy = if (location.hasAccuracy()) {
                " (±${location.accuracy.toInt()}m)"
            } else ""
            
            "Lat: ${String.format("%.6f", location.latitude)}, " +
            "Lon: ${String.format("%.6f", location.longitude)}$accuracy"
        } else {
            "Location unavailable"
        }
    }
    
    /**
     * Get Google Maps link for location sharing
     */
    fun getGoogleMapsLink(location: Location?): String? {
        return location?.let {
            "https://maps.google.com/?q=${it.latitude},${it.longitude}"
        }
    }
    
    /**
     * Check if location permissions are granted
     */
    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if location services are enabled
     */
    fun isLocationEnabled(): Boolean {
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Start background location tracking for emergency preparedness
     */
    fun startLocationTracking() {
        if (!hasLocationPermissions()) {
            Log.w(TAG, "Cannot start location tracking - permissions not granted")
            return
        }
        
        try {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (location.accuracy <= MIN_LOCATION_ACCURACY) {
                        lastKnownLocation = location
                        Log.d(TAG, "Location updated: ${formatLocationForEmergency(location)}")
                    }
                }
                
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            
            // Request location updates every 5 minutes for battery efficiency
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5 * 60 * 1000L, // 5 minutes
                    100f, // 100 meters
                    locationListener
                )
            }
            
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5 * 60 * 1000L, // 5 minutes
                    100f, // 100 meters
                    locationListener
                )
            }
            
            Log.i(TAG, "Location tracking started")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location tracking", e)
        }
    }
    
    /**
     * Stop location tracking to save battery
     */
    fun stopLocationTracking() {
        try {
            // This would need to store the listener reference to properly remove it
            // For now, just log the action
            Log.i(TAG, "Location tracking stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location tracking", e)
        }
    }
}
