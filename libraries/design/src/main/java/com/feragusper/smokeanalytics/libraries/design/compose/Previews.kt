package com.feragusper.smokeanalytics.libraries.design.compose

import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

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

@Preview(
    name = "Spanish",
    group = "Locale",
    locale = "es"
)
annotation class LocalePreview

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

@Preview(
    showBackground = true,
    showSystemUi = true,
)
annotation class BackgroundAndSystemUIPreview

/**
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


