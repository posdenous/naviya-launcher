# Naviya Launcher

A comprehensive application launcher designed for accessibility, safety, and ease of use, with special focus on elderly users and caregiver connectivity.

## Project Structure

```
naviya-launcher/
├── prompts/                     # AI prompt configurations
│   ├── base_prompt.yaml        # Core system prompt
│   ├── onboarding.rules.yaml   # User onboarding flow
│   ├── launcher_layout.rules.yaml # UI layout guidelines
│   ├── sos_button.rules.yaml   # Emergency button behavior
│   ├── caregiver_pairing.rules.yaml # Caregiver connection rules
│   └── offline_mode.rules.yaml # Offline functionality rules
├── schemas/                     # MCP data schemas
│   ├── user_profile.mcp.yaml   # User profile structure
│   ├── app_config.mcp.yaml     # Application configuration
│   ├── caregiver_link.mcp.yaml # Caregiver-user connections
│   └── sos_event_log.mcp.yaml  # Emergency event logging
├── assets/
│   └── translations/           # Multi-language support
│       ├── de/                 # German
│       ├── en/                 # English
│       ├── tr/                 # Turkish
│       ├── ar/                 # Arabic
│       └── ua/                 # Ukrainian
└── firebase/                   # Backend configuration
    ├── firestore_rules.txt     # Database security rules
    └── collections_structure.md # Database schema documentation
```

## Key Features

### 🚨 Emergency System
- Prominent SOS button with multiple activation methods
- Automatic emergency contact notification
- Location sharing and event logging
- Offline emergency capabilities

### 👥 Caregiver Integration
- Secure pairing system with 6-digit codes
- Granular permission management
- Real-time communication and monitoring
- Privacy-focused design

### ♿ Accessibility First
- WCAG 2.1 AA compliance
- Large touch targets and high contrast modes
- Voice control and screen reader support
- Customizable font sizes and themes

### 🌐 Multi-language Support
- Support for 5 languages: German, English, Turkish, Arabic, Ukrainian
- RTL language support for Arabic
- Culturally appropriate translations

### 📱 Offline Capability
- Core functionality available offline
- Emergency features work without internet
- Automatic sync when reconnected
- Cached content and settings

## Technical Architecture

### Data Schemas
All data structures are defined using MCP (Model Context Protocol) schemas:
- **User Profile**: Personal info, preferences, emergency contacts
- **App Configuration**: Feature flags, UI settings, security options
- **Caregiver Links**: Connection management and permissions
- **SOS Event Logs**: Emergency event tracking and response

### Security & Privacy
- End-to-end encryption for caregiver communications
- GDPR compliance with data retention policies
- Granular permission system
- Secure Firebase Firestore rules

### Firebase Backend
- Firestore database with comprehensive security rules
- Real-time synchronization
- Automatic backups and disaster recovery
- Cross-region replication

## Getting Started

1. **Setup Firebase Project**
   - Create a new Firebase project
   - Enable Firestore database
   - Apply the security rules from `firebase/firestore_rules.txt`

2. **Configure Application**
   - Update `schemas/app_config.mcp.yaml` with your Firebase credentials
   - Customize feature flags as needed

3. **Deploy Translations**
   - Add translation files to `assets/translations/`
   - Update supported languages in app configuration

4. **Implement UI**
   - Follow guidelines in `prompts/launcher_layout.rules.yaml`
   - Ensure accessibility compliance per `prompts/base_prompt.yaml`

## Development Guidelines

### Prompt Engineering
- All AI behavior is defined in YAML prompt files
- Follow the base prompt principles for consistency
- Update rules files when adding new features

### Schema Management
- Use MCP schemas for all data structures
- Validate data against schemas before storage
- Version schemas when making breaking changes

### Internationalization
- Add new languages by creating translation directories
- Update `app_config.mcp.yaml` to include new languages
- Test RTL languages thoroughly

### Security
- Never bypass Firestore security rules
- Implement proper authentication flows
- Regular security audits of permissions

## License

[Add your license information here]

## Contributing

[Add contribution guidelines here]
