package com.feragusper.smokeanalytics.libraries.design.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Green = Color(0xFF388E3C)
val GreenLighter = Color(0xFFC8E6C9)
val White = Color(0xFFFFFFFF)
val Orange = Color(0xFFFF5722)
val Gray = Color(0xFF757575)
val Blacky = Color(0xFF212121)
val Black = Color(0xFF000000)
val Red300 = Color(0xFFE57373)

val lightColorScheme = lightColorScheme(
    primary = Green,
    secondary = Orange,
    background = GreenLighter,
    surface = White,
    error = Red300,
    onError = Red300,
    onSurface = Black,
)

val darkColorScheme = darkColorScheme(
    primary = Green,
    background = Blacky,
    surface = Gray,
    error = Red300,
    onError = White,
    onBackground = White,
    onPrimary = White,
    onSurface = White,
)
