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
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
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
                GhostButton(text = "About", onClick = { window.location.hash = "#about" }, enabled = !displayLoading)
                GhostButton(text = "Refresh", onClick = { onIntent(SettingsIntent.FetchUser) }, enabled = !displayLoading)
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

        infoMessage?.let { msg ->
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(msg) }
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

            PreferencesCard(
                preferences = preferences,
                displayLoading = displayLoading,
                onSave = { prefs ->
                    onIntent(
                        SettingsIntent.UpdatePreferences(
                            packPrice = prefs.packPrice,
                            cigarettesPerPack = prefs.cigarettesPerPack,
                            dayStartHour = prefs.dayStartHour,
                            locationTrackingEnabled = prefs.locationTrackingEnabled,
                        )
                    )
                }
            )

            SurfaceCard {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Account") }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text("Current tier: ${preferences.accountTier.name}")
                }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text("Premium is defined but not billable yet.")
                }
                Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                    GhostButton(text = "Open About", onClick = { window.location.hash = "#about" })
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

@Composable
private fun PreferencesCard(
    preferences: UserPreferences,
    displayLoading: Boolean,
    onSave: (UserPreferences) -> Unit,
) {
    SurfaceCard {
        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Preferences") }

        PreferenceField(
            label = "Pack price",
            value = if (preferences.packPrice == 0.0) "" else preferences.packPrice.toString(),
            displayLoading = displayLoading,
            onChange = { raw ->
                onSave(preferences.copy(packPrice = raw.toDoubleOrNull() ?: 0.0))
            }
        )

        PreferenceField(
            label = "Cigarettes per pack",
            value = preferences.cigarettesPerPack.toString(),
            displayLoading = displayLoading,
            onChange = { raw ->
                onSave(
                    preferences.copy(
                        cigarettesPerPack = raw.toIntOrNull()?.coerceAtLeast(1) ?: preferences.cigarettesPerPack
                    )
                )
            }
        )

        PreferenceField(
            label = "First hour of the day",
            value = preferences.dayStartHour.toString().padStart(2, '0'),
            displayLoading = displayLoading,
            onChange = { raw ->
                onSave(preferences.copy(dayStartHour = raw.toIntOrNull()?.coerceIn(0, 23) ?: preferences.dayStartHour))
            }
        )

        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
            Input(type = InputType.Checkbox, attrs = {
                checked(preferences.locationTrackingEnabled)
                if (displayLoading) disabled()
                onInput {
                    onSave(preferences.copy(locationTrackingEnabled = !preferences.locationTrackingEnabled))
                }
            })
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("Track location with smokes")
            }
        }
    }
}

@Composable
private fun PreferenceField(
    label: String,
    value: String,
    displayLoading: Boolean,
    onChange: (String) -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(label) }
    Input(type = InputType.Text, attrs = {
        classes(SmokeWebStyles.dateInput)
        value(value)
        if (displayLoading) disabled()
        onInput { onChange(it.value) }
    })
}
