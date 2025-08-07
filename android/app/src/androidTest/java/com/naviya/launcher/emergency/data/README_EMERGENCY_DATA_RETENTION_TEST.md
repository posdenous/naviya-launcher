# Emergency Data Retention Integration Test

## Overview

This manual integration test verifies that the GDPR-compliant emergency data retention functionality works correctly. Specifically, it tests that the `EmergencyDao.deleteOldEvents()` method properly soft-deletes emergency events that are older than the configured retention period (30 days by default).

## Test Description

The test performs the following steps:
1. Creates an in-memory Room database for testing
2. Inserts three test emergency events with different timestamps:
   - Recent event (5 days old)
   - Old event (45 days old)
   - Very old event (90 days old)
3. Executes the `deleteOldEvents()` method with a 30-day retention period
4. Verifies that:
   - Recent events remain active (not soft-deleted)
   - Old events are properly soft-deleted (deletedAt field is set)
   - Only active events are returned by the `getAllActiveEventsBlocking()` method

## How to Run the Test Manually

Since this test requires an Android Context, it must be run from within an Android component (Activity, Service, etc.). The test cannot be run directly as a standalone JUnit test due to the Android Context requirement.

### Option 1: Run from an Activity

Add the following code to an Activity to run the test:

```kotlin
// Import the test class
import com.naviya.launcher.emergency.data.EmergencyDataRetentionIntegrationTest

// In your Activity
private fun runEmergencyDataRetentionTest() {
    val test = EmergencyDataRetentionIntegrationTest()
    test.runTest(this) // Pass the Activity context
}

// Call this method when you want to run the test
// For example, from a debug menu or button click
```

### Option 2: Run from Application Class

```kotlin
// In your Application class
fun runEmergencyDataRetentionTest() {
    val test = EmergencyDataRetentionIntegrationTest()
    test.runTest(applicationContext)
}
```

### Option 3: Run from a Debug Fragment or Activity

Create a dedicated debug fragment or activity that can be accessed only in debug builds:

```kotlin
class DebugTestsFragment : Fragment() {
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_debug_tests, container, false)
        
        view.findViewById<Button>(R.id.btn_run_emergency_retention_test).setOnClickListener {
            runEmergencyDataRetentionTest()
        }
        
        return view
    }
    
    private fun runEmergencyDataRetentionTest() {
        val test = EmergencyDataRetentionIntegrationTest()
        test.runTest(requireContext())
    }
}
```

## Test Output

The test outputs detailed logs to the console with timestamps and status indicators. Look for these key messages:

- ✅ TEST COMPLETED SUCCESSFULLY - All verifications passed
- ❌ TEST FAILED - An error occurred during test execution

Detailed logs will show:
- [SUCCESS] messages for passed verifications
- [ERROR] messages for failed verifications
- [INFO] messages for general information

## Troubleshooting

If the test fails, check the following:

1. Ensure the EmergencyDao interface has the required methods:
   - `insertEvent(event: EmergencyEvent)`
   - `getAllEventsBlocking(): List<EmergencyEvent>` (includes soft-deleted events)
   - `getAllActiveEventsBlocking(): List<EmergencyEvent>` (excludes soft-deleted events)
   - `deleteOldEvents(cutoffTime: Long, currentTime: Long = System.currentTimeMillis())`

2. Verify that the EmergencyEvent class has the required fields:
   - `id: String`
   - `eventType: EmergencyEventType`
   - `timestamp: Long`
   - `deletedAt: Long?` (nullable timestamp for soft deletion)
   - `userLanguage: String`
   - `contactId: String?` (optional)

3. Check that the Room database is properly configured with the EmergencyDao.

## GDPR Compliance Notes

This test validates a critical aspect of GDPR compliance - the proper implementation of data retention policies. By soft-deleting old emergency events rather than permanently deleting them, we maintain a complete audit trail while still complying with GDPR principles by ensuring the data is no longer actively used after the retention period.
