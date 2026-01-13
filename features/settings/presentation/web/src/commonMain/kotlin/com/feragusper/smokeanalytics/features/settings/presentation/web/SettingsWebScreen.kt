package com.feragusper.smokeanalytics.features.settings.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsWebStore
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponentWeb
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

/**
 * Screen for the Settings screen.
 *
 * @param deps The dependencies for the Settings screen.
 */
@Composable
fun SettingsWebScreen(
    deps: SettingsWebDependencies,
) {
    val store = remember(deps) { SettingsWebStore(processHolder = deps.processHolder) }

    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    state.Render(
        onIntent = { store.send(it) }
    )
}

@Composable
private fun SettingsViewState.Render(
    onIntent: (SettingsIntent) -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.mainInner) }) {

        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Settings") }

        if (displayLoading) {
            SurfaceCard { Text("Loading...") }
            return@Div
        }

        if (currentEmail != null) {
            SurfaceCard {
                P { Text("Signed in as: $currentEmail") }

                Div {
                    PrimaryButton(
                        text = "Sign out",
                        onClick = { onIntent(SettingsIntent.SignOut) },
                        enabled = !displayLoading
                    )
                    org.jetbrains.compose.web.dom.Span { Text(" ") }
                    GhostButton(
                        text = "Refresh session",
                        onClick = { onIntent(SettingsIntent.FetchUser) },
                        enabled = !displayLoading
                    )
                }
            }
        } else {
            SurfaceCard {
                P { Text("You are not signed in.") }

                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Sign in") }

                GoogleSignInComponentWeb(
                    onSignInSuccess = { onIntent(SettingsIntent.FetchUser) },
                    onSignInError = { t ->
                        console.error("Sign-in error", t)
                    }
                )
            }
        }

        errorMessage?.let { msg ->
            SurfaceCard {
                Text(msg)
            }
        }
    }
}