package com.feragusper.smokeanalytics.features.authentication.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationWebStore
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponentWeb
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

/**
 * Composable function that represents the authentication screen for the web platform.
 * It manages the authentication flow, including displaying a sign-in component and handling
 * user interactions like signing in, signing out, and navigating away.
 *
 * @param deps The dependencies required by this screen, such as the process holder for the MVI store.
 * @param onLoggedIn A lambda function to be invoked when the user has successfully logged in.
 */
@Composable
fun AuthenticationWebScreen(
    deps: AuthenticationWebDependencies,
    onLoggedIn: () -> Unit,
) {
    val store = remember(deps) { AuthenticationWebStore(processHolder = deps.processHolder) }

    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    state.Render(
        onLoggedIn = onLoggedIn,
        onIntent = { intent ->
            store.send(intent)
        }
    )
}

/**
 * Composable function that renders the authentication view state.
 *
 * @param onLoggedIn A lambda function to be invoked when the user has successfully logged in.
 * @param onIntent A lambda function to be invoked when an intent is received.
 */
@Composable
fun AuthenticationViewState.Render(
    onLoggedIn: () -> Unit,
    onIntent: (AuthenticationIntent) -> Unit,
) {
    if (isLoggedIn) {
        onLoggedIn()
        return
    }

    Div {
        H2 { Text("Auth") }

        if (displayLoading) {
            P { Text("Loading...") }
        }

        GoogleSignInComponentWeb(
            onSignInSuccess = {
                onIntent(AuthenticationIntent.FetchUser)
            },
            onSignInError = {
                onIntent(AuthenticationIntent.FetchUser)
            }
        )

        Button(attrs = { onClick { onIntent(AuthenticationIntent.SignOut) } }) {
            Text("Sign out")
        }

        Button(attrs = { onClick { onIntent(AuthenticationIntent.NavigateUp) } }) {
            Text("Back")
        }

        if (error != null) {
            P {
                Text(
                    when (error) {
                        AuthenticationViewState.AuthenticationError.Generic -> "Something went wrong"
                    }
                )
            }
        }
    }
}