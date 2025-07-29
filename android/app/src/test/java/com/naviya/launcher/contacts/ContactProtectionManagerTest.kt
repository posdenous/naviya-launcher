package com.naviya.launcher.contacts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.naviya.launcher.caregiver.CaregiverPermissionManager
import com.naviya.launcher.contacts.data.ContactDao
import com.naviya.launcher.contacts.data.*
import com.naviya.launcher.emergency.EmergencyService
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

/**
 * Unit tests for ContactProtectionManager
 * Tests abuse prevention, user autonomy, and security features
 */
@RunWith(MockitoJUnitRunner::class)
class ContactProtectionManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var contactDao: ContactDao

    @Mock
    private lateinit var caregiverPermissionManager: CaregiverPermissionManager

    @Mock
    private lateinit var emergencyService: EmergencyService

    private lateinit var contactProtectionManager: ContactProtectionManager

    private val testUserId = "test-user-123"
    private val testCaregiverId = "test-caregiver-456"
    private val testContactId = "test-contact-789"

    @Before
    fun setUp() {
        contactProtectionManager = ContactProtectionManager(
            context = context,
            contactDao = contactDao,
            caregiverPermissionManager = caregiverPermissionManager,
            emergencyService = emergencyService
        )
    }

    @Test
    fun `test caregiver contact removal is blocked`() = runTest {
        // Given
        val testContact = createTestProtectedContact()
        `when`(contactDao.getContactById(testContactId)).thenReturn(testContact)

        // When
        val result = contactProtectionManager.blockContactRemoval(
            caregiverId = testCaregiverId,
            contactId = testContactId,
            userId = testUserId
        )

        // Then
        assertTrue("Contact removal should be blocked", result is ContactProtectionResult.Blocked)
        
        // Verify audit logging
        verify(contactDao).insertContactModificationAttempt(
            argThat { attempt ->
                attempt.caregiverId == testCaregiverId &&
                attempt.action == ContactAction.REMOVE_CONTACT &&
                attempt.result == ContactActionResult.BLOCKED_BY_PROTECTION
            }
        )
    }

    @Test
    fun `test caregiver contact blocking is blocked`() = runTest {
        // Given
        val testContact = createTestProtectedContact()
        `when`(contactDao.getContactById(testContactId)).thenReturn(testContact)

        // When
        val result = contactProtectionManager.blockContactBlocking(
            caregiverId = testCaregiverId,
            contactId = testContactId,
            userId = testUserId
        )

        // Then
        assertTrue("Contact blocking should be blocked", result is ContactProtectionResult.Blocked)
        
        // Verify audit logging
        verify(contactDao).insertContactModificationAttempt(
            argThat { attempt ->
                attempt.caregiverId == testCaregiverId &&
                attempt.action == ContactAction.BLOCK_CONTACT &&
                attempt.result == ContactActionResult.BLOCKED_BY_PROTECTION
            }
        )
    }

    @Test
    fun `test caregiver contact addition requires user approval`() = runTest {
        // Given
        `when`(caregiverPermissionManager.hasPermission(testCaregiverId, "suggest_contacts"))
            .thenReturn(true)

        val contactRequest = ContactAdditionRequest(
            name = "Dr. Smith",
            phoneNumber = "+1234567890",
            relationship = "doctor",
            reason = "Primary care physician"
        )

        // When
        val result = contactProtectionManager.requestContactAddition(
            caregiverId = testCaregiverId,
            contactInfo = contactRequest,
            userId = testUserId
        )

        // Then
        assertTrue("Contact addition should require user approval", 
                  result is ContactProtectionResult.PendingUserApproval)
        
        // Verify pending request is created
        verify(contactDao).insertPendingContactRequest(
            argThat { request ->
                request.caregiverId == testCaregiverId &&
                request.userId == testUserId &&
                request.contactInfo.name == "Dr. Smith" &&
                request.status == RequestStatus.PENDING_USER_APPROVAL
            }
        )
    }

    @Test
    fun `test caregiver without permission cannot suggest contacts`() = runTest {
        // Given
        `when`(caregiverPermissionManager.hasPermission(testCaregiverId, "suggest_contacts"))
            .thenReturn(false)

        val contactRequest = ContactAdditionRequest(
            name = "Test Contact",
            phoneNumber = "+1234567890",
            relationship = "friend"
        )

        // When
        val result = contactProtectionManager.requestContactAddition(
            caregiverId = testCaregiverId,
            contactInfo = contactRequest,
            userId = testUserId
        )

        // Then
        assertTrue("Contact addition should be denied without permission", 
                  result is ContactProtectionResult.PermissionDenied)
        
        // Verify audit logging
        verify(contactDao).insertContactModificationAttempt(
            argThat { attempt ->
                attempt.result == ContactActionResult.PERMISSION_DENIED
            }
        )
    }

    @Test
    fun `test user can add contacts freely`() = runTest {
        // Given
        val contactInfo = ContactInfo(
            name = "My Friend",
            phoneNumber = "+1234567890",
            relationship = "friend"
        )

        // When
        val result = contactProtectionManager.userAddContact(
            userId = testUserId,
            contactInfo = contactInfo
        )

        // Then
        assertTrue("User should be able to add contacts freely", 
                  result is ContactProtectionResult.Success)
        
        // Verify contact is added as protected
        verify(contactDao).insertProtectedContact(
            argThat { contact ->
                contact.userId == testUserId &&
                contact.contactInfo.name == "My Friend" &&
                contact.canBeRemovedByCaregiver == false
            }
        )
    }

    @Test
    fun `test user can remove non-essential contacts`() = runTest {
        // Given
        val testContact = createTestProtectedContact(contactType = "user_added")
        `when`(contactDao.getProtectedContact(testContactId)).thenReturn(testContact)
        `when`(emergencyService.getEmergencyContacts(testUserId)).thenReturn(
            listOf(mock(), mock()) // Multiple emergency contacts
        )

        // When
        val result = contactProtectionManager.userRemoveContact(
            userId = testUserId,
            contactId = testContactId
        )

        // Then
        assertTrue("User should be able to remove non-essential contacts", 
                  result is ContactProtectionResult.Success)
        
        verify(contactDao).deleteProtectedContact(testContactId)
    }

    @Test
    fun `test user cannot remove elder rights advocate without warning`() = runTest {
        // Given
        val elderRightsContact = createTestProtectedContact(contactType = "elder_rights_advocate")
        `when`(contactDao.getProtectedContact(testContactId)).thenReturn(elderRightsContact)

        // When
        val result = contactProtectionManager.userRemoveContact(
            userId = testUserId,
            contactId = testContactId
        )

        // Then
        assertTrue("User should get warning when removing elder rights advocate", 
                  result is ContactProtectionResult.Warning)
        
        // Verify contact is NOT deleted without explicit confirmation
        verify(contactDao, never()).deleteProtectedContact(testContactId)
    }

    @Test
    fun `test user cannot remove last emergency contact`() = runTest {
        // Given
        val emergencyContact = createTestProtectedContact(contactType = "emergency")
        `when`(contactDao.getProtectedContact(testContactId)).thenReturn(emergencyContact)
        `when`(emergencyService.getEmergencyContacts(testUserId)).thenReturn(
            listOf(mock()) // Only one emergency contact
        )

        // When
        val result = contactProtectionManager.userRemoveContact(
            userId = testUserId,
            contactId = testContactId
        )

        // Then
        assertTrue("User should not be able to remove last emergency contact", 
                  result is ContactProtectionResult.Error)
        
        verify(contactDao, never()).deleteProtectedContact(testContactId)
    }

    @Test
    fun `test user can approve pending contact requests`() = runTest {
        // Given
        val pendingRequest = createTestPendingRequest()
        `when`(contactDao.getPendingContactRequest("request-123")).thenReturn(pendingRequest)

        // When
        val result = contactProtectionManager.respondToPendingRequest(
            userId = testUserId,
            requestId = "request-123",
            approved = true,
            userNote = "Approved - my doctor"
        )

        // Then
        assertTrue("User should be able to approve pending requests", 
                  result is ContactProtectionResult.Success)
        
        // Verify request is updated
        verify(contactDao).updatePendingContactRequest(
            argThat { request ->
                request.status == RequestStatus.APPROVED &&
                request.userResponse == true &&
                request.userNote == "Approved - my doctor"
            }
        )
        
        // Verify contact is added
        verify(contactDao).insertProtectedContact(
            argThat { contact ->
                contact.userId == testUserId &&
                contact.userApproved == true
            }
        )
    }

    @Test
    fun `test user can reject pending contact requests`() = runTest {
        // Given
        val pendingRequest = createTestPendingRequest()
        `when`(contactDao.getPendingContactRequest("request-123")).thenReturn(pendingRequest)

        // When
        val result = contactProtectionManager.respondToPendingRequest(
            userId = testUserId,
            requestId = "request-123",
            approved = false,
            userNote = "Not needed"
        )

        // Then
        assertTrue("User should be able to reject pending requests", 
                  result is ContactProtectionResult.Success)
        
        // Verify request is updated
        verify(contactDao).updatePendingContactRequest(
            argThat { request ->
                request.status == RequestStatus.REJECTED &&
                request.userResponse == false
            }
        )
        
        // Verify contact is NOT added
        verify(contactDao, never()).insertProtectedContact(any())
    }

    @Test
    fun `test abuse pattern detection triggers after multiple blocked attempts`() = runTest {
        // Given
        val blockedAttempts = listOf(
            createBlockedAttempt(ContactAction.REMOVE_CONTACT),
            createBlockedAttempt(ContactAction.BLOCK_CONTACT),
            createBlockedAttempt(ContactAction.REMOVE_CONTACT)
        )
        
        `when`(contactDao.getRecentContactModificationAttempts(
            eq(testCaregiverId), eq(testUserId), any()
        )).thenReturn(blockedAttempts)

        val testContact = createTestProtectedContact()
        `when`(contactDao.getContactById(testContactId)).thenReturn(testContact)

        // When
        contactProtectionManager.blockContactRemoval(
            caregiverId = testCaregiverId,
            contactId = testContactId,
            userId = testUserId
        )

        // Then
        // Verify abuse pattern is flagged
        verify(caregiverPermissionManager).flagPotentialAbuse(
            eq(testCaregiverId),
            eq("contact_manipulation_attempts"),
            eq("medium"),
            any(),
            any()
        )
    }

    @Test
    fun `test elder rights advocate contact is automatically created`() = runTest {
        // Given
        `when`(contactDao.getContactByType(testUserId, "elder_rights_advocate"))
            .thenReturn(null)

        // When
        contactProtectionManager.initializeContactProtection(testUserId)

        // Then
        verify(contactDao).insertProtectedContact(
            argThat { contact ->
                contact.contactType == "elder_rights_advocate" &&
                contact.contactInfo.name == "Elder Rights Advocate" &&
                contact.contactInfo.phoneNumber == "+1-800-677-1116" &&
                contact.protectionLevel == ProtectionLevel.SYSTEM_PROTECTED &&
                contact.canBeRemovedByCaregiver == false
            }
        )
    }

    @Test
    fun `test contact protection initialization loads existing contacts`() = runTest {
        // Given
        val existingContacts = listOf(
            createTestProtectedContact(),
            createTestProtectedContact(contactType = "emergency")
        )
        `when`(contactDao.getProtectedContacts(testUserId)).thenReturn(existingContacts)
        `when`(contactDao.getContactByType(testUserId, "elder_rights_advocate"))
            .thenReturn(createTestProtectedContact(contactType = "elder_rights_advocate"))

        // When
        contactProtectionManager.initializeContactProtection(testUserId)

        // Then
        verify(contactDao).getProtectedContacts(testUserId)
        // Verify contacts are loaded into state flow
        assertEquals(existingContacts, contactProtectionManager.protectedContacts.value)
    }

    @Test
    fun `test pending contact requests are retrieved correctly`() = runTest {
        // Given
        val pendingRequests = listOf(
            createTestPendingRequest(),
            createTestPendingRequest(requestId = "request-456")
        )
        `when`(contactDao.getPendingContactRequests(testUserId)).thenReturn(pendingRequests)

        // When
        val result = contactProtectionManager.getPendingContactRequests(testUserId)

        // Then
        assertEquals(pendingRequests, result)
        verify(contactDao).getPendingContactRequests(testUserId)
    }

    // Helper methods for creating test data

    private fun createTestProtectedContact(
        contactId: String = testContactId,
        contactType: String = "user_added"
    ): ProtectedContact {
        return ProtectedContact(
            contactId = contactId,
            userId = testUserId,
            contactInfo = ContactInfo(
                name = "Test Contact",
                phoneNumber = "+1234567890",
                relationship = "friend"
            ),
            contactType = contactType,
            protectionLevel = ProtectionLevel.USER_CONTROLLED,
            addedBy = testUserId,
            canBeRemovedByCaregiver = false
        )
    }

    private fun createTestPendingRequest(
        requestId: String = "request-123"
    ): PendingContactRequest {
        return PendingContactRequest(
            requestId = requestId,
            caregiverId = testCaregiverId,
            userId = testUserId,
            contactInfo = ContactInfo(
                name = "Dr. Smith",
                phoneNumber = "+1234567890",
                relationship = "doctor"
            ),
            requestType = ContactRequestType.ADD_CONTACT,
            status = RequestStatus.PENDING_USER_APPROVAL
        )
    }

    private fun createBlockedAttempt(action: ContactAction): ContactModificationAttempt {
        return ContactModificationAttempt(
            caregiverId = testCaregiverId,
            userId = testUserId,
            action = action,
            result = ContactActionResult.BLOCKED_BY_PROTECTION,
            timestamp = System.currentTimeMillis()
        )
    }
}
