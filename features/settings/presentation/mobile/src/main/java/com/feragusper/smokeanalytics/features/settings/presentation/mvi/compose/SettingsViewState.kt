package com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationOff
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.feragusper.smokeanalytics.features.settings.presentation.AboutSection
import com.feragusper.smokeanalytics.features.settings.presentation.R
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.GoogleSignInComponent
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.preferences.domain.AccountTier
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.TriggerEmojiPalette
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.normalizedTag
import com.valentinilk.shimmer.shimmer

data class SettingsViewState(
    internal val displayLoading: Boolean = false,
    internal val currentEmail: String? = null,
    internal val currentDisplayName: String? = null,
    internal val preferences: UserPreferences = UserPreferences(),
    internal val infoMessage: String? = null,
    internal val errorMessage: String? = null,
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
        var signInErrorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(preferences, currentEmail) {
            draftPreferences = preferences
            if (currentEmail != null) {
                signInErrorMessage = null
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (displayLoading && currentEmail == null && errorMessage == null) {
                SettingsShimmerContent()
                return@Column
            }

            SettingsScreenHeader(
                displayLoading = displayLoading,
                currentEmail = currentEmail,
                currentDisplayName = currentDisplayName,
            )

            errorMessage?.let { message ->
                SettingsErrorCard(
                    message = message,
                    onRetry = { intent(SettingsIntent.FetchUser) },
                )
            }

            if (errorMessage != null && currentEmail == null) {
                return@Column
            }

            SettingsSectionHeader(title = "Account")

            SessionCard(
                currentEmail = currentEmail,
                currentDisplayName = currentDisplayName,
                displayLoading = displayLoading,
                signInErrorMessage = signInErrorMessage,
                onSignOut = { intent(SettingsIntent.SignOut) },
                onSignInSuccess = { intent(SettingsIntent.FetchUser) },
                onSignInError = { signInErrorMessage = it },
            )

            SettingsSectionHeader(title = "Preferences")

            PreferencesCard(
                preferences = draftPreferences,
                enabled = !displayLoading && currentEmail != null,
                onPreferencesChange = { draftPreferences = it },
                onSave = { updated -> intent(SettingsIntent.UpdatePreferences(updated)) },
                onReset = { draftPreferences = preferences },
            )

            if (currentEmail != null) {
                SettingsSectionHeader(title = "Triggers")

                SettingsCard(
                    title = "Manage triggers",
                    subtitle = "Choose which built-in triggers appear when you tag a cigarette, and add your own.",
                ) {
                    ManageTriggersSection(
                        preferences = draftPreferences,
                        enabled = !displayLoading,
                        onChange = { updated ->
                            draftPreferences = updated
                            intent(SettingsIntent.UpdatePreferences(updated))
                        },
                    )
                }
            }

            SettingsSectionHeader(title = "App")

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
private fun SettingsErrorCard(
    message: String,
    onRetry: (() -> Unit)? = null,
    title: String = "Your space is unavailable",
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (onRetry != null) {
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    subtitle: String = "",
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SettingsScreenHeader(
    displayLoading: Boolean,
    currentEmail: String?,
    currentDisplayName: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = if (currentDisplayName.isNullOrBlank()) "You" else currentDisplayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (currentEmail == null) {
                "Sign in to sync preferences and keep goals across devices."
            } else {
                "Manage session, preferences, and app settings."
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
    signInErrorMessage: String?,
    onSignOut: () -> Unit,
    onSignInSuccess: () -> Unit,
    onSignInError: (String) -> Unit,
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
                    text = "Guest mode keeps the shell readable, but sign-in restores synced preferences, a stable archive, and goals across devices.",
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
                    title = "Goals",
                    value = "Targets",
                    body = "Keep reduction targets and product preferences connected to the same account.",
                )

                signInErrorMessage?.let { message ->
                    SettingsErrorCard(
                        title = "Sign-in failed",
                        message = message,
                    )
                }

                GoogleSignInComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SettingsViewState.TestTags.BUTTON_SIGN_IN),
                    onSignInSuccess = onSignInSuccess,
                    onSignInError = onSignInError,
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
    onSave: (UserPreferences) -> Unit,
    onReset: () -> Unit,
) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        val hasPermission = granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val updated = preferences.copy(locationTrackingEnabled = hasPermission)
        onPreferencesChange(updated)
        onSave(updated)
    }

    var showDayStartPicker by remember { mutableStateOf(false) }
    var showBedtimePicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showPackPricePicker by remember { mutableStateOf(false) }
    var showCigsPerPackPicker by remember { mutableStateOf(false) }

    SettingsCard(title = "Preferences") {
        // Row 1: Day starts + Sleep starts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = "Day starts",
                value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
                body = "Tap to change",
                enabled = enabled,
                onClick = { showDayStartPicker = true },
            )
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = "Sleep starts",
                value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                body = "Tap to change",
                enabled = enabled,
                onClick = { showBedtimePicker = true },
            )
        }

        // Row 2: Currency + Pack price + Cigs per pack
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = "Currency",
                value = preferences.currencySymbol,
                body = "Tap to change",
                enabled = enabled,
                onClick = { showCurrencyPicker = true },
            )
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = "Pack price",
                value = "%.2f".format(preferences.packPrice),
                body = "Tap to change",
                enabled = enabled,
                onClick = { showPackPricePicker = true },
            )
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = "Cigs/pack",
                value = preferences.cigarettesPerPack.toString(),
                body = "Tap to change",
                enabled = enabled,
                onClick = { showCigsPerPackPicker = true },
            )
        }

        // Location toggle card
        LocationPreferenceCard(
            enabled = enabled,
            isTracking = preferences.locationTrackingEnabled,
            onToggle = { checked ->
                if (!checked) {
                    val updated = preferences.copy(locationTrackingEnabled = false)
                    onPreferencesChange(updated); onSave(updated)
                } else if (context.hasLocationPermission()) {
                    val updated = preferences.copy(locationTrackingEnabled = true)
                    onPreferencesChange(updated); onSave(updated)
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                    )
                }
            },
        )
    }

    // Day start time picker
    if (showDayStartPicker) {
        TimePickerDialogCompat(
            label = "Day starts at",
            initialHour = preferences.dayStartHour,
            onDismiss = { showDayStartPicker = false },
            onConfirm = { hour ->
                showDayStartPicker = false
                val updated = preferences.copy(dayStartHour = hour)
                onPreferencesChange(updated); onSave(updated)
            },
        )
    }

    // Bedtime picker
    if (showBedtimePicker) {
        TimePickerDialogCompat(
            label = "Sleep starts at",
            initialHour = preferences.bedtimeHour,
            onDismiss = { showBedtimePicker = false },
            onConfirm = { hour ->
                showBedtimePicker = false
                val updated = preferences.copy(bedtimeHour = hour)
                onPreferencesChange(updated); onSave(updated)
            },
        )
    }

    // Currency picker
    if (showCurrencyPicker) {
        AlertDialog(
            onDismissRequest = { showCurrencyPicker = false },
            title = { Text("Currency") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("€", "$", "£").forEach { symbol ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showCurrencyPicker = false
                                    val updated = preferences.copy(currencySymbol = symbol)
                                    onPreferencesChange(updated); onSave(updated)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = symbol,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (symbol == preferences.currencySymbol)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = when (symbol) { "€" -> "Euro"; "$" -> "Dollar"; else -> "Pound" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCurrencyPicker = false }) { Text("Cancel") } },
        )
    }

    // Pack price picker
    if (showPackPricePicker) {
        var draftPrice by remember { mutableStateOf("%.2f".format(preferences.packPrice)) }
        AlertDialog(
            onDismissRequest = { showPackPricePicker = false },
            title = { Text("Pack price (${preferences.currencySymbol})") },
            text = {
                OutlinedTextField(
                    value = draftPrice,
                    onValueChange = { draftPrice = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = {
                    draftPrice.toDoubleOrNull()?.coerceAtLeast(0.0)?.let { price ->
                        showPackPricePicker = false
                        val updated = preferences.copy(packPrice = price)
                        onPreferencesChange(updated); onSave(updated)
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showPackPricePicker = false }) { Text("Cancel") } },
        )
    }

    // Cigarettes per pack picker
    if (showCigsPerPackPicker) {
        var draftCigs by remember { mutableStateOf(preferences.cigarettesPerPack.toString()) }
        AlertDialog(
            onDismissRequest = { showCigsPerPackPicker = false },
            title = { Text("Cigarettes per pack") },
            text = {
                OutlinedTextField(
                    value = draftCigs,
                    onValueChange = { draftCigs = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = {
                    draftCigs.toIntOrNull()?.coerceAtLeast(1)?.let { count ->
                        showCigsPerPackPicker = false
                        val updated = preferences.copy(cigarettesPerPack = count)
                        onPreferencesChange(updated); onSave(updated)
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showCigsPerPackPicker = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun TappableHighlightCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    body: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun LocationPreferenceCard(
    enabled: Boolean,
    isTracking: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isTracking) Icons.Filled.LocationOn else Icons.Filled.LocationOff,
                contentDescription = null,
                tint = if (isTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Location tracking",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (isTracking) "On — used for map insights" else "Off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Switch(
                checked = isTracking,
                onCheckedChange = onToggle,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun TimePickerDialogCompat(
    label: String,
    initialHour: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val context = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        TimePickerDialog(
            context,
            { _, hour, _ -> onConfirm(hour) },
            initialHour,
            0,
            true,
        ).apply {
            setOnDismissListener { onDismiss() }
        }.show()
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
private fun stringResourceSafe(id: Int, fallback: String): String =
    if (LocalInspectionMode.current) fallback else androidx.compose.ui.res.stringResource(id)

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

@Composable
private fun ManageTriggersSection(
    preferences: UserPreferences,
    enabled: Boolean,
    onChange: (UserPreferences) -> Unit,
) {
    var draft by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Built-in",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SmokeTrigger.defaultOptions().forEach { option ->
            val visible = option.key !in preferences.hiddenDefaultTriggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TriggerIconPicker(
                    icon = preferences.triggerIcons[option.key] ?: option.icon.orEmpty(),
                    enabled = enabled,
                    onCommit = { icon -> onChange(preferences.withTriggerIcon(option.key, icon)) },
                )
                Text(text = option.label, modifier = Modifier.weight(1f))
                Switch(
                    checked = visible,
                    enabled = enabled,
                    onCheckedChange = { checked ->
                        val hidden = if (checked) {
                            preferences.hiddenDefaultTriggers - option.key
                        } else {
                            preferences.hiddenDefaultTriggers + option.key
                        }
                        onChange(preferences.copy(hiddenDefaultTriggers = hidden))
                    },
                )
            }
        }

        if (preferences.customTriggers.isNotEmpty()) {
            Text(
                text = "Your tags",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            preferences.customTriggers.forEach { tag ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TriggerIconPicker(
                        icon = preferences.triggerIcons[tag].orEmpty(),
                        enabled = enabled,
                        onCommit = { icon -> onChange(preferences.withTriggerIcon(tag, icon)) },
                    )
                    Text(text = tag, modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { onChange(preferences.copy(customTriggers = preferences.customTriggers - tag)) },
                        enabled = enabled,
                    ) { Text("Remove") }
                }
            }
        }

        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Add a tag") },
            singleLine = true,
            enabled = enabled,
            trailingIcon = {
                draft.normalizedTag()?.let { key ->
                    TextButton(onClick = {
                        if (preferences.customTriggers.none { it.equals(key, ignoreCase = true) }) {
                            onChange(preferences.copy(customTriggers = preferences.customTriggers + key))
                        }
                        draft = ""
                    }) { Text("Add") }
                }
            },
        )
    }
}

/** Per-row emoji picker for a trigger: tap the icon, choose from a curated grid. */
@Composable
private fun TriggerIconPicker(
    icon: String,
    enabled: Boolean,
    onCommit: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    TextButton(onClick = { open = true }, enabled = enabled) {
        Text(
            text = icon.ifBlank { "＋" },
            style = MaterialTheme.typography.titleLarge,
        )
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("Pick an icon") },
            text = {
                FlowRow(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TriggerEmojiPalette.forEach { emoji ->
                        Text(
                            text = emoji,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onCommit(emoji)
                                    open = false
                                }
                                .padding(10.dp),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            },
            confirmButton = {
                // Clears the override: built-ins fall back to their default icon.
                TextButton(onClick = {
                    onCommit("")
                    open = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Cancel") }
            },
        )
    }
}

/** Sets/clears the emoji for a trigger key (blank clears the override). */
private fun UserPreferences.withTriggerIcon(key: String, icon: String): UserPreferences =
    copy(
        triggerIcons = if (icon.isBlank()) triggerIcons - key else triggerIcons + (key to icon),
    )
