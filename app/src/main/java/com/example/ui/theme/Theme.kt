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
    primary = PrimaryGeom,
    onPrimary = OnPrimaryGeom,
    primaryContainer = PrimaryContainerGeom,
    onPrimaryContainer = OnPrimaryContainerGeom,
    secondary = SecondaryGeom,
    onSecondary = OnSecondaryGeom,
    secondaryContainer = SecondaryContainerGeom,
    onSecondaryContainer = OnSecondaryContainerGeom,
    tertiary = TertiaryGeom,
    onTertiary = OnTertiaryGeom,
    tertiaryContainer = TertiaryContainerGeom,
    onTertiaryContainer = OnTertiaryContainerGeom,
    background = BackgroundGeom,
    onBackground = OnBackgroundGeom,
    surface = SurfaceGeom,
    onSurface = OnSurfaceGeom,
    surfaceVariant = SurfaceVariantGeom,
    onSurfaceVariant = OnSurfaceVariantGeom,
    outline = OutlineGeom
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryGeom,
    onPrimary = OnPrimaryGeom,
    primaryContainer = PrimaryContainerGeom,
    onPrimaryContainer = OnPrimaryContainerGeom,
    secondary = SecondaryGeom,
    onSecondary = OnSecondaryGeom,
    secondaryContainer = SecondaryContainerGeom,
    onSecondaryContainer = OnSecondaryContainerGeom,
    tertiary = TertiaryGeom,
    onTertiary = OnTertiaryGeom,
    tertiaryContainer = TertiaryContainerGeom,
    onTertiaryContainer = OnTertiaryContainerGeom,
    background = BackgroundGeom,
    onBackground = OnBackgroundGeom,
    surface = SurfaceGeom,
    onSurface = OnSurfaceGeom,
    surfaceVariant = SurfaceVariantGeom,
    onSurfaceVariant = OnSurfaceVariantGeom,
    outline = OutlineGeom
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to maintain strict Geometric Balance theme branding
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
