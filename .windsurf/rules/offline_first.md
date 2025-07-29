# Offline-First Development Rules for Naviya Launcher

## Core Offline-First Principles

1. **Local functionality first, online features as enhancement**
   - All core launcher features must work without internet connection
   - Network connectivity is optional, not required for basic operation
   - Emergency SOS must function completely offline via cellular/SMS

2. **Use Room database for all persistent data**
   - Store user preferences, emergency contacts, and app configurations locally
   - Cache frequently accessed data to avoid network dependencies
   - Implement local-first data access patterns with Repository pattern

3. **Emergency features must never depend on network connectivity**
   - SOS button activation works in airplane mode via cellular
   - Emergency contacts callable without internet connection
   - Emergency event logging to local database with offline protocols

4. **Implement graceful degradation when offline**
   - Show cached data instead of loading indicators
   - Display meaningful offline status messages with clear UI indicators
   - Provide offline alternatives for online-dependent features
   - Queue actions for later sync when reconnected

5. **Accessibility settings must work offline**
   - Font scaling, contrast, and touch target adjustments stored locally
   - TTS functionality without internet dependency
   - Language preferences cached on device
   - All elderly-friendly features available offline

6. **Use opportunistic sync, never block core functionality**
   - Background sync when network becomes available
   - Batch multiple updates for efficiency and battery optimization
   - Retry failed sync attempts with exponential backoff
   - WiFi-preferred for large data transfers, cellular for critical emergency data

7. **Caregiver features have offline fallback mechanisms**
   - Queue caregiver notifications locally first
   - Use SMS as fallback for urgent alerts when data unavailable
   - Store emergency events locally with high priority
   - Sync accumulated data on reconnection

8. **Security maintained in offline mode**
   - Local data encryption at rest for sensitive information
   - Secure local authentication methods (PIN, biometric)
   - Audit trail for offline actions stored locally
   - Protect cached sensitive information with proper encryption
