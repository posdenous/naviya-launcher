# Android Development Best Practices

1. Use Kotlin coroutines instead of AsyncTask or raw threads - replace with viewModelScope.launch or lifecycleScope.launch
2. Use Room database instead of raw SQLite for type safety - create Room entities and DAOs
3. Use Hilt dependency injection instead of manual instantiation - add @Inject constructor and @HiltAndroidApp
4. Ensure proper lifecycle management in all Android components
5. Implement memory leak prevention patterns in ViewModels and Activities
6. Use LiveData or StateFlow for reactive UI updates
7. Follow MVVM architecture pattern for separation of concerns
8. Implement proper error handling with sealed classes or Result types
