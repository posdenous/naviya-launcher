package com.naviya.launcher.database

import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.naviya.launcher.database.SharedTypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import java.util.concurrent.Executors
import com.naviya.launcher.healthcare.data.*
import com.naviya.launcher.healthcare.compliance.*
import com.naviya.launcher.security.*
import com.naviya.launcher.data.dao.SecurityAuditDao

/**
 * Main Room database for Naviya Elder Protection System
 * Includes all healthcare professional, abuse detection, and elder protection data
 */
@Database(
    entities = [
        // Healthcare Professional entities
        HealthcareProfessionalRegistration::class,
        ProfessionalInstallation::class,
        ClinicalOversight::class,
        ClinicalAssessment::class,
        
        // Medical Compliance entities
        HipaaComplianceLog::class,
        PatientConsentRecord::class,
        ClinicalGovernanceAudit::class,
        ElderProtectionAssessment::class,
        RegulatoryComplianceCheck::class,
        
        // Security Audit entities
        ModeSwitchAudit::class,
        SecurityEvent::class,
        AuthenticationAttempt::class,
        ElderlyConsentLog::class,
        SystemLockoutLog::class,
        CaregiverTokenValidation::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(SharedTypeConverters::class)
abstract class NaviyaDatabase : RoomDatabase() {
    
    // Healthcare Professional DAOs
    abstract fun healthcareProfessionalDao(): HealthcareProfessionalDao
    
    // Medical Compliance DAO
    abstract fun medicalComplianceDao(): MedicalComplianceDao
    
    // Security Audit DAO
    abstract fun securityAuditDao(): SecurityAuditDao
    
    companion object {
        @Volatile
        private var INSTANCE: NaviyaDatabase? = null
        
        /**
         * Thread-safe database instance creation
         * Removed fallbackToDestructiveMigration for production safety
         */
        fun getDatabase(context: Context): NaviyaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): NaviyaDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                NaviyaDatabase::class.java,
                "naviya_database"
            )
            .addMigrations(*getAllMigrations())
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Better performance for elderly users
            .setQueryCallback(QueryCallback { sqlQuery, _ ->
                // Log slow queries for performance monitoring
                if (System.getProperty("naviya.debug") == "true") {
                    Log.d("NaviyaDB", "Query: $sqlQuery")
                }
            }, Executors.newSingleThreadExecutor())
            .build()
        }
        
        private fun getAllMigrations(): Array<Migration> {
            return arrayOf(MIGRATION_1_2, MIGRATION_2_3)
        }
        
        /**
         * Migration from version 1 to 2 - adds healthcare professional tables
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create healthcare professional registration table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS healthcare_professional_registration (
                        registrationId TEXT PRIMARY KEY NOT NULL,
                        professionalId TEXT NOT NULL,
                        personalDetails TEXT NOT NULL,
                        credentials TEXT NOT NULL,
                        institutionAffiliation TEXT,
                        registrationTimestamp INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        certificationStatus TEXT NOT NULL,
                        clinicalOversightLevel TEXT NOT NULL,
                        lastReviewDate INTEGER NOT NULL,
                        nextRecertificationDate INTEGER NOT NULL,
                        installationAuthorized INTEGER NOT NULL,
                        trainingCompleted INTEGER NOT NULL,
                        backgroundCheckCompleted INTEGER NOT NULL,
                        ethicsTrainingCompleted INTEGER NOT NULL
                    )
                """)
                
                // Create professional installation table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS professional_installation (
                        installationId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        professionalId TEXT NOT NULL,
                        institutionId TEXT NOT NULL,
                        installationTimestamp INTEGER NOT NULL,
                        installationType TEXT NOT NULL,
                        clinicalContext TEXT NOT NULL,
                        patientConsent TEXT NOT NULL,
                        familyConsent TEXT,
                        installationStatus TEXT NOT NULL,
                        clinicalNotes TEXT NOT NULL,
                        nextReviewDate INTEGER NOT NULL,
                        elderRightsAdvocateInformed INTEGER NOT NULL,
                        clinicalAssessmentCompleted INTEGER NOT NULL,
                        systemConfigurationCompleted INTEGER NOT NULL
                    )
                """)
                
                // Create clinical oversight table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS clinical_oversight (
                        oversightId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        primaryPhysicianId TEXT NOT NULL,
                        specialistIds TEXT NOT NULL,
                        institutionId TEXT NOT NULL,
                        oversightLevel TEXT NOT NULL,
                        establishmentTimestamp INTEGER NOT NULL,
                        clinicalProtocols TEXT NOT NULL,
                        monitoringFrequency TEXT NOT NULL,
                        alertThresholds TEXT NOT NULL,
                        escalationProcedures TEXT NOT NULL,
                        qualityMetrics TEXT NOT NULL,
                        patientSafetyMeasures TEXT NOT NULL,
                        clinicalGovernance TEXT NOT NULL,
                        lastReviewDate INTEGER NOT NULL,
                        nextReviewDate INTEGER NOT NULL
                    )
                """)
                
                // Create clinical assessment table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS clinical_assessment (
                        assessmentId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        assessingPhysicianId TEXT NOT NULL,
                        assessmentTimestamp INTEGER NOT NULL,
                        assessmentType TEXT NOT NULL,
                        cognitiveAssessment TEXT NOT NULL,
                        functionalAssessment TEXT NOT NULL,
                        socialAssessment TEXT NOT NULL,
                        riskFactorAssessment TEXT NOT NULL,
                        caregiverAssessment TEXT NOT NULL,
                        familyDynamicsAssessment TEXT NOT NULL,
                        clinicalNotes TEXT NOT NULL,
                        overallRiskLevel TEXT NOT NULL,
                        abuseRiskLevel TEXT NOT NULL,
                        recommendations TEXT NOT NULL,
                        followUpRequired INTEGER NOT NULL,
                        elderRightsAdvocateRecommended INTEGER NOT NULL,
                        nextAssessmentDate INTEGER
                    )
                """)
                
                // Create indexes for better performance
                db.execSQL("CREATE INDEX IF NOT EXISTS index_healthcare_professional_registration_professionalId ON healthcare_professional_registration(professionalId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_professional_installation_userId ON professional_installation(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_professional_installation_professionalId ON professional_installation(professionalId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_clinical_oversight_userId ON clinical_oversight(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_clinical_assessment_userId ON clinical_assessment(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_clinical_assessment_assessingPhysicianId ON clinical_assessment(assessingPhysicianId)")
            }
        }
        
        /**
         * Migration from version 2 to 3 - adds security audit tables
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create mode switch audit table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS mode_switch_audit (
                        auditId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        fromMode TEXT NOT NULL,
                        toMode TEXT NOT NULL,
                        requestedBy TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        result TEXT NOT NULL,
                        reason TEXT,
                        userAge INTEGER,
                        authenticationToken TEXT,
                        ipAddress TEXT,
                        deviceInfo TEXT
                    )
                """)
                
                // Create security events table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS security_events (
                        eventId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        eventType TEXT NOT NULL,
                        description TEXT NOT NULL,
                        severity TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        requestedBy TEXT NOT NULL,
                        resolved INTEGER NOT NULL,
                        resolvedAt INTEGER,
                        resolvedBy TEXT,
                        metadata TEXT
                    )
                """)
                
                // Create authentication attempts table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS authentication_attempts (
                        attemptId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        authType TEXT NOT NULL,
                        success INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        requestedBy TEXT NOT NULL,
                        failureReason TEXT,
                        ipAddress TEXT,
                        deviceInfo TEXT
                    )
                """)
                
                // Create elderly consent log table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS elderly_consent_log (
                        consentId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        consentType TEXT NOT NULL,
                        consentGiven INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        targetMode TEXT,
                        witnessId TEXT,
                        consentMethod TEXT NOT NULL,
                        expiryTimestamp INTEGER,
                        revokedAt INTEGER,
                        revokedReason TEXT
                    )
                """)
                
                // Create system lockout log table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS system_lockout_log (
                        lockoutId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        lockoutReason TEXT NOT NULL,
                        lockoutStartTime INTEGER NOT NULL,
                        lockoutEndTime INTEGER,
                        unlockMethod TEXT,
                        elderRightsNotified INTEGER NOT NULL,
                        caregiverNotified INTEGER NOT NULL
                    )
                """)
                
                // Create caregiver token validation table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS caregiver_token_validation (
                        validationId TEXT PRIMARY KEY NOT NULL,
                        caregiverId TEXT NOT NULL,
                        tokenHash TEXT NOT NULL,
                        isValid INTEGER NOT NULL,
                        validationTimestamp INTEGER NOT NULL,
                        expiryTimestamp INTEGER NOT NULL,
                        revokedAt INTEGER,
                        revokedReason TEXT
                    )
                """)
                
                // Create indexes for security audit tables
                db.execSQL("CREATE INDEX IF NOT EXISTS index_mode_switch_audit_userId ON mode_switch_audit(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_mode_switch_audit_timestamp ON mode_switch_audit(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_mode_switch_audit_requestedBy ON mode_switch_audit(requestedBy)")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS index_security_events_userId ON security_events(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_security_events_eventType ON security_events(eventType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_security_events_timestamp ON security_events(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_security_events_resolved ON security_events(resolved)")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS index_authentication_attempts_userId ON authentication_attempts(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_authentication_attempts_timestamp ON authentication_attempts(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_authentication_attempts_success ON authentication_attempts(success)")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS index_elderly_consent_log_userId ON elderly_consent_log(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_elderly_consent_log_consentType ON elderly_consent_log(consentType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_elderly_consent_log_timestamp ON elderly_consent_log(timestamp)")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS index_system_lockout_log_userId ON system_lockout_log(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_system_lockout_log_lockoutStartTime ON system_lockout_log(lockoutStartTime)")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS index_caregiver_token_validation_caregiverId ON caregiver_token_validation(caregiverId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_caregiver_token_validation_isValid ON caregiver_token_validation(isValid)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_caregiver_token_validation_expiryTimestamp ON caregiver_token_validation(expiryTimestamp)")
            }
        }
    }
}


