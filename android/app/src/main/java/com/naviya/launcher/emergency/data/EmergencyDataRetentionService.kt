package com.naviya.launcher.emergency.data

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.naviya.launcher.config.EmergencyProductionConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for managing emergency data retention policies
 * Ensures GDPR compliance by scheduling regular cleanup of old emergency data
 */
@Singleton
class EmergencyDataRetentionService @Inject constructor(
    private val context: Context,
    private val emergencyDao: EmergencyDao
) {
    private val config = EmergencyProductionConfig(context)
    private val tag = "EmergencyDataRetention"
    
    /**
     * Initialize the data retention service
     * Schedules the worker to run periodically
     */
    fun initialize() {
        Log.i(tag, "Initializing emergency data retention service")
        scheduleDataRetentionWorker()
    }
    
    /**
     * Schedule the data retention worker
     */
    private fun scheduleDataRetentionWorker() {
        EmergencyDataRetentionWorker.schedule(context, config)
        Log.i(tag, "Emergency data retention worker scheduled (retention period: ${config.emergencyDataRetentionDays} days)")
    }
    
    /**
     * Manually trigger data retention cleanup
     * Useful for testing or immediate compliance needs
     */
    suspend fun performManualCleanup() {
        Log.i(tag, "Performing manual emergency data cleanup")
        val retentionPeriodDays = config.emergencyDataRetentionDays
        val cutoffTimeMillis = System.currentTimeMillis() - (retentionPeriodDays * 24 * 60 * 60 * 1000L)
        val currentTime = System.currentTimeMillis()
        
        emergencyDao.deleteOldEvents(cutoffTimeMillis, currentTime)
        Log.i(tag, "Manual emergency data cleanup completed")
    }
    
    /**
     * Cancel all scheduled data retention work
     * Use with caution - this will stop GDPR compliance enforcement
     */
    fun cancelScheduledWork() {
        WorkManager.getInstance(context).cancelUniqueWork(EmergencyDataRetentionWorker.WORK_NAME)
        Log.w(tag, "Emergency data retention worker cancelled - GDPR compliance at risk!")
    }
}
