package com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
                displayLoading = displayLoading,
                onSignOut = { intent(SettingsIntent.SignOut) },
                onSignInSuccess = { intent(SettingsIntent.FetchUser) },
            )

            PreferencesCard(
                preferences = preferences,
                enabled = !displayLoading && currentEmail != null,
                onSave = { intent(SettingsIntent.UpdatePreferences(it)) },
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
    SettingsCard(title = "More") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onOpenAbout),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("About", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Version, links, sharing and plan details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onOpenAbout, enabled = enabled) {
                Text("Open")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Current tier: ${currentTier.name}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SessionCard(
    currentEmail: String?,
    displayLoading: Boolean,
    onSignOut: () -> Unit,
    onSignInSuccess: () -> Unit,
) {
    SettingsCard(title = "Session") {
        if (currentEmail != null) {
            Text(
                text = currentEmail,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
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
    onSave: (UserPreferences) -> Unit,
) {
    SettingsCard(title = "Preferences") {
        val context = LocalContext.current
        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { granted ->
            val hasPermission = granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            onSave(preferences.copy(locationTrackingEnabled = hasPermission))
        }

        val packPriceText = if (preferences.packPrice == 0.0) "" else preferences.packPrice.toString()
        OutlinedTextField(
            value = packPriceText,
            onValueChange = { value ->
                onSave(
                    preferences.copy(
                        packPrice = value.toDoubleOrNull() ?: 0.0,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Pack price") },
            enabled = enabled,
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = preferences.cigarettesPerPack.toString(),
            onValueChange = { value ->
                onSave(
                    preferences.copy(
                        cigarettesPerPack = value.toIntOrNull()?.coerceAtLeast(1) ?: preferences.cigarettesPerPack,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cigarettes per pack") },
            enabled = enabled,
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = preferences.dayStartHour.toString().padStart(2, '0'),
            onValueChange = { value ->
                onSave(
                    preferences.copy(
                        dayStartHour = value.toIntOrNull()?.coerceIn(0, 23) ?: preferences.dayStartHour,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("First hour of the day") },
            enabled = enabled,
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Track location with smokes", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Optional. Used for map insights.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = preferences.locationTrackingEnabled,
                onCheckedChange = { checked ->
                    if (!checked) {
                        onSave(preferences.copy(locationTrackingEnabled = false))
                    } else if (context.hasLocationPermission()) {
                        onSave(preferences.copy(locationTrackingEnabled = true))
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
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
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
        SettingsViewState(currentEmail = "fer@gmail.com").Compose(
            intent = {},
            onOpenAbout = {},
        )
    }
}
