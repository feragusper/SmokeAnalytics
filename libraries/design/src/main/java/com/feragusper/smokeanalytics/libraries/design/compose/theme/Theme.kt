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
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as Activity
        SideEffect {
            val window = activity.window
            WindowCompat.setDecorFitsSystemWindows(window, false) // Allows content to extend into system bars
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Set the appearance of system bars based on theme
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme

            // Use transparent system bars
            window.setBackgroundDrawable(null) // Transparent background for the window
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
