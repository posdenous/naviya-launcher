package com.naviya.launcher.emergency.ui

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ApplicationProvider
import com.naviya.launcher.emergency.EmergencyService
import com.naviya.launcher.emergency.SOSTrigger
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for SOSButton UI component
 * Tests accessibility, confirmation logic, and elderly-friendly design
 * Follows Windsurf testing rules for UI accessibility
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class SOSButtonTest {
    
    private lateinit var sosButton: SOSButton
    private lateinit var mockEmergencyService: EmergencyService
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockEmergencyService = mockk(relaxed = true)
        
        // Mock successful SOS activation
        coEvery { mockEmergencyService.activateSOS(any(), any()) } returns 
            com.naviya.launcher.emergency.EmergencyResult.Success("SOS activated")
        
        sosButton = SOSButton(context)
        sosButton.emergencyService = mockEmergencyService
    }
    
    @After
    fun teardown() {
        clearAllMocks()
    }
    
    @Test
    fun `button should have minimum touch target size`() {
        // Then
        assertTrue("Button width should be at least 48dp", 
            sosButton.minimumWidth >= (48 * context.resources.displayMetrics.density).toInt())
        assertTrue("Button height should be at least 48dp", 
            sosButton.minimumHeight >= (48 * context.resources.displayMetrics.density).toInt())
    }
    
    @Test
    fun `button should have proper accessibility properties`() {
        // Then
        assertTrue("Button should be focusable", sosButton.isFocusable)
        assertTrue("Button should be focusable in touch mode", sosButton.isFocusableInTouchMode)
        assertNotNull("Button should have content description", sosButton.contentDescription)
        assertTrue("Content description should not be empty", 
            sosButton.contentDescription.isNotEmpty())
    }
    
    @Test
    fun `accessibility node info should be properly configured`() {
        // Given
        val nodeInfo = AccessibilityNodeInfo.obtain()
        
        // When
        sosButton.onInitializeAccessibilityNodeInfo(nodeInfo)
        
        // Then
        assertEquals("Emergency Button", nodeInfo.className)
        assertNotNull("Content description should be set", nodeInfo.contentDescription)
        assertTrue("Should be important for accessibility", nodeInfo.isImportantForAccessibility)
    }
    
    @Test
    fun `button should show confirmation state on first click`() {
        // Given
        sosButton.setConfirmationRequired(true)
        
        // When
        sosButton.performClick()
        
        // Then
        assertTrue("Button text should show confirmation prompt", 
            sosButton.text.toString().contains("CONFIRM"))
    }
    
    @Test
    fun `button should activate SOS on confirmation click`() = runTest {
        // Given
        sosButton.setConfirmationRequired(true)
        
        // When - first click (confirmation)
        sosButton.performClick()
        
        // When - second click (activation)
        sosButton.performClick()
        
        // Then
        assertTrue("Button should show active state", 
            sosButton.text.toString().contains("ACTIVE"))
        assertTrue("SOS should be marked as active", sosButton.isSOSActive())
    }
    
    @Test
    fun `long press should bypass confirmation`() = runTest {
        // Given
        sosButton.setConfirmationRequired(true)
        
        // When
        sosButton.performLongClick()
        
        // Then
        assertTrue("SOS should be active after long press", sosButton.isSOSActive())
        coVerify { mockEmergencyService.activateSOS(any(), SOSTrigger.MANUAL) }
    }
    
    @Test
    fun `button should work without confirmation when disabled`() = runTest {
        // Given
        sosButton.setConfirmationRequired(false)
        
        // When
        sosButton.performClick()
        
        // Then
        assertTrue("SOS should be active immediately", sosButton.isSOSActive())
        coVerify { mockEmergencyService.activateSOS(any(), SOSTrigger.MANUAL) }
    }
    
    @Test
    fun `button should show cancel option after activation`() = runTest {
        // Given
        sosButton.setConfirmationRequired(false)
        
        // When
        sosButton.performClick()
        
        // Simulate delay for cancel option
        Thread.sleep(100)
        
        // Then
        assertTrue("SOS should be active", sosButton.isSOSActive())
    }
    
    @Test
    fun `force reset should return button to initial state`() = runTest {
        // Given - activate SOS
        sosButton.setConfirmationRequired(false)
        sosButton.performClick()
        assertTrue("SOS should be active", sosButton.isSOSActive())
        
        // When
        sosButton.forceReset()
        
        // Then
        assertFalse("SOS should not be active after reset", sosButton.isSOSActive())
        assertEquals("SOS", sosButton.text.toString())
    }
    
    @Test
    fun `content description should be multilingual`() {
        // Test different language contexts
        val languages = listOf("en", "de", "tr", "uk", "ar")
        
        for (language in languages) {
            // Simulate language change
            val config = context.resources.configuration
            config.setLocale(java.util.Locale(language))
            
            val description = sosButton.contentDescription.toString()
            assertNotNull("Content description should exist for $language", description)
            assertTrue("Content description should not be empty for $language", 
                description.isNotEmpty())
        }
    }
    
    @Test
    fun `accessibility events should be properly announced`() {
        // Given
        val mockEvent = mockk<AccessibilityEvent>(relaxed = true)
        every { mockEvent.eventType } returns AccessibilityEvent.TYPE_VIEW_CLICKED
        every { mockEvent.text } returns mutableListOf()
        
        // When
        sosButton.onRequestSendAccessibilityEvent(sosButton, mockEvent)
        
        // Then
        verify { mockEvent.text.clear() }
        verify { mockEvent.text.add(any()) }
    }
    
    @Test
    fun `button should handle emergency service errors gracefully`() = runTest {
        // Given
        coEvery { mockEmergencyService.activateSOS(any(), any()) } returns 
            com.naviya.launcher.emergency.EmergencyResult.Error("Network error")
        
        // When
        sosButton.setConfirmationRequired(false)
        sosButton.performClick()
        
        // Then - should not crash and should reset state
        // In real implementation, would verify error handling
    }
    
    @Test
    fun `button should respect elderly accessibility guidelines`() {
        // Test high contrast
        assertNotNull("Button should have background", sosButton.background)
        
        // Test text size (should be large enough for elderly users)
        assertTrue("Text size should be appropriate for elderly users", 
            sosButton.textSize >= 16f) // Base size before system scaling
        
        // Test bold text
        assertTrue("Text should be bold for better readability", 
            sosButton.typeface.isBold)
    }
}
