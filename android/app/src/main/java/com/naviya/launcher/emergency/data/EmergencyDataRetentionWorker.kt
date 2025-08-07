package com.naviya.launcher.emergency.data

import android.content.Context
import android.util.Log
import androidx.work.*
import com.naviya.launcher.config.EmergencyProductionConfig
import com.naviya.launcher.data.NaviyaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker responsible for enforcing data retention policies for emergency data
 * Implements GDPR compliance by soft-deleting old emergency events
 */
class EmergencyDataRetentionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "EmergencyDataRetention"
        // Making this public so it can be accessed from EmergencyDataRetentionService
        const val WORK_NAME = "emergency_data_retention_work"
        
        /**
         * Schedule the emergency data retention worker to run periodically
         * @param context Application context
         * @param config Emergency configuration
         */
        fun schedule(context: Context, config: EmergencyProductionConfig) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
                
            val retentionPeriod = config.emergencyDataRetentionDays.toLong()
            
            // Run daily during quiet hours (3 AM)
            val retentionWorkRequest = PeriodicWorkRequestBuilder<EmergencyDataRetentionWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .addTag("data_retention")
                .addTag("gdpr_compliance")
                .build()
                
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                retentionWorkRequest
            )
            
            Log.i(TAG, "Scheduled emergency data retention worker (retention period: $retentionPeriod days)")
        }
        
        /**
         * Calculate initial delay to run at 3 AM
         */
        private fun calculateInitialDelay(): Long {
            val currentTimeMillis = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = currentTimeMillis
                set(java.util.Calendar.HOUR_OF_DAY, 3)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                
                // If it's already past 3 AM, schedule for tomorrow
                if (timeInMillis <= currentTimeMillis) {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
            }
            
            return calendar.timeInMillis - currentTimeMillis
        }
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting emergency data retention task")
            
            val config = EmergencyProductionConfig(applicationContext)
            val retentionPeriodDays = config.emergencyDataRetentionDays
            
            // Calculate cutoff time based on retention period
            val cutoffTimeMillis = System.currentTimeMillis() - (retentionPeriodDays * 24 * 60 * 60 * 1000L)
            
            // Get the DAO from the database
            val database = NaviyaDatabase.getDatabase(applicationContext)
            val emergencyDao = database.emergencyDao()
            
            // Soft delete old events by setting deleted_at timestamp
            val currentTime = System.currentTimeMillis()
            emergencyDao.deleteOldEvents(cutoffTimeMillis, currentTime)
            
            Log.i(TAG, "Emergency data retention completed successfully. Retention period: $retentionPeriodDays days")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during emergency data retention: ${e.message}", e)
            Result.retry()
        }
    }
}
