package com.naviya.launcher.di

import android.content.Context
import com.naviya.launcher.unread.UnreadTileEventHandler
import com.naviya.launcher.unread.UnreadTileService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger/Hilt module that provides unread tile related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object UnreadModule {

    /**
     * Provides the UnreadTileService singleton
     */
    @Provides
    @Singleton
    fun provideUnreadTileService(
        @ApplicationContext context: Context
    ): UnreadTileService {
        return UnreadTileService(context)
    }

    /**
     * Provides the UnreadTileEventHandler singleton
     */
    @Provides
    @Singleton
    fun provideUnreadTileEventHandler(
        unreadTileService: UnreadTileService
    ): UnreadTileEventHandler {
        return UnreadTileEventHandler(unreadTileService)
    }
}
