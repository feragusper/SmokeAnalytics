package com.feragusper.smokeanalytics.features.settings.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsWebStore
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponentWeb
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text

@Composable
fun SettingsWebScreen(
    deps: SettingsWebDependencies,
    onShare: suspend () -> Unit,
) {
    val store = remember(deps) { SettingsWebStore(processHolder = deps.processHolder) }

    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    state.Render(
        onIntent = { store.send(it) },
        onShare = onShare,
    )
}

@Composable
private fun SettingsViewState.Render(
    onIntent: (SettingsIntent) -> Unit,
    onShare: suspend () -> Unit,
) {
    var draftPreferences by remember(currentEmail, preferences) { mutableStateOf(preferences) }
    var showingGoals by remember(currentEmail, preferences.activeGoal) { mutableStateOf(false) }

    LaunchedEffect(preferences, currentEmail) {
        draftPreferences = preferences
    }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        if (showingGoals) {
            GoalsWebEditorPanel(
                currentEmail = currentEmail,
                preferences = draftPreferences,
                goalProgress = goalProgress,
                displayLoading = displayLoading,
                onBack = { showingGoals = false },
                onSaveGoal = { goal ->
                    draftPreferences = draftPreferences.copy(activeGoal = goal)
                    onIntent(SettingsIntent.UpdatePreferences(draftPreferences.copy(activeGoal = goal)))
                },
                onClearGoal = {
                    draftPreferences = draftPreferences.copy(activeGoal = null)
                    onIntent(SettingsIntent.UpdatePreferences(draftPreferences.copy(activeGoal = null)))
                },
            )
            errorMessage?.let { msg ->
                EmptyStateCard(
                    title = "Could not save your goal",
                    message = msg,
                    actionLabel = "Try again",
                    onAction = {
                        draftPreferences.activeGoal?.let { goal ->
                            onIntent(SettingsIntent.UpdatePreferences(draftPreferences.copy(activeGoal = goal)))
                        }
                    },
                )
            }
            infoMessage?.let { msg ->
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(msg) }
            }
            return@Div
        }

        HeroCard(
            displayLoading = displayLoading,
            currentEmail = currentEmail,
            currentDisplayName = currentDisplayName,
        )

        SectionHeader(
            title = "Account",
            subtitle = "Session state and core product context stay together here instead of splitting You into old Settings/About leftovers.",
        )

        if (currentEmail != null) {
            SessionCard(
                displayName = currentDisplayName,
                email = currentEmail,
                displayLoading = displayLoading,
                onSignOut = { onIntent(SettingsIntent.SignOut) },
            )
        } else {
            SignInCard(
                onRefresh = { onIntent(SettingsIntent.FetchUser) },
            )
        }

        Div(attrs = {
            attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
        }) {
            HighlightCard(
                title = "Plan",
                value = preferences.accountTier.name,
                body = "Premium stays framed as a future upgrade with richer insights and no ads.",
            )
            HighlightCard(
                title = "Points",
                value = "Recovery",
                body = "Progress is tied to smoke-free gaps, not perfection. Longer gaps keep the score moving.",
            )
        }

        SectionHeader(
            title = "Goals",
            subtitle = "The active target stays visible here, with the full editor still nested inside You.",
        )

        GoalsCard(
            goalProgress = goalProgress,
            activeGoal = preferences.activeGoal,
            onOpenGoals = { showingGoals = true },
        )

        errorMessage?.let { msg ->
            EmptyStateCard(
                title = "Your space is unavailable",
                message = msg,
                actionLabel = "Try again",
                onAction = { onIntent(SettingsIntent.FetchUser) },
            )
        }

        if (displayLoading && currentEmail == null) {
            LoadingSkeletonCard(heightPx = 120, lineWidths = listOf("42%", "70%", "36%"))
            return@Div
        }

        SectionHeader(
            title = "Preferences",
            subtitle = "Routine and cost settings shape how the rest of the product interprets your day.",
        )

        Div(attrs = {
            attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
        }) {
            HighlightCard(
                title = "Day model",
                value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
                body = "Wake-up hour used as the main bucket boundary across Home, History, and Analytics.",
            )
            HighlightCard(
                title = "Sleep starts",
                value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                body = "Sleep hours are excluded from the mindful gap target and the daily hourly average.",
            )
            HighlightCard(
                title = "Location",
                value = if (preferences.locationTrackingEnabled) "On" else "Off",
                body = if (preferences.locationTrackingEnabled) {
                    "Location tracking is enabled, so map insights can learn from repeated areas."
                } else {
                    "Location tracking is off, so map insights stay unavailable until the setting changes."
                },
            )
        }

        if (currentEmail != null) {
            PreferencesCard(
                preferences = draftPreferences,
                displayLoading = displayLoading,
                onPreferencesChange = { draftPreferences = it },
                onSave = {
                    onIntent(
                        SettingsIntent.UpdatePreferences(
                            preferences = draftPreferences,
                        )
                    )
                },
                onReset = { draftPreferences = preferences },
            )
        } else {
            SurfaceCard {
                Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;") }) {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Preferences") }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text("Sign in to edit routine preferences and keep them synced across mobile and web.")
                    }
                }
            }
        }

        SectionHeader(
            title = "App",
            subtitle = "Support, sharing, and product metadata stay visible inside You instead of a detached About route.",
        )

        AppInfoCard(
            accountTier = preferences.accountTier.name,
            onShare = onShare,
        )

        infoMessage?.let { msg ->
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(msg) }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:6px;max-width:760px;") }) {
        Div(attrs = { attr("style", "font-size:20px;font-weight:800;color:var(--sa-color-primary);") }) {
            Text(title)
        }
        Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(subtitle) }
    }
}

@Composable
private fun HeroCard(
    displayLoading: Boolean,
    currentEmail: String?,
    currentDisplayName: String?,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;") }) {
            Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                Text("You")
            }
            Div(attrs = { attr("style", "font-size:36px;font-weight:800;line-height:1.1;color:var(--sa-color-primary);max-width:680px;") }) {
                Text(
                    if (currentDisplayName.isNullOrBlank()) {
                        "Keep your routine, goals, and account in sync."
                    } else {
                        "Keep ${currentDisplayName}'s routine, goals, and account in sync."
                    }
                )
            }
            Div(attrs = { attr("style", "font-size:16px;line-height:1.6;color:var(--sa-color-secondary);max-width:760px;") }) {
                Text(
                    if (currentEmail == null) {
                        "Sign in to sync preferences, preserve progress, and keep goals ready across devices."
                    } else {
                        "Review session state, tune how the app interprets your day, and keep the next goals flow anchored here."
                    }
                )
            }
            Div(attrs = {
                attr(
                    "style",
                    "display:inline-flex;align-items:center;width:max-content;padding:8px 12px;border-radius:999px;background:var(--sa-color-secondaryContainer);color:var(--sa-color-onSecondaryContainer);font-size:13px;font-weight:600;"
                )
            }) {
                Text(
                    when {
                        displayLoading -> "Refreshing"
                        currentEmail != null -> "Signed in"
                        else -> "Guest mode"
                    }
                )
            }
        }
    }
}

@Composable
private fun GoalsCard(
    goalProgress: GoalProgress?,
    activeGoal: com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal?,
    onOpenGoals: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;") }) {
            Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                Text("Goals")
            }
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                Text(goalProgress?.title ?: "Your next personal targets start here.")
            }
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text(goalProgress?.supportingText ?: "Daily caps, reduction plans, and mindful-gap targets live here inside You.")
            }
            goalProgress?.targetLabel?.let {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(it) }
            }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                PrimaryButton(
                    text = if (activeGoal == null) "Set up goals" else "Review goals",
                    onClick = onOpenGoals,
                )
            }
        }
    }
}

@Composable
private fun AppInfoCard(
    accountTier: String,
    onShare: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:16px;") }) {
            Div(attrs = {
                attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
            }) {
                SessionValueCard(
                    title = "Plan",
                    value = accountTier,
                    body = "Premium remains framed as a future upgrade with richer insights and no ads."
                )
                SessionValueCard(
                    title = "Version",
                    value = "Web",
                    body = "The browser surface stays aligned with the same product direction as mobile."
                )
            }

            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;") }) {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Actions") }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text("Share the app, report bugs, and reach support from the same personal destination.")
                }
                Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                    PrimaryButton(
                        text = "Share app",
                        onClick = { scope.launch { onShare() } },
                    )
                    A("https://github.com/feragusper/SmokeAnalytics/issues/new/choose", attrs = { attr("target", "_blank") }) {
                        Text("Report bug")
                    }
                    A("mailto:feragusper@gmail.com", attrs = { attr("target", "_blank") }) {
                        Text("Contact us")
                    }
                    A("https://github.com/feragusper/SmokeAnalytics", attrs = { attr("target", "_blank") }) {
                        Text("GitHub")
                    }
                }
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
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Session") }
            displayName?.takeIf { it.isNotBlank() }?.let {
                Div(attrs = { attr("style", "font-size:20px;font-weight:700;color:var(--sa-color-primary);") }) { Text(it) }
            }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(email) }

            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                PrimaryButton(
                    text = if (displayLoading) "Working..." else "Sign out",
                    onClick = onSignOut,
                    enabled = !displayLoading,
                )
            }
        }
    }
}

@Composable
private fun SignInCard(
    onRefresh: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:18px;") }) {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;max-width:720px;") }) {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Session") }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text("Guest mode keeps the shell readable, but sign-in restores synced preferences, a stable archive, and the right context for coach insights.")
                }
            }

            Div(attrs = {
                attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:16px;")
            }) {
                SessionValueCard(
                    title = "Preferences",
                    value = "Routine sync",
                    body = "Carry pack price, day-start hour, and location settings across devices."
                )
                SessionValueCard(
                    title = "History",
                    value = "Stable archive",
                    body = "Keep edits and older smoke entries connected to the same account."
                )
                SessionValueCard(
                    title = "Coach",
                    value = "Relevant context",
                    body = "Give the guide enough recent behavior to stay grounded instead of generic."
                )
            }

            GoogleSignInComponentWeb(
                onSignInSuccess = onRefresh,
                onSignInError = { _ -> onRefresh() },
            )
        }
    }
}

@Composable
private fun SessionValueCard(
    title: String,
    value: String,
    body: String,
) {
    Div(
        attrs = {
            attr(
                "style",
                "display:flex;flex-direction:column;gap:8px;min-height:164px;padding:18px;border-radius:22px;background:var(--sa-color-surface-strong);border:1px solid var(--sa-color-outline);"
            )
        }
    ) {
        Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
            Text(title)
        }
        Div(attrs = { attr("style", "font-size:24px;font-weight:800;color:var(--sa-color-primary);") }) {
            Text(value)
        }
        Div(attrs = { classes(SmokeWebStyles.helperText) }) {
            Text(body)
        }
    }
}

@Composable
private fun HighlightCard(
    title: String,
    value: String,
    body: String,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:8px;min-height:180px;") }) {
            Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                Text(title)
            }
            Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                Text(value)
            }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text(body)
            }
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
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:16px;") }) {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Preferences") }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("Values that shape cost calculations, day buckets, and map behavior.")
            }

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

            TimeField(
                label = "Bedtime",
                value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                displayLoading = displayLoading,
                onChange = {
                    val raw = it.substringBefore(":").toIntOrNull() ?: return@TimeField
                    onPreferencesChange(preferences.copy(bedtimeHour = raw.coerceIn(0, 23)))
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

@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit,
) {
    Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:8px;") }) {
        Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(label) }
        content()
    }
}

private fun Double.asDecimalString(): String {
    val scaled = (this * 100).roundToInt()
    val integerPart = scaled / 100
    val decimalPart = abs(scaled % 100).toString().padStart(2, '0')
    return "$integerPart.$decimalPart"
}
