package com.example.rentalwheels.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    secondary = Color(0xFFC7D2FE),
    tertiary = Emerald,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo,
    secondary = IndigoDark,
    tertiary = Emerald,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    primaryContainer = IndigoContainer,
)

@Composable
fun RentalWheelsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
