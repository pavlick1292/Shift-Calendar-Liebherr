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

@Composable
fun ShiftCalendarTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShiftTypography,
        shapes = ShiftShapes,
        content = content
    )
}
