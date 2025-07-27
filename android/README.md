# Naviya Android Launcher

A comprehensive Android launcher designed specifically for elderly users, featuring accessibility-first design, emergency functionality, and caregiver integration.

## 🎯 Target Users

- **Primary**: Seniors 60+, non-tech-savvy adults
- **Secondary**: Caregivers, family members
- **Languages**: German, English, Turkish, Arabic, Ukrainian

## 🏗️ Architecture Overview

### Core Features
- **2×3 Grid Layout**: Simple, large tiles with 64dp icons
- **SOS Emergency System**: Offline-capable emergency button
- **Crash Recovery**: 3-crash threshold with safe mode
- **PIN Security**: Protected settings with recovery methods
- **App Whitelist**: User/caregiver controlled app management
- **Unread Notifications**: Combined missed calls + SMS tile
- **Caregiver Integration**: Remote pairing and assistance

### Technical Stack
- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **DI**: Hilt/Dagger
- **Database**: Room (local) + Firebase Firestore (sync)
- **UI**: View Binding + Material Design 3
- **Background**: WorkManager
- **Testing**: JUnit + Espresso + Accessibility testing

## 📱 Android Requirements

- **Min SDK**: 26 (Android 8.0) - Better accessibility support
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

### Required Permissions
```xml
<!-- Communication -->
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.CALL_PHONE" />

<!-- Emergency -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- App Management -->
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<!-- Accessibility -->
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
```

## 🏛️ Project Structure

```
android/
├── app/
│   ├── src/main/java/com/naviya/launcher/
│   │   ├── NaviyaLauncherApplication.kt          # Main app class
│   │   ├── data/
│   │   │   ├── models/                           # Data models (maps to MCP schemas)
│   │   │   │   ├── LauncherState.kt             # Main launcher state
│   │   │   │   ├── NotificationState.kt         # Unread notifications
│   │   │   │   ├── CrashRecoveryState.kt        # Crash recovery
│   │   │   │   ├── PinSecurityState.kt          # PIN protection
│   │   │   │   └── AppWhitelistState.kt         # App management
│   │   │   ├── local/                           # Room database
│   │   │   ├── remote/                          # Firebase integration
│   │   │   ├── repositories/                    # Data repositories
│   │   │   └── converters/                      # Type converters
│   │   ├── ui/
│   │   │   ├── launcher/                        # Main launcher UI
│   │   │   │   ├── LauncherActivity.kt          # Main activity
│   │   │   │   ├── LauncherViewModel.kt         # Launcher logic
│   │   │   │   └── LauncherTilesAdapter.kt      # Grid adapter
│   │   │   ├── onboarding/                      # First-time setup
│   │   │   ├── settings/                        # Settings management
│   │   │   ├── security/                        # PIN security
│   │   │   ├── caregiver/                       # Caregiver pairing
│   │   │   ├── emergency/                       # SOS functionality
│   │   │   ├── recovery/                        # Crash recovery
│   │   │   └── apps/                           # App whitelist
│   │   ├── core/
│   │   │   ├── accessibility/                   # Accessibility helpers
│   │   │   ├── crash/                          # Crash handling
│   │   │   ├── logging/                        # Logging system
│   │   │   ├── preferences/                    # User preferences
│   │   │   └── utils/                          # Utility classes
│   │   ├── services/                           # Background services
│   │   ├── receivers/                          # Broadcast receivers
│   │   └── providers/                          # Content providers
│   ├── src/main/res/
│   │   ├── layout/                             # XML layouts
│   │   ├── values/                             # Strings, colors, styles
│   │   ├── values-de/                          # German translations
│   │   ├── values-tr/                          # Turkish translations
│   │   ├── values-ar/                          # Arabic translations
│   │   ├── values-uk/                          # Ukrainian translations
│   │   ├── drawable/                           # Icons and graphics
│   │   └── xml/                               # Configuration files
│   └── src/androidTest/                        # Integration tests
├── build.gradle                               # Project build config
└── gradle.properties                          # Project properties
```

## 🎨 Design Principles

### Accessibility First
- **Font Scale**: 1.6× larger text by default
- **Touch Targets**: 48dp minimum (elderly-friendly)
- **High Contrast**: Maximum visibility
- **TTS Integration**: Screen reader support
- **Haptic Feedback**: Tactile confirmation
- **Slow Animations**: Reduced motion for clarity

### Safety & Security
- **Crash Recovery**: Safe mode after 3 crashes
- **PIN Protection**: Settings locked behind PIN
- **Emergency Access**: SOS always functional
- **App Whitelist**: Controlled app installation
- **Caregiver Oversight**: Remote assistance capability

### Multilingual Support
- **5 Languages**: DE, EN, TR, AR, UA
- **RTL Support**: Arabic text direction
- **Cultural Adaptation**: Region-specific defaults
- **TTS Languages**: Native pronunciation

## 🔧 Development Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or later
- Android SDK 34
- Firebase project setup

### Build Commands
```bash
# Debug build for testing
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Elderly user testing build
./gradlew buildElderlyTestVersion

# Caregiver demo build
./gradlew buildCaregiverDemo

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

### Firebase Setup
1. Create Firebase project: `naviya-launcher`
2. Add Android app with package: `com.naviya.launcher`
3. Download `google-services.json` to `app/`
4. Enable Firestore, Authentication, Analytics, Crashlytics

## 🧪 Testing Strategy

### Accessibility Testing
- **TalkBack**: Screen reader compatibility
- **Switch Access**: External switch support
- **Voice Access**: Voice command testing
- **Large Text**: Font scaling validation
- **High Contrast**: Visual accessibility

### Elderly User Testing
- **Usability Sessions**: Real elderly users
- **Caregiver Feedback**: Family member input
- **NGO Partnerships**: Senior center testing
- **Multilingual Testing**: Native speakers
- **Device Compatibility**: Older Android devices

### Automated Testing
- **Unit Tests**: Business logic validation
- **Integration Tests**: Component interaction
- **UI Tests**: User flow automation
- **Accessibility Tests**: A11y compliance
- **Performance Tests**: Memory and battery

## 📊 Data Models & Schemas

The Android data models directly map to the MCP schemas:

- **LauncherState** ↔ `launcher_state.mcp.yaml`
- **NotificationState** ↔ `notification_state.mcp.yaml`
- **CrashRecoveryState** ↔ `crash_recovery_state.mcp.yaml`
- **PinSecurityState** ↔ `pin_security_state.mcp.yaml`
- **AppWhitelistState** ↔ `app_whitelist_state.mcp.yaml`

This ensures consistency between the AI assistant rules and the Android implementation.

## 🚀 Deployment

### Release Process
1. **Code Review**: Accessibility and safety focus
2. **Testing**: Elderly user validation
3. **Staging**: Caregiver preview
4. **Production**: Gradual rollout
5. **Monitoring**: Crash and usage analytics

### Distribution
- **Google Play**: Primary distribution
- **APK Direct**: For testing and NGOs
- **Enterprise**: Caregiver organizations
- **Localized**: Region-specific releases

## 📈 Analytics & Monitoring

### Key Metrics
- **Crash Rate**: < 0.1% for elderly safety
- **Accessibility Usage**: TTS, large text adoption
- **Emergency Usage**: SOS button effectiveness
- **Caregiver Engagement**: Remote assistance usage
- **App Whitelist**: Installation blocking success

### Privacy Compliance
- **GDPR**: European elderly users
- **Data Minimization**: Essential data only
- **User Consent**: Clear permission requests
- **Caregiver Transparency**: Visible data sharing

## 🤝 Contributing

### Code Standards
- **Kotlin Style**: Official Kotlin coding conventions
- **Accessibility**: WCAG 2.1 AA compliance
- **Documentation**: Elderly user context
- **Testing**: Accessibility test coverage
- **Localization**: Translation-ready strings

### Pull Request Process
1. **Accessibility Review**: Screen reader testing
2. **Elderly User Impact**: Safety assessment
3. **Caregiver Features**: Remote assistance validation
4. **Multilingual**: Translation verification
5. **Performance**: Older device compatibility

## 📞 Support

- **Technical Issues**: GitHub Issues
- **Accessibility**: accessibility@naviya.com
- **Caregiver Support**: caregivers@naviya.com
- **Translations**: i18n@naviya.com
- **Emergency**: emergency@naviya.com

---

**Built with ❤️ for elderly users and their caregivers**
