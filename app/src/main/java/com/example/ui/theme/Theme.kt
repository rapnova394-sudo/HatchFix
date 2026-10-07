package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = NetflixRed,
  onPrimary = PureWhite,
  primaryContainer = NetflixRedContainer,
  onPrimaryContainer = PureWhite,
  secondary = PureWhite,
  onSecondary = AmoledBlack,
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = PureWhite,
  background = AmoledBlack,
  onBackground = PureWhite,
  surface = DarkSurface,
  onSurface = PureWhite,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextMuted,
  outline = DarkSurfaceBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Watch apps strictly require dark theme for OLED power efficiency
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
