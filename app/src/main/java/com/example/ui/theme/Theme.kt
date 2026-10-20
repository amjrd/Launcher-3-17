package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.ThemePalette

private fun getPaletteColors(palette: ThemePalette, darkTheme: Boolean): ColorScheme {
    return when (palette) {
        ThemePalette.DYNAMIC -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFD0BCFF),
                    onPrimary = Color(0xFF381E72),
                    primaryContainer = Color(0xFF4F378B),
                    onPrimaryContainer = Color(0xFFEADDFF),
                    secondary = Color(0xFFCCC2DC),
                    surface = Color(0xFF141218),
                    surfaceContainer = Color(0xFF211F26),
                    surfaceContainerHigh = Color(0xFF2B2930)
                )
            } else {
                lightColorScheme(
                    primary = ExpressivePrimary,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFEADDFF),
                    onPrimaryContainer = Color(0xFF21005D),
                    secondary = ExpressiveSecondary,
                    surface = Color(0xFFFEF7FF),
                    surfaceContainer = Color(0xFFF3EDF7),
                    surfaceContainerHigh = Color(0xFFECE6F0)
                )
            }
        }
        ThemePalette.OCEAN -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFF90CCFF),
                    onPrimary = Color(0xFF003353),
                    primaryContainer = Color(0xFF004B76),
                    surface = Color(0xFF0F1418),
                    surfaceContainer = Color(0xFF1A2127)
                )
            } else {
                lightColorScheme(
                    primary = OceanPrimary,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFCFE5FF),
                    surface = Color(0xFFF7F9FF),
                    surfaceContainer = Color(0xFFE8EEF5)
                )
            }
        }
        ThemePalette.PEACH -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFFFB4AB),
                    onPrimary = Color(0xFF690005),
                    primaryContainer = Color(0xFF93000A),
                    surface = Color(0xFF191111),
                    surfaceContainer = Color(0xFF261D1D)
                )
            } else {
                lightColorScheme(
                    primary = PeachPrimary,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFFDAD6),
                    surface = Color(0xFFFFF8F7),
                    surfaceContainer = Color(0xFFF9ECEB)
                )
            }
        }
        ThemePalette.EMERALD -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFF8CD7A3),
                    onPrimary = Color(0xFF00391F),
                    primaryContainer = Color(0xFF00522E),
                    surface = Color(0xFF0F1511),
                    surfaceContainer = Color(0xFF19221C)
                )
            } else {
                lightColorScheme(
                    primary = EmeraldPrimary,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFA7F3BE),
                    surface = Color(0xFFF6FBF4),
                    surfaceContainer = Color(0xFFE5EFE7)
                )
            }
        }
        ThemePalette.LAVENDER -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFF0B3CA),
                    onPrimary = Color(0xFF4A2033),
                    primaryContainer = Color(0xFF63384B),
                    surface = Color(0xFF171215),
                    surfaceContainer = Color(0xFF231C20)
                )
            } else {
                lightColorScheme(
                    primary = LavenderPrimary,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFFD8E5),
                    surface = Color(0xFFFFF7F9),
                    surfaceContainer = Color(0xFFF3E7EC)
                )
            }
        }
        ThemePalette.SUNSET -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFFFB870),
                    onPrimary = Color(0xFF4A2800),
                    primaryContainer = Color(0xFF6A3C00),
                    surface = Color(0xFF17130F),
                    surfaceContainer = Color(0xFF241E18)
                )
            } else {
                lightColorScheme(
                    primary = SunsetPrimary,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFFDCBE),
                    surface = Color(0xFFFFF8F5),
                    surfaceContainer = Color(0xFFF5ECE4)
                )
            }
        }
    }
}

@Composable
fun Launcher3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ThemePalette = ThemePalette.DYNAMIC,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = if (palette == ThemePalette.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        getPaletteColors(palette, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
