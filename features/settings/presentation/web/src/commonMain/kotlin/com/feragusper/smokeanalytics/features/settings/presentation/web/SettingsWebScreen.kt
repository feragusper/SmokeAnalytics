package com.feragusper.smokeanalytics.features.settings.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text

@Composable
fun SettingsWebScreen(
    deps: SettingsWebDependencies,
) {
    val store = remember(deps) { SettingsWebStore(processHolder = deps.processHolder) }

    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    state.Render(onIntent = { store.send(it) })
}

@Composable
private fun SettingsViewState.Render(
    onIntent: (SettingsIntent) -> Unit,
) {
    var draftPreferences by remember(currentEmail, preferences) { mutableStateOf(preferences) }

    LaunchedEffect(preferences, currentEmail) {
        draftPreferences = preferences
    }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Settings",
            eyebrow = "Preferences",
            badgeText = when {
                displayLoading -> "Saving"
                currentEmail != null -> "Signed in"
                else -> "Guest"
            },
            badgeTone = if (displayLoading) StatusTone.Busy else StatusTone.Default,
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
            SessionCard(
                displayName = currentDisplayName,
                email = currentEmail,
                displayLoading = displayLoading,
                onSignOut = { onIntent(SettingsIntent.SignOut) },
            )

            PreferencesCard(
                preferences = draftPreferences,
                displayLoading = displayLoading,
                onPreferencesChange = { draftPreferences = it },
                onSave = {
                    onIntent(
                        SettingsIntent.UpdatePreferences(
                            packPrice = draftPreferences.packPrice,
                            cigarettesPerPack = draftPreferences.cigarettesPerPack,
                            dayStartHour = draftPreferences.dayStartHour,
                            locationTrackingEnabled = draftPreferences.locationTrackingEnabled,
                            currencySymbol = draftPreferences.currencySymbol,
                        )
                    )
                },
                onReset = { draftPreferences = preferences },
            )

            SurfaceCard {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Account") }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text("Current tier: ${preferences.accountTier.name}")
                }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text("Premium is defined but not billable yet.")
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
private fun SessionCard(
    displayName: String?,
    email: String,
    displayLoading: Boolean,
    onSignOut: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Signed in") }
        displayName?.takeIf { it.isNotBlank() }?.let {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(it) }
        }
        Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(email) }

        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
            PrimaryButton(
                text = if (displayLoading) "Working..." else "Sign out",
                onClick = onSignOut,
                enabled = !displayLoading
            )
        }
    }
}

@Composable
private fun PreferencesCard(
    preferences: UserPreferences,
    displayLoading: Boolean,
    onPreferencesChange: (UserPreferences) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Preferences") }

        ChoiceField(
            label = "Currency",
            displayLoading = displayLoading,
            selected = preferences.currencySymbol,
            options = listOf("€", "$", "£"),
            onSelection = { onPreferencesChange(preferences.copy(currencySymbol = it)) },
        )

        NumberField(
            label = "Pack price",
            value = preferences.packPrice,
            prefix = preferences.currencySymbol,
            step = 0.5,
            displayLoading = displayLoading,
            onDecrease = {
                onPreferencesChange(preferences.copy(packPrice = (preferences.packPrice - 0.5).coerceAtLeast(0.0)))
            },
            onIncrease = {
                onPreferencesChange(preferences.copy(packPrice = preferences.packPrice + 0.5))
            },
            onManualChange = { raw ->
                raw.toDoubleOrNull()?.let { value ->
                    onPreferencesChange(preferences.copy(packPrice = value.coerceAtLeast(0.0)))
                }
            },
        )

        NumberField(
            label = "Cigarettes per pack",
            value = preferences.cigarettesPerPack.toDouble(),
            step = 1.0,
            displayLoading = displayLoading,
            onDecrease = {
                onPreferencesChange(
                    preferences.copy(cigarettesPerPack = (preferences.cigarettesPerPack - 1).coerceAtLeast(1))
                )
            },
            onIncrease = {
                onPreferencesChange(preferences.copy(cigarettesPerPack = preferences.cigarettesPerPack + 1))
            },
            onManualChange = { raw ->
                raw.toIntOrNull()?.let { value ->
                    onPreferencesChange(preferences.copy(cigarettesPerPack = value.coerceAtLeast(1)))
                }
            },
        )

        TimeField(
            label = "First hour of the day",
            value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
            displayLoading = displayLoading,
            onChange = {
                val raw = it.substringBefore(":").toIntOrNull() ?: return@TimeField
                onPreferencesChange(preferences.copy(dayStartHour = raw.coerceIn(0, 23)))
            },
        )

        LabeledField(label = "Track location with smokes") {
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                Input(type = InputType.Checkbox, attrs = {
                    if (preferences.locationTrackingEnabled) attr("checked", "true")
                    if (displayLoading) disabled()
                    onInput {
                        onPreferencesChange(preferences.copy(locationTrackingEnabled = !preferences.locationTrackingEnabled))
                    }
                })
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text("Optional. Used for map insights.")
                }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
            GhostButton(text = "Reset", onClick = onReset, enabled = !displayLoading)
            PrimaryButton(text = "Save", onClick = onSave, enabled = !displayLoading)
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: Double,
    prefix: String = "",
    step: Double,
    displayLoading: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onManualChange: (String) -> Unit,
) {
    LabeledField(label = label) {
        Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                if (prefix.isNotEmpty()) {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(prefix) }
                }
                Input(type = InputType.Number, attrs = {
                    classes(SmokeWebStyles.dateInput)
                    value(if (step < 1.0) value.asDecimalString() else value.toInt().toString())
                    attr("step", step.toString())
                    attr("inputmode", "decimal")
                    if (prefix.isNotEmpty()) attr("aria-label", "$label in $prefix")
                    if (displayLoading) disabled()
                    onInput { onManualChange(it.value.toString()) }
                })
            }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                GhostButton(text = "−", onClick = onDecrease, enabled = !displayLoading)
                GhostButton(text = "+", onClick = onIncrease, enabled = !displayLoading)
            }
        }
    }
}

@Composable
private fun TimeField(
    label: String,
    value: String,
    displayLoading: Boolean,
    onChange: (String) -> Unit,
) {
    LabeledField(label = label) {
        Input(type = InputType.Time, attrs = {
            classes(SmokeWebStyles.dateInput)
            value(value)
            if (displayLoading) disabled()
            onInput { onChange(it.value) }
        })
    }
}

@Composable
private fun ChoiceField(
    label: String,
    displayLoading: Boolean,
    selected: String,
    options: List<String>,
    onSelection: (String) -> Unit,
) {
    LabeledField(label = label) {
        Select(attrs = {
            classes(SmokeWebStyles.dateInput)
            if (displayLoading) disabled()
            onChange { event -> onSelection(event.value ?: selected) }
        }) {
            options.forEach { symbol ->
                Option(value = symbol, attrs = {
                    if (selected == symbol) attr("selected", "selected")
                }) {
                    Text(symbol)
                }
            }
        }
    }
}

private fun Double.asDecimalString(): String {
    val scaled = (this * 100).toInt()
    val whole = scaled / 100
    val fraction = (scaled % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
}

@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(label) }
        content()
    }
}
