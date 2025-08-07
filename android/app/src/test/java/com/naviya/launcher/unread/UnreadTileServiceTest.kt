package com.naviya.launcher.unread

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.Telephony
import androidx.core.content.ContextCompat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for UnreadTileService
 * Tests the core functionality of the unread tile service according to requirements:
 * - Show total count of missed calls + unread SMS in a large tile
 * - Works offline using local call log and SMS inbox access
 * - Updates on launcher_home_opened and app_resume events
 * - Shows reminder if total_unread > 0
 * - Adds caregiver availability note if offline
 */
@ExperimentalCoroutinesApi
class UnreadTileServiceTest {

    // Test dispatcher for controlled coroutine execution
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    // Mock dependencies
    private lateinit var context: Context
    private lateinit var contentResolver: android.content.ContentResolver
    private lateinit var unreadTileService: UnreadTileService
    
    // Mock cursors for call log and SMS
    private lateinit var callLogCursor: Cursor
    private lateinit var smsCursor: Cursor
    
    @Before
    fun setup() {
        // Create mock context and content resolver
        context = mock()
        contentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        
        // Create mock cursors
        callLogCursor = mock()
        smsCursor = mock()
        
        // Set up permission checks to return granted by default
        whenever(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG))
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS))
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        
        // Create the service with mock context
        unreadTileService = UnreadTileService(context)
    }
    
    @Test
    fun `test readMissedCalls returns correct count`() {
        // Given
        whenever(callLogCursor.count).thenReturn(3)
        whenever(contentResolver.query(
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(callLogCursor)
        
        // When
        val result = unreadTileService.readMissedCalls()
        
        // Then
        assertEquals(3, result)
        
        // Verify query was made with correct parameters
        verify(contentResolver).query(
            eq(CallLog.Calls.CONTENT_URI),
            any(),
            any(),
            any(),
            any()
        )
    }
    
    @Test
    fun `test readUnreadSms returns correct count`() {
        // Given
        whenever(smsCursor.count).thenReturn(5)
        whenever(contentResolver.query(
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(smsCursor)
        
        // When
        val result = unreadTileService.readUnreadSms()
        
        // Then
        assertEquals(5, result)
        
        // Verify query was made with correct parameters
        verify(contentResolver).query(
            eq(Telephony.Sms.Inbox.CONTENT_URI),
            any(),
            any(),
            any(),
            any()
        )
    }
    
    @Test
    fun `test updateTileData updates state correctly`() = testScope.runTest {
        // Given
        whenever(callLogCursor.count).thenReturn(2)
        whenever(smsCursor.count).thenReturn(3)
        whenever(contentResolver.query(
            argThat { this == CallLog.Calls.CONTENT_URI },
            any(),
            any(),
            any(),
            any()
        )).thenReturn(callLogCursor)
        whenever(contentResolver.query(
            argThat { this == Telephony.Sms.Inbox.CONTENT_URI },
            any(),
            any(),
            any(),
            any()
        )).thenReturn(smsCursor)
        
        // When
        unreadTileService.updateTileData()
        advanceUntilIdle() // Advance coroutines to complete
        
        // Then
        val tileData = unreadTileService.tileData.first()
        assertEquals(5, tileData.totalUnread) // 2 calls + 3 SMS
        assertEquals(2, tileData.missedCalls)
        assertEquals(false, tileData.caregiverOnline) // Default is false in our implementation
        
        // Verify reminder is shown for unread items
        val reminder = unreadTileService.reminderText.first()
        assertEquals("You have missed calls or messages.", reminder)
        
        // Verify caregiver note is added when offline
        val note = unreadTileService.noteText.first()
        assertEquals("Caregiver not available.", note)
    }
    
    @Test
    fun `test onLauncherHomeOpened updates tile data`() = testScope.runTest {
        // Given
        val spyService = spy(unreadTileService)
        doSuspend { spyService.updateTileData() }.thenReturn(Unit)
        
        // When
        spyService.onLauncherHomeOpened()
        advanceUntilIdle() // Advance coroutines to complete
        
        // Then
        verify(spyService).updateTileData()
    }
    
    @Test
    fun `test onAppResume updates tile data`() = testScope.runTest {
        // Given
        val spyService = spy(unreadTileService)
        doSuspend { spyService.updateTileData() }.thenReturn(Unit)
        
        // When
        spyService.onAppResume()
        advanceUntilIdle() // Advance coroutines to complete
        
        // Then
        verify(spyService).updateTileData()
    }
    
    @Test
    fun `test getUnreadBreakdown returns correct string`() = testScope.runTest {
        // Given
        whenever(callLogCursor.count).thenReturn(2)
        whenever(smsCursor.count).thenReturn(3)
        whenever(contentResolver.query(any(), any(), any(), any(), any()))
            .thenReturn(callLogCursor)
            .thenReturn(smsCursor)
        
        // When
        unreadTileService.updateTileData()
        advanceUntilIdle() // Advance coroutines to complete
        
        // Then
        val breakdown = unreadTileService.getUnreadBreakdown()
        assertEquals("2 missed calls, 3 unread messages", breakdown)
    }
    
    @Test
    fun `test missing permissions returns zero counts`() {
        // Given
        whenever(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        
        // When
        val callCount = unreadTileService.readMissedCalls()
        val smsCount = unreadTileService.readUnreadSms()
        
        // Then
        assertEquals(0, callCount)
        assertEquals(0, smsCount)
        
        // Verify content resolver was not queried
        verify(contentResolver, never()).query(any(), any(), any(), any(), any())
    }
}
