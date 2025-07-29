package com.naviya.launcher.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.naviya.launcher.healthcare.data.*
import com.naviya.launcher.abuse.data.*
import com.naviya.launcher.elderrights.data.*
import com.naviya.launcher.caregiver.data.*
import com.naviya.launcher.onboarding.data.*

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
        
        // Abuse Detection entities
        AbuseDetectionEvent::class,
        AbuseDetectionRule::class,
        
        // Elder Rights entities
        ElderRightsAdvocateContact::class,
        ElderRightsNotification::class,
        
        // Caregiver entities
        CaregiverConnection::class,
        CaregiverPermission::class,
        
        // Onboarding entities
        OnboardingProgress::class,
        UserProfile::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(
    HealthcareProfessionalTypeConverters::class,
    AbuseDetectionTypeConverters::class,
    ElderRightsTypeConverters::class,
    CaregiverTypeConverters::class
)
abstract class NaviyaDatabase : RoomDatabase() {
    
    // Healthcare Professional DAOs
    abstract fun healthcareProfessionalDao(): HealthcareProfessionalDao
    
    // Abuse Detection DAOs
    abstract fun abuseDetectionDao(): AbuseDetectionDao
    
    // Elder Rights DAOs
    abstract fun elderRightsDao(): ElderRightsDao
    
    // Caregiver DAOs
    abstract fun caregiverDao(): CaregiverDao
    
    // Onboarding DAOs
    abstract fun onboardingDao(): OnboardingDao
    
    companion object {
        @Volatile
        private var INSTANCE: NaviyaDatabase? = null
        
        fun getDatabase(context: Context): NaviyaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NaviyaDatabase::class.java,
                    "naviya_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // For development only
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Migration from version 1 to 2 - adds healthcare professional tables
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create healthcare professional registration table
                database.execSQL("""
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
                database.execSQL("""
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
                database.execSQL("""
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
                database.execSQL("""
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
                database.execSQL("CREATE INDEX IF NOT EXISTS index_healthcare_professional_registration_professionalId ON healthcare_professional_registration(professionalId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_professional_installation_userId ON professional_installation(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_professional_installation_professionalId ON professional_installation(professionalId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_clinical_oversight_userId ON clinical_oversight(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_clinical_assessment_userId ON clinical_assessment(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_clinical_assessment_assessingPhysicianId ON clinical_assessment(assessingPhysicianId)")
            }
        }
    }
}

// Placeholder DAOs and entities for compilation
// These would be replaced with actual implementations

interface AbuseDetectionDao {
    // Placeholder for abuse detection DAO methods
}

interface ElderRightsDao {
    // Placeholder for elder rights DAO methods
}

interface CaregiverDao {
    // Placeholder for caregiver DAO methods
}

interface OnboardingDao {
    // Placeholder for onboarding DAO methods
}

// Placeholder entities
data class AbuseDetectionEvent(val id: String = "")
data class AbuseDetectionRule(val id: String = "")
data class ElderRightsAdvocateContact(val id: String = "")
data class ElderRightsNotification(val id: String = "")
data class CaregiverConnection(val id: String = "")
data class CaregiverPermission(val id: String = "")
data class OnboardingProgress(val id: String = "")
data class UserProfile(val id: String = "")

// Placeholder type converters
class AbuseDetectionTypeConverters
class ElderRightsTypeConverters  
class CaregiverTypeConverters
