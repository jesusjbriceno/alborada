package com.jesusjbriceno.alborada.ui.theme

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

private val DarkColorScheme =
    darkColorScheme(
        primary = SunAmber,
        onPrimary = NightDeepBlue,
        secondary = DawnAccent,
        tertiary = DawnSky,
        background = NightDeepBlue,
        onBackground = TextOnNight,
        surface = NightSurface,
        onSurface = TextOnNight,
        surfaceVariant = NightSurface,
        onSurfaceVariant = NightMuted,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = DawnAccent,
        onPrimary = Color.White,
        secondary = SunOrange,
        tertiary = DawnSky,
        background = DawnCream,
        onBackground = NightDeepBlue,
        surface = Color.White,
        onSurface = NightDeepBlue,
        onSurfaceVariant = Color(0xFF5D6573),
    )

/**
 * App-wide theme. Dynamic color is opt-in for now: the sunrise palette is the
 * brand, so it stays stable across devices.
 */
@Composable
fun AlboradaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                DarkColorScheme
            }

            else -> {
                LightColorScheme
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
