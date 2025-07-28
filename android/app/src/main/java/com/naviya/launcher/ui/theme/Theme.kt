package com.naviya.launcher.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Elderly-friendly color scheme following Windsurf accessibility rules:
 * - High contrast ratios (4.5:1 minimum)
 * - Large text support (1.6x+ font scaling)
 * - Color blindness accessibility
 * - Clear visual hierarchy
 * - Reduced cognitive load through consistent colors
 */

// High contrast dark theme (primary for elderly users)
private val ElderlyDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),        // High contrast green
    onPrimary = Color(0xFF000000),      // Black text on green
    primaryContainer = Color(0xFF2E7D32), // Darker green container
    onPrimaryContainer = Color(0xFFFFFFFF), // White text on dark green
    
    secondary = Color(0xFF2196F3),      // High contrast blue
    onSecondary = Color(0xFF000000),    // Black text on blue
    secondaryContainer = Color(0xFF1565C0), // Darker blue container
    onSecondaryContainer = Color(0xFFFFFFFF), // White text on dark blue
    
    tertiary = Color(0xFFFF9800),       // High contrast orange
    onTertiary = Color(0xFF000000),     // Black text on orange
    tertiaryContainer = Color(0xFFE65100), // Darker orange container
    onTertiaryContainer = Color(0xFFFFFFFF), // White text on dark orange
    
    error = Color(0xFFFF5722),          // High contrast red for errors
    onError = Color(0xFFFFFFFF),        // White text on red
    errorContainer = Color(0xFFD32F2F), // Darker red container
    onErrorContainer = Color(0xFFFFFFFF), // White text on dark red
    
    background = Color(0xFF1E1E1E),     // Dark background for reduced eye strain
    onBackground = Color(0xFFFFFFFF),   // White text on dark background
    surface = Color(0xFF2D2D2D),       // Slightly lighter surface
    onSurface = Color(0xFFFFFFFF),      // White text on surface
    
    surfaceVariant = Color(0xFF3D3D3D), // Card/tile backgrounds
    onSurfaceVariant = Color(0xFFE0E0E0), // Light gray text on surface variant
    
    outline = Color(0xFF757575),        // Medium gray for borders
    outlineVariant = Color(0xFF424242), // Darker gray for subtle borders
    
    scrim = Color(0x80000000),          // Semi-transparent black overlay
    
    inverseSurface = Color(0xFFE0E0E0), // Light surface for contrast
    inverseOnSurface = Color(0xFF1E1E1E), // Dark text on light surface
    inversePrimary = Color(0xFF2E7D32)  // Dark green for inverse
)

// High contrast light theme (alternative for users who prefer light themes)
private val ElderlyLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),        // Dark green
    onPrimary = Color(0xFFFFFFFF),      // White text on green
    primaryContainer = Color(0xFFC8E6C9), // Light green container
    onPrimaryContainer = Color(0xFF1B5E20), // Dark green text on light green
    
    secondary = Color(0xFF1565C0),      // Dark blue
    onSecondary = Color(0xFFFFFFFF),    // White text on blue
    secondaryContainer = Color(0xFFBBDEFB), // Light blue container
    onSecondaryContainer = Color(0xFF0D47A1), // Dark blue text on light blue
    
    tertiary = Color(0xFFE65100),       // Dark orange
    onTertiary = Color(0xFFFFFFFF),     // White text on orange
    tertiaryContainer = Color(0xFFFFE0B2), // Light orange container
    onTertiaryContainer = Color(0xFFBF360C), // Dark orange text on light orange
    
    error = Color(0xFFD32F2F),          // Dark red for errors
    onError = Color(0xFFFFFFFF),        // White text on red
    errorContainer = Color(0xFFFFCDD2), // Light red container
    onErrorContainer = Color(0xFFB71C1C), // Dark red text on light red
    
    background = Color(0xFFFFFFFF),     // White background
    onBackground = Color(0xFF1E1E1E),   // Dark text on white background
    surface = Color(0xFFF5F5F5),       // Light gray surface
    onSurface = Color(0xFF1E1E1E),      // Dark text on surface
    
    surfaceVariant = Color(0xFFE0E0E0), // Card/tile backgrounds
    onSurfaceVariant = Color(0xFF424242), // Dark gray text on surface variant
    
    outline = Color(0xFF757575),        // Medium gray for borders
    outlineVariant = Color(0xFFBDBDBD), // Light gray for subtle borders
    
    scrim = Color(0x80000000),          // Semi-transparent black overlay
    
    inverseSurface = Color(0xFF2D2D2D), // Dark surface for contrast
    inverseOnSurface = Color(0xFFE0E0E0), // Light text on dark surface
    inversePrimary = Color(0xFF4CAF50)  // Light green for inverse
)

@Composable
fun NaviyaLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for elderly users - consistent colors are better
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        
        darkTheme -> ElderlyDarkColorScheme
        else -> ElderlyLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ElderlyTypography, // Custom typography for elderly users
        content = content
    )
}
