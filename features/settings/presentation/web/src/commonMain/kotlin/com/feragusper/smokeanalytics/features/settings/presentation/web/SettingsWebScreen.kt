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
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Session settings",
            eyebrow = "Settings",
            badgeText = when {
                displayLoading -> "Loading"
                currentEmail != null -> "Signed in"
                else -> "Guest"
            },
            badgeTone = if (displayLoading) StatusTone.Busy else StatusTone.Default,
            actions = {
                GhostButton(
                    text = "Refresh session",
                    onClick = { onIntent(SettingsIntent.FetchUser) },
                    enabled = !displayLoading
                )
            }
        )

        errorMessage?.let { msg ->
            InlineErrorCard(
                message = msg,
                actionLabel = "Try again",
                onAction = { onIntent(SettingsIntent.FetchUser) },
            )
        }

        if (displayLoading && currentEmail == null) {
            LoadingSkeletonCard(heightPx = 112, lineWidths = listOf("42%", "70%", "36%"))
            return@Div
        }

        if (currentEmail != null) {
            SurfaceCard {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Signed in") }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(currentEmail) }

                Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                    PrimaryButton(
                        text = if (displayLoading) "Working..." else "Sign out",
                        onClick = { onIntent(SettingsIntent.SignOut) },
                        enabled = !displayLoading
                    )
                    GhostButton(
                        text = "Refresh session",
                        onClick = { onIntent(SettingsIntent.FetchUser) },
                        enabled = !displayLoading
                    )
                }
            }
        } else {
            SurfaceCard {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Sign in") }

                GoogleSignInComponentWeb(
                    onSignInSuccess = { onIntent(SettingsIntent.FetchUser) },
                    onSignInError = { t ->
                        console.error("Sign-in error", t)
                        onIntent(SettingsIntent.FetchUser)
                    }
                )
            }
        }
    }
}
