package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6EDBA1),
    onPrimary = Color(0xFF003820),
    primaryContainer = Color(0xFF005231),
    onPrimaryContainer = Color(0xFF8BF9BC),
    secondary = HarvestGold,
    onSecondary = Color(0xFF3F2D00),
    secondaryContainer = Color(0xFF5A4300),
    onSecondaryContainer = HarvestGoldContainer,
    background = DarkForestBackground,
    surface = DarkForestSurface,
    onBackground = DarkForestOnSurface,
    onSurface = DarkForestOnSurface,
    surfaceVariant = Color(0xFF1B3B2B),
    onSurfaceVariant = Color(0xFFC1D0C6)
)

private val LightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = HarvestGold,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = HarvestGoldContainer,
    onSecondaryContainer = HarvestOnGoldContainer,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF414943)
)

@Composable
fun FarmVestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand theme identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FarmVestTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

