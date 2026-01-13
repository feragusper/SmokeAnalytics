package com.feragusper.smokeanalytics.libraries.design.compose

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

// Default preview settings
private const val heightInDp = 2000
private const val showBackground = true

/**
 * Font Scale Theme Previews:
 * Shows how the UI looks with different font scaling options.
 */
@Preview(
    name = "Small Font",
    group = "Font Scale",
    fontScale = 0.5f,
    heightDp = heightInDp,
    showBackground = showBackground,
)
@Preview(
    name = "Large Font",
    group = "Font Scale",
    fontScale = 1.5f,
    heightDp = heightInDp,
    showBackground = showBackground,
)
private annotation class FontScaleThemePreview

/**
 * UI Mode Theme Previews:
 * Shows how the UI adapts to light and dark modes.
 */
@Preview(
    name = "Light",
    group = "UI Mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    heightDp = heightInDp,
    showBackground = showBackground,
)
@Preview(
    name = "Dark",
    group = "UI Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = heightInDp,
    showBackground = showBackground,
)
internal annotation class UiModeThemePreview

/**
 * Wallpaper Theme Previews:
 * Shows how the UI appears against different wallpapers.
 */
@Preview(
    name = "Wallpaper Blue",
    group = "Wallpaper",
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE,
    heightDp = heightInDp,
    showBackground = showBackground,
)
@Preview(
    name = "Wallpaper Reed",
    group = "Wallpaper",
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
    heightDp = heightInDp,
    showBackground = showBackground,
)
@Preview(
    name = "Wallpaper Green",
    group = "Wallpaper",
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    heightDp = heightInDp,
    showBackground = showBackground,
)
@Preview(
    name = "Wallpaper Yellow",
    group = "Wallpaper",
    wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE,
    heightDp = heightInDp,
    showBackground = showBackground,
)
@Preview(
    name = "Wallpaper None",
    group = "Wallpaper",
    wallpaper = Wallpapers.NONE,
    heightDp = heightInDp,
    showBackground = showBackground,
)
private annotation class WallpaperThemePreview

/**
 * Combined Color Previews:
 * Combines UI Mode and Wallpaper previews to check the UI's adaptability to colors.
 */
@UiModeThemePreview
@WallpaperThemePreview
internal annotation class ColorsPreview

/**
 * Typography Previews:
 * Checks how text elements scale and adapt to different font settings.
 */
@FontScaleThemePreview
internal annotation class TypographiesPreview
