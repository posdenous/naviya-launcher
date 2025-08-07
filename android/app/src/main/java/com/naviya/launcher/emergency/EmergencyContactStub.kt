package com.naviya.launcher.emergency

/**
 * Stub implementation of an emergency contact for build stability
 */
data class EmergencyContactStub(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val isPrimary: Boolean = false,
    val isMedicalProfessional: Boolean = false
)
