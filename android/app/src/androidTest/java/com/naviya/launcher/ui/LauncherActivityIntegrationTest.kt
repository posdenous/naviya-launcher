package com.naviya.launcher.ui

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.naviya.launcher.unread.UnreadTileService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration test for LauncherActivity
 * Tests the full integration of the activity with the view model and services
 * Verifies that lifecycle events properly trigger unread tile updates
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class LauncherActivityIntegrationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<LauncherActivity>()
    
    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_SMS
    )
    
    @Inject
    lateinit var unreadTileService: UnreadTileService
    
    @Inject
    lateinit var viewModel: LauncherViewModel
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun testUnreadTileDisplaysCorrectly() {
        // Verify the unread tile is displayed
        composeTestRule.onNodeWithTag("unread_tile").assertIsDisplayed()
        
        // Verify the title is displayed
        composeTestRule.onNodeWithText("Unread").assertIsDisplayed()
    }
    
    @Test
    fun testUnreadCountDisplayed() = runTest {
        // The activity should have triggered onLauncherHomeOpened during onCreate
        // which should have updated the unread count
        
        // Wait for the UI to update
        composeTestRule.waitForIdle()
        
        // Verify the badge with unread count is displayed
        // Note: The actual count will depend on the test device's data
        composeTestRule.onNodeWithTag("unread_badge").assertIsDisplayed()
    }
    
    @Test
    fun testReminderDisplayedWhenUnreadItemsExist() = runTest {
        // Force a reminder to be displayed
        unreadTileService.updateReminder("You have missed calls or messages.")
        
        // Wait for the UI to update
        composeTestRule.waitForIdle()
        
        // Verify the reminder is displayed
        composeTestRule.onNodeWithText("You have missed calls or messages.").assertIsDisplayed()
    }
    
    @Test
    fun testCaregiverNoteDisplayed() = runTest {
        // Force a caregiver note to be displayed
        unreadTileService.updateNote("Caregiver not available.")
        
        // Wait for the UI to update
        composeTestRule.waitForIdle()
        
        // Verify the note is displayed
        composeTestRule.onNodeWithText("Caregiver not available.").assertIsDisplayed()
    }
    
    @Test
    fun testLifecycleEventsUpdateTile() {
        // The activity's onCreate should have triggered onLauncherHomeOpened
        // Verify the tile is displayed with data
        composeTestRule.onNodeWithTag("unread_tile").assertIsDisplayed()
        
        // Simulate activity recreation to trigger onResume
        composeTestRule.activityRule.scenario.recreate()
        
        // This should have triggered onAppResume
        // Verify the tile is still displayed with data
        composeTestRule.onNodeWithTag("unread_tile").assertIsDisplayed()
    }
    
    /**
     * Note: For a complete integration test, you would need to:
     * 
     * 1. Set up test data in content providers
     * 2. Verify specific counts and text based on that data
     * 3. Test permission changes and their effect on the UI
     * 
     * This test provides the structure but would need additional
     * implementation for a complete integration test.
     */
}
