package com.atakolstudio.universalremote.ui.theme

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

private val PurplePrimary = Color(0xFF7C4DFF)
private val TealSecondary = Color(0xFF03DAC5)

private val DarkColors = darkColorScheme(
    primary = PurplePrimary,
    secondary = TealSecondary,
    background = Color(0xFF121212),
    surface = Color(0xFF1B1B3A)
)

private val LightColors = lightColorScheme(
    primary = PurplePrimary,
    secondary = TealSecondary,
    background = Color(0xFFF6F6FB),
    surface = Color(0xFFFFFFFF)
)

enum class AppThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun UniversalRemoteTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        useDark -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
