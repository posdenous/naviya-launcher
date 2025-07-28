package com.naviya.launcher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography theme optimized for elderly users following Windsurf accessibility rules:
 * - Minimum 1.6x font scaling from standard sizes
 * - High contrast and readability
 * - Clear hierarchy with sufficient size differences
 * - Sans-serif fonts for better readability
 * - Appropriate line heights for easy reading
 */

// Base font sizes (already scaled 1.6x from Material Design defaults)
private val DisplayLargeFontSize = 91.sp    // 57sp * 1.6
private val DisplayMediumFontSize = 72.sp   // 45sp * 1.6
private val DisplaySmallFontSize = 58.sp    // 36sp * 1.6

private val HeadlineLargeFontSize = 51.sp   // 32sp * 1.6
private val HeadlineMediumFontSize = 45.sp  // 28sp * 1.6
private val HeadlineSmallFontSize = 38.sp   // 24sp * 1.6

private val TitleLargeFontSize = 35.sp      // 22sp * 1.6
private val TitleMediumFontSize = 26.sp     // 16sp * 1.6
private val TitleSmallFontSize = 22.sp      // 14sp * 1.6

private val BodyLargeFontSize = 26.sp       // 16sp * 1.6
private val BodyMediumFontSize = 22.sp      // 14sp * 1.6
private val BodySmallFontSize = 19.sp       // 12sp * 1.6

private val LabelLargeFontSize = 22.sp      // 14sp * 1.6
private val LabelMediumFontSize = 19.sp     // 12sp * 1.6
private val LabelSmallFontSize = 18.sp      // 11sp * 1.6

val ElderlyTypography = Typography(
    // Display styles - for large headings and hero text
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = DisplayLargeFontSize,
        lineHeight = (DisplayLargeFontSize.value * 1.2).sp, // 20% larger line height
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = DisplayMediumFontSize,
        lineHeight = (DisplayMediumFontSize.value * 1.2).sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = DisplaySmallFontSize,
        lineHeight = (DisplaySmallFontSize.value * 1.2).sp,
        letterSpacing = 0.sp
    ),
    
    // Headline styles - for section headers and important text
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = HeadlineLargeFontSize,
        lineHeight = (HeadlineLargeFontSize.value * 1.25).sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = HeadlineMediumFontSize,
        lineHeight = (HeadlineMediumFontSize.value * 1.25).sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = HeadlineSmallFontSize,
        lineHeight = (HeadlineSmallFontSize.value * 1.25).sp,
        letterSpacing = 0.sp
    ),
    
    // Title styles - for card titles and medium importance text
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = TitleLargeFontSize,
        lineHeight = (TitleLargeFontSize.value * 1.3).sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = TitleMediumFontSize,
        lineHeight = (TitleMediumFontSize.value * 1.3).sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = TitleSmallFontSize,
        lineHeight = (TitleSmallFontSize.value * 1.3).sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body styles - for main content and descriptions
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = BodyLargeFontSize,
        lineHeight = (BodyLargeFontSize.value * 1.4).sp, // Extra line height for readability
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = BodyMediumFontSize,
        lineHeight = (BodyMediumFontSize.value * 1.4).sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = BodySmallFontSize,
        lineHeight = (BodySmallFontSize.value * 1.4).sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label styles - for buttons, tabs, and UI elements
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = LabelLargeFontSize,
        lineHeight = (LabelLargeFontSize.value * 1.3).sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = LabelMediumFontSize,
        lineHeight = (LabelMediumFontSize.value * 1.3).sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = LabelSmallFontSize,
        lineHeight = (LabelSmallFontSize.value * 1.3).sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Custom text styles for specific elderly-friendly use cases
 */
object ElderlyTextStyles {
    
    // Extra large text for emergency situations
    val EmergencyText = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp, // 3x scaling for emergency visibility
        lineHeight = 58.sp,
        letterSpacing = 0.sp
    )
    
    // App tile labels - optimized for launcher grid
    val TileLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp, // 1.25x scaling for tile readability
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )
    
    // Status text - for connectivity and mode indicators
    val StatusText = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    )
    
    // Accessibility description text - for screen readers
    val AccessibilityText = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )
}
