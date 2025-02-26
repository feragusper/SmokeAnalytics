package com.feragusper.smokeanalytics.libraries.design.compose

import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

/**
 * Font Scale Previews:
 * Shows how the UI looks with different font scaling options.
 */
@Preview(
    name = "Small Font",
    group = "Font Scale",
    fontScale = 0.5f
)
@Preview(
    name = "Large Font",
    group = "Font Scale",
    fontScale = 1.5f
)
annotation class FontScalePreview

/**
 * API Level Previews:
 * Displays the UI in different API levels to ensure compatibility.
 */
@Preview(
    name = "33 (Tiramisu)",
    group = "API Level",
    apiLevel = Build.VERSION_CODES.TIRAMISU,
)
@Preview(
    name = "34 (Upside Down Cake)",
    group = "API Level",
    apiLevel = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
)
annotation class ApiLevelPreview

/**
 * UI Mode Previews:
 * Shows how the UI adapts to light and dark modes.
 */
@Preview(
    name = "Light",
    group = "UI Mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    group = "UI Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class UiModePreview

/**
 * Device Previews:
 * Renders the UI on different device configurations to check responsiveness.
 */
@Preview(
    name = "Pixel C",
    group = "Device",
    device = Devices.PIXEL_C,
)
@Preview(
    name = "Nexus 5",
    group = "Device",
    device = Devices.NEXUS_5,
)
@Preview(
    name = "Pixel 4 XL",
    group = "Device",
    device = Devices.PIXEL_4_XL,
)
annotation class DevicePreview

/**
 * Locale Previews:
 * Displays the UI in different locales to ensure proper localization.
 */
@Preview(
    name = "Spanish",
    group = "Locale",
    locale = "es"
)
annotation class LocalePreview

/**
 * Wallpaper Previews:
 * Shows how the UI appears against different wallpapers.
 */
@Preview(
    name = "Wallpaper Blue",
    group = "Wallpaper",
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Preview(
    name = "Wallpaper Reed",
    group = "Wallpaper",
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Preview(
    name = "Wallpaper Green",
    group = "Wallpaper",
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE
)
@Preview(
    name = "Wallpaper Yellow",
    group = "Wallpaper",
    wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE
)
@Preview(
    name = "Wallpaper None",
    group = "Wallpaper",
    wallpaper = Wallpapers.NONE
)
annotation class WallpaperPreview

/**
 * Background and System UI Previews:
 * Displays the UI with system UI (status and navigation bars) and background visibility.
 */
@Preview(
    showBackground = true,
    showSystemUi = true,
)
annotation class BackgroundAndSystemUIPreview

/**
 * Combined Previews:
 * A collection of annotations for previewing Composables across different font scales, UI modes (light/dark),
 * API levels, devices, locales, wallpapers, and with different background and system UI visibility settings.
 * These annotations help in designing and testing UI components across a wide range of conditions.
 */
@FontScalePreview
@ApiLevelPreview
@UiModePreview
@DevicePreview
@LocalePreview
@WallpaperPreview
@BackgroundAndSystemUIPreview
annotation class CombinedPreviews
