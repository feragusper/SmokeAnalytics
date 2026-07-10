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
import com.feragusper.smokeanalytics.libraries.design.i18n.LocalStrings
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.searchEmojis
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
                title = LocalStrings.current.yourSpaceUnavailable,
                message = errorMessage,
                actionLabel = LocalStrings.current.tryAgain,
                onAction = { onIntent(SettingsIntent.FetchUser) },
            )
            return@Div
        }

        SectionHeader(
            title = LocalStrings.current.account,
            subtitle = LocalStrings.current.accountSubtitle,
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
                title = LocalStrings.current.plan,
                value = preferences.accountTier.name,
                body = LocalStrings.current.planPremiumBody,
            )
            HighlightCard(
                title = LocalStrings.current.pointsCard,
                value = LocalStrings.current.recovery,
                body = LocalStrings.current.pointsBody,
            )
        }

        errorMessage?.let { msg ->
            EmptyStateCard(
                title = LocalStrings.current.yourSpaceUnavailable,
                message = msg,
                actionLabel = LocalStrings.current.tryAgain,
                onAction = { onIntent(SettingsIntent.FetchUser) },
            )
        }

        SectionHeader(
            title = LocalStrings.current.preferences,
            subtitle = LocalStrings.current.preferencesSubtitle,
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
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(LocalStrings.current.preferences) }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text(LocalStrings.current.preferencesSignInHint)
                    }
                }
            }
        }

        if (currentEmail != null) {
            CollapsibleSection(
                title = LocalStrings.current.personalization,
                subtitle = LocalStrings.current.personalizationSubtitle,
                initiallyExpanded = false,
            ) {
                PersonalizationPanelWeb(
                    preferences = draftPreferences,
                    displayLoading = displayLoading,
                    onChange = { updated ->
                        draftPreferences = updated
                        onIntent(SettingsIntent.UpdatePreferences(preferences = updated))
                    },
                )
            }
        }

        if (currentEmail != null) {
            CollapsibleSection(
                title = LocalStrings.current.triggers,
                subtitle = LocalStrings.current.triggersSubtitle,
                initiallyExpanded = false,
            ) {
                ManageTriggersPanelWeb(
                    preferences = draftPreferences,
                    displayLoading = displayLoading,
                    onChange = { updated ->
                        draftPreferences = updated
                        onIntent(SettingsIntent.UpdatePreferences(preferences = updated))
                    },
                )
            }
        }

        SectionHeader(
            title = LocalStrings.current.appSection,
            subtitle = LocalStrings.current.appSectionSubtitle,
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

/** Section whose header toggles its content, so long panels don't stretch the page. */
@Composable
private fun CollapsibleSection(
    title: String,
    subtitle: String,
    initiallyExpanded: Boolean = true,
    content: @Composable () -> Unit,
) {
    var expanded by remember(title) { mutableStateOf(initiallyExpanded) }
    Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
        Div(attrs = {
            attr("style", "display:flex;align-items:center;gap:12px;cursor:pointer;max-width:760px;")
            onClick { expanded = !expanded }
        }) {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:6px;flex:1;") }) {
                Div(attrs = { attr("style", "font-size:20px;font-weight:800;color:var(--sa-color-primary);") }) {
                    Text(title)
                }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(subtitle) }
            }
            Div(attrs = { attr("style", "font-size:16px;color:var(--sa-color-secondary);") }) {
                Text(if (expanded) "▾" else "▸")
            }
        }
        if (expanded) content()
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
                    title = LocalStrings.current.plan,
                    value = accountTier,
                    body = LocalStrings.current.planPremiumBody
                )
                SessionValueCard(
                    title = LocalStrings.current.version,
                    value = LocalStrings.current.sidebarSectionWeb,
                    body = LocalStrings.current.webCardBody
                )
            }

            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;") }) {
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(LocalStrings.current.actions) }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text(LocalStrings.current.actionsSubtitle)
                }
                Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                    PrimaryButton(
                        text = LocalStrings.current.shareAppArrow,
                        onClick = { scope.launch { onShare() } },
                    )
                    LinkButton(
                        glyph = "⚑",
                        label = LocalStrings.current.reportBug,
                        href = "https://github.com/feragusper/SmokeAnalytics/issues/new/choose",
                    )
                    LinkButton(
                        glyph = "✉",
                        label = LocalStrings.current.contactUs,
                        href = "mailto:feragusper@gmail.com",
                    )
                    LinkButton(
                        glyph = "⌗",
                        label = LocalStrings.current.github,
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
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(LocalStrings.current.session) }
            displayName?.takeIf { it.isNotBlank() }?.let {
                Div(attrs = { attr("style", "font-size:20px;font-weight:700;color:var(--sa-color-primary);") }) { Text(it) }
            }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(email) }

            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                PrimaryButton(
                    text = if (displayLoading) LocalStrings.current.working else LocalStrings.current.signOut,
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
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(LocalStrings.current.session) }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text(LocalStrings.current.sessionGuestBody)
                }
            }

            Div(attrs = {
                attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:16px;")
            }) {
                SessionValueCard(
                    title = LocalStrings.current.preferences,
                    value = LocalStrings.current.routineSync,
                    body = LocalStrings.current.preferencesCardBody
                )
                SessionValueCard(
                    title = LocalStrings.current.navHistory,
                    value = LocalStrings.current.stableArchive,
                    body = LocalStrings.current.historyCardBody
                )
                SessionValueCard(
                    title = LocalStrings.current.goalsTitle,
                    value = LocalStrings.current.stableTargets,
                    body = LocalStrings.current.goalsCardBody
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
                    title = LocalStrings.current.dayModel,
                    value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
                    body = LocalStrings.current.dayModelBody,
                ) {
                    TimeField(
                        label = LocalStrings.current.firstHourOfDay,
                        value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
                        displayLoading = displayLoading,
                        onChange = {
                            val raw = it.substringBefore(":").toIntOrNull() ?: return@TimeField
                            onCommit(preferences.copy(dayStartHour = raw.coerceIn(0, 23)))
                        },
                    )
                }
                PreferenceSummaryCard(
                    title = LocalStrings.current.sleepStarts,
                    value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                    body = LocalStrings.current.firstHourBody,
                ) {
                    TimeField(
                        label = LocalStrings.current.bedtime,
                        value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                        displayLoading = displayLoading,
                        onChange = {
                            val raw = it.substringBefore(":").toIntOrNull() ?: return@TimeField
                            onCommit(preferences.copy(bedtimeHour = raw.coerceIn(0, 23)))
                        },
                    )
                }
                PreferenceSummaryCard(
                    title = LocalStrings.current.location,
                    value = if (preferences.locationTrackingEnabled) "On" else LocalStrings.current.off,
                    body = if (preferences.locationTrackingEnabled) {
                        LocalStrings.current.locationOnBody
                    } else {
                        LocalStrings.current.locationOffShortBody
                    },
                ) {
                    LabeledField(label = LocalStrings.current.trackLocationWithSmokes) {
                        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                            Input(type = InputType.Checkbox, attrs = {
                                if (preferences.locationTrackingEnabled) attr("checked", "true")
                                if (displayLoading) disabled()
                                onInput {
                                    onCommit(preferences.copy(locationTrackingEnabled = !preferences.locationTrackingEnabled))
                                }
                            })
                            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                                Text(LocalStrings.current.currencyOptional)
                            }
                        }
                    }
                }
                PreferenceSummaryCard(
                    title = LocalStrings.current.currency,
                    value = preferences.currencySymbol,
                    body = LocalStrings.current.currencyBody,
                ) {
                    ChoiceField(
                        label = LocalStrings.current.currency,
                        displayLoading = displayLoading,
                        selected = preferences.currencySymbol,
                        options = listOf("€", "$", "£"),
                        onSelection = { onCommit(preferences.copy(currencySymbol = it)) },
                    )
                }
                PreferenceSummaryCard(
                    title = LocalStrings.current.packPrice,
                    value = preferences.currencySymbol + preferences.packPrice.asDecimalString(),
                    body = LocalStrings.current.packPriceBody,
                ) {
                    NumberField(
                        label = LocalStrings.current.packPrice,
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
                    title = LocalStrings.current.cigarettesPerPack,
                    value = preferences.cigarettesPerPack.toString(),
                    body = LocalStrings.current.cigarettesPerPackBody,
                ) {
                    NumberField(
                        label = LocalStrings.current.cigarettesPerPack,
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
                Text(LocalStrings.current.changesSavedAutomatically)
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
    val strings = LocalStrings.current
    var draft by remember { mutableStateOf("") }

    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(LocalStrings.current.builtIn) }
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
                    text = LocalStrings.current.restoreRemovedDefaults(preferences.hiddenDefaultTriggers.size),
                    enabled = !displayLoading,
                    onClick = { onChange(preferences.copy(hiddenDefaultTriggers = emptySet())) },
                )
            }

            if (preferences.customTriggers.isNotEmpty()) {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(LocalStrings.current.yourTags) }
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
                        attr("placeholder", strings.addATagPlaceholder)
                        style {
                            property("flex", "1")
                            property("box-sizing", "border-box")
                            property("padding", "8px")
                        }
                    },
                )
                PrimaryButton(
                    text = LocalStrings.current.add,
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
    val strings = LocalStrings.current
    var open by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val results = remember(query) { searchEmojis(query) }

    Div(attrs = { attr("style", "position:relative;") }) {
        Button(
            attrs = {
                if (!enabled) disabled()
                onClick { open = !open }
                attr("aria-label", strings.pickTriggerIcon)
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
                        "display:flex;flex-direction:column;gap:8px;width:300px;",
                )
            }) {
                Input(
                    type = InputType.Text,
                    attrs = {
                        value(query)
                        onInput { query = it.value }
                        attr("placeholder", strings.searchEmoji)
                        attr("autofocus", "true")
                        style {
                            property("box-sizing", "border-box")
                            property("width", "100%")
                            property("padding", "8px 10px")
                            property("background", "var(--sa-color-surface-strong)")
                            property("color", "var(--sa-color-onSurface)")
                            property("border", "1px solid var(--sa-color-outline)")
                            property("border-radius", "8px")
                        }
                    },
                )
                Div(attrs = {
                    attr("style", "display:flex;flex-wrap:wrap;gap:4px;max-height:220px;overflow-y:auto;")
                }) {
                    results.forEach { entry ->
                        Button(
                            attrs = {
                                attr("title", entry.keywords.firstOrNull().orEmpty())
                                onClick {
                                    onCommit(entry.emoji)
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
                        ) { Text(entry.emoji) }
                    }
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
                ) { Text(LocalStrings.current.reset) }
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
    val strings = LocalStrings.current
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
                    attr("placeholder", strings.leaveEmptyToRestore)
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
                text = LocalStrings.current.save,
                enabled = enabled,
                onClick = {
                    onRename(draft.trim())
                    renaming = false
                },
            )
            GhostButton(text = LocalStrings.current.cancel, onClick = { renaming = false })
        } else {
            Span(attrs = { attr("style", "flex:1;") }) { Text(label) }
            GhostButton(text = LocalStrings.current.rename, enabled = enabled, onClick = { renaming = true })
            GhostButton(text = LocalStrings.current.remove, enabled = enabled, onClick = onRemove)
        }
    }
}

/** Renames a trigger without touching the key stored on smokes (blank clears the override). */
private fun UserPreferences.withTriggerLabel(key: String, label: String): UserPreferences =
    copy(
        triggerLabels = if (label.isBlank()) triggerLabels - key else triggerLabels + (key to label),
    )

/** Nickname + personal reason inputs; commit on change (blur/enter). */
@Composable
private fun PersonalizationPanelWeb(
    preferences: UserPreferences,
    displayLoading: Boolean,
    onChange: (UserPreferences) -> Unit,
) {
    val strings = LocalStrings.current
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            PersonalizationFieldWeb(
                label = strings.nicknameLabel,
                placeholder = strings.nicknamePlaceholder,
                value = preferences.nickname,
                enabled = !displayLoading,
                onCommit = { onChange(preferences.copy(nickname = it)) },
            )
            PersonalizationFieldWeb(
                label = strings.quitReasonLabel,
                placeholder = strings.quitReasonPlaceholder,
                value = preferences.quitReason,
                enabled = !displayLoading,
                onCommit = { onChange(preferences.copy(quitReason = it)) },
            )
        }
    }
}

@Composable
private fun PersonalizationFieldWeb(
    label: String,
    placeholder: String,
    value: String,
    enabled: Boolean,
    onCommit: (String) -> Unit,
) {
    var draft by remember(value) { mutableStateOf(value) }
    Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:6px;") }) {
        Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(label) }
        Input(
            type = InputType.Text,
            attrs = {
                value(draft)
                if (!enabled) disabled()
                onInput { draft = it.value }
                onChange { if (draft.trim() != value) onCommit(draft.trim()) }
                attr("placeholder", placeholder)
                style {
                    property("box-sizing", "border-box")
                    property("padding", "8px 10px")
                    property("background", "var(--sa-color-surface-strong)")
                    property("color", "var(--sa-color-onSurface)")
                    property("border", "1px solid var(--sa-color-outline)")
                    property("border-radius", "8px")
                }
            },
        )
    }
}
