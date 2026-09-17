package com.example.shiftcalendar.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.shiftcalendar.data.settings.ThemeMode

// ===== СТАНДАРТНАЯ СВЕТЛАЯ =====
private val LightColors = lightColorScheme(
    primary = ShiftColors.Aurora,
    secondary = ShiftColors.Sunrise,
    background = ShiftColors.LightBg,
    surface = ShiftColors.LightSurface,
    surfaceVariant = Color(0xFFE8EAF2),
    onPrimary = Color.White,
    onBackground = Color(0xFF1A1F36),
    onSurface = Color(0xFF1A1F36),
    error = ShiftColors.HolidayRed
)

// ===== СТАНДАРТНАЯ ТЁМНАЯ =====
private val DarkColors = darkColorScheme(
    primary = ShiftColors.Aurora,
    secondary = ShiftColors.Sunrise,
    background = ShiftColors.NightBg,
    surface = ShiftColors.NightSurface,
    surfaceVariant = Color(0xFF343B5C),
    onPrimary = Color.White,
    onBackground = Color(0xFFE6E8F0),
    onSurface = Color(0xFFE6E8F0),
    error = ShiftColors.HolidayRed
)

// ===== ВЫСОКИЙ КОНТРАСТ (светлая) =====
private val HighContrastLight = lightColorScheme(
    primary = Color(0xFF000000),
    secondary = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0E0E0),
    onPrimary = Color.White,
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    error = Color(0xFFB00020)
)

// ===== ВЫСОКИЙ КОНТРАСТ (тёмная) =====
private val HighContrastDark = darkColorScheme(
    primary = Color(0xFFFFD700),
    secondary = Color(0xFFFFFFFF),
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF1A1A1A),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFFF6B6B)
)

// ===== СЕВЕРНОЕ СИЯНИЕ (тёмная, фиолетово-оранжевая) =====
private val AuroraColors = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    secondary = Color(0xFFFF9F43),
    background = Color(0xFF0F0B1E),
    surface = Color(0xFF1A1230),
    surfaceVariant = Color(0xFF2A1E4A),
    onPrimary = Color.White,
    onBackground = Color(0xFFEDE9FE),
    onSurface = Color(0xFFEDE9FE),
    error = Color(0xFFFF6B6B)
)

// ===== ИНДУСТРИАЛЬНАЯ (серо-оранжевая) =====
private val IndustrialColors = darkColorScheme(
    primary = Color(0xFFF97316),
    secondary = Color(0xFFFB923C),
    background = Color(0xFF1C1C1E),
    surface = Color(0xFF2C2C2E),
    surfaceVariant = Color(0xFF3A3A3C),
    onPrimary = Color.White,
    onBackground = Color(0xFFF2F2F7),
    onSurface = Color(0xFFF2F2F7),
    error = Color(0xFFFF453A)
)

@Composable
fun ShiftCalendarTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = when (themeMode) {
        ThemeMode.SYSTEM -> {
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val ctx = LocalContext.current
                    if (isSystemDark) dynamicDarkColorScheme(ctx)
                    else dynamicLightColorScheme(ctx)
                }
                isSystemDark -> DarkColors
                else -> LightColors
            }
        }
        ThemeMode.LIGHT -> LightColors
        ThemeMode.DARK -> DarkColors
        ThemeMode.HIGH_CONTRAST -> {
            if (isSystemDark) HighContrastDark else HighContrastLight
        }
        ThemeMode.AURORA -> AuroraColors
        ThemeMode.INDUSTRIAL -> IndustrialColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShiftTypography,
        shapes = ShiftShapes,
        content = content
    )
}
