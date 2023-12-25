package com.feragusper.smokeanalytics.libraries.design.theme

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
import com.feragusper.smokeanalytics.libraries.design.ColorsPreview
import com.feragusper.smokeanalytics.libraries.design.UiModeThemePreview

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
    val Gray_11 = Color(0xFF1B3534)
    val Gray_20 = Color(0xFF3F4948)
    val Gray_21 = Color(0xFF324B4B)
    val Gray_50 = Color(0xFF4A6363)
    val Gray_60 = Color(0xFF6F7979)
    val Gray_70 = Color(0xFF889392)
    val Gray_80 = Color(0xFFB0CCCB)
    val Gray_81 = Color(0xFFBEC9C8)
    val Gray_82 = Color(0xFFCCE8E7)
    val Gray_85 = Color(0xFFECF2F1)
    val Gray_88 = Color(0xFFF4FBFA)
    val Gray_90 = Color(0xFFDDE4E3)

    val Purple_10 = Color(0xFF041C35)
    val Purple_20 = Color(0xFF1C314B)
    val Purple_40 = Color(0xFF334863)
    val Purple_50 = Color(0xFF4B607C)
    val Purple_70 = Color(0xFFB3C8E8)
    val Purple_90 = Color(0xFFD3E4FF)

    val White = Color(0xFFFFFFFF)

    val Red_10 = Color(0xFF410002)
    val Red_20 = Color(0xFF690005)
    val Red_40 = Color(0xFF93000A)
    val Red_50 = Color(0xFFBA1A1A)
    val Red_80 = Color(0xFFFFB4AB)
    val Red_90 = Color(0xFFFFDAD6)
}

internal val lightColorScheme = lightColorScheme(
    primary = PaletteTokens.Green_40,
    onPrimary = PaletteTokens.White,
    primaryContainer = PaletteTokens.Green_90,
    onPrimaryContainer = PaletteTokens.Green_10,
    inversePrimary = PaletteTokens.Green_80,
    secondary = PaletteTokens.Gray_50,
    onSecondary = PaletteTokens.White,
    secondaryContainer = PaletteTokens.Gray_82,
    onSecondaryContainer = PaletteTokens.Gray_2,
    tertiary = PaletteTokens.Purple_50,
    onTertiary = PaletteTokens.White,
    tertiaryContainer = PaletteTokens.Purple_90,
    onTertiaryContainer = PaletteTokens.Purple_10,
    background = PaletteTokens.White,
    onBackground = PaletteTokens.Black,
    surface = PaletteTokens.Gray_88,
    onSurface = PaletteTokens.Green_05,
    surfaceVariant = PaletteTokens.Gray_88,
    onSurfaceVariant = PaletteTokens.Gray_20,
    inverseSurface = PaletteTokens.Gray_10,
    inverseOnSurface = PaletteTokens.Gray_85,
    error = PaletteTokens.Red_50,
    onError = PaletteTokens.White,
    errorContainer = PaletteTokens.Red_90,
    onErrorContainer = PaletteTokens.Red_10,
    outline = PaletteTokens.Gray_60,
    outlineVariant = PaletteTokens.Gray_81,
)

internal val darkColorScheme = darkColorScheme(
    primary = PaletteTokens.Green_80,
    onPrimary = PaletteTokens.Green_20,
    primaryContainer = PaletteTokens.Green_30,
    onPrimaryContainer = PaletteTokens.Green_90,
    inversePrimary = PaletteTokens.Green_40,
    secondary = PaletteTokens.Gray_80,
    onSecondary = PaletteTokens.Gray_11,
    secondaryContainer = PaletteTokens.Gray_21,
    onSecondaryContainer = PaletteTokens.Gray_82,
    tertiary = PaletteTokens.Purple_70,
    onTertiary = PaletteTokens.Purple_20,
    tertiaryContainer = PaletteTokens.Purple_40,
    onTertiaryContainer = PaletteTokens.Purple_90,
    background = PaletteTokens.Black,
    onBackground = PaletteTokens.White,
    surface = PaletteTokens.Gray_1,
    onSurface = PaletteTokens.Gray_81,
    surfaceVariant = PaletteTokens.Gray_1,
    onSurfaceVariant = PaletteTokens.Gray_81,
    inverseSurface = PaletteTokens.Gray_90,
    inverseOnSurface = PaletteTokens.Gray_10,
    error = PaletteTokens.Red_80,
    onError = PaletteTokens.Red_20,
    errorContainer = PaletteTokens.Red_40,
    onErrorContainer = PaletteTokens.Red_90,
    outline = PaletteTokens.Gray_70,
    outlineVariant = PaletteTokens.Gray_20,
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
            "Tertiary" to MaterialTheme.colorScheme.tertiary,
            "Tertiary Container" to MaterialTheme.colorScheme.tertiaryContainer,
            "Background" to MaterialTheme.colorScheme.background,
            "Surface" to MaterialTheme.colorScheme.surface,
            "Surface Variant" to MaterialTheme.colorScheme.surfaceVariant,
            "Surface Tint" to MaterialTheme.colorScheme.surfaceTint,
            "Inverse Surface" to MaterialTheme.colorScheme.inverseSurface,
            "Error" to MaterialTheme.colorScheme.error,
            "Error Container" to MaterialTheme.colorScheme.errorContainer,
            "Outline" to MaterialTheme.colorScheme.outline,
            "Outline Variant" to MaterialTheme.colorScheme.outlineVariant,
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
