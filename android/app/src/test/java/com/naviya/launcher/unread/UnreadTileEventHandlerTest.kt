package com.naviya.launcher.unread

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

/**
 * Unit tests for UnreadTileEventHandler
 * Verifies that events are properly forwarded to the UnreadTileService
 */
@ExperimentalCoroutinesApi
class UnreadTileEventHandlerTest {

    // Use a test dispatcher for controlled coroutine execution
    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    
    // Mock dependencies
    private lateinit var unreadTileService: UnreadTileService
    private lateinit var eventHandler: UnreadTileEventHandler
    
    @Before
    fun setup() {
        // Create a mock UnreadTileService
        unreadTileService = mock()
        
        // Create the event handler with the mock service
        eventHandler = UnreadTileEventHandler(unreadTileService)
    }
    
    @Test
    fun `test onLauncherHomeOpened calls service`() = testScope.runBlockingTest {
        // When
        eventHandler.onLauncherHomeOpened()
        
        // Then
        // Advance coroutines to ensure all async work completes
        advanceUntilIdle()
        
        // Verify that the service method was called
        verify(unreadTileService).onLauncherHomeOpened()
    }
    
    @Test
    fun `test onAppResume calls service`() = testScope.runBlockingTest {
        // When
        eventHandler.onAppResume()
        
        // Then
        // Advance coroutines to ensure all async work completes
        advanceUntilIdle()
        
        // Verify that the service method was called
        verify(unreadTileService).onAppResume()
    }
    
    @Test
    fun `test initialize calls updateTileData`() = testScope.runBlockingTest {
        // When
        eventHandler.initialize()
        
        // Then
        // Advance coroutines to ensure all async work completes
        advanceUntilIdle()
        
        // Verify that updateTileData was called
        verify(unreadTileService).updateTileData()
    }
}
