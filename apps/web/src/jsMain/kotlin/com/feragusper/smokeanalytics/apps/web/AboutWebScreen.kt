package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.design.i18n.LocalStrings
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
    val strings = LocalStrings.current
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        SurfaceCard {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
                Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                    Text(strings.about)
                }
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(strings.aboutTagline) }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text(strings.aboutBody)
                }
                onShare?.let { share ->
                    Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                        PrimaryButton(
                            text = strings.shareApp,
                            onClick = { scope.launch { share() } },
                        )
                    }
                }
            }
        }

        SurfaceCard {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(strings.contact) }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text(strings.contactBody)
                }
                Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                    A("https://github.com/feragusper/SmokeAnalytics", attrs = { attr("target", "_blank") }) {
                        Text(strings.github)
                    }
                    A("https://github.com/feragusper/SmokeAnalytics/issues/new/choose", attrs = { attr("target", "_blank") }) {
                        Text(strings.reportBug)
                    }
                    A("mailto:feragusper@gmail.com", attrs = { attr("target", "_blank") }) {
                        Text(strings.contactUs)
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
                        Text(strings.plan)
                    }
                    Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                        Text(strings.free)
                    }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text(strings.planAboutBody)
                    }
                }
            }

            SurfaceCard {
                Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:8px;min-height:160px;") }) {
                    Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                        Text(strings.copyright)
                    }
                    Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                        Text(strings.brandName)
                    }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text(strings.copyrightBody)
                    }
                }
            }
        }
    }
}
