package com.naviya.launcher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Elderly-friendly typography following Windsurf accessibility rules:
 * - Minimum 1.6x font scaling for elderly users
 * - High contrast and readable fonts
 * - Clear hierarchy with sufficient size differences
 * - Sans-serif fonts for better readability
 */
val ElderlyTypography = Typography(
    // Display styles - for large headings
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp * 1.6f, // 91.2sp - Extra large for elderly users
        lineHeight = 64.sp * 1.6f,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp * 1.6f, // 72sp
        lineHeight = 52.sp * 1.6f,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp * 1.6f, // 57.6sp
        lineHeight = 44.sp * 1.6f,
        letterSpacing = 0.sp
    ),
    
    // Headline styles - for section headers
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp * 1.6f, // 51.2sp
        lineHeight = 40.sp * 1.6f,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp * 1.6f, // 44.8sp
        lineHeight = 36.sp * 1.6f,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp * 1.6f, // 38.4sp
        lineHeight = 32.sp * 1.6f,
        letterSpacing = 0.sp
    ),
    
    // Title styles - for card titles and important text
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp * 1.6f, // 35.2sp
        lineHeight = 28.sp * 1.6f,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp * 1.6f, // 25.6sp
        lineHeight = 24.sp * 1.6f,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp * 1.6f, // 22.4sp
        lineHeight = 20.sp * 1.6f,
        letterSpacing = 0.1.sp
    ),
    
    // Body styles - for main content text
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp * 1.6f, // 25.6sp
        lineHeight = 24.sp * 1.6f,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp * 1.6f, // 22.4sp
        lineHeight = 20.sp * 1.6f,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp * 1.6f, // 19.2sp
        lineHeight = 16.sp * 1.6f,
        letterSpacing = 0.4.sp
    ),
    
    // Label styles - for buttons and small UI elements
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp * 1.6f, // 22.4sp
        lineHeight = 20.sp * 1.6f,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp * 1.6f, // 19.2sp
        lineHeight = 16.sp * 1.6f,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp * 1.6f, // 17.6sp
        lineHeight = 16.sp * 1.6f,
        letterSpacing = 0.5.sp
    )
)
