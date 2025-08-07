package com.naviya.launcher.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.naviya.launcher.emergency.data.ChildWorkerFactory
import com.naviya.launcher.emergency.data.DaggerEmergencyDataRetentionWorker
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Factory for creating workers with dependencies
 * Allows WorkManager to create workers with injected dependencies
 */
@Singleton
class NaviyaWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>
) : WorkerFactory() {
    
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        // Get the worker class from the class name
        val workerClass = try {
            Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
        } catch (e: ClassNotFoundException) {
            null
        }
        
        // Return null to let WorkManager's default factory handle it
        // if we don't have a factory for this worker class
        return workerClass?.let { clazz ->
            val factoryProvider = workerFactories[clazz]
            factoryProvider?.get()?.create(appContext, workerParameters)
        }
    }
}
