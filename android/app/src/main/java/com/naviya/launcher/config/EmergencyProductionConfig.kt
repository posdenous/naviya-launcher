package com.naviya.launcher.config

import android.content.Context
import androidx.annotation.StringRes
import androidx.annotation.IntegerRes
import androidx.annotation.BoolRes
import com.naviya.launcher.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production Configuration Manager for Emergency SOS + Medical Compliance System
 * Centralises all production settings, timeouts, and feature flags
 */
@Singleton
class EmergencyProductionConfig @Inject constructor(
    private val context: Context
) {
    
    // Emergency Response Timeouts
    val emergencyActivationTimeout: Long
        get() = getInteger(R.integer.emergency_activation_timeout).toLong()
    
    val healthcareProfessionalResponseTimeout: Long
        get() = getInteger(R.integer.healthcare_professional_response_timeout).toLong()
    
    val emergencyServiceTimeout: Long
        get() = getInteger(R.integer.emergency_service_timeout).toLong()
    
    val smsNotificationTimeout: Long
        get() = getInteger(R.integer.sms_notification_timeout).toLong()
    
    val emailNotificationTimeout: Long
        get() = getInteger(R.integer.email_notification_timeout).toLong()
    
    val pushNotificationTimeout: Long
        get() = getInteger(R.integer.push_notification_timeout).toLong()
    
    // Emergency Priority Response Times
    val criticalEmergencyResponseTime: Int
        get() = getInteger(R.integer.critical_emergency_response_time)
    
    val highEmergencyResponseTime: Int
        get() = getInteger(R.integer.high_emergency_response_time)
    
    val mediumEmergencyResponseTime: Int
        get() = getInteger(R.integer.medium_emergency_response_time)
    
    val lowEmergencyResponseTime: Int
        get() = getInteger(R.integer.low_emergency_response_time)
    
    // Healthcare Professional Notification Configuration
    val criticalSmsRepeatCount: Int
        get() = getInteger(R.integer.critical_sms_repeat_count)
    
    val highSmsRepeatCount: Int
        get() = getInteger(R.integer.high_sms_repeat_count)
    
    val mediumSmsRepeatCount: Int
        get() = getInteger(R.integer.medium_sms_repeat_count)
    
    val smsRepeatDelayMs: Long
        get() = getInteger(R.integer.sms_repeat_delay_ms).toLong()
    
    // Compliance Monitoring Configuration
    val complianceCheckIntervalHours: Int
        get() = getInteger(R.integer.compliance_check_interval_hours)
    
    val certificationExpiryWarningDays: Int
        get() = getInteger(R.integer.certification_expiry_warning_days)
    
    val professionalReviewOverdueDays: Int
        get() = getInteger(R.integer.professional_review_overdue_days)
    
    val auditLogRetentionDays: Int
        get() = getInteger(R.integer.audit_log_retention_days)
    
    // Emergency System Health Checks
    val systemHealthCheckIntervalMinutes: Int
        get() = getInteger(R.integer.system_health_check_interval_minutes)
    
    val emergencyContactValidationHours: Int
        get() = getInteger(R.integer.emergency_contact_validation_hours)
    
    val professionalAvailabilityCheckMinutes: Int
        get() = getInteger(R.integer.professional_availability_check_minutes)
    
    // Performance Monitoring Thresholds
    val maxEmergencyActivationTimeMs: Long
        get() = getInteger(R.integer.max_emergency_activation_time_ms).toLong()
    
    val maxNotificationDeliveryTimeMs: Long
        get() = getInteger(R.integer.max_notification_delivery_time_ms).toLong()
    
    val minSystemAvailabilityPercentage: Int
        get() = getInteger(R.integer.min_system_availability_percentage)
    
    val maxAcceptableResponseFailures: Int
        get() = getInteger(R.integer.max_acceptable_response_failures)
    
    // Security Configuration
    val emergencySessionTimeoutMinutes: Int
        get() = getInteger(R.integer.emergency_session_timeout_minutes)
    
    val maxFailedEmergencyAttempts: Int
        get() = getInteger(R.integer.max_failed_emergency_attempts)
    
    val securityLockoutDurationMinutes: Int
        get() = getInteger(R.integer.security_lockout_duration_minutes)
    
    // Emergency Contact Validation
    val minEmergencyContactsRequired: Int
        get() = getInteger(R.integer.min_emergency_contacts_required)
    
    val maxEmergencyContactsAllowed: Int
        get() = getInteger(R.integer.max_emergency_contacts_allowed)
    
    val emergencyContactValidationTimeoutMs: Long
        get() = getInteger(R.integer.emergency_contact_validation_timeout_ms).toLong()
    
    // Healthcare Professional Requirements
    val minProfessionalTrainingHours: Int
        get() = getInteger(R.integer.min_professional_training_hours)
    
    val professionalCertificationValidityMonths: Int
        get() = getInteger(R.integer.professional_certification_validity_months)
    
    val backgroundCheckValidityMonths: Int
        get() = getInteger(R.integer.background_check_validity_months)
    
    val ethicsTrainingValidityMonths: Int
        get() = getInteger(R.integer.ethics_training_validity_months)
    
    // Emergency Location Services
    val locationAccuracyThresholdMeters: Int
        get() = getInteger(R.integer.location_accuracy_threshold_meters)
    
    val locationTimeoutSeconds: Int
        get() = getInteger(R.integer.location_timeout_seconds)
    
    val locationRetryAttempts: Int
        get() = getInteger(R.integer.location_retry_attempts)
    
    // Notification Delivery Configuration
    val emergencySmsSenderId: String
        get() = getString(R.string.emergency_sms_sender_id)
    
    val emergencyEmailSender: String
        get() = getString(R.string.emergency_email_sender)
    
    val emergencyNotificationChannelId: String
        get() = getString(R.string.emergency_notification_channel_id)
    
    // Production Environment Flags
    val isEmergencySystemEnabled: Boolean
        get() = getBoolean(R.bool.enable_emergency_system)
    
    val isMedicalComplianceEnabled: Boolean
        get() = getBoolean(R.bool.enable_medical_compliance)
    
    val isHealthcareProfessionalNotificationsEnabled: Boolean
        get() = getBoolean(R.bool.enable_healthcare_professional_notifications)
    
    val isComplianceMonitoringEnabled: Boolean
        get() = getBoolean(R.bool.enable_compliance_monitoring)
    
    val isAuditLoggingEnabled: Boolean
        get() = getBoolean(R.bool.enable_audit_logging)
    
    val isPerformanceMonitoringEnabled: Boolean
        get() = getBoolean(R.bool.enable_performance_monitoring)
    
    val isOfflineEmergencyModeEnabled: Boolean
        get() = getBoolean(R.bool.enable_offline_emergency_mode)
    
    // Debug and Testing Configuration
    val isEmergencyTestingModeEnabled: Boolean
        get() = getBoolean(R.bool.enable_emergency_testing_mode)
    
    val isMockEmergencyServicesEnabled: Boolean
        get() = getBoolean(R.bool.enable_mock_emergency_services)
    
    val isDebugLoggingEnabled: Boolean
        get() = getBoolean(R.bool.enable_debug_logging)
    
    val shouldSkipEmergencyConfirmation: Boolean
        get() = getBoolean(R.bool.skip_emergency_confirmation)
    
    // Regulatory Compliance Settings
    val isHipaaComplianceEnforced: Boolean
        get() = getBoolean(R.bool.enforce_hipaa_compliance)
    
    val isGdprComplianceEnforced: Boolean
        get() = getBoolean(R.bool.enforce_gdpr_compliance)
    
    val isUkClinicalGovernanceEnforced: Boolean
        get() = getBoolean(R.bool.enforce_uk_clinical_governance)
    
    val isElderProtectionStandardsEnforced: Boolean
        get() = getBoolean(R.bool.enforce_elder_protection_standards)
    
    val isProfessionalValidationRequired: Boolean
        get() = getBoolean(R.bool.require_professional_validation)
    
    // Emergency Service Integration
    val primaryEmergencyNumber: String
        get() = getString(R.string.primary_emergency_number)
    
    val secondaryEmergencyNumber: String
        get() = getString(R.string.secondary_emergency_number)
    
    val emergencyServiceIdentifier: String
        get() = getString(R.string.emergency_service_identifier)
    
    // Healthcare Integration URLs
    val healthcareProfessionalApiBaseUrl: String
        get() = getString(R.string.healthcare_professional_api_base_url)
    
    val complianceMonitoringApiUrl: String
        get() = getString(R.string.compliance_monitoring_api_url)
    
    val emergencyNotificationApiUrl: String
        get() = getString(R.string.emergency_notification_api_url)
    
    val auditLoggingApiUrl: String
        get() = getString(R.string.audit_logging_api_url)
    
    // API Configuration
    val apiTimeoutSeconds: Int
        get() = getInteger(R.integer.api_timeout_seconds)
    
    val apiRetryAttempts: Int
        get() = getInteger(R.integer.api_retry_attempts)
    
    val apiRetryDelayMs: Long
        get() = getInteger(R.integer.api_retry_delay_ms).toLong()
    
    // Data Retention and Privacy
    val emergencyDataRetentionDays: Int
        get() = getInteger(R.integer.emergency_data_retention_days)
    
    val personalDataRetentionDays: Int
        get() = getInteger(R.integer.personal_data_retention_days)
    
    val auditLogBackupIntervalHours: Int
        get() = getInteger(R.integer.audit_log_backup_interval_hours)
    
    val isDataEncryptionAtRestEnabled: Boolean
        get() = getBoolean(R.bool.enable_data_encryption_at_rest)
    
    val isDataEncryptionInTransitEnabled: Boolean
        get() = getBoolean(R.bool.enable_data_encryption_in_transit)
    
    // Monitoring and Analytics
    val analyticsEndpoint: String
        get() = getString(R.string.analytics_endpoint)
    
    val analyticsBatchSize: Int
        get() = getInteger(R.integer.analytics_batch_size)
    
    val analyticsUploadIntervalMinutes: Int
        get() = getInteger(R.integer.analytics_upload_interval_minutes)
    
    val isPerformanceAnalyticsEnabled: Boolean
        get() = getBoolean(R.bool.enable_performance_analytics)
    
    val isUsageAnalyticsEnabled: Boolean
        get() = getBoolean(R.bool.enable_usage_analytics)
    
    val isErrorReportingEnabled: Boolean
        get() = getBoolean(R.bool.enable_error_reporting)
    
    // Supported Languages
    val supportedEmergencyLanguages: Array<String>
        get() = context.resources.getStringArray(R.array.supported_emergency_languages)
    
    /**
     * Get SMS repeat count based on emergency urgency level
     */
    fun getSmsRepeatCount(urgencyLevel: String): Int {
        return when (urgencyLevel.uppercase()) {
            "CRITICAL" -> criticalSmsRepeatCount
            "HIGH" -> highSmsRepeatCount
            "MEDIUM", "LOW" -> mediumSmsRepeatCount
            else -> mediumSmsRepeatCount
        }
    }
    
    /**
     * Get response time based on emergency priority
     */
    fun getResponseTimeSeconds(priority: String): Int {
        return when (priority.uppercase()) {
            "CRITICAL" -> criticalEmergencyResponseTime
            "HIGH" -> highEmergencyResponseTime
            "MEDIUM" -> mediumEmergencyResponseTime
            "LOW" -> lowEmergencyResponseTime
            else -> lowEmergencyResponseTime
        }
    }
    
    /**
     * Check if system is in production mode
     */
    val isProductionMode: Boolean
        get() = !isEmergencyTestingModeEnabled && !isMockEmergencyServicesEnabled && !isDebugLoggingEnabled
    
    /**
     * Check if all compliance features are enabled
     */
    val isFullComplianceMode: Boolean
        get() = isHipaaComplianceEnforced && 
                isGdprComplianceEnforced && 
                isUkClinicalGovernanceEnforced && 
                isElderProtectionStandardsEnforced
    
    /**
     * Validate production configuration
     */
    fun validateProductionConfig(): List<String> {
        val issues = mutableListOf<String>()
        
        if (!isEmergencySystemEnabled) {
            issues.add("Emergency system is disabled in production")
        }
        
        if (!isMedicalComplianceEnabled) {
            issues.add("Medical compliance is disabled in production")
        }
        
        if (!isAuditLoggingEnabled) {
            issues.add("Audit logging is disabled in production")
        }
        
        if (isEmergencyTestingModeEnabled) {
            issues.add("Emergency testing mode is enabled in production")
        }
        
        if (isMockEmergencyServicesEnabled) {
            issues.add("Mock emergency services are enabled in production")
        }
        
        if (shouldSkipEmergencyConfirmation) {
            issues.add("Emergency confirmation is disabled in production")
        }
        
        if (!isDataEncryptionAtRestEnabled) {
            issues.add("Data encryption at rest is disabled")
        }
        
        if (!isDataEncryptionInTransitEnabled) {
            issues.add("Data encryption in transit is disabled")
        }
        
        if (minEmergencyContactsRequired < 1) {
            issues.add("Minimum emergency contacts requirement is too low")
        }
        
        if (auditLogRetentionDays < 2555) { // 7 years for HIPAA
            issues.add("Audit log retention period is below HIPAA requirements")
        }
        
        return issues
    }
    
    // Helper methods
    private fun getString(@StringRes resId: Int): String = context.getString(resId)
    private fun getInteger(@IntegerRes resId: Int): Int = context.resources.getInteger(resId)
    private fun getBoolean(@BoolRes resId: Int): Boolean = context.resources.getBoolean(resId)
}
