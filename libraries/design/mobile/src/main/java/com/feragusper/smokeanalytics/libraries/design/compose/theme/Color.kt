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

    val Green_05 = Color(0xFF142022)
    val Green_10 = Color(0xFF0F2D31)
    val Green_20 = Color(0xFF19444A)
    val Green_30 = Color(0xFF255F67)
    val Green_40 = Color(0xFF2E7A84)
    val Green_80 = Color(0xFF97D7DE)
    val Green_90 = Color(0xFFD0EFF3)

    val Gray_1 = Color(0xFF0D1416)
    val Gray_2 = Color(0xFF152124)
    val Gray_10 = Color(0xFF2A3538)
    val Gray_20 = Color(0xFF415154)
    val Gray_50 = Color(0xFF5B6F73)
    val Gray_80 = Color(0xFFC1D1D4)
    val Gray_90 = Color(0xFFEBF1F2)

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
    background = Color(0xFFF6F8F8),
    onBackground = PaletteTokens.Green_05,
    surface = PaletteTokens.White,
    onSurface = PaletteTokens.Green_05,
    surfaceVariant = PaletteTokens.Gray_90,
    onSurfaceVariant = PaletteTokens.Gray_50,
    outline = Color(0xFFB4C2C5),
    outlineVariant = Color(0xFFD7E1E3),
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
    background = Color(0xFF071112),
    onBackground = Color(0xFFE6EFEF),
    surface = PaletteTokens.Gray_1,
    onSurface = PaletteTokens.Gray_80,
    surfaceVariant = PaletteTokens.Gray_2,
    onSurfaceVariant = Color(0xFF9EB1B4),
    outline = Color(0xFF3D4D50),
    outlineVariant = Color(0xFF263437),
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
