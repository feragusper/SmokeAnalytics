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

        AboutWebSections()
    }
}

@Composable
fun AboutWebSections() {
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Track less, notice more.") }
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text("Smoke Analytics is a personal smoking journal built to make patterns visible without turning the app into noise. It helps you review the day, follow longer trends, understand cost, and notice where or when smoking tends to cluster across mobile and web.")
            }
        }

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Contact") }
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text("Built by Fernando Perez. If something feels off, the best path today is GitHub: check the repo, open a bug report, or start a discussion.")
            }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                A("https://github.com/feragusper/SmokeAnalytics", attrs = { attr("target", "_blank") }) {
                    Text("GitHub")
                }
                A("https://github.com/feragusper/SmokeAnalytics/issues/new/choose", attrs = { attr("target", "_blank") }) {
                    Text("Report bug")
                }
                A("mailto:feragusper@gmail.com", attrs = { attr("target", "_blank") }) {
                    Text("Contact us")
                }
            }
        }

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Plan") }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("Free is the current live tier. Premium is defined as a future upgrade with richer insights and no ads.")
            }
        }

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Copyright") }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("Smoke Analytics © Fernando Perez. All rights reserved.")
            }
        }
    }
}
