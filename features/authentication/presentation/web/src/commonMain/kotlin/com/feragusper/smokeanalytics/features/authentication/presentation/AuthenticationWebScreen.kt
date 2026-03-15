package com.feragusper.smokeanalytics.features.authentication.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationWebStore
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponentWeb
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.InlineErrorCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.StatusTone
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

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

@Composable
fun AuthenticationViewState.Render(
    onLoggedIn: () -> Unit,
    onIntent: (AuthenticationIntent) -> Unit,
) {
    if (isLoggedIn) {
        onLoggedIn()
        return
    }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Sign in to continue",
            eyebrow = "Auth",
            badgeText = if (displayLoading) "Checking session" else "Secure sign-in",
            badgeTone = if (displayLoading) StatusTone.Busy else StatusTone.Default,
        )

        error?.let {
            InlineErrorCard(
                title = "Authentication failed",
                message = "The session could not be restored. Try signing in again.",
                actionLabel = "Retry session check",
                onAction = { onIntent(AuthenticationIntent.FetchUser) },
            )
        }

        if (displayLoading) {
            LoadingSkeletonCard(heightPx = 140, lineWidths = listOf("44%", "72%", "50%"))
        }

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Continue with Google") }

            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                GoogleSignInComponentWeb(
                    onSignInSuccess = { onIntent(AuthenticationIntent.FetchUser) },
                    onSignInError = { onIntent(AuthenticationIntent.FetchUser) }
                )
                PrimaryButton(
                    text = "Refresh session",
                    onClick = { onIntent(AuthenticationIntent.FetchUser) },
                    enabled = !displayLoading,
                )
                GhostButton(
                    text = "Sign out",
                    onClick = { onIntent(AuthenticationIntent.SignOut) },
                    enabled = !displayLoading,
                )
            }
        }
    }
}
