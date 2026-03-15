package com.feragusper.smokeanalytics.libraries.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.browser.window
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Div

@Composable
fun SmokeWebTheme(
    forceDarkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val prefersDark = remember { mutableStateOf(false) }
    val prefersReducedMotion = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val darkModeQuery = window.matchMedia("(prefers-color-scheme: dark)")
        val reducedMotionQuery = window.matchMedia("(prefers-reduced-motion: reduce)")
        prefersDark.value = darkModeQuery.matches
        prefersReducedMotion.value = reducedMotionQuery.matches
        darkModeQuery.addEventListener("change", { _ -> prefersDark.value = darkModeQuery.matches })
        reducedMotionQuery.addEventListener("change", { _ ->
            prefersReducedMotion.value = reducedMotionQuery.matches
        })
    }

    val darkTheme = forceDarkTheme ?: prefersDark.value
    val themeAttr = if (darkTheme) "dark" else "light"

    Style(SmokeWebStyles)

    Div(
        attrs = {
            classes(SmokeWebStyles.appRoot)
            attr("data-theme", themeAttr)
            if (darkTheme) classes(SmokeWebStyles.appRootDarkTokens)
            if (prefersReducedMotion.value) classes(SmokeWebStyles.appRootReducedMotion)
        }
    ) {
        content()
    }
}
