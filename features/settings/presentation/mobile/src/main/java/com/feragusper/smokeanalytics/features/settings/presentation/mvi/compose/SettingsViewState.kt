package com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsDetailActivity
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsSection
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.SignedOutState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.design.compose.theme.AccentHolder
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsScreen
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTarget
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import org.koin.compose.koinInject
import com.feragusper.smokeanalytics.libraries.design.compose.theme.MobileAccent
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
        var signInErrorMessage by remember { mutableStateOf<String?>(null) }
        var showLogoutConfirm by remember { mutableStateOf(false) }
        val analytics = koinInject<AnalyticsTracker>()
        val context = LocalContext.current

        LaunchedEffect(currentEmail) {
            if (currentEmail != null) {
                signInErrorMessage = null
            }
        }

        // Signed-out: a full-screen centered state, consistent with the other screens (no header).
        // Appearance (theme + language) is device-local, so it's offered here too — no account needed.
        if (currentEmail == null && !displayLoading && errorMessage == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                SignedOutState(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag(SettingsViewState.TestTags.BUTTON_SIGN_IN),
                    icon = Icons.Filled.ManageAccounts,
                    title = stringResource(R.string.settings_need_account),
                    message = stringResource(R.string.settings_session_guest_body),
                    signInErrorMessage = signInErrorMessage,
                    onSignInSuccess = {
                        analytics.login()
                        intent(SettingsIntent.FetchUser)
                    },
                    onSignInError = { signInErrorMessage = it },
                )
                SettingsNavRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    icon = Icons.Filled.Contrast,
                    title = stringResource(R.string.settings_appearance),
                    subtitle = stringResource(R.string.settings_appearance_subtitle),
                    onClick = { context.openSettingsDetail(SettingsSection.APPEARANCE) },
                )
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (displayLoading && currentEmail == null && errorMessage == null) {
                SettingsShimmerContent()
                return@Column
            }

            SettingsProfileHeader(
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

            if (currentEmail != null) {
                SettingsNavRow(
                    icon = Icons.Filled.Tune,
                    title = stringResource(R.string.settings_preferences),
                    subtitle = stringResource(R.string.settings_preferences_subtitle),
                    onClick = { context.openSettingsDetail(SettingsSection.PREFERENCES) },
                )
                SettingsNavRow(
                    icon = Icons.Filled.Palette,
                    title = stringResource(R.string.settings_make_it_yours),
                    subtitle = stringResource(R.string.settings_personalization_subtitle),
                    onClick = { context.openSettingsDetail(SettingsSection.PERSONALIZATION) },
                )
                SettingsNavRow(
                    icon = Icons.Filled.Sell,
                    title = stringResource(R.string.settings_manage_triggers),
                    subtitle = stringResource(R.string.settings_manage_triggers_subtitle),
                    onClick = { context.openSettingsDetail(SettingsSection.TRIGGERS) },
                )
            }

            SettingsNavRow(
                icon = Icons.Filled.Contrast,
                title = stringResource(R.string.settings_appearance),
                subtitle = stringResource(R.string.settings_appearance_subtitle),
                onClick = { context.openSettingsDetail(SettingsSection.APPEARANCE) },
            )

            SettingsNavRow(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.settings_about_support),
                subtitle = stringResource(R.string.settings_about_subtitle),
                onClick = { context.openSettingsDetail(SettingsSection.ABOUT) },
            )

            if (currentEmail != null) {
                SettingsLogoutRow(
                    enabled = !displayLoading,
                    onClick = { showLogoutConfirm = true },
                )
            }

            if (showLogoutConfirm) {
                AlertDialog(
                    onDismissRequest = { showLogoutConfirm = false },
                    title = { Text(stringResource(R.string.settings_logout_confirm_title)) },
                    text = { Text(stringResource(R.string.settings_logout_confirm_body)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutConfirm = false
                            analytics.logout()
                            intent(SettingsIntent.SignOut)
                        }) { Text(stringResource(R.string.settings_logout)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutConfirm = false }) {
                            Text(stringResource(R.string.settings_cancel))
                        }
                    },
                )
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

private fun android.content.Context.openSettingsDetail(section: SettingsSection) {
    startActivity(SettingsDetailActivity.intent(this, section))
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

/** Profile header: avatar + name + email, merging the old session summary into the header. */
@Composable
private fun SettingsProfileHeader(
    displayLoading: Boolean,
    currentEmail: String?,
    currentDisplayName: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsAvatar(name = currentDisplayName, email = currentEmail)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (currentDisplayName.isNullOrBlank()) stringResource(R.string.settings_you) else currentDisplayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = currentEmail ?: stringResource(R.string.settings_sign_in_subtitle),
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
}

/** Circular avatar showing the account initials (Google photo is a future enhancement). */
@Composable
private fun SettingsAvatar(name: String?, email: String?) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = sessionInitials(name, email),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/** A tappable settings entry that opens its own screen. */
@Composable
private fun SettingsNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Logout, styled as an error-tinted row at the bottom of the list. */
@Composable
private fun SettingsLogoutRow(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag(SettingsViewState.TestTags.BUTTON_SIGN_OUT)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = stringResource(R.string.settings_logout),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}



@Composable
internal fun PreferencesSection(
    preferences: UserPreferences,
    enabled: Boolean,
    onPreferencesChange: (UserPreferences) -> Unit,
    onSave: (UserPreferences) -> Unit,
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PreferenceValueRow(
            icon = Icons.Filled.Schedule,
            title = stringResource(R.string.settings_day_starts),
            value = "${preferences.dayStartHour.toString().padStart(2, '0')}:00",
            enabled = enabled,
            onClick = {
                analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_DAY_START)
                showDayStartPicker = true
            },
        )
        PreferenceValueRow(
            icon = Icons.Filled.Bedtime,
            title = stringResource(R.string.settings_sleep_starts),
            value = "${preferences.bedtimeHour.toString().padStart(2, '0')}:00",
            enabled = enabled,
            onClick = {
                analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_BEDTIME)
                showBedtimePicker = true
            },
        )
        PreferenceValueRow(
            icon = Icons.Filled.Payments,
            title = stringResource(R.string.settings_currency),
            value = preferences.currencySymbol,
            enabled = enabled,
            onClick = {
                analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_CURRENCY)
                showCurrencyPicker = true
            },
        )
        PreferenceValueRow(
            icon = Icons.Filled.LocalOffer,
            title = stringResource(R.string.settings_pack_price),
            value = "${preferences.currencySymbol}${"%.2f".format(preferences.packPrice)}",
            enabled = enabled,
            onClick = {
                analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_PACK_PRICE)
                showPackPricePicker = true
            },
        )
        PreferenceValueRow(
            icon = Icons.Filled.Numbers,
            title = stringResource(R.string.settings_cigs_per_pack),
            value = preferences.cigarettesPerPack.toString(),
            enabled = enabled,
            onClick = {
                analytics.buttonTap(AnalyticsScreen.SETTINGS, AnalyticsTarget.CHANGE_CIGS_PER_PACK)
                showCigsPerPackPicker = true
            },
        )
        PreferenceToggleRow(
            icon = if (preferences.locationTrackingEnabled) Icons.Filled.LocationOn else Icons.Filled.LocationOff,
            title = stringResource(R.string.settings_location_tracking),
            checked = preferences.locationTrackingEnabled,
            enabled = enabled,
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

/** A preference shown as a row: icon + label on the left, current value + chevron on the right. */
@Composable
private fun PreferenceValueRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A preference shown as a row with a trailing switch. */
@Composable
private fun PreferenceToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onToggle, enabled = enabled)
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
internal fun ManageTriggersSection(
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
internal fun PersonalizationSection(
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
        AccentPicker()
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
