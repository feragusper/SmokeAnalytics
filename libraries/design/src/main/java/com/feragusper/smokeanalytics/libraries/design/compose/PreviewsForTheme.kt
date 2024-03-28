package com.feragusper.smokeanalytics.libraries.design.compose

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

private const val heightInDp = 2000
private const val showBackground = true

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

@UiModeThemePreview
@WallpaperThemePreview
internal annotation class ColorsPreview

@FontScaleThemePreview
internal annotation class TypographiesPreview


