package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.apps.web.BuildKonfig
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebScaffold(
    route: WebRoute,
    onNavigate: (WebRoute) -> Unit,
    content: @Composable () -> Unit,
) {
    var isSidebarCollapsed by remember { mutableStateOf(false) }
    val items = listOf(
        NavItem("/icons/nav-home.svg", "Home", WebRoute.Home),
        NavItem("/icons/nav-analytics.svg", "Analytics & Map", WebRoute.Analytics),
        NavItem("/icons/nav-history.svg", "History", WebRoute.History),
        NavItem("/icons/nav-guide.svg", "The Guide", WebRoute.Coach),
        NavItem("/icons/nav-you.svg", "You", WebRoute.Settings),
    )

    Div(attrs = { classes(SmokeWebStyles.shell) }) {
        Div(attrs = {
            classes(SmokeWebStyles.sidebar)
            if (isSidebarCollapsed) classes(SmokeWebStyles.sidebarCollapsed)
        }) {
            Div(attrs = {
                classes(SmokeWebStyles.sidebarHeader)
                if (isSidebarCollapsed) classes(SmokeWebStyles.sidebarHeaderCompact)
            }) {
                Div(attrs = { classes(SmokeWebStyles.brandBadge) }) {
                    Img(
                        src = "/favicon.svg",
                        attrs = {
                            attr("alt", "Smoke Analytics")
                            attr("style", "width: 100%; height: 100%; display: block;")
                        }
                    )
                }
                if (!isSidebarCollapsed) {
                    Div(attrs = { classes(SmokeWebStyles.brandText) }) {
                        Div(attrs = { classes(SmokeWebStyles.sidebarTitle) }) { Text("Smoke Analytics") }
                    }
                }
                Div(attrs = { classes(SmokeWebStyles.sidebarToggle) }) {
                    GhostButton(
                        text = if (isSidebarCollapsed) "›" else "‹",
                        onClick = { isSidebarCollapsed = !isSidebarCollapsed },
                    )
                }
            }

            Div(attrs = { classes(SmokeWebStyles.navList) }) {
                items.forEach { item ->
                    val isActive = route == item.target
                    A(
                        href = item.target.toHash(),
                        attrs = {
                            classes(SmokeWebStyles.navItem)
                            if (isActive) classes(SmokeWebStyles.navItemActive)
                            if (isSidebarCollapsed) classes(SmokeWebStyles.navItemCollapsed)
                            onClick { event ->
                                if (event.button == 0.toShort() && !event.ctrlKey && !event.metaKey && !event.shiftKey && !event.altKey) {
                                    onNavigate(item.target)
                                }
                            }
                        }
                    ) {
                        Div(attrs = { classes(SmokeWebStyles.navIcon) }) {
                            Img(
                                src = item.iconSrc,
                                attrs = {
                                    attr("alt", "")
                                    attr(
                                        "style",
                                        buildString {
                                            append("width: 20px; height: 20px; display: block;")
                                            if (isActive) {
                                                append("filter: brightness(0) saturate(100%) invert(100%);")
                                            }
                                        }
                                    )
                                }
                            )
                        }
                        if (!isSidebarCollapsed) {
                            Div(attrs = { classes(SmokeWebStyles.navLabel) }) { Text(item.label) }
                        }
                    }
                }
            }
            Div(attrs = { classes(SmokeWebStyles.navSpacer) })

            Div(attrs = {
                classes(SmokeWebStyles.navFooter)
                if (isSidebarCollapsed) classes(SmokeWebStyles.navFooterCompact)
            }) {
                if (!isSidebarCollapsed) {
                    Div(attrs = { classes(SmokeWebStyles.navFooterTitle) }) { Text("Web") }
                }
                Div(attrs = { classes(SmokeWebStyles.navFooterBody) }) {
                    Text(BuildKonfig.APP_VERSION)
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

private data class NavItem(
    val iconSrc: String,
    val label: String,
    val target: WebRoute,
)
