package com.feragusper.smokeanalytics.libraries.design.compose.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * A custom Compose theme wrapper that applies the application's design language,
 * including color schemes and typography. Supports dynamic color theming on compatible Android versions.
 *
 * @param darkTheme Indicates if the dark theme should be applied.
 * @param dynamicColor Indicates if dynamic color theming is enabled, available on Android 12+.
 * @param content The composable content to which this theme will be applied.
 */
@Composable
fun SmokeAnalyticsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Determine the color scheme based on the current theme and device capabilities.
    val colorScheme = when {
        // Use dynamic color scheme if supported (Android 12+).
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Use the custom dark color scheme.
        darkTheme -> darkColorScheme
        // Use the custom light color scheme.
        else -> lightColorScheme
    }

    // Obtain the current view and check if it's in edit mode (e.g., in the preview).
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as Activity
        SideEffect {
            val window = activity.window
            // Allow content to extend into the system bars.
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val insetsController = WindowCompat.getInsetsController(window, view)

            // Set the appearance of system bars (status and navigation bars) based on the theme.
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme

            // Use transparent system bars for a more immersive UI experience.
            window.setBackgroundDrawable(null)
        }
    }

    // Apply the color scheme, typography, and other design tokens to the MaterialTheme.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
