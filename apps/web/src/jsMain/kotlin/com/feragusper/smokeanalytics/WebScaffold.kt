package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebScaffold(
    route: WebRoute,
    onNavigate: (WebRoute) -> Unit,
    content: @Composable () -> Unit,
) {
    val items = listOf(
        "Home" to WebRoute.Home,
        "History" to WebRoute.History,
        "Stats" to WebRoute.Stats,
        "Map" to WebRoute.Map,
        "About" to WebRoute.About,
        "Settings" to WebRoute.Settings,
    )

    Div(attrs = { classes(SmokeWebStyles.shell) }) {
        Div(attrs = { classes(SmokeWebStyles.sidebar) }) {
            Div(attrs = { classes(SmokeWebStyles.sidebarHeader) }) {
                Div(attrs = { classes(SmokeWebStyles.brandBadge) }) {
                    Img(
                        src = "/favicon.svg",
                        attrs = {
                            attr("alt", "Smoke Analytics")
                            attr("style", "width: 100%; height: 100%; display: block;")
                        }
                    )
                }
                Div(attrs = { classes(SmokeWebStyles.brandText) }) {
                    Div(attrs = { classes(SmokeWebStyles.sidebarTitle) }) { Text("Smoke Analytics") }
                }
            }

            Div(attrs = { classes(SmokeWebStyles.navList) }) {
                items.forEach { (label, target) ->
                    Div(
                        attrs = {
                            classes(SmokeWebStyles.navItem)
                            if (route == target) classes(SmokeWebStyles.navItemActive)
                            onClick { onNavigate(target) }
                        }
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.main) }) {
            Div(attrs = { classes(SmokeWebStyles.mainInner) }) {
                key(route.toHash()) {
                    Div(attrs = { classes(SmokeWebStyles.pageTransition) }) {
                        content()
                    }
                }
            }
        }
    }
}
