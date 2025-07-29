package com.naviya.launcher.core

/**
 * Unified result pattern for the Naviya 3-Mode Launcher
 * Provides consistent error handling across all operations
 * Particularly important for elderly users who need clear error messages
 */
sealed class NaviyaResult<out T> {
    data class Success<T>(val data: T) : NaviyaResult<T>()
    data class Error(val exception: NaviyaException) : NaviyaResult<Nothing>()
    object Loading : NaviyaResult<Nothing>()
}

/**
 * Custom exception hierarchy for Naviya launcher
 * Provides elderly-friendly error messages and appropriate handling
 */
sealed class NaviyaException(
    message: String,
    cause: Throwable? = null,
    val userFriendlyMessage: String = message,
    val isRecoverable: Boolean = true
) : Exception(message, cause) {
    
    // Mode-related errors
    class ModeTransitionError(
        mode: String,
        cause: Throwable? = null
    ) : NaviyaException(
        message = "Failed to switch to $mode mode",
        cause = cause,
        userFriendlyMessage = "Unable to change launcher mode. Please try again.",
        isRecoverable = true
    )
    
    // Database errors
    class DatabaseError(
        operation: String,
        cause: Throwable? = null
    ) : NaviyaException(
        message = "Database operation failed: $operation",
        cause = cause,
        userFriendlyMessage = "Unable to save your settings. Please try again.",
        isRecoverable = true
    )
    
    // Security errors
    class SecurityError(
        operation: String,
        cause: Throwable? = null
    ) : NaviyaException(
        message = "Security validation failed: $operation",
        cause = cause,
        userFriendlyMessage = "Security check failed. Please contact your caregiver.",
        isRecoverable = false
    )
    
    // Emergency system errors
    class EmergencySystemError(
        cause: Throwable? = null
    ) : NaviyaException(
        message = "Emergency system malfunction",
        cause = cause,
        userFriendlyMessage = "Emergency system is not working. Please call for help directly.",
        isRecoverable = false
    )
    
    // Caregiver integration errors
    class CaregiverError(
        operation: String,
        cause: Throwable? = null
    ) : NaviyaException(
        message = "Caregiver operation failed: $operation",
        cause = cause,
        userFriendlyMessage = "Unable to contact your caregiver. Please try again later.",
        isRecoverable = true
    )
    
    // Accessibility errors
    class AccessibilityError(
        feature: String,
        cause: Throwable? = null
    ) : NaviyaException(
        message = "Accessibility feature failed: $feature",
        cause = cause,
        userFriendlyMessage = "Accessibility feature is not working properly.",
        isRecoverable = true
    )
    
    // Network errors
    class NetworkError(
        cause: Throwable? = null
    ) : NaviyaException(
        message = "Network connection failed",
        cause = cause,
        userFriendlyMessage = "No internet connection. Some features may not work.",
        isRecoverable = true
    )
    
    // Performance errors
    class PerformanceError(
        operation: String,
        cause: Throwable? = null
    ) : NaviyaException(
        message = "Performance issue detected: $operation",
        cause = cause,
        userFriendlyMessage = "The app is running slowly. Please restart it.",
        isRecoverable = true
    )
}

/**
 * Extension functions for easier result handling
 */
inline fun <T> NaviyaResult<T>.onSuccess(action: (T) -> Unit): NaviyaResult<T> {
    if (this is NaviyaResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> NaviyaResult<T>.onError(action: (NaviyaException) -> Unit): NaviyaResult<T> {
    if (this is NaviyaResult.Error) {
        action(exception)
    }
    return this
}

inline fun <T> NaviyaResult<T>.onLoading(action: () -> Unit): NaviyaResult<T> {
    if (this is NaviyaResult.Loading) {
        action()
    }
    return this
}

/**
 * Safe execution wrapper for operations that might fail
 * Automatically converts exceptions to NaviyaResult.Error
 */
inline fun <T> safeCall(
    operation: String,
    block: () -> T
): NaviyaResult<T> {
    return try {
        NaviyaResult.Success(block())
    } catch (e: NaviyaException) {
        NaviyaResult.Error(e)
    } catch (e: Exception) {
        NaviyaResult.Error(
            NaviyaException.DatabaseError(
                operation = operation,
                cause = e
            )
        )
    }
}

/**
 * Async safe execution wrapper for coroutine operations
 */
suspend inline fun <T> safeAsyncCall(
    operation: String,
    crossinline block: suspend () -> T
): NaviyaResult<T> {
    return try {
        NaviyaResult.Success(block())
    } catch (e: NaviyaException) {
        NaviyaResult.Error(e)
    } catch (e: Exception) {
        NaviyaResult.Error(
            NaviyaException.DatabaseError(
                operation = operation,
                cause = e
            )
        )
    }
}
