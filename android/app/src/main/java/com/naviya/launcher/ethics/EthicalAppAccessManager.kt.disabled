package com.naviya.launcher.ethics

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.VisibleForTesting
import com.naviya.launcher.caregiver.CaregiverPermissionManager
import com.naviya.launcher.data.dao.EthicalControlDao
import com.naviya.launcher.layout.TileType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ethical App Access Manager
 * 
 * Ensures app access and tile behaviour aligns with ethical principles:
 * - User autonomy and dignity
 * - Protection from surveillance abuse
 * - Prevention of social isolation
 * - Financial protection
 * - Emergency escape mechanisms
 */
@Singleton
class EthicalAppAccessManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ethicalControlDao: EthicalControlDao,
    private val caregiverPermissionManager: CaregiverPermissionManager
) {
    
    companion object {
        private const val TAG = "EthicalAppAccessManager"
        
        // Apps that should NEVER be restricted or removed
        private val PROTECTED_ESSENTIAL_APPS = setOf(
            "com.android.dialer",           // Phone - emergency access
            "com.android.contacts",         // Contacts - social connection
            "com.android.settings",         // Settings - user control
            "com.naviya.launcher.emergency" // Our emergency system
        )
        
        // Apps that require explicit user consent before installation
        private val SURVEILLANCE_RISK_APPS = setOf(
            "com.google.android.apps.kids.familylink",
            "com.qustodio.qustodioapp",
            "com.screentime",
            "com.kidslox.copilot",
            "com.familytime.android",
            "com.life360.android.safetymapd"
        )
        
        // Financial apps that require enhanced protection
        private val FINANCIAL_APPS = setOf(
            "com.paypal.android.p2pmobile",
            "com.chase.sig.android",
            "com.bankofamerica.mobile",
            "com.amazon.mShop.android.shopping",
            "com.ebay.mobile"
        )
    }
    
    private val _userControlledApps = MutableStateFlow<Set<String>>(emptySet())
    val userControlledApps: StateFlow<Set<String>> = _userControlledApps.asStateFlow()
    
    private val _emergencyEscapeActive = MutableStateFlow(false)
    val emergencyEscapeActive: StateFlow<Boolean> = _emergencyEscapeActive.asStateFlow()
    
    /**
     * Check if an app installation/access is ethically appropriate
     */
    suspend fun validateAppAccess(
        packageName: String,
        requestedBy: String, // "user" or caregiver ID
        accessType: AppAccessType
    ): AppAccessValidation {
        
        // Always allow user-initiated access
        if (requestedBy == "user") {
            return AppAccessValidation.ALLOWED
        }
        
        // Check if app is protected essential
        if (packageName in PROTECTED_ESSENTIAL_APPS) {
            return when (accessType) {
                AppAccessType.INSTALL -> AppAccessValidation.ALLOWED
                AppAccessType.REMOVE -> AppAccessValidation.BLOCKED_ESSENTIAL
                AppAccessType.RESTRICT -> AppAccessValidation.BLOCKED_ESSENTIAL
            }
        }
        
        // Check surveillance risk apps
        if (packageName in SURVEILLANCE_RISK_APPS) {
            return when (accessType) {
                AppAccessType.INSTALL -> {
                    val userConsent = ethicalControlDao.hasExplicitConsent(
                        packageName, 
                        ConsentType.SURVEILLANCE_APP
                    )
                    if (userConsent) AppAccessValidation.ALLOWED 
                    else AppAccessValidation.REQUIRES_USER_CONSENT
                }
                AppAccessType.REMOVE -> AppAccessValidation.ALLOWED // User can always remove
                AppAccessType.RESTRICT -> AppAccessValidation.REQUIRES_USER_CONSENT
            }
        }
        
        // Check financial apps
        if (packageName in FINANCIAL_APPS) {
            return when (accessType) {
                AppAccessType.INSTALL -> AppAccessValidation.BLOCKED_FINANCIAL
                AppAccessType.REMOVE -> AppAccessValidation.ALLOWED
                AppAccessType.RESTRICT -> AppAccessValidation.BLOCKED_FINANCIAL
            }
        }
        
        // Default: require user awareness for caregiver actions
        return AppAccessValidation.REQUIRES_USER_NOTIFICATION
    }
    
    /**
     * Filter tile types to remove ethically problematic ones for elderly users
     */
    fun getEthicallyAppropriateTileTypes(): Set<TileType> {
        return TileType.values().toSet() - setOf(
            TileType.PARENTAL_CONTROL  // Inappropriate for elderly users
        )
    }
    
    /**
     * Emergency escape mechanism - disable all caregiver monitoring
     */
    suspend fun activateEmergencyEscape(
        triggeredBy: EscapeTrigger,
        userConfirmation: Boolean = false
    ): EscapeResult {
        
        if (!userConfirmation && triggeredBy != EscapeTrigger.TRIPLE_TAP) {
            return EscapeResult.REQUIRES_CONFIRMATION
        }
        
        // Disable all caregiver monitoring temporarily
        _emergencyEscapeActive.value = true
        
        // Log the escape activation (immutable audit trail)
        ethicalControlDao.logEscapeActivation(
            trigger = triggeredBy,
            timestamp = System.currentTimeMillis(),
            reason = "User activated emergency privacy mode"
        )
        
        // Notify elder rights advocate if configured
        notifyElderRightsAdvocate("Emergency escape activated")
        
        return EscapeResult.ACTIVATED
    }
    
    /**
     * Ensure user has control over their app environment
     */
    suspend fun enableUserAppControl(
        userSelectedApps: Set<String>,
        userBlockedApps: Set<String>
    ) {
        _userControlledApps.value = userSelectedApps
        
        ethicalControlDao.updateUserAppPreferences(
            allowedApps = userSelectedApps,
            blockedApps = userBlockedApps,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Validate that caregiver actions respect user autonomy
     */
    suspend fun validateCaregiverAction(
        caregiverId: String,
        action: CaregiverAction,
        targetApp: String? = null
    ): CaregiverActionValidation {
        
        // Check if emergency escape is active
        if (_emergencyEscapeActive.value) {
            return CaregiverActionValidation.BLOCKED_EMERGENCY_ESCAPE
        }
        
        // Validate against user's explicit preferences
        if (targetApp != null && targetApp in _userControlledApps.value) {
            return CaregiverActionValidation.BLOCKED_USER_CONTROLLED
        }
        
        // Check caregiver permissions
        val permissions = caregiverPermissionManager.getCaregiverPermissions(caregiverId)
        
        return when (action) {
            CaregiverAction.INSTALL_APP -> {
                if (permissions?.remoteConfiguration == true) {
                    CaregiverActionValidation.ALLOWED_WITH_NOTIFICATION
                } else {
                    CaregiverActionValidation.BLOCKED_INSUFFICIENT_PERMISSION
                }
            }
            CaregiverAction.REMOVE_APP -> {
                if (targetApp in PROTECTED_ESSENTIAL_APPS) {
                    CaregiverActionValidation.BLOCKED_ESSENTIAL_APP
                } else {
                    CaregiverActionValidation.REQUIRES_USER_CONSENT
                }
            }
            CaregiverAction.MONITOR_USAGE -> {
                if (permissions?.appUsageMonitoring == true) {
                    CaregiverActionValidation.ALLOWED_WITH_NOTIFICATION
                } else {
                    CaregiverActionValidation.BLOCKED_INSUFFICIENT_PERMISSION
                }
            }
        }
    }
    
    private suspend fun notifyElderRightsAdvocate(message: String) {
        // Implementation would notify configured elder rights advocate
        // This is a critical safeguard against abuse
    }
}

enum class AppAccessType {
    INSTALL,
    REMOVE,
    RESTRICT
}

enum class AppAccessValidation {
    ALLOWED,
    BLOCKED_ESSENTIAL,
    BLOCKED_FINANCIAL,
    REQUIRES_USER_CONSENT,
    REQUIRES_USER_NOTIFICATION
}

enum class EscapeTrigger {
    TRIPLE_TAP,
    VOICE_COMMAND,
    MANUAL_BUTTON,
    PANIC_SEQUENCE
}

enum class EscapeResult {
    ACTIVATED,
    REQUIRES_CONFIRMATION,
    FAILED
}

enum class CaregiverAction {
    INSTALL_APP,
    REMOVE_APP,
    MONITOR_USAGE
}

enum class CaregiverActionValidation {
    ALLOWED_WITH_NOTIFICATION,
    BLOCKED_INSUFFICIENT_PERMISSION,
    BLOCKED_ESSENTIAL_APP,
    BLOCKED_USER_CONTROLLED,
    BLOCKED_EMERGENCY_ESCAPE,
    REQUIRES_USER_CONSENT
}

enum class ConsentType {
    SURVEILLANCE_APP,
    FINANCIAL_ACCESS,
    LOCATION_TRACKING
}
