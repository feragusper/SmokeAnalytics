package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebScaffold(
    tab: WebTab,
    onTabSelected: (WebTab) -> Unit,
    content: @Composable () -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.shell) }) {
        Div(attrs = { classes(SmokeWebStyles.sidebar) }) {
            Div(attrs = { classes(SmokeWebStyles.sidebarTitle) }) { Text("Smoke Analytics") }

            Div(attrs = { classes(SmokeWebStyles.navList) }) {
                WebTab.entries.forEach { t ->
                    Div(
                        attrs = {
                            classes(SmokeWebStyles.navItem)
                            if (t == tab) classes(SmokeWebStyles.navItemActive)
                            onClick { onTabSelected(t) }
                        }
                    ) { Text(t.label()) }
                }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.main) }) {
            Div(attrs = { classes(SmokeWebStyles.mainInner) }) {
                content()
            }
        }
    }
}

private fun WebTab.label(): String = when (this) {
    WebTab.Home -> "Home"
    WebTab.Stats -> "Stats"
    WebTab.Settings -> "Settings"
}