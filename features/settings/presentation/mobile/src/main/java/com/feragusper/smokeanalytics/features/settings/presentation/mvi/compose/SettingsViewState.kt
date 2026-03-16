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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.feragusper.smokeanalytics.features.settings.presentation.R
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.versionName
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
    internal val infoMessage: String? = null,
) : MVIViewState<SettingsIntent> {

    interface TestTags {
        companion object {
            const val BUTTON_SIGN_OUT = "buttonSignOut"
            const val BUTTON_SIGN_IN = "buttonSignIn"
        }
    }

    @Composable
    fun Compose(
        intent: (SettingsIntent) -> Unit,
        onOpenAbout: () -> Unit,
    ) {
        var draftPreferences by remember(currentEmail, preferences) { mutableStateOf(preferences) }

        LaunchedEffect(preferences, currentEmail) {
            draftPreferences = preferences
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (displayLoading && currentEmail == null) {
                SettingsShimmerContent()
                return@Column
            }

            SessionCard(
                currentEmail = currentEmail,
                currentDisplayName = currentDisplayName,
                displayLoading = displayLoading,
                onSignOut = { intent(SettingsIntent.SignOut) },
                onSignInSuccess = { intent(SettingsIntent.FetchUser) },
            )

            PreferencesCard(
                preferences = draftPreferences,
                enabled = !displayLoading && currentEmail != null,
                onPreferencesChange = { draftPreferences = it },
                onSave = { intent(SettingsIntent.UpdatePreferences(draftPreferences)) },
                onReset = { draftPreferences = preferences }
            )

            AccountTierCard(tier = preferences.accountTier)
            ActionsCard(
                currentTier = preferences.accountTier,
                onOpenAbout = onOpenAbout,
                enabled = !displayLoading,
            )

            infoMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            AppVersionFooter()
        }
    }
}

@Composable
private fun ActionsCard(
    currentTier: AccountTier,
    onOpenAbout: () -> Unit,
    enabled: Boolean,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    SettingsCard(title = "More") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Current tier", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = currentTier.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box {
                OutlinedButton(
                    onClick = { menuExpanded = true },
                    enabled = enabled,
                ) {
                    Text("More")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("About") },
                        onClick = {
                            menuExpanded = false
                            onOpenAbout()
                        }
                    )
                }
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
    SettingsCard(title = "Session") {
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
                modifier = Modifier.testTag(SettingsViewState.TestTags.BUTTON_SIGN_OUT),
                onClick = onSignOut,
                enabled = !displayLoading,
            ) {
                Text(text = stringResourceSafe(R.string.settings_logout, "Logout"))
            }
        } else {
            GoogleSignInComponent(
                modifier = Modifier.testTag(SettingsViewState.TestTags.BUTTON_SIGN_IN),
                onSignInSuccess = onSignInSuccess,
                onSignInError = {},
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

    SettingsCard(title = "Preferences") {
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
private fun AccountTierCard(tier: AccountTier) {
    SettingsCard(title = "Plan") {
        Text(
            text = "Current tier: ${tier.name}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Premium removes ads and unlocks richer insights. Billing is not enabled yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
private fun AppVersionFooter() {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        context.versionName()?.let { versionName ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = versionName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
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
    androidx.compose.ui.res.stringResource(id = id)
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
            onOpenAbout = {},
        )
    }
}
