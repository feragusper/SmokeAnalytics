package com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.feragusper.smokeanalytics.features.settings.presentation.AboutSection
import com.feragusper.smokeanalytics.features.settings.presentation.GoalsEditorScreen
import com.feragusper.smokeanalytics.features.settings.presentation.R
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.preferences.domain.AccountTier
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.valentinilk.shimmer.shimmer

data class SettingsViewState(
    internal val displayLoading: Boolean = false,
    internal val currentEmail: String? = null,
    internal val currentDisplayName: String? = null,
    internal val preferences: UserPreferences = UserPreferences(),
    internal val goalProgress: GoalProgress? = null,
    internal val infoMessage: String? = null,
) : MVIViewState<SettingsIntent> {

    interface TestTags {
        companion object {
            const val BUTTON_SIGN_OUT = "buttonSignOut"
            const val BUTTON_SIGN_IN = "buttonSignIn"
            const val VIEW_PROGRESS = "viewProgress"
        }
    }

    @Composable
    fun Compose(
        intent: (SettingsIntent) -> Unit,
    ) {
        var draftPreferences by remember(currentEmail, preferences) { mutableStateOf(preferences) }
        var showingGoals by remember(currentEmail, preferences.activeGoal) { mutableStateOf(false) }

        LaunchedEffect(preferences, currentEmail) {
            draftPreferences = preferences
        }

        if (showingGoals) {
            GoalsEditorScreen(
                currentEmail = currentEmail,
                preferences = draftPreferences,
                goalProgress = goalProgress,
                displayLoading = displayLoading,
                onBack = { showingGoals = false },
                onSaveGoal = { goal ->
                    draftPreferences = draftPreferences.copy(activeGoal = goal)
                    intent(SettingsIntent.UpdatePreferences(draftPreferences.copy(activeGoal = goal)))
                },
                onClearGoal = {
                    draftPreferences = draftPreferences.copy(activeGoal = null)
                    intent(SettingsIntent.UpdatePreferences(draftPreferences.copy(activeGoal = null)))
                },
                onSignInSuccess = { intent(SettingsIntent.FetchUser) },
            )
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (displayLoading && currentEmail == null) {
                SettingsShimmerContent()
                return@Column
            }

            SettingsHeroCard(
                displayLoading = displayLoading,
                currentEmail = currentEmail,
                currentDisplayName = currentDisplayName,
            )

            GoalsEntryCard(
                goalProgress = goalProgress,
                activeGoal = preferences.activeGoal,
                onOpenGoals = { showingGoals = true },
            )

            SessionCard(
                currentEmail = currentEmail,
                currentDisplayName = currentDisplayName,
                displayLoading = displayLoading,
                onSignOut = { intent(SettingsIntent.SignOut) },
                onSignInSuccess = { intent(SettingsIntent.FetchUser) },
            )

            HighlightsRow(tier = preferences.accountTier)

            RoutineSnapshotCard(preferences = preferences)

            PreferencesCard(
                preferences = draftPreferences,
                enabled = !displayLoading && currentEmail != null,
                onPreferencesChange = { draftPreferences = it },
                onSave = { intent(SettingsIntent.UpdatePreferences(draftPreferences)) },
                onReset = { draftPreferences = preferences },
            )

            SettingsCard(
                title = "About & Support",
                subtitle = "Share the app, reach support, and review plan metadata from your personal destination.",
            ) {
                AboutSection()
            }

            infoMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun SettingsHeroCard(
    displayLoading: Boolean,
    currentEmail: String?,
    currentDisplayName: String?,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "You",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (currentDisplayName.isNullOrBlank()) {
                    "Keep your routine, goals, and account in sync."
                } else {
                    "Keep $currentDisplayName's routine, goals, and account in sync."
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (currentEmail == null) {
                    "Sign in to sync preferences, preserve progress, and keep goals ready across devices."
                } else {
                    "Review session state, tune how the app interprets your day, and keep the next goals flow anchored here."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatusBadge(
                text = when {
                    displayLoading -> "Refreshing"
                    currentEmail != null -> "Signed in"
                    else -> "Guest mode"
                },
            )
        }
    }
}

@Composable
private fun GoalsEntryCard(
    goalProgress: GoalProgress?,
    activeGoal: com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal?,
    onOpenGoals: () -> Unit,
) {
    SettingsCard(
        title = "Goals",
        subtitle = "This is the stable entry point for your personal targets.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = goalProgress?.supportingText
                    ?: "Daily caps, reduction plans, and mindful-gap targets live here as your personal next step.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatusBadge(text = goalProgress?.status?.name ?: if (activeGoal == null) "No goal yet" else "Ready")
            Text(
                text = goalProgress?.targetLabel
                    ?: "Open Goals to create one active target without turning it into a new bottom-bar destination.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onOpenGoals) {
                Text(if (activeGoal == null) "Set up goals" else "Review goals")
            }
        }
    }
}

@Composable
private fun HighlightsRow(
    tier: AccountTier,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HighlightCard(
            modifier = Modifier.weight(1f),
            title = "Plan",
            value = tier.name,
            body = "Premium stays defined as a future upgrade with richer insights and no ads.",
        )
        HighlightCard(
            modifier = Modifier.weight(1f),
            title = "Points",
            value = "Recovery",
            body = "Progress is tied to smoke-free gaps, not perfection. Longer gaps keep the score moving.",
        )
    }
}

@Composable
private fun HighlightCard(
    modifier: Modifier,
    title: String,
    value: String,
    body: String,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RoutineSnapshotCard(
    preferences: UserPreferences,
) {
    SettingsCard(
        title = "Routine model",
        subtitle = "The app's daily interpretation depends on these values before any chart or map is computed.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = "Day starts",
                    value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
                    body = "Custom day boundary for Home, History, and Analytics.",
                )
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = "Sleep starts",
                    value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                    body = "Sleep hours are excluded from the mindful gap target and hourly averages.",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = "Awake window",
                    value = "${preferences.awakeMinutesPerDay / 60}h",
                    body = "This is the daily window used to calculate a healthier target pace.",
                )
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = "Location",
                    value = if (preferences.locationTrackingEnabled) "On" else "Off",
                    body = "Controls whether the map can learn from repeated smoking areas.",
                )
            }
        }
    }
}

@Composable
private fun SessionCard(
    currentEmail: String?,
    currentDisplayName: String?,
    displayLoading: Boolean,
    onSignOut: () -> Unit,
    onSignInSuccess: () -> Unit,
) {
    SettingsCard(
        title = "Session",
        subtitle = "Authentication and sync state.",
    ) {
        if (currentEmail != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = sessionInitials(currentDisplayName, currentEmail),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    currentDisplayName?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = currentEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SettingsViewState.TestTags.BUTTON_SIGN_OUT),
                onClick = onSignOut,
                enabled = !displayLoading,
            ) {
                Text(text = stringResourceSafe(R.string.settings_logout, "Logout"))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Guest mode keeps the shell readable, but sign-in restores synced preferences, a stable archive, and the right context for coach insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SessionBenefitCard(
                        modifier = Modifier.weight(1f),
                        title = "Routine",
                        value = "Sync",
                        body = "Carry pack price, day-start hour, and location settings across devices.",
                    )
                    SessionBenefitCard(
                        modifier = Modifier.weight(1f),
                        title = "History",
                        value = "Archive",
                        body = "Keep edits and older smoke entries tied to the same account.",
                    )
                }

                SessionBenefitCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Coach",
                    value = "Context",
                    body = "Give the guide enough recent behavior to stay grounded instead of generic.",
                )

                GoogleSignInComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SettingsViewState.TestTags.BUTTON_SIGN_IN),
                    onSignInSuccess = onSignInSuccess,
                    onSignInError = {},
                )
            }
        }
    }
}

@Composable
private fun SessionBenefitCard(
    modifier: Modifier,
    title: String,
    value: String,
    body: String,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PreferencesCard(
    preferences: UserPreferences,
    enabled: Boolean,
    onPreferencesChange: (UserPreferences) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        val hasPermission = granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onPreferencesChange(preferences.copy(locationTrackingEnabled = hasPermission))
    }

    SettingsCard(
        title = "Preferences",
        subtitle = "Values that shape cost calculations, day buckets, and map behavior.",
    ) {
        CurrencyField(
            selected = preferences.currencySymbol,
            enabled = enabled,
            onCurrencySelected = { onPreferencesChange(preferences.copy(currencySymbol = it)) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        PriceField(
            label = "Pack price",
            currencySymbol = preferences.currencySymbol,
            value = preferences.packPrice,
            enabled = enabled,
            onValueChange = { onPreferencesChange(preferences.copy(packPrice = it.coerceAtLeast(0.0))) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        IntegerField(
            label = "Cigarettes per pack",
            value = preferences.cigarettesPerPack,
            enabled = enabled,
            minValue = 1,
            step = 1,
            onValueChange = { onPreferencesChange(preferences.copy(cigarettesPerPack = it)) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        TimePreferenceRow(
            label = "First hour of the day",
            hour = preferences.dayStartHour,
            enabled = enabled,
            onTimeSelected = { hour ->
                onPreferencesChange(preferences.copy(dayStartHour = hour))
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        TimePreferenceRow(
            label = "Bedtime",
            hour = preferences.bedtimeHour,
            enabled = enabled,
            onTimeSelected = { hour ->
                onPreferencesChange(preferences.copy(bedtimeHour = hour))
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Track location with smokes", style = MaterialTheme.typography.bodySmall)
            Switch(
                checked = preferences.locationTrackingEnabled,
                onCheckedChange = { checked ->
                    if (!checked) {
                        onPreferencesChange(preferences.copy(locationTrackingEnabled = false))
                    } else if (context.hasLocationPermission()) {
                        onPreferencesChange(preferences.copy(locationTrackingEnabled = true))
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                        )
                    }
                },
                enabled = enabled,
            )
            Text(
                "Optional. Used for map insights.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onReset,
                enabled = enabled,
            ) {
                Text("Reset")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onSave,
                enabled = enabled,
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun CurrencyField(
    selected: String,
    enabled: Boolean,
    onCurrencySelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Currency", style = MaterialTheme.typography.bodySmall)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
            ) {
                Text(selected)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                listOf("€", "$", "£").forEach { symbol ->
                    DropdownMenuItem(
                        text = { Text(symbol) },
                        onClick = {
                            expanded = false
                            onCurrencySelected(symbol)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceField(
    label: String,
    currencySymbol: String,
    value: Double,
    enabled: Boolean,
    onValueChange: (Double) -> Unit,
) {
    var text by remember(value) { mutableStateOf("%.2f".format(value)) }
    LaunchedEffect(value) { text = "%.2f".format(value) }

    FieldWithStepper(
        label = label,
        enabled = enabled,
        field = {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    it.toDoubleOrNull()?.let { parsed -> onValueChange(parsed.coerceAtLeast(0.0)) }
                },
                enabled = enabled,
                prefix = { Text(currencySymbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        },
        onDecrease = { onValueChange((value - 0.5).coerceAtLeast(0.0)) },
        onIncrease = { onValueChange(value + 0.5) },
    )
}

@Composable
private fun IntegerField(
    label: String,
    value: Int,
    enabled: Boolean,
    minValue: Int,
    step: Int,
    onValueChange: (Int) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    LaunchedEffect(value) { text = value.toString() }

    FieldWithStepper(
        label = label,
        enabled = enabled,
        field = {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    it.toIntOrNull()?.let { parsed -> onValueChange(parsed.coerceAtLeast(minValue)) }
                },
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        },
        onDecrease = { onValueChange((value - step).coerceAtLeast(minValue)) },
        onIncrease = { onValueChange(value + step) },
    )
}

@Composable
private fun FieldWithStepper(
    label: String,
    enabled: Boolean,
    field: @Composable () -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        field()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDecrease, enabled = enabled) { Text("−") }
            OutlinedButton(onClick = onIncrease, enabled = enabled) { Text("+") }
        }
    }
}

@Composable
private fun TimePreferenceRow(
    label: String,
    hour: Int,
    enabled: Boolean,
    onTimeSelected: (Int) -> Unit,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "${hour.toString().padStart(2, '0')}:00",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedButton(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, selectedHour, _ -> onTimeSelected(selectedHour) },
                    hour,
                    0,
                    true,
                ).show()
            },
            enabled = enabled,
        ) {
            Text("Change")
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            content()
        }
    }
}

@Composable
private fun SettingsShimmerContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(3) {
            Column(
                modifier = Modifier
                    .testTag(SettingsViewState.TestTags.VIEW_PROGRESS)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .shimmer()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private fun sessionInitials(name: String?, email: String?): String {
    val source = name?.takeIf { it.isNotBlank() } ?: email.orEmpty()
    return source
        .split(" ", ".", "@")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "SA" }
}

@Composable
private fun stringResourceSafe(id: Int, fallback: String): String = runCatching {
    androidx.compose.ui.res.stringResource(id)
}.getOrDefault(fallback)

private fun Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}

@CombinedPreviews
@Composable
private fun SettingsPreview() {
    SmokeAnalyticsTheme {
        SettingsViewState(
            currentEmail = "fer@gmail.com",
            currentDisplayName = "Fernando Perez",
        ).Compose(
            intent = {},
        )
    }
}
