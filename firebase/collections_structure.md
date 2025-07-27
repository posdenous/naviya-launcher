# Firestore Collections Structure

This document outlines the structure of Firestore collections used in the Naviya Launcher application.

## Collections Overview

### 1. users
**Purpose**: Store user profile information and preferences
**Document ID**: User's Firebase Auth UID
**Schema**: Follows `user_profile.mcp.yaml`

```
users/{userId}
├── personal_info/
├── preferences/
├── accessibility/
├── emergency_contacts/
├── caregiver_connections/
├── app_preferences/
└── timestamps
```

### 2. app_configs
**Purpose**: Application configuration and feature flags
**Document ID**: Configuration version or environment
**Schema**: Follows `app_config.mcp.yaml`

```
app_configs/{configId}
├── launcher_settings/
├── ui_configuration/
├── feature_flags/
├── security_settings/
├── emergency_configuration/
├── connectivity/
├── supported_languages/
└── logging/
```

### 3. caregiver_links
**Purpose**: Manage connections between users and caregivers
**Document ID**: `{caregiverId}_{userId}` format
**Schema**: Follows `caregiver_link.mcp.yaml`

```
caregiver_links/{linkId}
├── user_id
├── caregiver_id
├── caregiver_info/
├── connection_details/
├── permissions/
├── communication_preferences/
├── activity_log/
└── security/
```

### 4. sos_events
**Purpose**: Log emergency SOS events and responses
**Document ID**: Auto-generated UUID
**Schema**: Follows `sos_event_log.mcp.yaml`

```
sos_events/{eventId}
├── user_id
├── event_details/
├── location_data/
├── device_context/
├── contact_attempts/
├── emergency_response/
├── follow_up/
├── system_metrics/
└── privacy_compliance/
```

### 5. messages
**Purpose**: Store messages between users and caregivers
**Document ID**: Auto-generated

```
messages/{messageId}
├── sender_id
├── recipient_id
├── message_type (text, voice, video_call_request, etc.)
├── content
├── timestamp
├── read_status
├── delivery_status
└── metadata/
```

### 6. activity_logs
**Purpose**: Track user activity for caregivers (with permission)
**Document ID**: Auto-generated

```
activity_logs/{logId}
├── user_id
├── activity_type
├── timestamp
├── details/
├── app_context/
└── privacy_level
```

### 7. emergency_contacts
**Purpose**: Store emergency contact information
**Document ID**: Auto-generated UUID

```
emergency_contacts/{contactId}
├── user_id
├── name
├── relationship
├── phone_number
├── email
├── is_primary
├── contact_preferences/
└── verification_status
```

### 8. pairing_codes
**Purpose**: Temporary storage for caregiver pairing codes
**Document ID**: The pairing code itself
**TTL**: 24 hours (auto-delete)

```
pairing_codes/{code}
├── user_id
├── caregiver_id (when claimed)
├── created_at
├── expires_at
├── status (active, claimed, expired)
└── verification_data/
```

### 9. notifications
**Purpose**: System and caregiver notifications
**Document ID**: Auto-generated

```
notifications/{notificationId}
├── recipient_id
├── sender_id
├── type (emergency, reminder, system, message)
├── title
├── content
├── timestamp
├── read (boolean)
├── read_at
├── priority (low, normal, high, urgent)
└── action_data/
```

### 10. app_usage_stats
**Purpose**: Anonymous usage statistics (if enabled)
**Document ID**: Date-based (YYYY-MM-DD)

```
app_usage_stats/{date}
├── total_users
├── feature_usage/
├── error_counts/
├── performance_metrics/
└── anonymized_data/
```

## Indexes

### Composite Indexes Required

1. **caregiver_links**
   - `user_id` + `connection_details.connection_status`
   - `caregiver_id` + `connection_details.connection_status`

2. **sos_events**
   - `user_id` + `event_details.trigger_timestamp` (descending)
   - `emergency_response.response_received` + `created_at` (descending)

3. **messages**
   - `recipient_id` + `timestamp` (descending)
   - `sender_id` + `timestamp` (descending)

4. **activity_logs**
   - `user_id` + `timestamp` (descending)
   - `activity_type` + `timestamp` (descending)

5. **notifications**
   - `recipient_id` + `read` + `timestamp` (descending)
   - `recipient_id` + `priority` + `timestamp` (descending)

## Security Rules Summary

- **users**: Users can only access their own data; caregivers can read basic status with permission
- **caregiver_links**: Both user and caregiver can access their connection data
- **sos_events**: Users own their events; caregivers can read with emergency permission
- **messages**: Only sender and recipient can access
- **activity_logs**: Users own their logs; caregivers can read with permission
- **pairing_codes**: Readable by all authenticated users; writable by involved parties
- **notifications**: Only recipients can read; system can create

## Data Retention Policies

- **sos_events**: 90 days default (configurable per user)
- **messages**: 1 year (user configurable)
- **activity_logs**: 30 days (caregiver permission dependent)
- **pairing_codes**: 24 hours (auto-delete)
- **notifications**: 30 days for read notifications, 1 year for unread

## Backup and Recovery

- Daily automated backups
- Point-in-time recovery available
- Cross-region replication for disaster recovery
- User data export functionality for GDPR compliance
