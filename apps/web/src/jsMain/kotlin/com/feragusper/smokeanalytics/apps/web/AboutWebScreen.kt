package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun AboutWebScreen(
    onShare: suspend () -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "About",
            eyebrow = "Smoke Analytics",
            badgeText = "Free",
            actions = {
                PrimaryButton(
                    text = "Share",
                    onClick = {
                        GlobalScope.promise { onShare() }
                    }
                )
            }
        )

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Track less, notice more.") }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("Smoke Analytics tracks smokes, streaks, costs, and location patterns across mobile and web.")
            }
        }

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Links") }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                GhostButton(text = "Open site", onClick = { window.open(window.location.origin, "_blank") })
                A("https://github.com/feragusper/SmokeAnalytics", attrs = { attr("target", "_blank") }) {
                    Text("GitHub")
                }
            }
        }

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Plan") }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("Free is the current live tier. Premium is defined as a future upgrade with richer insights and no ads.")
            }
        }
    }
}
