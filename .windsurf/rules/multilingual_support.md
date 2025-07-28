# Multilingual Support Rules

1. All user-facing strings must be externalized for i18n - move strings to strings.xml with proper keys
2. Layouts must support RTL for Arabic language support - use start/end instead of left/right margins
3. Support all 5 languages: German (DE), English (EN), Turkish (TR), Arabic (AR), and Ukrainian (UA)
4. Ensure 100% translation completeness for all supported languages
5. TTS support required for all languages and accessibility features
6. Use proper locale-specific formatting for dates, numbers, and currencies
7. Test UI layout with longest translated strings to prevent overflow
8. Implement proper font support for Arabic and Ukrainian scripts
