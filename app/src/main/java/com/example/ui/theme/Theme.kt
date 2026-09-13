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
    primary = IslamicEmeraldDarkPrimary,
    onPrimary = IslamicEmeraldDark,
    primaryContainer = IslamicEmeraldDarkContainer,
    onPrimaryContainer = IslamicEmeraldContainer,
    secondary = IslamicGoldDarkSecondary,
    onSecondary = IslamicGoldOnContainer,
    secondaryContainer = IslamicEmeraldDarkContainer,
    onSecondaryContainer = IslamicGoldContainer,
    background = IslamicDarkBackground,
    surface = IslamicDarkSurface,
    surfaceVariant = IslamicDarkSurfaceVariant,
    onBackground = IslamicDarkOnSurface,
    onSurface = IslamicDarkOnSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = IslamicEmeraldPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = IslamicEmeraldContainer,
    onPrimaryContainer = IslamicEmeraldOnContainer,
    secondary = IslamicGoldSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = IslamicGoldContainer,
    onSecondaryContainer = IslamicGoldOnContainer,
    background = IslamicBackgroundLight,
    surface = IslamicSurfaceLight,
    surfaceVariant = IslamicSurfaceVariantLight,
    onBackground = IslamicOnSurfaceLight,
    onSurface = IslamicOnSurfaceLight,
    outline = IslamicOutlineLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our handcrafted Islamic theme by default
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
