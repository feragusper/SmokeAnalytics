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

    LaunchedEffect(Unit) {
        val mql = window.matchMedia("(prefers-color-scheme: dark)")
        prefersDark.value = mql.matches
        mql.addEventListener("change", { _ -> prefersDark.value = mql.matches })
    }

    val darkTheme = forceDarkTheme ?: prefersDark.value
    val themeAttr = if (darkTheme) "dark" else "light"

    Style(SmokeWebStyles)

    Div(
        attrs = {
            classes(SmokeWebStyles.appRoot)
            attr("data-theme", themeAttr)
            if (darkTheme) classes(SmokeWebStyles.appRootDarkTokens)
        }
    ) {
        content()
    }
}