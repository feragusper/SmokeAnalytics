package com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.feragusper.smokeanalytics.libraries.design.compose.theme.AccentHolder
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsScreen
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTarget
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import org.koin.compose.koinInject
import com.feragusper.smokeanalytics.libraries.design.compose.theme.MobileAccent
import com.feragusper.smokeanalytics.libraries.preferences.domain.AccountTier
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.searchEmojis
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
        val analytics = koinInject<AnalyticsTracker>()

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

            SettingsSectionHeader(title = stringResource(R.string.settings_account))

            SessionCard(
                currentEmail = currentEmail,
                currentDisplayName = currentDisplayName,
                displayLoading = displayLoading,
                signInErrorMessage = signInErrorMessage,
                onSignOut = {
                    analytics.logout()
                    intent(SettingsIntent.SignOut)
                },
                onSignInSuccess = {
                    analytics.login()
                    intent(SettingsIntent.FetchUser)
                },
                onSignInError = { signInErrorMessage = it },
            )

            SettingsSectionHeader(title = stringResource(R.string.settings_preferences))

            PreferencesCard(
                preferences = draftPreferences,
                enabled = !displayLoading && currentEmail != null,
                onPreferencesChange = { draftPreferences = it },
                onSave = { updated -> intent(SettingsIntent.UpdatePreferences(updated)) },
                onReset = { draftPreferences = preferences },
            )

            if (currentEmail != null) {
                SettingsSectionHeader(title = stringResource(R.string.settings_personalization))

                SettingsCard(
                    title = stringResource(R.string.settings_make_it_yours),
                    subtitle = stringResource(R.string.settings_personalization_subtitle),
                    initiallyExpanded = false,
                ) {
                    PersonalizationSection(
                        preferences = draftPreferences,
                        enabled = !displayLoading,
                        onChange = { updated ->
                            draftPreferences = updated
                            intent(SettingsIntent.UpdatePreferences(updated))
                        },
                    )
                }
            }

            if (currentEmail != null) {
                SettingsSectionHeader(title = stringResource(R.string.settings_triggers))

                SettingsCard(
                    title = stringResource(R.string.settings_manage_triggers),
                    subtitle = stringResource(R.string.settings_manage_triggers_subtitle),
                    initiallyExpanded = false,
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

            SettingsSectionHeader(title = stringResource(R.string.settings_app))

            SettingsCard(
                title = stringResource(R.string.settings_about_support),
                subtitle = stringResource(R.string.settings_about_subtitle),
                initiallyExpanded = false,
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
    title: String = stringResource(R.string.settings_space_unavailable),
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
                    Text(stringResource(R.string.settings_retry))
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
            text = if (currentDisplayName.isNullOrBlank()) stringResource(R.string.settings_you) else currentDisplayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (currentEmail == null) {
                stringResource(R.string.settings_sign_in_subtitle)
            } else {
                stringResource(R.string.settings_manage_session_subtitle)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatusBadge(
            text = when {
                displayLoading -> stringResource(R.string.settings_refreshing)
                currentEmail != null -> stringResource(R.string.settings_signed_in)
                else -> stringResource(R.string.settings_guest_mode)
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
            title = stringResource(R.string.settings_plan),
            value = tier.name,
            body = stringResource(R.string.settings_plan_body),
        )
        HighlightCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.settings_points),
            value = stringResource(R.string.settings_recovery),
            body = stringResource(R.string.settings_points_body),
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
        title = stringResource(R.string.settings_routine_model),
        subtitle = stringResource(R.string.settings_routine_body),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.settings_day_starts),
                    value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
                    body = stringResource(R.string.settings_day_boundary_body),
                )
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.settings_sleep_starts),
                    value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                    body = stringResource(R.string.settings_sleep_body),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.settings_awake_window),
                    value = "${preferences.awakeMinutesPerDay / 60}h",
                    body = stringResource(R.string.settings_awake_body),
                )
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.settings_location),
                    value = if (preferences.locationTrackingEnabled) "On" else stringResource(R.string.settings_off),
                    body = stringResource(R.string.settings_location_body),
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
        title = stringResource(R.string.settings_session),
        subtitle = stringResource(R.string.settings_account_subtitle),
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
                Text(text = stringResourceSafe(R.string.settings_logout, stringResource(R.string.settings_logout)))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stringResource(R.string.settings_session_guest_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SessionBenefitCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.settings_routine),
                        value = stringResource(R.string.settings_sync),
                        body = stringResource(R.string.settings_preferences_card_body),
                    )
                    SessionBenefitCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.settings_history),
                        value = stringResource(R.string.settings_archive),
                        body = stringResource(R.string.settings_history_card_body),
                    )
                }

                SessionBenefitCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.settings_goals),
                    value = stringResource(R.string.settings_targets),
                    body = stringResource(R.string.settings_goals_card_body),
                )

                signInErrorMessage?.let { message ->
                    SettingsErrorCard(
                        title = stringResource(R.string.settings_sign_in_failed),
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
    val analytics = koinInject<AnalyticsTracker>()
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

    SettingsCard(title = stringResource(R.string.settings_preferences)) {
        // Row 1: Day starts + Sleep starts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_day_starts),
                value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
                body = stringResource(R.string.settings_tap_to_change),
                enabled = enabled,
                onClick = {
                    analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_DAY_START)
                    showDayStartPicker = true
                },
            )
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_sleep_starts),
                value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
                body = stringResource(R.string.settings_tap_to_change),
                enabled = enabled,
                onClick = {
                    analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_BEDTIME)
                    showBedtimePicker = true
                },
            )
        }

        // Row 2: Currency + Pack price + Cigs per pack
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_currency),
                value = preferences.currencySymbol,
                body = stringResource(R.string.settings_tap_to_change),
                enabled = enabled,
                onClick = {
                    analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_CURRENCY)
                    showCurrencyPicker = true
                },
            )
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_pack_price),
                value = "%.2f".format(preferences.packPrice),
                body = stringResource(R.string.settings_tap_to_change),
                enabled = enabled,
                onClick = {
                    analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_PACK_PRICE)
                    showPackPricePicker = true
                },
            )
            TappableHighlightCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_cigs_pack_short),
                value = preferences.cigarettesPerPack.toString(),
                body = stringResource(R.string.settings_tap_to_change),
                enabled = enabled,
                onClick = {
                    analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_CIGS_PER_PACK)
                    showCigsPerPackPicker = true
                },
            )
        }

        // Location toggle card
        LocationPreferenceCard(
            enabled = enabled,
            isTracking = preferences.locationTrackingEnabled,
            onToggle = { checked ->
                analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.TOGGLE_LOCATION)
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
            label = stringResource(R.string.settings_day_starts_at),
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
            label = stringResource(R.string.settings_sleep_starts_at),
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
            title = { Text(stringResource(R.string.settings_currency)) },
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
                                text = when (symbol) { "€" -> stringResource(R.string.settings_currency_euro); "$" -> stringResource(R.string.settings_currency_dollar); else -> stringResource(R.string.settings_currency_pound) },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCurrencyPicker = false }) { Text(stringResource(R.string.settings_cancel)) } },
        )
    }

    // Pack price picker
    if (showPackPricePicker) {
        var draftPrice by remember { mutableStateOf("%.2f".format(preferences.packPrice)) }
        AlertDialog(
            onDismissRequest = { showPackPricePicker = false },
            title = { Text(stringResource(R.string.settings_pack_price_with_symbol, preferences.currencySymbol)) },
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
                }) { Text(stringResource(R.string.settings_save)) }
            },
            dismissButton = { TextButton(onClick = { showPackPricePicker = false }) { Text(stringResource(R.string.settings_cancel)) } },
        )
    }

    // Cigarettes per pack picker
    if (showCigsPerPackPicker) {
        var draftCigs by remember { mutableStateOf(preferences.cigarettesPerPack.toString()) }
        AlertDialog(
            onDismissRequest = { showCigsPerPackPicker = false },
            title = { Text(stringResource(R.string.settings_cigs_per_pack)) },
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
                }) { Text(stringResource(R.string.settings_save)) }
            },
            dismissButton = { TextButton(onClick = { showCigsPerPackPicker = false }) { Text(stringResource(R.string.settings_cancel)) } },
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
                    text = stringResource(R.string.settings_location_tracking),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (isTracking) stringResource(R.string.settings_location_on) else stringResource(R.string.settings_off),
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
        Text(text = stringResource(R.string.settings_currency), style = MaterialTheme.typography.bodySmall)
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
            Text(stringResource(R.string.settings_change))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String? = null,
    initiallyExpanded: Boolean = true,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "settingsCardChevron",
    )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) stringResource(R.string.settings_collapse) else stringResource(R.string.settings_expand),
                    modifier = Modifier.rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    content()
                }
            }
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
            text = stringResource(R.string.settings_builtin),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SmokeTrigger.defaultOptions()
            .filter { it.key !in preferences.hiddenDefaultTriggers }
            .forEach { option ->
                TriggerRow(
                    icon = preferences.triggerIcons[option.key] ?: option.icon.orEmpty(),
                    label = preferences.triggerLabels[option.key] ?: option.label,
                    enabled = enabled,
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
            TextButton(
                onClick = { onChange(preferences.copy(hiddenDefaultTriggers = emptySet())) },
                enabled = enabled,
            ) { Text(stringResource(R.string.settings_restore_removed_defaults, preferences.hiddenDefaultTriggers.size)) }
        }

        if (preferences.customTriggers.isNotEmpty()) {
            Text(
                text = stringResource(R.string.settings_your_tags),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            preferences.customTriggers.forEach { tag ->
                TriggerRow(
                    icon = preferences.triggerIcons[tag].orEmpty(),
                    label = preferences.triggerLabels[tag] ?: tag,
                    enabled = enabled,
                    onIconCommit = { icon -> onChange(preferences.withTriggerIcon(tag, icon)) },
                    onRename = { name -> onChange(preferences.withTriggerLabel(tag, name)) },
                    onRemove = { onChange(preferences.copy(customTriggers = preferences.customTriggers - tag)) },
                )
            }
        }

        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.settings_add_a_tag)) },
            singleLine = true,
            enabled = enabled,
            trailingIcon = {
                draft.normalizedTag()?.let { key ->
                    TextButton(onClick = {
                        if (preferences.customTriggers.none { it.equals(key, ignoreCase = true) }) {
                            onChange(preferences.copy(customTriggers = preferences.customTriggers + key))
                        }
                        draft = ""
                    }) { Text(stringResource(R.string.settings_add)) }
                }
            },
        )
    }
}

/** One trigger row: icon picker, (renamable) label, rename and remove actions. */
@Composable
private fun TriggerRow(
    icon: String,
    label: String,
    enabled: Boolean,
    onIconCommit: (String) -> Unit,
    onRename: (String) -> Unit,
    onRemove: () -> Unit,
) {
    var renaming by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TriggerIconPicker(icon = icon, enabled = enabled, onCommit = onIconCommit)
        Text(text = label, modifier = Modifier.weight(1f))
        TextButton(onClick = { renaming = true }, enabled = enabled) { Text(stringResource(R.string.settings_rename)) }
        TextButton(onClick = onRemove, enabled = enabled) { Text(stringResource(R.string.settings_remove)) }
    }
    if (renaming) {
        var draft by remember { mutableStateOf(label) }
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text(stringResource(R.string.settings_rename_trigger)) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.settings_name)) },
                    supportingText = { Text(stringResource(R.string.settings_leave_empty_restore)) },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRename(draft.trim())
                    renaming = false
                }) { Text(stringResource(R.string.settings_save)) }
            },
            dismissButton = {
                TextButton(onClick = { renaming = false }) { Text(stringResource(R.string.settings_cancel)) }
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
        var query by remember { mutableStateOf("") }
        val results = remember(query) { searchEmojis(query) }
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(stringResource(R.string.settings_pick_an_icon)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_search)) },
                        singleLine = true,
                    )
                    FlowRow(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        results.forEach { entry ->
                            Text(
                                text = entry.emoji,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        onCommit(entry.emoji)
                                        open = false
                                    }
                                    .padding(10.dp),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                // Clears the override: built-ins fall back to their default icon.
                TextButton(onClick = {
                    onCommit("")
                    open = false
                }) { Text(stringResource(R.string.settings_reset)) }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text(stringResource(R.string.settings_cancel)) }
            },
        )
    }
}

/** Sets/clears the emoji for a trigger key (blank clears the override). */
private fun UserPreferences.withTriggerIcon(key: String, icon: String): UserPreferences =
    copy(
        triggerIcons = if (icon.isBlank()) triggerIcons - key else triggerIcons + (key to icon),
    )

/** Renames a trigger without touching the key stored on smokes (blank clears the override). */
private fun UserPreferences.withTriggerLabel(key: String, label: String): UserPreferences =
    copy(
        triggerLabels = if (label.isBlank()) triggerLabels - key else triggerLabels + (key to label),
    )

/** Nickname + personal reason fields; commit when the field loses focus. */
@Composable
private fun PersonalizationSection(
    preferences: UserPreferences,
    enabled: Boolean,
    onChange: (UserPreferences) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PersonalizationField(
            label = stringResource(R.string.settings_nickname),
            value = preferences.nickname,
            enabled = enabled,
            onCommit = { onChange(preferences.copy(nickname = it)) },
        )
        PersonalizationField(
            label = stringResource(R.string.settings_your_reason),
            value = preferences.quitReason,
            enabled = enabled,
            onCommit = { onChange(preferences.copy(quitReason = it)) },
        )
        ToggleRow(
            label = stringResource(R.string.settings_clock_24h),
            checked = preferences.use24HourClock,
            enabled = enabled,
            onToggle = { onChange(preferences.copy(use24HourClock = it)) },
        )
        ToggleRow(
            label = stringResource(R.string.settings_week_starts_monday),
            checked = preferences.weekStartsMonday,
            enabled = enabled,
            onToggle = { onChange(preferences.copy(weekStartsMonday = it)) },
        )
        HomeFocusPicker(
            current = preferences.homeHeroChoice,
            enabled = enabled,
            onSelect = { onChange(preferences.copy(homeHeroChoice = it)) },
        )
        AccentPicker()
    }
}

/** Segmented picker for which metric the Home hero emphasizes. */
@Composable
private fun HomeFocusPicker(
    current: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    val options = listOf(
        "auto" to stringResource(R.string.settings_focus_auto),
        "count" to stringResource(R.string.settings_focus_count),
        "streak" to stringResource(R.string.settings_focus_streak),
        "money" to stringResource(R.string.settings_focus_money),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.settings_home_focus),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (key, label) ->
                FilterChip(
                    selected = current == key,
                    enabled = enabled,
                    onClick = { onSelect(key) },
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, enabled = enabled, onCheckedChange = onToggle)
    }
}

/** Accent color swatches; applied immediately and stored locally on the device. */
@Composable
private fun AccentPicker() {
    val context = LocalContext.current
    val analytics = koinInject<AnalyticsTracker>()
    val current = AccentHolder.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.settings_accent),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MobileAccent.entries.forEach { accent ->
                val swatch = accent.primary ?: MaterialTheme.colorScheme.primary
                val selected = accent == current
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            shape = CircleShape,
                        )
                        .clickable {
                            AccentHolder.set(context, accent)
                            analytics.accentChanged(accent.id)
                        },
                )
            }
        }
    }
}

@Composable
private fun PersonalizationField(
    label: String,
    value: String,
    enabled: Boolean,
    onCommit: (String) -> Unit,
) {
    var draft by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = draft,
        onValueChange = { draft = it },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { state ->
                if (!state.isFocused && draft.trim() != value) onCommit(draft.trim())
            },
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
    )
}
