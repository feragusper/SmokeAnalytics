package com.feragusper.smokeanalytics.features.authentication.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationWebStore
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponentWeb
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.i18n.LocalStrings
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

    val strings = LocalStrings.current
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = strings.signInToContinue,
            eyebrow = strings.navYou,
            badgeText = if (displayLoading) strings.checkingSession else strings.secureSignIn,
            badgeTone = if (displayLoading) StatusTone.Busy else StatusTone.Default,
        )

        error?.let {
            EmptyStateCard(
                title = strings.authFailed,
                message = strings.authFailedBody,
                actionLabel = strings.retrySessionCheck,
                onAction = { onIntent(AuthenticationIntent.FetchUser) },
            )
        }

        if (displayLoading) {
            LoadingSkeletonCard(heightPx = 140, lineWidths = listOf("44%", "72%", "50%"))
        }

        SurfaceCard {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:18px;") }) {
                Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;max-width:720px;") }) {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(strings.restoreShell) }
                    Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                        Text(strings.restoreShellBody)
                    }
                }

                Div(attrs = {
                    attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
                }) {
                    AuthValueCard(
                        title = strings.navHistory,
                        value = strings.archiveSync,
                        body = strings.keepEditsBody
                    )
                    AuthValueCard(
                        title = strings.routine,
                        value = strings.stableDayModel,
                        body = strings.restorePreferencesBody
                    )
                    AuthValueCard(
                        title = strings.goalsTitle,
                        value = strings.contextAware,
                        body = strings.keepReductionBody
                    )
                }

                Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                    GoogleSignInComponentWeb(
                        onSignInSuccess = { onIntent(AuthenticationIntent.FetchUser) },
                        onSignInError = { onIntent(AuthenticationIntent.FetchUser) }
                    )
                    PrimaryButton(
                        text = strings.refreshSession,
                        onClick = { onIntent(AuthenticationIntent.FetchUser) },
                        enabled = !displayLoading,
                    )
                    GhostButton(
                        text = strings.clearSession,
                        onClick = { onIntent(AuthenticationIntent.SignOut) },
                        enabled = !displayLoading,
                    )
                }
            }
        }

        SurfaceCard {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;") }) {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(strings.beforeYouContinue) }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text(strings.beforeYouContinueBody)
                }
            }
        }
    }
}

@Composable
private fun AuthValueCard(
    title: String,
    value: String,
    body: String,
) {
    Div(
        attrs = {
            attr(
                "style",
                "display:flex;flex-direction:column;gap:8px;min-height:168px;padding:18px;border-radius:22px;background:var(--sa-color-surface-strong);border:1px solid var(--sa-color-outline);"
            )
        }
    ) {
        Div(attrs = { classes(SmokeWebStyles.pageHeroEyebrow) }) { Text(title) }
        Div(attrs = { attr("style", "font-size:24px;font-weight:800;color:var(--sa-color-primary);") }) { Text(value) }
        Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(body) }
    }
}
