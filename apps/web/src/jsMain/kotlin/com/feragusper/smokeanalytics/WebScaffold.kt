package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebScaffold(
    route: WebRoute,
    onNavigate: (WebRoute) -> Unit,
    content: @Composable () -> Unit,
) {
    val items = listOf(
        Triple("Home", "Today and latest activity", WebRoute.Home),
        Triple("History", "Browse and edit past entries", WebRoute.History),
        Triple("Stats", "Trends and period breakdowns", WebRoute.Stats),
        Triple("Settings", "Session and preferences", WebRoute.Settings),
    )

    Div(attrs = { classes(SmokeWebStyles.shell) }) {
        Div(attrs = { classes(SmokeWebStyles.sidebar) }) {
            Div(attrs = { classes(SmokeWebStyles.sidebarHeader) }) {
                Div(attrs = { classes(SmokeWebStyles.brandBadge) }) { Text("SA") }
                Div(attrs = { classes(SmokeWebStyles.brandText) }) {
                    Div(attrs = { classes(SmokeWebStyles.sidebarTitle) }) { Text("Smoke Analytics") }
                    Div(attrs = { classes(SmokeWebStyles.sidebarSubtitle) }) {
                        Text("Track the habit, make the next hour easier.")
                    }
                }
            }

            Div(attrs = { classes(SmokeWebStyles.navList) }) {
                items.forEach { (label, meta, target) ->
                    Div(
                        attrs = {
                            classes(SmokeWebStyles.navItem)
                            if (route == target) classes(SmokeWebStyles.navItemActive)
                            onClick { onNavigate(target) }
                        }
                    ) {
                        Div { Text(label) }
                        Div(attrs = { classes(SmokeWebStyles.navItemMeta) }) { Text(meta) }
                    }
                }
            }

            Div(attrs = { classes(SmokeWebStyles.navFooter) }) {
                Div(attrs = { classes(SmokeWebStyles.navFooterTitle) }) { Text("Web refresh") }
                Div(attrs = { classes(SmokeWebStyles.navFooterBody) }) {
                    Text("Smoother navigation, better loading states, and clearer feedback on every route.")
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
