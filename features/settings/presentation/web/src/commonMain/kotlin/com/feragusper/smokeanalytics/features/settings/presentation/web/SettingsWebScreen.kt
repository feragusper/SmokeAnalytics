package com.feragusper.smokeanalytics.features.settings.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsWebStore
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponentWeb
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Hr
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
    Div {
        H2 { Text("Settings") }

        if (displayLoading) {
            P { Text("Loading...") }
            return@Div
        }

        if (currentEmail != null) {
            P { Text("Signed in as: $currentEmail") }

            Hr()

            Button(
                attrs = {
                    if (displayLoading) disabled()
                    onClick { onIntent(SettingsIntent.SignOut) }
                }
            ) {
                Text("Sign out")
            }
        } else {
            P { Text("You are not signed in") }

            Hr()

            GoogleSignInComponentWeb(
                onSignInSuccess = { onIntent(SettingsIntent.FetchUser) },
                onSignInError = { t ->
                    // Keep it simple: show a generic message, or you can push it into Store if you want
                    console.error("Sign-in error", t)
                }
            )
        }

        errorMessage?.let {
            Hr()
            P { Text(it) }
        }
    }
}