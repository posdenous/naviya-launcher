# Testing & Quality Rules

1. All data models must have corresponding unit tests - create *Test.kt file with @Test methods
2. Accessibility features must have dedicated test coverage - add tests for TalkBack, high contrast, and large text
3. Crash recovery logic must be thoroughly tested - add tests for 3-crash threshold and safe mode
4. Maintain minimum 80% test coverage across all modules
5. Integration tests required for all user workflows
6. Mock external dependencies in unit tests
7. Use parameterized tests for testing multiple scenarios
8. Implement UI tests for critical user paths
