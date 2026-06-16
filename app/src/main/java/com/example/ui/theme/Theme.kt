package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = RiverPrimary,
    secondary = RiverSecondary,
    tertiary = RiverTertiary,
    background = RiverDarkBackground,
    surface = RiverDarkSurface,
    surfaceVariant = RiverDarkSurfaceVariant,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    onSecondary = androidx.compose.ui.graphics.Color.Black
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RiverPrimary,
    secondary = RiverSecondary,
    tertiary = RiverTertiary,
    background = OnBackgroundDark,
    surface = OnBackgroundDark,
    onBackground = RiverDarkBackground,
    onSurface = RiverDarkBackground,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Sophisticated Dark mode by default
  // Dynamic color is disabled to preserve Sophisticated Dark aesthetic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
