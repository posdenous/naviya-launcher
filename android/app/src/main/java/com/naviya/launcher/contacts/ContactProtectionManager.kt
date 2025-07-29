package com.naviya.launcher.contacts

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.naviya.launcher.caregiver.CaregiverPermissionManager
import com.naviya.launcher.emergency.EmergencyService

/**
 * Contact Protection Manager - Prevents caregiver abuse through contact manipulation
 * 
 * Key Protection Rules:
 * 1. Caregivers CANNOT remove or block existing contacts
 * 2. Caregivers can only ADD contacts with user approval
 * 3. Emergency contacts are completely protected from caregiver access
 * 4. Elder rights advocate contact cannot be removed by anyone except user
 * 5. All contact changes are logged in immutable audit trail
 * 6. User maintains full control over their contact list
 */
@Singleton
class ContactProtectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactDao: ContactDao,
    private val caregiverPermissionManager: CaregiverPermissionManager,
    private val emergencyService: EmergencyService
) {
    
    companion object {
        private const val TAG = "ContactProtectionManager"
        
        // Protected contact types that caregivers cannot modify
        private const val CONTACT_TYPE_EMERGENCY = "emergency"
        private const val CONTACT_TYPE_ELDER_RIGHTS = "elder_rights_advocate"
        private const val CONTACT_TYPE_FAMILY = "family"
        private const val CONTACT_TYPE_MEDICAL = "medical"
        private const val CONTACT_TYPE_USER_ADDED = "user_added"
        
        // Elder Rights Advocate - cannot be removed by caregivers
        private const val ELDER_RIGHTS_HOTLINE = "+1-800-677-1116" // National Elder Abuse Hotline
        private const val ELDER_RIGHTS_NAME = "Elder Rights Advocate"
    }
    
    private val _protectedContacts = MutableStateFlow<List<ProtectedContact>>(emptyList())
    val protectedContacts: StateFlow<List<ProtectedContact>> = _protectedContacts.asStateFlow()
    
    private val _contactModificationAttempts = MutableStateFlow<List<ContactModificationAttempt>>(emptyList())
    val contactModificationAttempts: StateFlow<List<ContactModificationAttempt>> = _contactModificationAttempts.asStateFlow()
    
    /**
     * Initialize contact protection system
     * Sets up elder rights advocate contact and loads protected contacts
     */
    suspend fun initializeContactProtection(userId: String) {
        try {
            // Ensure elder rights advocate contact exists and is protected
            ensureElderRightsAdvocateContact(userId)
            
            // Load all protected contacts
            loadProtectedContacts(userId)
            
            // Set up contact change monitoring
            setupContactChangeMonitoring(userId)
            
        } catch (e: Exception) {
            logContactProtectionError("Failed to initialize contact protection", e)
        }
    }
    
    /**
     * Attempt to add a contact (caregiver-initiated)
     * Requires user approval and logs the action
     */
    suspend fun requestContactAddition(
        caregiverId: String,
        contactInfo: ContactAdditionRequest,
        userId: String
    ): ContactProtectionResult {
        return try {
            // Verify caregiver has permission to suggest contacts
            if (!caregiverPermissionManager.hasPermission(caregiverId, "suggest_contacts")) {
                logContactModificationAttempt(
                    caregiverId = caregiverId,
                    action = ContactAction.ADD_CONTACT,
                    contactInfo = contactInfo.toContactInfo(),
                    result = ContactActionResult.PERMISSION_DENIED,
                    userId = userId
                )
                return ContactProtectionResult.PermissionDenied("Caregiver does not have permission to suggest contacts")
            }
            
            // Create pending contact addition request
            val pendingRequest = PendingContactRequest(
                requestId = java.util.UUID.randomUUID().toString(),
                caregiverId = caregiverId,
                userId = userId,
                contactInfo = contactInfo.toContactInfo(),
                requestType = ContactRequestType.ADD_CONTACT,
                requestReason = contactInfo.reason,
                requestTimestamp = System.currentTimeMillis(),
                status = RequestStatus.PENDING_USER_APPROVAL
            )
            
            // Save pending request
            contactDao.insertPendingContactRequest(pendingRequest)
            
            // Log the request attempt
            logContactModificationAttempt(
                caregiverId = caregiverId,
                action = ContactAction.ADD_CONTACT,
                contactInfo = contactInfo.toContactInfo(),
                result = ContactActionResult.PENDING_USER_APPROVAL,
                userId = userId
            )
            
            ContactProtectionResult.PendingUserApproval(
                requestId = pendingRequest.requestId,
                message = "Contact addition request sent to user for approval"
            )
            
        } catch (e: Exception) {
            logContactProtectionError("Failed to process contact addition request", e)
            ContactProtectionResult.Error("Failed to process contact addition request: ${e.message}")
        }
    }
    
    /**
     * Attempt to remove a contact (caregiver-initiated) - BLOCKED
     * This is explicitly prevented to avoid social isolation abuse
     */
    suspend fun blockContactRemoval(
        caregiverId: String,
        contactId: String,
        userId: String
    ): ContactProtectionResult {
        return try {
            val contact = contactDao.getContactById(contactId)
            
            // Log the blocked attempt
            logContactModificationAttempt(
                caregiverId = caregiverId,
                action = ContactAction.REMOVE_CONTACT,
                contactInfo = contact?.toContactInfo(),
                result = ContactActionResult.BLOCKED_BY_PROTECTION,
                userId = userId
            )
            
            // Check if this is a pattern of abuse attempts
            checkForAbusePattern(caregiverId, userId)
            
            ContactProtectionResult.Blocked(
                "Caregivers cannot remove contacts. This protects against social isolation abuse. " +
                "Only the user can remove their own contacts."
            )
            
        } catch (e: Exception) {
            logContactProtectionError("Failed to block contact removal", e)
            ContactProtectionResult.Error("Failed to process contact removal attempt")
        }
    }
    
    /**
     * Attempt to block a contact (caregiver-initiated) - BLOCKED
     * This is explicitly prevented to avoid communication control abuse
     */
    suspend fun blockContactBlocking(
        caregiverId: String,
        contactId: String,
        userId: String
    ): ContactProtectionResult {
        return try {
            val contact = contactDao.getContactById(contactId)
            
            // Log the blocked attempt
            logContactModificationAttempt(
                caregiverId = caregiverId,
                action = ContactAction.BLOCK_CONTACT,
                contactInfo = contact?.toContactInfo(),
                result = ContactActionResult.BLOCKED_BY_PROTECTION,
                userId = userId
            )
            
            // Check if this is a pattern of abuse attempts
            checkForAbusePattern(caregiverId, userId)
            
            ContactProtectionResult.Blocked(
                "Caregivers cannot block contacts. This protects against communication control abuse. " +
                "Only the user can block contacts themselves."
            )
            
        } catch (e: Exception) {
            logContactProtectionError("Failed to block contact blocking", e)
            ContactProtectionResult.Error("Failed to process contact blocking attempt")
        }
    }
    
    /**
     * User-initiated contact addition (always allowed)
     */
    suspend fun userAddContact(
        userId: String,
        contactInfo: ContactInfo,
        contactType: String = CONTACT_TYPE_USER_ADDED
    ): ContactProtectionResult {
        return try {
            val protectedContact = ProtectedContact(
                contactId = java.util.UUID.randomUUID().toString(),
                userId = userId,
                contactInfo = contactInfo,
                contactType = contactType,
                protectionLevel = ProtectionLevel.USER_CONTROLLED,
                addedBy = userId,
                addedTimestamp = System.currentTimeMillis(),
                isProtected = true,
                canBeRemovedByCaregiver = false
            )
            
            contactDao.insertProtectedContact(protectedContact)
            
            // Log user action
            logContactModificationAttempt(
                caregiverId = null,
                action = ContactAction.ADD_CONTACT,
                contactInfo = contactInfo,
                result = ContactActionResult.SUCCESS,
                userId = userId,
                initiatedByUser = true
            )
            
            loadProtectedContacts(userId)
            
            ContactProtectionResult.Success("Contact added successfully")
            
        } catch (e: Exception) {
            logContactProtectionError("Failed to add user contact", e)
            ContactProtectionResult.Error("Failed to add contact: ${e.message}")
        }
    }
    
    /**
     * User-initiated contact removal (always allowed)
     */
    suspend fun userRemoveContact(
        userId: String,
        contactId: String
    ): ContactProtectionResult {
        return try {
            val contact = contactDao.getProtectedContact(contactId)
            
            if (contact == null) {
                return ContactProtectionResult.Error("Contact not found")
            }
            
            // Check if this is an elder rights advocate contact
            if (contact.contactType == CONTACT_TYPE_ELDER_RIGHTS) {
                return ContactProtectionResult.Warning(
                    "This is your Elder Rights Advocate contact. " +
                    "Are you sure you want to remove this important safety contact? " +
                    "This contact provides independent support and protection."
                )
            }
            
            // Check if this is an emergency contact
            if (contact.contactType == CONTACT_TYPE_EMERGENCY) {
                val emergencyContacts = emergencyService.getEmergencyContacts(userId)
                if (emergencyContacts.size <= 1) {
                    return ContactProtectionResult.Error(
                        "Cannot remove your only emergency contact. " +
                        "Please add another emergency contact first for your safety."
                    )
                }
            }
            
            // Remove the contact
            contactDao.deleteProtectedContact(contactId)
            
            // Log user action
            logContactModificationAttempt(
                caregiverId = null,
                action = ContactAction.REMOVE_CONTACT,
                contactInfo = contact.contactInfo,
                result = ContactActionResult.SUCCESS,
                userId = userId,
                initiatedByUser = true
            )
            
            loadProtectedContacts(userId)
            
            ContactProtectionResult.Success("Contact removed successfully")
            
        } catch (e: Exception) {
            logContactProtectionError("Failed to remove user contact", e)
            ContactProtectionResult.Error("Failed to remove contact: ${e.message}")
        }
    }
    
    /**
     * User approval/rejection of pending contact requests
     */
    suspend fun respondToPendingRequest(
        userId: String,
        requestId: String,
        approved: Boolean,
        userNote: String? = null
    ): ContactProtectionResult {
        return try {
            val pendingRequest = contactDao.getPendingContactRequest(requestId)
            
            if (pendingRequest == null) {
                return ContactProtectionResult.Error("Pending request not found")
            }
            
            if (pendingRequest.userId != userId) {
                return ContactProtectionResult.Error("Unauthorized access to pending request")
            }
            
            val updatedRequest = pendingRequest.copy(
                status = if (approved) RequestStatus.APPROVED else RequestStatus.REJECTED,
                userResponse = approved,
                userNote = userNote,
                responseTimestamp = System.currentTimeMillis()
            )
            
            contactDao.updatePendingContactRequest(updatedRequest)
            
            if (approved) {
                // Add the contact as protected
                val protectedContact = ProtectedContact(
                    contactId = java.util.UUID.randomUUID().toString(),
                    userId = userId,
                    contactInfo = pendingRequest.contactInfo,
                    contactType = CONTACT_TYPE_USER_ADDED,
                    protectionLevel = ProtectionLevel.USER_CONTROLLED,
                    addedBy = pendingRequest.caregiverId,
                    addedTimestamp = System.currentTimeMillis(),
                    isProtected = true,
                    canBeRemovedByCaregiver = false,
                    userApproved = true,
                    approvalTimestamp = System.currentTimeMillis()
                )
                
                contactDao.insertProtectedContact(protectedContact)
                loadProtectedContacts(userId)
            }
            
            // Log the user response
            logContactModificationAttempt(
                caregiverId = pendingRequest.caregiverId,
                action = ContactAction.RESPOND_TO_REQUEST,
                contactInfo = pendingRequest.contactInfo,
                result = if (approved) ContactActionResult.USER_APPROVED else ContactActionResult.USER_REJECTED,
                userId = userId,
                initiatedByUser = true
            )
            
            ContactProtectionResult.Success(
                if (approved) "Contact request approved and contact added"
                else "Contact request rejected"
            )
            
        } catch (e: Exception) {
            logContactProtectionError("Failed to respond to pending request", e)
            ContactProtectionResult.Error("Failed to process request response: ${e.message}")
        }
    }
    
    /**
     * Get all pending contact requests for user review
     */
    suspend fun getPendingContactRequests(userId: String): List<PendingContactRequest> {
        return try {
            contactDao.getPendingContactRequests(userId)
        } catch (e: Exception) {
            logContactProtectionError("Failed to get pending requests", e)
            emptyList()
        }
    }
    
    /**
     * Check for abuse patterns in contact modification attempts
     */
    private suspend fun checkForAbusePattern(caregiverId: String, userId: String) {
        try {
            val recentAttempts = contactDao.getRecentContactModificationAttempts(
                caregiverId = caregiverId,
                userId = userId,
                timeWindowMs = 3600000L // Last hour
            )
            
            val blockedAttempts = recentAttempts.filter { 
                it.result == ContactActionResult.BLOCKED_BY_PROTECTION 
            }
            
            // Red flag: Multiple blocked attempts in short time
            if (blockedAttempts.size >= 3) {
                // Log potential abuse pattern
                caregiverPermissionManager.flagPotentialAbuse(
                    caregiverId = caregiverId,
                    abuseType = "contact_manipulation_attempts",
                    severity = "medium",
                    details = "Multiple attempts to remove/block contacts in short timeframe",
                    evidenceData = mapOf(
                        "blocked_attempts_count" to blockedAttempts.size,
                        "time_window_hours" to 1,
                        "attempt_types" to blockedAttempts.map { it.action.name }
                    )
                )
                
                // Consider temporarily restricting caregiver permissions
                if (blockedAttempts.size >= 5) {
                    caregiverPermissionManager.temporarilyRestrictPermissions(
                        caregiverId = caregiverId,
                        restrictionReason = "Repeated contact manipulation attempts",
                        restrictionDurationMs = 86400000L // 24 hours
                    )
                }
            }
            
        } catch (e: Exception) {
            logContactProtectionError("Failed to check abuse pattern", e)
        }
    }
    
    /**
     * Ensure elder rights advocate contact exists and is protected
     */
    private suspend fun ensureElderRightsAdvocateContact(userId: String) {
        try {
            val existingAdvocate = contactDao.getContactByType(userId, CONTACT_TYPE_ELDER_RIGHTS)
            
            if (existingAdvocate == null) {
                val advocateContact = ProtectedContact(
                    contactId = java.util.UUID.randomUUID().toString(),
                    userId = userId,
                    contactInfo = ContactInfo(
                        name = ELDER_RIGHTS_NAME,
                        phoneNumber = ELDER_RIGHTS_HOTLINE,
                        email = null,
                        relationship = "Elder Rights Advocate"
                    ),
                    contactType = CONTACT_TYPE_ELDER_RIGHTS,
                    protectionLevel = ProtectionLevel.SYSTEM_PROTECTED,
                    addedBy = "system",
                    addedTimestamp = System.currentTimeMillis(),
                    isProtected = true,
                    canBeRemovedByCaregiver = false,
                    systemContact = true
                )
                
                contactDao.insertProtectedContact(advocateContact)
            }
        } catch (e: Exception) {
            logContactProtectionError("Failed to ensure elder rights advocate contact", e)
        }
    }
    
    /**
     * Load all protected contacts for the user
     */
    private suspend fun loadProtectedContacts(userId: String) {
        try {
            val contacts = contactDao.getProtectedContacts(userId)
            _protectedContacts.value = contacts
        } catch (e: Exception) {
            logContactProtectionError("Failed to load protected contacts", e)
        }
    }
    
    /**
     * Set up monitoring for contact changes
     */
    private suspend fun setupContactChangeMonitoring(userId: String) {
        // This would integrate with Android's ContactsContract.CommonDataKinds.Phone
        // to monitor for external contact changes and protect against them
    }
    
    /**
     * Log contact modification attempts for audit trail
     */
    private suspend fun logContactModificationAttempt(
        caregiverId: String?,
        action: ContactAction,
        contactInfo: ContactInfo?,
        result: ContactActionResult,
        userId: String,
        initiatedByUser: Boolean = false
    ) {
        try {
            val attempt = ContactModificationAttempt(
                attemptId = java.util.UUID.randomUUID().toString(),
                caregiverId = caregiverId,
                userId = userId,
                action = action,
                contactInfo = contactInfo,
                result = result,
                initiatedByUser = initiatedByUser,
                timestamp = System.currentTimeMillis(),
                deviceInfo = getDeviceInfo(),
                appVersion = getAppVersion()
            )
            
            contactDao.insertContactModificationAttempt(attempt)
            
            // Update flow for UI
            val attempts = contactDao.getContactModificationAttempts(userId)
            _contactModificationAttempts.value = attempts
            
        } catch (e: Exception) {
            logContactProtectionError("Failed to log contact modification attempt", e)
        }
    }
    
    private suspend fun logContactProtectionError(message: String, exception: Exception) {
        // Log to system for debugging and monitoring
        android.util.Log.e(TAG, message, exception)
    }
    
    private fun getDeviceInfo(): String {
        return "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}

/**
 * Results of contact protection operations
 */
sealed class ContactProtectionResult {
    data class Success(val message: String) : ContactProtectionResult()
    data class Error(val message: String) : ContactProtectionResult()
    data class Warning(val message: String) : ContactProtectionResult()
    data class Blocked(val message: String) : ContactProtectionResult()
    data class PermissionDenied(val message: String) : ContactProtectionResult()
    data class PendingUserApproval(val requestId: String, val message: String) : ContactProtectionResult()
}

/**
 * Contact actions for audit logging
 */
enum class ContactAction {
    ADD_CONTACT,
    REMOVE_CONTACT,
    BLOCK_CONTACT,
    UNBLOCK_CONTACT,
    MODIFY_CONTACT,
    RESPOND_TO_REQUEST
}

/**
 * Results of contact actions
 */
enum class ContactActionResult {
    SUCCESS,
    BLOCKED_BY_PROTECTION,
    PERMISSION_DENIED,
    PENDING_USER_APPROVAL,
    USER_APPROVED,
    USER_REJECTED,
    ERROR
}

/**
 * Protection levels for contacts
 */
enum class ProtectionLevel {
    USER_CONTROLLED,      // User has full control
    SYSTEM_PROTECTED,     // System-added, high protection
    EMERGENCY_PROTECTED,  // Emergency contacts, special protection
    ADVOCATE_PROTECTED    // Elder rights advocate, maximum protection
}

/**
 * Request types for pending contact operations
 */
enum class ContactRequestType {
    ADD_CONTACT,
    MODIFY_CONTACT,
    SUGGEST_REMOVAL // Caregiver can suggest but not enforce
}

/**
 * Status of pending requests
 */
enum class RequestStatus {
    PENDING_USER_APPROVAL,
    APPROVED,
    REJECTED,
    EXPIRED,
    CANCELLED
}
