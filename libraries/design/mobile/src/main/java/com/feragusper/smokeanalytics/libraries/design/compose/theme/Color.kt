package com.feragusper.smokeanalytics.libraries.design.compose.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.libraries.design.compose.ColorsPreview
import com.feragusper.smokeanalytics.libraries.design.compose.UiModeThemePreview

/**
 * Defines the color palette for the application, encapsulating both primary and secondary color variations,
 * as well as specific utility colors like error states and backgrounds.
 */
internal object PaletteTokens {

    val Black = Color(0xFF000000)

    val Green_05 = Color(0xFF161D1D)
    val Green_10 = Color(0xFF002020)
    val Green_20 = Color(0xFF003737)
    val Green_30 = Color(0xFF004F4F)
    val Green_40 = Color(0xFF006A6A)
    val Green_80 = Color(0xFF80D5D4)
    val Green_90 = Color(0xFF9CF1F0)

    val Gray_1 = Color(0xFF0E1514)
    val Gray_2 = Color(0xFF051F1F)
    val Gray_10 = Color(0xFF2B3231)
    val Gray_20 = Color(0xFF3F4948)
    val Gray_50 = Color(0xFF4A6363)
    val Gray_80 = Color(0xFFB0CCCB)
    val Gray_90 = Color(0xFFDDE4E3)

    val White = Color(0xFFFFFFFF)

    val Red_10 = Color(0xFF410002)
    val Red_20 = Color(0xFF690005)
    val Red_40 = Color(0xFF93000A)
    val Red_50 = Color(0xFFBA1A1A)
    val Red_80 = Color(0xFFFFB4AB)
    val Red_90 = Color(0xFFFFDAD6)
}

/**
 * Defines the light color scheme for the application, based on the defined [PaletteTokens].
 */
internal val lightColorScheme = lightColorScheme(
    primary = PaletteTokens.Green_40,
    onPrimary = PaletteTokens.White,
    primaryContainer = PaletteTokens.Green_90,
    onPrimaryContainer = PaletteTokens.Green_10,
    inversePrimary = PaletteTokens.Green_80,
    secondary = PaletteTokens.Gray_50,
    onSecondary = PaletteTokens.White,
    secondaryContainer = PaletteTokens.Gray_80,
    onSecondaryContainer = PaletteTokens.Gray_2,
    background = PaletteTokens.White,
    onBackground = PaletteTokens.Black,
    surface = PaletteTokens.Gray_90,
    onSurface = PaletteTokens.Green_05,
    error = PaletteTokens.Red_50,
    onError = PaletteTokens.White,
    errorContainer = PaletteTokens.Red_90,
    onErrorContainer = PaletteTokens.Red_10,
)

/**
 * Defines the dark color scheme for the application, complementing the light scheme and ensuring
 * visual consistency across different user theme preferences.
 */
internal val darkColorScheme = darkColorScheme(
    primary = PaletteTokens.Green_80,
    onPrimary = PaletteTokens.Green_20,
    primaryContainer = PaletteTokens.Green_30,
    onPrimaryContainer = PaletteTokens.Green_90,
    inversePrimary = PaletteTokens.Green_40,
    secondary = PaletteTokens.Gray_80,
    onSecondary = PaletteTokens.Gray_10,
    secondaryContainer = PaletteTokens.Gray_20,
    onSecondaryContainer = PaletteTokens.Gray_90,
    background = PaletteTokens.Black,
    onBackground = PaletteTokens.White,
    surface = PaletteTokens.Gray_1,
    onSurface = PaletteTokens.Gray_80,
    error = PaletteTokens.Red_80,
    onError = PaletteTokens.Red_20,
    errorContainer = PaletteTokens.Red_40,
    onErrorContainer = PaletteTokens.Red_90,
)

@ColorsPreview
@Composable
private fun ColorsPreviewDynamic() {
    SmokeAnalyticsTheme {
        ColorList()
    }
}

@UiModeThemePreview
@Composable
private fun ColorsPreviewNonDynamic() {
    SmokeAnalyticsTheme(
        dynamicColor = false,
    ) {
        ColorList()
    }
}

/**
 * Previews the application's color scheme by displaying a list of colors defined in the theme.
 * This includes dynamic and non-dynamic color previews, showcasing how the colors adapt to light and dark themes.
 */
@Composable
private fun ColorList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        listOf(
            "Primary" to MaterialTheme.colorScheme.primary,
            "Primary Container" to MaterialTheme.colorScheme.primaryContainer,
            "Inverse Primary" to MaterialTheme.colorScheme.inversePrimary,
            "Secondary" to MaterialTheme.colorScheme.secondary,
            "Secondary Container" to MaterialTheme.colorScheme.secondaryContainer,
            "Background" to MaterialTheme.colorScheme.background,
            "Surface" to MaterialTheme.colorScheme.surface,
            "Error" to MaterialTheme.colorScheme.error,
            "Error Container" to MaterialTheme.colorScheme.errorContainer,
            "Outline" to MaterialTheme.colorScheme.outline,
        ).forEach { (name, color) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(color)
                    .padding(8.dp)
            ) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.contentColorFor(color),
                )
            }
        }
    }
}
