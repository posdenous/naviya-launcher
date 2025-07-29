package com.naviya.launcher.contacts.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naviya.launcher.contacts.*
import com.naviya.launcher.database.NaviyaDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Unit tests for ContactDao
 * Tests database operations for contact protection system
 */
@RunWith(AndroidJUnit4::class)
class ContactDaoTest {

    private lateinit var database: NaviyaDatabase
    private lateinit var contactDao: ContactDao

    private val testUserId = "test-user-123"
    private val testCaregiverId = "test-caregiver-456"

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NaviyaDatabase::class.java
        ).allowMainThreadQueries().build()
        
        contactDao = database.contactDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test insert and retrieve protected contact`() = runTest {
        // Given
        val protectedContact = createTestProtectedContact()

        // When
        val insertId = contactDao.insertProtectedContact(protectedContact)
        val retrievedContact = contactDao.getProtectedContact(protectedContact.contactId)

        // Then
        assertTrue("Insert should return positive ID", insertId > 0)
        assertNotNull("Contact should be retrievable", retrievedContact)
        assertEquals("Contact name should match", protectedContact.contactInfo.name, 
                    retrievedContact?.contactInfo?.name)
        assertEquals("User ID should match", protectedContact.userId, retrievedContact?.userId)
        assertFalse("Contact should not be removable by caregiver", 
                   retrievedContact?.canBeRemovedByCaregiver ?: true)
    }

    @Test
    fun `test get protected contacts by user`() = runTest {
        // Given
        val contact1 = createTestProtectedContact(contactId = "contact-1", name = "Contact 1")
        val contact2 = createTestProtectedContact(contactId = "contact-2", name = "Contact 2")
        val otherUserContact = createTestProtectedContact(
            contactId = "contact-3", 
            userId = "other-user",
            name = "Other User Contact"
        )

        contactDao.insertProtectedContact(contact1)
        contactDao.insertProtectedContact(contact2)
        contactDao.insertProtectedContact(otherUserContact)

        // When
        val userContacts = contactDao.getProtectedContacts(testUserId)

        // Then
        assertEquals("Should return 2 contacts for test user", 2, userContacts.size)
        assertTrue("Should contain contact 1", 
                  userContacts.any { it.contactInfo.name == "Contact 1" })
        assertTrue("Should contain contact 2", 
                  userContacts.any { it.contactInfo.name == "Contact 2" })
        assertFalse("Should not contain other user's contact", 
                   userContacts.any { it.contactInfo.name == "Other User Contact" })
    }

    @Test
    fun `test get emergency contacts only`() = runTest {
        // Given
        val emergencyContact = createTestProtectedContact(
            contactId = "emergency-1",
            name = "Emergency Contact",
            emergencyContact = true
        )
        val regularContact = createTestProtectedContact(
            contactId = "regular-1",
            name = "Regular Contact",
            emergencyContact = false
        )

        contactDao.insertProtectedContact(emergencyContact)
        contactDao.insertProtectedContact(regularContact)

        // When
        val emergencyContacts = contactDao.getEmergencyContacts(testUserId)

        // Then
        assertEquals("Should return 1 emergency contact", 1, emergencyContacts.size)
        assertEquals("Should be the emergency contact", "Emergency Contact", 
                    emergencyContacts[0].contactInfo.name)
        assertTrue("Should be marked as emergency", emergencyContacts[0].emergencyContact)
    }

    @Test
    fun `test get contact by type`() = runTest {
        // Given
        val elderRightsContact = createTestProtectedContact(
            contactId = "elder-rights-1",
            name = "Elder Rights Advocate",
            contactType = "elder_rights_advocate"
        )
        val familyContact = createTestProtectedContact(
            contactId = "family-1",
            name = "Family Member",
            contactType = "family"
        )

        contactDao.insertProtectedContact(elderRightsContact)
        contactDao.insertProtectedContact(familyContact)

        // When
        val retrievedElderRights = contactDao.getContactByType(testUserId, "elder_rights_advocate")
        val retrievedFamily = contactDao.getContactByType(testUserId, "family")
        val nonExistent = contactDao.getContactByType(testUserId, "medical")

        // Then
        assertNotNull("Should find elder rights contact", retrievedElderRights)
        assertEquals("Should be elder rights contact", "Elder Rights Advocate", 
                    retrievedElderRights?.contactInfo?.name)
        
        assertNotNull("Should find family contact", retrievedFamily)
        assertEquals("Should be family contact", "Family Member", 
                    retrievedFamily?.contactInfo?.name)
        
        assertNull("Should not find non-existent type", nonExistent)
    }

    @Test
    fun `test insert and retrieve pending contact request`() = runTest {
        // Given
        val pendingRequest = createTestPendingRequest()

        // When
        val insertId = contactDao.insertPendingContactRequest(pendingRequest)
        val retrievedRequest = contactDao.getPendingContactRequest(pendingRequest.requestId)

        // Then
        assertTrue("Insert should return positive ID", insertId > 0)
        assertNotNull("Request should be retrievable", retrievedRequest)
        assertEquals("Caregiver ID should match", pendingRequest.caregiverId, 
                    retrievedRequest?.caregiverId)
        assertEquals("Status should be pending", RequestStatus.PENDING_USER_APPROVAL, 
                    retrievedRequest?.status)
    }

    @Test
    fun `test get pending requests by user`() = runTest {
        // Given
        val request1 = createTestPendingRequest(requestId = "req-1")
        val request2 = createTestPendingRequest(requestId = "req-2")
        val approvedRequest = createTestPendingRequest(
            requestId = "req-3",
            status = RequestStatus.APPROVED
        )

        contactDao.insertPendingContactRequest(request1)
        contactDao.insertPendingContactRequest(request2)
        contactDao.insertPendingContactRequest(approvedRequest)

        // When
        val pendingRequests = contactDao.getPendingContactRequests(testUserId)

        // Then
        assertEquals("Should return 2 pending requests", 2, pendingRequests.size)
        assertTrue("Should contain request 1", 
                  pendingRequests.any { it.requestId == "req-1" })
        assertTrue("Should contain request 2", 
                  pendingRequests.any { it.requestId == "req-2" })
        assertFalse("Should not contain approved request", 
                   pendingRequests.any { it.requestId == "req-3" })
    }

    @Test
    fun `test insert and retrieve contact modification attempt`() = runTest {
        // Given
        val attempt = createTestModificationAttempt()

        // When
        val insertId = contactDao.insertContactModificationAttempt(attempt)
        val retrievedAttempts = contactDao.getContactModificationAttempts(testUserId, 10)

        // Then
        assertTrue("Insert should return positive ID", insertId > 0)
        assertEquals("Should return 1 attempt", 1, retrievedAttempts.size)
        assertEquals("Caregiver ID should match", attempt.caregiverId, 
                    retrievedAttempts[0].caregiverId)
        assertEquals("Action should match", attempt.action, retrievedAttempts[0].action)
        assertEquals("Result should match", attempt.result, retrievedAttempts[0].result)
    }

    @Test
    fun `test get recent contact modification attempts`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentTime = currentTime - 1800000L // 30 minutes ago
        val oldTime = currentTime - 7200000L // 2 hours ago

        val recentAttempt = createTestModificationAttempt(
            attemptId = "recent-1",
            timestamp = recentTime
        )
        val oldAttempt = createTestModificationAttempt(
            attemptId = "old-1",
            timestamp = oldTime
        )

        contactDao.insertContactModificationAttempt(recentAttempt)
        contactDao.insertContactModificationAttempt(oldAttempt)

        // When
        val recentAttempts = contactDao.getRecentContactModificationAttempts(
            caregiverId = testCaregiverId,
            userId = testUserId,
            timeWindowStart = currentTime - 3600000L // 1 hour ago
        )

        // Then
        assertEquals("Should return 1 recent attempt", 1, recentAttempts.size)
        assertEquals("Should be the recent attempt", "recent-1", recentAttempts[0].attemptId)
    }

    @Test
    fun `test get blocked attempts count`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentTime = currentTime - 1800000L // 30 minutes ago

        val blockedAttempt1 = createTestModificationAttempt(
            attemptId = "blocked-1",
            result = ContactActionResult.BLOCKED_BY_PROTECTION,
            timestamp = recentTime
        )
        val blockedAttempt2 = createTestModificationAttempt(
            attemptId = "blocked-2",
            result = ContactActionResult.BLOCKED_BY_PROTECTION,
            timestamp = recentTime
        )
        val successfulAttempt = createTestModificationAttempt(
            attemptId = "success-1",
            result = ContactActionResult.SUCCESS,
            timestamp = recentTime
        )

        contactDao.insertContactModificationAttempt(blockedAttempt1)
        contactDao.insertContactModificationAttempt(blockedAttempt2)
        contactDao.insertContactModificationAttempt(successfulAttempt)

        // When
        val blockedCount = contactDao.getBlockedAttemptsCount(
            caregiverId = testCaregiverId,
            timeWindowStart = currentTime - 3600000L // 1 hour ago
        )

        // Then
        assertEquals("Should return 2 blocked attempts", 2, blockedCount)
    }

    @Test
    fun `test contact protection summary`() = runTest {
        // Given
        val emergencyContact = createTestProtectedContact(
            contactId = "emergency-1",
            emergencyContact = true
        )
        val systemContact = createTestProtectedContact(
            contactId = "system-1",
            systemContact = true
        )
        val regularContact = createTestProtectedContact(
            contactId = "regular-1"
        )
        val pendingRequest = createTestPendingRequest()

        contactDao.insertProtectedContact(emergencyContact)
        contactDao.insertProtectedContact(systemContact)
        contactDao.insertProtectedContact(regularContact)
        contactDao.insertPendingContactRequest(pendingRequest)

        // When
        val summary = contactDao.getContactProtectionSummary(testUserId)

        // Then
        assertNotNull("Summary should not be null", summary)
        assertEquals("Should have 3 total contacts", 3, summary?.totalProtectedContacts)
        assertEquals("Should have 1 emergency contact", 1, summary?.emergencyContactCount)
        assertEquals("Should have 1 system contact", 1, summary?.systemContactCount)
        assertEquals("Should have 3 fully protected contacts", 3, summary?.fullyProtectedCount)
        assertEquals("Should have 1 pending request", 1, summary?.pendingRequestCount)
    }

    @Test
    fun `test caregiver activity summary`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentTime = currentTime - 1800000L // 30 minutes ago

        val blockedAttempt = createTestModificationAttempt(
            result = ContactActionResult.BLOCKED_BY_PROTECTION,
            timestamp = recentTime
        )
        val successAttempt = createTestModificationAttempt(
            attemptId = "success-1",
            result = ContactActionResult.SUCCESS,
            timestamp = recentTime
        )
        val abusiveAttempt = createTestModificationAttempt(
            attemptId = "abusive-1",
            result = ContactActionResult.BLOCKED_BY_PROTECTION,
            flaggedAsAbusive = true,
            timestamp = recentTime
        )

        contactDao.insertContactModificationAttempt(blockedAttempt)
        contactDao.insertContactModificationAttempt(successAttempt)
        contactDao.insertContactModificationAttempt(abusiveAttempt)

        // When
        val summary = contactDao.getCaregiverActivitySummary(
            userId = testUserId,
            caregiverId = testCaregiverId,
            sinceTime = currentTime - 3600000L // 1 hour ago
        )

        // Then
        assertNotNull("Summary should not be null", summary)
        assertEquals("Should have 3 total attempts", 3, summary?.totalAttempts)
        assertEquals("Should have 2 blocked attempts", 2, summary?.blockedAttempts)
        assertEquals("Should have 1 successful attempt", 1, summary?.successfulAttempts)
        assertEquals("Should have 1 abusive attempt", 1, summary?.abusiveAttempts)
    }

    @Test
    fun `test caregiver abuse risk score calculation`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentTime = currentTime - 1800000L // 30 minutes ago

        // Create multiple blocked attempts to increase risk score
        repeat(4) { index ->
            val blockedAttempt = createTestModificationAttempt(
                attemptId = "blocked-$index",
                result = ContactActionResult.BLOCKED_BY_PROTECTION,
                timestamp = recentTime
            )
            contactDao.insertContactModificationAttempt(blockedAttempt)
        }

        // When
        val riskScore = contactDao.getCaregiverAbuseRiskScore(
            caregiverId = testCaregiverId,
            userId = testUserId,
            recentTimeWindow = currentTime - 3600000L // 1 hour ago
        )

        // Then
        assertNotNull("Risk score should not be null", riskScore)
        assertTrue("Risk score should be high (>= 0.6)", riskScore!! >= 0.6f)
    }

    @Test
    fun `test can contact be removed logic`() = runTest {
        // Given
        val elderRightsContact = createTestProtectedContact(
            contactId = "elder-rights",
            contactType = "elder_rights_advocate"
        )
        val regularContact = createTestProtectedContact(
            contactId = "regular",
            contactType = "user_added"
        )

        contactDao.insertProtectedContact(elderRightsContact)
        contactDao.insertProtectedContact(regularContact)

        // When
        val canRemoveElderRights = contactDao.canContactBeRemoved("elder-rights", testUserId)
        val canRemoveRegular = contactDao.canContactBeRemoved("regular", testUserId)

        // Then
        assertEquals("Elder rights contact should not be removable", false, canRemoveElderRights)
        assertEquals("Regular contact should be removable", true, canRemoveRegular)
    }

    @Test
    fun `test expire pending requests`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val expiredRequest = createTestPendingRequest(
            requestId = "expired-1",
            expiresAt = currentTime - 3600000L // Expired 1 hour ago
        )
        val validRequest = createTestPendingRequest(
            requestId = "valid-1",
            expiresAt = currentTime + 3600000L // Expires in 1 hour
        )

        contactDao.insertPendingContactRequest(expiredRequest)
        contactDao.insertPendingContactRequest(validRequest)

        // When
        contactDao.expirePendingRequests(currentTime)

        // Then
        val expiredRetrieved = contactDao.getPendingContactRequest("expired-1")
        val validRetrieved = contactDao.getPendingContactRequest("valid-1")

        assertTrue("Expired request should be marked as expired", 
                  expiredRetrieved?.isExpired == true)
        assertEquals("Expired request status should be EXPIRED", 
                    RequestStatus.EXPIRED, expiredRetrieved?.status)
        
        assertFalse("Valid request should not be expired", 
                   validRetrieved?.isExpired == true)
        assertEquals("Valid request should still be pending", 
                    RequestStatus.PENDING_USER_APPROVAL, validRetrieved?.status)
    }

    // Helper methods for creating test data

    private fun createTestProtectedContact(
        contactId: String = "test-contact-123",
        userId: String = testUserId,
        name: String = "Test Contact",
        contactType: String = "user_added",
        emergencyContact: Boolean = false,
        systemContact: Boolean = false
    ): ProtectedContact {
        return ProtectedContact(
            contactId = contactId,
            userId = userId,
            contactInfo = ContactInfo(
                name = name,
                phoneNumber = "+1234567890",
                relationship = "friend"
            ),
            contactType = contactType,
            protectionLevel = ProtectionLevel.USER_CONTROLLED,
            addedBy = userId,
            emergencyContact = emergencyContact,
            systemContact = systemContact,
            canBeRemovedByCaregiver = false
        )
    }

    private fun createTestPendingRequest(
        requestId: String = "test-request-123",
        status: RequestStatus = RequestStatus.PENDING_USER_APPROVAL,
        expiresAt: Long = System.currentTimeMillis() + 604800000L
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
            status = status,
            expiresAt = expiresAt
        )
    }

    private fun createTestModificationAttempt(
        attemptId: String = "test-attempt-123",
        result: ContactActionResult = ContactActionResult.BLOCKED_BY_PROTECTION,
        flaggedAsAbusive: Boolean = false,
        timestamp: Long = System.currentTimeMillis()
    ): ContactModificationAttempt {
        return ContactModificationAttempt(
            attemptId = attemptId,
            caregiverId = testCaregiverId,
            userId = testUserId,
            action = ContactAction.REMOVE_CONTACT,
            result = result,
            flaggedAsAbusive = flaggedAsAbusive,
            timestamp = timestamp
        )
    }
}
