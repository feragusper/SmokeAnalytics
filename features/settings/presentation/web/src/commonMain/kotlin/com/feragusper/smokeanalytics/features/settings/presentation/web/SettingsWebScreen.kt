package com.feragusper.smokeanalytics.features.settings.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.TriggerEmojiPalette
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.normalizedTag
import org.jetbrains.compose.web.dom.Button
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
import org.jetbrains.compose.web.dom.Span
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

    LaunchedEffect(preferences, currentEmail) {
        draftPreferences = preferences
    }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        if (displayLoading && currentEmail == null && errorMessage == null) {
            YouLoadingSkeleton()
            return@Div
        }

        if (errorMessage != null && currentEmail == null) {
            EmptyStateCard(
                title = "Your space is unavailable",
                message = errorMessage,
                actionLabel = "Try again",
                onAction = { onIntent(SettingsIntent.FetchUser) },
            )
            return@Div
        }

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

        errorMessage?.let { msg ->
            EmptyStateCard(
                title = "Your space is unavailable",
                message = msg,
                actionLabel = "Try again",
                onAction = { onIntent(SettingsIntent.FetchUser) },
            )
        }

        SectionHeader(
            title = "Preferences",
            subtitle = "Routine and cost settings shape how the rest of the product interprets your day.",
        )

        if (currentEmail != null) {
            PreferencesCard(
                preferences = draftPreferences,
                displayLoading = displayLoading,
                onCommit = { updated ->
                    draftPreferences = updated
                    onIntent(SettingsIntent.UpdatePreferences(preferences = updated))
                },
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

        if (currentEmail != null) {
            SectionHeader(
                title = "Triggers",
                subtitle = "Choose which built-in triggers appear when tagging a cigarette, and add your own.",
            )
            ManageTriggersPanelWeb(
                preferences = draftPreferences,
                displayLoading = displayLoading,
                onChange = { updated ->
                    draftPreferences = updated
                    onIntent(SettingsIntent.UpdatePreferences(preferences = updated))
                },
            )
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
private fun YouLoadingSkeleton() {
    LoadingSkeletonCard(heightPx = 176, lineWidths = listOf("30%", "68%", "44%"))
    LoadingSkeletonCard(heightPx = 128, lineWidths = listOf("24%", "54%", "38%"))
    Div(attrs = {
        attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
    }) {
        LoadingSkeletonCard(heightPx = 116, lineWidths = listOf("28%", "48%"))
        LoadingSkeletonCard(heightPx = 116, lineWidths = listOf("30%", "52%"))
    }
    LoadingSkeletonCard(heightPx = 148, lineWidths = listOf("24%", "62%", "42%"))
    LoadingSkeletonCard(heightPx = 180, lineWidths = listOf("22%", "58%", "46%"))
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
                        text = "↗ Share app",
                        onClick = { scope.launch { onShare() } },
                    )
                    LinkButton(
                        glyph = "⚑",
                        label = "Report bug",
                        href = "https://github.com/feragusper/SmokeAnalytics/issues/new/choose",
                    )
                    LinkButton(
                        glyph = "✉",
                        label = "Contact us",
                        href = "mailto:feragusper@gmail.com",
                    )
                    LinkButton(
                        glyph = "⌗",
                        label = "GitHub",
                        href = "https://github.com/feragusper/SmokeAnalytics",
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkButton(
    glyph: String,
    label: String,
    href: String,
) {
    A(href, attrs = {
        classes(SmokeWebStyles.button)
        attr("target", "_blank")
        attr("style", "text-decoration:none;display:inline-flex;align-items:center;gap:8px;")
    }) {
        Span { Text(glyph) }
        Span { Text(label) }
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
                    Text("Guest mode keeps the shell readable, but sign-in restores synced preferences, a stable archive, and goals across devices.")
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
                    title = "Goals",
                    value = "Stable targets",
                    body = "Keep reduction targets and product preferences connected to the same account."
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
    onCommit: (UserPreferences) -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:16px;") }) {
            Div(attrs = {
                attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
            }) {
                PreferenceSummaryCard(
                    title = "Day model",
                    value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
                    body = "Wake-up hour used as the main bucket boundary across Home, History, and Analytics.",
                ) {
                    TimeField(
                        label = "First hour of the day",
                        value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
                        displayLoading = displayLoading,
                        onChange = {
                            val raw = it.substringBefore(":").toIntOrNull() ?: return@TimeField
                            onCommit(preferences.copy(dayStartHour = raw.coerceIn(0, 23)))
                        },
                    )
                }
                PreferenceSummaryCard(
                    title = "Sleep starts",
                    value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                    body = "Sleep hours are excluded from the mindful gap target and the daily hourly average.",
                ) {
                    TimeField(
                        label = "Bedtime",
                        value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                        displayLoading = displayLoading,
                        onChange = {
                            val raw = it.substringBefore(":").toIntOrNull() ?: return@TimeField
                            onCommit(preferences.copy(bedtimeHour = raw.coerceIn(0, 23)))
                        },
                    )
                }
                PreferenceSummaryCard(
                    title = "Location",
                    value = if (preferences.locationTrackingEnabled) "On" else "Off",
                    body = if (preferences.locationTrackingEnabled) {
                        "Location tracking is enabled, so map insights can learn from repeated areas."
                    } else {
                        "Location tracking is off, so map insights stay unavailable until the setting changes."
                    },
                ) {
                    LabeledField(label = "Track location with smokes") {
                        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                            Input(type = InputType.Checkbox, attrs = {
                                if (preferences.locationTrackingEnabled) attr("checked", "true")
                                if (displayLoading) disabled()
                                onInput {
                                    onCommit(preferences.copy(locationTrackingEnabled = !preferences.locationTrackingEnabled))
                                }
                            })
                            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                                Text("Optional. Used for map insights.")
                            }
                        }
                    }
                }
                PreferenceSummaryCard(
                    title = "Currency",
                    value = preferences.currencySymbol,
                    body = "Used for pack-price and cost calculations across the product.",
                ) {
                    ChoiceField(
                        label = "Currency",
                        displayLoading = displayLoading,
                        selected = preferences.currencySymbol,
                        options = listOf("€", "$", "£"),
                        onSelection = { onCommit(preferences.copy(currencySymbol = it)) },
                    )
                }
                PreferenceSummaryCard(
                    title = "Pack price",
                    value = preferences.currencySymbol + preferences.packPrice.asDecimalString(),
                    body = "The cost base for spend estimates and progress summaries.",
                ) {
                    NumberField(
                        label = "Pack price",
                        value = preferences.packPrice,
                        prefix = preferences.currencySymbol,
                        step = 0.5,
                        displayLoading = displayLoading,
                        onDecrease = {
                            onCommit(preferences.copy(packPrice = (preferences.packPrice - 0.5).coerceAtLeast(0.0)))
                        },
                        onIncrease = {
                            onCommit(preferences.copy(packPrice = preferences.packPrice + 0.5))
                        },
                        onManualChange = { raw ->
                            raw.toDoubleOrNull()?.let { value ->
                                onCommit(preferences.copy(packPrice = value.coerceAtLeast(0.0)))
                            }
                        },
                    )
                }
                PreferenceSummaryCard(
                    title = "Cigarettes per pack",
                    value = preferences.cigarettesPerPack.toString(),
                    body = "Keeps cost metrics and pack-level estimates aligned.",
                ) {
                    NumberField(
                        label = "Cigarettes per pack",
                        value = preferences.cigarettesPerPack.toDouble(),
                        step = 1.0,
                        displayLoading = displayLoading,
                        onDecrease = {
                            onCommit(
                                preferences.copy(cigarettesPerPack = (preferences.cigarettesPerPack - 1).coerceAtLeast(1))
                            )
                        },
                        onIncrease = {
                            onCommit(preferences.copy(cigarettesPerPack = preferences.cigarettesPerPack + 1))
                        },
                        onManualChange = { raw ->
                            raw.toIntOrNull()?.let { value ->
                                onCommit(preferences.copy(cigarettesPerPack = value.coerceAtLeast(1)))
                            }
                        },
                    )
                }
            }

            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("Changes are saved automatically.")
            }
        }
    }
}

@Composable
private fun PreferenceSummaryCard(
    title: String,
    value: String,
    body: String,
    editor: @Composable () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;min-height:220px;") }) {
            Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                Text(title)
            }
            Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                Text(value)
            }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text(body)
            }
            editor()
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
                    onChange { onManualChange(it.value.toString()) }
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
            onChange { event -> onChange(event.value) }
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

@Composable
private fun ManageTriggersPanelWeb(
    preferences: UserPreferences,
    displayLoading: Boolean,
    onChange: (UserPreferences) -> Unit,
) {
    var draft by remember { mutableStateOf("") }

    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text("Built-in") }
            SmokeTrigger.defaultOptions()
                .filter { it.key !in preferences.hiddenDefaultTriggers }
                .forEach { option ->
                    TriggerRowWeb(
                        icon = preferences.triggerIcons[option.key] ?: option.icon.orEmpty(),
                        label = preferences.triggerLabels[option.key] ?: option.label,
                        enabled = !displayLoading,
                        onIconCommit = { icon -> onChange(preferences.withTriggerIcon(option.key, icon)) },
                        onRename = { name -> onChange(preferences.withTriggerLabel(option.key, name)) },
                        onRemove = {
                            onChange(
                                preferences.copy(hiddenDefaultTriggers = preferences.hiddenDefaultTriggers + option.key),
                            )
                        },
                    )
                }
            if (preferences.hiddenDefaultTriggers.isNotEmpty()) {
                GhostButton(
                    text = "Restore removed defaults (${preferences.hiddenDefaultTriggers.size})",
                    enabled = !displayLoading,
                    onClick = { onChange(preferences.copy(hiddenDefaultTriggers = emptySet())) },
                )
            }

            if (preferences.customTriggers.isNotEmpty()) {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text("Your tags") }
                preferences.customTriggers.forEach { tag ->
                    TriggerRowWeb(
                        icon = preferences.triggerIcons[tag].orEmpty(),
                        label = preferences.triggerLabels[tag] ?: tag,
                        enabled = !displayLoading,
                        onIconCommit = { icon -> onChange(preferences.withTriggerIcon(tag, icon)) },
                        onRename = { name -> onChange(preferences.withTriggerLabel(tag, name)) },
                        onRemove = { onChange(preferences.copy(customTriggers = preferences.customTriggers - tag)) },
                    )
                }
            }

            Div(attrs = { attr("style", "display:flex;gap:8px;align-items:center;") }) {
                Input(
                    type = InputType.Text,
                    attrs = {
                        value(draft)
                        onInput { draft = it.value }
                        attr("placeholder", "Add a tag…")
                        style {
                            property("flex", "1")
                            property("box-sizing", "border-box")
                            property("padding", "8px")
                        }
                    },
                )
                PrimaryButton(
                    text = "Add",
                    enabled = !displayLoading && draft.normalizedTag() != null,
                    onClick = {
                        val key = draft.normalizedTag() ?: return@PrimaryButton
                        if (preferences.customTriggers.none { it.equals(key, ignoreCase = true) }) {
                            onChange(preferences.copy(customTriggers = preferences.customTriggers + key))
                        }
                        draft = ""
                    },
                )
            }
        }
    }
}

/** Small per-row editor for a trigger's emoji; commits on change (blur/enter). */
@Composable
private fun TriggerIconInputWeb(
    icon: String,
    enabled: Boolean,
    onCommit: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }

    Div(attrs = { attr("style", "position:relative;") }) {
        Button(
            attrs = {
                if (!enabled) disabled()
                onClick { open = !open }
                attr("aria-label", "Pick trigger icon")
                style {
                    property("width", "44px")
                    property("height", "36px")
                    property("text-align", "center")
                    property("cursor", "pointer")
                    property("font-size", "18px")
                    property("background", "var(--sa-color-surface-strong)")
                    property("color", "var(--sa-color-onSurface)")
                    property("border", "1px solid var(--sa-color-outline)")
                    property("border-radius", "8px")
                }
            }
        ) { Text(icon.ifBlank { "＋" }) }

        if (open) {
            // Transparent backdrop: click anywhere outside closes the picker.
            Div(attrs = {
                attr("style", "position:fixed;inset:0;z-index:9998;")
                onClick { open = false }
            })
            Div(attrs = {
                attr(
                    "style",
                    "position:absolute;top:40px;left:0;z-index:9999;" +
                        "background:var(--sa-color-surface);color:var(--sa-color-onSurface);" +
                        "border:1px solid var(--sa-color-outline);border-radius:12px;" +
                        "padding:10px;box-shadow:0 8px 24px rgba(0,0,0,0.25);" +
                        "display:flex;flex-wrap:wrap;gap:4px;width:280px;max-height:240px;overflow-y:auto;",
                )
            }) {
                TriggerEmojiPalette.forEach { emoji ->
                    Button(
                        attrs = {
                            onClick {
                                onCommit(emoji)
                                open = false
                            }
                            style {
                                property("font-size", "18px")
                                property("padding", "6px")
                                property("cursor", "pointer")
                                property("background", "transparent")
                                property("border", "none")
                                property("border-radius", "8px")
                            }
                        }
                    ) { Text(emoji) }
                }
                Button(
                    attrs = {
                        onClick {
                            // Clears the override: built-ins fall back to their default icon.
                            onCommit("")
                            open = false
                        }
                        style {
                            property("width", "100%")
                            property("margin-top", "6px")
                            property("padding", "6px")
                            property("cursor", "pointer")
                            property("background", "transparent")
                            property("color", "var(--sa-color-primary)")
                            property("border", "1px solid var(--sa-color-outline)")
                            property("border-radius", "8px")
                        }
                    }
                ) { Text("Reset") }
            }
        }
    }
}

/** Sets/clears the emoji for a trigger key (blank clears the override). */
private fun UserPreferences.withTriggerIcon(key: String, icon: String): UserPreferences =
    copy(
        triggerIcons = if (icon.isBlank()) triggerIcons - key else triggerIcons + (key to icon),
    )

/** One trigger row: icon picker, (renamable) label with inline edit, remove action. */
@Composable
private fun TriggerRowWeb(
    icon: String,
    label: String,
    enabled: Boolean,
    onIconCommit: (String) -> Unit,
    onRename: (String) -> Unit,
    onRemove: () -> Unit,
) {
    var renaming by remember { mutableStateOf(false) }
    var draft by remember(label) { mutableStateOf(label) }

    Div(attrs = { attr("style", "display:flex;align-items:center;gap:10px;") }) {
        TriggerIconInputWeb(icon = icon, enabled = enabled, onCommit = onIconCommit)
        if (renaming) {
            Input(
                type = InputType.Text,
                attrs = {
                    value(draft)
                    onInput { draft = it.value }
                    attr("placeholder", "Leave empty to restore the original name")
                    style {
                        property("flex", "1")
                        property("box-sizing", "border-box")
                        property("padding", "6px 8px")
                        property("background", "var(--sa-color-surface-strong)")
                        property("color", "var(--sa-color-onSurface)")
                        property("border", "1px solid var(--sa-color-outline)")
                        property("border-radius", "8px")
                    }
                },
            )
            PrimaryButton(
                text = "Save",
                enabled = enabled,
                onClick = {
                    onRename(draft.trim())
                    renaming = false
                },
            )
            GhostButton(text = "Cancel", onClick = { renaming = false })
        } else {
            Span(attrs = { attr("style", "flex:1;") }) { Text(label) }
            GhostButton(text = "Rename", enabled = enabled, onClick = { renaming = true })
            GhostButton(text = "Remove", enabled = enabled, onClick = onRemove)
        }
    }
}

/** Renames a trigger without touching the key stored on smokes (blank clears the override). */
private fun UserPreferences.withTriggerLabel(key: String, label: String): UserPreferences =
    copy(
        triggerLabels = if (label.isBlank()) triggerLabels - key else triggerLabels + (key to label),
    )
