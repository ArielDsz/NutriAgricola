package com.nutriagricola.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppFontFamily = FontFamily.SansSerif

private val DefaultTypography = Typography()

val Typography = DefaultTypography.copy(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 38.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 20.sp
    ),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold)
)
