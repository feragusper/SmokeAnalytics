package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.coroutines.launch
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun AboutWebScreen(
    onShare: suspend () -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        AboutWebSections(onShare = onShare)
    }
}

@Composable
fun AboutWebSections(
    onShare: (suspend () -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        SurfaceCard {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
                Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                    Text("About")
                }
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Track less, notice more.") }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text("Smoke Analytics is a personal smoking journal built to make patterns visible without turning the app into noise. It helps you review the day, follow longer trends, understand cost, and notice where or when smoking tends to cluster across mobile and web.")
                }
                onShare?.let { share ->
                    Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                        PrimaryButton(
                            text = "Share app",
                            onClick = { scope.launch { share() } },
                        )
                    }
                }
            }
        }

        SurfaceCard {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
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
        }

        Div(attrs = {
            attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
        }) {
            SurfaceCard {
                Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:8px;min-height:160px;") }) {
                    Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                        Text("Plan")
                    }
                    Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                        Text("Free")
                    }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text("Premium is defined as a future upgrade with richer insights and no ads.")
                    }
                }
            }

            SurfaceCard {
                Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:8px;min-height:160px;") }) {
                    Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                        Text("Copyright")
                    }
                    Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                        Text("Smoke Analytics")
                    }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text("© Fernando Perez. All rights reserved.")
                    }
                }
            }
        }
    }
}
