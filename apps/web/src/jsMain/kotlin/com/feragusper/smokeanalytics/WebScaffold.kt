package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebScaffold(
    route: WebRoute,
    onNavigate: (WebRoute) -> Unit,
    content: @Composable () -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.shell) }) {
        Div(attrs = { classes(SmokeWebStyles.sidebar) }) {
            Div(attrs = { classes(SmokeWebStyles.sidebarTitle) }) { Text("Smoke Analytics") }

            Div(attrs = { classes(SmokeWebStyles.navList) }) {
                val items = listOf(
                    "Home" to WebRoute.Home,
                    "Stats" to WebRoute.Stats,
                    "Settings" to WebRoute.Settings,
                )

                items.forEach { (label, target) ->
                    Div(
                        attrs = {
                            classes(SmokeWebStyles.navItem)
                            if (route == target) classes(SmokeWebStyles.navItemActive)
                            onClick { onNavigate(target) }
                        }
                    ) { Text(label) }
                }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.main) }) {
            Div(attrs = { classes(SmokeWebStyles.mainInner) }) { content() }
        }
    }
}