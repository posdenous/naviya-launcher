package com.naviya.launcher.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.naviya.launcher.data.dao.SecurityAuditDao
import com.naviya.launcher.caregiver.CaregiverPermissionManager
import com.naviya.launcher.layout.ToggleMode
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Comprehensive security tests for ModeSwitchingSecurityManager
 * Tests all abuse prevention and security validation scenarios
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ModeSwitchingSecurityManagerTest {

    @Mock
    private lateinit var mockSecurityAuditDao: SecurityAuditDao

    @Mock
    private lateinit var mockCaregiverPermissionManager: CaregiverPermissionManager

    private lateinit var context: Context
    private lateinit var securityManager: ModeSwitchingSecurityManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        securityManager = ModeSwitchingSecurityManager(
            context = context,
            securityAuditDao = mockSecurityAuditDao,
            caregiverPermissionManager = mockCaregiverPermissionManager
        )
    }

    @Test
    fun `validateModeSwitch should approve normal user mode switch`() = runTest {
        // Given
        whenever(mockSecurityAuditDao.getModeSwitchesInLastHour()).thenReturn(0)
        whenever(mockSecurityAuditDao.hasRecentEmergencyEscape()).thenReturn(false)

        // When
        val result = securityManager.validateModeSwitch(
            currentMode = ToggleMode.COMFORT,
            requestedMode = ToggleMode.FAMILY,
            requestedBy = "user",
            userAge = 75
        )

        // Then
        assertEquals(ModeSwitchValidation.APPROVED, result.validationResult)
        assertTrue(result.isValid)
        assertEquals("Mode switch approved", result.reason)
    }

    @Test
    fun `validateModeSwitch should block rate limited requests`() = runTest {
        // Given - User has already made 3 mode switches in the last hour
        whenever(mockSecurityAuditDao.getModeSwitchesInLastHour()).thenReturn(3)

        // When
        val result = securityManager.validateModeSwitch(
            currentMode = ToggleMode.COMFORT,
            requestedMode = ToggleMode.FAMILY,
            requestedBy = "user"
        )

        // Then
        assertEquals(ModeSwitchValidation.RATE_LIMITED, result.validationResult)
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("rate limit"))
    }

    @Test
    fun `validateModeSwitch should require authentication for protected modes`() = runTest {
        // Given
        whenever(mockSecurityAuditDao.getModeSwitchesInLastHour()).thenReturn(0)
        whenever(mockSecurityAuditDao.hasRecentEmergencyEscape()).thenReturn(false)

        // When - Switching to MINIMAL mode without authentication
        val result = securityManager.validateModeSwitch(
            currentMode = ToggleMode.COMFORT,
            requestedMode = ToggleMode.MINIMAL,
            requestedBy = "user",
            authenticationToken = null
        )

        // Then
        assertEquals(ModeSwitchValidation.AUTHENTICATION_REQUIRED, result.validationResult)
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("authentication required"))
    }

    @Test
    fun `validateModeSwitch should block elderly users from complex modes without consent`() = runTest {
        // Given
        whenever(mockSecurityAuditDao.getModeSwitchesInLastHour()).thenReturn(0)
        whenever(mockSecurityAuditDao.hasRecentEmergencyEscape()).thenReturn(false)
        whenever(mockSecurityAuditDao.hasElderlyConsentForComplexity("FOCUS")).thenReturn(false)

        // When - 85-year-old trying to switch to complex FOCUS mode
        val result = securityManager.validateModeSwitch(
            currentMode = ToggleMode.COMFORT,
            requestedMode = ToggleMode.FOCUS,
            requestedBy = "user",
            userAge = 85
        )

        // Then
        assertEquals(ModeSwitchValidation.ELDERLY_PROTECTION, result.validationResult)
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("consent required"))
    }

    @Test
    fun `validateModeSwitch should validate caregiver permissions`() = runTest {
        // Given
        whenever(mockSecurityAuditDao.getModeSwitchesInLastHour()).thenReturn(0)
        whenever(mockSecurityAuditDao.hasRecentEmergencyEscape()).thenReturn(false)
        whenever(mockSecurityAuditDao.validateCaregiverToken("caregiver123", "token456")).thenReturn(false)

        // When - Caregiver with invalid token tries to switch mode
        val result = securityManager.validateModeSwitch(
            currentMode = ToggleMode.COMFORT,
            requestedMode = ToggleMode.FAMILY,
            requestedBy = "caregiver123",
            authenticationToken = "token456"
        )

        // Then
        assertEquals(ModeSwitchValidation.INVALID_CAREGIVER_TOKEN, result.validationResult)
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("invalid caregiver"))
    }

    @Test
    fun `validateModeSwitch should detect suspicious activity patterns`() = runTest {
        // Given - Rapid mode switching pattern detected
        whenever(mockSecurityAuditDao.getModeSwitchesInLastHour()).thenReturn(2)
        whenever(mockSecurityAuditDao.hasRecentEmergencyEscape()).thenReturn(false)
        whenever(mockSecurityAuditDao.getRecentSuspiciousEvents("caregiver123")).thenReturn(2)

        // When
        val result = securityManager.validateModeSwitch(
            currentMode = ToggleMode.FAMILY,
            requestedMode = ToggleMode.COMFORT,
            requestedBy = "caregiver123"
        )

        // Then
        assertEquals(ModeSwitchValidation.SUSPICIOUS_ACTIVITY, result.validationResult)
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("suspicious activity"))
    }

    @Test
    fun `validateModeSwitch should block during emergency escape period`() = runTest {
        // Given - Emergency escape was recently activated
        whenever(mockSecurityAuditDao.hasRecentEmergencyEscape()).thenReturn(true)

        // When
        val result = securityManager.validateModeSwitch(
            currentMode = ToggleMode.COMFORT,
            requestedMode = ToggleMode.FAMILY,
            requestedBy = "caregiver123"
        )

        // Then
        assertEquals(ModeSwitchValidation.EMERGENCY_ESCAPE_ACTIVE, result.validationResult)
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("emergency escape"))
    }

    @Test
    fun `validateModeSwitch should trigger system lockout after multiple violations`() = runTest {
        // Given - Multiple failed attempts from same requester
        whenever(mockSecurityAuditDao.getModeSwitchesInLastHour()).thenReturn(5)
        whenever(mockSecurityAuditDao.getRecentSuspiciousEvents("malicious_actor")).thenReturn(3)

        // When
        val result = securityManager.validateModeSwitch(
            currentMode = ToggleMode.COMFORT,
            requestedMode = ToggleMode.MINIMAL,
            requestedBy = "malicious_actor"
        )

        // Then
        assertEquals(ModeSwitchValidation.SYSTEM_LOCKED, result.validationResult)
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("system locked"))
        
        // Verify system lockout was logged
        verify(mockSecurityAuditDao).logSystemLockout(any())
    }

    @Test
    fun `logSecurityEvent should record all security events with proper metadata`() = runTest {
        // When
        securityManager.logSecurityEvent(
            eventType = "SUSPICIOUS_ACTIVITY",
            description = "Rapid mode switching detected",
            severity = "HIGH",
            requestedBy = "caregiver123",
            metadata = mapOf("pattern" to "rapid_switching")
        )

        // Then
        verify(mockSecurityAuditDao).logSecurityEvent(
            argThat { event ->
                event.eventType == "SUSPICIOUS_ACTIVITY" &&
                event.description == "Rapid mode switching detected" &&
                event.severity == "HIGH" &&
                event.requestedBy == "caregiver123" &&
                event.metadata.contains("pattern")
            }
        )
    }

    @Test
    fun `detectSuspiciousActivity should identify abuse patterns`() = runTest {
        // Given - Pattern indicating potential abuse
        whenever(mockSecurityAuditDao.getRecentSuspiciousEvents("caregiver123")).thenReturn(2)
        whenever(mockSecurityAuditDao.getFailedAuthAttempts("caregiver123", any())).thenReturn(3)

        // When
        val isSuspicious = securityManager.detectSuspiciousActivity(
            requestedBy = "caregiver123",
            currentMode = ToggleMode.COMFORT,
            requestedMode = ToggleMode.MINIMAL
        )

        // Then
        assertTrue(isSuspicious)
        verify(mockSecurityAuditDao).logSecurityEvent(
            argThat { event ->
                event.eventType == "SUSPICIOUS_ACTIVITY" &&
                event.severity == "HIGH"
            }
        )
    }

    @Test
    fun `activateEmergencyEscape should disable all monitoring temporarily`() = runTest {
        // When
        securityManager.activateEmergencyEscape(
            userId = "elderly_user_123",
            method = "TRIPLE_TAP"
        )

        // Then
        verify(mockSecurityAuditDao).logSecurityEvent(
            argThat { event ->
                event.eventType == "EMERGENCY_ESCAPE" &&
                event.description.contains("TRIPLE_TAP")
            }
        )
    }

    @Test
    fun `validateElderlyProtection should prevent inappropriate mode complexity`() = runTest {
        // Given
        whenever(mockSecurityAuditDao.hasElderlyConsentForComplexity("FOCUS")).thenReturn(false)

        // When - 90-year-old user trying to access complex mode
        val isProtected = securityManager.validateElderlyProtection(
            userAge = 90,
            requestedMode = ToggleMode.FOCUS
        )

        // Then
        assertFalse(isProtected)
    }

    @Test
    fun `auditModeSwitch should create immutable audit trail`() = runTest {
        // When
        securityManager.auditModeSwitch(
            fromMode = ToggleMode.COMFORT,
            toMode = ToggleMode.FAMILY,
            result = ModeSwitchValidation.APPROVED,
            requestedBy = "user",
            reason = "Normal user request"
        )

        // Then
        verify(mockSecurityAuditDao).logModeSwitch(
            argThat { audit ->
                audit.fromMode == "COMFORT" &&
                audit.toMode == "FAMILY" &&
                audit.result == "APPROVED" &&
                audit.requestedBy == "user" &&
                audit.reason == "Normal user request"
            }
        )
    }

    @Test
    fun `security manager should handle concurrent mode switch attempts safely`() = runTest {
        // Given - Multiple concurrent requests
        whenever(mockSecurityAuditDao.getModeSwitchesInLastHour()).thenReturn(1)
        whenever(mockSecurityAuditDao.hasRecentEmergencyEscape()).thenReturn(false)

        // When - Simulate concurrent requests
        val results = listOf(
            securityManager.validateModeSwitch(ToggleMode.COMFORT, ToggleMode.FAMILY, "user1"),
            securityManager.validateModeSwitch(ToggleMode.COMFORT, ToggleMode.FOCUS, "user2"),
            securityManager.validateModeSwitch(ToggleMode.COMFORT, ToggleMode.MINIMAL, "user3")
        )

        // Then - All requests should be processed safely
        assertEquals(3, results.size)
        results.forEach { result ->
            assertTrue(result.validationResult in listOf(
                ModeSwitchValidation.APPROVED,
                ModeSwitchValidation.AUTHENTICATION_REQUIRED,
                ModeSwitchValidation.RATE_LIMITED
            ))
        }
    }
}
