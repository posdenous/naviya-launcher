package com.naviya.launcher.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.naviya.launcher.unread.UnreadTileEventHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * Unit tests for LauncherViewModel
 * Tests the integration with UnreadTileEventHandler to ensure events are properly triggered
 */
@ExperimentalCoroutinesApi
class LauncherViewModelTest {

    // Rule for LiveData testing
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    // Test dispatcher for controlled coroutine execution
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    // Mock dependencies
    private lateinit var unreadTileEventHandler: UnreadTileEventHandler
    private lateinit var viewModel: LauncherViewModel
    
    @Before
    fun setup() {
        // Set the main dispatcher for testing
        Dispatchers.setMain(testDispatcher)
        
        // Create mock event handler
        unreadTileEventHandler = mock()
        
        // Create the view model with mock dependencies
        viewModel = LauncherViewModel(unreadTileEventHandler)
    }
    
    @After
    fun tearDown() {
        // Reset the main dispatcher
        Dispatchers.resetMain()
    }
    
    @Test
    fun `test initialization calls event handler initialize`() {
        // Then
        // Verify that initialize was called during initialization
        verify(unreadTileEventHandler).initialize()
    }
    
    @Test
    fun `test onLauncherHomeOpened calls event handler`() {
        // When
        viewModel.onLauncherHomeOpened()
        
        // Then
        verify(unreadTileEventHandler).onLauncherHomeOpened()
    }
    
    @Test
    fun `test onAppResume calls event handler`() {
        // When
        viewModel.onAppResume()
        
        // Then
        verify(unreadTileEventHandler).onAppResume()
    }
}
